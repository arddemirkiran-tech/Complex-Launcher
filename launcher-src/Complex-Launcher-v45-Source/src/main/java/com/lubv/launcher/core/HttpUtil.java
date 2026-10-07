/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public final class HttpUtil {
    private HttpUtil() {
    }

    public static String getText(String url) throws IOException {
        return HttpUtil.getText(url, null);
    }

    /**
     * Same as getText(url, null) but retries transient transport failures
     * (timeout / connection reset / 5xx) so a single API hiccup doesn't kill
     * a mods-panel install. 4xx client errors throw immediately.
     */
    public static String getTextWithRetry(String url, int maxAttempts) throws IOException {
        return HttpUtil.getTextWithRetry(url, null, maxAttempts);
    }

    /**
     * Retry variant that passes an API key header (CurseForge). Same policy:
     * transient transport/infra failures are retried with backoff, real 4xx
     * client errors fail fast on the first attempt.
     */
    public static String getTextWithRetry(String url, String apiKey, int maxAttempts) throws IOException {
        IOException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return HttpUtil.getText(url, apiKey);
            } catch (IOException e) {
                last = e;
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(350L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw last != null ? last : new IOException("get failed after retries: " + url);
    }

    public static String getText(String url, String apiKey) throws IOException {
        HttpURLConnection conn = HttpUtil.open(url, "GET");
        try {
            String string;
            block10: {
                conn.setRequestProperty("Accept", "application/json");
                if (apiKey != null && !apiKey.isEmpty()) {
                    conn.setRequestProperty("x-api-key", apiKey);
                }
                InputStream in = conn.getInputStream();
                try {
                    string = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    if (in == null) break block10;
                }
                catch (Throwable throwable) {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                in.close();
            }
            return string;
        }
        finally {
            conn.disconnect();
        }
    }

    public static JsonObject getJson(String url) throws IOException {
        return JsonParser.parseString(HttpUtil.getText(url)).getAsJsonObject();
    }

    public static JsonObject getJson(String url, String apiKey) throws IOException {
        return JsonParser.parseString(HttpUtil.getText(url, apiKey)).getAsJsonObject();
    }

    public static byte[] getBytes(String url) throws IOException {
        return HttpUtil.getBytes(url, 15000, 60000);
    }

    /** Kisa zaman asili GET - ceviri istekleri icin (hizli basarisizlik, hizli yedek saglayiciya gecis). */
    private static byte[] getBytes(String url, int connectTimeoutMs, int readTimeoutMs) throws IOException {
        HttpURLConnection conn = HttpUtil.open(url, "GET", connectTimeoutMs, readTimeoutMs);
        try {
            byte[] byArray;
            block9: {
                InputStream in = conn.getInputStream();
                try {
                    byArray = in.readAllBytes();
                    if (in == null) break block9;
                }
                catch (Throwable throwable) {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                in.close();
            }
            return byArray;
        }
        finally {
            conn.disconnect();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static JsonObject getJsonAuthAllowError(String url, String bearerToken) throws IOException {
        HttpURLConnection conn = HttpUtil.open(url, "GET");
        try {
            conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
            conn.setRequestProperty("Accept", "application/json");
            JsonObject jsonObject = HttpUtil.readJsonAllowError(conn);
            return jsonObject;
        }
        finally {
            HttpUtil.safeDisconnect(conn);
        }
    }

    public static JsonObject postForm(String url, String formBody) throws IOException {
        HttpURLConnection conn = HttpUtil.open(url, "POST");
        try {
            JsonObject jsonObject;
            block9: {
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Accept", "application/json");
                HttpUtil.writeBody(conn, formBody.getBytes(StandardCharsets.UTF_8));
                InputStream in = conn.getInputStream();
                try {
                    jsonObject = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                    if (in == null) break block9;
                }
                catch (Throwable throwable) {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                in.close();
            }
            return jsonObject;
        }
        finally {
            HttpUtil.safeDisconnect(conn);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static JsonObject postFormAllowError(String url, String formBody) throws IOException {
        HttpURLConnection conn = HttpUtil.open(url, "POST");
        try {
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/json");
            HttpUtil.writeBody(conn, formBody.getBytes(StandardCharsets.UTF_8));
            JsonObject jsonObject = HttpUtil.readJsonAllowError(conn);
            return jsonObject;
        }
        finally {
            HttpUtil.safeDisconnect(conn);
        }
    }

    public static JsonObject postJson(String url, JsonObject body) throws IOException {
        HttpURLConnection conn = HttpUtil.open(url, "POST");
        try {
            JsonObject jsonObject;
            block9: {
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                HttpUtil.writeBody(conn, body.toString().getBytes(StandardCharsets.UTF_8));
                InputStream in = conn.getInputStream();
                try {
                    jsonObject = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                    if (in == null) break block9;
                }
                catch (Throwable throwable) {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                in.close();
            }
            return jsonObject;
        }
        finally {
            HttpUtil.safeDisconnect(conn);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static JsonObject postJsonAllowError(String url, JsonObject body) throws IOException {
        HttpURLConnection conn = HttpUtil.open(url, "POST");
        try {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            HttpUtil.writeBody(conn, body.toString().getBytes(StandardCharsets.UTF_8));
            JsonObject jsonObject = HttpUtil.readJsonAllowError(conn);
            return jsonObject;
        }
        finally {
            HttpUtil.safeDisconnect(conn);
        }
    }

    private static JsonObject readJsonAllowError(HttpURLConnection conn) throws IOException {
        InputStream stream;
        block8: {
            try {
                stream = conn.getInputStream();
            }
            catch (IOException e) {
                stream = conn.getErrorStream();
                if (stream != null) break block8;
                throw e;
            }
        }
        try (InputStream in = stream;){
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(text).getAsJsonObject();
            return jsonObject;
        }
    }

    private static void writeBody(HttpURLConnection conn, byte[] data) throws IOException {
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream();){
            os.write(data);
        }
    }

    private static void safeDisconnect(HttpURLConnection conn) {
        if (conn != null) {
            try {
                conn.disconnect();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private static HttpURLConnection open(String url, String method) throws IOException {
        return HttpUtil.open(url, method, 15000, 60000);
    }

    /** Kisa zaman asili acilis - ceviri istekleri icin (hizli basarisizlik, hizli yedek saglayiciya gecis). */
    private static HttpURLConnection open(String url, String method, int connectTimeoutMs, int readTimeoutMs) throws IOException {
        HttpURLConnection conn = (HttpURLConnection)URI.create(url).toURL().openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(connectTimeoutMs);
        conn.setReadTimeout(readTimeoutMs);
        conn.setInstanceFollowRedirects(true);
        // Bazi CDN'ler (ozellikle CurseForge/Modrinth ikon sunuculari) eksik
        // User-Agent / Accept / Referer basliklariyla gelen isteklere 403
        // donduruyor. Gercekci bir tarayici istegi gibi gorunmesi icin
        // baslıklari genisletiyoruz - bu, ikonlarin inmemesi sorununu cozer.
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Complex-Launcher/2.0 (+https://github.com)");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Accept-Language", "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7");
        // ONEMLI DUZELTME: "Referer: modrinth.com" onceden HER istege
        // (Google Translate API'si dahil) sabit olarak ekleniyordu. Bazi
        // servisler (Google Translate'in resmi olmayan endpoint'i gibi)
        // beklenmeyen/yaniltici bir Referer gorunce istegi sessizce
        // reddediyor - bu da "Turkce'ye cevirme calismiyor" sikayetinin
        // kok sebebiydi (translateText hata alip orijinal metni geri
        // donduruyordu, kullaniciya hicbir hata gorunmuyordu). Artik
        // Referer sadece gercekten Modrinth/CurseForge host'larina
        // giden isteklerde gonderiliyor.
        if (url.contains("modrinth.com") || url.contains("curseforge.com")) {
            conn.setRequestProperty("Referer", "https://modrinth.com/");
        }
        conn.setRequestProperty("Connection", "keep-alive");
        return conn;
    }

    /**
     * getBytes ile ayni, fakat gecici hatalarda (timeout, 5xx, baglanti
     * kopmasi vb.) otomatik olarak yeniden dener. Ikon/resim indirmede
     * kullanilir; boylece ilk denemede basarisiz olan bir CDN istegi
     * kalici olarak "bozuk" isaretlenmez.
     */
    public static byte[] getBytesWithRetry(String url, int maxAttempts) throws IOException {
        IOException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return HttpUtil.getBytes(url);
            } catch (IOException e) {
                last = e;
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(300L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw last != null ? last : new IOException("download failed: " + url);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    // Kucuk buffer (eskiden 8KB) indirme hizini onemli olcude
    // dusuruyordu - ozellikle yuksek gecikmeli/yuksek bant genisligi
    // baglantilarda (Modrinth/CurseForge CDN'leri gibi). 256KB'a cikarildi.
    private static final int DOWNLOAD_BUFFER_SIZE = 262144;
    // Paralel parca indirme icin esik: bu boyutun ustundeki dosyalar
    // (sunucu Range destekliyorsa) birden fazla es zamanli baglantiyla
    // parca parca indirilir - bu, tek bir TCP baglantisinin bant
    // genisligi tavanina tikanmasini onler ve genellikle 2-4x daha
    // hizli indirme saglar.
    private static final long PARALLEL_DOWNLOAD_THRESHOLD = 8L * 1024 * 1024; // 8MB
    private static final int PARALLEL_CHUNK_COUNT = 4;
    private static final ExecutorService DOWNLOAD_POOL = Executors.newFixedThreadPool(PARALLEL_CHUNK_COUNT, r -> {
        Thread t = new Thread(r, "parallel-download");
        t.setDaemon(true);
        return t;
    });

    public static void downloadFile(String url, File dest, ByteProgress progress) throws IOException {
        HttpUtil.downloadFile(url, dest, progress, null);
    }

    /**
     * Cancellable download. When {@code cancelled} flips to true the streams
     * stop at the next buffer read and an IOException is raised - a
     * half-downloaded file never reaches dest (it stays in .part, which is
     * deleted here).
     */
    public static void downloadFile(String url, File dest, ByteProgress progress, java.util.concurrent.atomic.AtomicBoolean cancelled) throws IOException {
        if (cancelled != null && cancelled.get()) {
            throw new IOException("Iptal edildi");
        }
        dest.getParentFile().mkdirs();
        File tmp = new File(dest.getParentFile(), dest.getName() + ".part");
        // Once sunucunun Content-Length'ini ve Range destegini HEAD/GET
        // ile ogrenmeye calisiyoruz. Yeterince buyuk ve Range destekli
        // bir dosyaysa paralel parcali indirmeye geciyoruz.
        long contentLength = -1L;
        boolean acceptsRanges = false;
        try {
            HttpURLConnection probe = HttpUtil.open(url, "HEAD");
            try {
                contentLength = probe.getContentLengthLong();
                String ranges = probe.getHeaderField("Accept-Ranges");
                acceptsRanges = ranges != null && ranges.toLowerCase().contains("bytes");
            } finally {
                HttpUtil.safeDisconnect(probe);
            }
        } catch (Exception ignored) {
            // HEAD desteklenmiyor olabilir - asagida tek-akis indirmeye dusulur
        }

        if (acceptsRanges && contentLength >= PARALLEL_DOWNLOAD_THRESHOLD) {
            try {
                HttpUtil.downloadFileParallel(url, tmp, contentLength, progress, cancelled);
                Files.move(tmp.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return;
            } catch (Exception parallelFailure) {
                // Paralel indirme basarisiz olursa (baglanti kesintisi,
                // sunucunun range'i yarim yolda reddetmesi vb.) sessizce
                // tek-akis moduna geri don - kullanici hata gormemeli.
                // Iptal edildiyse tek-akis hemen yine iptal hatasi firlatacak.
                tmp.delete();
            }
        }
        try {
            HttpUtil.downloadFileSingleStream(url, tmp, progress, cancelled);
            Files.move(tmp.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Yarim kalan/iptal edilen indirme hedef klasorde .part birikmesin
            tmp.delete();
            throw e;
        }
    }

    private static void downloadFileSingleStream(String url, File tmp, ByteProgress progress, java.util.concurrent.atomic.AtomicBoolean cancelled) throws IOException {
        HttpURLConnection conn = HttpUtil.open(url, "GET");
        try {
            long total = conn.getContentLengthLong();
            try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream(), DOWNLOAD_BUFFER_SIZE);
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmp), DOWNLOAD_BUFFER_SIZE);){
                int read;
                byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
                long downloaded = 0L;
                while ((read = ((InputStream)in).read(buffer)) != -1) {
                    if (cancelled != null && cancelled.get()) {
                        throw new IOException("Iptal edildi");
                    }
                    ((OutputStream)out).write(buffer, 0, read);
                    downloaded += (long)read;
                    if (progress == null) continue;
                    progress.onProgress(downloaded, total);
                }
            }
        }
        finally {
            HttpUtil.safeDisconnect(conn);
        }
    }

    /**
     * Dosyayi PARALLEL_CHUNK_COUNT adet es zamanli HTTP Range istegiyle
     * parcalara bolerek indirir, her parcayi RandomAccessFile ile dogru
     * offsetine yazar. Tum parcalar bitince dosya tek parca halinde
     * hazir olur. Herhangi bir parca basarisiz olursa exception firlar
     * ve cagiran taraf tek-akis moduna geri doner.
     */
    private static void downloadFileParallel(String url, File tmp, long totalSize, ByteProgress progress, java.util.concurrent.atomic.AtomicBoolean cancelled) throws Exception {
        long chunkSize = totalSize / PARALLEL_CHUNK_COUNT;
        AtomicLong totalDownloaded = new AtomicLong(0);
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            raf.setLength(totalSize);
        }
        Future<?>[] futures = new Future<?>[PARALLEL_CHUNK_COUNT];
        for (int i = 0; i < PARALLEL_CHUNK_COUNT; i++) {
            long start = i * chunkSize;
            long end = (i == PARALLEL_CHUNK_COUNT - 1) ? totalSize - 1 : (start + chunkSize - 1);
            final long chunkStart = start;
            final long chunkEnd = end;
            futures[i] = DOWNLOAD_POOL.submit(() -> {
                HttpURLConnection conn = HttpUtil.open(url, "GET");
                conn.setRequestProperty("Range", "bytes=" + chunkStart + "-" + chunkEnd);
                try {
                    int code = conn.getResponseCode();
                    if (code != 206 && code != 200) {
                        throw new IOException("Beklenmeyen HTTP durumu (Range istegi): " + code);
                    }
                    try (InputStream in = new BufferedInputStream(conn.getInputStream(), DOWNLOAD_BUFFER_SIZE);
                         RandomAccessFile out = new RandomAccessFile(tmp, "rw")) {
                        out.seek(chunkStart);
                        byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            if (cancelled != null && cancelled.get()) {
                                throw new IOException("Iptal edildi");
                            }
                            out.write(buffer, 0, read);
                            long done = totalDownloaded.addAndGet(read);
                            if (progress != null) {
                                progress.onProgress(done, totalSize);
                            }
                        }
                    }
                    return null;
                } finally {
                    HttpUtil.safeDisconnect(conn);
                }
            });
        }
        for (Future<?> f : futures) {
            f.get(); // herhangi bir parca hata fırlatirsa burada ExecutionException olarak yukari tasar
        }
    }

    public static void downloadFile(String url, File dest) throws IOException {
        HttpUtil.downloadFile(url, dest, null);
    }

    /**
     * Indir + SHA-1 dogrula. Modrinth/CurseForge her dosyanin hash'ini
     * verir; kopuk/bozuk indirme artik hedef klasore asla ulasamaz:
     *  - hash biliniyorsa uymayan dosya silinir, indirme 2 kez daha denenir
     *  - hash yoksa (eski cagiranlar) bugunku davranis: dogrudan kaydet
     * cancelled bayragi set edilirse indirme bir sonraki okumada kesilir.
     */
    public static void downloadFileVerified(String url, File dest, ByteProgress progress, String expectedSha1) throws IOException {
        HttpUtil.downloadFileVerified(url, dest, progress, expectedSha1, null);
    }

    public static void downloadFileVerified(String url, File dest, ByteProgress progress, String expectedSha1, java.util.concurrent.atomic.AtomicBoolean cancelled) throws IOException {
        IOException last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            if (cancelled != null && cancelled.get()) {
                throw new IOException("Iptal edildi");
            }
            try {
                HttpUtil.downloadFile(url, dest, progress, cancelled);
                if (expectedSha1 == null || expectedSha1.isEmpty() || HttpUtil.sha1Matches(dest, expectedSha1)) {
                    return;
                }
                last = new IOException("Indirilen dosya bozuk (SHA-1 uyusmadi): " + dest.getName());
                dest.delete();
            }
            catch (IOException e) {
                last = e;
                dest.delete();
                if (cancelled != null && cancelled.get()) {
                    throw new IOException("Iptal edildi");
                }
            }
            if (attempt < 3) {
                try {
                    Thread.sleep(400L * attempt);
                }
                catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        throw last != null ? last : new IOException("indirme basarisiz: " + url);
    }

    // =====================================================================
    // V29 PERFORMANS: SHA-1 dogrulama onbellegi.
    //
    // Problem: her oyun acilisinda yuzlerce kutuphane JAR'i (toplamda
    // ~1-2 GB) bastan hash'leniyordu; bu tek basina 5-15 saniye surer.
    //
    // Cozum: hesaplanan hash'ler path -> {sha1, size, lastModified}
    // olarak sha1-cache.json'da saklanir. Dosyanin boyutu+mtime onbellege
    // uyuyorsa hash'lemeden onbellegin sonucu kullanilir; uyusmazsa dosya
    // degismis demektir -> gercek hash hesaplanir ve onbellek guncellenir.
    // Onbellege SADECE kendi hesapladigimiz hash'ler yazildigi icin
    // tastimis/bozuk dosya onbellege giremez (checksum hala gercek icerige
    // karsi dogrulanir; onbellek sadece ayni icerige ikinci kez
    // bakmayi onler).
    // =====================================================================
    private static final java.util.Map<String, Sha1CacheEntry> SHA1_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Set<String> SHA1_CACHE_DIRTY = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static volatile boolean SHA1_CACHE_LOADED = false;
    private static final Object SHA1_CACHE_SAVE_LOCK = new Object();

    private static final class Sha1CacheEntry {
        String sha1;
        long size;
        long lastModified;
        Sha1CacheEntry() {
        }
        Sha1CacheEntry(String sha1, long size, long lastModified) {
            this.sha1 = sha1;
            this.size = size;
            this.lastModified = lastModified;
        }
    }

    private static File sha1CacheFile() {
        return new File(Paths.GAME_DIR, "sha1-cache.json");
    }

    private static void loadSha1CacheIfNeeded() {
        if (SHA1_CACHE_LOADED) {
            return;
        }
        synchronized (SHA1_CACHE_SAVE_LOCK) {
            if (SHA1_CACHE_LOADED) {
                return;
            }
            SHA1_CACHE_LOADED = true;
            File f = HttpUtil.sha1CacheFile();
            if (!f.isFile()) {
                return;
            }
            try {
                JsonObject root = JsonParser.parseString(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
                for (Map.Entry<String, com.google.gson.JsonElement> e : root.entrySet()) {
                    if (!e.getValue().isJsonObject()) continue;
                    JsonObject o = e.getValue().getAsJsonObject();
                    if (!o.has("sha1") || !o.has("size") || !o.has("mtime")) continue;
                    SHA1_CACHE.put(e.getKey(), new Sha1CacheEntry(o.get("sha1").getAsString(), o.get("size").getAsLong(), o.get("mtime").getAsLong()));
                }
            } catch (Exception ignored) {
                // Bozuk onbellek sessizce atlanir - her zaman gercek
                // hash'lemeye donebiliriz.
            }
        }
    }

    private static void scheduleSha1CacheSave() {
        synchronized (SHA1_CACHE_SAVE_LOCK) {
            SHA1_CACHE_SAVE_LOCK.notifyAll();
        }
    }

    /** Onbellegi arka planda bir kez yazar; 30sn'de bir toplu (debounce) kaydeder. */
    private static Thread SHA1_CACHE_SAVER;

    static {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    synchronized (SHA1_CACHE_SAVE_LOCK) {
                        SHA1_CACHE_SAVE_LOCK.wait(30_000L);
                    }
                    if (SHA1_CACHE_DIRTY.isEmpty()) {
                        continue;
                    }
                    synchronized (SHA1_CACHE_SAVE_LOCK) {
                        if (SHA1_CACHE_DIRTY.isEmpty()) {
                            continue;
                        }
                        JsonObject root = new JsonObject();
                        for (java.util.Map.Entry<String, Sha1CacheEntry> e : SHA1_CACHE.entrySet()) {
                            JsonObject o = new JsonObject();
                            o.addProperty("sha1", e.getValue().sha1);
                            o.addProperty("size", e.getValue().size);
                            o.addProperty("mtime", e.getValue().lastModified);
                            root.add(e.getKey(), o);
                        }
                        File f = HttpUtil.sha1CacheFile();
                        f.getParentFile().mkdirs();
                        File tmp = new File(f.getParentFile(), f.getName() + ".tmp");
                        try (OutputStream out = new FileOutputStream(tmp)) {
                            out.write(root.toString().getBytes(StandardCharsets.UTF_8));
                        }
                        Files.move(tmp.toPath(), f.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
                        SHA1_CACHE_DIRTY.clear();
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception ignored) {
                    // Kayit basarisiz olursa onbellek sadece calismaz; dogru
                    // davranis (gercek hashleme) bozulmaz.
                }
            }
        }, "sha1-cache-saver");
        t.setDaemon(true);
        SHA1_CACHE_SAVER = t;
        t.start();
    }

    private static String computeSha1(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));){
            int n;
            byte[] buf = new byte[65536]; // 64KB buffer - buyuk JAR'larda olculebilir hiz
            while ((n = ((InputStream)in).read(buf)) != -1) {
                digest.update(buf, 0, n);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder(hashBytes.length * 2);
        for (byte b : hashBytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    public static boolean sha1Matches(File file, String expectedSha1) {
        if (!file.exists() || expectedSha1 == null || expectedSha1.isEmpty()) {
            return false;
        }
        try {
            HttpUtil.loadSha1CacheIfNeeded();
            long size = file.length();
            long mtime = file.lastModified();
            String key = file.getAbsolutePath();
            Sha1CacheEntry cached = SHA1_CACHE.get(key);
            if (cached != null && cached.size == size && cached.lastModified == mtime && cached.sha1 != null) {
                // Dosya degismemis -> disk I/O YOK, sadece karsilastir.
                return cached.sha1.equalsIgnoreCase(expectedSha1);
            }
            String actual = HttpUtil.computeSha1(file);
            SHA1_CACHE.put(key, new Sha1CacheEntry(actual, size, mtime));
            SHA1_CACHE_DIRTY.add(key);
            HttpUtil.scheduleSha1CacheSave();
            return actual.equalsIgnoreCase(expectedSha1);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * V34.7 BASLANGIC HIZI: sha1 onbellegini ACILISTA arka planda isitir.
     * launch() sirasinda her kutuphane/asset icin sha1Matches cagrilir; ilk
     * acilista bu, yuzlerce dosyanin hash'ini (disk I/O) ana akista yapar.
     * Onbellegi (dosya + boyut + mtime) acilis thread'inden once isitirsek
     * launch sirasindaki buyuk bolum zaten islenmis olur.
     */
    public static void warmSha1CacheAsync() {
        HttpUtil.loadSha1CacheIfNeeded();
        new Thread(() -> {
            try {
                // Kutuphaneleri ve client jar'larini gez; sha1Matches'in
                // cache'e attigi (size+mtime) anahtarlari doldur.
                java.util.Deque<File> stack = new java.util.ArrayDeque<>();
                if (com.lubv.launcher.core.Paths.LIBRARIES_DIR.isDirectory()) {
                    stack.push(com.lubv.launcher.core.Paths.LIBRARIES_DIR);
                }
                File versionsDir = com.lubv.launcher.core.Paths.VERSIONS_DIR;
                File[] versDirs = versionsDir.listFiles(File::isDirectory);
                if (versDirs != null) {
                    for (File vd : versDirs) {
                        stack.push(vd);
                    }
                }
                // V34.8: ASSET objelerini de isit. AssetDownloader her asset
                // icin sha1Matches cagirir; binci asset'in ilkinde disk I/O
                // ana indirme akisinda olur. Assets kucuk dosyalardir ama
                // binlercedir - onbellegi acilista isitmek launch suresini
                // dogrudan kirpar. Assets'in uzantisi olmadigi icin bu agacta
                // tum dosyalar hash'lenir.
                if (com.lubv.launcher.core.Paths.ASSET_OBJECTS_DIR.isDirectory()) {
                    stack.push(com.lubv.launcher.core.Paths.ASSET_OBJECTS_DIR);
                }
                while (!stack.isEmpty()) {
                    File dir = stack.pop();
                    boolean inAssets = dir.getAbsolutePath().startsWith(com.lubv.launcher.core.Paths.ASSET_OBJECTS_DIR.getAbsolutePath());
                    File[] children = dir.listFiles();
                    if (children == null) {
                        continue;
                    }
                    for (File f : children) {
                        if (f.isDirectory()) {
                            stack.push(f);
                        } else if (f.isFile() && (inAssets || f.getName().endsWith(".jar"))) {
                            long size = f.length();
                            long mtime = f.lastModified();
                            String key = f.getAbsolutePath();
                            Sha1CacheEntry cached = SHA1_CACHE.get(key);
                            if (cached != null && cached.size == size && cached.lastModified == mtime) {
                                continue; // zaten biliniyor
                            }
                            try {
                                String sha = HttpUtil.computeSha1(f);
                                SHA1_CACHE.put(key, new Sha1CacheEntry(sha, size, mtime));
                                SHA1_CACHE_DIRTY.add(key);
                            } catch (Exception ignore) {
                                // tek dosya hatasi isitmayi bloklamaz
                            }
                        }
                    }
                }
                HttpUtil.scheduleSha1CacheSave();
            } catch (Exception ignored) {
                // isitma hicbir sekilde uygulamayi etkilemesin
            }
        }, "sha1-cache-warm").start();
    }

    public static boolean sha256Matches(File file, String expectedSha256) {
        if (!file.exists() || expectedSha256 == null || expectedSha256.isEmpty()) {
            return false;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));){
                int n;
                byte[] buf = new byte[8192];
                while ((n = ((InputStream)in).read(buf)) != -1) {
                    digest.update(buf, 0, n);
                }
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equalsIgnoreCase(expectedSha256);
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Ucretsiz (API anahtari gerektirmeyen) ceviri saglayicilarini kullanarak
     * metni verilen hedef dile cevirir.
     *
     * ONEMLI DUZELTME: Onceden sadece translate.googleapis.com/translate_a/single
     * endpoint'i kullaniliyordu. Bu endpoint artik cogu IP'den 429 (Too Many
     * Requests) donduruyor - ve hata sessizce yutulup orijinal metin geri
     * donduruldugu icin kullanicilar "cevirme calismiyor" sorunu yasiyordu.
     *
     * Artik 3 saglayici sirayla denenir (ilk basarili kazanir):
     *  1. clients5.google.com  (Chrome'un dahili dict-chrome-ex istemcisi -
     *     genelde 429'a takilmaz, en hizli olan)
     *  2. translate.googleapis.com/translate_a/single (eski endpoint, yedek)
     *  3. api.mymemory.translated.net (tamamen ayri servis, son yedek)
     *
     * Buyuk metinler parca parca (chunk) cevrilir; tum saglayicilar
     * basarisiz olursa orijinal metin dondurulur.
     */
    public static String translateText(String text, String targetLang) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String viaChrome = HttpUtil.translateViaClients5(text, targetLang);
        if (viaChrome != null) {
            return viaChrome;
        }
        String viaSingle = HttpUtil.translateViaGoogleSingle(text, targetLang);
        if (viaSingle != null) {
            return viaSingle;
        }
        String viaMyMemory = HttpUtil.translateViaMyMemory(text, targetLang);
        if (viaMyMemory != null) {
            return viaMyMemory;
        }
        System.err.println("[Translate] Tum ceviri saglayicilari basarisiz, orijinal metin donduruluyor.");
        return text;
    }

    /** 1. yontem: Chrome'un dahili ceviri istemcisi (clients5.google.com). */
    private static String translateViaClients5(String text, String targetLang) {
        try {
            java.util.List<String> chunks = HttpUtil.splitForTranslate(text, 1200);
            return HttpUtil.translateChunksParallel(chunks, chunk -> {
                try {
                    String url = "https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=auto&tl=" + targetLang
                        + "&q=" + java.net.URLEncoder.encode(chunk, "UTF-8");
                    // KISA zaman asili: 5 parcali bir aciklama artik ~10sn degil ~2-3sn'de cevrilir;
                    // bir parca takilirsa hizla yedek saglayiciya gecilir.
                    byte[] bytes = HttpUtil.getBytes(url, 6000, 8000);
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    return HttpUtil.parseClients5Response(json);
                } catch (Exception e) {
                    return null;
                }
            });
        }
        catch (Exception e) {
            System.err.println("[Translate] clients5 basarisiz: " + e.getMessage());
            return null;
        }
    }

    /** Ceviri icin metni satir/sozcuk sinirlarina yakin noktalardan parcalara ayirir. */
    private static java.util.List<String> splitForTranslate(String text, int chunkSize) {
        java.util.List<String> chunks = new java.util.ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + chunkSize);
            if (end < text.length()) {
                // Sozcuk ortasindan kesmemek icin son bosluk/nokta konumuna geri kay
                int lastBreak = Math.max(text.lastIndexOf(' ', end), text.lastIndexOf('.', end));
                if (lastBreak > start + chunkSize / 2) {
                    end = lastBreak + 1;
                }
            }
            chunks.add(text.substring(start, end));
            start = end;
        }
        if (chunks.isEmpty()) {
            chunks.add(text);
        }
        return chunks;
    }

    /**
     * Parcalari SIRAYLA degil PARALEL cevirir - uzun aciklamalarda beklemeyi ~4x kisaltir.
     * Herhangi bir parca basarisiz olursa null doner (cagiran kod yedek saglayiciya gecer).
     */
    private static String translateChunksParallel(java.util.List<String> chunks, java.util.function.Function<String, String> oneChunkTranslator) {
        if (chunks.size() == 1) {
            String s = oneChunkTranslator.apply(chunks.get(0));
            return s == null ? null : s;
        }
        String[] parts = new String[chunks.size()];
        java.util.concurrent.atomic.AtomicBoolean failed = new java.util.concurrent.atomic.AtomicBoolean(false);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(chunks.size());
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(Math.min(4, chunks.size()));
        try {
            for (int i = 0; i < chunks.size(); i++) {
                final int idx = i;
                final String chunk = chunks.get(i);
                pool.submit(() -> {
                    try {
                        if (!failed.get()) {
                            String r = oneChunkTranslator.apply(chunk);
                            if (r == null) {
                                failed.set(true);
                            } else {
                                parts[idx] = r;
                            }
                        }
                    } catch (Throwable t) {
                        failed.set(true);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            try {
                latch.await(30, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return null;
            }
        } finally {
            pool.shutdownNow();
        }
        if (failed.get()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null) {
                return null;
            }
            sb.append(p);
        }
        String s = sb.toString();
        return s.isBlank() ? null : s;
    }

    /**
     * clients5 yanitini cozumler. Olasi formatlar:
     *  [["ceviri","kaynak_dil"]]            -> tek segment
     *  [["seg1","en"],["seg2","en"],...]     -> coklu segment
     *  ["ceviri"]                            -> duz dizi
     * Cozulemezse null doner.
     */
    private static String parseClients5Response(String json) {
        try {
            com.google.gson.JsonElement rootEl = JsonParser.parseString(json);
            if (!rootEl.isJsonArray()) {
                return null;
            }
            com.google.gson.JsonArray root = rootEl.getAsJsonArray();
            if (root.size() == 0 || root.get(0).isJsonNull()) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            if (root.get(0).isJsonArray()) {
                for (int i = 0; i < root.size(); i++) {
                    com.google.gson.JsonElement segEl = root.get(i);
                    if (segEl.isJsonArray()) {
                        com.google.gson.JsonArray seg = segEl.getAsJsonArray();
                        if (seg.size() > 0 && !seg.get(0).isJsonNull()) {
                            sb.append(seg.get(0).getAsString());
                        }
                    } else if (!segEl.isJsonNull()) {
                        sb.append(segEl.getAsString());
                    }
                }
            } else {
                sb.append(root.get(0).getAsString());
            }
            String s = sb.toString();
            return s.isBlank() ? null : s;
        }
        catch (Exception e) {
            return null;
        }
    }

    /** 2. yontem: eski translate_a/single endpoint'i (hala bazi IP'lerde calisir). */
    private static String translateViaGoogleSingle(String text, String targetLang) {
        try {
            java.util.List<String> chunks = HttpUtil.splitForTranslate(text, 1800);
            return HttpUtil.translateChunksParallel(chunks, chunk -> {
                try {
                    String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + targetLang + "&dt=t&q=" + java.net.URLEncoder.encode(chunk, "UTF-8");
                    byte[] bytes = HttpUtil.getBytes(url, 6000, 8000);
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    // Yanit formati: [[["ceviri1","orijinal1",null,null,...],["ceviri2",...]],...]
                    com.google.gson.JsonArray root = JsonParser.parseString(json).getAsJsonArray();
                    if (root.size() > 0 && !root.get(0).isJsonNull()) {
                        com.google.gson.JsonArray segments = root.get(0).getAsJsonArray();
                        StringBuilder result = new StringBuilder();
                        for (int i = 0; i < segments.size(); i++) {
                            com.google.gson.JsonElement segEl = segments.get(i);
                            if (segEl.isJsonArray()) {
                                com.google.gson.JsonArray segment = segEl.getAsJsonArray();
                                if (segment.size() > 0 && !segment.get(0).isJsonNull()) {
                                    result.append(segment.get(0).getAsString());
                                }
                            }
                        }
                        return result.toString().isBlank() ? null : result.toString();
                    }
                    return null;
                } catch (Exception e) {
                    return null;
                }
            });
        }
        catch (Exception e) {
            System.err.println("[Translate] googleapis/single basarisiz: " + e.getMessage());
            return null;
        }
    }

    /** 3. yontem: MyMemory API (tamamen ayri servis, parca basi ~500 karakter limiti var). */
    private static String translateViaMyMemory(String text, String targetLang) {
        try {
            StringBuilder result = new StringBuilder();
            int chunkSize = 450;
            for (int offset = 0; offset < text.length(); offset += chunkSize) {
                String chunk = text.substring(offset, Math.min(text.length(), offset + chunkSize));
                String url = "https://api.mymemory.translated.net/get?q=" + java.net.URLEncoder.encode(chunk, "UTF-8")
                    + "&langpair=auto|" + targetLang;
                byte[] bytes = HttpUtil.getBytesWithRetry(url, 2);
                String json = new String(bytes, StandardCharsets.UTF_8);
                com.google.gson.JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                if (!obj.has("responseData") || obj.get("responseData").isJsonNull()) {
                    return null;
                }
                com.google.gson.JsonObject responseData = obj.getAsJsonObject("responseData");
                if (!responseData.has("translatedText") || responseData.get("translatedText").isJsonNull()) {
                    return null;
                }
                String part = responseData.get("translatedText").getAsString();
                // MyMemory hata durumunda sorguyun kendisini geri dondurebilir.
                if (part.isBlank()) {
                    return null;
                }
                result.append(part);
            }
            String translated = result.toString();
            return translated.isBlank() ? null : translated;
        }
        catch (Exception e) {
            System.err.println("[Translate] mymemory basarisiz: " + e.getMessage());
            return null;
        }
    }

    public static interface ByteProgress {
        public void onProgress(long var1, long var3);
    }
}

