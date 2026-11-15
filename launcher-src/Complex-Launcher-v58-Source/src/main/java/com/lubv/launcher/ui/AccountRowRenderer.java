package com.lubv.launcher.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/** Hesap listesi satiri: avatar, kullanici adi, tur etiketi ve "aktif" gostergesi. */
final class AccountRowRenderer implements ListCellRenderer<String> {
    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean selected, boolean focus) {
        final String raw = value == null ? "" : value;
        final boolean active = raw.startsWith(" ") && !raw.startsWith("  ");
        String t = raw.trim();
        String type = "";
        int p = t.lastIndexOf("   (");
        if (p > 0 && t.endsWith(")")) {
            type = t.substring(p + 4, t.length() - 1);
            t = t.substring(0, p);
        }
        final String name = t, kind = type;
        return new JComponent() {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g.setColor(Theme.BG_BASE);
                g.fillRect(0, 0, w, h);
                Color a = Theme.ACCENT;
                g.setColor(selected ? new Color(a.getRed(), a.getGreen(), a.getBlue(), 44) : new Color(255, 255, 255, 8));
                g.fillRoundRect(4, 3, w - 8, h - 6, 16, 16);
                g.setColor(selected ? new Color(a.getRed(), a.getGreen(), a.getBlue(), 150) : Theme.BG_BORDER);
                g.drawRoundRect(4, 3, w - 9, h - 7, 16, 16);
                int d = 38, ax = 18, ay = (h - d) / 2;
                float hue = (Math.abs(name.hashCode()) % 360) / 360f;
                g.setPaint(new java.awt.GradientPaint(0, ay, Color.getHSBColor(hue, 0.45f, 0.95f), 0, ay + d, Color.getHSBColor(hue, 0.75f, 0.55f)));
                g.fillOval(ax, ay, d, d);
                g.setColor(Color.WHITE);
                g.setFont(Theme.uiFont(1, 16.0f));
                FontMetrics fm = g.getFontMetrics();
                String ch = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
                g.drawString(ch, ax + (d - fm.stringWidth(ch)) / 2, ay + (d - fm.getHeight()) / 2 + fm.getAscent());
                if (active) {
                    g.setColor(new Color(34, 197, 94));
                    g.setStroke(new java.awt.BasicStroke(2f));
                    g.drawOval(ax - 3, ay - 3, d + 5, d + 5);
                }
                g.setFont(Theme.uiFont(1, 14.0f));
                g.setColor(Theme.TEXT_PRIMARY);
                fm = g.getFontMetrics();
                int tx = ax + d + 16;
                g.drawString(name, tx, h / 2 - 2);
                int nameW = fm.stringWidth(name);
                if (!kind.isEmpty()) {
                    boolean ms = "Microsoft".equals(kind);
                    g.setFont(Theme.uiFont(1, 10.0f));
                    FontMetrics kf = g.getFontMetrics();
                    int kw = kf.stringWidth(kind) + 16, kh = 18;
                    g.setColor(ms ? new Color(a.getRed(), a.getGreen(), a.getBlue(), 50) : new Color(255, 255, 255, 18));
                    g.fillRoundRect(tx, h / 2 + 5, kw, kh, kh, kh);
                    g.setColor(ms ? Theme.ACCENT_BRIGHT : Theme.TEXT_SECONDARY);
                    g.drawString(kind, tx + 8, h / 2 + 5 + (kh - kf.getHeight()) / 2 + kf.getAscent());
                }
                if (active) {
                    g.setFont(Theme.uiFont(1, 10.0f));
                    FontMetrics af = g.getFontMetrics();
                    String tag = L10nHolder.active();
                    int aw = af.stringWidth(tag) + 24, ah = 22;
                    int ax2 = w - aw - 18, ay2 = (h - ah) / 2;
                    g.setColor(new Color(34, 197, 94, 40));
                    g.fillRoundRect(ax2, ay2, aw, ah, ah, ah);
                    g.setColor(new Color(34, 197, 94));
                    g.fillOval(ax2 + 9, ay2 + ah / 2 - 3, 6, 6);
                    g.drawString(tag, ax2 + 19, ay2 + (ah - af.getHeight()) / 2 + af.getAscent());
                }
                g.dispose();
            }
        };
    }

    /** Dil secimine gore "AKTIF/ACTIVE" metni. */
    private static final class L10nHolder {
        static String active() { return com.lubv.launcher.core.L10n.isEnglish() ? "ACTIVE" : "AKT\u0130F"; }
    }
}
