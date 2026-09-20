package com.lubv.agent;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Kucuk FPS overlay penceresi (F7 ile acilip kapanir).
 * Java 8 uyumlu: lambda + var yok, sadece temel Swing API.
 */
public class FpsOverlay {
    private static volatile FpsOverlay instance;

    private JFrame frame;
    private JLabel label;
    private volatile int fps = -1;

    public static FpsOverlay getInstance() {
        FpsOverlay local = instance;
        if (local == null) {
            synchronized (FpsOverlay.class) {
                local = instance;
                if (local == null) {
                    local = new FpsOverlay();
                    instance = local;
                }
            }
        }
        return local;
    }

    /** OverlayRenderer oyun render dongusunden FPS guncellemesi icin cagirir. */
    public static void setFps(int value) {
        FpsOverlay local = instance;
        if (local != null) {
            local.updateFps(value);
        }
    }

    private FpsOverlay() {
    }

    public synchronized void toggle() {
        try {
            if (frame != null && frame.isVisible()) {
                frame.setVisible(false);
                return;
            }
            if (frame == null) {
                build();
            }
            if (frame != null) {
                frame.setVisible(true);
            }
        } catch (Throwable t) {
            System.out.println("[ComplexAgent] overlay toggle hatasi: " + t);
        }
    }

    public boolean isVisibleOverlay() {
        JFrame f = frame;
        return f != null && f.isVisible();
    }

    private void build() {
        try {
            JFrame f = new JFrame("ComplexTools");
            f.setUndecorated(true);
            f.setAlwaysOnTop(true);
            try {
                f.setType(java.awt.Window.Type.UTILITY);
            } catch (Throwable ignore) {
            }
            JLabel l = new JLabel("FPS: -", SwingConstants.CENTER);
            l.setOpaque(true);
            l.setBackground(new Color(12, 12, 16, 200));
            l.setForeground(new Color(90, 255, 130));
            l.setFont(new Font("Monospaced", Font.BOLD, 14));
            f.setContentPane(l);
            f.setSize(112, 34);
            f.setLocation(24, 24);
            f.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame = f;
            label = l;
            updateLabel();
        } catch (Throwable t) {
            System.out.println("[ComplexAgent] overlay olusturulamadi: " + t);
            frame = null;
        }
    }

    private void updateFps(int value) {
        fps = value;
        updateLabel();
    }

    private void updateLabel() {
        final int value = fps;
        final JLabel l = label;
        if (l == null) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JLabel ll = label;
                if (ll != null) {
                    ll.setText(value >= 0 ? "FPS: " + value : "FPS: -");
                }
            }
        });
    }
}
