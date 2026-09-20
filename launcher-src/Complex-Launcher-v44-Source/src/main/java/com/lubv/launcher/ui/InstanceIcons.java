package com.lubv.launcher.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Instance kartlarinda gosterilen hazir ikonlari (grass, diamond, nether,
 * end, ocean, cave, forest, desert, snow) ve ozel PNG yuklemeyi render eder.
 * Onceden Instance.iconName alani kaydediliyordu ama hicbir yerde okunup
 * cizilmiyordu - InstanceCard sadece ismin bas harfini gosteriyordu. Bu
 * sinif o eksigi tamamlar.
 */
public final class InstanceIcons {
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    // Custom (kullanicinin yukledigi PNG) ikonlar icin ayri bir cache.
    // ONEMLI DUZELTME: onceden custom ikonlar HIC cache'lenmiyordu - her
    // tek paintComponent cagrisinda (hover animasyonu ~60fps calistigi
    // icin saniyede onlarca kez) ImageIO.read() ile diskten yeniden
    // okunuyordu. Bu hem asiri yavastim hem de UI thread'ini bloke edip
    // render karisikligina (eski/yeni karelerin ust uste binmesi, "duplike"
    // gorunen kartlar) yol aciyordu. Artik dosya yolu + boyut + son
    // degisiklik zamanina gore cache'leniyor; dosya degismedigi surece
    // tekrar okunmuyor.
    private static final Map<String, BufferedImage> CUSTOM_CACHE = new HashMap<>();
    private static final Map<String, Long> CUSTOM_CACHE_MTIME = new HashMap<>();

    private InstanceIcons() {
    }

    /**
     * Verilen ikon adi icin bir gorsel dondurur. "custom" ise customFile
     * varsa disk uzerinden okunur (dosya yolu+boyut+son degisiklik
     * zamanina gore cache'lenir - dosya degismedikce tekrar okunmaz).
     * Diger tum isimler icin vektor tabanli bir blok/sahne cizilir ve
     * boyuta gore cache'lenir.
     */
    public static ImageIcon render(String iconName, int size, File customFile) {
        String key = (iconName == null || iconName.isBlank()) ? "default" : iconName;
        if ("custom".equalsIgnoreCase(key) && customFile != null && customFile.exists()) {
            String cacheKey = customFile.getAbsolutePath() + "@" + size;
            long mtime = customFile.lastModified();
            Long cachedMtime = CUSTOM_CACHE_MTIME.get(cacheKey);
            if (cachedMtime != null && cachedMtime == mtime) {
                BufferedImage cachedImg = CUSTOM_CACHE.get(cacheKey);
                if (cachedImg != null) {
                    return new ImageIcon(cachedImg);
                }
            }
            try {
                BufferedImage img = ImageIO.read(customFile);
                if (img != null) {
                    BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = scaled.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.drawImage(img, 0, 0, size, size, null);
                    g.dispose();
                    CUSTOM_CACHE.put(cacheKey, scaled);
                    CUSTOM_CACHE_MTIME.put(cacheKey, mtime);
                    return new ImageIcon(scaled);
                }
            } catch (Exception ignored) {
                // dusup asagidaki varsayilan cizime devam edilir
            }
        }
        String cacheKey = key + "@" + size;
        BufferedImage cached = CACHE.get(cacheKey);
        if (cached != null) {
            return new ImageIcon(cached);
        }
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintIcon(g, key.toLowerCase(), size);
        g.dispose();
        CACHE.put(cacheKey, img);
        return new ImageIcon(img);
    }

    public static boolean isKnown(String iconName) {
        if (iconName == null) return false;
        switch (iconName.toLowerCase()) {
            case "grass": case "diamond": case "nether": case "end":
            case "ocean": case "cave": case "forest": case "desert":
            case "snow": case "default":
                return true;
            default:
                return false;
        }
    }

    private static void paintIcon(Graphics2D g, String key, int n) {
        switch (key) {
            case "grass":       paintGrass(g, n); break;
            case "diamond":     paintDiamond(g, n); break;
            case "nether":      paintNether(g, n); break;
            case "end":         paintEnd(g, n); break;
            case "ocean":       paintOcean(g, n); break;
            case "cave":        paintCave(g, n); break;
            case "forest":      paintForest(g, n); break;
            case "desert":      paintDesert(g, n); break;
            case "snow":        paintSnow(g, n); break;
            default:            paintDefault(g, n); break;
        }
    }

    /** Basit bir Minecraft-vari cim blogu: kahverengi govde + yesil ust yuzey + benekler. */
    private static void paintGrass(Graphics2D g, int n) {
        g.setColor(new Color(122, 79, 46));
        g.fillRect(0, 0, n, n);
        int topH = (int) (n * 0.32);
        g.setColor(new Color(94, 171, 62));
        g.fillRect(0, 0, n, topH);
        g.setColor(new Color(74, 140, 46));
        java.util.Random r = new java.util.Random(7);
        for (int i = 0; i < 10; i++) {
            int bx = r.nextInt(n);
            int by = (int) (r.nextInt(Math.max(1, n / 3)) * 0.5);
            g.fillRect(bx, by, Math.max(1, n / 14), Math.max(1, n / 14));
        }
        g.setColor(new Color(0, 0, 0, 70));
        g.fillRect(0, topH, n, Math.max(1, n / 20));
    }

    /** Elmas blogu: mavi-cyan tonlarinda parlak, kesik koseli kristal desenli kare. */
    private static void paintDiamond(Graphics2D g, int n) {
        g.setColor(new Color(46, 128, 138));
        g.fillRect(0, 0, n, n);
        g.setColor(new Color(93, 214, 214));
        int inset = (int) (n * 0.14);
        g.fillRect(inset, inset, n - inset * 2, n - inset * 2);
        g.setColor(new Color(180, 245, 245));
        int[] xs = {n / 2, (int) (n * 0.72), n / 2, (int) (n * 0.28)};
        int[] ys = {(int) (n * 0.24), n / 2, (int) (n * 0.76), n / 2};
        g.fillPolygon(xs, ys, 4);
        g.setColor(new Color(255, 255, 255, 160));
        g.fillOval((int) (n * 0.30), (int) (n * 0.24), Math.max(2, n / 10), Math.max(2, n / 10));
    }

    /** Nether: koyu kirmizi-siyah, lav dokusu (gozenekli, kizil parlaklik). */
    private static void paintNether(Graphics2D g, int n) {
        g.setColor(new Color(70, 20, 18));
        g.fillRect(0, 0, n, n);
        java.util.Random r = new java.util.Random(11);
        for (int i = 0; i < 26; i++) {
            int bx = r.nextInt(n);
            int by = r.nextInt(n);
            int size = 1 + r.nextInt(Math.max(1, n / 10));
            g.setColor(r.nextBoolean() ? new Color(40, 8, 6) : new Color(120, 40, 20));
            g.fillOval(bx, by, size, size);
        }
        g.setColor(new Color(255, 110, 30, 130));
        g.fillOval((int) (n * 0.30), (int) (n * 0.55), Math.max(2, n / 6), Math.max(2, n / 8));
    }

    /** The End: acik menekse-sari desenli tas doku. */
    private static void paintEnd(Graphics2D g, int n) {
        g.setColor(new Color(233, 227, 172));
        g.fillRect(0, 0, n, n);
        g.setColor(new Color(190, 160, 90));
        int cell = Math.max(2, n / 6);
        for (int y = 0; y < n; y += cell) {
            for (int x = 0; x < n; x += cell) {
                if (((x / cell) + (y / cell)) % 2 == 0) {
                    g.fillRect(x, y, cell, cell);
                }
            }
        }
        g.setColor(new Color(180, 130, 220, 90));
        g.fillOval((int) (n * 0.25), (int) (n * 0.25), (int) (n * 0.5), (int) (n * 0.5));
    }

    /** Okyanus: dalgali mavi tonlar. */
    private static void paintOcean(Graphics2D g, int n) {
        g.setColor(new Color(18, 70, 120));
        g.fillRect(0, 0, n, n);
        g.setColor(new Color(40, 120, 190));
        g.setStroke(new BasicStroke(Math.max(1f, n * 0.05f)));
        for (int i = 0; i < 4; i++) {
            int y = (int) (n * (0.2 + i * 0.2));
            g.drawArc(-n / 4, y - n / 6, n, n / 3, 0, 180);
        }
        g.setColor(new Color(120, 200, 230, 140));
        g.fillOval((int) (n * 0.6), (int) (n * 0.2), (int) (n * 0.28), (int) (n * 0.28));
    }

    /** Magara: koyu gri tas + siyah bosluk (magara agzi hissi). */
    private static void paintCave(Graphics2D g, int n) {
        g.setColor(new Color(60, 60, 66));
        g.fillRect(0, 0, n, n);
        java.util.Random r = new java.util.Random(3);
        g.setColor(new Color(40, 40, 46));
        for (int i = 0; i < 14; i++) {
            int bx = r.nextInt(n);
            int by = r.nextInt(n);
            int size = 2 + r.nextInt(Math.max(1, n / 8));
            g.fillOval(bx, by, size, size);
        }
        g.setColor(new Color(8, 8, 10));
        g.fillOval((int) (n * 0.28), (int) (n * 0.28), (int) (n * 0.44), (int) (n * 0.44));
    }

    /** Orman: koyu yesil govde + agac siluetleri. */
    private static void paintForest(Graphics2D g, int n) {
        g.setColor(new Color(24, 60, 34));
        g.fillRect(0, 0, n, n);
        g.setColor(new Color(58, 38, 22));
        int trunkW = Math.max(2, n / 10);
        g.fillRect(n / 2 - trunkW / 2, (int) (n * 0.6), trunkW, (int) (n * 0.4));
        g.setColor(new Color(36, 110, 58));
        g.fillOval((int) (n * 0.18), (int) (n * 0.12), (int) (n * 0.64), (int) (n * 0.58));
        g.setColor(new Color(50, 140, 74));
        g.fillOval((int) (n * 0.30), (int) (n * 0.05), (int) (n * 0.4), (int) (n * 0.36));
    }

    /** Col: sicak sari-kahve tonlar + kum dokusu. */
    private static void paintDesert(Graphics2D g, int n) {
        g.setColor(new Color(214, 178, 108));
        g.fillRect(0, 0, n, n);
        g.setColor(new Color(190, 150, 85));
        for (int y = 0; y < n; y += Math.max(2, n / 8)) {
            g.drawLine(0, y, n, y - n / 12);
        }
        g.setColor(new Color(120, 150, 60));
        g.fillRect((int) (n * 0.42), (int) (n * 0.30), Math.max(2, n / 12), (int) (n * 0.5));
        g.fillRect((int) (n * 0.30), (int) (n * 0.45), Math.max(2, n / 14), (int) (n * 0.3));
    }

    /** Kar: beyaz zemin + acik mavi golgeler. */
    private static void paintSnow(Graphics2D g, int n) {
        g.setColor(new Color(235, 240, 245));
        g.fillRect(0, 0, n, n);
        g.setColor(new Color(200, 215, 230));
        java.util.Random r = new java.util.Random(21);
        for (int i = 0; i < 8; i++) {
            int bx = r.nextInt(n);
            int by = r.nextInt(n);
            int size = 2 + r.nextInt(Math.max(1, n / 6));
            g.fillOval(bx, by, size, size);
        }
        g.setColor(new Color(150, 180, 210));
        g.fillRect(0, (int) (n * 0.82), n, (int) (n * 0.18));
    }

    /** Varsayilan/bilinmeyen ikon adi: yumusak gri-mor blok. */
    private static void paintDefault(Graphics2D g, int n) {
        g.setColor(new Color(70, 70, 90));
        g.fillRect(0, 0, n, n);
        g.setColor(new Color(110, 100, 140));
        int inset = (int) (n * 0.2);
        g.fillRect(inset, inset, n - inset * 2, n - inset * 2);
    }
}
