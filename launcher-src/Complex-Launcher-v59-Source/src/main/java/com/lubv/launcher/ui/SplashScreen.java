/*
 * v49 "Modern" splash: deep gradient card, aurora glow, ring logo,
 * version chip and a slim accent bar. Same contract as before
 * (start()/close()); only the chrome changed.
 */
package com.lubv.launcher.ui;

import com.lubv.launcher.core.L10n;
import com.lubv.launcher.update.UpdateManager;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
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
    private static final int W = 460;
    private static final int H = 248;
    private final JProgressBar bar;
    private float alpha = 0.0f;
    private float rise = 18.0f;
    private Timer entranceTimer;
    private Timer fadeOutTimer;
    private boolean disposed = false;

    public SplashScreen() {
        this.setSize(W, H);
        this.setLocationRelativeTo(null);
        this.setBackground(new Color(0, 0, 0, 0));
        this.setLayout(new BorderLayout());

        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = this.getWidth();
                int h = this.getHeight();
                g.setComposite(AlphaComposite.SrcOver.derive(0.55f));
                g.setColor(new Color(0, 0, 0, 120));
                g.fill(new RoundRectangle2D.Float(6, 10, w - 12, h - 12, 26, 26));
                g.setComposite(AlphaComposite.SrcOver);
                RoundRectangle2D.Float shape = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 24, 24);
                g.setPaint(new java.awt.GradientPaint(0, 0, Theme.BG_ELEVATED, w, h, Theme.BG_SURFACE));
                g.fill(shape);
                // Aurora: two soft accent blooms, kept subtle so the text stays crisp.
                g.setPaint(new RadialGradientPaint(w * 0.18f, h * 0.12f, w * 0.55f,
                        new float[]{0.0f, 1.0f},
                        new Color[]{UiFx.withAlpha(Theme.ACCENT, 90), UiFx.withAlpha(Theme.ACCENT, 0)}));
                g.fill(shape);
                g.setPaint(new RadialGradientPaint(w * 0.92f, h * 0.95f, w * 0.48f,
                        new float[]{0.0f, 1.0f},
                        new Color[]{UiFx.withAlpha(Theme.ACCENT_BRIGHT, 55), UiFx.withAlpha(Theme.ACCENT_BRIGHT, 0)}));
                g.fill(shape);
                g.setColor(UiFx.withAlpha(Theme.TEXT_PRIMARY, 26));
                g.setStroke(new BasicStroke(1.0f));
                g.draw(new RoundRectangle2D.Float(1.5f, 1.5f, w - 3, h - 3, 24, 24));
                // Hairline accent on the very top edge.
                g.setPaint(new java.awt.GradientPaint(0, 0, Theme.ACCENT_BRIGHT, w, 0, Theme.ACCENT_DARK));
                g.fill(new RoundRectangle2D.Float(1, 1, w - 2, 3, 3, 3));
                g.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(30, 34, 26, 34));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, 1));
        center.setOpaque(false);

        JPanel logoRow = new JPanel(new BorderLayout());
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(0.0f);
        logoRow.add(new RingLogo(), BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, 1));
        text.setOpaque(false);
        text.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));

        JLabel title = new JLabel("Complex Launcher");
        title.setFont(Theme.uiFont(Font.BOLD, 24.0f));
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(0.0f);

        JLabel sub = new JLabel(L10n.get("app.subtitle").toUpperCase());
        sub.setFont(Theme.uiFont(Font.PLAIN, 10.0f));
        sub.setForeground(Theme.ACCENT_BRIGHT);
        sub.setAlignmentX(0.0f);

        text.add(Box.createVerticalStrut(8));
        text.add(title);
        text.add(Box.createVerticalStrut(5));
        text.add(sub);
        logoRow.add(text, BorderLayout.CENTER);
        center.add(logoRow);
        center.add(Box.createVerticalStrut(22));

        JLabel loading = new JLabel(L10n.get("splash.loading"));
        loading.setFont(Theme.uiFont(Font.PLAIN, 12.0f));
        loading.setForeground(Theme.TEXT_SECONDARY);
        loading.setAlignmentX(0.0f);
        center.add(loading);
        center.add(Box.createVerticalStrut(10));

        this.bar = UiFx.progressPill();
        this.bar.setIndeterminate(true);
        this.bar.setPreferredSize(new Dimension(W - 80, 5));
        this.bar.setMaximumSize(new Dimension(W - 80, 5));
        this.bar.setAlignmentX(0.0f);
        center.add(this.bar);
        center.add(Box.createVerticalStrut(16));

        JLabel version = new JLabel(L10n.fmt("splash.sub", UpdateManager.displayVersion()));
        version.setFont(Theme.uiFont(Font.PLAIN, 10.0f));
        version.setForeground(Theme.TEXT_MUTED);
        version.setAlignmentX(0.0f);
        center.add(version);

        card.add(center, BorderLayout.CENTER);
        this.add(card);
    }

    /** 64 px logo inside a soft ring - reads as the app icon, not a sprite. */
    private static final class RingLogo extends JPanel {
        RingLogo() {
            this.setOpaque(false);
            this.setPreferredSize(new Dimension(76, 76));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = 76;
            g.setPaint(new java.awt.GradientPaint(0, 0, UiFx.withAlpha(Theme.ACCENT, 120), size, size, UiFx.withAlpha(Theme.ACCENT_BRIGHT, 40)));
            g.fillRoundRect(0, 0, size, size, 24, 24);
            g.setColor(UiFx.withAlpha(Theme.ACCENT_BRIGHT, 110));
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(0, 0, size - 1, size - 1, 24, 24);
            ImageIcon icon = LogoRes.icon(52);
            if (icon != null && icon.getIconWidth() > 0) {
                icon.paintIcon(this, g, (size - 52) / 2, (size - 52) / 2);
            }
            g.dispose();
        }
    }

    public void start() {
        this.setVisible(true);
        long start = System.currentTimeMillis();
        this.entranceTimer = new Timer(14, actionEvent -> {
            try {
                float p = Math.min(1.0f, (float) (System.currentTimeMillis() - start) / 380.0f);
                this.alpha = UiFx.easeOutCubic(p);
                this.rise = 18.0f * (1.0f - UiFx.easeOutCubic(p));
                this.repaint();
                if (p >= 1.0f) {
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
            ((Timer) actionEvent.getSource()).stop();
            this.forceDispose();
        });
        timer.setRepeats(false);
        timer.start();
        long start = System.currentTimeMillis();
        this.fadeOutTimer = new Timer(14, null);
        this.fadeOutTimer.addActionListener(actionEvent -> {
            try {
                float p = Math.min(1.0f, (float) (System.currentTimeMillis() - start) / 200.0f);
                this.alpha = 1.0f - UiFx.easeOutCubic(p);
                this.repaint();
                if (p >= 1.0f) {
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
