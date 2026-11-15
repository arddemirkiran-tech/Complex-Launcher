package com.lubv.launcher.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Metni bilesenin gercek genisligine gore "..." ile kirpan etiket.
 *
 * <p>JLabel uzun metni uc nokta koymadan keser: liste satirlarinda aciklama
 * cumle ortasinda ("...features in a s") biter ve etiketin tercih ettigi
 * genislik viewport'tan buyuk oldugu icin listenin altinda gereksiz yatay
 * kaydirma cubugu acilir. Bu etiket genislik olarak 0 tercih eder, boylece
 * satir mevcut kolon genisligine oturur, ve cizim sirasinda metni kirpar.</p>
 */
public class EllipsisLabel extends JLabel {
    private static final String ELLIPSIS = "\u2026";
    private boolean middleTruncate;

    public EllipsisLabel() {
        super();
        Dimension preferred = this.getPreferredSize();
        this.setPreferredSize(new Dimension(0, preferred.height));
        this.setMinimumSize(new Dimension(0, 0));
    }

    /**
     * Uzun dosya yollari icin: metnin basini degil ortasini kirp, sonu
     * (asil klasor adi) gorunur kalsin.
     */
    public void setMiddleTruncate(boolean middleTruncate) {
        this.middleTruncate = middleTruncate;
        this.repaint();
    }

    /** Verilen genislige sigmayan metni "..." ile kirpar. */
    static String ellipsize(String text, FontMetrics fontMetrics, int availableWidth) {
        return EllipsisLabel.ellipsize(text, fontMetrics, availableWidth, false);
    }

    static String ellipsize(String text, FontMetrics fontMetrics, int availableWidth, boolean middleTruncate) {
        if (text == null || text.isEmpty() || availableWidth <= 0) {
            return text;
        }
        if (fontMetrics.stringWidth(text) <= availableWidth) {
            return text;
        }
        int ellipsisWidth = fontMetrics.stringWidth(ELLIPSIS);
        if (middleTruncate) {
            // Sondan basla ve sigdigi surece one dogru buyut: en uzun
            // ("..." + son parca) metni bulunur.
            int start = text.length() - 1;
            while (start > 0 && fontMetrics.stringWidth(ELLIPSIS + text.substring(start - 1)) <= availableWidth) {
                --start;
            }
            if (fontMetrics.stringWidth(ELLIPSIS + text.substring(start)) > availableWidth) {
                return ELLIPSIS;
            }
            return ELLIPSIS + text.substring(start);
        }
        int end = text.length();
        while (end > 0 && fontMetrics.stringWidth(text.substring(0, end)) + ellipsisWidth > availableWidth) {
            --end;
        }
        while (end > 0 && Character.isWhitespace(text.charAt(end - 1))) {
            --end;
        }
        return end > 0 ? text.substring(0, end) + ELLIPSIS : "";
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        String text = this.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        Graphics2D graphics2D = (Graphics2D)graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setFont(this.getFont());
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        String shown = EllipsisLabel.ellipsize(text, fontMetrics, this.getWidth(), this.middleTruncate);
        graphics2D.setColor(this.getForeground());
        int x = 0;
        if (this.getHorizontalAlignment() == SwingConstants.CENTER) {
            x = (this.getWidth() - fontMetrics.stringWidth(shown)) / 2;
        } else if (this.getHorizontalAlignment() == SwingConstants.RIGHT) {
            x = this.getWidth() - fontMetrics.stringWidth(shown);
        }
        int y = (this.getHeight() - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();
        graphics2D.drawString(shown, Math.max(0, x), y);
        graphics2D.dispose();
    }
}
