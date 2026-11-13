package com.lubv.launcher.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Yeniden kullanılabilir kart / chip bileşenleri (yeni tasarım dili). */
final class Cards {
    private Cards() {}

    static final int ARC = 18;

    /** Yuvarlatılmış, ince çerçeveli yüzey. */
    static class Surface extends JPanel {
        Surface(java.awt.LayoutManager lm) {
            super(lm);
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.BG_ELEVATED);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.setColor(Theme.BG_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static JPanel card(String title, JComponent body) {
        Surface s = new Surface(new BorderLayout(0, 12));
        s.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        if (title != null) {
            JLabel t = new JLabel(title.toUpperCase());
            t.setFont(Theme.uiFont(1, 11.0f));
            t.setForeground(Theme.TEXT_MUTED);
            s.add(t, BorderLayout.NORTH);
        }
        s.add(body, BorderLayout.CENTER);
        return s;
    }

    /** Küçük hap etiket. */
    static class Chip extends JLabel {
        private boolean accent;
        Chip(String text, boolean accent) {
            super(text);
            this.accent = accent;
            setFont(Theme.uiFont(1, 12.0f));
            setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
            setOpaque(false);
        }
        // v52: setText override kaldirildi. JLabel.setText zaten revalidate
        // yapiyor; buradaki ikinci revalidate her chip guncellemesinde ana
        // sayfayi iki kez yeniden yerlesime sokuyordu (glitch kaynagi).
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(accent ? new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 46) : new Color(255, 255, 255, 14));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setColor(accent ? new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 120) : new Color(255, 255, 255, 28));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g2.dispose();
            setForeground(accent ? Theme.ACCENT_BRIGHT : Theme.TEXT_SECONDARY);
            super.paintComponent(g);
        }
    }

    /** Alan etiketi (kart içi satır başlığı). */
    static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.uiFont(1, 12.0f));
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    static Dimension fixed(int w, int h) { return new Dimension(w, h); }

    static String clip(FontMetrics fm, String s, int max) { return Nav.clip(fm, s, max); }

    /** Izometrik "blok" cizimi (Minecraft hissi) — hero karti dekoru. */
    static void paintCube(Graphics2D g0, int cx, int cy, int s, int alpha, boolean grid) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double k = 0.866;
        int[] tx = {cx, (int) (cx + s * k), cx, (int) (cx - s * k)};
        int[] ty = {cy - s, cy - s / 2, cy, cy - s / 2};
        int[] lx = {(int) (cx - s * k), cx, cx, (int) (cx - s * k)};
        int[] ly = {cy - s / 2, cy, cy + s, cy + s / 2};
        int[] rx = {cx, (int) (cx + s * k), (int) (cx + s * k), cx};
        int[] ry = {cy, cy - s / 2, cy + s / 2, cy + s};
        Color a = Theme.ACCENT_BRIGHT, d = Theme.ACCENT_DARK;
        g.setPaint(new java.awt.GradientPaint(cx, cy - s, new Color(255, 255, 255, Math.min(255, alpha + 40)), cx, cy, new Color(a.getRed(), a.getGreen(), a.getBlue(), alpha)));
        g.fillPolygon(tx, ty, 4);
        g.setPaint(new java.awt.GradientPaint(cx - s, cy, new Color(a.getRed(), a.getGreen(), a.getBlue(), alpha), cx, cy + s, new Color(d.getRed(), d.getGreen(), d.getBlue(), alpha)));
        g.fillPolygon(lx, ly, 4);
        g.setPaint(new java.awt.GradientPaint(cx + s, cy, new Color(d.getRed(), d.getGreen(), d.getBlue(), alpha), cx, cy + s, new Color(0, 0, 0, Math.min(255, alpha / 2 + 30))));
        g.fillPolygon(rx, ry, 4);
        g.setColor(new Color(255, 255, 255, Math.min(255, alpha / 3 + 20)));
        g.setStroke(new java.awt.BasicStroke(1.2f));
        g.drawPolygon(tx, ty, 4); g.drawPolygon(lx, ly, 4); g.drawPolygon(rx, ry, 4);
        if (grid) {
            g.setColor(new Color(255, 255, 255, 28));
            for (int i = 1; i < 4; i++) {
                int xl = (int) (cx - s * k * i / 4.0), xr = (int) (cx + s * k * i / 4.0);
                g.drawLine(xl, cy - s / 2 + (int) (s * 0.5 * i / 4.0 * 0) + (int) (s * 0.5 * (4 - i) / 4.0 * 0) + (int) (s * 0.5 * (i) / 4.0), xl, cy + s / 2 + (int) (s * 0.5 * i / 4.0));
                g.drawLine(xr, cy - s / 2 + (int) (s * 0.5 * i / 4.0), xr, cy + s / 2 + (int) (s * 0.5 * i / 4.0));
            }
        }
        g.dispose();
    }

    /** Buyuk deger + kucuk aciklama iceren istatistik kutusu. */
    static class StatTile extends JComponent {
        private String value = "-";
        private final String caption;
        private McArt.Sprite sprite;
        StatTile(String caption, McArt.Sprite sprite) {
            this(caption);
            this.sprite = sprite;
        }
        StatTile(String caption) {
            this.caption = caption;
            setPreferredSize(new Dimension(100, 74));
            setOpaque(false);
        }
        void setValue(String v) {
            if (!v.equals(value)) { value = v; repaint(); }
        }
        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setColor(Theme.BG_ELEVATED);
            g.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
            g.setColor(Theme.BG_BORDER);
            g.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);
            int tx = 14;
            if (sprite != null) {
                McArt.paintSprite(g, sprite, 12, 13, 3, 1f);
                tx = 42;
            } else {
                g.setColor(Theme.ACCENT);
                g.fillRoundRect(12, 14, 3, 18, 3, 3);
                tx = 22;
            }
            g.setFont(Theme.uiFont(1, 17.0f));
            g.setColor(Theme.TEXT_PRIMARY);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(clip(fm, value, w - tx - 8), tx, 31);
            g.setFont(Theme.uiFont(0, 11.0f));
            g.setColor(Theme.TEXT_MUTED);
            fm = g.getFontMetrics();
            g.drawString(clip(fm, caption, w - 24), 14, h - 16);
            g.dispose();
        }
    }

    /** Ayar bolumu basligi: vurgu cubugu + buyuk harf baslik + ince cizgi. */
    static JComponent section(String title, String subtitle) {
        return new JComponent() {
            {
                setPreferredSize(new Dimension(100, subtitle == null ? 34 : 46));
                setOpaque(false);
            }
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth();
                g.setColor(Theme.ACCENT);
                g.fillRoundRect(0, 6, 4, 18, 4, 4);
                g.setFont(Theme.uiFont(1, 13.0f));
                g.setColor(Theme.TEXT_PRIMARY);
                FontMetrics fm = g.getFontMetrics();
                String t = title.toUpperCase();
                g.drawString(t, 14, 6 + fm.getAscent() - 1);
                int lx = 14 + fm.stringWidth(t) + 14;
                g.setColor(Theme.BG_BORDER);
                g.fillRect(lx, 15, Math.max(0, w - lx), 1);
                if (subtitle != null) {
                    g.setFont(Theme.uiFont(0, 11.0f));
                    g.setColor(Theme.TEXT_MUTED);
                    g.drawString(subtitle, 14, 40);
                }
                g.dispose();
            }
        };
    }
}
