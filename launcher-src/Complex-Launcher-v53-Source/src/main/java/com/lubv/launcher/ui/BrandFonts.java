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
    private static final String[] FILES = new String[]{
        "BricolageGrotesque-latin.ttf",
        "BricolageGrotesque-latin-ext.ttf",
        "SpaceGrotesk-500.ttf",
        "SpaceGrotesk-500-ext.ttf",
        "SpaceGrotesk-700.ttf",
        "SpaceGrotesk-700-ext.ttf"
    };

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
