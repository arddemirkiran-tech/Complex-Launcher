/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.game;

import com.lubv.launcher.auth.MinecraftSession;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.L10n;
import com.lubv.launcher.core.NameChangeServer;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.core.SessionServerMock;
import com.lubv.launcher.game.ArgumentBuilder;
import com.lubv.launcher.game.AssetDownloader;
import com.lubv.launcher.game.AutoLoginBot;
import com.lubv.launcher.game.FabricInstaller;
import com.lubv.launcher.game.ForgeInstaller;
import com.lubv.launcher.game.GameVersion;
import com.lubv.launcher.game.JavaRuntime;
import com.lubv.launcher.game.NativesExtractor;
import com.lubv.launcher.game.NeoForgeInstaller;
import com.lubv.launcher.game.OptiFineInstaller;
import com.lubv.launcher.game.OsRules;
import com.lubv.launcher.game.VersionManifest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GameLauncher {
    private static NameChangeServer activeNameServer = null;
    private static volatile String _pendingServerHost = null;
    private static volatile int _pendingServerPort = 25565;
    private static volatile String _pendingServerPassword = null;

    private static String formatPlayTime(long l) {
        if (l < 60L) {
            return l + "s";
        }
        if (l < 3600L) {
            return l / 60L + "dk";
        }
        return l / 3600L + "s " + l % 3600L / 60L + "dk";
    }

    public static NameChangeServer getActiveNameServer() {
        return activeNameServer;
    }

    public static Process launch(VersionManifest versionManifest, String string, String string2, MinecraftSession minecraftSession, int n, String string3, String string4, String string5, String string6, File file, String string7, int n2, String string8, ProgressCallback progressCallback) throws Exception {
        _pendingServerHost = string7;
        _pendingServerPort = n2;
        _pendingServerPassword = string8;
        return GameLauncher.launch(versionManifest, string, string2, minecraftSession, n, string3, string4, string5, string6, file, progressCallback);
    }

    /**
     * Maven-layout kutuphane yolundan koordinat uretir:
     * "org/ow2/asm/asm/9.6/asm-9.6.jar" -> "org.ow2.asm:asm".
     * Ayni kutuphanenin farkli surumlerini dedup/override etmek icin.
     */
    private static String libCoordinate(String path) {
        try {
            // Maven layout: <group path>/<artifact>/<version>/<artifact>-<version>.jar
            // Son grup segmenti = artifact adi (org/ow2/asm/asm/9.6/... ->
            // group=org.ow2.asm, artifact=asm). Surum sonekini dosya adindan
            // soymak yetmez: bazi jar'larda dosya adi version'dan farkli
            // olabilir; artifact'i yol yapısindan almak en guvenlisi.
            String verDir = path.substring(0, path.lastIndexOf('/'));   // org/ow2/asm/asm/9.6
            int vSlash = verDir.lastIndexOf('/');
            String version = verDir.substring(vSlash + 1);              // 9.6
            String groupPath = verDir.substring(0, vSlash);             // org/ow2/asm/asm
            int aSlash = groupPath.lastIndexOf('/');
            String artifact = aSlash >= 0 ? groupPath.substring(aSlash + 1) : groupPath; // asm
            String group = (aSlash >= 0 ? groupPath.substring(0, aSlash) : groupPath).replace('/', '.'); // org.ow2.asm
            // V32 FIX: classifier'i koordinata dahil et. Forge'un "universal"
            // ve "client" jar'lari ayni Maven koordinatinda
            // (net.minecraftforge:forge) ama farkli classifier'dadir. Ikisi de
            // ayni anahtara dusunce son giren "client" kaliyor, "universal"
            // classpath'ten duser; FML de icinde META-INF/mods.toml olan
            // universal jar'i bulamadigi icin "Failed to find system mod:
            // forge" ile cokuyordu (Forge 1.21+ / NeoForge 1.21+).
            // Classifier farklari ayri kutuphane olarak kalmali.
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String base = artifact + "-" + version;
            if (fileName.length() > base.length() + 4 && fileName.startsWith(base) && fileName.endsWith(".jar")) {
                String rest = fileName.substring(base.length(), fileName.length() - 4); // ".jar" oncesi
                if (rest.startsWith("-") && rest.length() > 1) {
                    return group + ":" + artifact + ":" + rest.substring(1);
                }
            }
            return group + ":" + artifact;
        }
        catch (Exception exception) {
            return path;
        }
    }

    private static boolean isVersion1_13OrNewer(String string) {
        if (string == null) {
            return false;
        }
        try {
            String[] stringArray = string.replace("-OptiFine", "").replace("-Forge", "").split("\\.");
            if (stringArray.length >= 2) {
                int n = Integer.parseInt(stringArray[0]);
                int n2 = Integer.parseInt(stringArray[1]);
                if (n != 1) {
                    return n >= 20;
                }
                return n2 >= 13;
            }
        }
        catch (Exception exception) {
            return false;
        }
        return false;
    }

    private static boolean isVersion1_20OrNewer(String string) {
        if (string == null) {
            return false;
        }
        try {
            String[] stringArray = string.replace("-OptiFine", "").replace("-Forge", "").split("\\.");
            if (stringArray.length >= 2) {
                int n = Integer.parseInt(stringArray[0]);
                int n2 = Integer.parseInt(stringArray[1]);
                if (n != 1) {
                    return n >= 20;
                }
                return n2 >= 20;
            }
        }
        catch (Exception exception) {
            return false;
        }
        return false;
    }

    public static Process launch(VersionManifest versionManifest, String string, String string2, MinecraftSession minecraftSession, int n, String string3, String string4, String string5, String string6, File file, ProgressCallback progressCallback) throws Exception {
        PrintWriter printWriter;
        File file2 = new File(file, "launcher_game_output.log");
        try {
            file.mkdirs();
            printWriter = new PrintWriter((Writer)new FileWriter(file2, false), true);
            printWriter.println("=== Launch baslangici " + String.valueOf(new Date()) + " ===");
            printWriter.println("versionId=" + string + " loader=" + string2);
        }
        catch (IOException iOException) {
            printWriter = null;
        }
        PrintWriter printWriter2 = printWriter;
        try {
            return GameLauncher.launchInner(versionManifest, string, string2, minecraftSession, n, string3, string4, string5, string6, file, progressCallback, printWriter2);
        }
        catch (Throwable throwable) {
            if (printWriter2 != null) {
                printWriter2.println("[FATAL] launch() disariya exception firlatti: " + throwable.getClass().getName() + ": " + throwable.getMessage());
                StringWriter stringWriter = new StringWriter();
                throwable.printStackTrace(new PrintWriter(stringWriter));
                printWriter2.println(stringWriter.toString());
                printWriter2.close();
            }
            if (throwable instanceof Exception) {
                throw (Exception)throwable;
            }
            throw new Exception(throwable);
        }
    }

    private static Process launchInner(VersionManifest versionManifest, String string, String string2, MinecraftSession minecraftSession, int n3, String string3, String string4, String string5, String string6, File file, ProgressCallback progressCallback, final PrintWriter printWriter) throws Exception {
        final ProgressCallback progressCallback2 = progressCallback;
        final ProgressCallback progressCallback3 = new ProgressCallback(){

            @Override
            public void onLog(String string) {
                progressCallback2.onLog(string);
                if (printWriter != null) {
                    printWriter.println(string);
                }
            }

            @Override
            public void onProgress(int n, String string) {
                progressCallback2.onProgress(n, string);
                if (printWriter != null) {
                    printWriter.println("[progress " + n + "%] " + string);
                }
            }
        };
        Paths.ensureAll();
        progressCallback3.onLog("S\u00fcr\u00fcm bilgisi al\u0131n\u0131yor: " + string);
        progressCallback3.onProgress(2, "S\u00fcr\u00fcm bilgisi al\u0131n\u0131yor");
        VersionManifest.Entry entry = versionManifest.find(string);
        if (entry == null) {
            if (printWriter != null) {
                printWriter.println("[HATA] S\u00fcr\u00fcm bulunamad\u0131: " + string);
                printWriter.close();
            }
            throw new Exception("S\u00fcr\u00fcm bulunamad\u0131: " + string);
        }
        GameVersion gameVersion = GameVersion.parse(string, entry.url);
        progressCallback3.onProgress(8, "Temel s\u00fcr\u00fcm ayr\u0131\u015ft\u0131r\u0131ld\u0131");
        progressCallback3.onLog("Java kontrol ediliyor...");
        progressCallback3.onProgress(8, "Java kontrol ediliyor");
        String string8 = JavaRuntime.ensureJava(gameVersion.javaMajorVersion, string4, new JavaRuntime.ProgressCallback(){

            @Override
            public void onLog(String string) {
                progressCallback3.onLog(string);
            }

            @Override
            public void onProgress(int n, String string) {
                progressCallback3.onProgress(Math.min(20, 8 + Math.max(n, 0) * 12 / 100), string);
            }
        });
        int n4 = JavaRuntime.detectMajor(string8);
        progressCallback3.onLog("Bu s\u00fcr\u00fcm Java " + gameVersion.javaMajorVersion + "+ gerektiriyor.");
        progressCallback3.onLog("Java: " + string8 + (n4 > 0 ? " (v" + n4 + ")" : ""));
        if (n4 > 0 && n4 < gameVersion.javaMajorVersion) {
            progressCallback3.onLog("Se\u00e7ilen Java bu s\u00fcr\u00fcm i\u00e7in eski olabilir.");
        }
        ArrayList<GameVersion.LibraryEntry> arrayList = new ArrayList<GameVersion.LibraryEntry>(gameVersion.libraries);
        String string9 = null;
        GameVersion loaderProfile = null;
        ArrayList<String> arrayList2 = new ArrayList<String>();
        if ("Fabric".equalsIgnoreCase(string2)) {
            progressCallback3.onLog("Fabric loader kuruluyor...");
            progressCallback3.onProgress(12, "Fabric kuruluyor");
            String fabricLoaderVersion = FabricInstaller.getLatestLoaderVersion(string);
            GameVersion fabricResult = FabricInstaller.installFabric(string, fabricLoaderVersion, progressCallback3::onLog);
            if (fabricResult != null && fabricResult.libraries != null) {
                arrayList.addAll(fabricResult.libraries);
            }
            string9 = fabricResult != null ? fabricResult.mainClass : null;
            loaderProfile = fabricResult;
        } else if ("Forge".equalsIgnoreCase(string2)) {
            progressCallback3.onLog("Forge loader kuruluyor...");
            progressCallback3.onProgress(12, "Forge kuruluyor");
            String forgeVersion = ForgeInstaller.getRecommendedForgeVersion(string);
            GameVersion forgeResult = ForgeInstaller.installForge(string, forgeVersion, string8, progressCallback3::onLog);
            if (forgeResult != null && forgeResult.libraries != null) {
                arrayList.addAll(forgeResult.libraries);
            }
            string9 = forgeResult != null ? forgeResult.mainClass : null;
            loaderProfile = forgeResult;
        } else if ("NeoForge".equalsIgnoreCase(string2)) {
            progressCallback3.onLog("NeoForge loader kuruluyor...");
            progressCallback3.onProgress(12, "NeoForge kuruluyor");
            String neoForgeVersion = NeoForgeInstaller.getLatestNeoForgeVersion(string);
            GameVersion neoForgeResult = NeoForgeInstaller.installNeoForge(string, string8, neoForgeVersion, progressCallback3::onLog);
            if (neoForgeResult != null && neoForgeResult.libraries != null) {
                arrayList.addAll(neoForgeResult.libraries);
            }
            string9 = neoForgeResult != null ? neoForgeResult.mainClass : null;
            loaderProfile = neoForgeResult;
        } else if ("OptiFine".equalsIgnoreCase(string2)) {
            progressCallback3.onLog("OptiFine kuruluyor...");
            progressCallback3.onProgress(12, "OptiFine kuruluyor");
            try {
                progressCallback3.onLog("OptiFine i\u00e7in Forge altyap\u0131s\u0131 haz\u0131rlan\u0131yor...");
                String forgeVersion = ForgeInstaller.getRecommendedForgeVersion(string);
                GameVersion forgeResult = ForgeInstaller.installForge(string, forgeVersion, string8, progressCallback3::onLog);
                if (forgeResult == null) {
                    throw new Exception("Forge kurulamad\u0131 \u2014 OptiFine art\u0131k Forge olmadan \u00e7al\u0131\u015fm\u0131yor (1.13+ s\u00fcr\u00fcmlerde).");
                }
                if (forgeResult.libraries != null) {
                    arrayList.addAll(forgeResult.libraries);
                }
                string9 = forgeResult.mainClass;
                loaderProfile = forgeResult;
                progressCallback3.onLog("Forge kuruldu, \u015fimdi OptiFine ekleniyor...");
                String optiFineVersion = OptiFineInstaller.getLatestOptiFineVersion(string);
                File modsDirForOptiFine = new File(file, "mods");
                File file2 = OptiFineInstaller.installOptiFineAsMod(string, optiFineVersion, modsDirForOptiFine, progressCallback3::onLog);
                if (file2 != null) {
                    progressCallback3.onLog("OptiFine mod olarak eklendi: " + file2.getName());
                }
                progressCallback3.onLog("OptiFine kurulumu tamamland\u0131!");
            }
            catch (Exception exception) {
                progressCallback3.onLog("[HATA] OptiFine y\u00fcklenemedi: " + exception.getMessage());
                progressCallback3.onLog("OptiFine olmadan orijinal s\u00fcr\u00fcmle devam ediliyor...");
            }
        }
        progressCallback3.onLog("Minecraft istemci JAR'\u0131 kontrol ediliyor...");
        progressCallback3.onProgress(18, "\u0130stemci JAR indiriliyor");
        File clientJarFile = gameVersion.clientJarFile();
        // V34.8 BASLANGIC HIZI: istemci JAR (25MB+) daha once kutuphanelerden
        // ONCE tek basina iniyordu; 400+ kutuphane indirmesi onu bekliyordu.
        // Artik JAR arka planda inerken kutuphaneler ayni anda iner - iki ag
        // yuku bindiginden toplam hazirlik suresi kisalir (bant genisligi
        // genellikle RTT'ye gore boldur, paralel akis doldurur).
        final java.util.concurrent.atomic.AtomicReference<Exception> clientJarError = new java.util.concurrent.atomic.AtomicReference<Exception>();
        Thread clientJarThread = null;
        if (clientJarFile != null) {
            clientJarFile.getParentFile().mkdirs();
            if (!HttpUtil.sha1Matches(clientJarFile, gameVersion.clientJarSha1)) {
                progressCallback3.onLog("\u0130stemci JAR indiriliyor...");
                final String cjUrl = gameVersion.clientJarUrl;
                final File cjDest = clientJarFile;
                clientJarThread = new Thread(() -> {
                    try {
                        HttpUtil.downloadFile(cjUrl, cjDest);
                    }
                    catch (Exception e) {
                        clientJarError.set(e);
                    }
                }, "client-jar-download");
                clientJarThread.setDaemon(true);
                clientJarThread.start();
                progressCallback3.onLog(" \u0130stemci JAR arka planda indiriliyor (k\u00fct\u00fcphanelerle paralel)");
            } else {
                progressCallback3.onLog(" \u0130stemci JAR zaten mevcut");
            }
        }
        // V34.8: asset index JSON'unu da paralel cek (500KB civari) -
        // kaynalarin ilk baytini beklemeden index hazir olsun.
        AssetDownloader.prefetchIndexAsync(gameVersion);
        // V35.1 ACILIS HIZI: kaynagi da kutuphanelerle ORTUSTUR. Asset
        // index'i geldiğinde (prefetch arka planda) eksik asset'leri
        // PARALEL indirmeye baslar; kutuphane indirmesi bittiginde
        // asset'lerin buyuk bolumu zaten inmis olur. AssetDownloader
        // kendi katilim noktasinda kalanlari tamamlar.
        AssetDownloader.warmAssetsAsync(gameVersion);
        LinkedHashMap<String, GameVersion.LibraryEntry> dedupedLibs = new LinkedHashMap<String, GameVersion.LibraryEntry>();
        // V33.1 FIX: Dedup artik koordinat bazli (grup:artifact). Vanilla
        // manifest'i or. asm-9.6 getirirken Fabric/Forge profili ayni
        // kutuphanenin daha yeni surumunu (asm-9.10.1) yeniden bildirir;
        // eski tam-yol dedup'i ikisini de classpath'te birakir ve Fabric
        // Loader 0.15+ acilista "duplicate ASM classes found on classpath"
        // ile coker (KnotClient ExceptionInInitializerError, oyun 3 sn'de
        // kapanir). Loader kutuphaneleri listeye SONRA eklendigi icin put
        // = override: ayni koordinatta loader surumu kazanir (resmi
        // launcher davranisi). Natives jar'lari tam yollarla anahtarlanir
        // (classifier farklari ezilmesin).
        for (GameVersion.LibraryEntry libraryEntry : arrayList) {
            String key = libraryEntry.isNative ? libraryEntry.path : libCoordinate(libraryEntry.path);
            dedupedLibs.put(key, libraryEntry);
        }
        arrayList = new ArrayList<GameVersion.LibraryEntry>(dedupedLibs.values());
        progressCallback3.onLog("K\u00fct\u00fcphaneler indiriliyor (" + arrayList.size() + " adet)...");
        // V29 PERFORMANS: kutuphaneler ONCEDEN TEK TEK indiriliyordu -
        // ~400 kutuphane icin bu, baglanti gecikmesi (RTT) kadar sikisan
        // bir seri akis demekti. Artik 10 is parcacikli sabit havuzla
        // paralel iniyor: cok daha az RTT beklemesi, cok daha kisa
        // hazirlik suresi. SHA-1/disk kontrolleri ve "atla" mantigi
        // aynen korunur; sadece SIRA paralellesti.
        int n5 = arrayList.size();
        java.util.concurrent.atomic.AtomicInteger downloadedCount = new java.util.concurrent.atomic.AtomicInteger(0);
        int libPoolSize = Math.max(4, Math.min(10, Runtime.getRuntime().availableProcessors() * 2));
        java.util.concurrent.ExecutorService libPool = java.util.concurrent.Executors.newFixedThreadPool(libPoolSize, r -> {
            Thread t = new Thread(r, "lib-download");
            t.setDaemon(true);
            return t;
        });
        try {
            java.util.List<java.util.concurrent.Future<?>> libFutures = new ArrayList<>();
            for (GameVersion.LibraryEntry libraryEntry : arrayList) {
                libFutures.add(libPool.submit(() -> {
                    try {
                        if (libraryEntry.isNative || libraryEntry.url == null) {
                            return;
                        }
                        File libFile = new File(Paths.LIBRARIES_DIR, libraryEntry.path);
                        if (!HttpUtil.sha1Matches(libFile, libraryEntry.sha1) && !libFile.exists()) {
                            File file3 = libFile.getParentFile();
                            if (file3 != null) {
                                file3.mkdirs();
                            }
                            HttpUtil.downloadFile(libraryEntry.url, libFile);
                        }
                    } catch (Exception exception) {
                        progressCallback3.onLog("  ! Atlan\u0131yor: " + libraryEntry.name);
                    } finally {
                        int done = downloadedCount.incrementAndGet();
                        progressCallback3.onProgress(18 + (int)((double)done / (double)Math.max(n5, 1) * 30.0), "K\u00fct\u00fcphaneler indiriliyor");
                    }
                }));
            }
            for (java.util.concurrent.Future<?> libFuture : libFutures) {
                try {
                    libFuture.get();
                } catch (Exception exception) {
                    // Tek kutuphane hatasi tum acilisi bloklamaz - eski
                    // davranista da hatali kutuphane "atlandi" olarak log
                    // lanip devam ediliyordu.
                }
            }
        } finally {
            libPool.shutdown();
        }
        progressCallback3.onLog("K\u00fct\u00fcphaneler haz\u0131r");
        // V34.8: paralel client JAR indirmesini bekle. Hata varsa gercek
        // indirme akisi gibi launch'i dusur (sessizce yarim jar birakma).
        if (clientJarThread != null) {
            try {
                clientJarThread.join();
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
            Exception cje = clientJarError.get();
            if (cje != null) {
                throw new Exception("\u0130stemci JAR indirilemedi: " + cje.getMessage(), cje);
            }
            progressCallback3.onLog(" \u0130stemci JAR indirildi (paralel)");
        }
        progressCallback3.onLog("Native dosyalar haz\u0131rlan\u0131yor...");
        progressCallback3.onProgress(50, "Native dosyalar haz\u0131rlan\u0131yor");
        File file4 = NativesExtractor.extract(gameVersion, arrayList, progressCallback3::onLog);
        progressCallback3.onLog("Native dosyalar haz\u0131r");
        progressCallback3.onLog("Oyun kaynaklar\u0131 indiriliyor...");
        progressCallback3.onProgress(55, "Kaynaklar indiriliyor");
        AssetDownloader.download(gameVersion, (n, n2) -> {
            int n3b = 55 + (int)((double)n.intValue() / (double)Math.max(n2, 1) * 35.0);
            progressCallback3.onProgress(Math.min(n3b, 90), "Kaynaklar indiriliyor (" + n + "/" + n2 + ")");
        });
        progressCallback3.onLog("Kaynaklar haz\u0131r");
        StringBuilder stringBuilder = new StringBuilder();
        if (clientJarFile != null) {
            stringBuilder.append(clientJarFile.getAbsolutePath());
        }
        for (GameVersion.LibraryEntry libraryEntry : arrayList) {
            File libFile = new File(Paths.LIBRARIES_DIR, libraryEntry.path);
            if (libraryEntry.isNative || !libFile.exists()) continue;
            stringBuilder.append(File.pathSeparator).append(libFile.getAbsolutePath());
        }
        if (activeNameServer != null) {
            try {
                activeNameServer.stop();
            }
            catch (Exception exception) {
                // empty catch block
            }
            activeNameServer = null;
        }
        NameChangeServer nameChangeServer = new NameChangeServer(minecraftSession, progressCallback3::onLog);
        nameChangeServer.setLaunchContext(versionManifest, string, string2, n3, string3, string4, string5, file);
        try {
            nameChangeServer.start();
            activeNameServer = nameChangeServer;
            progressCallback3.onLog("NameChangeServer haz\u0131r (port " + nameChangeServer.getBoundPort() + ")");
        }
        catch (Exception exception) {
            progressCallback3.onLog("NameChangeServer ba\u015flat\u0131lamad\u0131: " + exception.getMessage());
        }
        progressCallback3.onProgress(95, "Ba\u015flat\u0131l\u0131yor");
        int n6 = SessionServerMock.port();
        if (n6 <= 0) {
            int n7 = SessionServerMock.start();
            if (n7 > 0) {
                progressCallback3.onLog("[SessionMock] Ba\u015flat\u0131ld\u0131, port=" + n7);
            } else {
                progressCallback3.onLog("[SessionMock] UYARI: ba\u015flat\u0131lamad\u0131, multiplayer etkilenebilir.");
            }
        }
        if (minecraftSession != null) {
            String skinUrl = minecraftSession.skinUrl != null ? minecraftSession.skinUrl : "";
            SessionServerMock.SkinData skinData = new SessionServerMock.SkinData(minecraftSession.uuid, minecraftSession.username, skinUrl);
            if (minecraftSession.accessToken != null && !minecraftSession.accessToken.isBlank() && !"0".equals(minecraftSession.accessToken)) {
                SessionServerMock.registerToken(minecraftSession.accessToken, skinData);
            }
            SessionServerMock.setSkin(minecraftSession.uuid, skinData);
            progressCallback3.onLog("[SessionMock] Profil kaydedildi: " + minecraftSession.username);
        }
        ArgumentBuilder.LaunchContext launchContext = new ArgumentBuilder.LaunchContext();
        launchContext.version = gameVersion;
        launchContext.loaderProfile = loaderProfile;
        launchContext.session = minecraftSession;
        launchContext.clientJar = clientJarFile;
        launchContext.classpath = stringBuilder.toString();
        launchContext.gameDir = file;
        launchContext.nativesDir = file4;
        launchContext.ramGB = n3;
        launchContext.extraJvmArgs = string3;
        launchContext.gpuSelection = string5;
        launchContext.preLaunchCmd = string6;
        launchContext.forgeMainClassOverride = string9;
        launchContext.extraGameArgs.addAll(arrayList2);
        try {
            String launcherDirPath;
            if (Paths.PORTABLE) {
                launcherDirPath = Paths.getLauncherDir().getAbsolutePath();
            } else {
                launcherDirPath = System.getProperty("launcher.jar.path", "");
                if (launcherDirPath == null || launcherDirPath.isEmpty()) {
                    try {
                        File codeSourceFile = new File(GameLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                        launcherDirPath = codeSourceFile.isFile() ? codeSourceFile.getParent() : (Paths.GAME_DIR.getParentFile() != null ? Paths.GAME_DIR.getParentFile().getAbsolutePath() : System.getProperty("user.dir"));
                    }
                    catch (Exception exception) {
                        launcherDirPath = System.getProperty("user.dir");
                    }
                }
            }
            if (launcherDirPath == null) {
                launcherDirPath = System.getProperty("user.dir");
            }
            File agentFile = new File(launcherDirPath, "namechanger.jar");
            if (!agentFile.isFile()) {
                agentFile = new File(launcherDirPath, "resources/namechanger.jar");
            }
            if (agentFile.isFile()) {
                // V39 GUVENLIK KAPISI: agent sinif surumu, oyunun calisacagi Java
                // surumunden yuksekse -javaagent eklemek oyunu aninda dusurur
                // (UnsupportedClassVersionError / FATAL ERROR in native method).
                // Boyle durumlarda agent'i atla, oyun acilmaya devam etsin.
                int agentMajor = JavaRuntime.readClassMajor(agentFile);
                int gameJava = JavaRuntime.detectMajor(string8);
                if (agentMajor > 0 && gameJava > 0 && agentMajor > gameJava) {
                    progressCallback3.onLog("[Agent] UYARI: namechanger.jar Java " + gameJava
                        + " ile uyumsuz (agent class v" + agentMajor + ") \u2014 bu lans icin atlandi. Oyun etkilenmez.");
                } else {
                    launchContext.extraJvmArgList.add("-javaagent:" + agentFile.getAbsolutePath());
                    progressCallback3.onLog("NameChanger agent y\u00fcklendi: " + agentFile.getName());
                }
            } else {
                progressCallback3.onLog("namechanger.jar bulunamad\u0131 \u2014 isimsiz de\u011fi\u015ftirme devre d\u0131\u015f\u0131.");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        List<String> commandList = ArgumentBuilder.buildFullCommand(launchContext, string8);
        progressCallback3.onLog("=== LAUNCH COMMAND ===");
        StringBuilder commandPreview = new StringBuilder();
        for (String cmdPart : commandList) {
            commandPreview.append(cmdPart).append(" ");
        }
        progressCallback3.onLog(commandPreview.toString());
        progressCallback3.onLog("=== END COMMAND ===");
        String pendingHost = _pendingServerHost;
        String pendingPassword = null;
        if (pendingHost != null && !pendingHost.isBlank()) {
            int n8 = _pendingServerPort;
            pendingPassword = _pendingServerPassword;
            _pendingServerHost = null;
            _pendingServerPort = 25565;
            _pendingServerPassword = null;
            if (GameLauncher.isVersion1_20OrNewer(string)) {
                commandList.add("--quickPlayMultiplayer");
                commandList.add(pendingHost + ":" + n8);
                progressCallback3.onLog("[Join] QuickPlay: " + pendingHost + ":" + n8);
            } else {
                commandList.add("--server");
                commandList.add(pendingHost);
                commandList.add("--port");
                commandList.add(String.valueOf(n8));
                progressCallback3.onLog("[Join] Sunucuya ba\u011flan\u0131l\u0131yor: " + pendingHost + ":" + n8);
            }
        }
        progressCallback3.onLog("Minecraft ba\u015flat\u0131l\u0131yor (" + minecraftSession.username + ")...");
        if (string6 != null && !string6.trim().isEmpty()) {
            try {
                String[] stringArray;
                progressCallback3.onLog("[Pre-launch] " + string6);
                if (OsRules.CURRENT_OS == OsRules.Os.WINDOWS) {
                    String[] stringArray2 = new String[3];
                    stringArray2[0] = "cmd";
                    stringArray2[1] = "/c";
                    stringArray = stringArray2;
                    stringArray2[2] = string6;
                } else {
                    String[] stringArray3 = new String[3];
                    stringArray3[0] = "/bin/sh";
                    stringArray3[1] = "-c";
                    stringArray = stringArray3;
                    stringArray3[2] = string6;
                }
                String[] stringArray4 = stringArray;
                Process preLaunchProcess = new ProcessBuilder(stringArray4).directory(file).redirectErrorStream(true).start();
                if (!preLaunchProcess.waitFor(10L, TimeUnit.SECONDS)) {
                    preLaunchProcess.destroyForcibly();
                    progressCallback3.onLog("[Pre-launch] Zaman a\u015f\u0131m\u0131 (10s), devam ediliyor...");
                } else {
                    progressCallback3.onLog("[Pre-launch] Tamamland\u0131 (\u00e7\u0131k\u0131\u015f: " + preLaunchProcess.exitValue() + ")");
                }
            }
            catch (Exception exception) {
                progressCallback3.onLog("[Pre-launch] Hata (oyun yine de ba\u015flat\u0131l\u0131yor): " + exception.getMessage());
            }
        }
        if (printWriter != null) {
            printWriter.println("Komut: " + String.join(" ", commandList));
        }
        PrintWriter printWriter2 = printWriter;
        File outputLogFile = new File(file, "launcher_game_output.log");
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        processBuilder.directory(file);
        processBuilder.redirectErrorStream(true);
        File file5 = new File(string8).getParentFile();
        if (file5 != null) {
            Map<String, String> env = processBuilder.environment();
            String string10 = env.getOrDefault("PATH", "");
            env.put("PATH", file5.getAbsolutePath() + File.pathSeparator + string10);
            File file6 = file5.getParentFile();
            if (file6 != null) {
                env.put("JAVA_HOME", file6.getAbsolutePath());
            }
        }
        progressCallback3.onLog("[DEBUG] Komut sayisi: " + commandList.size());
        Process process;
        try {
            process = processBuilder.start();
            progressCallback3.onLog("[DEBUG] Process baslatildi, PID: " + process.pid());
        }
        catch (Exception exception) {
            progressCallback3.onLog("[HATA] Process baslatilamadi: " + exception.getMessage());
            if (printWriter2 != null) {
                printWriter2.println("[HATA] Process baslatilamadi: " + exception.getMessage());
                printWriter2.close();
            }
            throw exception;
        }
        long l = System.currentTimeMillis();
        progressCallback3.onProgress(100, "\u00c7al\u0131\u015f\u0131yor");
        if (pendingPassword != null && !pendingPassword.isBlank()) {
            AutoLoginBot.startYazdir(pendingPassword, process, progressCallback3::onLog);
            progressCallback3.onLog("[AutoLogin] /login botu tetiklendi");
        } else {
            Thread outputThread = new Thread(() -> GameLauncher.onMinecraftOutputThread(process, progressCallback3, printWriter2), "minecraft-output");
            outputThread.setDaemon(true);
            outputThread.start();
        }
        final NameChangeServer finalNameServer = nameChangeServer;
        new Thread(() -> GameLauncher.onProcessHealthThread(process, progressCallback3), "process-health").start();
        Thread thread = new Thread(() -> GameLauncher.onMinecraftExitThread(process, l, progressCallback3, printWriter2, launchContext, finalNameServer), "minecraft-exit");
        thread.setDaemon(true);
        thread.start();
        progressCallback3.onLog("Minecraft ba\u015flat\u0131ld\u0131! (\u00c7\u0131kt\u0131 kayd\u0131: " + outputLogFile.getName() + ")");
        return process;
    }

    public static String analyzeCrash(String string) {
        if (string == null) {
            return L10n.get("crash.unknown");
        }
        String string2 = string.toLowerCase();
        StringBuilder stringBuilder = new StringBuilder();
        // --- RAM / bellek ---
        if (string2.contains("outofmemoryerror") || string2.contains("java heap space") || string2.contains("gc overhead limit exceeded")) {
            stringBuilder.append(L10n.get("crash.oom")).append("\n");
        }
        // --- Mod yukleme/cakisma sorunlari ---
        if (string2.contains("missing mod") || string2.contains("modloadingexception") || string2.contains("modresolutionexception") || string2.contains("mod resolution")) {
            stringBuilder.append(L10n.get("crash.mod_issue")).append("\n");
        }
        if (string2.contains("duplicate mod") || string2.contains("duplicatemodsfounderror") || string2.contains("found duplicate mods")) {
            stringBuilder.append(L10n.get("crash.duplicate_mod")).append("\n");
        }
        if (string2.contains("requires") && (string2.contains("but only found") || string2.contains("incompatible"))) {
            stringBuilder.append(L10n.get("crash.mod_version_mismatch")).append("\n");
        }
        if (string2.contains("optifine") && (string2.contains("sodium") || string2.contains("iris") || string2.contains("lithium"))) {
            stringBuilder.append(L10n.get("crash.optifine_conflict")).append("\n");
        }
        // --- Mixin ---
        if (string2.contains("mixinexception") || string2.contains("mixin apply failed") || string2.contains("mixintransformererror")) {
            stringBuilder.append(L10n.get("crash.mixins")).append("\n");
        }
        // --- GPU / render / shader ---
        if (string2.contains("opengl") || string2.contains("gl error") || string2.contains("lwjgl")) {
            stringBuilder.append(L10n.get("crash.driver")).append("\n");
        }
        if (string2.contains("shader") && (string2.contains("compile") || string2.contains("error") || string2.contains("failed"))) {
            stringBuilder.append(L10n.get("crash.shader_error")).append("\n");
        }
        if (string2.contains("gl_out_of_memory") || (string2.contains("vram") && string2.contains("memory"))) {
            stringBuilder.append(L10n.get("crash.gpu_out_of_memory")).append("\n");
        }
        // --- Java surum uyumsuzlugu ---
        if (string2.contains("java.lang.incompatibleclasschangeerror") || string2.contains("unsupportedclassversionerror") || string2.contains("has been compiled by a more recent version")) {
            stringBuilder.append(L10n.get("crash.java")).append("\n");
        }
        // --- Sinif/bagimlilik eksikligi ---
        if (string2.contains("classnotfoundexception") || string2.contains("noclassdeffounderror")) {
            stringBuilder.append(L10n.get("crash.class_not_found")).append("\n");
        }
        // --- Sonsuz dongu / cok derin cagri zinciri ---
        if (string2.contains("stackoverflowerror")) {
            if (string2.contains("worldgen") || string2.contains("chunkgenerator") || string2.contains("biome")) {
                stringBuilder.append(L10n.get("crash.stackoverflow_worldgen")).append("\n");
            } else {
                stringBuilder.append(L10n.get("crash.stack_overflow")).append("\n");
            }
        }
        // --- Es zamanli veri erisimi ---
        if (string2.contains("concurrentmodificationexception")) {
            stringBuilder.append(L10n.get("crash.concurrent_modification")).append("\n");
        }
        // --- NullPointerException (genel mod hatasi belirtisi) ---
        if (string2.contains("nullpointerexception")) {
            stringBuilder.append(L10n.get("crash.null_pointer")).append("\n");
        }
        // --- Dunya / chunk / kayit verisi bozulmasi ---
        if (string2.contains("chunk") && (string2.contains("corrupt") || string2.contains("exception loading chunk") || string2.contains("exception ticking"))) {
            stringBuilder.append(L10n.get("crash.corrupt_world")).append("\n");
        }
        if (string2.contains("nbtexception") || string2.contains("failed to load") && string2.contains(".dat")) {
            stringBuilder.append(L10n.get("crash.saveddata_error")).append("\n");
        }
        // --- Doku paketi / resource pack ---
        if (string2.contains("resourcepack") || string2.contains("resource pack") && string2.contains("error")) {
            stringBuilder.append(L10n.get("crash.texture_pack")).append("\n");
        }
        // --- Tarif / crafting ---
        if (string2.contains("recipe") && (string2.contains("error") || string2.contains("invalid") || string2.contains("duplicate"))) {
            stringBuilder.append(L10n.get("crash.recipe_error")).append("\n");
        }
        // --- Datapack / config JSON hatasi ---
        if (string2.contains("jsonsyntaxexception") || string2.contains("malformedjsonexception")) {
            stringBuilder.append(L10n.get("crash.datapack_error")).append("\n");
        }
        // --- Ag / sunucu baglantisi ---
        if (string2.contains("connection refused") || string2.contains("connect timed out") || string2.contains("io.netty.channel") || string2.contains("unknownhostexception")) {
            stringBuilder.append(L10n.get("crash.network")).append("\n");
        }
        if (string2.contains("address already in use") || string2.contains("bindexception")) {
            stringBuilder.append(L10n.get("crash.port_in_use")).append("\n");
        }
        // --- Hesap / kimlik dogrulama ---
        if (string2.contains("authenticationexception") || string2.contains("invalid session") || string2.contains("invalidcredentialsexception")) {
            stringBuilder.append(L10n.get("crash.auth_error")).append("\n");
        }
        // --- Disk / dosya sistemi ---
        if (string2.contains("no space left on device") || string2.contains("disk full")) {
            stringBuilder.append(L10n.get("crash.disk_space")).append("\n");
        }
        if (string2.contains("accessdeniedexception") || string2.contains("permission denied")) {
            stringBuilder.append(L10n.get("crash.permission")).append("\n");
        }
        if (stringBuilder.length() == 0) {
            for (String string3 : string.split("\n")) {
                String string4 = string3.trim();
                if (!string4.startsWith("Description:") && (!string4.contains("Exception") || !string4.contains(":"))) continue;
                stringBuilder.append(string4, 0, Math.min(string4.length(), 120)).append("\n");
                break;
            }
        }
        if (stringBuilder.length() == 0) {
            stringBuilder.append(L10n.get("crash.unknown"));
        }
        // Crash Analizi v2: hizli kategori taramasinin uzerine derin tani
        // (kok neden, sorumlu mod dosyasi, Java dogrulamasi, onerilen
        // adimlar) eklenir.
        return CrashAnalyzer.enhance(string, stringBuilder.toString().trim());
    }

    public static String analyzeJvmCrash(String string) {
        if (string == null) {
            return L10n.get("crash.native");
        }
        StringBuilder stringBuilder = new StringBuilder(L10n.get("crash.native")).append("\n");
        String[] lines = string.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("# A fatal error") && !trimmed.startsWith("SIGFPE") && !trimmed.startsWith("SIGSEGV") && !trimmed.startsWith("SIGBUS") && !trimmed.startsWith("SIGILL") && !trimmed.startsWith("EXCEPTION_")) continue;
            stringBuilder.append(trimmed, 0, Math.min(trimmed.length(), 120)).append("\n");
            break;
        }
        String lower = string.toLowerCase();
        if (lower.contains("outofmemory") || lower.contains("java.lang.outofmemoryerror") || lower.contains("could not reserve enough space")) {
            stringBuilder.append(L10n.get("crash.oom")).append("\n");
        }
        if (lower.contains("opengl") || lower.contains("nvidia") || lower.contains("amdgpu") || lower.contains("nvoglv") || lower.contains("atioglxx") || lower.contains("igdumdim")) {
            stringBuilder.append(L10n.get("crash.driver")).append("\n");
        }
        if (lower.contains("shader") && (lower.contains("compile") || lower.contains("crash"))) {
            stringBuilder.append(L10n.get("crash.shader_error")).append("\n");
        }
        if (lower.contains("no space left on device") || lower.contains("disk full")) {
            stringBuilder.append(L10n.get("crash.disk_space")).append("\n");
        }
        if (lower.contains("access violation") || lower.contains("permission denied")) {
            stringBuilder.append(L10n.get("crash.permission")).append("\n");
        }
        // Crash Analizi v2: JVM (native) crash raporlarina da derin tani.
        return CrashAnalyzer.enhance(string, stringBuilder.toString().trim());
    }

    private static /* synthetic */ void onMinecraftExitThread(Process process, long l, ProgressCallback progressCallback, PrintWriter printWriter, ArgumentBuilder.LaunchContext launchContext, NameChangeServer nameChangeServer) {
        try {
            int n = process.waitFor();
            long l2 = (System.currentTimeMillis() - l) / 1000L;
            progressCallback.onLog("Minecraft kapand\u0131 (\u00e7\u0131k\u0131\u015f kodu: " + n + ", s\u00fcre: " + GameLauncher.formatPlayTime(l2) + ")");
            if (printWriter != null) {
                printWriter.println("=== Cikis kodu: " + n + " ===");
            }
            progressCallback.onGameExit(n, l2);
            if (launchContext.gameDir != null) {
                String[] stringArray;
                String string2;
                File file2;
                File[] fileArray;
                File file3 = new File(launchContext.gameDir, "crash-reports");
                if (file3.isDirectory() && (fileArray = file3.listFiles((file, string) -> string.endsWith(".txt") || string.endsWith(".log"))) != null && fileArray.length > 0) {
                    Arrays.sort(fileArray, Comparator.comparingLong(File::lastModified).reversed());
                    file2 = fileArray[0];
                    if (System.currentTimeMillis() - file2.lastModified() < 60000L) {
                        try {
                            string2 = new String(Files.readAllBytes(file2.toPath()), StandardCharsets.UTF_8);
                            progressCallback.onLog("=== CRASH REPORT: " + file2.getName() + " ===");
                            stringArray = string2.split("\n");
                            for (int i = 0; i < Math.min(stringArray.length, 60); ++i) {
                                progressCallback.onLog(stringArray[i]);
                            }
                            String string3 = GameLauncher.analyzeCrash(string2);
                            progressCallback.onCrash("CRASH_REPORT", file2.getName(), string3, string2);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
                if (n != 0) {
                    File[] fileArray2 = fileArray = launchContext.gameDir.getParentFile() != null ? launchContext.gameDir.getParentFile().listFiles((file, string) -> string.startsWith("hs_err_pid") && string.endsWith(".log")) : null;
                    if (fileArray == null) {
                        fileArray = new File(System.getProperty("user.dir")).listFiles((file, string) -> string.startsWith("hs_err_pid") && string.endsWith(".log"));
                    }
                    if (fileArray != null && fileArray.length > 0) {
                        Arrays.sort(fileArray, Comparator.comparingLong(File::lastModified).reversed());
                        file2 = fileArray[0];
                        if (System.currentTimeMillis() - file2.lastModified() < 60000L) {
                            try {
                                string2 = new String(Files.readAllBytes(file2.toPath()), StandardCharsets.UTF_8);
                                progressCallback.onLog("=== JVM CRASH: " + file2.getName() + " ===");
                                stringArray = string2.split("\n");
                                for (int i = 0; i < Math.min(stringArray.length, 30); ++i) {
                                    progressCallback.onLog(stringArray[i]);
                                }
                                progressCallback.onCrash("JVM_CRASH", file2.getName(), GameLauncher.analyzeJvmCrash(string2), string2);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                    }
                }
            }
            if (printWriter != null) {
                printWriter.close();
            }
            nameChangeServer.stop();
            if (activeNameServer == nameChangeServer) {
                activeNameServer = null;
            }
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    private static /* synthetic */ void onProcessHealthThread(Process process, ProgressCallback progressCallback) {
        try {
            if (!process.waitFor(3L, TimeUnit.SECONDS)) {
                progressCallback.onLog("[DEBUG] Process 3sn icinde bitmedi \u2014 hala calisiyor");
            } else {
                progressCallback.onLog("[DEBUG] Process 3sn icinde bitti, exit code: " + process.exitValue());
            }
        }
        catch (Exception exception) {
            progressCallback.onLog("[DEBUG] Process kontrol hatasi: " + exception.getMessage());
        }
    }

    private static /* synthetic */ void onMinecraftOutputThread(Process process, ProgressCallback progressCallback, PrintWriter printWriter) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
            String string;
            while ((string = bufferedReader.readLine()) != null) {
                progressCallback.onLog(string);
                if (printWriter == null) continue;
                printWriter.println(string);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static interface ProgressCallback {
        public void onLog(String var1);

        public void onProgress(int var1, String var2);

        default public void onGameExit(int n, long l) {
        }

        default public void onCrash(String string, String string2, String string3, String string4) {
        }
    }
}

