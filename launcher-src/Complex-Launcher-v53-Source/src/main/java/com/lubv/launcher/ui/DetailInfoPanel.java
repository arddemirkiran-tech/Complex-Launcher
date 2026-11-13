/*
 * DetailInfoPanel - ModsPanel'deki "mod bilgi" panelinin BIREBIR klonu.
 *
 * Ayni yapi: 56x56 cerceveli ikon + bold baslik + kucuk meta + bold rozet +
 * BG_BASE govde + 2x2 buton izgarasi (Proje Sayfasi / GitHub / aksiyon /
 * Cevir) + gradient kart arka plani. Shaders / Resourcepacks / Modpacks
 * sekmeleri de artik pixel-pixel ayni bilgi panelini kullanir.
 *
 * Ceviri davranisi (dile duyarli):
 *  - Arayuz dili Ingilizceyken metni INGILIZCE'ye cevirir.
 *  - Arayuz dili Turkceyken metni TURKCE'ye cevirir.
 *  - Metin zaten hedef dildeyse (ceviri == orijinal) acik bir bilgi notu
 *    gosterilir, boylece buton "calismiyor" gibi gorunmez.
 *  - Ceviri cache'lenir; ikinci tik "Orijinali Goster" ile geri alir.
 */
package com.lubv.launcher.ui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.L10n;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class DetailInfoPanel
extends JPanel {
    private static final ExecutorService POOL = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "detail-info");
        t.setDaemon(true);
        return t;
    });

    private final JLabel iconLabel;
    private final JLabel titleLabel;
    private final JLabel metaLabel;
    private final JLabel badgeLabel;
    private final JTextArea bodyArea;
    private final JButton translateBtn;
    private final JButton linkBtn;
    private final JButton githubBtn;
    /** 4. izgara hucresi: bos panel; setActionButton verilirse degistirilir. */
    private JPanel actionSlot;
    private final JPanel buttonGrid;
    private final JButton closeBtn;

    private String originalText = "";
    private String translatedText = null;
    private boolean showingTranslation = false;
    private String currentPageUrl = null;
    private int loadGeneration = 0;

    public DetailInfoPanel(Runnable onClose) {
        super(new BorderLayout(0, 0));
        this.setOpaque(false);

        // --- Ust kisim: ModsPanel ile ayni (ikon + baslik/meta/rozet + kapat) ---
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(12, 14, 10, 14));
        this.iconLabel = new JLabel();
        this.iconLabel.setPreferredSize(new Dimension(56, 56));
        this.iconLabel.setMinimumSize(new Dimension(56, 56));
        this.iconLabel.setHorizontalAlignment(0);
        this.iconLabel.setOpaque(true);
        this.iconLabel.setBackground(Theme.BG_BASE);
        this.iconLabel.setBorder(BorderFactory.createLineBorder(Theme.BG_BORDER, 1));
        JPanel titleCol = new JPanel();
        titleCol.setLayout(new BoxLayout(titleCol, 1));
        titleCol.setOpaque(false);
        this.titleLabel = new JLabel(" ");
        this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(1, 14.0f));
        this.titleLabel.setForeground(Theme.TEXT_PRIMARY);
        this.titleLabel.setAlignmentX(0.0f);
        this.metaLabel = new JLabel(" ");
        this.metaLabel.setFont(this.metaLabel.getFont().deriveFont(0, 10.0f));
        this.metaLabel.setForeground(Theme.TEXT_MUTED);
        this.metaLabel.setAlignmentX(0.0f);
        this.badgeLabel = new JLabel(" ");
        this.badgeLabel.setFont(this.badgeLabel.getFont().deriveFont(1, 9.0f));
        this.badgeLabel.setForeground(Color.WHITE);
        this.badgeLabel.setOpaque(true);
        this.badgeLabel.setBackground(Theme.ACCENT_DARK);
        this.badgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        this.badgeLabel.setAlignmentX(0.0f);
        titleCol.add(this.titleLabel);
        titleCol.add(Box.createVerticalStrut(3));
        titleCol.add(this.metaLabel);
        titleCol.add(Box.createVerticalStrut(4));
        titleCol.add(this.badgeLabel);
        // --- Dikkat ceken tema-duyarli X butonu: dolgu rengi temadan gelir
        // (ACCENT kirmiziya yakin temalarda kirmizilasir, mor temada morlasir).
        // Hover'da parlaklasir, bas-in'de koyulasir; cizim self-contained.
        this.closeBtn = new JButton("\u2715") {
            private float hoverA = 0.0f;
            private float pressA = 0.0f;
            private float animA = 0.0f;
            private final javax.swing.Timer anim = new javax.swing.Timer(28, ev -> {
                float target = pressA > 0.5f ? -0.6f : hoverA;
                float next = animA + (target - animA) * 0.35f;
                if (Math.abs(next - target) < 0.004f) next = target;
                if (next != animA) {
                    animA = next;
                    repaint();
                }
            });
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) { hoverA = 1.0f; }
                    @Override public void mouseExited(java.awt.event.MouseEvent e) { hoverA = 0.0f; pressA = 0.0f; }
                    @Override public void mousePressed(java.awt.event.MouseEvent e) { pressA = 1.0f; repaint(); }
                    @Override public void mouseReleased(java.awt.event.MouseEvent e) { pressA = 0.0f; repaint(); }
                });
                anim.start();
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                // Tema renkleri: dolgu ACCENT'ten, parlama ACCENT_BRIGHT'tan.
                Color fill = Theme.ACCENT != null ? Theme.ACCENT : new Color(139, 92, 246);
                Color bright = Theme.ACCENT_BRIGHT != null ? Theme.ACCENT_BRIGHT : fill.brighter();
                Color glyph = Theme.TEXT_PRIMARY != null ? Theme.TEXT_PRIMARY : Color.WHITE;
                Color ring = bright;
                if (pressA > 0.5f) {
                    fill = fill.darker().darker();
                    ring = fill;
                } else if (animA > 0.05f) {
                    float t = animA;
                    fill = new Color(
                        (int)(fill.getRed() + (bright.getRed() - fill.getRed()) * t),
                        (int)(fill.getGreen() + (bright.getGreen() - fill.getGreen()) * t),
                        (int)(fill.getBlue() + (bright.getBlue() - fill.getBlue()) * t));
                }
                g2.setColor(fill);
                g2.fillOval(2, 2, w - 5, h - 5);
                if (animA > 0.05f) {
                    g2.setColor(new Color(ring.getRed(), ring.getGreen(), ring.getBlue(), (int)(170 * Math.max(animA, 0f))));
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawOval(1, 1, w - 3, h - 3);
                }
                g2.setColor(glyph);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String s = "\u2715";
                int sx = (w - fm.stringWidth(s)) / 2;
                int sy = (h - fm.getHeight()) / 2 + fm.getAscent() - 1;
                g2.drawString(s, sx, sy);
                g2.dispose();
            }
        };
        this.closeBtn.setPreferredSize(new Dimension(30, 30));
        this.closeBtn.setToolTipText(L10n.isEnglish() ? "Close (X)" : "Kapat (X)");
        this.closeBtn.setContentAreaFilled(false);
        this.closeBtn.setBorderPainted(false);
        this.closeBtn.setFocusPainted(false);
        this.closeBtn.setOpaque(false);
        this.closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (onClose != null) {
            this.closeBtn.addActionListener(ae -> onClose.run());
        }
        header.add((java.awt.Component)this.iconLabel, "West");
        header.add((java.awt.Component)titleCol, "Center");
        header.add((java.awt.Component)this.closeBtn, "East");

        // --- Orta: govde (ModsPanel ile ayni: BG_BASE uzerinde 12px metin) ---
        this.bodyArea = new JTextArea(" ");
        this.bodyArea.setEditable(false);
        this.bodyArea.setLineWrap(true);
        this.bodyArea.setWrapStyleWord(true);
        this.bodyArea.setFont(new Font("SansSerif", 0, 12));
        this.bodyArea.setForeground(Theme.TEXT_SECONDARY);
        this.bodyArea.setBackground(Theme.BG_BASE);
        this.bodyArea.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        JScrollPane scroll = UiFx.cleanScroll(this.bodyArea);
        scroll.setBackground(Theme.BG_BASE);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.BG_BASE);

        // --- Alt: 2x2 buton izgarasi (ModsPanel ile ayni yerlesim) ---
        boolean en = L10n.isEnglish();
        this.linkBtn = UiFx.accentButton(en ? "Project Page" : "Proje Sayfas\u0131");
        this.linkBtn.addActionListener(ae -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(this.currentPageUrl));
            } catch (Exception ignored) {}
        });
        this.linkBtn.setVisible(false);
        this.githubBtn = UiFx.ghostButton("GitHub");
        this.githubBtn.setVisible(false);
        this.translateBtn = UiFx.ghostButton(en ? "Translate to English" : "T\u00fcrk\u00e7e \u00c7evir");
        this.translateBtn.addActionListener(ae -> this.toggleTranslation());
        this.actionSlot = new JPanel();
        this.actionSlot.setOpaque(false);
        this.actionSlot.setVisible(false);
        this.buttonGrid = new JPanel(new GridLayout(2, 2, 6, 6));
        this.buttonGrid.setOpaque(false);
        this.buttonGrid.setBorder(BorderFactory.createEmptyBorder(6, 14, 12, 14));
        this.buttonGrid.add(this.linkBtn);
        this.buttonGrid.add(this.githubBtn);
        this.buttonGrid.add(this.actionSlot);
        this.buttonGrid.add(this.translateBtn);

        this.add((java.awt.Component)header, "North");
        this.add((java.awt.Component)scroll, "Center");
        this.add((java.awt.Component)this.buttonGrid, "South");
    }

    /** ModsPanel'deki gradient kart gorunumunun aynisi. */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0.0f, 0.0f, Theme.BG_ELEVATED, 0.0f, this.getHeight(),
            UiFx.lerp(Theme.BG_ELEVATED, Theme.BG_BASE, 0.5f)));
        g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 16, 16);
        g2.setColor(Theme.BG_BORDER);
        g2.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 16, 16);
        g2.dispose();
        super.paintComponent(g);
    }

    /** 4. buton hucresi icin ozel aksiyon (orn. "Kur" / "Modrinth'te Ac"). */
    public void setActionButton(JButton b) {
        this.buttonGrid.remove(this.actionSlot);
        if (b != null) {
            this.buttonGrid.add(b, 2);
        } else {
            this.actionSlot = new JPanel();
            this.actionSlot.setOpaque(false);
            this.actionSlot.setVisible(false);
            this.buttonGrid.add(this.actionSlot, 2);
        }
        this.buttonGrid.revalidate();
        this.buttonGrid.repaint();
    }

    public void setMeta(String text) {
        this.metaLabel.setText(text != null && !text.isEmpty() ? text : " ");
    }

    public void setBadge(String text) {
        if (text == null || text.isEmpty()) {
            this.badgeLabel.setVisible(false);
            return;
        }
        this.badgeLabel.setText("  " + text + "  ");
        // CurseForge rozeti turuncu, digerleri tema vurgusu (ModsPanel ile ayni).
        this.badgeLabel.setBackground("CURSEFORGE".equalsIgnoreCase(text.trim()) ? new Color(240, 100, 40) : Theme.ACCENT_DARK);
        this.badgeLabel.setVisible(true);
    }

    /**
     * Panele icerik acar. modrinthSlugOrId verilmisse tam aciklama + ikon +
     * indirme sayisi arka planda Modrinth'ten cekilir; fallbackBody o ana
     * kadar gosterilir.
     */
    public void open(String title, String fallbackBody, String pageUrl, String modrinthSlugOrId, String badge) {
        int gen = ++this.loadGeneration;
        this.titleLabel.setText(title != null && !title.isEmpty() ? title : " ");
        this.setMeta("");
        this.setBadge(badge);
        this.currentPageUrl = pageUrl;
        this.linkBtn.setVisible(pageUrl != null && !pageUrl.isEmpty());
        this.githubBtn.setVisible(false);
        this.iconLabel.setIcon(null);
        this.setBodyState(fallbackBody != null ? fallbackBody : "");
        this.resetTranslateButton();
        this.revalidate();
        this.repaint();
        if (modrinthSlugOrId == null || modrinthSlugOrId.isEmpty()) {
            return;
        }
        boolean en = L10n.isEnglish();
        POOL.submit(() -> {
            try {
                byte[] bytes = HttpUtil.getBytesWithRetry("https://api.modrinth.com/v2/project/" + modrinthSlugOrId, 3);
                JsonObject obj = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
                String body = obj.has("body") && !obj.get("body").isJsonNull() ? obj.get("body").getAsString() : "";
                // Markdown/HTML kalintilarini temizle (ModsPanel ile ayni).
                body = body.replaceAll("(?m)^#{1,6}\\s*", "")
                    .replaceAll("\\*{1,2}([^*]+)\\*{1,2}", "$1")
                    .replaceAll("\\[([^]]+)\\]\\([^)]+\\)", "$1")
                    .replaceAll("<[^>]+>", "")
                    .replaceAll("\\n{3,}", "\n\n")
                    .trim();
                if (body.isEmpty()) {
                    body = en ? "(No description)" : "(A\u00e7\u0131klama bulunamad\u0131)";
                }
                long downloads = obj.has("downloads") ? obj.get("downloads").getAsLong() : 0L;
                StringBuilder meta = new StringBuilder();
                if (downloads > 0L) {
                    meta.append(DetailInfoPanel.fmtCount(downloads)).append(en ? " downloads" : " indirme");
                }
                String iconUrl = obj.has("icon_url") && !obj.get("icon_url").isJsonNull() ? obj.get("icon_url").getAsString() : null;
                String finalBody = body;
                String finalMeta = meta.toString();
                SwingUtilities.invokeLater(() -> {
                    if (gen != DetailInfoPanel.this.loadGeneration) {
                        return;
                    }
                    DetailInfoPanel.this.setBodyState(finalBody);
                    DetailInfoPanel.this.setMeta(finalMeta);
                });
                if (iconUrl != null && !iconUrl.isEmpty()) {
                    DetailInfoPanel.this.loadIcon(iconUrl, gen);
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    if (gen == DetailInfoPanel.this.loadGeneration) {
                        DetailInfoPanel.this.setBodyState((en ? "Could not load info: " : "Bilgi y\u00fcklenemedi: ") + e.getMessage());
                    }
                });
            }
        });
    }

    /** Fetch gerektirmeyen yerel icerik (orn. "yerel dosya - detay yok"). */
    public void openLocal(String title, String body, String badge) {
        this.open(title, body, null, null, badge);
    }

    /** Cevirinin hedef dili = arayuz dili (EN modda en, TR modda tr). */
    private static String targetLang() {
        return L10n.isEnglish() ? "en" : "tr";
    }

    private void resetTranslateButton() {
        boolean en = L10n.isEnglish();
        this.showingTranslation = false;
        this.translateBtn.setVisible(true);
        this.translateBtn.setEnabled(true);
        this.translateBtn.setText(en ? "Translate to English" : "T\u00fcrk\u00e7e \u00c7evir");
    }

    /** Dile duyarli ceviri toggle'i: cache'li, geri alinabilir, dil uyari notlu. */
    private void toggleTranslation() {
        if (this.originalText == null || this.originalText.isBlank()) {
            return;
        }
        boolean en = L10n.isEnglish();
        if (this.showingTranslation) {
            this.bodyArea.setText(this.originalText);
            this.bodyArea.setCaretPosition(0);
            this.showingTranslation = false;
            this.translateBtn.setText(en ? "Translate to English" : "T\u00fcrk\u00e7e \u00c7evir");
            return;
        }
        if (this.translatedText != null) {
            this.bodyArea.setText(this.translatedText);
            this.bodyArea.setCaretPosition(0);
            this.showingTranslation = true;
            this.translateBtn.setText(en ? "Show Original" : "Orijinali G\u00f6ster");
            return;
        }
        final String toTranslate = this.originalText;
        final String target = DetailInfoPanel.targetLang();
        this.translateBtn.setEnabled(false);
        this.translateBtn.setText(en ? "Translating\u2026" : "\u00c7evriliyor\u2026");
        POOL.submit(() -> {
            String translated = HttpUtil.translateText(toTranslate, target);
            SwingUtilities.invokeLater(() -> {
                this.translatedText = translated;
                if (this.originalText.equals(toTranslate)) {
                    if (translated != null && translated.equals(toTranslate)) {
                        // Metin zaten hedef dildeydi - buton "bos calisti" gibi
                        // gorunmesin diye acik bir not gosteriyoruz.
                        String note = en ? "[Already in English]" : "[Zaten T\u00fcrk\u00e7e]";
                        this.bodyArea.setText(toTranslate + "\n\n" + note);
                    } else {
                        this.bodyArea.setText(translated);
                    }
                    this.bodyArea.setCaretPosition(0);
                    this.showingTranslation = true;
                    this.translateBtn.setText(en ? "Show Original" : "Orijinali G\u00f6ster");
                }
                this.translateBtn.setEnabled(true);
            });
        });
    }

    /** Orijinal metni kaydeder + ceviri durumunu sifirlar (tek Dogru Yol). */
    private void setBodyState(String text) {
        this.originalText = text != null ? text : "";
        this.translatedText = null;
        this.showingTranslation = false;
        this.bodyArea.setText(this.originalText.isEmpty() ? " " : this.originalText);
        this.bodyArea.setCaretPosition(0);
        this.resetTranslateButton();
    }

    private void loadIcon(String iconUrl, int gen) {
        POOL.submit(() -> {
            try {
                byte[] bytes = HttpUtil.getBytesWithRetry(iconUrl, 2);
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new ByteArrayInputStream(bytes));
                if (img != null) {
                    java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(56, 56, 2);
                    Graphics2D gg = scaled.createGraphics();
                    gg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    gg.drawImage(img, 0, 0, 56, 56, null);
                    gg.dispose();
                    SwingUtilities.invokeLater(() -> {
                        if (gen == this.loadGeneration) {
                            this.iconLabel.setIcon(new ImageIcon(scaled));
                        }
                    });
                }
            } catch (Exception ignored) {}
        });
    }

    private static String fmtCount(long n) {
        if (n >= 1_000_000L) {
            return String.format("%.1fM", n / 1_000_000.0);
        }
        if (n >= 1_000L) {
            return String.format("%.1fk", n / 1_000.0);
        }
        return String.valueOf(n);
    }
}
