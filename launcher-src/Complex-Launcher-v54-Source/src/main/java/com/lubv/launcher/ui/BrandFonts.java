/*
 * v49 TEST BUILD: the launcher uses the same typefaces as the website.
 *
 * Bricolage Grotesque (display) and Space Grotesk (text) ship as TTF inside
 * the jar, so the look is identical on Windows and Linux without installing
 * anything. If registration fails for any reason the caller falls back to the
 * platform UI font - this class never throws.
 */
package com.lubv.launcher.ui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class BrandFonts {
    private static final String DIR = "/com/lubv/launcher/ui/fonts/";
    /* v53: the old bundle shipped the Google-Fonts *subsets* (latin and
       latin-ext) as separate files that shared one family name. Java picked
       the latin face, which has no ş/ğ/İ/ı glyphs, so Turkish text was drawn with the
       .notdef glyph - the "?" users saw. Each file below is the two subsets
       merged into a single face with full Latin + Latin Extended coverage. */
    private static final String[] FILES = new String[]{
        "SpaceGrotesk-Medium.ttf",
        "BricolageGrotesque-ExtraBold.ttf"
    };
    /** Characters every bundled face must be able to draw. */
    private static final String REQUIRED =
        "ABCabc0123.,:!?-_/()[]'\"#%&*+=<>|~@ "
        + "\u00c7\u011e\u0130\u00d6\u015e\u00dc\u00e7\u011f\u0131\u00f6\u015f\u00fc";

    private static String body;
    private static String display;
    private static boolean loaded;

    private BrandFonts() {
    }

    /** Registers the bundled faces once; safe to call from any thread. */
    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        List<Font> registered = new ArrayList<>();
        for (String name : FILES) {
            try (InputStream in = BrandFonts.class.getResourceAsStream(DIR + name)) {
                if (in == null) {
                    continue;
                }
                Font font = Font.createFont(Font.TRUETYPE_FONT, in);
                if (font.canDisplayUpTo(REQUIRED) != -1) {
                    continue; // a face without Turkish glyphs must never win
                }
                ge.registerFont(font);
                registered.add(font);
            }
            catch (Throwable ignored) {
                // a broken or unsupported face must not stop the launcher
            }
        }
        for (Font font : registered) {
            String family = font.getFamily();
            if (family == null) {
                continue;
            }
            String lower = family.toLowerCase();
            if (display == null && lower.contains("bricolage")) {
                display = family;
            }
            if (body == null && lower.contains("space grotesk")) {
                body = family;
            }
        }
        // A family name can still resolve to a different installed face on
        // some systems; only accept it when it really covers our characters.
        if (display != null && new Font(display, Font.PLAIN, 12).canDisplayUpTo(REQUIRED) != -1) {
            display = null;
        }
        if (body != null && new Font(body, Font.PLAIN, 12).canDisplayUpTo(REQUIRED) != -1) {
            body = null;
        }
    }

    /** Space Grotesk, or null when the bundle could not be registered. */
    public static String body() {
        return body;
    }

    /** Bricolage Grotesque, or null when the bundle could not be registered. */
    public static String display() {
        return display;
    }

    public static boolean isLoaded() {
        return body != null || display != null;
    }
}
