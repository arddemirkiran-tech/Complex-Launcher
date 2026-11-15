<?php
/**
 * Complex Launcher - Community Suggestions API
 * ------------------------------------------------------------------
 * InfinityFree (PHP) uzerinde calisir. Veritabani GEREKMEZ: oneriler
 * data/suggestions.json icinde saklanir. Launcher (v46 -> Ayarlar >
 * Oneri & Geri Bildirim) ve web sitesi ayni ucu kullanir.
 *
 * Uclar (endpoint):
 *   GET  api.php?action=list&limit=60        -> yayinlanan oneriler
 *   POST api.php?action=submit               -> yeni oneri (JSON veya form)
 *   POST api.php?action=delete&key=ADMIN&id= -> moderasyon (ADMIN_KEY gerekir)
 *   (action verilmezse: GET=list, POST=submit)
 *
 * POST govdesi (JSON veya application/x-www-form-urlencoded):
 *   text     : 4-1000 karakter  (zorunlu)
 *   name     : 0-40 karakter    (opsiyonel, bos = "Anonymous")
 *   category : idea|bug|feature|mod|other
 *   lang     : tr|en            (launcher'in dili, sadece bilgi amacli)
 *   version  : launcher surumu  (or. "46")
 *   platform : windows|linux|macos|other
 *   website  : HONEYPOT - bos birakilmali, doluysa kayit sessizce atilir
 *
 * Kurulum: bu dosyayi index.html'in yanina (htdocs kokune) kopyala.
 * Yazma izni data/ klasorune PHP tarafindan otomatik verilir; vermezse
 * vPanel'den data/ klasorunun iznini 755 yap.
 */

// ---------------------------------------------------------------------
// AYARLAR
// ---------------------------------------------------------------------

/** Moderasyon anahtari. Bos ise "delete" kapalidir. Bir seyler yaz
 *  (or. rastgele 24 karakter) sonra: api.php?action=delete&key=...&id=... */
const ADMIN_KEY = '';

const DATA_DIR        = __DIR__ . '/data';
const SUGGESTIONS_FILE = DATA_DIR . '/suggestions.json';
const RATE_FILE        = DATA_DIR . '/rate.json';
const LOCK_FILE        = DATA_DIR . '/api.lock';

const TEXT_MIN      = 4;
const TEXT_MAX      = 1000;
const NAME_MAX      = 40;
const MAX_STORED    = 400;   // en yeni 400 oneri tutulur
const RATE_WINDOW   = 3600;  // saniye
const RATE_MAX_HOUR = 12;    // ayni IP'den saatte en fazla
const RATE_MIN_GAP  = 20;    // ayni IP'den iki oneri arasi en az saniye

const CATEGORIES = array('idea', 'bug', 'feature', 'mod', 'other');
const PLATFORMS  = array('windows', 'linux', 'macos', 'other');

// ---------------------------------------------------------------------
// Yardimcilar
// ---------------------------------------------------------------------

function respond($payload, $status = 200)
{
    http_response_code($status);
    header('Content-Type: application/json; charset=utf-8');
    header('Cache-Control: no-store, no-cache, must-revalidate');
    header('X-Robots-Tag: noindex');
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, Accept');
    echo json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

function fail($code, $message, $status = 400, $extra = array())
{
    respond(array_merge(array('ok' => false, 'error' => $code, 'message' => $message), $extra), $status);
}

function ensure_data_dir()
{
    if (!is_dir(DATA_DIR)) {
        @mkdir(DATA_DIR, 0755, true);
    }
    if (!is_dir(DATA_DIR)) {
        fail('storage_unavailable', 'Storage directory could not be created.', 500);
    }
    // data/ klasoru dogrudan webden okunmasin (oneriler zaten kamuya acik
    // ama yazma dosyalari ve IP hashl'ari disaridan gorunmesin).
    $htaccess = DATA_DIR . '/.htaccess';
    if (!file_exists($htaccess)) {
        @file_put_contents($htaccess, "Require all denied\n<IfModule !mod_authz_core.c>\nOrder allow,deny\nDeny from all\n</IfModule>\n");
    }
    if (!file_exists(SUGGESTIONS_FILE)) {
        @file_put_contents(SUGGESTIONS_FILE, json_encode(array('nextId' => 1, 'items' => array())));
    }
}

/** Kilit altinda oku-degistir-yaz. */
function with_lock($callback)
{
    ensure_data_dir();
    $fh = @fopen(LOCK_FILE, 'c');
    if ($fh === false) {
        fail('storage_unavailable', 'Could not open the storage lock.', 500);
    }
    if (!flock($fh, LOCK_EX)) {
        fclose($fh);
        fail('storage_busy', 'Storage is busy, please try again.', 503);
    }
    try {
        $result = $callback();
    } finally {
        flock($fh, LOCK_UN);
        fclose($fh);
    }
    return $result;
}

function read_store()
{
    $raw = @file_get_contents(SUGGESTIONS_FILE);
    $data = json_decode($raw === false ? '' : $raw, true);
    if (!is_array($data) || !isset($data['items']) || !is_array($data['items'])) {
        $data = array('nextId' => 1, 'items' => array());
    }
    if (!isset($data['nextId'])) {
        $data['nextId'] = count($data['items']) + 1;
    }
    return $data;
}

function write_store($data)
{
    $ok = @file_put_contents(
        SUGGESTIONS_FILE,
        json_encode($data, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
        LOCK_EX
    );
    if ($ok === false) {
        fail('storage_unavailable', 'Could not write the storage file.', 500);
    }
}

function client_ip()
{
    if (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) {
        $parts = explode(',', $_SERVER['HTTP_X_FORWARDED_FOR']);
        $candidate = trim($parts[0]);
        if ($candidate !== '') {
            return $candidate;
        }
    }
    return isset($_SERVER['REMOTE_ADDR']) ? $_SERVER['REMOTE_ADDR'] : '0.0.0.0';
}

/** Rate-limit icin IP hashi - ham IP hicbir zaman diske yazilmaz. */
function ip_hash()
{
    $salt = 'complex-launcher-suggestions';
    if (defined('ADMIN_KEY') && ADMIN_KEY !== '') {
        $salt .= '|' . ADMIN_KEY;
    }
    return substr(hash('sha256', client_ip() . '|' . $salt), 0, 24);
}

function clean_text($value, $max)
{
    $value = (string) $value;
    $value = str_replace(array("\r\n", "\r"), "\n", $value);
    // Kontrol karakterlerini (tab ve \n haric) temizle.
    $value = preg_replace('/[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/u', '', $value);
    // Ust uste 3+ bos satiri 2'ye indir.
    $value = preg_replace("/\n{3,}/", "\n\n", $value);
    $value = trim($value);
    if (function_exists('mb_substr')) {
        $value = mb_substr($value, 0, $max, 'UTF-8');
    } else {
        $value = substr($value, 0, $max);
    }
    return $value;
}

function one_of($value, $allowed, $fallback)
{
    $value = strtolower(trim((string) $value));
    return in_array($value, $allowed, true) ? $value : $fallback;
}

function request_body()
{
    $raw = file_get_contents('php://input');
    $body = array();
    if ($raw !== false && $raw !== '') {
        $decoded = json_decode($raw, true);
        if (is_array($decoded)) {
            $body = $decoded;
        } else {
            $parsed = array();
            parse_str($raw, $parsed);
            $body = $parsed;
        }
    }
    // Form POST'unda govde ayri gelir, GET parametreleriyle birlestir.
    return array_merge($_POST, $_GET, $body);
}

/** Rate limit: hem minimum aralik hem saatlik kota. */
function rate_check_and_record()
{
    ensure_data_dir();
    $hash = ip_hash();
    $now = time();
    $fh = @fopen(RATE_FILE, 'c+');
    if ($fh === false) {
        return; // rate limit kurulamadiysa gonderimi engellemeyelim
    }
    if (!flock($fh, LOCK_EX)) {
        fclose($fh);
        return;
    }
    $raw = stream_get_contents($fh);
    $data = json_decode($raw === false ? '' : $raw, true);
    if (!is_array($data)) {
        $data = array();
    }
    $stamps = isset($data[$hash]) && is_array($data[$hash]) ? $data[$hash] : array();
    $recent = array();
    foreach ($stamps as $ts) {
        if (is_int($ts) && ($now - $ts) < RATE_WINDOW) {
            $recent[] = $ts;
        }
    }
    sort($recent);

    if (count($recent) > 0) {
        $last = $recent[count($recent) - 1];
        if (($now - $last) < RATE_MIN_GAP) {
            $wait = RATE_MIN_GAP - ($now - $last);
            flock($fh, LOCK_UN);
            fclose($fh);
            fail('rate_limited', 'Please wait a moment before sending another suggestion.', 429, array('retry_after' => $wait));
        }
    }
    if (count($recent) >= RATE_MAX_HOUR) {
        $wait = RATE_WINDOW - ($now - $recent[0]);
        flock($fh, LOCK_UN);
        fclose($fh);
        fail('rate_limited', 'Too many suggestions from your connection. Try again later.', 429, array('retry_after' => max(1, $wait)));
    }

    $recent[] = $now;
    $data[$hash] = $recent;
    // Baska IP'lerin kayitlarini ucur (kayit sonsuz buyumesin).
    $kept = 0;
    foreach ($data as $key => $list) {
        if ($key === $hash) {
            $kept++;
            continue;
        }
        if (!is_array($list)) {
            unset($data[$key]);
            continue;
        }
        $alive = array();
        foreach ($list as $ts) {
            if (is_int($ts) && ($now - $ts) < RATE_WINDOW) {
                $alive[] = $ts;
            }
        }
        if (count($alive) === 0) {
            unset($data[$key]);
        } else {
            $data[$key] = $alive;
            $kept++;
        }
        if ($kept > 500) {
            break;
        }
    }
    ftruncate($fh, 0);
    rewind($fh);
    fwrite($fh, json_encode($data));
    fflush($fh);
    flock($fh, LOCK_UN);
    fclose($fh);
}

function public_item($item)
{
    return array(
        'id'       => isset($item['id']) ? $item['id'] : 0,
        'name'     => isset($item['name']) && $item['name'] !== '' ? $item['name'] : null,
        'category' => isset($item['category']) ? $item['category'] : 'other',
        'text'     => isset($item['text']) ? $item['text'] : '',
        'lang'     => isset($item['lang']) ? $item['lang'] : 'en',
        'version'  => isset($item['version']) ? $item['version'] : '',
        'platform' => isset($item['platform']) ? $item['platform'] : 'other',
        'time'     => isset($item['time']) ? $item['time'] : 0,
        'iso'      => isset($item['time']) ? gmdate('c', (int) $item['time']) : '',
    );
}

// ---------------------------------------------------------------------
// Islemler
// ---------------------------------------------------------------------

function action_list()
{
    $limit = isset($_GET['limit']) ? (int) $_GET['limit'] : 60;
    if ($limit < 1) {
        $limit = 1;
    }
    if ($limit > 200) {
        $limit = 200;
    }
    $store = with_lock(function () {
        return read_store();
    });
    $items = array_reverse($store['items']);
    $out = array();
    foreach ($items as $item) {
        if (count($out) >= $limit) {
            break;
        }
        $out[] = public_item($item);
    }
    respond(array(
        'ok'          => true,
        'action'      => 'list',
        'total'       => count($store['items']),
        'count'       => count($out),
        'suggestions' => $out,
    ));
}

function action_submit()
{
    $body = request_body();

    // Honeypot: botlar bu gizli alani doldurur. Kullaniciya basarili gibi
    // gorunur ama hicbir sey kaydedilmez.
    if (isset($body['website']) && trim((string) $body['website']) !== '') {
        respond(array('ok' => true, 'action' => 'submit', 'id' => 0, 'stored' => false));
    }

    $text = isset($body['text']) ? clean_text($body['text'], TEXT_MAX) : '';
    if ($text === '' || mb_strlen_safe($text) < TEXT_MIN) {
        fail('text_too_short', 'Please write at least ' . TEXT_MIN . ' characters.', 400);
    }

    $name = isset($body['name']) ? clean_text($body['name'], NAME_MAX) : '';
    $category = one_of(isset($body['category']) ? $body['category'] : '', CATEGORIES, 'other');
    $lang = one_of(isset($body['lang']) ? $body['lang'] : '', array('tr', 'en'), 'en');
    $platform = one_of(isset($body['platform']) ? $body['platform'] : '', PLATFORMS, 'other');
    $version = isset($body['version']) ? clean_text($body['version'], 16) : '';

    rate_check_and_record();

    $newId = 0;
    $total = 0;
    with_lock(function () use ($text, $name, $category, $lang, $version, $platform, &$newId, &$total) {
        $store = read_store();
        $id = (int) $store['nextId'];
        if ($id < 1) {
            $id = 1;
        }
        $store['nextId'] = $id + 1;
        // Metin HAM saklanir (HTML kacisi YAPILMAZ): JSON'u tuketen taraf
        // metni her zaman textContent ile yazmali. Kayit asamasinda
        // kacislamak, site tarafinda &#039; gibi bozuk gorunume yol acardi.
        $store['items'][] = array(
            'id'       => $id,
            'name'     => $name,
            'category' => $category,
            'text'     => $text,
            'lang'     => $lang,
            'version'  => $version,
            'platform' => $platform,
            'time'     => time(),
        );
        if (count($store['items']) > MAX_STORED) {
            $store['items'] = array_slice($store['items'], count($store['items']) - MAX_STORED);
        }
        write_store($store);
        $newId = $id;
        $total = count($store['items']);
    });

    respond(array('ok' => true, 'action' => 'submit', 'id' => $newId, 'stored' => true, 'total' => $total));
}

function mb_strlen_safe($value)
{
    return function_exists('mb_strlen') ? mb_strlen($value, 'UTF-8') : strlen($value);
}

function action_delete()
{
    if (ADMIN_KEY === '') {
        fail('delete_disabled', 'Moderation is disabled: set ADMIN_KEY in api.php first.', 403);
    }
    $body = request_body();
    $key = isset($body['key']) ? (string) $body['key'] : '';
    if ($key === '' || !hash_equals(ADMIN_KEY, $key)) {
        fail('bad_key', 'Invalid moderation key.', 403);
    }
    $id = isset($body['id']) ? (int) $body['id'] : 0;
    if ($id < 1) {
        fail('missing_id', 'Provide the id of the suggestion to delete.', 400);
    }
    $removed = false;
    $left = 0;
    with_lock(function () use ($id, &$removed, &$left) {
        $store = read_store();
        $kept = array();
        foreach ($store['items'] as $item) {
            if ((int) $item['id'] === $id && !$removed) {
                $removed = true;
                continue;
            }
            $kept[] = $item;
        }
        $store['items'] = $kept;
        write_store($store);
        $left = count($kept);
    });
    if (!$removed) {
        fail('not_found', 'No suggestion with that id.', 404);
    }
    respond(array('ok' => true, 'action' => 'delete', 'id' => $id, 'total' => $left));
}

// ---------------------------------------------------------------------
// Yonlendirme
// ---------------------------------------------------------------------

if (isset($_SERVER['REQUEST_METHOD']) && $_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    respond(array('ok' => true));
}

$method = isset($_SERVER['REQUEST_METHOD']) ? strtoupper($_SERVER['REQUEST_METHOD']) : 'GET';
$action = isset($_GET['action']) ? strtolower(trim($_GET['action'])) : '';
if ($action === '') {
    $action = $method === 'POST' ? 'submit' : 'list';
}

switch ($action) {
    case 'list':
        action_list();
        break;
    case 'submit':
        if ($method !== 'POST') {
            fail('method_not_allowed', 'Send suggestions with POST.', 405);
        }
        action_submit();
        break;
    case 'delete':
        action_delete();
        break;
    default:
        fail('unknown_action', 'Unknown action: ' . $action, 404);
}
