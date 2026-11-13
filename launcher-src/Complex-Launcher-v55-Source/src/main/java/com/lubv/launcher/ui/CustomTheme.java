package com.lubv.launcher.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lubv.launcher.core.Paths;
import javax.swing.JLabel;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.Random;

/**
 * Kullanici tarafindan olusturulan ozel tema (Tema Oluşturucu) - v5.
 *
 * Turetme artik ORIJINAL v2 merdiveninin BIREBIR kendisidir: sabit
 * paletlerin (Nebula, Ember, Ocean...) degerlerinden tersine
 * cozulmus oranlarla calisir:
 *   yuzey hue = vurgu hue'su (karakter), doygunluk = taban * 0.68
 *   yuzey  = taban parlaklik + 0.055
 *   yuksek = taban parlaklik + 0.110
 *   kenar  = taban parlaklik + 0.210
 *   kart   = yuksek + ~0.045
 *   baslik = taban - 0.012
 * Boylece Oluşturucu temalari sabit temalarla AYNI zenginlikte olur.
 *
 * Kullanicinin elle duzenledigi alanlar (userEdited) turetme
 * sirasinda korunur; migrate ile eski/bayat degerler temizlenir.
 */
public class CustomTheme {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** 5 = orijinal merdiven matematigi. Eski surumler otomatik gecirilir. */
    public int version = 1;
    public String name = "My Theme";
    public boolean light = false;

    // --- Cekirdek renkler (kullanicinin sectigi) ---
    public String accent = "#8B5CF6";
    public String bgBase = "#0A0616";

    // --- Turetilmis/elle duzenlenen alanlar ---
    public String accentBright = "";
    public String accentDark = "";
    public String bgSurface = "";
    public String bgElevated = "";
    public String bgBorder = "";
    public String titleBar = "";
    public String textPrimary = "";
    public String textSecondary = "";
    public String textMuted = "";
    public String cardGradientTop = "";
    public String cardGradientBot = "";

    /** Kullanicinin Oluşturucu'da GERCEKTEN elle duzenledigi alanlar;
     *  turetme bunlara dokunmaz. */
    public java.util.List<String> userEdited = new java.util.ArrayList<>();

    // ---- V29.6 UI sekli & tipografi (temanin parcasi, temayla kaydedilir) ----
    /** Köşe yuvarlaklığı: 0=kare, 6=hafif, 12=yuvarlak, 22=pill. */
    public int uiArc = 10;
    /** Odak halkasi kalinligi (px). FlatLaf 'Component.focusWidth'. */
    public int focusWidth = 1;
    /** Yazı tipi ailesi: Java mantıksal veya kurulu sistem fontu. null=öntanımlı. */
    public String fontFamily = null;
    /** Temel yazı boyutu (px). UiFx/Label/Button boyutlari bununla ölçeklenir. */
    public int fontSize = 12;
    /** Başlık/etiket kalınlığı: 0=normal, 1=bold. */
    public int headingWeight = 1;
    /** Düğme metni kalınlığı: 0=normal, 1=bold. */
    public int buttonWeight = 0;

    public static final String[] FONT_CHOICES = {
        null,
        "Segoe UI", "Inter", "Roboto", "Poppins", "Montserrat", "Consolas",
        "Cascadia Code", "JetBrains Mono", "Verdana", "Tahoma", "Arial",
        "Trebuchet MS", "Calibri", "Georgia", "Dialog", "DialogInput",
        "Monospaced", "SansSerif", "Serif"
    };

    public static String defaultFontFamily() {
        return new JLabel().getFont().getFamily();
    }

    public void markEdited(String field) {
        if (userEdited == null) userEdited = new java.util.ArrayList<>();
        if (!userEdited.contains(field)) userEdited.add(field);
    }

    public CustomTheme() {
    }

    /* ---------------- Renk erisimi ---------------- */

    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static Color parse(String hex, Color fallback) {
        try {
            if (hex == null || hex.isBlank()) return fallback;
            String h = hex.trim();
            if (!h.startsWith("#")) h = "#" + h;
            if (h.length() == 7) return Color.decode(h);
            if (h.length() == 4) {
                return Color.decode("#" + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2) + h.charAt(3) + h.charAt(3));
            }
            return fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    public Color accentColor() { return parse(accent, new Color(139, 92, 246)); }
    public Color bgBaseColor() { return parse(bgBase, new Color(10, 6, 22)); }

    /** Turetilmis alanlar bos ise (yeni taslak) bir kez turetir. */
    public void ensureDerived() {
        if (userEdited == null) userEdited = new java.util.ArrayList<>();
        if (accentBright == null || accentBright.isBlank() || bgSurface == null || bgSurface.isBlank()) {
            derive();
        }
    }

    public Color accentBrightColor()   { ensureDerived(); return parse(accentBright, accentColor()); }
    public Color accentDarkColor()     { ensureDerived(); return parse(accentDark, accentColor().darker()); }
    public Color bgSurfaceColor()      { ensureDerived(); return parse(bgSurface, bgBaseColor()); }
    public Color bgElevatedColor()     { ensureDerived(); return parse(bgElevated, bgSurfaceColor()); }
    public Color bgBorderColor()       { ensureDerived(); return parse(bgBorder, bgElevatedColor()); }
    public Color titleBarColor()       { ensureDerived(); return parse(titleBar, accentDarkColor()); }
    public Color textPrimaryColor()    { ensureDerived(); return parse(textPrimary, Color.WHITE); }
    public Color textSecondaryColor()  { ensureDerived(); return parse(textSecondary, textPrimaryColor()); }
    public Color textMutedColor()      { ensureDerived(); return parse(textMuted, textSecondaryColor()); }
    public Color cardTopColor()        { ensureDerived(); return parse(cardGradientTop, bgElevatedColor()); }
    public Color cardBotColor()        { ensureDerived(); return parse(cardGradientBot, bgSurfaceColor()); }

    /* ---------------- Turetme: ORIJINAL merdiven ---------------- */

    public void derive() {
        if (userEdited == null) userEdited = new java.util.ArrayList<>();
        // Elle duzenlenen alanlari koru
        String kAccentBright  = userEdited.contains("accentBright")    ? accentBright    : null;
        String kAccentDark    = userEdited.contains("accentDark")      ? accentDark      : null;
        String kBgSurface     = userEdited.contains("bgSurface")       ? bgSurface       : null;
        String kBgElevated    = userEdited.contains("bgElevated")      ? bgElevated      : null;
        String kBgBorder      = userEdited.contains("bgBorder")        ? bgBorder        : null;
        String kTitleBar      = userEdited.contains("titleBar")        ? titleBar        : null;
        String kTextPrimary   = userEdited.contains("textPrimary")     ? textPrimary     : null;
        String kTextSecondary = userEdited.contains("textSecondary")   ? textSecondary   : null;
        String kTextMuted     = userEdited.contains("textMuted")       ? textMuted       : null;
        String kCardTop       = userEdited.contains("cardGradientTop") ? cardGradientTop : null;
        String kCardBot       = userEdited.contains("cardGradientBot") ? cardGradientBot : null;

        Color a = accentColor();
        Color base = bgBaseColor();
        boolean isLight = light;

        float[] ah = new float[3];
        Color.RGBtoHSB(a.getRed(), a.getGreen(), a.getBlue(), ah);
        float hue = ah[0], asat = ah[1], abri = ah[2];
        float[] bh = new float[3];
        Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), bh);
        float baseHue = bh[0], baseSat = bh[1], baseBri = bh[2];

        // Vurgu: oldugu gibi kullan (kullanici secti), parlak/koyu turet.
        accentBright = toHex(Color.getHSBColor(hue, Math.max(0f, asat - 0.15f), Math.min(1f, abri + 0.04f)));
        accentDark   = toHex(Color.getHSBColor(hue, Math.min(1f, asat + 0.15f), Math.max(0f, abri - 0.25f)));

        // ORIJINAL merdiven: yuzeyler vurgu hue'sunu tasir (karakter),
        // doygunluk tabanin 0.68'i (Nebula birebir), parlaklik +0.055 merdiven.
        if (isLight) {
            float sSat = Math.max(0.04f, baseSat * 0.68f);
            bgSurface = toHex(Color.getHSBColor(hue, sSat, Math.max(0.965f, baseBri - 0.010f)));
            bgElevated = toHex(Color.getHSBColor(hue, sSat, Math.max(0.925f, baseBri - 0.030f)));
            bgBorder = toHex(Color.getHSBColor(hue, Math.min(1f, sSat * 1.4f), Math.max(0.72f, baseBri - 0.140f)));
            titleBar = accentDark;
            textPrimary = toHex(Color.getHSBColor(hue, 0.40f, 0.14f));
            textSecondary = toHex(Color.getHSBColor(hue, 0.30f, 0.34f));
            textMuted = toHex(Color.getHSBColor(hue, 0.22f, 0.55f));
            cardGradientTop = toHex(Color.getHSBColor(hue, sSat, 1f));
            cardGradientBot = bgSurface;
        } else {
            float sSat = Math.max(0.10f, baseSat * 0.68f);
            bgSurface = toHex(Color.getHSBColor(hue, sSat, Math.min(1f, baseBri + 0.055f)));
            bgElevated = toHex(Color.getHSBColor(hue, sSat, Math.min(1f, baseBri + 0.110f)));
            bgBorder = toHex(Color.getHSBColor(hue, Math.min(1f, sSat * 1.4f), Math.min(1f, baseBri + 0.210f)));
            titleBar = toHex(Color.getHSBColor(hue, sSat, Math.max(0f, baseBri - 0.012f)));
            textPrimary = toHex(Color.getHSBColor(hue, 0.10f, 0.97f));
            textSecondary = toHex(Color.getHSBColor(hue, 0.26f, 0.72f));
            textMuted = toHex(Color.getHSBColor(hue, 0.30f, 0.48f));
            cardGradientTop = toHex(Color.getHSBColor(hue, Math.min(1f, sSat * 1.2f), Math.min(1f, baseBri + 0.150f)));
            cardGradientBot = bgSurface;
        }

        // Korumali (kullanici duzenlemesi) alanlari geri yaz
        if (kAccentBright  != null) accentBright    = kAccentBright;
        if (kAccentDark    != null) accentDark      = kAccentDark;
        if (kBgSurface     != null) bgSurface       = kBgSurface;
        if (kBgElevated    != null) bgElevated      = kBgElevated;
        if (kBgBorder      != null) bgBorder        = kBgBorder;
        if (kTitleBar      != null) titleBar        = kTitleBar;
        if (kTextPrimary   != null) textPrimary     = kTextPrimary;
        if (kTextSecondary != null) textSecondary   = kTextSecondary;
        if (kTextMuted     != null) textMuted       = kTextMuted;
        if (kCardTop       != null) cardGradientTop = kCardTop;
        if (kCardBot       != null) cardGradientBot = kCardBot;
    }

    /** Cekirdek renk degistiginde cagrilir: yuzeyler yeniden turetilir. */
    public void coreChanged() {
        derive();
    }

    /** Arka plan parlakligina gore light/dark modunu otomatik secer. */
    public void autoDetectLight() {
        Color base = bgBaseColor();
        this.light = 0.299 * base.getRed() + 0.587 * base.getGreen() + 0.114 * base.getBlue() > 128.0;
    }

    /* ---------------- Kalicilik (json) ---------------- */

    public static File file() {
        return new File(Paths.GAME_DIR, "custom_theme.json");
    }

    public boolean save() {
        this.version = 6;
        this.ensureDerived();
        try {
            Paths.GAME_DIR.mkdirs();
            try (FileWriter w = new FileWriter(file())) {
                GSON.toJson(this, w);
            }
            return true;
        } catch (Exception e) {
            System.err.println("[Theme] Ozel tema kaydedilemedi: " + e.getMessage());
            return false;
        }
    }

    /** Kayitli ozel temayi yukler; eski formatlari otomatik gecirir. */
    public static CustomTheme load() {
        File f = file();
        if (!f.exists()) return new CustomTheme();
        try (Reader r = new FileReader(f)) {
            CustomTheme t = GSON.fromJson(r, CustomTheme.class);
            return t != null ? migrate(t) : new CustomTheme();
        } catch (Exception e) {
            System.err.println("[Theme] Ozel tema yuklenemedi: " + e.getMessage());
            return new CustomTheme();
        }
    }

    /**
     * v1-v4 kayitlari: elle duzenlenmemis tum yuzeyler atilir ve
     * ORIJINAL merdivenle yeniden turetilir. Bayat deger sizmasi yok.
     */
    public static CustomTheme migrate(CustomTheme t) {
        if (t == null) return new CustomTheme();
        if (t.userEdited == null) t.userEdited = new java.util.ArrayList<>();
        // v6: UI sekil + tipografi alanlari eklendi -> mantikli sinirlara cek.
        if (t.version < 6) {
            if (t.uiArc < 0 || t.uiArc > 30) t.uiArc = 10;
            if (t.focusWidth < 0 || t.focusWidth > 4) t.focusWidth = 1;
            if (t.fontSize < 10 || t.fontSize > 20) t.fontSize = 12;
            if (t.headingWeight != 0 && t.headingWeight != 1) t.headingWeight = 1;
            if (t.buttonWeight != 0 && t.buttonWeight != 1) t.buttonWeight = 0;
            if (t.fontFamily != null && t.fontFamily.isBlank()) t.fontFamily = null;
            t.version = 6;
        }
        if (t.version < 5) {
            try {
                t.autoDetectLight();
                if (!t.userEdited.contains("accentBright"))    t.accentBright = "";
                if (!t.userEdited.contains("accentDark"))      t.accentDark = "";
                if (!t.userEdited.contains("bgSurface"))       t.bgSurface = "";
                if (!t.userEdited.contains("bgElevated"))      t.bgElevated = "";
                if (!t.userEdited.contains("bgBorder"))        t.bgBorder = "";
                if (!t.userEdited.contains("titleBar"))        t.titleBar = "";
                if (!t.userEdited.contains("textPrimary"))     t.textPrimary = "";
                if (!t.userEdited.contains("textSecondary"))   t.textSecondary = "";
                if (!t.userEdited.contains("textMuted"))       t.textMuted = "";
                if (!t.userEdited.contains("cardGradientTop")) t.cardGradientTop = "";
                if (!t.userEdited.contains("cardGradientBot")) t.cardGradientBot = "";
                t.derive();
                t.version = 5;
                t.save();
            } catch (Exception ignored) {
            }
        }
        return t;
    }

    /** Temayi istenilen dosyaya disa aktarir (paylasim icin). */
    public boolean saveTo(File out) {
        this.version = 6;
        this.ensureDerived();
        try (FileWriter w = new FileWriter(out)) {
            GSON.toJson(this, w);
            return true;
        } catch (Exception e) {
            System.err.println("[Theme] Tema disa aktarilamadi: " + e.getMessage());
            return false;
        }
    }

    /** Disaridan .json tema dosyasi ice aktarir; bozuksa null dondurur. */
    public static CustomTheme loadFrom(File in) {
        try (Reader r = new FileReader(in)) {
            CustomTheme t = GSON.fromJson(r, CustomTheme.class);
            if (t == null || t.accent == null || t.bgBase == null) return null;
            return migrate(t);
        } catch (Exception e) {
            return null;
        }
    }

    /** O an uygulandigi temanin cekirdegini yeni bir taslak olarak alir. */
    public static CustomTheme snapshotFromCurrent() {
        CustomTheme t = new CustomTheme();
        t.version = 6;
        t.accent = toHex(Theme.ACCENT);
        t.bgBase = toHex(Theme.BG_BASE);
        t.light = 0.299 * Theme.BG_BASE.getRed() + 0.587 * Theme.BG_BASE.getGreen() + 0.114 * Theme.BG_BASE.getBlue() > 128.0;
        // Sekil/tipografi: aktif temaninkini tasla (ozel temadan gecis yapilirsa
        // kullanicinin secimleri kaybolmaz; presetten geciste on tanimlilar).
        t.uiArc = Theme.UI_ARC;
        t.focusWidth = Theme.FOCUS_WIDTH;
        t.fontSize = Theme.FONT_SIZE;
        t.fontFamily = Theme.FONT_FAMILY;
        t.headingWeight = Theme.HEADING_WEIGHT;
        t.buttonWeight = Theme.BUTTON_WEIGHT;
        t.derive();
        return t;
    }

    /* ---------------- Rastgele uyumlu tema ureteci ---------------- */

    /** Renk teorisine dayali rastgele tema; cikti SADECE cekirdektir,
     *  yuzeyler orijinal merdivenle turetilir. */
    public static CustomTheme random() {
        Random rnd = new Random();
        float hue = rnd.nextFloat();
        float accentSat = 0.60f + rnd.nextFloat() * 0.25f;
        float accentBri = 0.58f + rnd.nextFloat() * 0.14f;
        float bgSat = 0.30f + rnd.nextFloat() * 0.35f;

        CustomTheme t = new CustomTheme();
        t.version = 6;
        t.name = "Random " + (1 + rnd.nextInt(99));
        t.light = false;
        t.accent = toHex(Color.getHSBColor(hue, accentSat, accentBri));
        t.bgBase = toHex(Color.getHSBColor(hue, bgSat, 0.035f + rnd.nextFloat() * 0.02f));
        // Rastgele sekil + tipografi: tamamen farkli karakterde temalar.
        int[] arcs = {0, 4, 8, 10, 14, 18, 22};
        t.uiArc = arcs[rnd.nextInt(arcs.length)];
        t.focusWidth = rnd.nextInt(3);            // 0..2
        t.fontSize = 11 + rnd.nextInt(4);         // 11..14
        t.headingWeight = rnd.nextInt(2);
        t.buttonWeight = rnd.nextInt(2);
        String[] fonts = {null, "Segoe UI", "Verdana", "Tahoma", "Consolas", "Dialog", "SansSerif", "Trebuchet MS"};
        t.fontFamily = fonts[rnd.nextInt(fonts.length)];
        t.derive();
        return t;
    }
}
