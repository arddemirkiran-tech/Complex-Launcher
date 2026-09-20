/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.update;

import com.lubv.launcher.ui.Theme;
import com.lubv.launcher.ui.UiFx;
import com.lubv.launcher.update.UpdateManager;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class UpdatePanel
extends JPanel {
    private final JButton updateButton;
    private final JLabel statusLabel;
    private boolean updateAvailable = false;
    private String pendingVersion = "";
    private UpdateManager.UpdateInfo pendingInfo;
    private float pulse = 0.0f;
    private float hover = 0.0f;
    private Timer pulseTimer;
    private Timer hoverTimer;

    public UpdatePanel(Runnable onUpdateClick) {
        this.setLayout(new BorderLayout(8, 0));
        this.setOpaque(false);
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        this.statusLabel = new JLabel(" ");
        this.statusLabel.setFont(new Font("SansSerif", 0, 11));
        this.statusLabel.setForeground(Theme.TEXT_MUTED);
        this.updateButton = new JButton("G\u00fcncelleme Var"){
            {
                this.addMouseListener(new MouseAdapter(){

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        UpdatePanel.this.animHover(1.0f);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        UpdatePanel.this.animHover(0.0f);
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }
                });
                long[] pt0 = new long[]{System.currentTimeMillis()};
                UpdatePanel.this.pulseTimer = new Timer(30, e -> {
                    UpdatePanel.this.pulse = (float)((Math.sin((double)(System.currentTimeMillis() - pt0[0]) / 500.0) + 1.0) / 2.0);
                    this.repaint();
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (!UpdatePanel.this.updateAvailable) {
                    super.paintComponent(g);
                    return;
                }
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = this.getWidth();
                int h = this.getHeight();
                float sc = 1.0f + UpdatePanel.this.hover * 0.03f;
                g2.translate((double)w / 2.0, (double)h / 2.0);
                g2.scale(sc, sc);
                g2.translate((double)(-w) / 2.0, (double)(-h) / 2.0);
                int glowAlpha = (int)(15.0f + 25.0f * UpdatePanel.this.pulse + 20.0f * UpdatePanel.this.hover);
                g2.setColor(new Color(255, 180, 50, glowAlpha));
                g2.setStroke(new BasicStroke(3.0f));
                g2.drawRoundRect(-2, -2, w + 3, h + 3, 12, 12);
                Color top = UiFx.lerp(new Color(255, 167, 38), new Color(255, 200, 80), UpdatePanel.this.hover);
                Color bot = UiFx.lerp(new Color(200, 110, 0), new Color(255, 167, 38), UpdatePanel.this.hover);
                g2.setPaint(new GradientPaint(0.0f, 0.0f, top, 0.0f, h, bot));
                g2.fillRoundRect(0, 0, w, h, 10, 10);
                g2.setPaint(new GradientPaint(0.0f, 0.0f, new Color(255, 255, 255, 70), 0.0f, h / 2, new Color(255, 255, 255, 0)));
                g2.fillRoundRect(0, 0, w, h / 2 + 2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        this.updateButton.setContentAreaFilled(false);
        this.updateButton.setBorderPainted(false);
        this.updateButton.setFocusPainted(false);
        this.updateButton.setForeground(Color.WHITE);
        this.updateButton.setFont(new Font("SansSerif", 1, 11));
        this.updateButton.setCursor(Cursor.getPredefinedCursor(12));
        this.updateButton.setPreferredSize(new Dimension(130, 28));
        this.updateButton.setVisible(false);
        this.updateButton.addActionListener(e -> {
            if (this.pendingInfo != null && onUpdateClick != null && this.updateButton.isEnabled()) {
                this.updateButton.setEnabled(false);
                this.updateButton.setText(com.lubv.launcher.core.L10n.isEnglish() ? "Downloading\u2026" : "\u0130ndiriliyor\u2026");
                onUpdateClick.run();
            }
        });
        this.add((Component)this.statusLabel, "Center");
        this.add((Component)this.updateButton, "East");
    }

    private void animHover(float target) {
        if (this.hoverTimer != null) {
            this.hoverTimer.stop();
        }
        float from = this.hover;
        long t0 = System.currentTimeMillis();
        this.hoverTimer = new Timer(14, null);
        this.hoverTimer.addActionListener(e -> {
            float p = Math.min(1.0f, (float)(System.currentTimeMillis() - t0) / 120.0f);
            this.hover = from + (target - from) * UiFx.easeOutCubic(p);
            this.repaint();
            if (p >= 1.0f) {
                this.hoverTimer.stop();
            }
        });
        this.hoverTimer.start();
    }

    public void showUpdateAvailable(UpdateManager.UpdateInfo info) {
        this.updateAvailable = true;
        this.pendingInfo = info;
        this.pendingVersion = info.version;
        boolean isEn = com.lubv.launcher.core.L10n.isEnglish();
        SwingUtilities.invokeLater(() -> {
            this.updateButton.setText(isEn ? "Update" : "G\u00fcncelle");
            this.updateButton.setVisible(true);
            // Surum numarasini acikca goster: "Yeni surum: 29 (siz v28)".
            this.statusLabel.setText((isEn ? "New version: " : "Yeni s\u00fcr\u00fcm: ") + info.version
                + "  (v" + UpdateManager.CURRENT_VERSION + " \u2192 v" + info.version + ")");
            this.statusLabel.setForeground(new Color(255, 180, 50));
            if (!this.pulseTimer.isRunning()) {
                this.pulseTimer.start();
            }
            // ONEMLI DUZELTME: setVisible/setText tek basina layout'i
            // yeniden hesaplatmaz. Bunlar cagrilmadan onceki durumun
            // "hayalet" (duplicate) gorunumu ekranda kalabiliyordu -
            // butonlar birden fazla yerde/kaydirilmis gorunuyordu.
            this.revalidate();
            this.repaint();
        });
    }

    public void showUpToDate(String version) {
        this.updateAvailable = false;
        boolean isEn = com.lubv.launcher.core.L10n.isEnglish();
        SwingUtilities.invokeLater(() -> {
            this.pulseTimer.stop();
            this.updateButton.setVisible(false);
            this.statusLabel.setText((isEn ? "Up to date \u2014 v" : "G\u00fcncel \u2014 v") + version);
            this.statusLabel.setForeground(Theme.TEXT_MUTED);
            this.revalidate();
            this.repaint();
        });
    }

    public void showChecking() {
        boolean isEn = com.lubv.launcher.core.L10n.isEnglish();
        SwingUtilities.invokeLater(() -> {
            this.statusLabel.setText(isEn ? "Checking for updates\u2026" : "G\u00fcncellemeler kontrol ediliyor\u2026");
            this.statusLabel.setForeground(Theme.TEXT_MUTED);
            this.revalidate();
            this.repaint();
        });
    }

    public void showError(String msg) {
        boolean isEn = com.lubv.launcher.core.L10n.isEnglish();
        SwingUtilities.invokeLater(() -> {
            this.statusLabel.setText((isEn ? "Update check failed: " : "Kontrol basarisiz: ") + msg);
            this.statusLabel.setForeground(Theme.RED);
            this.updateButton.setEnabled(true);
            this.updateButton.setText(isEn ? "Update" : "G\u00fcncelle");
            this.revalidate();
            this.repaint();
        });
    }

    public void showDownloadProgress(String msg, int pct) {
        SwingUtilities.invokeLater(() -> {
            this.statusLabel.setText(msg);
            this.statusLabel.setForeground(Theme.ACCENT);
            this.revalidate();
            this.repaint();
        });
    }

    public void showComplete(String msg) {
        this.updateAvailable = false;
        SwingUtilities.invokeLater(() -> {
            this.pulseTimer.stop();
            this.updateButton.setVisible(false);
            this.statusLabel.setText("\u2713 " + msg);
            this.statusLabel.setForeground(Theme.GREEN);
            this.revalidate();
            this.repaint();
        });
    }

    public boolean isUpdateAvailable() {
        return this.updateAvailable;
    }

    public UpdateManager.UpdateInfo getPendingInfo() {
        return this.pendingInfo;
    }
}

