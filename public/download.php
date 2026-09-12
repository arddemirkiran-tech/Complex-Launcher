<?php
// Complex Launcher - always-latest download via GitHub releases API
$wantZip = isset($_GET['file']) && $_GET['file'] === 'zip';
$ctx = stream_context_create(['http' => ['timeout' => 8, 'header' => "User-Agent: CL-Site
"]]);
$url = 'https://github.com/arddemirkiran-tech/Complex-Launcher/releases/latest';
$meta = @file_get_contents('https://api.github.com/repos/arddemirkiran-tech/Complex-Launcher/releases/latest', false, $ctx);
if ($meta) {
    $j = json_decode($meta, true);
    if (!empty($j['assets'])) {
        // oncelik: Setup.exe (installer) isteniyorsa exe, zip isteniyorsa Windows zip
        foreach ($j['assets'] as $a) {
            $n = isset($a['name']) ? $a['name'] : '';
            $ok = $wantZip ? (stripos($n, 'Windows') !== false && substr($n, -4) === '.zip')
                           : (stripos($n, 'Setup') !== false && substr($n, -4) === '.exe');
            if ($ok) { $url = $a['browser_download_url']; break; }
        }
        // exe asset'i yayinda yoksa Windows zip'e dus
        if (!$wantZip && $url === 'https://github.com/arddemirkiran-tech/Complex-Launcher/releases/latest') {
            foreach ($j['assets'] as $a) {
                $n = isset($a['name']) ? $a['name'] : '';
                if (stripos($n, 'Windows') !== false && substr($n, -4) === '.zip') { $url = $a['browser_download_url']; break; }
            }
        }
    }
}
header('Location: ' . $url);
exit;
