package com.lubv.launcher.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Modern sol gezinme çubuğu: bölüm başlıkları, ikonlu satırlar, seçili "hap" göstergesi. */
public final class Nav extends JPanel {
    public static final int WIDTH = 236;
    private final List<Item> items = new ArrayList<>();
    private final JPanel list = new JPanel();
    private final IntConsumer onSelect;
    private int selected = -1;

    public Nav(IntConsumer onSelect) {
        super(new BorderLayout());
        this.onSelect = onSelect;
        setOpaque(false);
        setPreferredSize(new Dimension(WIDTH, 0));
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        add(list, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Theme.BG_BASE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Theme.BG_BORDER);
        g2.fillRect(getWidth() - 1, 0, 1, getHeight());
        g2.dispose();
    }

    public void setHeader(JComponent c) { add(c, BorderLayout.NORTH); }
    public void setFooter(JComponent c) { add(c, BorderLayout.SOUTH); }

    public void addSection(String title) {
        JLabel l = new JLabel(title.toUpperCase());
        l.setFont(Theme.uiFont(1, 10.0f));
        l.setForeground(Theme.TEXT_MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(16, 12, 6, 0));
        l.setAlignmentX(0f);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        list.add(l);
    }

    public void addItem(String text, Icons.Kind kind, int tabIndex) {
        Item it = new Item(text, kind, tabIndex);
        items.add(it);
        list.add(it);
        list.add(Box.createVerticalStrut(2));
    }

    public void addGlue() { list.add(Box.createVerticalGlue()); }

    public void setSelected(int tabIndex) {
        selected = tabIndex;
        for (Item i : items) i.repaint();
    }

    private final class Item extends JComponent {
        private final String text;
        private final Icons.Kind kind;
        private final int tab;
        private boolean hover;

        Item(String text, Icons.Kind kind, int tab) {
            this.text = text;
            this.kind = kind;
            this.tab = tab;
            setAlignmentX(0f);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setPreferredSize(new Dimension(100, 40));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                private boolean pressHandled;
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; pressHandled = false; repaint(); }
                // Secim PRESS aninda yapilir. Eskiden mouseClicked kullaniliyordu;
                // Swing bunu yalnizca press ve release AYNI bilesende biterse
                // tetikler. Satirlar arasinda kayan / hafif hareket eden tiklamalar
                // ve pencere ilk kez aktiflesirken gelen tiklamalar kayboluyor,
                // kullanici "iki kere basmam gerekiyor" diyordu. Release yalnizca
                // press kacirilmissa yedek yol olarak calisir; cift tetikleme yok.
                @Override public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        pressHandled = true;
                        Nav.this.onSelect.accept(tab);
                    }
                }
                @Override public void mouseReleased(MouseEvent e) {
                    if (!pressHandled && e.getButton() == MouseEvent.BUTTON1) {
                        Nav.this.onSelect.accept(tab);
                    }
                    pressHandled = false;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            boolean sel = tab == selected;
            int w = getWidth(), h = getHeight();
            if (sel) {
                g2.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 40));
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                g2.setColor(Theme.ACCENT);
                g2.fillRoundRect(0, h / 2 - 9, 3, 18, 3, 3);
            } else if (hover) {
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillRoundRect(0, 0, w, h, 12, 12);
            }
            Color fg = sel ? Theme.TEXT_PRIMARY : (hover ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
            Icons.paint(g2, kind, 14, (h - 20) / 2f, 20, sel ? Theme.ACCENT_BRIGHT : fg);
            g2.setFont(Theme.uiFont(sel ? 1 : 0, 13.0f));
            g2.setColor(fg);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, 44, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Alt köşedeki hesap kartı: avatar + isim + tür. Tıklayınca hesaplar sekmesi. */
    public static JComponent accountChip(Supplier<String> name, Supplier<String> sub, Supplier<BufferedImage> photo, Runnable click) {
        JComponent c = new JComponent() {
            boolean hover;
            boolean pressHandled;
            {
                setPreferredSize(new Dimension(100, 60));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; pressHandled = false; repaint(); }
                    // Ayni press/release guvenilirligi hesap kartinda da gecerli.
                    @Override public void mousePressed(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            pressHandled = true;
                            click.run();
                        }
                    }
                    @Override public void mouseReleased(MouseEvent e) {
                        if (!pressHandled && e.getButton() == MouseEvent.BUTTON1) {
                            click.run();
                        }
                        pressHandled = false;
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(hover ? Theme.BG_ELEVATED : Theme.BG_SURFACE);
                g2.fillRoundRect(0, 0, w, h, 14, 14);
                g2.setColor(Theme.BG_BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
                int d = 36, ax = 12, ay = (h - d) / 2;
                BufferedImage ph = photo.get();
                String n = name.get();
                if (ph != null) {
                    Graphics2D p = (Graphics2D) g2.create();
                    p.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    p.setClip(new java.awt.geom.Ellipse2D.Float(ax, ay, d, d));
                    p.drawImage(ph, ax, ay, d, d, null);
                    p.dispose();
                } else {
                    g2.setPaint(new java.awt.GradientPaint(ax, ay, Theme.ACCENT_BRIGHT, ax + d, ay + d, Theme.ACCENT_DARK));
                    g2.fillOval(ax, ay, d, d);
                    g2.setColor(Color.WHITE);
                    g2.setFont(Theme.uiFont(1, 15.0f));
                    String ch = n == null || n.isEmpty() ? "?" : n.substring(0, 1).toUpperCase();
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(ch, ax + (d - fm.stringWidth(ch)) / 2, ay + (d - fm.getHeight()) / 2 + fm.getAscent());
                }
                int tx = ax + d + 10, maxW = w - tx - 10;
                g2.setFont(Theme.uiFont(1, 13.0f));
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.drawString(clip(g2.getFontMetrics(), n == null ? "" : n, maxW), tx, h / 2 - 2);
                g2.setFont(Theme.uiFont(0, 11.0f));
                g2.setColor(Theme.TEXT_MUTED);
                String s = sub.get();
                g2.drawString(clip(g2.getFontMetrics(), s == null ? "" : s, maxW), tx, h / 2 + 13);
                g2.dispose();
            }
        };
        return c;
    }

    static String clip(FontMetrics fm, String s, int max) {
        if (fm.stringWidth(s) <= max) return s;
        while (s.length() > 1 && fm.stringWidth(s + "\u2026") > max) s = s.substring(0, s.length() - 1);
        return s + "\u2026";
    }
}
