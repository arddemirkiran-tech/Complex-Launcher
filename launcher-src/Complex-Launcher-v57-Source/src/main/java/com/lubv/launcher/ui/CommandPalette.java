package com.lubv.launcher.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Ctrl+K komut paleti: sayfalara, instance'lara ve eylemlere hizli gecis. */
final class CommandPalette {
    static final class Entry {
        final String title, hint;
        final Icons.Kind icon;
        final Runnable run;
        Entry(String title, String hint, Icons.Kind icon, Runnable run) {
            this.title = title; this.hint = hint; this.icon = icon; this.run = run;
        }
    }

    private CommandPalette() {}

    static void show(Window owner, List<Entry> all, String placeholder) {
        final JDialog d = new JDialog(owner);
        d.setUndecorated(true);
        d.setAlwaysOnTop(true);
        final DefaultListModel<Entry> model = new DefaultListModel<>();
        for (Entry e : all) model.addElement(e);
        final JList<Entry> list = new JList<>(model);
        list.setFixedCellHeight(46);
        list.setBackground(Theme.BG_ELEVATED);
        list.setSelectedIndex(0);
        list.setCellRenderer(new ListCellRenderer<Entry>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Entry> l, Entry e, int i, boolean sel, boolean foc) {
                return new JComponent() {
                    @Override protected void paintComponent(Graphics g0) {
                        Graphics2D g = (Graphics2D) g0.create();
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        int w = getWidth(), h = getHeight();
                        g.setColor(Theme.BG_ELEVATED);
                        g.fillRect(0, 0, w, h);
                        if (sel) {
                            Color a = Theme.ACCENT;
                            g.setColor(new Color(a.getRed(), a.getGreen(), a.getBlue(), 46));
                            g.fillRoundRect(8, 3, w - 16, h - 6, 12, 12);
                        }
                        Icons.paint(g, e.icon, 22, (h - 20) / 2f, 20, sel ? Theme.ACCENT_BRIGHT : Theme.TEXT_SECONDARY);
                        g.setFont(Theme.uiFont(sel ? 1 : 0, 14.0f));
                        g.setColor(Theme.TEXT_PRIMARY);
                        FontMetrics fm = g.getFontMetrics();
                        g.drawString(e.title, 54, (h - fm.getHeight()) / 2 + fm.getAscent());
                        if (e.hint != null && !e.hint.isEmpty()) {
                            g.setFont(Theme.uiFont(0, 11.0f));
                            g.setColor(Theme.TEXT_MUTED);
                            FontMetrics hf = g.getFontMetrics();
                            g.drawString(e.hint, w - hf.stringWidth(e.hint) - 22, (h - hf.getHeight()) / 2 + hf.getAscent());
                        }
                        g.dispose();
                    }
                };
            }
        });
        final JTextField field = new JTextField();
        field.setFont(Theme.uiFont(0, 16.0f));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("JTextField.showClearButton", Boolean.FALSE);
        field.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        field.setBackground(Theme.BG_ELEVATED);
        field.setForeground(Theme.TEXT_PRIMARY);
        field.setCaretColor(Theme.ACCENT_BRIGHT);
        field.getDocument().addDocumentListener(new DocumentListener() {
            void refilter() {
                String q = field.getText().trim().toLowerCase();
                model.clear();
                for (Entry e : all) {
                    if (q.isEmpty() || e.title.toLowerCase().contains(q) || (e.hint != null && e.hint.toLowerCase().contains(q))) model.addElement(e);
                }
                if (!model.isEmpty()) list.setSelectedIndex(0);
            }
            @Override public void insertUpdate(DocumentEvent e) { refilter(); }
            @Override public void removeUpdate(DocumentEvent e) { refilter(); }
            @Override public void changedUpdate(DocumentEvent e) { refilter(); }
        });
        final Runnable runSelected = () -> {
            Entry e = list.getSelectedValue();
            d.dispose();
            if (e != null) e.run.run();
        };
        field.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent k) {
                int i = list.getSelectedIndex(), n = model.getSize();
                if (k.getKeyCode() == KeyEvent.VK_ESCAPE) d.dispose();
                else if (k.getKeyCode() == KeyEvent.VK_ENTER) runSelected.run();
                else if (k.getKeyCode() == KeyEvent.VK_DOWN && n > 0) { list.setSelectedIndex((i + 1) % n); list.ensureIndexIsVisible(list.getSelectedIndex()); }
                else if (k.getKeyCode() == KeyEvent.VK_UP && n > 0) { list.setSelectedIndex((i - 1 + n) % n); list.ensureIndexIsVisible(list.getSelectedIndex()); }
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int i = list.locationToIndex(e.getPoint());
                if (i >= 0) { list.setSelectedIndex(i); runSelected.run(); }
            }
        });
        JScrollPane sp = UiFx.cleanScroll(list);
        sp.setPreferredSize(new Dimension(580, 330));
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.BG_ELEVATED);
        top.add(field, BorderLayout.CENTER);
        JPanel sep = new JPanel();
        sep.setBackground(Theme.BG_BORDER);
        sep.setPreferredSize(new Dimension(0, 1));
        top.add(sep, BorderLayout.SOUTH);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_ELEVATED);
        root.setBorder(BorderFactory.createLineBorder(Theme.ACCENT_DARK, 1));
        root.add(top, BorderLayout.NORTH);
        root.add(sp, BorderLayout.CENTER);
        d.setContentPane(root);
        d.pack();
        Point p = owner.getLocationOnScreen();
        d.setLocation(p.x + (owner.getWidth() - d.getWidth()) / 2, p.y + Math.max(40, owner.getHeight() / 6));
        d.addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent e) { d.dispose(); }
        });
        d.setVisible(true);
        field.requestFocusInWindow();
    }
}
