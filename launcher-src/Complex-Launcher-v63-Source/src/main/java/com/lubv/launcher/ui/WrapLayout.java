package com.lubv.launcher.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/** FlowLayout'un satir atlayan (wrap) surumu; JScrollPane icinde genisligi izleyen panellerle kullanilir. */
final class WrapLayout extends FlowLayout {
    private int lastWidth = -1;

    WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

    /**
     * Sarma sonrasi yukseklik degisirse ust panelden bir kez daha yerlesim
     * istenir; yoksa tek satirlik yukseklik ayrilip alt satirdaki ogeler
     * kesiliyordu (metinler "..." ile kirpiliyordu).
     */
    @Override
    public void layoutContainer(Container target) {
        super.layoutContainer(target);
        if (target.getWidth() != this.lastWidth) {
            this.lastWidth = target.getWidth();
            target.revalidate();
        }
    }

    @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }

    @Override public Dimension minimumLayoutSize(Container target) {
        Dimension d = layoutSize(target, false);
        d.width -= getHgap() + 1;
        return d;
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            Container scroll = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (scroll != null && scroll.getWidth() > 0) {
                targetWidth = ((JScrollPane) scroll).getViewport().getWidth();
            }
            if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
            int hgap = getHgap(), vgap = getVgap();
            Insets in = target.getInsets();
            int maxWidth = targetWidth - (in.left + in.right + hgap * 2);
            Dimension dim = new Dimension(0, 0);
            int rowW = 0, rowH = 0;
            for (int i = 0; i < target.getComponentCount(); i++) {
                Component m = target.getComponent(i);
                if (!m.isVisible()) continue;
                Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                if (rowW + d.width > maxWidth && rowW > 0) {
                    dim.width = Math.max(dim.width, rowW);
                    dim.height += rowH + vgap;
                    rowW = 0; rowH = 0;
                }
                if (rowW != 0) rowW += hgap;
                rowW += d.width;
                rowH = Math.max(rowH, d.height);
            }
            dim.width = Math.max(dim.width, rowW);
            dim.height += rowH;
            dim.width += in.left + in.right + hgap * 2;
            dim.height += in.top + in.bottom + vgap * 2;
            return dim;
        }
    }
}
