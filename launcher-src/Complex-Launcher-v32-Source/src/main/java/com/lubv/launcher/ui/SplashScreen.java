/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.ui;

import com.lubv.launcher.ui.LogoRes;
import com.lubv.launcher.ui.Theme;
import com.lubv.launcher.ui.UiFx;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.Timer;

public class SplashScreen
extends JWindow {
    private final JProgressBar bar;
    private float alpha = 0.0f;
    private float logoScale = 0.7f;
    private Timer entranceTimer;
    private Timer fadeOutTimer;
    private boolean disposed = false;

    public SplashScreen() {
        this.setSize(440, 220);
        this.setLocationRelativeTo(null);
        this.setBackground(new Color(0, 0, 0, 0));
        this.setLayout(new BorderLayout());
        JPanel jPanel = new JPanel(new BorderLayout(0, 0)){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setColor(new Color(0, 0, 0, 60));
                graphics2D.fillRoundRect(4, 6, this.getWidth() - 8, this.getHeight() - 8, 20, 20);
                // TEMA BUG FIXI: sabit (32,20,62) moru yerine temanin yuzey
                // rengi - splash artik secilen temaya birebir uyar.
                graphics2D.setColor(Theme.BG_SURFACE != null ? Theme.BG_SURFACE : new Color(32, 20, 62));
                graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 16, 16);
                graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, Theme.ACCENT_BRIGHT, this.getWidth(), 0.0f, Theme.ACCENT_DARK));
                graphics2D.fillRoundRect(0, 0, this.getWidth(), 4, 4, 4);
                // Vurgu halesi de temadan: sabit (168,115,253) moru degil.
                Color splashGlow = Theme.ACCENT != null ? Theme.ACCENT : new Color(168, 115, 253);
                RadialGradientPaint radialGradientPaint = new RadialGradientPaint((float)this.getWidth() / 2.0f, (float)this.getHeight() / 2.0f, (float)this.getWidth() * 0.6f, new float[]{0.0f, 1.0f}, new Color[]{new Color(splashGlow.getRed(), splashGlow.getGreen(), splashGlow.getBlue(), 30), new Color(splashGlow.getRed(), splashGlow.getGreen(), splashGlow.getBlue(), 0)});
                graphics2D.setPaint(radialGradientPaint);
                graphics2D.fillRect(0, 0, this.getWidth(), this.getHeight());
                graphics2D.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        jPanel.setOpaque(false);
        jPanel.setBorder(BorderFactory.createEmptyBorder(24, 32, 20, 32));
        JPanel jPanel2 = new JPanel(new FlowLayout(1, 14, 0)){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setComposite(AlphaComposite.SrcOver.derive(SplashScreen.this.alpha));
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ImageIcon imageIcon = LogoRes.icon(52);
                int n = 0;
                int n2 = (this.getHeight() - 52) / 2;
                float f = SplashScreen.this.logoScale;
                graphics2D.translate(n + 26, n2 + 26);
                graphics2D.scale(f, f);
                graphics2D.translate(-26, -26);
                imageIcon.paintIcon(this, graphics2D, 0, 0);
                graphics2D.dispose();
                super.paintComponent(graphics);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        jPanel2.setOpaque(false);
        JPanel jPanel3 = new JPanel();
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new Dimension(52, 52));
        jPanel2.add(jPanel3);
        JPanel jPanel4 = new JPanel();
        jPanel4.setLayout(new BoxLayout(jPanel4, 1));
        jPanel4.setOpaque(false);
        JLabel jLabel = new JLabel("Complex Launcher v" + com.lubv.launcher.update.UpdateManager.CURRENT_VERSION);
        jLabel.setFont(new Font("SansSerif", 1, 22));
        jLabel.setForeground(Theme.TEXT_PRIMARY != null ? Theme.TEXT_PRIMARY : new Color(237, 222, 254));
        jLabel.setAlignmentX(0.0f);
        JLabel jLabel2 = new JLabel("tlauncherdan iyidir");
        jLabel2.setFont(new Font("SansSerif", 0, 11));
        jLabel2.setForeground(Theme.ACCENT_BRIGHT != null ? Theme.ACCENT_BRIGHT : new Color(168, 115, 253));
        jLabel2.setAlignmentX(0.0f);
        JLabel jLabel3 = new JLabel("Y\u00fckleniyor\u2026");
        jLabel3.setFont(new Font("SansSerif", 0, 11));
        jLabel3.setForeground(Theme.TEXT_MUTED != null ? Theme.TEXT_MUTED : new Color(100, 80, 140));
        jLabel3.setAlignmentX(0.0f);
        jPanel4.add(jLabel);
        jPanel4.add(Box.createVerticalStrut(3));
        jPanel4.add(jLabel2);
        jPanel4.add(Box.createVerticalStrut(6));
        jPanel4.add(jLabel3);
        jPanel2.add(jPanel4);
        this.bar = UiFx.progressPill();
        this.bar.setIndeterminate(true);
        this.bar.setPreferredSize(new Dimension(0, 5));
        JPanel jPanel5 = new JPanel(new BorderLayout());
        jPanel5.setOpaque(false);
        jPanel5.add((Component)this.bar, "Center");
        jPanel.add((Component)jPanel2, "Center");
        jPanel.add((Component)jPanel5, "South");
        this.add(jPanel);
    }

    public void start() {
        this.setVisible(true);
        long l = System.currentTimeMillis();
        this.entranceTimer = new Timer(14, actionEvent -> {
            try {
                float f = Math.min(1.0f, (float)(System.currentTimeMillis() - l) / 350.0f);
                this.alpha = UiFx.easeOutCubic(f);
                this.logoScale = 0.7f + 0.3f * UiFx.easeOutBack(f);
                this.repaint();
                if (f >= 1.0f) {
                    this.entranceTimer.stop();
                    this.entranceTimer = null;
                }
            }
            catch (Exception exception) {
                if (this.entranceTimer != null) {
                    this.entranceTimer.stop();
                }
                this.entranceTimer = null;
            }
        });
        this.entranceTimer.start();
    }

    public void close() {
        if (this.entranceTimer != null) {
            this.entranceTimer.stop();
            this.entranceTimer = null;
        }
        if (this.disposed) {
            return;
        }
        Timer timer = new Timer(500, actionEvent -> {
            ((Timer)actionEvent.getSource()).stop();
            this.forceDispose();
        });
        timer.setRepeats(false);
        timer.start();
        long l = System.currentTimeMillis();
        this.fadeOutTimer = new Timer(14, null);
        this.fadeOutTimer.addActionListener(actionEvent -> {
            try {
                float f = Math.min(1.0f, (float)(System.currentTimeMillis() - l) / 200.0f);
                this.alpha = 1.0f - UiFx.easeOutCubic(f);
                this.repaint();
                if (f >= 1.0f) {
                    this.fadeOutTimer.stop();
                    this.fadeOutTimer = null;
                    timer.stop();
                    this.forceDispose();
                }
            }
            catch (Exception exception) {
                this.fadeOutTimer.stop();
                this.fadeOutTimer = null;
                timer.stop();
                this.forceDispose();
            }
        });
        this.fadeOutTimer.start();
    }

    private void forceDispose() {
        if (this.disposed) {
            return;
        }
        this.disposed = true;
        if (this.entranceTimer != null) {
            this.entranceTimer.stop();
            this.entranceTimer = null;
        }
        if (this.fadeOutTimer != null) {
            this.fadeOutTimer.stop();
            this.fadeOutTimer = null;
        }
        try {
            this.setVisible(false);
            this.dispose();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

