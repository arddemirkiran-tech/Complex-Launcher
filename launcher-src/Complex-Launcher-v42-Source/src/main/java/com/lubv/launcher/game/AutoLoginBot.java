/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.game;

import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class AutoLoginBot {
    public static void startYazdir(String string, Process process, Consumer<String> consumer) {
        if (string == null || string.isBlank() || process == null) {
            return;
        }
        Thread thread = new Thread(() -> {
            block7: {
                try {
                    String string2;
                    if (consumer != null) {
                        consumer.accept("[AutoLogin] Log dinleyici aktif. Sunucuya giris bekleniyor...");
                    }
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                    boolean bl = false;
                    while ((string2 = bufferedReader.readLine()) != null) {
                        if (consumer != null) {
                            consumer.accept(string2);
                        }
                        if (bl || !string2.contains("[CHAT]")) continue;
                        if (consumer != null) {
                            consumer.accept("[AutoLogin] Sunucu sohbeti algilandi! Giris baslatiliyor...");
                        }
                        bl = true;
                        String string3 = string;
                        Consumer consumer2 = consumer;
                        Thread loginThread = new Thread(() -> {
                            block2: {
                                try {
                                    Thread.sleep(2000L);
                                    AutoLoginBot.performLogin(string3, consumer2);
                                }
                                catch (Exception exception) {
                                    if (consumer2 == null) break block2;
                                    consumer2.accept("[AutoLogin] Login hatasi: " + exception.getMessage());
                                }
                            }
                        }, "auto-login-perform");
                        loginThread.setDaemon(true);
                        loginThread.start();
                    }
                    if (consumer != null) {
                        consumer.accept("[AutoLogin] Log akisi sona erdi.");
                    }
                }
                catch (Exception exception) {
                    if (consumer == null) break block7;
                    consumer.accept("[AutoLogin] Hata: " + exception.getMessage());
                }
            }
        }, "auto-login-bot");
        thread.setDaemon(true);
        thread.start();
    }

    private static void performLogin(String string, Consumer<String> consumer) {
        block3: {
            try {
                AutoLoginBot.focusMinecraftNative();
                Thread.sleep(400L);
                Robot robot = new Robot();
                robot.setAutoDelay(50);
                Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                int n = dimension.width / 2;
                int n2 = dimension.height / 2;
                robot.mouseMove(n, n2);
                robot.mousePress(1024);
                robot.mouseRelease(1024);
                Thread.sleep(400L);
                String string2 = "/login " + string;
                StringSelection stringSelection = new StringSelection(string2);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                AutoLoginBot.pressKey(robot, 84);
                Thread.sleep(500L);
                int n3 = System.getProperty("os.name").toLowerCase().contains("mac") ? 157 : 17;
                robot.keyPress(n3);
                robot.keyPress(65);
                robot.keyRelease(65);
                robot.keyRelease(n3);
                Thread.sleep(100L);
                AutoLoginBot.pressKey(robot, 8);
                Thread.sleep(150L);
                robot.keyPress(n3);
                robot.keyPress(86);
                robot.keyRelease(86);
                robot.keyRelease(n3);
                Thread.sleep(300L);
                AutoLoginBot.pressKey(robot, 10);
                if (consumer != null) {
                    consumer.accept("[AutoLogin] /login komutu basariyla gonderildi!");
                }
            }
            catch (Exception exception) {
                if (consumer == null) break block3;
                consumer.accept("[AutoLogin] Perform login hatasi: " + exception.getMessage());
            }
        }
    }

    private static void pressKey(Robot robot, int n) throws InterruptedException {
        robot.keyPress(n);
        Thread.sleep(60L);
        robot.keyRelease(n);
    }

    private static void focusMinecraftNative() {
        try {
            String string = System.getProperty("os.name").toLowerCase();
            if (string.contains("win")) {
                String string2 = "$app = Get-Process | Where-Object {$_.MainWindowTitle -like '*Minecraft*'}; if ($app) { (New-Object -ComObject WScript.Shell).AppActivate($app.Id) }";
                ProcessBuilder processBuilder = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", string2);
                processBuilder.start().waitFor();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

