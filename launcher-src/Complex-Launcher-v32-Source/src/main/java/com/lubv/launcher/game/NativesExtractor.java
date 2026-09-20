/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.game;

import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.game.GameVersion;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NativesExtractor {
    private static final String[] NATIVE_EXTS = new String[]{".dll", ".so", ".dylib", ".jnilib"};

    /**
     * V29 PERFORMANS: natives HER ACILISTA yeniden cikariliyordu (zip
     * acma + disk I/O). Artik hedef klasorun yaniina yazilan .natives-ready
     * marker dosyasi kaynak JAR'larin (ad+boyut+mtime) parmak izini
     * tutar; iz degismediyse cikarma tamamen atlanir. Kaynak degisirse
     * (surum degisikligi, bozuk/eksik dosya) eski icerik temizlenip
     * yeniden cikarilir - dogru davranis korunur.
     */
    public static File extract(GameVersion gameVersion, List<GameVersion.LibraryEntry> list, Consumer<String> consumer) throws IOException {
        File file;
        File file2 = file = new File(Paths.NATIVES_ROOT_DIR, gameVersion.id);
        if (gameVersion.nativesLibraryPath != null && !gameVersion.nativesLibraryPath.isEmpty()) {
            file2 = new File(file, gameVersion.nativesLibraryPath);
        }
        file2.mkdirs();
        // --- Kaynak JAR parmak izini kur ---
        StringBuilder fingerprint = new StringBuilder();
        java.util.List<File> nativeJars = new java.util.ArrayList<>();
        for (GameVersion.LibraryEntry libraryEntry : list) {
            if (!libraryEntry.isNative || libraryEntry.nativeUrl == null) continue;
            File file3 = new File(Paths.LIBRARIES_DIR, libraryEntry.nativePath);
            try {
                if (!HttpUtil.sha1Matches(file3, libraryEntry.nativeSha1)) {
                    consumer.accept("  Native indiriliyor: " + libraryEntry.name);
                    HttpUtil.downloadFile(libraryEntry.nativeUrl, file3);
                }
            }
            catch (Exception exception) {
                consumer.accept("  ! Native indirilemedi/\u00e7\u0131kar\u0131lamad\u0131: " + libraryEntry.name + " \u2014 " + exception.getMessage());
                continue;
            }
            nativeJars.add(file3);
            fingerprint.append(file3.getName()).append(':').append(file3.length()).append(':').append(file3.lastModified()).append(';');
        }
        // --- Marker kontrolu: iz ayniysa cikarma yok ---
        File marker = new File(file2.getParentFile(), ".natives-ready");
        String expectedFp = gameVersion.id + "|" + file2.getName() + "|" + fingerprint;
        if (nativeJars.isEmpty()) {
            // Ayri native JAR yok: client JAR'a bakilacak (asagida) - marker
            // mantigi client JAR icin de gecerli olur.
            fingerprint.append("client:0");
            expectedFp = gameVersion.id + "|" + file2.getName() + "|" + fingerprint;
        }
        if (marker.isFile() && !nativeJars.isEmpty()) {
            try {
                String saved = new String(java.nio.file.Files.readAllBytes(marker.toPath()), java.nio.charset.StandardCharsets.UTF_8).trim();
                if (saved.equals(expectedFp) && file2.isDirectory() && file2.list().length > 0) {
                    consumer.accept("  Native dosyalar guncel, cikarma atlandi (hizli acilis)");
                    return file2;
                }
            } catch (Exception ignored) {
                // Marker okunamadi -> yeniden cikar.
            }
        }
        int n = 0;
        if (nativeJars.size() > 1) {
            // V29: birden fazla native JAR varsa PARALEL cikar.
            java.util.concurrent.atomic.AtomicInteger total = new java.util.concurrent.atomic.AtomicInteger(0);
            java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(Math.min(4, nativeJars.size()), r -> {
                Thread t = new Thread(r, "natives-extract");
                t.setDaemon(true);
                return t;
            });
            try {
                // Lambda yakalamasi icin efektif-final kopya (file2 yukarida
                // iki kez atandigi icin dogrudan yakalanamaz).
                final File targetDir = file2;
                final java.util.function.Consumer<String> logFn = consumer;
                java.util.List<java.util.concurrent.Future<Integer>> futures = new java.util.ArrayList<>();
                for (final File jar : nativeJars) {
                    futures.add(pool.submit(() -> NativesExtractor.extractJar(jar, targetDir, logFn)));
                }
                for (java.util.concurrent.Future<Integer> f : futures) {
                    try {
                        total.addAndGet(f.get());
                    } catch (Exception ignored) {
                    }
                }
            } finally {
                pool.shutdown();
            }
            n = total.get();
        } else {
            for (File jar : nativeJars) {
                n += NativesExtractor.extractJar(jar, file2, consumer);
            }
        }
        if (n == 0) {
            int n2;
            consumer.accept("  Ayr\u0131 native JAR bulunamad\u0131, client JAR taran\u0131yor...");
            File file4 = gameVersion.clientJarFile();
            if (file4.isFile() && (n2 = NativesExtractor.extractJar(file4, file2, consumer)) > 0) {
                n = n2;
                consumer.accept("  Client JAR'dan " + n2 + " native dosya \u00e7\u0131kar\u0131ld\u0131");
            }
        }
        if (n == 0) {
            consumer.accept("  \u26a0 Hi\u00e7 native dosya \u00e7\u0131kar\u0131lamad\u0131 \u2014 oyun \u00e7al\u0131\u015fmayabilir");
        } else {
            // Basarili cikarma sonrasi marker yaz - bir sonraki acilis atlar.
            try {
                java.nio.file.Files.write(marker.toPath(), expectedFp.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
            consumer.accept("  " + n + " native dosya haz\u0131rland\u0131: " + file2.getAbsolutePath());
        }
        return file2;
    }

    private static int extractJar(File file, File file2, Consumer<String> consumer) throws IOException {
        if (!file.isFile()) {
            return 0;
        }
        int n = 0;
        HashSet<String> hashSet = new HashSet<String>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));){
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String string;
                String string2 = zipEntry.getName();
                if (zipEntry.isDirectory() || string2.startsWith("META-INF/") || !NativesExtractor.isNativeFile(string2) || !hashSet.add(string = new File(string2).getName())) continue;
                File file3 = new File(file2, string);
                try (FileOutputStream fileOutputStream = new FileOutputStream(file3);){
                    zipInputStream.transferTo(fileOutputStream);
                }
                catch (IOException iOException) {
                    zipInputStream.closeEntry();
                    file3.delete();
                    throw iOException;
                }
                ++n;
            }
        }
        return n;
    }

    private static boolean isNativeFile(String string) {
        String string2 = string.toLowerCase();
        for (String string3 : NATIVE_EXTS) {
            if (!string2.endsWith(string3)) continue;
            return true;
        }
        return false;
    }
}

