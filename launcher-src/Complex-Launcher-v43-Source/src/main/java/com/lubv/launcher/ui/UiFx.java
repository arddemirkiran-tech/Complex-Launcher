/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.ui;

import com.lubv.launcher.ui.Theme;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;

public final class UiFx {
    private UiFx() {
    }

    public static float easeOutCubic(float f) {
        return 1.0f - (1.0f - f) * (1.0f - f) * (1.0f - f);
    }

    public static float easeOutQuart(float f) {
        float f2 = 1.0f - f;
        return 1.0f - f2 * f2 * f2 * f2;
    }

    public static float easeInOutQuad(float f) {
        return f < 0.5f ? 2.0f * f * f : 1.0f - 2.0f * (1.0f - f) * (1.0f - f);
    }

    public static float easeOutBack(float f) {
        float f2 = 1.70158f;
        return 1.0f + (f2 + 1.0f) * (f -= 1.0f) * f * f + f2 * f * f;
    }

    public static float easeOutExpo(float f) {
        return f >= 1.0f ? 1.0f : 1.0f - (float)Math.pow(2.0, -10.0f * f);
    }

    public static float easeInOutCubic(float f) {
        return f < 0.5f ? 4.0f * f * f * f : 1.0f - 4.0f * (f - 1.0f) * (f - 1.0f) * (f - 1.0f);
    }

    public static Color uiColor(String string, Color color) {
        Color color2 = UIManager.getColor(string);
        return color2 != null ? color2 : color;
    }

    public static Color lerp(Color color, Color color2, float f) {
        if (color == null || color2 == null) {
            return color2;
        }
        return new Color(UiFx.clamp(Math.round((float)color.getRed() + (float)(color2.getRed() - color.getRed()) * f)), UiFx.clamp(Math.round((float)color.getGreen() + (float)(color2.getGreen() - color.getGreen()) * f)), UiFx.clamp(Math.round((float)color.getBlue() + (float)(color2.getBlue() - color.getBlue()) * f)), UiFx.clamp(Math.round((float)color.getAlpha() + (float)(color2.getAlpha() - color.getAlpha()) * f)));
    }

    public static Color withAlpha(Color color, int n) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), n);
    }

    public static Color brighten(Color color, float f) {
        return new Color(UiFx.clamp((int)((float)color.getRed() * f)), UiFx.clamp((int)((float)color.getGreen() * f)), UiFx.clamp((int)((float)color.getBlue() * f)), color.getAlpha());
    }

    /** Orani verilen miktarda karartir (f>0: koyu, f<0: acik). Thread-safe, yeni Color dondurur. */
    public static Color darken(Color color, float f) {
        return new Color(UiFx.clamp((int)((float)color.getRed() * (1f - f))), UiFx.clamp((int)((float)color.getGreen() * (1f - f))), UiFx.clamp((int)((float)color.getBlue() * (1f - f))), color.getAlpha());
    }

    private static int clamp(int n) {
        return Math.max(0, Math.min(255, n));
    }

    public static void animateProgress(JProgressBar jProgressBar, int n, int n2) {
        if (n < jProgressBar.getMinimum()) {
            return;
        }
        int n3 = jProgressBar.getValue();
        if (n3 == n) {
            return;
        }
        long l = System.currentTimeMillis();
        Timer timer = new Timer(14, null);
        timer.addActionListener(actionEvent -> {
            float f = Math.min(1.0f, (float)(System.currentTimeMillis() - l) / (float)n2);
            jProgressBar.setValue(Math.round((float)n3 + (float)(n - n3) * UiFx.easeOutCubic(f)));
            if (f >= 1.0f) {
                timer.stop();
                jProgressBar.setValue(n);
            }
        });
        timer.start();
    }

    public static JPanel fadeIn(JComponent jComponent, int n) {
        final float[] fArray = new float[]{0.0f};
        JPanel jPanel = new JPanel((LayoutManager)new BorderLayout()){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setComposite(AlphaComposite.SrcOver.derive(fArray[0]));
                super.paintComponent(graphics2D);
                graphics2D.dispose();
            }
        };
        jPanel.setOpaque(false);
        jPanel.add(jComponent);
        long l = System.currentTimeMillis();
        Timer timer = new Timer(14, null);
        timer.addActionListener(actionEvent -> {
            float f = Math.min(1.0f, (float)(System.currentTimeMillis() - l) / (float)n);
            fArray[0] = UiFx.easeOutCubic(f);
            jPanel.repaint();
            if (f >= 1.0f) {
                timer.stop();
            }
        });
        timer.start();
        return jPanel;
    }

    public static JButton accentButton(String string) {
        JButton jButton = new JButton(string){
            private float hover = 0.0f;
            private float press = 0.0f;
            private Timer ht;
            {
                this.addMouseListener(new MouseAdapter(){

                    @Override
                    public void mouseEntered(MouseEvent mouseEvent) {
                        anim(1.0f);
                    }

                    @Override
                    public void mouseExited(MouseEvent mouseEvent) {
                        anim(0.0f);
                    }

                    @Override
                    public void mousePressed(MouseEvent mouseEvent) {
                        press = 1.0f;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent mouseEvent) {
                        press = 0.0f;
                        repaint();
                    }
                });
            }

            private void anim(float f) {
                if (this.ht != null) {
                    this.ht.stop();
                }
                float f2 = this.hover;
                long l = System.currentTimeMillis();
                this.ht = new Timer(16, null);
                this.ht.addActionListener(actionEvent -> {
                    float f3 = Math.min(1.0f, (float)(System.currentTimeMillis() - l) / 140.0f);
                    this.hover = f2 + (f - f2) * UiFx.easeOutCubic(f3);
                    repaint();
                    if (f3 >= 1.0f) {
                        this.ht.stop();
                    }
                });
                this.ht.start();
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = this.getWidth();
                int n2 = this.getHeight();
                if (!this.isEnabled()) {
                    graphics2D.setColor(UiFx.withAlpha(Theme.BG_ELEVATED, 180));
                    graphics2D.fillRoundRect(0, 0, n, n2, 12, 12);
                    graphics2D.setColor(Theme.BG_BORDER);
                    graphics2D.setStroke(new BasicStroke(1.0f));
                    graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 12, 12);
                    graphics2D.dispose();
                    super.paintComponent(graphics);
                    return;
                }
                float f = 1.0f - this.press * 0.04f;
                graphics2D.translate((double)n / 2.0, (double)n2 / 2.0);
                graphics2D.scale(f, f);
                graphics2D.translate((double)(-n) / 2.0, (double)(-n2) / 2.0);
                float[] fArray = new float[]{0.0f, 0.45f, 1.0f};
                Color[] colorArray = new Color[]{UiFx.lerp(Theme.ACCENT_BRIGHT, Theme.ACCENT_BRIGHT, this.hover), UiFx.lerp(Theme.ACCENT, Theme.ACCENT_BRIGHT, this.hover * 0.5f), UiFx.lerp(Theme.ACCENT_DARK, Theme.ACCENT, this.hover)};
                graphics2D.setPaint(new LinearGradientPaint(0.0f, 0.0f, 0.0f, n2, fArray, colorArray));
                graphics2D.fillRoundRect(0, 0, n, n2, 12, 12);
                graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, new Color(255, 255, 255, (int)(70.0f + 25.0f * this.hover)), 0.0f, (float)n2 * 0.4f, new Color(255, 255, 255, 0)));
                graphics2D.fillRoundRect(0, 0, n, (int)((float)n2 * 0.5f), 12, 12);
                if (this.hover > 0.01f) {
                    graphics2D.setColor(UiFx.withAlpha(Theme.ACCENT, (int)(55.0f * this.hover)));
                    graphics2D.setStroke(new BasicStroke(2.0f));
                    graphics2D.drawRoundRect(-1, -1, n + 1, n2 + 1, 13, 13);
                }
                graphics2D.setColor(UiFx.withAlpha(Theme.ACCENT_DARK, 120));
                graphics2D.setStroke(new BasicStroke(1.0f));
                graphics2D.drawLine(4, n2 - 1, n - 4, n2 - 1);
                graphics2D.dispose();
                super.paintComponent(graphics);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        jButton.setContentAreaFilled(false);
        jButton.setBorderPainted(false);
        jButton.setFocusPainted(false);
        jButton.setForeground(Color.WHITE);
        jButton.setFont(Theme.uiFont(java.awt.Font.BOLD, 13.0f));
        jButton.setCursor(Cursor.getPredefinedCursor(12));
        jButton.setPreferredSize(new Dimension(jButton.getPreferredSize().width, 34));
        return jButton;
    }

    public static JButton ghostButton(String string) {
        JButton jButton = new JButton(string){
            private float hover = 0.0f;
            private float press = 0.0f;
            private Timer ht;
            {
                this.addMouseListener(new MouseAdapter(){

                    @Override
                    public void mouseEntered(MouseEvent mouseEvent) {
                        anim(1.0f);
                    }

                    @Override
                    public void mouseExited(MouseEvent mouseEvent) {
                        anim(0.0f);
                    }

                    @Override
                    public void mousePressed(MouseEvent mouseEvent) {
                        press = 1.0f;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent mouseEvent) {
                        press = 0.0f;
                        repaint();
                    }
                });
            }

            private void anim(float f) {
                if (this.ht != null) {
                    this.ht.stop();
                }
                float f2 = this.hover;
                long l = System.currentTimeMillis();
                this.ht = new Timer(16, null);
                this.ht.addActionListener(actionEvent -> {
                    float f3 = Math.min(1.0f, (float)(System.currentTimeMillis() - l) / 140.0f);
                    this.hover = f2 + (f - f2) * UiFx.easeOutCubic(f3);
                    repaint();
                    if (f3 >= 1.0f) {
                        this.ht.stop();
                    }
                });
                this.ht.start();
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = this.getWidth();
                int n2 = this.getHeight();
                if (!this.isEnabled()) {
                    graphics2D.setColor(UiFx.withAlpha(Theme.BG_BORDER, 60));
                    graphics2D.fillRoundRect(0, 0, n, n2, 10, 10);
                    graphics2D.setColor(UiFx.withAlpha(Theme.BG_BORDER, 100));
                    graphics2D.setStroke(new BasicStroke(1.0f));
                    graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 10, 10);
                    graphics2D.dispose();
                    super.paintComponent(graphics);
                    return;
                }
                float f = 1.0f - this.press * 0.03f;
                graphics2D.translate((double)n / 2.0, (double)n2 / 2.0);
                graphics2D.scale(f, f);
                graphics2D.translate((double)(-n) / 2.0, (double)(-n2) / 2.0);
                graphics2D.setColor(UiFx.withAlpha(Theme.BG_ELEVATED, (int)(150.0f + 60.0f * this.hover)));
                graphics2D.fillRoundRect(0, 0, n, n2, 10, 10);
                graphics2D.setColor(UiFx.withAlpha(Theme.ACCENT, (int)(10.0f + 30.0f * this.hover)));
                graphics2D.fillRoundRect(0, 0, n, n2, 10, 10);
                Color color = UiFx.lerp(Theme.BG_BORDER, Theme.ACCENT_BRIGHT, this.hover * 0.75f);
                graphics2D.setColor(color);
                graphics2D.setStroke(new BasicStroke(1.2f));
                graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 10, 10);
                if (this.hover > 0.1f) {
                    graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, new Color(255, 255, 255, (int)(20.0f * this.hover)), 0.0f, (float)n2 * 0.3f, new Color(255, 255, 255, 0)));
                    graphics2D.fillRoundRect(0, 0, n, (int)((float)n2 * 0.35f), 10, 10);
                }
                graphics2D.dispose();
                super.paintComponent(graphics);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        jButton.setContentAreaFilled(false);
        jButton.setBorderPainted(false);
        jButton.setFocusPainted(false);
        jButton.setForeground(Theme.TEXT_PRIMARY);
        jButton.setFont(Theme.uiFont(java.awt.Font.PLAIN, 12.0f));
        jButton.setCursor(Cursor.getPredefinedCursor(12));
        jButton.setPreferredSize(new Dimension(jButton.getPreferredSize().width, 34));
        return jButton;
    }

    /**
     * V39.2: ghostButton'in JToggleButton kardesi — ayni gorunum, secili
     * durumda accent dolgu. Combo yerine bu kullaniliyor (crash fix).
     */
    public static javax.swing.JToggleButton toggleButton(String text) {
        javax.swing.JToggleButton btn = new javax.swing.JToggleButton(text) {
            private float hover = 0.0f;
            private float press = 0.0f;
            private Timer ht;
            {
                this.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent mouseEvent) { anim(1.0f); }
                    @Override
                    public void mouseExited(MouseEvent mouseEvent) { anim(0.0f); }
                    @Override
                    public void mousePressed(MouseEvent mouseEvent) { press = 1.0f; }
                    @Override
                    public void mouseReleased(MouseEvent mouseEvent) { press = 0.0f; }
                });
            }
            private void anim(float target) {
                if (this.ht != null) this.ht.stop();
                float from = this.hover;
                long t0 = System.currentTimeMillis();
                this.ht = new Timer(16, null);
                this.ht.addActionListener(e -> {
                    float p = Math.min(1.0f, (float)(System.currentTimeMillis() - t0) / 140.0f);
                    this.hover = from + (target - from) * UiFx.easeOutCubic(p);
                    repaint();
                    if (p >= 1.0f) this.ht.stop();
                });
                this.ht.start();
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = this.getWidth();
                int h = this.getHeight();
                float f = 1.0f - this.press * 0.03f;
                g.translate((double) w / 2.0, (double) h / 2.0);
                g.scale(f, f);
                g.translate((double) -w / 2.0, (double) -h / 2.0);
                boolean sel = this.isSelected();
                g.setColor(UiFx.withAlpha(sel ? Theme.ACCENT : Theme.BG_ELEVATED, (int) (sel ? 230.0f : 150.0f + 60.0f * this.hover)));
                g.fillRoundRect(0, 0, w, h, 10, 10);
                if (sel) {
                    g.setPaint(new GradientPaint(0.0f, 0.0f, Theme.ACCENT_BRIGHT, 0.0f, (float) h, Theme.ACCENT_DARK));
                    g.fillRoundRect(0, 0, w, h, 10, 10);
                }
                g.setColor(UiFx.lerp(Theme.BG_BORDER, sel ? Theme.ACCENT_BRIGHT : Theme.ACCENT_BRIGHT, sel ? 1.0f : this.hover * 0.75f));
                g.setStroke(new BasicStroke(sel ? 1.6f : 1.2f));
                g.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
                g.setColor(sel ? java.awt.Color.WHITE : Theme.TEXT_PRIMARY);
                java.awt.FontMetrics fm = g.getFontMetrics();
                int tx = (w - fm.stringWidth(text)) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(text, tx, ty);
                g.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setFont(Theme.uiFont(java.awt.Font.PLAIN, 12.0f));
        btn.setCursor(Cursor.getPredefinedCursor(12));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 34));
        return btn;
    }

    public static JButton dangerButton(String string) {
        JButton jButton = new JButton(string){
            private float hover = 0.0f;
            private float press = 0.0f;
            private Timer ht;
            {
                this.addMouseListener(new MouseAdapter(){

                    @Override
                    public void mouseEntered(MouseEvent mouseEvent) {
                        anim(1.0f);
                    }

                    @Override
                    public void mouseExited(MouseEvent mouseEvent) {
                        anim(0.0f);
                    }

                    @Override
                    public void mousePressed(MouseEvent mouseEvent) {
                        press = 1.0f;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent mouseEvent) {
                        press = 0.0f;
                        repaint();
                    }
                });
            }

            private void anim(float f) {
                if (this.ht != null) {
                    this.ht.stop();
                }
                float f2 = this.hover;
                long l = System.currentTimeMillis();
                this.ht = new Timer(16, null);
                this.ht.addActionListener(actionEvent -> {
                    float f3 = Math.min(1.0f, (float)(System.currentTimeMillis() - l) / 140.0f);
                    this.hover = f2 + (f - f2) * UiFx.easeOutCubic(f3);
                    repaint();
                    if (f3 >= 1.0f) {
                        this.ht.stop();
                    }
                });
                this.ht.start();
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = this.getWidth();
                int n2 = this.getHeight();
                float f = 1.0f - this.press * 0.03f;
                graphics2D.translate((double)n / 2.0, (double)n2 / 2.0);
                graphics2D.scale(f, f);
                graphics2D.translate((double)(-n) / 2.0, (double)(-n2) / 2.0);
                // TEMA BUG FIXI: onceden sabit (55,18,18)/(80,22,22) vardi -
                // danger butonu her temada ayni koyu kirmiziydi. Artik
                // Theme.RED_DARK/RED'den turetiliyor, temayla birlikte donuyor.
                graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, UiFx.lerp(UiFx.darken(Theme.RED_DARK, 0.55f), UiFx.darken(Theme.RED_DARK, 0.30f), this.hover), 0.0f, n2, UiFx.lerp(UiFx.darken(Theme.RED_DARK, 0.75f), UiFx.darken(Theme.RED_DARK, 0.60f), this.hover)));
                graphics2D.fillRoundRect(0, 0, n, n2, 10, 10);
                graphics2D.setColor(UiFx.lerp(Theme.RED_DARK, Theme.RED, this.hover * 0.7f));
                graphics2D.setStroke(new BasicStroke(1.0f));
                graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 10, 10);
                if (this.hover > 0.05f) {
                    graphics2D.setColor(UiFx.withAlpha(Theme.RED, (int)(30.0f * this.hover)));
                    graphics2D.setStroke(new BasicStroke(2.0f));
                    graphics2D.drawRoundRect(-1, -1, n + 1, n2 + 1, 11, 11);
                }
                graphics2D.dispose();
                super.paintComponent(graphics);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        jButton.setContentAreaFilled(false);
        jButton.setBorderPainted(false);
        jButton.setFocusPainted(false);
        jButton.setForeground(Theme.RED);
        jButton.setFont(Theme.uiFont(java.awt.Font.PLAIN, 12.0f));
        jButton.setCursor(Cursor.getPredefinedCursor(12));
        jButton.setPreferredSize(new Dimension(jButton.getPreferredSize().width, 34));
        return jButton;
    }

    public static JPanel animCard() {
        return new JPanel(){
            private float hover = 0.0f;
            private Timer ht;
            {
                this.setOpaque(false);
                this.addMouseListener(new MouseAdapter(){

                    @Override
                    public void mouseEntered(MouseEvent mouseEvent) {
                        anim(1.0f);
                    }

                    @Override
                    public void mouseExited(MouseEvent mouseEvent) {
                        anim(0.0f);
                    }

                    private void anim(float f) {
                        if (ht != null) {
                            ht.stop();
                        }
                        float f2 = hover;
                        long l = System.currentTimeMillis();
                        ht = new Timer(16, null);
                        ht.addActionListener(actionEvent -> {
                            float f3 = Math.min(1.0f, (float)(System.currentTimeMillis() - l) / 200.0f);
                            hover = f2 + (f - f2) * UiFx.easeOutCubic(f3);
                            repaint();
                            if (f3 >= 1.0f) {
                                ht.stop();
                            }
                        });
                        ht.start();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = this.getWidth();
                int n2 = this.getHeight();
                float[] fArray = new float[]{0.0f, 0.5f, 1.0f};
                Color[] colorArray = new Color[]{UiFx.lerp(Theme.BG_ELEVATED, UiFx.brighten(Theme.BG_ELEVATED, 1.15f), this.hover * 0.4f), UiFx.lerp(Theme.BG_SURFACE, Theme.BG_ELEVATED, this.hover * 0.3f), Theme.BG_SURFACE};
                graphics2D.setPaint(new LinearGradientPaint(0.0f, 0.0f, 0.0f, n2, fArray, colorArray));
                graphics2D.fillRoundRect(0, 0, n, n2, 16, 16);
                if (this.hover > 0.01f) {
                    graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, new Color(255, 255, 255, (int)(18.0f * this.hover)), 0.0f, (float)n2 * 0.28f, new Color(255, 255, 255, 0)));
                    graphics2D.fillRoundRect(0, 0, n, (int)((float)n2 * 0.32f), 16, 16);
                    graphics2D.setColor(UiFx.withAlpha(Theme.ACCENT, (int)(50.0f * this.hover)));
                    graphics2D.setStroke(new BasicStroke(1.5f));
                    graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 16, 16);
                } else {
                    graphics2D.setColor(UiFx.withAlpha(Theme.BG_BORDER, 150));
                    graphics2D.setStroke(new BasicStroke(1.0f));
                    graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 16, 16);
                }
                graphics2D.dispose();
                super.paintComponent(graphics);
            }
        };
    }

    public static JPanel card(Color color, final int n) {
        return new JPanel(){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n3 = this.getWidth();
                int n2 = this.getHeight();
                float[] fArray = new float[]{0.0f, 0.6f, 1.0f};
                Color[] colorArray = new Color[]{Theme.CARD_GRADIENT_TOP, UiFx.lerp(Theme.CARD_GRADIENT_TOP, Theme.CARD_GRADIENT_BOT, 0.5f), Theme.CARD_GRADIENT_BOT};
                graphics2D.setPaint(new LinearGradientPaint(0.0f, 0.0f, 0.0f, n2, fArray, colorArray));
                graphics2D.fillRoundRect(0, 0, n3, n2, n, n);
                graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, new Color(255, 255, 255, 10), 0.0f, (float)n2 * 0.3f, new Color(255, 255, 255, 0)));
                graphics2D.fillRoundRect(0, 0, n3, (int)((float)n2 * 0.35f), n, n);
                graphics2D.setColor(UiFx.withAlpha(Theme.ACCENT, 60));
                graphics2D.setStroke(new BasicStroke(2.0f));
                graphics2D.drawLine(0, n / 2, 0, n2 - n / 2);
                graphics2D.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
    }

    public static JButton launchButton(String string) {
        JButton jButton = new JButton(string){
            private float hover = 0.0f;
            private float press = 0.0f;
            private float pulse = 0.0f;
            private float shine = 0.0f;
            private Timer ht;
            private Timer pt;
            {
                this.addMouseListener(new MouseAdapter(){

                    @Override
                    public void mouseEntered(MouseEvent mouseEvent) {
                        animHov(1.0f);
                        if (pt == null || !pt.isRunning()) {
                            long[] lArray = new long[]{System.currentTimeMillis()};
                            pt = new Timer(30, actionEvent -> {
                                pulse = (float)((Math.sin((double)(System.currentTimeMillis() - lArray[0]) / 700.0) + 1.0) / 2.0);
                                shine = (float)((System.currentTimeMillis() - lArray[0]) % 2000L) / 2000.0f;
                                repaint();
                            });
                            pt.start();
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent mouseEvent) {
                        animHov(0.0f);
                        if (pt != null) {
                            pt.stop();
                            pt = null;
                        }
                        pulse = 0.0f;
                        shine = 0.0f;
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent mouseEvent) {
                        press = 1.0f;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent mouseEvent) {
                        press = 0.0f;
                        repaint();
                    }
                });
            }

            private void animHov(float f) {
                if (this.ht != null) {
                    this.ht.stop();
                }
                float f2 = this.hover;
                long l = System.currentTimeMillis();
                this.ht = new Timer(16, null);
                this.ht.addActionListener(actionEvent -> {
                    float f3 = Math.min(1.0f, (float)(System.currentTimeMillis() - l) / 160.0f);
                    this.hover = f2 + (f - f2) * UiFx.easeOutCubic(f3);
                    repaint();
                    if (f3 >= 1.0f) {
                        this.ht.stop();
                    }
                });
                this.ht.start();
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = this.getWidth();
                int n2 = this.getHeight();
                if (!this.isEnabled()) {
                    graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, Theme.BG_ELEVATED, 0.0f, n2, Theme.BG_SURFACE));
                    graphics2D.fillRoundRect(0, 0, n, n2, 14, 14);
                    graphics2D.setColor(Theme.BG_BORDER);
                    graphics2D.setStroke(new BasicStroke(1.2f));
                    graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 14, 14);
                    graphics2D.dispose();
                    super.paintComponent(graphics);
                    return;
                }
                float f = this.press > 0.0f ? 0.97f : 1.0f + this.hover * 0.012f;
                graphics2D.translate((double)n / 2.0, (double)n2 / 2.0);
                graphics2D.scale(f, f);
                graphics2D.translate((double)(-n) / 2.0, (double)(-n2) / 2.0);
                graphics2D.setColor(UiFx.withAlpha(Color.BLACK, (int)(30.0f + 15.0f * this.hover)));
                graphics2D.fillRoundRect(2, 5, n - 2, n2, 14, 14);
                graphics2D.setColor(UiFx.withAlpha(Theme.ACCENT, (int)(20.0f + 30.0f * this.pulse + 20.0f * this.hover)));
                graphics2D.setStroke(new BasicStroke(5.0f));
                graphics2D.drawRoundRect(-3, -3, n + 5, n2 + 5, 18, 18);
                float[] fArray = new float[]{0.0f, 0.35f, 0.7f, 1.0f};
                Color[] colorArray = new Color[]{Theme.ACCENT_BRIGHT, UiFx.lerp(Theme.ACCENT_BRIGHT, Theme.ACCENT, 0.4f + this.hover * 0.2f), UiFx.lerp(Theme.ACCENT, Theme.ACCENT_DARK, 0.3f), Theme.ACCENT_DARK};
                graphics2D.setPaint(new LinearGradientPaint(0.0f, 0.0f, 0.0f, n2, fArray, colorArray));
                graphics2D.fillRoundRect(0, 0, n, n2, 14, 14);
                graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, new Color(255, 255, 255, (int)(75.0f + 20.0f * this.hover)), 0.0f, (float)n2 * 0.45f, new Color(255, 255, 255, 0)));
                graphics2D.fillRoundRect(0, 0, n, (int)((float)n2 * 0.5f), 14, 14);
                if (this.hover > 0.1f && this.shine > 0.0f) {
                    float f2 = this.shine * (float)(n + 80) - 40.0f;
                    graphics2D.setPaint(new LinearGradientPaint(f2 - 30.0f, 0.0f, f2 + 30.0f, 0.0f, new float[]{0.0f, 0.5f, 1.0f}, new Color[]{new Color(255, 255, 255, 0), new Color(255, 255, 255, (int)(50.0f * this.hover)), new Color(255, 255, 255, 0)}));
                    graphics2D.fillRoundRect(0, 0, n, n2, 14, 14);
                }
                graphics2D.setColor(UiFx.withAlpha(Theme.ACCENT_BRIGHT, (int)(80.0f + 40.0f * this.hover)));
                graphics2D.setStroke(new BasicStroke(1.5f));
                graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 14, 14);
                graphics2D.dispose();
                super.paintComponent(graphics);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        jButton.setContentAreaFilled(false);
        jButton.setBorderPainted(false);
        jButton.setFocusPainted(false);
        jButton.setForeground(Color.WHITE);
        jButton.setFont(Theme.uiFont(java.awt.Font.BOLD, 15.0f));
        jButton.setCursor(Cursor.getPredefinedCursor(12));
        return jButton;
    }

    public static JTextField searchField(String string) {
        JTextField jTextField = new JTextField(){
            private float focus = 0.0f;
            private Timer ft;
            {
                this.addFocusListener(new FocusAdapter(){

                    @Override
                    public void focusGained(FocusEvent focusEvent) {
                        anim(1.0f);
                    }

                    @Override
                    public void focusLost(FocusEvent focusEvent) {
                        anim(0.0f);
                    }

                    private void anim(float f) {
                        if (ft != null) {
                            ft.stop();
                        }
                        float f2 = focus;
                        long l = System.currentTimeMillis();
                        ft = new Timer(16, null);
                        ft.addActionListener(actionEvent -> {
                            float f3 = Math.min(1.0f, (float)(System.currentTimeMillis() - l) / 160.0f);
                            focus = f2 + (f - f2) * UiFx.easeOutCubic(f3);
                            repaint();
                            if (f3 >= 1.0f) {
                                ft.stop();
                            }
                        });
                        ft.start();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = this.getWidth();
                int n2 = this.getHeight();
                graphics2D.setColor(UiFx.lerp(Theme.BG_BASE, Theme.BG_ELEVATED, this.focus * 0.5f));
                graphics2D.fillRoundRect(0, 0, n, n2, 10, 10);
                if (this.focus > 0.01f) {
                    graphics2D.setColor(UiFx.withAlpha(Theme.ACCENT, (int)(180.0f * this.focus)));
                    graphics2D.setStroke(new BasicStroke(1.5f));
                } else {
                    graphics2D.setColor(Theme.BG_BORDER);
                    graphics2D.setStroke(new BasicStroke(1.0f));
                }
                graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 10, 10);
                graphics2D.dispose();
                super.paintComponent(graphics);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        jTextField.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        jTextField.setBackground(Theme.BG_BASE);
        jTextField.setForeground(Theme.TEXT_PRIMARY);
        jTextField.setCaretColor(Theme.ACCENT);
        jTextField.setFont(Theme.uiFont(java.awt.Font.PLAIN, 13.0f));
        jTextField.putClientProperty("JTextField.placeholderText", string);
        return jTextField;
    }

    public static JProgressBar progressPill() {
        JProgressBar jProgressBar = new JProgressBar(0, 100){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = this.getWidth();
                int n2 = this.getHeight();
                graphics2D.setColor(UiFx.withAlpha(Theme.BG_BORDER, 150));
                graphics2D.fillRoundRect(0, 0, n, n2, n2, n2);
                if (!this.isIndeterminate()) {
                    int n3 = (int)((float)this.getValue() / (float)this.getMaximum() * (float)n);
                    if (n3 > 0) {
                        graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, Theme.ACCENT_BRIGHT, n3, 0.0f, Theme.ACCENT));
                        graphics2D.fillRoundRect(0, 0, n3, n2, n2, n2);
                        graphics2D.setPaint(new GradientPaint(Math.max(0, n3 - 50), 0.0f, new Color(255, 255, 255, 0), n3, 0.0f, new Color(255, 255, 255, 70)));
                        graphics2D.fillRoundRect(0, 0, n3, n2, n2, n2);
                    }
                } else {
                    long l = System.currentTimeMillis();
                    float f = (float)(l % 1200L) / 1200.0f;
                    int n4 = n / 3;
                    int n5 = (int)(f * (float)(n + n4)) - n4;
                    Color color = UiFx.withAlpha(Theme.ACCENT, 0);
                    graphics2D.setPaint(new LinearGradientPaint(n5, 0.0f, n5 + n4, 0.0f, new float[]{0.0f, 0.5f, 1.0f}, new Color[]{color, Theme.ACCENT_BRIGHT, color}));
                    graphics2D.fillRoundRect(0, 0, n, n2, n2, n2);
                }
                graphics2D.dispose();
            }
        };
        jProgressBar.setOpaque(false);
        jProgressBar.setBorderPainted(false);
        jProgressBar.setStringPainted(false);
        jProgressBar.setPreferredSize(new Dimension(0, 5));
        Timer[] timerArray = new Timer[]{null};
        jProgressBar.addChangeListener(changeEvent -> {
            if (jProgressBar.isIndeterminate() && timerArray[0] == null) {
                timerArray[0] = new Timer(30, actionEvent -> jProgressBar.repaint());
                timerArray[0].start();
            } else if (!jProgressBar.isIndeterminate() && timerArray[0] != null) {
                timerArray[0].stop();
                timerArray[0] = null;
            }
        });
        return jProgressBar;
    }

    public static JLabel chip(String string, Color color, Color color2) {
        JLabel jLabel = new JLabel(string){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, UiFx.brighten(this.getBackground(), 1.3f), 0.0f, this.getHeight(), this.getBackground()));
                graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), this.getHeight(), this.getHeight());
                graphics2D.setColor(UiFx.withAlpha(this.getBackground().brighter(), 120));
                graphics2D.setStroke(new BasicStroke(1.0f));
                graphics2D.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, this.getHeight(), this.getHeight());
                graphics2D.dispose();
                super.paintComponent(graphics);
            }
        };
        jLabel.setBackground(color);
        jLabel.setForeground(color2);
        jLabel.setFont(Theme.uiFont(java.awt.Font.BOLD, 10.0f));
        jLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        jLabel.setOpaque(false);
        return jLabel;
    }

    public static JSeparator separator() {
        JSeparator jSeparator = new JSeparator();
        jSeparator.setForeground(Theme.BG_BORDER);
        jSeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return jSeparator;
    }

    public static JLabel sectionLabel(String string) {
        JLabel jLabel = new JLabel(string.toUpperCase());
        jLabel.setFont(Theme.uiFont(java.awt.Font.BOLD, 10.0f));
        jLabel.setForeground(Theme.TEXT_MUTED);
        return jLabel;
    }

    public static JLabel label(String string) {
        JLabel jLabel = new JLabel(string);
        jLabel.setFont(Theme.uiFont(java.awt.Font.PLAIN, 12.0f));
        jLabel.setForeground(Theme.TEXT_SECONDARY);
        return jLabel;
    }

    public static JToggleButton loaderToggleButton(String string) {
        JToggleButton jToggleButton = new JToggleButton(string){
            private float hover = 0.0f;
            private Timer ht;
            {
                this.setContentAreaFilled(false);
                this.setBorderPainted(false);
                this.setFocusPainted(false);
                this.setOpaque(false);
                this.setCursor(Cursor.getPredefinedCursor(12));
                this.addMouseListener(new MouseAdapter(){

                    @Override
                    public void mouseEntered(MouseEvent mouseEvent) {
                        anim(1.0f);
                    }

                    @Override
                    public void mouseExited(MouseEvent mouseEvent) {
                        anim(0.0f);
                    }
                });
            }

            private void anim(float f) {
                if (this.ht != null) {
                    this.ht.stop();
                }
                float f2 = this.hover;
                long l = System.currentTimeMillis();
                this.ht = new Timer(16, null);
                this.ht.addActionListener(actionEvent -> {
                    float f3 = Math.min(1.0f, (float)(System.currentTimeMillis() - l) / 130.0f);
                    this.hover = f2 + (f - f2) * UiFx.easeOutCubic(f3);
                    repaint();
                    if (f3 >= 1.0f) {
                        this.ht.stop();
                    }
                });
                this.ht.start();
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = this.getWidth();
                int n2 = this.getHeight();
                boolean bl = this.isSelected();
                if (bl) {
                    graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, UiFx.lerp(Theme.ACCENT_BRIGHT, Theme.ACCENT_BRIGHT, this.hover * 0.3f), 0.0f, n2, UiFx.lerp(Theme.ACCENT_DARK, Theme.ACCENT, this.hover * 0.4f)));
                    graphics2D.fillRoundRect(0, 0, n, n2, 10, 10);
                    graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, new Color(255, 255, 255, 55), 0.0f, (float)n2 * 0.45f, new Color(255, 255, 255, 0)));
                    graphics2D.fillRoundRect(0, 0, n, (int)((float)n2 * 0.5f), 10, 10);
                    graphics2D.setColor(UiFx.withAlpha(Theme.ACCENT_BRIGHT, (int)(90.0f + 35.0f * this.hover)));
                    graphics2D.setStroke(new BasicStroke(1.5f));
                    graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 10, 10);
                } else {
                    graphics2D.setColor(UiFx.withAlpha(Theme.BG_ELEVATED, (int)(150.0f + 60.0f * this.hover)));
                    graphics2D.fillRoundRect(0, 0, n, n2, 10, 10);
                    graphics2D.setColor(UiFx.withAlpha(Theme.ACCENT, (int)(20.0f + 35.0f * this.hover)));
                    graphics2D.fillRoundRect(0, 0, n, n2, 10, 10);
                    graphics2D.setColor(UiFx.lerp(Theme.BG_BORDER, Theme.ACCENT_MUTED, this.hover * 0.85f));
                    graphics2D.setStroke(new BasicStroke(1.0f));
                    graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 10, 10);
                }
                graphics2D.dispose();
                super.paintComponent(graphics);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        jToggleButton.setFont(Theme.uiFont(java.awt.Font.PLAIN, 11.0f));
        jToggleButton.setForeground(Theme.TEXT_PRIMARY);
        jToggleButton.getModel().addChangeListener(changeEvent -> {
            jToggleButton.setForeground(jToggleButton.isSelected() ? Color.WHITE : Theme.TEXT_PRIMARY);
            jToggleButton.repaint();
        });
        jToggleButton.setPreferredSize(new Dimension(72, 28));
        return jToggleButton;
    }

    public static void addHoverEffect(final AbstractButton abstractButton, final Color color, final Color color2) {
        abstractButton.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                abstractButton.setBackground(color2);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                abstractButton.setBackground(color);
            }
        });
    }

    public static Border roundedBorder(final Color color, final int n, final int n2) {
        return new Border(){

            @Override
            public Insets getBorderInsets(Component component) {
                int n3 = n2 + 2;
                return new Insets(n3, n3, n3, n3);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }

            @Override
            public void paintBorder(Component component, Graphics graphics, int n5, int n22, int n3, int n4) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setStroke(new BasicStroke(n2));
                graphics2D.setColor(color);
                graphics2D.drawRoundRect(n5 + n2 / 2, n22 + n2 / 2, n3 - n2, n4 - n2, n, n);
                graphics2D.dispose();
            }
        };
    }

    public static JScrollPane cleanScroll(Component component) {
        JScrollPane jScrollPane = new JScrollPane(component);
        jScrollPane.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane.setOpaque(false);
        jScrollPane.getViewport().setOpaque(false);
        jScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return jScrollPane;
    }

    public static Border pad(int n, int n2, int n3, int n4) {
        return BorderFactory.createEmptyBorder(n, n2, n3, n4);
    }
}

