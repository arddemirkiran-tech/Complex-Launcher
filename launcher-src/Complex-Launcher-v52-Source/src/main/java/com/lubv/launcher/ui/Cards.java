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
}
