package com.lubv.launcher.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import javax.swing.JList;
import javax.swing.ListModel;

/** Bos oldugunda ortada ikon + baslik + aciklama ("bos durum") cizen JList. */
class EmptyList<T> extends JList<T> {
    private final Icons.Kind kind;
    private final String title;
    private final String sub;

    EmptyList(ListModel<T> model, Icons.Kind kind, String title, String sub) {
        super(model);
        this.kind = kind;
        this.title = title;
        this.sub = sub;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getModel().getSize() != 0) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle r = getVisibleRect();
        int cx = r.x + r.width / 2, cy = r.y + r.height / 2 - 14;
        Color a = Theme.ACCENT;
        g2.setColor(new Color(a.getRed(), a.getGreen(), a.getBlue(), 28));
        g2.fillOval(cx - 36, cy - 62, 72, 72);
        g2.setColor(new Color(a.getRed(), a.getGreen(), a.getBlue(), 70));
        g2.drawOval(cx - 36, cy - 62, 72, 72);
        Icons.paint(g2, kind, cx - 16, cy - 42, 32, Theme.ACCENT_BRIGHT);
        g2.setFont(Theme.uiFont(1, 14.0f));
        g2.setColor(Theme.TEXT_PRIMARY);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, cx - fm.stringWidth(title) / 2, cy + 34);
        g2.setFont(Theme.uiFont(0, 12.0f));
        g2.setColor(Theme.TEXT_MUTED);
        fm = g2.getFontMetrics();
        String s = Nav.clip(fm, sub, Math.max(60, r.width - 24));
        g2.drawString(s, cx - fm.stringWidth(s) / 2, cy + 54);
        g2.dispose();
    }
}
