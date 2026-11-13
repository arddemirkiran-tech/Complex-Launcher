package com.lubv.launcher.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/** Minecraft temali piksel sanati: izometrik bloklar, sprite'lar, kalp/XP cizimleri. Tamamen kodla uretilir (resim dosyasi yok). */
final class McArt {
    private McArt() {}

    enum Block { GRASS, DIRT, STONE, DIAMOND, GOLD, TNT }

    private static final Map<Block, BufferedImage> TOP = new EnumMap<>(Block.class);
    private static final Map<Block, BufferedImage> SIDE = new EnumMap<>(Block.class);

    private static Color c(int r, int g, int b) { return new Color(r, g, b); }

    private static final Color[] GRASS_C = {c(91, 153, 52), c(104, 168, 60), c(80, 138, 46), c(118, 180, 70)};
    private static final Color[] DIRT_C = {c(134, 96, 67), c(121, 85, 58), c(150, 108, 76), c(104, 72, 48)};
    private static final Color[] STONE_C = {c(125, 125, 125), c(112, 112, 112), c(137, 137, 137), c(100, 100, 100)};

    private static BufferedImage blank() { return new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB); }

    private static BufferedImage noise(Color[] pal, long seed) {
        BufferedImage im = blank();
        Random r = new Random(seed);
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) im.setRGB(x, y, pal[r.nextInt(pal.length)].getRGB());
        return im;
    }

    /** Tas dokusu: cogunlukla orta gri, az sayida acik/koyu benek. */
    private static BufferedImage stone(long seed) {
        Color base = c(127, 127, 127), light = c(142, 142, 142), dark = c(105, 105, 105), deep = c(92, 92, 92);
        BufferedImage im = blank();
        Random r = new Random(seed);
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
            int v = r.nextInt(100);
            Color col = v < 58 ? base : v < 76 ? light : v < 94 ? dark : deep;
            im.setRGB(x, y, col.getRGB());
        }
        return im;
    }

    /** Cevher damari sablonlari: h = isik, m = ana renk, s = golge. */
    private static final String[][] BLOB = {
        {"hm.", "mmm", ".ms"},
        {"hm", "ms"},
        {"hmm", ".ms"},
        {".h.", "mmm", "sm."},
    };
    private static final int[][] SPOTS = {
        {1, 1, 0}, {7, 1, 1}, {11, 4, 2}, {4, 6, 1}, {8, 8, 3}, {1, 11, 0}, {12, 11, 2}, {6, 13, 1},
    };

    private static BufferedImage ore(long seed, Color hi, Color mid, Color sh, Color outline) {
        BufferedImage im = stone(seed);
        for (int[] sp : SPOTS) {
            String[] t = BLOB[sp[2]];
            // ince koyu kenar (taa dokuya oturmasi icin)
            for (int j = 0; j < t.length; j++) for (int i = 0; i < t[j].length(); i++) {
                if (t[j].charAt(i) == '.') continue;
                for (int dj = -1; dj <= 1; dj++) for (int di = -1; di <= 1; di++) {
                    int x = sp[0] + i + di, y = sp[1] + j + dj;
                    if (x < 0 || y < 0 || x > 15 || y > 15) continue;
                    if ((di == 0 || dj == 0) && im.getRGB(x, y) != 0) {
                        Color cur = new Color(im.getRGB(x, y));
                        if (cur.getRed() == cur.getGreen() && cur.getGreen() == cur.getBlue())
                            im.setRGB(x, y, mixc(cur, outline, 0.55f).getRGB());
                    }
                }
            }
            for (int j = 0; j < t.length; j++) for (int i = 0; i < t[j].length(); i++) {
                char ch = t[j].charAt(i);
                Color col = ch == 'h' ? hi : ch == 'm' ? mid : ch == 's' ? sh : null;
                if (col != null) im.setRGB(sp[0] + i, sp[1] + j, col.getRGB());
            }
        }
        return im;
    }

    private static Color mixc(Color a, Color b, float t) {
        return c((int) (a.getRed() * (1 - t) + b.getRed() * t), (int) (a.getGreen() * (1 - t) + b.getGreen() * t), (int) (a.getBlue() * (1 - t) + b.getBlue() * t));
    }

    private static final int[][][] LET = {
        {{1,1,1},{0,1,0},{0,1,0},{0,1,0},{0,1,0}},   // T
        {{1,0,1},{1,1,1},{1,1,1},{1,0,1},{1,0,1}},   // N
    };

    private static BufferedImage tntSide() {
        BufferedImage im = blank();
        Random r = new Random(71);
        Color[] stick = {c(236, 96, 76), c(216, 58, 46), c(168, 34, 30)};   // acik / ana / koyu (dinamit cubugu)
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
            Color col;
            if (y >= 4 && y <= 11) {
                col = r.nextInt(5) == 0 ? c(198, 198, 190) : c(224, 224, 216);   // beyaz bant
                if (y == 4 || y == 11) col = c(124, 44, 38);
            } else {
                col = stick[x % 3];
                if (y == 0 || y == 15) col = mixc(col, c(90, 20, 18), 0.35f);
            }
            im.setRGB(x, y, col.getRGB());
        }
        int[] xs = {2, 6, 10};
        int[][][] order = {LET[0], LET[1], LET[0]};
        for (int li = 0; li < 3; li++) for (int y = 0; y < 5; y++) for (int x = 0; x < 3; x++)
            if (order[li][y][x] == 1) im.setRGB(xs[li] + x, 5 + y, c(48, 48, 52).getRGB());
        return im;
    }

    private static BufferedImage tntTop() {
        BufferedImage im = blank();
        Random r = new Random(72);
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
            Color col = x % 3 == 0 ? c(236, 96, 76) : x % 3 == 1 ? c(216, 58, 46) : c(168, 34, 30);
            if (x >= 4 && x <= 11 && y >= 4 && y <= 11) col = r.nextInt(4) == 0 ? c(176, 150, 96) : c(196, 170, 112);   // fitil plakasi
            if ((x == 3 || x == 12) && y >= 3 && y <= 12 || (y == 3 || y == 12) && x >= 3 && x <= 12) col = c(110, 36, 30);
            im.setRGB(x, y, col.getRGB());
        }
        for (int y = 7; y <= 8; y++) for (int x = 7; x <= 8; x++) im.setRGB(x, y, c(40, 36, 34).getRGB());
        return im;
    }

    private static synchronized void ensure() {
        if (!TOP.isEmpty()) return;
        TOP.put(Block.GRASS, noise(GRASS_C, 11));
        BufferedImage gs = noise(DIRT_C, 12);
        Random r = new Random(13);
        for (int x = 0; x < 16; x++) {
            int depth = 3 + r.nextInt(3);
            for (int y = 0; y < depth; y++) gs.setRGB(x, y, GRASS_C[r.nextInt(GRASS_C.length)].getRGB());
        }
        SIDE.put(Block.GRASS, gs);
        TOP.put(Block.DIRT, noise(DIRT_C, 21)); SIDE.put(Block.DIRT, noise(DIRT_C, 22));
        TOP.put(Block.STONE, stone(31)); SIDE.put(Block.STONE, stone(32));
        BufferedImage d = ore(41, c(220, 255, 250), c(92, 219, 213), c(38, 150, 150), c(30, 60, 62));
        TOP.put(Block.DIAMOND, d); SIDE.put(Block.DIAMOND, d);
        BufferedImage g = ore(51, c(255, 250, 160), c(250, 224, 60), c(200, 140, 30), c(70, 50, 20));
        TOP.put(Block.GOLD, g); SIDE.put(Block.GOLD, g);
        TOP.put(Block.TNT, tntTop()); SIDE.put(Block.TNT, tntSide());
    }

    private static void face(Graphics2D g, BufferedImage img, double ox, double oy, double ux, double uy, double vx, double vy, int shadeAlpha) {
        AffineTransform at = new AffineTransform(ux / 16.0, uy / 16.0, vx / 16.0, vy / 16.0, ox, oy);
        g.drawImage(img, at, null);
        if (shadeAlpha > 0) {
            java.awt.geom.Path2D p = new java.awt.geom.Path2D.Double();
            p.moveTo(ox, oy); p.lineTo(ox + ux, oy + uy); p.lineTo(ox + ux + vx, oy + uy + vy); p.lineTo(ox + vx, oy + vy); p.closePath();
            g.setColor(new Color(0, 0, 0, shadeAlpha));
            g.fill(p);
            g.setColor(new Color(0, 0, 0, 70));
            g.draw(p);
        }
    }

    /** Izometrik blok (cx,cy = ust yuzun alt kosesi / kuenin merkezi). s = kenar uzunlugu(piksel). */
    static void paintBlock(Graphics2D g0, int cx, int cy, int s, Block b) {
        ensure();
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double k = 0.866;
        face(g, TOP.get(b), cx, cy - s, s * k, s / 2.0, -s * k, s / 2.0, 0);
        face(g, SIDE.get(b), cx - s * k, cy - s / 2.0, s * k, s / 2.0, 0, s, 55);
        face(g, SIDE.get(b), cx, cy, s * k, -s / 2.0, 0, s, 105);
        g.setColor(new Color(255, 255, 255, 38));
        g.drawLine(cx, cy - s, (int) (cx + s * k), cy - s / 2);
        g.drawLine(cx, cy - s, (int) (cx - s * k), cy - s / 2);
        g.dispose();
    }

    /** Slot icin: tek karelik kucuk blok ikonu. */
    static void paintBlockIcon(Graphics2D g, int x, int y, int size, Block b) {
        int s = (int) (size * 0.5);
        paintBlock(g, x + size / 2, y + (int) (size * 0.5), s, b);
    }

    // ---------------- sprite'lar (8x8) ----------------
    enum Sprite {
        DIAMOND(new String[]{"..bbbb..", ".bwcccb.", "bwcccccb", "bcccccdb", ".bcccdb.", "..bcdb..", "...bb...", "........"}),
        PICKAXE(new String[]{"..mmmmm.", ".mmwwwmm", "....s..m", "...s....", "..s.....", ".s......", "s.......", "........"}),
        CREEPER(new String[]{"gGgggGgg", "gggGgggG", "gKKggKKg", "gKKggKKg", "gggKKggg", "ggKKKKgg", "ggKKKKgg", "ggKggKgg"}),
        HEART(new String[]{".rr.rr..", "rwrrrrr.", "rrrrrrr.", "rrrrrrr.", ".rrrrr..", "..rrr...", "...r....", "........"});
        final String[] rows;
        Sprite(String[] rows) { this.rows = rows; }
    }

    private static Color pal(char ch) {
        switch (ch) {
            case 'b': return c(20, 120, 140); case 'c': return c(75, 233, 224); case 'w': return c(220, 255, 250); case 'd': return c(40, 170, 180);
            case 'm': return c(165, 165, 170); case 's': return c(140, 98, 57);
            case 'g': return c(90, 170, 70); case 'G': return c(60, 130, 50); case 'K': return c(18, 18, 18);
            case 'r': return c(222, 40, 40); default: return null;
        }
    }

    static void paintSprite(Graphics2D g0, Sprite sp, int x, int y, int px, float alpha) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setComposite(java.awt.AlphaComposite.SrcOver.derive(alpha));
        for (int j = 0; j < 8; j++) for (int i = 0; i < 8; i++) {
            Color col = pal(sp.rows[j].charAt(i));
            if (col == null) continue;
            if (sp == Sprite.HEART && sp.rows[j].charAt(i) == 'w') col = c(255, 170, 170);
            g.setColor(col);
            g.fillRect(x + i * px, y + j * px, px, px);
        }
        g.dispose();
    }

    /** RAM'i "can" olarak gosteren kalp seridi. fraction 0..1, 10 kalp. */
    static void paintHearts(Graphics2D g, int x, int y, int px, double fraction) {
        double filled = Math.max(0, Math.min(1, fraction)) * 10.0;
        for (int i = 0; i < 10; i++) {
            int hx = x + i * (7 * px + px);
            double f = Math.max(0, Math.min(1, filled - i));
            paintSprite(g, Sprite.HEART, hx, y, px, 0.28f);
            if (f >= 1.0) {
                paintSprite(g, Sprite.HEART, hx, y, px, 1f);
            } else if (f > 0) {
                Graphics2D gg = (Graphics2D) g.create();
                gg.setClip(hx, y, (int) (4 * px), 8 * px);
                paintSprite(gg, Sprite.HEART, hx, y, px, 1f);
                gg.dispose();
            }
        }
    }

    /** Minecraft "slot": koyu govde + acik/koyu bevel. */
    static void paintSlot(Graphics2D g, int x, int y, int size, boolean selected) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(x, y, size, size);
        g.setColor(new Color(40, 40, 46));
        g.fillRect(x + 2, y + 2, size - 4, size - 4);
        g.setColor(new Color(10, 10, 12));
        g.fillRect(x + 2, y + 2, size - 4, 2);
        g.fillRect(x + 2, y + 2, 2, size - 4);
        g.setColor(new Color(85, 85, 95));
        g.fillRect(x + 2, y + size - 4, size - 4, 2);
        g.fillRect(x + size - 4, y + 2, 2, size - 4);
        if (selected) {
            g.setColor(new Color(255, 255, 255, 235));
            g.fillRect(x - 2, y - 2, size + 4, 3);
            g.fillRect(x - 2, y + size - 1, size + 4, 3);
            g.fillRect(x - 2, y - 2, 3, size + 4);
            g.fillRect(x + size - 1, y - 2, 3, size + 4);
        }
    }

    static Block blockForLoader(String loader) {
        String l = loader == null ? "" : loader.toLowerCase();
        if (l.contains("neoforge")) return Block.TNT;
        if (l.contains("forge")) return Block.GOLD;
        if (l.contains("fabric")) return Block.DIAMOND;
        if (l.contains("optifine")) return Block.STONE;
        return Block.GRASS;
    }

    /** XP cubugu gorunumlu ilerleme gostergesi (JProgressBar'in degerini okur). */
    static javax.swing.JComponent xpBar(final javax.swing.JProgressBar pb) {
        final javax.swing.JComponent bar = new javax.swing.JComponent() {
            private final javax.swing.Timer anim = new javax.swing.Timer(70, e -> repaint());
            @Override public void addNotify() { super.addNotify(); anim.start(); }
            @Override public void removeNotify() { anim.stop(); super.removeNotify(); }
            @Override protected void paintComponent(java.awt.Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                int w = getWidth(), h = getHeight();
                int bh = 10, by = h - bh - 1;
                g.setColor(Color.BLACK);
                g.fillRect(0, by, w, bh);
                g.setColor(new Color(72, 72, 78));
                g.fillRect(1, by + 1, w - 2, bh - 2);
                g.setColor(new Color(40, 40, 44));
                g.fillRect(1, by + bh - 3, w - 2, 2);
                int max = Math.max(1, pb.getMaximum() - pb.getMinimum());
                double frac = (pb.getValue() - pb.getMinimum()) / (double) max;
                int inner = w - 2;
                if (pb.isIndeterminate()) {
                    int seg = Math.max(30, inner / 5);
                    int pos = (int) ((System.currentTimeMillis() / 12) % (inner + seg)) - seg;
                    java.awt.Shape old = g.getClip();
                    g.setClip(1, by + 1, inner, bh - 2);
                    paintFill(g, 1 + pos, by + 1, seg, bh - 2);
                    g.setClip(old);
                } else if (frac > 0) {
                    paintFill(g, 1, by + 1, (int) (inner * Math.min(1.0, frac)), bh - 2);
                }
                g.setColor(new Color(0, 0, 0, 90));
                for (int x = 1 + inner / 20; x < w - 1; x += Math.max(2, inner / 20)) g.fillRect(x, by + 1, 1, bh - 2);
                if (!pb.isIndeterminate() && frac > 0) {
                    String t = String.valueOf((int) Math.round(frac * 100));
                    g.setFont(Theme.uiFont(1, 14.0f));
                    java.awt.FontMetrics fm = g.getFontMetrics();
                    int tx = (w - fm.stringWidth(t)) / 2, ty = by - 3;
                    g.setColor(Color.BLACK);
                    g.drawString(t, tx - 1, ty); g.drawString(t, tx + 1, ty); g.drawString(t, tx, ty - 1); g.drawString(t, tx, ty + 1);
                    g.setColor(new Color(128, 255, 32));
                    g.drawString(t, tx, ty);
                }
                g.dispose();
            }
            private void paintFill(Graphics2D g, int x, int y, int fw, int fh) {
                g.setColor(new Color(100, 220, 20));
                g.fillRect(x, y, fw, fh);
                g.setColor(new Color(190, 255, 130));
                g.fillRect(x, y, fw, 2);
                g.setColor(new Color(60, 150, 10));
                g.fillRect(x, y + fh - 2, fw, 2);
            }
        };
        bar.setPreferredSize(new java.awt.Dimension(100, 34));
        bar.setOpaque(false);
        return bar;
    }

    /** Kalp seridi bileseni (RAM'i can olarak gosterir). */
    static javax.swing.JComponent heartsRow(final java.util.function.DoubleSupplier fraction) {
        javax.swing.JComponent c = new javax.swing.JComponent() {
            @Override protected void paintComponent(java.awt.Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                paintHearts(g, 0, 0, 2, fraction.getAsDouble());
                g.dispose();
            }
        };
        c.setPreferredSize(new java.awt.Dimension(10 * 16, 16));
        c.setMaximumSize(new java.awt.Dimension(10 * 16, 16));
        c.setOpaque(false);
        return c;
    }
}
