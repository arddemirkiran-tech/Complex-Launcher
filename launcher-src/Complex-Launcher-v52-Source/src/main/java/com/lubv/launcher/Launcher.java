/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher;

import com.lubv.launcher.core.CrashLog;
import com.lubv.launcher.core.L10n;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.core.Settings;
import com.lubv.launcher.ui.MainWindow;
import com.lubv.launcher.ui.SplashScreen;
import com.lubv.launcher.ui.Theme;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

public class Launcher {
    private static FileChannel lockChannel;
    private static FileLock lock;

    public static void main(String[] stringArray) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> CrashLog.log(throwable));
        if (!Launcher.acquireSingleInstanceLock()) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, L10n.get("already_running"), "Complex Launcher", 1));
            System.exit(0);
            return;
        }
        SwingUtilities.invokeLater(Launcher::boot);
    }

    /** Setup EXE'sinin dil dosyasini olasi kurulum konumlarinda arar. */
    private static File findInstallerLangFile() {
        java.util.LinkedHashSet<File> candidates = new java.util.LinkedHashSet<File>();
        try {
            File launcherDir = Paths.getLauncherDir();
            if (launcherDir != null) {
                candidates.add(new File(launcherDir, "setup-lang.txt"));
            }
        }
        catch (Throwable ignored) {
        }
        try {
            File parent = Paths.GAME_DIR.getParentFile();
            if (parent != null) {
                candidates.add(new File(parent, "setup-lang.txt"));
            }
        }
        catch (Throwable ignored) {
        }
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            candidates.add(new File(new File(localAppData, "ComplexLauncher"), "setup-lang.txt"));
        }
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            candidates.add(new File(new File(appData, "ComplexLauncher"), "setup-lang.txt"));
        }
        for (File candidate : candidates) {
            if (candidate.isFile()) {
                return candidate;
            }
        }
        return null;
    }

    /** "tr"/"en" disindaki (veya okunamayan) dosyalari yok sayar. */
    private static String readLangFile(File file) {
        try {
            if (file == null || !file.isFile()) {
                return null;
            }
            String value = new String(Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8)
                .trim().toLowerCase(java.util.Locale.ROOT);
            return "tr".equals(value) || "en".equals(value) ? value : null;
        }
        catch (Exception exception) {
            return null;
        }
    }

    /**
     * Dil secimi: en yeni kaynak kazanir. Setup secimi ancak lang.txt'den
     * yeniyse (yeni kurulum / yeniden kurulum) uygulanir; aksi halde
     * kullanicinin launcher icinden yaptigi secim korunur.
     */
    static String pickLanguage(String userLang, long userStamp, String installerLang, long installerStamp) {
        if (installerLang != null && (userLang == null || installerStamp > userStamp)) {
            return installerLang;
        }
        return userLang != null ? userLang : "en";
    }

    private static void boot() {
        Object object;
        Serializable serializable;
        Object object2;
        try {
            object2 = Settings.load();
            // v52: Kurulum EXE'sinde secilen dil launcher'i da belirler.
            // Setup, secimi %LOCALAPPDATA%\ComplexLauncher\setup-lang.txt
            // dosyasina yazar. Launcher ilk acilista bunu okur; kullanici
            // sonradan Ayarlar'dan dil degistirirse yazilan lang.txt daha
            // yeni oldugu icin onun secimi korunur. Setup yeniden calisip
            // yeni bir dil secilirse setup-lang.txt daha yeni olur ve yine
            // kurulumdaki secim kazanir.
            File langFile = new File(Paths.GAME_DIR, "lang.txt");
            File installerLangFile = findInstallerLangFile();
            String installerLang = readLangFile(installerLangFile);
            String userLang = readLangFile(langFile);
            long installerStamp = installerLangFile != null && installerLangFile.isFile() ? installerLangFile.lastModified() : Long.MIN_VALUE;
            long userStamp = langFile.isFile() ? langFile.lastModified() : Long.MIN_VALUE;
            String language = pickLanguage(userLang, userStamp, installerLang, installerStamp);
            L10n.setLanguage(language);
            try {
                // Secimi lang.txt'ye isle: Ayarlar secicisi gercek kullanilan
                // dili gostersin ve sonraki acilislar ayni dilde devam etsin.
                if (!language.equals(userLang)) {
                    langFile.getParentFile().mkdirs();
                    Files.write(langFile.toPath(), language.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
            }
            catch (Exception exception) {
                // yazamazsak da secim bu oturumda gecerli
            }                com.lubv.launcher.ui.BrandFonts.load();
                Theme.apply(((Settings)object2).theme != null ? ((Settings)object2).theme : "Complex");
                Theme.applyModernShape();
        }
        catch (Exception exception) {
            CrashLog.log(exception);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (Exception exception2) {
                // empty catch block
            }
        }
        try {
            object2 = new SplashScreen();
            ((SplashScreen)object2).start();
        }
        catch (Throwable throwable) {
            CrashLog.log(throwable);
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException)throwable;
            }
            throw new RuntimeException(throwable);
        }
        // V34.8 BASLANGIC HIZI: sha1 onbellegini pencere acilirken arkada
        // isit - launch() sirasindaki yuzlerce hash kontrolu (kutuphane +
        // asset) cogunlukla onbellekten donecek, disk I/O beklemesi kalmayacak.
        try {
            com.lubv.launcher.core.HttpUtil.warmSha1CacheAsync();
        }
        catch (Throwable ignored) {
            // isitma hicbir sekilde acilisi bozmasin
        }
        try {
            serializable = new MainWindow();
        }
        catch (Throwable throwable) {
            CrashLog.log(throwable);
            ((SplashScreen)object2).close();
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException)throwable;
            }
            throw new RuntimeException(throwable);
        }
        final MainWindow bootWindow = (MainWindow)serializable;
        final SplashScreen bootSplash = (SplashScreen)object2;
        Timer bootTimer = new Timer(1000, arg_0 -> Launcher.lambdaBootFinish(bootSplash, bootWindow, arg_0));
        bootTimer.setRepeats(false);
        bootTimer.start();
    }

    private static boolean acquireSingleInstanceLock() {
        try {
            Paths.GAME_DIR.mkdirs();
            lockChannel = FileChannel.open(new File(Paths.GAME_DIR, "launcher.lock").toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            lock = lockChannel.tryLock();
            if (lock == null) {
                lockChannel.close();
                lockChannel = null;
                return false;
            }
            return true;
        }
        catch (Exception exception) {
            return true;
        }
    }

    private static /* synthetic */ void lambdaBootFinish(SplashScreen splashScreen, MainWindow mainWindow, ActionEvent actionEvent) {
        ((Timer)actionEvent.getSource()).stop();
        splashScreen.close();
        mainWindow.setVisible(true);
    }
}

