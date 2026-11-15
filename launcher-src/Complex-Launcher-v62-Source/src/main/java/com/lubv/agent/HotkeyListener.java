package com.lubv.agent;

import com.lubv.agent.IpcClient;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.lang.instrument.Instrumentation;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class HotkeyListener {
    private final IpcClient ipc;
    private final Instrumentation inst;
    private FpsOverlay overlay;
    private final KeyEventDispatcher dispatcher;

    public HotkeyListener(IpcClient ipcClient, Instrumentation instrumentation) {
        this.ipc = ipcClient;
        this.inst = instrumentation;
        this.dispatcher = new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(final KeyEvent keyEvent) {
                if (keyEvent.getID() != 401) {
                    return false;
                }
                switch (keyEvent.getKeyCode()) {
                    case 118: {
                        HotkeyListener.this.onF7();
                        break;
                    }
                    case 119: {
                        HotkeyListener.this.onF8();
                        break;
                    }
                    case 120: {
                        HotkeyListener.this.onF9();
                        break;
                    }
                    case 121: {
                        if (HotkeyListener.this.overlay == null) break;
                    }
                }
                return false;
            }
        };
    }

    public void setOverlay(FpsOverlay fpsOverlay) {
        this.overlay = fpsOverlay;
    }

    public void start() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(HotkeyListener.this.dispatcher);
                System.out.println("[ComplexAgent] Hotkeys hazir: F7=overlay, F8=isim, F9=2.client");
            }
        });
    }

    private void onF7() {
        if (this.overlay == null) {
            this.overlay = FpsOverlay.getInstance();
        }
        this.overlay.toggle();
    }

    private void onF8() {
        if (!this.ipc.isConnected()) {
            this.showInfo("Launcher baglantisi yok. ComplexLauncher ile baslatin.");
            return;
        }
        String current = this.ipc.getCurrentName();
        String input = this.prompt("Isim Degistir (F8)", "Yeni oyuncu ismi (harf/rakam/alt cizgi, max 16):", current != null ? current : "");
        if (input == null) {
            return;
        }
        input = input.trim();
        if (input.isEmpty() || !input.matches("[a-zA-Z0-9_]+") || input.length() > 16) {
            this.showError("Gecersiz isim!");
            return;
        }
        boolean ok = this.ipc.setName(input);
        if (ok) {
            this.showInfo("Isim '" + input + "' olarak degistirildi.\nSunucudan ayrilip tekrar baglanin.");
        } else {
            this.showError("Isim degistirilemedi.");
        }
    }

    private void onF9() {
        if (!this.ipc.isConnected()) {
            this.showInfo("Launcher baglantisi yok. ComplexLauncher ile baslatin.");
            return;
        }
        String current = this.ipc.getCurrentName();
        String suggestion = current == null ? "Player_2" : (current.length() <= 13 ? current + "_2" : current.substring(0, 13) + "_2");
        String input = this.prompt("Ikinci Client (F9)", "Ikinci client icin isim:", suggestion);
        if (input == null) {
            return;
        }
        input = input.trim();
        if (input.isEmpty() || !input.matches("[a-zA-Z0-9_]+") || input.length() > 16) {
            this.showError("Gecersiz isim!");
            return;
        }
        boolean ok = this.ipc.requestSecondClient(input);
        if (ok) {
            this.showInfo("Ikinci client baslatiliyor: " + input);
        } else {
            this.showError("Ikinci client baslat\u0131lamadi.");
        }
    }

    private String prompt(String title, String message, String initial) {
        try {
            return (String) JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE, null, null, initial);
        } catch (Throwable t) {
            System.out.println("[ComplexAgent] prompt hatasi: " + t);
            return null;
        }
    }

    private void showInfo(String message) {
        try {
            JOptionPane.showMessageDialog(null, message, "ComplexTools", JOptionPane.INFORMATION_MESSAGE);
        } catch (Throwable t) {
            System.out.println("[ComplexAgent] " + message);
        }
    }

    private void showError(String message) {
        try {
            JOptionPane.showMessageDialog(null, message, "ComplexTools - Hata", JOptionPane.ERROR_MESSAGE);
        } catch (Throwable t) {
            System.out.println("[ComplexAgent] HATA: " + message);
        }
    }
}
