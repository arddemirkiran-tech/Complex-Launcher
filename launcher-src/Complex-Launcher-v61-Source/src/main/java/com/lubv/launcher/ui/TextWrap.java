package com.lubv.launcher.ui;

/**
 * Uzun dosya yollarini okunabilir sekilde sarmak icin kucuk yardimci.
 *
 * <p>JTextArea {@code wrapStyleWord(true)} iken yalnizca bosluklardan boler;
 * "C:\Users\...\instances\survival\shaderpacks" gibi bosluksuz bir yol
 * kelime ortasindan kirilip "shaderpac / ks" gibi bozuk gorunuyordu. Yol
 * ayiricilarindan sonra sifir genislikli bosluk (U+200B) eklenince satir
 * kaydirma tam olarak klasor sinirlarinda olur.
 */
final class TextWrap {
    private TextWrap() {
    }

    static String breakPath(String path) {
        if (path == null || path.isEmpty()) {
            return path == null ? "" : path;
        }
        StringBuilder out = new StringBuilder(path.length() + 8);
        for (int i = 0; i < path.length(); ++i) {
            char c = path.charAt(i);
            out.append(c);
            if ((c == '\\' || c == '/') && i < path.length() - 1) {
                out.append('\u200B');
            }
        }
        return out.toString();
    }
}
