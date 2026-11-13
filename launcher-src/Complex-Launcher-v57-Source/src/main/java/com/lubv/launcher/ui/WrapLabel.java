package com.lubv.launcher.ui;

import javax.swing.JLabel;

/**
 * Panel genisligine gore satir atlayan HTML etiket.
 *
 * ONCEDEN uzun aciklamalar sabit {@code <br>} ile yaziliyordu; panel daraldiginda
 * yazinin sagi kesiliyor ve ekranda sonuna "..." eklenmis yarim cumleler
 * gorunuyordu. Bu etiket metni kendi genisligine gore sarar.
 */
final class WrapLabel extends JLabel {
    private String rawText = "";
    private int lastWrapWidth = -1;

    WrapLabel(String text) {
        this.setText(text == null ? "" : text);
    }

    /** Durum yazisi sonradan atandiginda da sarilmis halde kalsin. */
    @Override
    public void setText(String text) {
        this.rawText = text == null ? "" : text;
        this.lastWrapWidth = -1;
        this.applyWrap();
    }

    private void applyWrap() {
        int width = this.getWidth();
        String next = html(this.rawText, width >= 40 ? Math.max(40, width - 6) : 0);
        if (!next.equals(super.getText())) {
            super.setText(next);
        }
        this.lastWrapWidth = width;
    }

    private static String html(String text, int width) {
        String escaped = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        if (width < 40) {
            return "<html>" + escaped + "</html>";
        }
        return "<html><body style='width:" + width + "px'>" + escaped + "</body></html>";
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        if (w != this.lastWrapWidth) {
            this.applyWrap();
        }
    }
}
