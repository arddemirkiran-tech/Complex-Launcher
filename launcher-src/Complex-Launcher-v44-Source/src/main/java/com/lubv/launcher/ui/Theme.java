package com.lubv.launcher.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.UIManager;

/*
 * EN ESKI tema sistemi - ORIJINAL haliyle geri getirildi.
 *  - 11 preset, orijinal sabit renkler, dev switch yapisi (birebir).
 *  - UIManager degerleri orijinalle ayni (arc 10, focusWidth 2 vb.).
 *  - Eklenen tek seyler (EKLENTI olarak isaretli): "Custom" temasi
 *    yonlendirmesi (Tema Olusturucu icin). Aksi halde davranis
 *    orijinalin aynisidir.
 */
public final class Theme {
    /* EKLENTI: Tema Olusturucu temasi aktif mi? */
    public static boolean activeIsCustom = false;

    public static Color ACCENT;
    public static Color ACCENT_BRIGHT;
    public static Color ACCENT_DARK;
    public static Color ACCENT_MUTED;
    public static Color ACCENT_GLOW;
    public static Color BG_BASE;
    public static Color BG_SURFACE;
    public static Color BG_ELEVATED;
    public static Color BG_BORDER;
    public static Color BG_GLASS;
    public static Color TEXT_PRIMARY;
    public static Color TEXT_SECONDARY;
    public static Color TEXT_MUTED;
    public static Color TEXT_INVERSE;
    public static Color GREEN;
    public static Color GREEN_DARK;
    public static Color RED;
    public static Color RED_DARK;
    public static Color YELLOW;
    public static Color ORANGE;
    public static Color CYAN;
    public static Color CARD_GRADIENT_TOP;
    public static Color CARD_GRADIENT_BOT;
    public static Color TITLE_BAR;

    // ---- V29.6 UI sekli & tipografi (temanin parcasi) ----
    /** Köşe yuvarlaklığı: 0=kare, 10=öntanımlı, 22=pill. FlatLaf arc + UiFx çizimleri. */
    public static int UI_ARC = 10;
    /** Odak halkası kalınlığı (px). */
    public static int FOCUS_WIDTH = 1;
    /** Yazı tipi ailesi (null = JVM öntanımlısı). */
    public static String FONT_FAMILY = null;
    /** Temel yazı boyutu (px). */
    public static int FONT_SIZE = 12;
    /** Başlık/etiket kalınlığı: 0=normal, 1=bold. */
    public static int HEADING_WEIGHT = 1;
    /** Düğme metni kalınlığı: 0=normal, 1=bold. */
    public static int BUTTON_WEIGHT = 0;

    /**
     * UiFx bileşenleri için tema yazı tipi: her çağrı yeni bir Font döndürür
     * (Font nesneleri değişmez, paylaşım sorunu yok). size/weight parametreleri
     * bileşenin kendi ölçeğidir; temel aile+boyut temadan gelir.
     */
    public static Font uiFont(int style, float size) {
        String fam = FONT_FAMILY != null && !FONT_FAMILY.isBlank() ? FONT_FAMILY : "Dialog";
        return new Font(fam, style, Math.round(FONT_SIZE + (size - 12.0f))).deriveFont(Math.max(8f, FONT_SIZE + (size - 12.0f)));
    }

    /** FlatLaf UIManager'a tipografi iter (şekil değerleri pushToUIManager'da). */
    private static void pushShapeAndTypography() {
        // Tüm FlatLaf bileşen fontları tek kaynaktan: aile + boyut temadan.
        String fam = FONT_FAMILY != null && !FONT_FAMILY.isBlank() ? FONT_FAMILY : "Dialog";
        java.awt.Font base = new java.awt.Font(fam, java.awt.Font.PLAIN, FONT_SIZE);
        UIManager.put("defaultFont", base);
        UIManager.put("Label.font", base);
        UIManager.put("Button.font", base.deriveFont(BUTTON_WEIGHT == 1 ? java.awt.Font.BOLD : java.awt.Font.PLAIN));
        UIManager.put("ToggleButton.font", base);
        UIManager.put("TextField.font", base);
        UIManager.put("TextArea.font", base);
        UIManager.put("ComboBox.font", base);
        UIManager.put("CheckBox.font", base);
        UIManager.put("RadioButton.font", base);
        UIManager.put("List.font", base);
        UIManager.put("Table.font", base);
        UIManager.put("TableHeader.font", base);
        UIManager.put("TabbedPane.font", base);
        UIManager.put("ScrollPane.font", base);
        UIManager.put("MenuBar.font", base);
        UIManager.put("Menu.font", base);
        UIManager.put("MenuItem.font", base);
        UIManager.put("PopupMenu.font", base);
        UIManager.put("Slider.font", base);
        UIManager.put("Spinner.font", base);
        UIManager.put("ToolBar.font", base);
        UIManager.put("ProgressBar.font", base);
        UIManager.put("ToolTip.font", base.deriveFont(Math.max(9f, FONT_SIZE - 1.0f)));
    }

    /** Preset temalara dönüşte tipografiyi JVM öntanımlılarına sıfırlar. */
    public static void resetTypography() {
        UI_ARC = 10;
        FOCUS_WIDTH = 1;
        FONT_FAMILY = null;
        FONT_SIZE = 12;
        HEADING_WEIGHT = 1;
        BUTTON_WEIGHT = 0;
    }

    private Theme() {
    }

    public static void apply(String string) {
        /* EKLENTI: Tema Olusturucu temasi kayitliyse yukle. */
        if ("Custom".equalsIgnoreCase(string)) {
            Theme.applyCustom(CustomTheme.load());
            return;
        }
        Theme.apply(Preset.fromName(string));
    }

    public static void apply(boolean bl) {
        /* EKLENTI: acik (light) modda kayitli ozel light tema varsa onu geri yukle. */
        if (bl) {
            try {
                CustomTheme custom = CustomTheme.load();
                if (custom.light && CustomTheme.file().exists()) {
                    Theme.applyCustom(custom);
                    return;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            Theme.apply(Preset.LIGHT);
            return;
        }
        Theme.apply(Preset.NEBULA);
    }

    public static void apply(Preset preset) {
        boolean bl;
        boolean bl2 = bl = preset == Preset.LIGHT;
        if (bl) {
            FlatLightLaf.setup();
        } else {
            FlatDarkLaf.setup();
        }
        switch (preset) {
            case NEBULA: {
                ACCENT = new Color(139, 92, 246);
                ACCENT_BRIGHT = new Color(167, 139, 250);
                ACCENT_DARK = new Color(91, 33, 182);
                ACCENT_MUTED = new Color(139, 92, 246, 40);
                ACCENT_GLOW = new Color(139, 92, 246, 90);
                BG_BASE = new Color(10, 6, 22);
                BG_SURFACE = new Color(18, 12, 38);
                BG_ELEVATED = new Color(28, 18, 56);
                BG_BORDER = new Color(55, 38, 95);
                BG_GLASS = new Color(25, 16, 50, 190);
                TITLE_BAR = new Color(8, 4, 18);
                TEXT_PRIMARY = new Color(241, 233, 254);
                TEXT_SECONDARY = new Color(167, 145, 210);
                TEXT_MUTED = new Color(90, 70, 130);
                TEXT_INVERSE = new Color(10, 6, 22);
                CARD_GRADIENT_TOP = new Color(32, 22, 64);
                CARD_GRADIENT_BOT = new Color(18, 12, 38);
                break;
            }
            case DUSK: {
                ACCENT = new Color(196, 77, 232);
                ACCENT_BRIGHT = new Color(220, 120, 255);
                ACCENT_DARK = new Color(130, 30, 175);
                ACCENT_MUTED = new Color(196, 77, 232, 40);
                ACCENT_GLOW = new Color(196, 77, 232, 90);
                BG_BASE = new Color(14, 8, 26);
                BG_SURFACE = new Color(22, 12, 40);
                BG_ELEVATED = new Color(34, 18, 58);
                BG_BORDER = new Color(70, 35, 100);
                BG_GLASS = new Color(28, 14, 48, 190);
                TITLE_BAR = new Color(10, 5, 20);
                TEXT_PRIMARY = new Color(248, 232, 255);
                TEXT_SECONDARY = new Color(190, 145, 220);
                TEXT_MUTED = new Color(105, 65, 140);
                TEXT_INVERSE = new Color(14, 8, 26);
                CARD_GRADIENT_TOP = new Color(40, 20, 65);
                CARD_GRADIENT_BOT = new Color(22, 12, 40);
                break;
            }
            case AURORA: {
                ACCENT = new Color(52, 211, 153);
                ACCENT_BRIGHT = new Color(110, 231, 183);
                ACCENT_DARK = new Color(16, 130, 90);
                ACCENT_MUTED = new Color(52, 211, 153, 40);
                ACCENT_GLOW = new Color(52, 211, 153, 90);
                BG_BASE = new Color(6, 14, 20);
                BG_SURFACE = new Color(10, 22, 30);
                BG_ELEVATED = new Color(15, 32, 44);
                BG_BORDER = new Color(28, 60, 70);
                BG_GLASS = new Color(10, 24, 34, 190);
                TITLE_BAR = new Color(4, 10, 16);
                TEXT_PRIMARY = new Color(220, 248, 240);
                TEXT_SECONDARY = new Color(110, 185, 160);
                TEXT_MUTED = new Color(50, 105, 90);
                TEXT_INVERSE = new Color(6, 14, 20);
                CARD_GRADIENT_TOP = new Color(18, 38, 52);
                CARD_GRADIENT_BOT = new Color(10, 22, 30);
                break;
            }
            case MIDNIGHT: {
                ACCENT = new Color(59, 130, 246);
                ACCENT_BRIGHT = new Color(96, 165, 250);
                ACCENT_DARK = new Color(29, 78, 216);
                ACCENT_MUTED = new Color(59, 130, 246, 40);
                ACCENT_GLOW = new Color(59, 130, 246, 90);
                BG_BASE = new Color(7, 9, 16);
                BG_SURFACE = new Color(12, 16, 26);
                BG_ELEVATED = new Color(18, 25, 40);
                BG_BORDER = new Color(35, 48, 70);
                BG_GLASS = new Color(14, 20, 34, 190);
                TITLE_BAR = new Color(4, 6, 12);
                TEXT_PRIMARY = new Color(230, 236, 248);
                TEXT_SECONDARY = new Color(120, 145, 180);
                TEXT_MUTED = new Color(60, 80, 110);
                TEXT_INVERSE = new Color(7, 9, 16);
                CARD_GRADIENT_TOP = new Color(22, 32, 48);
                CARD_GRADIENT_BOT = new Color(12, 16, 26);
                break;
            }
            case STORM: {
                ACCENT = new Color(100, 149, 237);
                ACCENT_BRIGHT = new Color(140, 185, 255);
                ACCENT_DARK = new Color(60, 95, 175);
                ACCENT_MUTED = new Color(100, 149, 237, 40);
                ACCENT_GLOW = new Color(100, 149, 237, 90);
                BG_BASE = new Color(12, 14, 18);
                BG_SURFACE = new Color(18, 21, 28);
                BG_ELEVATED = new Color(26, 30, 40);
                BG_BORDER = new Color(45, 52, 68);
                BG_GLASS = new Color(20, 24, 32, 190);
                TITLE_BAR = new Color(8, 10, 14);
                TEXT_PRIMARY = new Color(225, 230, 242);
                TEXT_SECONDARY = new Color(130, 142, 168);
                TEXT_MUTED = new Color(70, 80, 100);
                TEXT_INVERSE = new Color(12, 14, 18);
                CARD_GRADIENT_TOP = new Color(30, 35, 48);
                CARD_GRADIENT_BOT = new Color(18, 21, 28);
                break;
            }
            case FOREST: {
                ACCENT = new Color(34, 197, 94);
                ACCENT_BRIGHT = new Color(74, 222, 128);
                ACCENT_DARK = new Color(20, 110, 52);
                ACCENT_MUTED = new Color(34, 197, 94, 40);
                ACCENT_GLOW = new Color(34, 197, 94, 90);
                BG_BASE = new Color(6, 13, 9);
                BG_SURFACE = new Color(10, 20, 14);
                BG_ELEVATED = new Color(16, 30, 20);
                BG_BORDER = new Color(28, 56, 36);
                BG_GLASS = new Color(10, 22, 15, 190);
                TITLE_BAR = new Color(4, 9, 6);
                TEXT_PRIMARY = new Color(215, 240, 222);
                TEXT_SECONDARY = new Color(100, 165, 120);
                TEXT_MUTED = new Color(48, 95, 62);
                TEXT_INVERSE = new Color(6, 13, 9);
                CARD_GRADIENT_TOP = new Color(20, 36, 26);
                CARD_GRADIENT_BOT = new Color(10, 20, 14);
                break;
            }
            case CRIMSON: {
                ACCENT = new Color(220, 38, 38);
                ACCENT_BRIGHT = new Color(252, 100, 100);
                ACCENT_DARK = new Color(140, 16, 16);
                ACCENT_MUTED = new Color(220, 38, 38, 40);
                ACCENT_GLOW = new Color(220, 38, 38, 90);
                BG_BASE = new Color(14, 6, 6);
                BG_SURFACE = new Color(22, 10, 10);
                BG_ELEVATED = new Color(34, 15, 15);
                BG_BORDER = new Color(68, 28, 28);
                BG_GLASS = new Color(26, 11, 11, 190);
                TITLE_BAR = new Color(10, 4, 4);
                TEXT_PRIMARY = new Color(250, 222, 222);
                TEXT_SECONDARY = new Color(190, 110, 110);
                TEXT_MUTED = new Color(110, 55, 55);
                TEXT_INVERSE = new Color(14, 6, 6);
                CARD_GRADIENT_TOP = new Color(40, 18, 18);
                CARD_GRADIENT_BOT = new Color(22, 10, 10);
                break;
            }
            case EMBER: {
                ACCENT = new Color(249, 115, 22);
                ACCENT_BRIGHT = new Color(251, 160, 80);
                ACCENT_DARK = new Color(180, 68, 0);
                ACCENT_MUTED = new Color(249, 115, 22, 40);
                ACCENT_GLOW = new Color(249, 115, 22, 90);
                BG_BASE = new Color(14, 9, 4);
                BG_SURFACE = new Color(22, 14, 6);
                BG_ELEVATED = new Color(34, 20, 8);
                BG_BORDER = new Color(70, 38, 12);
                BG_GLASS = new Color(26, 15, 7, 190);
                TITLE_BAR = new Color(10, 6, 2);
                TEXT_PRIMARY = new Color(255, 238, 215);
                TEXT_SECONDARY = new Color(200, 145, 75);
                TEXT_MUTED = new Color(115, 75, 25);
                TEXT_INVERSE = new Color(14, 9, 4);
                CARD_GRADIENT_TOP = new Color(40, 24, 10);
                CARD_GRADIENT_BOT = new Color(22, 14, 6);
                break;
            }
            case OCEAN: {
                ACCENT = new Color(6, 182, 212);
                ACCENT_BRIGHT = new Color(34, 211, 238);
                ACCENT_DARK = new Color(8, 105, 140);
                ACCENT_MUTED = new Color(6, 182, 212, 40);
                ACCENT_GLOW = new Color(6, 182, 212, 90);
                BG_BASE = new Color(5, 11, 20);
                BG_SURFACE = new Color(8, 18, 32);
                BG_ELEVATED = new Color(12, 26, 46);
                BG_BORDER = new Color(22, 52, 78);
                BG_GLASS = new Color(8, 20, 36, 190);
                TITLE_BAR = new Color(3, 7, 14);
                TEXT_PRIMARY = new Color(220, 242, 252);
                TEXT_SECONDARY = new Color(100, 162, 192);
                TEXT_MUTED = new Color(42, 92, 118);
                TEXT_INVERSE = new Color(5, 11, 20);
                CARD_GRADIENT_TOP = new Color(14, 30, 52);
                CARD_GRADIENT_BOT = new Color(8, 18, 32);
                break;
            }
            case GOLDEN: {
                ACCENT = new Color(212, 170, 40);
                ACCENT_BRIGHT = new Color(250, 200, 80);
                ACCENT_DARK = new Color(140, 105, 10);
                ACCENT_MUTED = new Color(212, 170, 40, 40);
                ACCENT_GLOW = new Color(212, 170, 40, 90);
                BG_BASE = new Color(12, 9, 3);
                BG_SURFACE = new Color(20, 15, 5);
                BG_ELEVATED = new Color(30, 22, 8);
                BG_BORDER = new Color(65, 48, 14);
                BG_GLASS = new Color(22, 16, 6, 190);
                TITLE_BAR = new Color(8, 6, 2);
                TEXT_PRIMARY = new Color(255, 245, 210);
                TEXT_SECONDARY = new Color(195, 160, 80);
                TEXT_MUTED = new Color(110, 85, 25);
                TEXT_INVERSE = new Color(12, 9, 3);
                CARD_GRADIENT_TOP = new Color(36, 26, 10);
                CARD_GRADIENT_BOT = new Color(20, 15, 5);
                break;
            }
            // --- Minecraft temalari (v29) ---
            // Tum paletler ayni merdivene uyar: BG_SURFACE = base+0.055,
            // BG_ELEVATED = base+0.110, BG_BORDER = base+0.210 parlaklik.
            case GRASS_BLOCK: {
                ACCENT = new Color(85, 168, 74);
                ACCENT_BRIGHT = new Color(120, 205, 105);
                ACCENT_DARK = new Color(38, 110, 40);
                ACCENT_MUTED = new Color(85, 168, 74, 40);
                ACCENT_GLOW = new Color(85, 168, 74, 90);
                BG_BASE = new Color(8, 13, 7);
                BG_SURFACE = new Color(13, 21, 11);
                BG_ELEVATED = new Color(19, 31, 16);
                BG_BORDER = new Color(36, 58, 30);
                BG_GLASS = new Color(12, 20, 10, 190);
                TITLE_BAR = new Color(5, 9, 4);
                TEXT_PRIMARY = new Color(226, 242, 220);
                TEXT_SECONDARY = new Color(125, 170, 115);
                TEXT_MUTED = new Color(60, 95, 55);
                TEXT_INVERSE = new Color(8, 13, 7);
                CARD_GRADIENT_TOP = new Color(22, 36, 18);
                CARD_GRADIENT_BOT = new Color(13, 21, 11);
                break;
            }
            case CREEPER: {
                ACCENT = new Color(100, 190, 70);
                ACCENT_BRIGHT = new Color(150, 230, 120);
                ACCENT_DARK = new Color(50, 120, 35);
                ACCENT_MUTED = new Color(100, 190, 70, 40);
                ACCENT_GLOW = new Color(100, 190, 70, 90);
                BG_BASE = new Color(7, 12, 6);
                BG_SURFACE = new Color(12, 20, 9);
                BG_ELEVATED = new Color(17, 29, 13);
                BG_BORDER = new Color(32, 54, 24);
                BG_GLASS = new Color(11, 19, 8, 190);
                TITLE_BAR = new Color(4, 8, 3);
                TEXT_PRIMARY = new Color(228, 246, 215);
                TEXT_SECONDARY = new Color(130, 180, 110);
                TEXT_MUTED = new Color(62, 95, 50);
                TEXT_INVERSE = new Color(7, 12, 6);
                CARD_GRADIENT_TOP = new Color(21, 35, 15);
                CARD_GRADIENT_BOT = new Color(12, 20, 9);
                break;
            }
            case NETHER: {
                ACCENT = new Color(200, 60, 50);
                ACCENT_BRIGHT = new Color(240, 110, 90);
                ACCENT_DARK = new Color(130, 25, 22);
                ACCENT_MUTED = new Color(200, 60, 50, 40);
                ACCENT_GLOW = new Color(200, 60, 50, 90);
                BG_BASE = new Color(16, 7, 8);
                BG_SURFACE = new Color(25, 11, 13);
                BG_ELEVATED = new Color(37, 17, 19);
                BG_BORDER = new Color(68, 30, 32);
                BG_GLASS = new Color(23, 10, 12, 190);
                TITLE_BAR = new Color(11, 5, 5);
                TEXT_PRIMARY = new Color(250, 225, 222);
                TEXT_SECONDARY = new Color(190, 115, 110);
                TEXT_MUTED = new Color(110, 55, 55);
                TEXT_INVERSE = new Color(16, 7, 8);
                CARD_GRADIENT_TOP = new Color(42, 20, 22);
                CARD_GRADIENT_BOT = new Color(25, 11, 13);
                break;
            }
            case THE_END: {
                ACCENT = new Color(218, 205, 120);
                ACCENT_BRIGHT = new Color(240, 228, 170);
                ACCENT_DARK = new Color(140, 130, 60);
                ACCENT_MUTED = new Color(218, 205, 120, 40);
                ACCENT_GLOW = new Color(218, 205, 120, 90);
                BG_BASE = new Color(6, 6, 8);
                BG_SURFACE = new Color(11, 11, 14);
                BG_ELEVATED = new Color(16, 16, 20);
                BG_BORDER = new Color(30, 30, 38);
                BG_GLASS = new Color(9, 9, 12, 190);
                TITLE_BAR = new Color(4, 4, 5);
                TEXT_PRIMARY = new Color(240, 238, 225);
                TEXT_SECONDARY = new Color(160, 155, 130);
                TEXT_MUTED = new Color(80, 78, 65);
                TEXT_INVERSE = new Color(6, 6, 8);
                CARD_GRADIENT_TOP = new Color(20, 20, 25);
                CARD_GRADIENT_BOT = new Color(11, 11, 14);
                break;
            }
            case DIAMOND: {
                ACCENT = new Color(90, 220, 230);
                ACCENT_BRIGHT = new Color(140, 240, 248);
                ACCENT_DARK = new Color(30, 130, 150);
                ACCENT_MUTED = new Color(90, 220, 230, 40);
                ACCENT_GLOW = new Color(90, 220, 230, 90);
                BG_BASE = new Color(6, 12, 20);
                BG_SURFACE = new Color(10, 19, 31);
                BG_ELEVATED = new Color(15, 27, 43);
                BG_BORDER = new Color(26, 48, 70);
                BG_GLASS = new Color(8, 17, 28, 190);
                TITLE_BAR = new Color(3, 8, 14);
                TEXT_PRIMARY = new Color(224, 244, 250);
                TEXT_SECONDARY = new Color(110, 170, 190);
                TEXT_MUTED = new Color(48, 90, 110);
                TEXT_INVERSE = new Color(6, 12, 20);
                CARD_GRADIENT_TOP = new Color(17, 31, 48);
                CARD_GRADIENT_BOT = new Color(10, 19, 31);
                break;
            }
            case REDSTONE: {
                ACCENT = new Color(255, 70, 45);
                ACCENT_BRIGHT = new Color(255, 120, 85);
                ACCENT_DARK = new Color(170, 35, 20);
                ACCENT_MUTED = new Color(255, 70, 45, 40);
                ACCENT_GLOW = new Color(255, 70, 45, 90);
                BG_BASE = new Color(13, 8, 7);
                BG_SURFACE = new Color(21, 12, 10);
                BG_ELEVATED = new Color(32, 18, 15);
                BG_BORDER = new Color(62, 34, 26);
                BG_GLASS = new Color(19, 11, 9, 190);
                TITLE_BAR = new Color(9, 5, 4);
                TEXT_PRIMARY = new Color(252, 230, 220);
                TEXT_SECONDARY = new Color(195, 125, 105);
                TEXT_MUTED = new Color(112, 65, 50);
                TEXT_INVERSE = new Color(13, 8, 7);
                CARD_GRADIENT_TOP = new Color(37, 21, 17);
                CARD_GRADIENT_BOT = new Color(21, 12, 10);
                break;
            }
            case LAPIS: {
                ACCENT = new Color(60, 105, 220);
                ACCENT_BRIGHT = new Color(110, 155, 250);
                ACCENT_DARK = new Color(30, 60, 150);
                ACCENT_MUTED = new Color(60, 105, 220, 40);
                ACCENT_GLOW = new Color(60, 105, 220, 90);
                BG_BASE = new Color(7, 10, 22);
                BG_SURFACE = new Color(12, 16, 33);
                BG_ELEVATED = new Color(18, 24, 46);
                BG_BORDER = new Color(32, 42, 72);
                BG_GLASS = new Color(11, 15, 30, 190);
                TITLE_BAR = new Color(4, 6, 15);
                TEXT_PRIMARY = new Color(228, 234, 250);
                TEXT_SECONDARY = new Color(125, 145, 195);
                TEXT_MUTED = new Color(62, 80, 115);
                TEXT_INVERSE = new Color(7, 10, 22);
                CARD_GRADIENT_TOP = new Color(24, 32, 52);
                CARD_GRADIENT_BOT = new Color(12, 16, 33);
                break;
            }
            case EMERALD: {
                ACCENT = new Color(23, 200, 120);
                ACCENT_BRIGHT = new Color(80, 235, 160);
                ACCENT_DARK = new Color(12, 130, 70);
                ACCENT_MUTED = new Color(23, 200, 120, 40);
                ACCENT_GLOW = new Color(23, 200, 120, 90);
                BG_BASE = new Color(5, 14, 10);
                BG_SURFACE = new Color(9, 22, 15);
                BG_ELEVATED = new Color(14, 32, 22);
                BG_BORDER = new Color(24, 56, 38);
                BG_GLASS = new Color(8, 20, 13, 190);
                TITLE_BAR = new Color(3, 9, 6);
                TEXT_PRIMARY = new Color(220, 246, 230);
                TEXT_SECONDARY = new Color(105, 180, 135);
                TEXT_MUTED = new Color(50, 98, 65);
                TEXT_INVERSE = new Color(5, 14, 10);
                CARD_GRADIENT_TOP = new Color(18, 38, 26);
                CARD_GRADIENT_BOT = new Color(9, 22, 15);
                break;
            }
            case WITHER: {
                ACCENT = new Color(155, 160, 168);
                ACCENT_BRIGHT = new Color(200, 205, 215);
                ACCENT_DARK = new Color(95, 100, 110);
                ACCENT_MUTED = new Color(155, 160, 168, 40);
                ACCENT_GLOW = new Color(155, 160, 168, 90);
                BG_BASE = new Color(9, 9, 10);
                BG_SURFACE = new Color(15, 15, 17);
                BG_ELEVATED = new Color(22, 22, 25);
                BG_BORDER = new Color(42, 42, 47);
                BG_GLASS = new Color(13, 13, 15, 190);
                TITLE_BAR = new Color(6, 6, 7);
                TEXT_PRIMARY = new Color(238, 238, 240);
                TEXT_SECONDARY = new Color(140, 142, 150);
                TEXT_MUTED = new Color(75, 76, 82);
                TEXT_INVERSE = new Color(9, 9, 10);
                CARD_GRADIENT_TOP = new Color(26, 26, 30);
                CARD_GRADIENT_BOT = new Color(15, 15, 17);
                break;
            }
            case SLIME: {
                ACCENT = new Color(120, 220, 60);
                ACCENT_BRIGHT = new Color(165, 240, 110);
                ACCENT_DARK = new Color(70, 140, 30);
                ACCENT_MUTED = new Color(120, 220, 60, 40);
                ACCENT_GLOW = new Color(120, 220, 60, 90);
                BG_BASE = new Color(8, 14, 5);
                BG_SURFACE = new Color(13, 22, 8);
                BG_ELEVATED = new Color(19, 32, 12);
                BG_BORDER = new Color(35, 58, 20);
                BG_GLASS = new Color(12, 21, 7, 190);
                TITLE_BAR = new Color(5, 9, 3);
                TEXT_PRIMARY = new Color(232, 248, 215);
                TEXT_SECONDARY = new Color(140, 190, 100);
                TEXT_MUTED = new Color(68, 100, 45);
                TEXT_INVERSE = new Color(8, 14, 5);
                CARD_GRADIENT_TOP = new Color(24, 38, 14);
                CARD_GRADIENT_BOT = new Color(13, 22, 8);
                break;
            }
            case LIGHT: {
                ACCENT = new Color(99, 58, 190);
                ACCENT_BRIGHT = new Color(124, 88, 220);
                ACCENT_DARK = new Color(67, 30, 150);
                ACCENT_MUTED = new Color(99, 58, 190, 30);
                ACCENT_GLOW = new Color(99, 58, 190, 60);
                BG_BASE = new Color(248, 246, 255);
                BG_SURFACE = new Color(255, 255, 255);
                BG_ELEVATED = new Color(240, 236, 252);
                BG_BORDER = new Color(210, 202, 238);
                BG_GLASS = new Color(255, 255, 255, 210);
                TITLE_BAR = new Color(88, 44, 180);
                TEXT_PRIMARY = new Color(22, 12, 50);
                TEXT_SECONDARY = new Color(80, 58, 130);
                TEXT_MUTED = new Color(140, 118, 175);
                TEXT_INVERSE = new Color(255, 255, 255);
                CARD_GRADIENT_TOP = new Color(255, 255, 255);
                CARD_GRADIENT_BOT = new Color(240, 236, 252);
            }
        }
        activeIsCustom = false;
        GREEN = new Color(34, 197, 94);
        GREEN_DARK = new Color(22, 101, 52);
        RED = new Color(239, 68, 68);
        RED_DARK = new Color(153, 27, 27);
        YELLOW = new Color(234, 179, 8);
        ORANGE = new Color(249, 115, 22);
        CYAN = new Color(6, 182, 212);
        Theme.resetTypography();
        Theme.pushShapeAndTypography();
        Theme.pushToUIManager(bl);
    }

    /* EKLENTI: Tema Olusturucu temasini uygular (renkleri CustomTheme
     * uretir/saklar; burada yalnizca kopyalanir). */
    public static void applyCustom(CustomTheme t) {
        if (t == null) {
            Theme.apply(Preset.NEBULA);
            return;
        }
        boolean bl = t.light;
        if (bl) {
            FlatLightLaf.setup();
        } else {
            FlatDarkLaf.setup();
        }
        t.ensureDerived();
        // V29.6: UI sekli + tipografi temayla birlikte uygulanir.
        Theme.UI_ARC = t.uiArc;
        Theme.FOCUS_WIDTH = t.focusWidth;
        Theme.FONT_FAMILY = t.fontFamily;
        Theme.FONT_SIZE = t.fontSize;
        Theme.HEADING_WEIGHT = t.headingWeight;
        Theme.BUTTON_WEIGHT = t.buttonWeight;
        ACCENT = t.accentColor();
        ACCENT_BRIGHT = t.accentBrightColor();
        ACCENT_DARK = t.accentDarkColor();
        BG_BASE = t.bgBaseColor();
        BG_SURFACE = t.bgSurfaceColor();
        BG_ELEVATED = t.bgElevatedColor();
        BG_BORDER = t.bgBorderColor();
        TEXT_PRIMARY = t.textPrimaryColor();
        TEXT_SECONDARY = t.textSecondaryColor();
        TEXT_MUTED = t.textMutedColor();
        TEXT_INVERSE = bl ? Color.WHITE : t.bgBaseColor();
        CARD_GRADIENT_TOP = t.cardTopColor();
        CARD_GRADIENT_BOT = t.cardBotColor();
        TITLE_BAR = t.titleBarColor();
        activeIsCustom = true;
        GREEN = new Color(34, 197, 94);
        GREEN_DARK = new Color(22, 101, 52);
        RED = new Color(239, 68, 68);
        RED_DARK = new Color(153, 27, 27);
        YELLOW = new Color(234, 179, 8);
        ORANGE = new Color(249, 115, 22);
        CYAN = new Color(6, 182, 212);
        Theme.pushShapeAndTypography();
        Theme.pushToUIManager(bl);
    }

    private static void pushToUIManager(boolean bl) {
        UIManager.put("TitlePane.background", TITLE_BAR);
        UIManager.put("TitlePane.foreground", TEXT_PRIMARY);
        UIManager.put("TitlePane.inactiveForeground", TEXT_MUTED);
        UIManager.put("TitlePane.inactiveBackground", TITLE_BAR);
        UIManager.put("TitlePane.iconifyHoverBackground", BG_ELEVATED);
        UIManager.put("TitlePane.maximizeHoverBackground", BG_ELEVATED);
        UIManager.put("TitlePane.closeHoverBackground", RED_DARK);
        UIManager.put("TitlePane.closeHoverForeground", Color.WHITE);
        UIManager.put("TitlePane.buttonSize", new Dimension(38, 28));
        UIManager.put("TitlePane.menuBarEmbedded", false);
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("Component.accentColor", ACCENT);
        UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("Component.focusWidth", FOCUS_WIDTH);
        // V29.7: yuksek arc text alanlarinda yaziyi bozuyor (FlatLaf rounded
        // clip text'i kesiyor). Bilesen genel arc'i makul sinirda tutulur;
        // buton/kart gibi yazi icermeyen kesimler tam degeri alir.
        UIManager.put("Component.arc", Math.min(UI_ARC, 10));
        UIManager.put("Component.borderColor", BG_BORDER);
        UIManager.put("Button.arc", UI_ARC);
        UIManager.put("Button.margin", new Insets(6, 16, 6, 16));
        UIManager.put("Button.default.boldText", false);
        UIManager.put("Button.background", BG_ELEVATED);
        UIManager.put("Button.foreground", TEXT_PRIMARY);
        UIManager.put("Button.hoverBackground", ACCENT);
        UIManager.put("Button.hoverForeground", Color.WHITE);
        UIManager.put("Button.defaultBackground", ACCENT);
        UIManager.put("Button.defaultForeground", Color.WHITE);
        UIManager.put("Button.disabledBackground", BG_SURFACE);
        UIManager.put("Button.disabledForeground", TEXT_MUTED);
        UIManager.put("TextComponent.arc", Math.min(UI_ARC, 10));
        UIManager.put("TextField.background", BG_ELEVATED);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("TextField.placeholderForeground", TEXT_MUTED);
        UIManager.put("TextArea.background", BG_BASE);
        UIManager.put("TextArea.foreground", TEXT_SECONDARY);
        UIManager.put("ComboBox.arc", UI_ARC);
        UIManager.put("ComboBox.background", BG_ELEVATED);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.buttonBackground", BG_ELEVATED);
        UIManager.put("ComboBox.selectionBackground", ACCENT);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("CheckBox.arc", Math.max(2, UI_ARC / 2));
        UIManager.put("CheckBox.background", BG_SURFACE);
        UIManager.put("CheckBox.foreground", TEXT_PRIMARY);
        UIManager.put("ProgressBar.arc", UI_ARC >= 14 ? 999 : UI_ARC);
        UIManager.put("ProgressBar.horizontalSize", new Dimension(0, 5));
        UIManager.put("ProgressBar.background", BG_BORDER);
        UIManager.put("ProgressBar.foreground", ACCENT);
        UIManager.put("TabbedPane.tabHeight", 40);
        UIManager.put("TabbedPane.tabArc", Math.min(UI_ARC, 10));
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.tabSelectionHeight", 2);
        UIManager.put("TabbedPane.underlineColor", ACCENT);
        UIManager.put("TabbedPane.tabInsets", new Insets(4, 18, 4, 18));
        UIManager.put("TabbedPane.selectedBackground", BG_ELEVATED);
        UIManager.put("TabbedPane.background", BG_BASE);
        UIManager.put("TabbedPane.foreground", TEXT_SECONDARY);
        UIManager.put("TabbedPane.selectedForeground", TEXT_PRIMARY);
        UIManager.put("ScrollBar.width", 8);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("ScrollBar.track", BG_BASE);
        UIManager.put("ScrollBar.thumb", BG_BORDER);
        UIManager.put("ScrollBar.hoverThumbColor", ACCENT_MUTED);
        UIManager.put("List.background", BG_SURFACE);
        UIManager.put("List.selectionBackground", new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 60));
        UIManager.put("List.selectionForeground", TEXT_PRIMARY);
        UIManager.put("Panel.background", BG_SURFACE);
        UIManager.put("Separator.foreground", BG_BORDER);
        UIManager.put("PopupMenu.background", BG_ELEVATED);
        UIManager.put("PopupMenu.foreground", TEXT_PRIMARY);
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(BG_BORDER, 1));
        UIManager.put("MenuItem.background", BG_ELEVATED);
        UIManager.put("MenuItem.foreground", TEXT_PRIMARY);
        UIManager.put("MenuItem.selectionBackground", ACCENT);
        UIManager.put("MenuItem.selectionForeground", Color.WHITE);
        UIManager.put("ToolTip.background", BG_ELEVATED);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(BG_BORDER, 1));
        UIManager.put("Slider.background", BG_SURFACE);
        UIManager.put("Slider.foreground", ACCENT);
        UIManager.put("Slider.thumbColor", ACCENT);
        UIManager.put("Slider.trackValueColor", ACCENT);
        UIManager.put("Slider.trackColor", BG_BORDER);
        UIManager.put("ToggleButton.background", BG_ELEVATED);
        UIManager.put("ToggleButton.foreground", TEXT_PRIMARY);
        UIManager.put("ToggleButton.selectedBackground", ACCENT);
        UIManager.put("ToggleButton.selectedForeground", Color.WHITE);
        UIManager.put("ToggleButton.disabledBackground", BG_SURFACE);
        UIManager.put("ToggleButton.disabledForeground", TEXT_MUTED);
        UIManager.put("ToggleButton.hoverBackground", BG_ELEVATED);
        UIManager.put("ToggleButton.pressedBackground", ACCENT_DARK);
        UIManager.put("ToggleButton.arc", 10);
        UIManager.put("ToggleButton.borderColor", BG_BORDER);
        UIManager.put("ToggleButton.selectedBorderColor", ACCENT);
        UIManager.put("ToggleButton.focusedBorderColor", ACCENT_BRIGHT);
        UIManager.put("Table.background", BG_SURFACE);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground", ACCENT);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.gridColor", BG_BORDER);
        UIManager.put("TableHeader.background", BG_ELEVATED);
        UIManager.put("TableHeader.foreground", TEXT_SECONDARY);
        UIManager.put("Spinner.background", BG_ELEVATED);
        UIManager.put("Spinner.foreground", TEXT_PRIMARY);
        UIManager.put("Spinner.buttonBackground", BG_ELEVATED);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("Label.disabledForeground", TEXT_MUTED);
        UIManager.put("RadioButton.background", BG_SURFACE);
        UIManager.put("RadioButton.foreground", TEXT_PRIMARY);
        UIManager.put("RadioButton.arc", 999);
        UIManager.put("PasswordField.background", BG_ELEVATED);
        UIManager.put("PasswordField.foreground", TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground", ACCENT);
        UIManager.put("PasswordField.placeholderForeground", TEXT_MUTED);
        UIManager.put("FormattedTextField.background", BG_ELEVATED);
        UIManager.put("FormattedTextField.foreground", TEXT_PRIMARY);
        UIManager.put("FormattedTextField.caretForeground", ACCENT);
        UIManager.put("EditorPane.background", BG_BASE);
        UIManager.put("EditorPane.foreground", TEXT_SECONDARY);
        UIManager.put("TextPane.background", BG_BASE);
        UIManager.put("TextPane.foreground", TEXT_SECONDARY);
        UIManager.put("Tree.background", BG_SURFACE);
        UIManager.put("Tree.foreground", TEXT_PRIMARY);
        UIManager.put("Tree.selectionBackground", ACCENT);
        UIManager.put("Tree.selectionForeground", Color.WHITE);
        UIManager.put("InternalFrame.activeTitleBackground", BG_ELEVATED);
        UIManager.put("InternalFrame.activeTitleForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.background", BG_SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.messageBackground", BG_SURFACE);
        UIManager.put("Panel.background", BG_SURFACE);
        UIManager.put("FileChooser.background", BG_SURFACE);
        UIManager.put("FileView.directoryIcon", null);
        UIManager.put("SplitPane.background", BG_BASE);
        UIManager.put("SplitPaneDivider.background", BG_BORDER);
        UIManager.put("SplitPane.dividerSize", 1);
        UIManager.put("Component.focusedBorderColor", ACCENT);
        UIManager.put("Component.focusWidth", 2);
        UIManager.put("Button.focusedBorderColor", ACCENT_BRIGHT);
        UIManager.put("ScrollBar.hoverThumbColor", new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 140));
    }

    public static enum Preset {
        NEBULA("Nebula"),
        DUSK("Dusk"),
        AURORA("Aurora"),
        MIDNIGHT("Midnight"),
        STORM("Storm"),
        FOREST("Forest"),
        CRIMSON("Crimson"),
        EMBER("Ember"),
        OCEAN("Ocean"),
        GOLDEN("Golden"),
        LIGHT("Light"),
        // --- Minecraft temalari (v29) ---
        GRASS_BLOCK("Grass Block"),
        CREEPER("Creeper"),
        NETHER("Nether"),
        THE_END("The End"),
        DIAMOND("Diamond"),
        REDSTONE("Redstone"),
        LAPIS("Lapis"),
        EMERALD("Emerald"),
        WITHER("Wither"),
        SLIME("Slime");

        public final String displayName;

        private Preset(String string2) {
            this.displayName = string2;
        }

        public static Preset fromName(String string) {
            if (string == null) {
                return NEBULA;
            }
            if (string.equalsIgnoreCase("dark")) {
                return NEBULA;
            }
            if (string.equalsIgnoreCase("light")) {
                return LIGHT;
            }
            // Normalizasyon: bosluk/alt cizgi silinip kucuk harfe dusurulur;
            // boylece "Grass Block", "GrassBlock", "grass_block" ve enum
            // adi "GRASS_BLOCK" hepsi ayni preset'e eslesir. Eski kayitlar
            // ("Nebula", "Ocean"...) ve yeni MC temalari ayni yoldan bulunur.
            String norm = string.replaceAll("[\\s_-]+", "").toLowerCase();
            for (Preset preset : Preset.values()) {
                String dn = preset.displayName.replaceAll("[\\s_-]+", "").toLowerCase();
                String en = preset.name().replaceAll("[\\s_-]+", "").toLowerCase();
                if (dn.equals(norm) || en.equals(norm)) {
                    return preset;
                }
            }
            return NEBULA;
        }
    }
}
