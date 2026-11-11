/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.game.OsRules;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class JavaRuntime {
    private JavaRuntime() {
    }

    public static String ensureJava(int n, String string, ProgressCallback progressCallback) throws Exception {
        String string2 = JavaRuntime.findInstalled(n, string);
        if (string2 != null) {
            return string2;
        }
        progressCallback.onLog("Uygun Java bulunamad\u0131. Java " + n + " otomatik indiriliyor...");
        return JavaRuntime.installJava(n, progressCallback);
    }

    private static String findInstalled(int n, String string) {
        File file;
        Object[] objectArray;
        String string2;
        File file2;
        String string3;
        String string4;
        String string5 = string4 = OsRules.CURRENT_OS == OsRules.Os.WINDOWS ? "java.exe" : "java";
        if (string != null && !string.isBlank() && new File(string).isFile()) {
            return string;
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        File file3 = new File(new File(System.getProperty("java.home"), "bin"), string4);
        if (file3.isFile()) {
            arrayList.add(file3.getAbsolutePath());
        }
        if ((string3 = System.getenv("JAVA_HOME")) != null && !string3.isBlank() && (file2 = new File(new File(string3, "bin"), string4)).isFile()) {
            arrayList.add(file2.getAbsolutePath());
        }
        if ((string2 = System.getenv("PATH")) != null) {
            objectArray = string2.split(File.pathSeparator);
            for (Object object2 : objectArray) {
                if (((String)object2).isBlank() || !(file = new File((String)object2, string4)).isFile()) continue;
                arrayList.add(file.getAbsolutePath());
            }
        }
        JavaRuntime.addCommonDirs(arrayList, string4);
        if (Paths.RUNTIME_DIR.isDirectory() && (objectArray = Paths.RUNTIME_DIR.listFiles(File::isDirectory)) != null) {
            for (Object object : objectArray) {
                file = new File(new File((File)object, "bin"), string4);
                if (!file.isFile()) continue;
                arrayList.add(file.getAbsolutePath());
            }
        }
        // En uygun aday: tam surum eslesenler arasinda 64-bit'i tercih et;
        // uyumlu hicbiri yoksa yeni surumlu Java 17+ adaylarina izin ver.
        String exact64 = null;
        String exactAny = null;
        String fallback = null;
        for (String string6 : arrayList) {
            int n2 = JavaRuntime.detectMajor(string6);
            if (n2 == n) {
                if (exact64 == null && !JavaRuntime.isProbably32Bit(string6)) {
                    exact64 = string6;
                }
                if (exactAny == null) {
                    exactAny = string6;
                }
                continue;
            }
            if (n2 <= n || n2 - n > 2 || n < 17 || fallback != null) continue;
            fallback = string6;
        }
        if (exact64 != null) {
            return exact64;
        }
        if (exactAny != null) {
            return exactAny;
        }
        if (fallback != null) {
            return fallback;
        }
        return null;
    }

    private static boolean isProbably32Bit(String javaExe) {
        if (javaExe == null) {
            return false;
        }
        String p = javaExe.toLowerCase().replace(File.separatorChar, '/');
        // Oracle'n paylasilan java8path kopyasi ve Program Files (x86) kurulumlari 32-bit'dir.
        return p.contains("program files (x86)") || p.contains("common files/oracle/java");
    }

    public static String installJava(int n, ProgressCallback progressCallback) throws Exception {
        String string;
        File file = new File(Paths.RUNTIME_DIR, "java-" + n);
        String string2 = JavaRuntime.findBinary(file, string = OsRules.CURRENT_OS == OsRules.Os.WINDOWS ? "java.exe" : "java");
        if (string2 != null) {
            progressCallback.onLog("Java " + n + " zaten kurulu: " + string2);
            return string2;
        }
        String[] stringArray = new String[]{"jre", "jdk"};
        Throwable throwable = null;
        for (String string3 : stringArray) {
            try {
                return JavaRuntime.downloadAndInstall(n, string3, file, string, progressCallback);
            }
            catch (IOException iOException) {
                throwable = iOException;
                progressCallback.onLog("Java " + n + " (" + string3 + ") indirilemedi: " + iOException.getMessage());
            }
        }
        throw new Exception("Java " + n + " indirilemedi" + (String)(throwable != null ? ": " + throwable.getMessage() : "."));
    }

    private static String downloadAndInstall(int n, String string, File file, String string2, ProgressCallback progressCallback) throws IOException {
        String string3 = "https://api.adoptium.net/v3/binary/latest/" + n + "/ga/" + JavaRuntime.osName() + "/" + JavaRuntime.archName() + "/" + string + "/hotspot/normal/eclipse";
        boolean bl = OsRules.CURRENT_OS == OsRules.Os.WINDOWS;
        String string4 = bl ? ".zip" : ".tar.gz";
        File file2 = new File(Paths.RUNTIME_DIR, "java-" + n + string4);
        Paths.RUNTIME_DIR.mkdirs();
        progressCallback.onLog("Java " + n + " (" + string + ") indiriliyor...");
        JavaRuntime.download(string3, file2, (l, l2) -> {
            if (l2 > 0L) {
                int pct = (int)Math.min(99L, l * 100L / l2);
                progressCallback.onProgress(pct, "Java indiriliyor (" + JavaRuntime.humanBytes(l) + " / " + JavaRuntime.humanBytes(l2) + ")");
            } else {
                progressCallback.onProgress(-1, "Java indiriliyor (" + JavaRuntime.humanBytes(l) + ")");
            }
        });
        String string5 = JavaRuntime.fetchChecksum(n, string);
        if (string5 != null && !HttpUtil.sha256Matches(file2, string5)) {
            throw new IOException("SHA-256 do\u011frulamas\u0131 ba\u015far\u0131s\u0131z (indirme bozuk)");
        }
        progressCallback.onLog("Java ar\u015fivi a\u00e7\u0131l\u0131yor...");
        JavaRuntime.deleteRecursive(file);
        if (bl) {
            JavaRuntime.unzip(file2, file);
        } else {
            JavaRuntime.untargz(file2, file);
        }
        file2.delete();
        String string6 = JavaRuntime.findBinary(file, string2);
        if (string6 == null) {
            throw new IOException("Java kuruldu ancak \u00e7al\u0131\u015ft\u0131r\u0131labilir bulunamad\u0131: " + file.getAbsolutePath());
        }
        int n2 = JavaRuntime.detectMajor(string6);
        if (n2 > 0 && n2 < n) {
            throw new IOException("\u0130ndirilen Java s\u00fcr\u00fcm\u00fc (" + n2 + ") istenenden (" + n + ") eski");
        }
        progressCallback.onLog("\u2713 Java " + n + " kuruldu: " + string6);
        return string6;
    }

    private static String fetchChecksum(int n, String string) {
        String string2 = "https://api.adoptium.net/v3/assets/latest/" + n + "/hotspot?architecture=" + JavaRuntime.archName() + "&image_type=" + string + "&jvm_impl=hotspot&os=" + JavaRuntime.osName() + "&vendor=eclipse";
        try {
            JsonArray jsonArray;
            String string3 = HttpUtil.getText(string2);
            if (string3 == null || string3.isBlank()) {
                return null;
            }
            JsonElement jsonElement = JsonParser.parseString(string3);
            JsonArray jsonArray2 = jsonArray = jsonElement.isJsonArray() ? jsonElement.getAsJsonArray() : null;
            if (jsonArray == null || jsonArray.size() == 0) {
                return null;
            }
            JsonObject jsonObject = jsonArray.get(0).getAsJsonObject().getAsJsonObject("binary").getAsJsonObject("package");
            if (jsonObject.has("checksum") && !jsonObject.get("checksum").isJsonNull()) {
                return jsonObject.get("checksum").getAsString().trim().toLowerCase();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    // Baska bir Java surumunu -version ile yoklamayi cache'ler (surec acmak pahali).
    private static final java.util.concurrent.ConcurrentHashMap<String, Integer> MAJOR_CACHE = new java.util.concurrent.ConcurrentHashMap<String, Integer>();

    public static int detectMajor(String string) {
        if (string == null || string.isEmpty()) {
            return 0;
        }
        Integer cached = MAJOR_CACHE.get(string);
        if (cached != null) {
            return cached.intValue();
        }
        int major = JavaRuntime.detectMajorUncached(string);
        if (major > 0) {
            MAJOR_CACHE.put(string, Integer.valueOf(major));
        }
        return major;
    }

    public static int detectMajorUncached(String string) {
        try {
            Process process = new ProcessBuilder(string, "-version").redirectErrorStream(true).start();
            String string2 = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            process.waitFor();
            Matcher matcher = Pattern.compile("version \"([0-9._]+)\"").matcher(string2);
            if (matcher.find()) {
                String[] stringArray = matcher.group(1).split("\\.");
                if (stringArray.length > 1 && stringArray[0].equals("1")) {
                    return Integer.parseInt(stringArray[1]);
                }
                return Integer.parseInt(stringArray[0]);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return 0;
    }

    /** Kullanilacak java.icin sinif dosyasi surumu (metadata'yi acmadan) — agent uyumluluk kapisi icin. */
    public static int readClassMajor(File jarFile) {
        if (jarFile == null || !jarFile.isFile()) {
            return 0;
        }
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(jarFile)) {
            java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zip.entries();
            int max = 0;
            while (entries.hasMoreElements()) {
                java.util.zip.ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("META-INF/versions/") || name.equals("module-info.class")) {
                    continue;
                }
                if (!name.endsWith(".class")) {
                    continue;
                }
                try (InputStream in = zip.getInputStream(entry)) {
                    byte[] header = new byte[8];
                    int off = 0;
                    while (off < 8) {
                        int rd = in.read(header, off, 8 - off);
                        if (rd < 0) break;
                        off += rd;
                    }
                    if (off < 8 || header[0] != (byte)0xCA || header[1] != (byte)0xFE) {
                        continue;
                    }
                    int major = ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
                    if (major > max) {
                        max = major;
                    }
                }
            }
            return max;
        }
        catch (Exception exception) {
            return 0;
        }
    }

    private static String osName() {
        switch (OsRules.CURRENT_OS) {
            case WINDOWS: {
                return "windows";
            }
            case MAC: {
                return "mac";
            }
        }
        return "linux";
    }

    private static String archName() {
        String string = OsRules.CURRENT_ARCH;
        return "arm64".equals(string) ? "aarch64" : string;
    }

    private static String humanBytes(long l) {
        if (l < 1024L) {
            return l + " B";
        }
        double d = (double)l / 1024.0;
        if (d < 1024.0) {
            return String.format("%.1f KB", d);
        }
        double d2 = d / 1024.0;
        if (d2 < 1024.0) {
            return String.format("%.1f MB", d2);
        }
        return String.format("%.2f GB", d2 / 1024.0);
    }

    private static String findBinary(File file, String string) {
        if (!file.isDirectory()) {
            return null;
        }
        File[] fileArray = file.listFiles();
        if (fileArray == null) {
            return null;
        }
        for (File file2 : fileArray) {
            if (!file2.isDirectory()) continue;
            File file3 = new File(new File(file2, "bin"), string);
            if (file3.isFile()) {
                return file3.getAbsolutePath();
            }
            String string2 = JavaRuntime.findBinary(file2, string);
            if (string2 == null) continue;
            return string2;
        }
        return null;
    }

    private static void addCommonDirs(List<String> list, String string) {
        ArrayList<File> arrayList = new ArrayList<File>();
        if (OsRules.CURRENT_OS == OsRules.Os.WINDOWS) {
            String string2 = System.getenv("ProgramFiles");
            String object = System.getenv("ProgramFiles(x86)");
            if (string2 != null) {
                arrayList.add(new File(string2, "Java"));
                arrayList.add(new File(string2, "Eclipse Adoptium"));
                arrayList.add(new File(string2, "Microsoft"));
            }
            if (object != null) {
                arrayList.add(new File(object, "Java"));
            }
        } else if (OsRules.CURRENT_OS == OsRules.Os.MAC) {
            arrayList.add(new File("/Library/Java/JavaVirtualMachines"));
        } else {
            arrayList.add(new File("/usr/lib/jvm"));
            arrayList.add(new File("/opt"));
        }
        for (File file : arrayList) {
            JavaRuntime.addJavaFrom(file, list, string);
        }
    }

    private static void addJavaFrom(File file, List<String> list, String string) {
        if (!file.isDirectory()) {
            return;
        }
        File file2 = new File(new File(file, "bin"), string);
        if (file2.isFile()) {
            list.add(file2.getAbsolutePath());
            return;
        }
        File[] fileArray = file.listFiles(File::isDirectory);
        if (fileArray == null) {
            return;
        }
        for (File file3 : fileArray) {
            File file4 = new File(new File(file3, "bin"), string);
            if (file4.isFile()) {
                list.add(file4.getAbsolutePath());
                continue;
            }
            File file5 = new File(new File(new File(file3, "Contents"), "Home"), "bin");
            File file6 = new File(file5, string);
            if (!file6.isFile()) continue;
            list.add(file6.getAbsolutePath());
        }
    }

    private static void deleteRecursive(File file) {
        File[] fileArray = file.listFiles();
        if (fileArray != null) {
            for (File file2 : fileArray) {
                JavaRuntime.deleteRecursive(file2);
            }
        }
        file.delete();
    }

    private static void download(String string, File file, DownloadListener downloadListener) throws IOException {
        file.getParentFile().mkdirs();
        File file2 = new File(file.getParentFile(), file.getName() + ".part");
        IOException iOException = null;
        for (int i = 1; i <= 3; ++i) {
            try {
                JavaRuntime.downloadOnce(string, file2, downloadListener);
                Files.move(file2.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return;
            }
            catch (IOException iOException2) {
                iOException = iOException2;
                file2.delete();
                if (i >= 3) continue;
                try {
                    Thread.sleep(1000L * (long)i);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                continue;
            }
        }
        throw iOException != null ? iOException : new IOException("\u0130ndirme ba\u015far\u0131s\u0131z: " + string);
    }

    private static void downloadOnce(String string, File file, DownloadListener downloadListener) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection)URI.create(string).toURL().openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setConnectTimeout(15000);
        httpURLConnection.setReadTimeout(300000);
        httpURLConnection.setRequestProperty("User-Agent", "Complex-Launcher/2.0");
        int n = httpURLConnection.getResponseCode();
        if (n < 200 || n >= 300) {
            throw new IOException("HTTP " + n);
        }
        long l = httpURLConnection.getContentLengthLong();
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream());
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));){
            int n2;
            byte[] byArray = new byte[8192];
            long l2 = 0L;
            while ((n2 = ((InputStream)bufferedInputStream).read(byArray)) != -1) {
                ((OutputStream)bufferedOutputStream).write(byArray, 0, n2);
                l2 += (long)n2;
                if (downloadListener == null) continue;
                downloadListener.onBytes(l2, l);
            }
        }
    }

    private static void unzip(File file, File file2) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));){
            ZipEntry zipEntry;
            byte[] byArray = new byte[8192];
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                File file3 = JavaRuntime.safeResolve(file2, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    file3.mkdirs();
                    continue;
                }
                file3.getParentFile().mkdirs();
                try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file3));){
                    int n;
                    while ((n = zipInputStream.read(byArray)) != -1) {
                        ((OutputStream)bufferedOutputStream).write(byArray, 0, n);
                    }
                }
                zipInputStream.closeEntry();
            }
        }
    }

    private static void untargz(File file, File file2) throws IOException {
        try (GZIPInputStream gZIPInputStream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));){
            int n;
            byte[] byArray = new byte[512];
            while ((n = JavaRuntime.readFully(gZIPInputStream, byArray)) >= 512) {
                if (JavaRuntime.isZeroBlock(byArray)) {
                    break;
                }
                String string = JavaRuntime.readString(byArray, 0, 100);
                long l = JavaRuntime.parseOctal(byArray, 124, 12);
                int n2 = byArray[156] & 0xFF;
                String string2 = JavaRuntime.readString(byArray, 157, 100);
                if (n2 == 76 || n2 == 75 || n2 == 120 || n2 == 103) {
                    JavaRuntime.skipFully(gZIPInputStream, l + JavaRuntime.padding(l));
                    continue;
                }
                File file3 = JavaRuntime.safeResolve(file2, string);
                if (n2 == 53 || string.endsWith("/")) {
                    file3.mkdirs();
                    JavaRuntime.skipFully(gZIPInputStream, l + JavaRuntime.padding(l));
                    continue;
                }
                if (n2 == 50) {
                    file3.getParentFile().mkdirs();
                    try {
                        Files.createSymbolicLink(file3.toPath(), Path.of(string2, new String[0]), new FileAttribute[0]);
                    }
                    catch (Exception exception) {
                        JavaRuntime.copyLinkTarget(file2, file3, string2);
                    }
                    JavaRuntime.skipFully(gZIPInputStream, l + JavaRuntime.padding(l));
                    continue;
                }
                if (n2 == 49) {
                    file3.getParentFile().mkdirs();
                    JavaRuntime.copyLinkTarget(file2, file3, string2);
                    JavaRuntime.skipFully(gZIPInputStream, l + JavaRuntime.padding(l));
                    continue;
                }
                file3.getParentFile().mkdirs();
                try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file3));){
                    int n3;
                    byte[] byArray2 = new byte[8192];
                    for (long i = l; i > 0L; i -= (long)n3) {
                        n3 = ((InputStream)gZIPInputStream).read(byArray2, 0, (int)Math.min((long)byArray2.length, i));
                        if (n3 == -1) {
                            break;
                        }
                        ((OutputStream)bufferedOutputStream).write(byArray2, 0, n3);
                    }
                }
                JavaRuntime.skipFully(gZIPInputStream, JavaRuntime.padding(l));
            }
        }
    }

    private static void copyLinkTarget(File file, File file2, String string) {
        try {
            File file3 = new File(file2.getParentFile(), string);
            if (file3.isFile()) {
                Files.copy(file3.toPath(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static long padding(long l) {
        return (512L - l % 512L) % 512L;
    }

    private static File safeResolve(File file, String string) {
        File file2 = new File(file, string).getAbsoluteFile();
        String string2 = file.getAbsolutePath();
        if (!file2.getPath().startsWith(string2)) {
            throw new IllegalArgumentException("Ge\u00e7ersiz ar\u015fiv yolu: " + string);
        }
        return file2;
    }

    private static boolean isZeroBlock(byte[] byArray) {
        for (byte by : byArray) {
            if (by == 0) continue;
            return false;
        }
        return true;
    }

    private static String readString(byte[] byArray, int n, int n2) {
        int n3;
        for (n3 = n; n3 < n + n2 && byArray[n3] != 0; ++n3) {
        }
        return new String(byArray, n, n3 - n, StandardCharsets.UTF_8);
    }

    private static long parseOctal(byte[] byArray, int n, int n2) {
        String string = JavaRuntime.readString(byArray, n, n2).trim();
        if (string.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(string, 8);
        }
        catch (NumberFormatException numberFormatException) {
            return 0L;
        }
    }

    private static int readFully(InputStream inputStream, byte[] byArray) throws IOException {
        int n;
        int n2;
        for (n = 0; n < byArray.length && (n2 = inputStream.read(byArray, n, byArray.length - n)) != -1; n += n2) {
        }
        return n;
    }

    private static void skipFully(InputStream inputStream, long l) throws IOException {
        int n;
        byte[] byArray = new byte[8192];
        for (long i = l; i > 0L && (n = inputStream.read(byArray, 0, (int)Math.min((long)byArray.length, i))) != -1; i -= (long)n) {
        }
    }

    public static interface ProgressCallback {
        public void onLog(String var1);

        public void onProgress(int var1, String var2);
    }

    private static interface DownloadListener {
        public void onBytes(long var1, long var3);
    }
}

