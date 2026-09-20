/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.L10n;
import com.lubv.launcher.core.Paths;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UpdateManager {
    private static final String GITHUB_OWNER = "arddemirkiran-tech";
    private static final String GITHUB_REPO = "Complex-Launcher";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/arddemirkiran-tech/Complex-Launcher/releases/latest";
    public static final String CURRENT_VERSION = "44";
    private static final File UPDATE_DIR = new File(Paths.GAME_DIR, "update");
    private static final File VERSION_FILE = new File(Paths.GAME_DIR, "launcher_version.txt");
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static volatile boolean updating = false;
    private static final String[] SKIP = new String[]{"runtime/", "data/", "app/", "META-INF/", "shell/", "update/", "logs/", "versions/", "libraries/", "assets/", "natives/", "instances/", "resourcepacks/", "shaderpacks/", "saves/", "mods/"};

    public static String getLocalVersion() {
        // Derlenmis surum (CURRENT_VERSION) her zaman gercektir.
        // launcher_version.txt eski/bayat bir deger iceriyorsa (orn. eski bir
        // guncellemeden kalan "27" veya "15") asla onu kullanma - yoksa v30
        // calisirken "v30 -> v27" gibi yanlis bir dusurme teklif edilirdi.
        if (VERSION_FILE.exists()) {
            try {
                String fileVersion = Files.readString(VERSION_FILE.toPath()).trim();
                if (isAtLeastCurrent(fileVersion)) {
                    return fileVersion;
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return CURRENT_VERSION;
    }

    /** Dosyadaki surum CURRENT_VERSION'dan buyuk veya esit mi? (ayrismo gecerli bir sayi mi?) */
    private static boolean isAtLeastCurrent(String v) {
        if (v == null || v.isBlank()) {
            return false;
        }
        String c = v.replaceAll("[^0-9.]", "");
        if (c.isEmpty()) {
            return false;
        }
        try {
            return UpdateInfo.compareVersions(c, CURRENT_VERSION) >= 0;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static void saveLocalVersion(String version) {
        try {
            UPDATE_DIR.mkdirs();
            Files.writeString(VERSION_FILE.toPath(), (CharSequence)version, new OpenOption[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static void checkForUpdates(UpdateCallback callback) {
        new Thread(() -> {
            try {
                String localVersion = UpdateManager.getLocalVersion();
                callback.onStatus(L10n.get("update.checking"));
                String json = HttpUtil.getText(GITHUB_API_URL);
                JsonObject release = JsonParser.parseString(json).getAsJsonObject();
                UpdateInfo info = new UpdateInfo();
                String string = info.tagName = release.has("tag_name") ? release.get("tag_name").getAsString() : "";
                info.changelog = release.has("body") && !release.get("body").isJsonNull() ? release.get("body").getAsString() : (release.has("name") ? release.get("name").getAsString() : "");
                info.version = UpdateManager.extractVersion(info.tagName);
                if (release.has("assets") && release.get("assets").isJsonArray()) {
                    String lower;
                    String name;
                    JsonObject asset;
                    int i;
                    JsonArray assets = release.getAsJsonArray("assets");
                    for (i = 0; i < assets.size(); ++i) {
                        asset = assets.get(i).getAsJsonObject();
                        name = asset.has("name") ? asset.get("name").getAsString() : "";
                        lower = name.toLowerCase();
                        if (!lower.endsWith(".zip") || lower.contains("source") || lower.contains("-src") || lower.contains("full") || IS_WINDOWS && lower.contains("linux") || !IS_WINDOWS && !lower.contains("linux")) continue;
                        info.downloadUrl = asset.has("browser_download_url") ? asset.get("browser_download_url").getAsString() : "";
                        break;
                    }
                    if (info.downloadUrl == null || info.downloadUrl.isEmpty()) {
                        for (i = 0; i < assets.size(); ++i) {
                            asset = assets.get(i).getAsJsonObject();
                            name = asset.has("name") ? asset.get("name").getAsString() : "";
                            lower = name.toLowerCase();
                            if (!lower.endsWith(".zip") || lower.contains("source") || lower.contains("-src")) continue;
                            info.downloadUrl = asset.has("browser_download_url") ? asset.get("browser_download_url").getAsString() : "";
                            break;
                        }
                    }
                }
                if (info.version.isEmpty() || info.downloadUrl == null || info.downloadUrl.isEmpty()) {
                    callback.onStatus("G\u00fcncelleme bilgisi al\u0131namad\u0131.");
                    return;
                }
                if (info.isNewerThan(localVersion)) {
                    callback.onStatus(L10n.fmt("update.available", "v" + info.version));
                    callback.onUpdateReady(info);
                } else {
                    callback.onStatus((L10n.isEnglish() ? "Up to date \u2014 v" : "G\u00fcncel \u2014 v") + localVersion);
                }
            }
            catch (Exception e) {
                callback.onStatus(L10n.fmt("update.error", e.getMessage()));
            }
        }).start();
    }

    private static String extractVersion(String tag) {
        if (tag == null || tag.isEmpty()) {
            return "";
        }
        Matcher m = Pattern.compile("(\\d+(?:\\.\\d+)*)").matcher(tag);
        if (m.find()) {
            return m.group(1);
        }
        return tag;
    }

    public static void downloadUpdate(UpdateInfo info, UpdateCallback callback) {
        if (updating) {
            callback.onError(L10n.isEnglish() ? "Update already in progress..." : "G\u00fcncelleme zaten devam ediyor...");
            return;
        }
        updating = true;
        new Thread(() -> {
            try {
                UPDATE_DIR.mkdirs();
                callback.onStatus(L10n.isEnglish() ? "Downloading..." : "\u0130ndiriliyor...");
                File zipFile = new File(UPDATE_DIR, "launcher_update.zip");
                HttpUtil.downloadFile(info.downloadUrl, zipFile, (dl, total) -> {
                    if (total > 0L) {
                        int pct = (int)(dl * 100L / total);
                        callback.onProgress(Math.min(90, pct));
                        callback.onStatus((L10n.isEnglish() ? "Downloading... " : "\u0130ndiriliyor... ") + pct + "%");
                    }
                });
                callback.onProgress(90);
                UpdateManager.applyUpdate(zipFile, info, callback);
            }
            catch (Exception e) {
                callback.onError(L10n.fmt("update.update_error", e.getMessage()));
            }
            finally {
                updating = false;
            }
        }).start();
    }

    public static void applyUpdate(File zipFile, UpdateInfo info, UpdateCallback callback) throws IOException {
        File script;
        StringBuilder sb;
        File staging = new File(UPDATE_DIR, "staging");
        if (staging.exists()) {
            UpdateManager.deleteRecursive(staging);
        }
        staging.mkdirs();
        callback.onStatus(L10n.isEnglish() ? "Extracting..." : "\u00c7\u0131kar\u0131l\u0131yor...");
        callback.onProgress(92);
        UpdateManager.extractZip(zipFile, staging);
        File srcDir = UpdateManager.findSourceDir(staging);
        File launcherDir = UpdateManager.findLauncherDir();
        if (launcherDir == null) {
            callback.onError(L10n.isEnglish() ? "Launcher directory not found." : "Launcher dizini bulunamad\u0131.");
            return;
        }
        callback.onStatus(L10n.isEnglish() ? "Preparing update script..." : "Script olu\u015fturuluyor...");
        callback.onProgress(95);
        long pid = ProcessHandle.current().pid();
        File pidFile = new File(UPDATE_DIR, "launcher.pid");
        Files.writeString(pidFile.toPath(), (CharSequence)String.valueOf(pid), new OpenOption[0]);
        String srcPath = srcDir.getAbsolutePath();
        String targetPath = launcherDir.getAbsolutePath();
        String updatePath = UPDATE_DIR.getAbsolutePath();
        if (IS_WINDOWS) {
            script = new File(UPDATE_DIR, "update.bat");
        } else {
            script = new File(UPDATE_DIR, "update.sh");
        }
        // V30.1: script icerigi test edilebilir statik metotta uretilir.
        // launcherJava: launcher'in KENDI JVM'i (paketlenmis runtime yoksa
        // PATH'teki esik javaw yerine bu kullanilir - esik JVM class
        // surumu hatasi verip launcher'in acilmamasini engeller).
        String launcherJava = System.getProperty("java.home") + File.separator + "bin" + File.separator + (IS_WINDOWS ? "javaw.exe" : "java");
        Files.writeString(script.toPath(), (CharSequence)UpdateManager.buildUpdateScript(IS_WINDOWS, srcDir.getAbsolutePath(), targetPath, pidFile.getAbsolutePath(), updatePath, pidFile.exists(), launcherJava), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        if (!IS_WINDOWS) {
            script.setExecutable(true);
        }
        callback.onStatus(L10n.isEnglish() ? "Closing launcher..." : "Launcher kapat\u0131l\u0131yor...");
        callback.onProgress(98);
        try {
            if (IS_WINDOWS) {
                new ProcessBuilder("cmd", "/c", "start", "", "/min", script.getAbsolutePath()).directory(UPDATE_DIR).start();
            } else {
                new ProcessBuilder("bash", script.getAbsolutePath()).directory(UPDATE_DIR).start();
            }
        }
        catch (Exception e) {
            throw new IOException((L10n.isEnglish() ? "Could not start update script: " : "Script ba\u015flat\u0131lamad\u0131: ") + e.getMessage());
        }
        UpdateManager.saveLocalVersion(info.version);
        callback.onProgress(100);
        callback.onUpdateComplete(true, L10n.fmt("update.complete", "v" + info.version));
        try {
            Thread.sleep(1000L);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
        // V30.1: script zaten PID cikisini bekleyip gerekirse kill ediyor ve
        // yeni launcher'i O baslatiyor. Burada ekstra force-kill YOK - onceki
        // cift-kill yarisi (yeni instance'in olmesi) ve kullanici diyalogunu
        // kapatan ani cikis kaldirildi. Sadece normal cikis.
        System.exit(0);
    }

    /**
     * V30.1: guncelleme scriptini uretir. Saglamlilik kurallari:
     *  - PID dosyasi yok/bos ise sonsuz bekleme YOK, kisa guvenli bekleme.
     *  - PID cikisi en fazla 30 tur (~60s) beklenir; sonra force kill.
     *    Beklemeler "timeout /t" DEGIL "ping -n": timeout konsolsuz
     *    baslatilan script'te aninda hata verip beklemeyi 0 yapiyordu
     *    (launcher kapanmadan kopya = bozuk jar).
     *  - Eski jar once .bak olarak yedeklenir; kopya sonrasi boyut
     *    eslesmesi kontrol edilir, uyusmazsa .bak geri yuklenir (yarim
     *    kalan kopya artik ASLA "Invalid or corrupt jarfile" uretmez).
     *  - Launcher sirasiyla paketlenmis runtime -> launcher'in kendi
     *    JVM'i -> PATH javaw ile ACIK olarak baslatilir (jar dosya
     *    iliskilendirmesine guvenilmez).
     *  - Tum adimlar update.log'a yazilir (destek/hata analizi icin).
     */
    static String buildUpdateScript(boolean windows, String srcPath, String targetPath, String pidPath, String updatePath, boolean hasPidFile, String launcherJava) {
        StringBuilder sb = new StringBuilder();
        if (windows) {
            sb.append("@echo off\r\n");
            sb.append("title Complex Launcher Update\r\n");
            sb.append("set LOG=\"" + updatePath + "\\update.log\"\r\n");
            sb.append("echo %date% %time% update start > %LOG%\r\n");
            sb.append("set LAUNCHER_PID=\r\n");
            if (hasPidFile) {
                sb.append("if exist \"" + pidPath + "\" set /p LAUNCHER_PID=<\"" + pidPath + "\"\r\n");
            }
            sb.append("echo PID=%LAUNCHER_PID% >> %LOG%\r\n");
            sb.append("if \"%LAUNCHER_PID%\"==\"\" (\r\n");
            sb.append("  echo no pid - short grace wait >> %LOG%\r\n");
            sb.append("  ping -n 4 -w 1000 127.0.0.1 >nul\r\n");
            sb.append("  goto copy\r\n");
            sb.append(")\r\n");
            sb.append("set /A TRIES=0\r\n");
            sb.append(":check\r\n");
            sb.append("tasklist /FI \"PID eq %LAUNCHER_PID%\" 2>nul | findstr /i \"%LAUNCHER_PID%\" >nul\r\n");
            sb.append("if errorlevel 1 goto backup\r\n");
            sb.append("set /A TRIES+=1\r\n");
            sb.append("if %TRIES% GEQ 30 (\r\n");
            sb.append("  echo pid still alive after ~60s - force kill >> %LOG%\r\n");
            sb.append("  taskkill /PID %LAUNCHER_PID% /F >nul 2>&1\r\n");
            sb.append("  ping -n 3 -w 1000 127.0.0.1 >nul\r\n");
            sb.append("  goto backup\r\n");
            sb.append(")\r\n");
            sb.append("ping -n 3 -w 1000 127.0.0.1 >nul\r\n");
            sb.append("goto check\r\n");
            sb.append(":backup\r\n");
            sb.append("if exist \"" + targetPath + "\\Complex-Launcher.jar\" (\r\n");
            sb.append("  copy /Y \"" + targetPath + "\\Complex-Launcher.jar\" \"" + targetPath + "\\Complex-Launcher.jar.bak\" >nul 2>&1\r\n");
            sb.append("  echo old jar backed up >> %LOG%\r\n");
            sb.append(")\r\n");
            sb.append(":copy\r\n");
            sb.append("echo copying files >> %LOG%\r\n");
            sb.append("xcopy /E /C /I /H /R /Y \"" + srcPath + "\\*\" \"" + targetPath + "\\\" >> %LOG% 2>&1\r\n");
            sb.append("echo xcopy exit=%ERRORLEVEL% >> %LOG%\r\n");
            // Butunluk kontrolu: kaynak ve hedef jar boyutu ESLESMELI.
            // Yarim kalan kopya -> .bak geri yuklenir (bozuk jar engellenir).
            sb.append("set S1=0\r\n");
            sb.append("set S2=0\r\n");
            sb.append("for %%A in (\"" + srcPath + "\\Complex-Launcher.jar\") do set S1=%%~zA\r\n");
            sb.append("for %%B in (\"" + targetPath + "\\Complex-Launcher.jar\") do set S2=%%~zB\r\n");
            sb.append("echo jar size src=%S1% tgt=%S2% >> %LOG%\r\n");
            sb.append("if not \"%S1%\"==\"%S2%\" (\r\n");
            sb.append("  echo SIZE MISMATCH - restoring backup >> %LOG%\r\n");
            sb.append("  copy /Y \"" + targetPath + "\\Complex-Launcher.jar.bak\" \"" + targetPath + "\\Complex-Launcher.jar\" >nul 2>&1\r\n");
            sb.append(")\r\n");
            sb.append("rmdir /S /Q \"" + srcPath + "\" >nul 2>&1\r\n");
            sb.append("del /F /Q \"" + pidPath + "\" >nul 2>&1\r\n");
            sb.append("echo starting launcher >> %LOG%\r\n");
            sb.append("cd /d \"" + targetPath + "\"\r\n");
            sb.append("if exist \"runtime\\bin\\javaw.exe\" (\r\n");
            sb.append("  echo using bundled runtime >> %LOG%\r\n");
            sb.append("  start \"\" \"runtime\\bin\\javaw.exe\" -jar \"Complex-Launcher.jar\"\r\n");
            sb.append(") else if exist \"" + launcherJava + "\" (\r\n");
            sb.append("  echo using launcher jvm >> %LOG%\r\n");
            sb.append("  start \"\" \"" + launcherJava + "\" -jar \"Complex-Launcher.jar\"\r\n");
            sb.append(") else (\r\n");
            sb.append("  echo using PATH javaw >> %LOG%\r\n");
            sb.append("  start \"\" javaw -jar \"Complex-Launcher.jar\"\r\n");
            sb.append(")\r\n");
            sb.append("echo %date% %time% update done >> %LOG%\r\n");
            sb.append("exit\r\n");
        } else {
            sb.append("#!/bin/bash\n");
            sb.append("LOG=\"" + updatePath + "/update.log\"\n");
            sb.append("echo \"$(date) update start\" > \"$LOG\"\n");
            sb.append("LAUNCHER_PID=$(cat \"" + pidPath + "\" 2>/dev/null || true)\n");
            sb.append("echo \"PID=$LAUNCHER_PID\" >> \"$LOG\"\n");
            if (hasPidFile) {
                sb.append("if [ -n \"$LAUNCHER_PID\" ]; then\n");
                sb.append("  TRIES=0\n");
                sb.append("  while kill -0 \"$LAUNCHER_PID\" 2>/dev/null; do\n");
                sb.append("    TRIES=$((TRIES+1))\n");
                sb.append("    if [ \"$TRIES\" -ge 30 ]; then\n");
                sb.append("      echo \"pid still alive - force kill\" >> \"$LOG\"\n");
                sb.append("      kill -9 \"$LAUNCHER_PID\" 2>/dev/null || true\n");
                sb.append("      sleep 2\n");
                sb.append("      break\n");
                sb.append("    fi\n");
                sb.append("    sleep 2\n");
                sb.append("  done\n");
                sb.append("else\n");
                sb.append("  sleep 3\n");
                sb.append("fi\n");
            } else {
                sb.append("sleep 3\n");
            }
            sb.append("cp -f \"" + targetPath + "/Complex-Launcher.jar\" \"" + targetPath + "/Complex-Launcher.jar.bak\" 2>/dev/null || true\n");
            sb.append("echo \"copying files\" >> \"$LOG\"\n");
            sb.append("cp -r \"" + srcPath + "/\"* \"" + targetPath + "/\"\n");
            sb.append("S1=$(stat -c%s \"" + srcPath + "/Complex-Launcher.jar\" 2>/dev/null || echo 0)\n");
            sb.append("S2=$(stat -c%s \"" + targetPath + "/Complex-Launcher.jar\" 2>/dev/null || echo 0)\n");
            sb.append("echo \"jar size src=$S1 tgt=$S2\" >> \"$LOG\"\n");
            sb.append("if [ \"$S1\" != \"$S2\" ]; then\n");
            sb.append("  echo \"SIZE MISMATCH - restoring backup\" >> \"$LOG\"\n");
            sb.append("  cp -f \"" + targetPath + "/Complex-Launcher.jar.bak\" \"" + targetPath + "/Complex-Launcher.jar\"\n");
            sb.append("fi\n");
            sb.append("rm -rf \"" + srcPath + "\"\n");
            sb.append("rm -f \"" + pidPath + "\"\n");
            sb.append("cd \"" + targetPath + "\"\n");
            sb.append("if [ -x \"runtime/bin/java\" ]; then\n");
            sb.append("  nohup \"runtime/bin/java\" -jar Complex-Launcher.jar > /dev/null 2>&1 &\n");
            sb.append("elif [ -x \"" + launcherJava + "\" ]; then\n");
            sb.append("  nohup \"" + launcherJava + "\" -jar Complex-Launcher.jar > /dev/null 2>&1 &\n");
            sb.append("else\n");
            sb.append("  nohup java -jar Complex-Launcher.jar > /dev/null 2>&1 &\n");
            sb.append("fi\n");
            sb.append("echo \"$(date) update done\" >> \"$LOG\"\n");
        }
        return sb.toString();
    }

    private static void extractZip(File zipFile, File destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));){
            ZipEntry entry;
            byte[] buf = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                String lower = name.toLowerCase().replace('\\', '/');
                boolean skip = false;
                for (String p : SKIP) {
                    if (!lower.startsWith(p) && !lower.contains("/" + p)) continue;
                    skip = true;
                    break;
                }
                if (skip) {
                    zis.closeEntry();
                    continue;
                }
                File dest = new File(destDir, name);
                if (!dest.getCanonicalPath().startsWith(destDir.getCanonicalPath())) {
                    throw new IOException("ZIP outside");
                }
                if (entry.isDirectory()) {
                    dest.mkdirs();
                } else {
                    dest.getParentFile().mkdirs();
                    try (FileOutputStream os = new FileOutputStream(dest);){
                        int n;
                        while ((n = zis.read(buf)) > 0) {
                            ((OutputStream)os).write(buf, 0, n);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private static File findSourceDir(File root) {
        File cl = new File(root, GITHUB_REPO);
        if (cl.isDirectory()) {
            return cl;
        }
        File cl2 = new File(root, "Complex-Launcher-Linux");
        if (cl2.isDirectory()) {
            return cl2;
        }
        if (new File(root, "Complex-Launcher.jar").exists()) {
            return root;
        }
        if (new File(root, "start.sh").exists()) {
            return root;
        }
        File[] ch = root.listFiles(File::isDirectory);
        if (ch != null) {
            for (File c : ch) {
                if (!new File(c, "Complex-Launcher.jar").exists()) continue;
                return c;
            }
        }
        return root;
    }

    private static File findLauncherDir() {
        if (Paths.PORTABLE) {
            return Paths.getLauncherDir();
        }
        String m = IS_WINDOWS ? "Complex-Launcher.exe" : "Complex-Launcher.jar";
        File cwd = new File(System.getProperty("user.dir"));
        if (new File(cwd, m).exists()) {
            return cwd;
        }
        File p = cwd.getParentFile();
        if (p != null && new File(p, m).exists()) {
            return p;
        }
        try {
            File jd;
            File jf = new File(UpdateManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            File file = jd = jf.isFile() ? jf.getParentFile() : jf;
            if (jd != null) {
                File b;
                if (new File(jd, m).exists()) {
                    return jd;
                }
                if ("app".equals(jd.getName()) && (b = jd.getParentFile()) != null && new File(b, m).exists()) {
                    return b;
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return Paths.getLauncherDir();
    }

    private static void deleteRecursive(File f) {
        File[] c;
        if (f.isDirectory() && (c = f.listFiles()) != null) {
            for (File x : c) {
                UpdateManager.deleteRecursive(x);
            }
        }
        f.delete();
    }

    public static interface UpdateCallback {
        public void onStatus(String var1);

        public void onProgress(int var1);

        public void onError(String var1);

        public void onUpdateReady(UpdateInfo var1);

        public void onUpdateComplete(boolean var1, String var2);
    }

    public static class UpdateInfo {
        public String version;
        public String downloadUrl;
        public String changelog;
        public String tagName;

        public boolean isNewerThan(String current) {
            if (this.version == null || current == null) {
                return false;
            }
            return UpdateInfo.compareVersions(this.version, current) > 0;
        }

        private static int compareVersions(String v1, String v2) {
            String c1 = v1.replaceAll("[^0-9.]", "");
            String c2 = v2.replaceAll("[^0-9.]", "");
            if (c1.isEmpty()) {
                c1 = "0";
            }
            if (c2.isEmpty()) {
                c2 = "0";
            }
            String[] p1 = c1.split("\\.");
            String[] p2 = c2.split("\\.");
            int len = Math.max(p1.length, p2.length);
            for (int i = 0; i < len; ++i) {
                int b;
                int a = i < p1.length ? Integer.parseInt(p1[i]) : 0;
                int n = b = i < p2.length ? Integer.parseInt(p2[i]) : 0;
                if (a == b) continue;
                return Integer.compare(a, b);
            }
            return 0;
        }
    }
}

