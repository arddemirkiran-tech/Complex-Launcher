package com.lubv.launcher.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Liste hucre bilesenleri icin ortak taban: yuvarlatilmis "satir" cizer.
 * Secim, eski kodun arka plan rengi kalibindan anlasilir (ACCENT_DARK veya
 * yari saydam vurgu = secili), boylece her panelin renderer'i degismeden calisir.
 */
class RowPanel extends JPanel {
    RowPanel() {
        setOpaque(true);
    }

    private boolean isSelectedLook() {
        Color bg = getBackground();
        if (bg == null) return false;
        return bg.equals(Theme.ACCENT_DARK) || (bg.getAlpha() < 255 && bg.getAlpha() > 0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.BG_BASE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        int x = 4, y = 2, w = getWidth() - 8, h = getHeight() - 4;
        if (isSelectedLook()) {
            Color a = Theme.ACCENT;
            g2.setColor(new Color(a.getRed(), a.getGreen(), a.getBlue(), 48));
            g2.fillRoundRect(x, y, w, h, 14, 14);
            g2.setColor(new Color(a.getRed(), a.getGreen(), a.getBlue(), 150));
            g2.drawRoundRect(x, y, w - 1, h - 1, 14, 14);
            g2.setColor(a);
            g2.fillRoundRect(x, y + h / 2 - 11, 3, 22, 3, 3);
        } else {
            g2.setColor(new Color(255, 255, 255, 7));
            g2.fillRoundRect(x, y, w, h, 14, 14);
        }
        g2.dispose();
    }
}
