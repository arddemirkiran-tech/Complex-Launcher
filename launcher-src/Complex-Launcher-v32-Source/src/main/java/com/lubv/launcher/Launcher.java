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
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Complex Launcher zaten \u00e7al\u0131\u015f\u0131yor. G\u00f6rev \u00e7ubu\u011fundaki pencereyi kullan\u0131n.", "Complex Launcher", 1));
            System.exit(0);
            return;
        }
        SwingUtilities.invokeLater(Launcher::boot);
    }

    private static void boot() {
        Object object;
        Serializable serializable;
        Object object2;
        try {
            object2 = Settings.load();
            try {
                File langFile = new File(Paths.GAME_DIR, "lang.txt");
                if (langFile.exists()) {
                    object = new String(Files.readAllBytes(langFile.toPath())).trim();
                    L10n.setLanguage((String)object);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            Theme.apply(((Settings)object2).theme != null ? ((Settings)object2).theme : "Nebula");
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

