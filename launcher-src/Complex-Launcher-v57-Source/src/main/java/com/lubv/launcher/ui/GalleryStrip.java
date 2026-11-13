/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.ui.Theme;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class GalleryStrip
extends JPanel {
    private static final int THUMB_W = 192;
    private static final int THUMB_H = 108;
    private static final int ICON_SZ = 68;
    private static final int MAX_IMGS = 8;
    private static final Pattern MD_IMG = Pattern.compile("!\\[[^\\]]*\\]\\((https://[^)]+\\.(?:png|jpg|jpeg|webp|gif)[^)]*)\\)|\"(https://cdn\\.modrinth\\.com/data/[^\"]+\\.(?:png|jpg|jpeg|webp|gif))\"", 2);
    private static final ExecutorService POOL = Executors.newFixedThreadPool(3, runnable -> {
        Thread thread = new Thread(runnable, "gallery-fetch");
        thread.setDaemon(true);
        return thread;
    });
    private final JPanel thumbsRow;
    private final JLabel nameLabel;
    private final JLabel descLabel;
    private final JLabel emptyLabel;
    private String currentId;

    public GalleryStrip() {
        this.setLayout(new BorderLayout(0, 6));
        this.setOpaque(false);
        this.nameLabel = new JLabel("Bir \u00f6\u011fe se\u00e7in");
        this.nameLabel.setFont(new Font("SansSerif", 1, 13));
        this.nameLabel.setForeground(Theme.TEXT_PRIMARY);
        this.descLabel = new JLabel(" ");
        this.descLabel.setFont(new Font("SansSerif", 0, 11));
        this.descLabel.setForeground(Theme.TEXT_SECONDARY);
        JPanel jPanel = new JPanel(new BorderLayout(0, 2));
        jPanel.setOpaque(false);
        jPanel.add((Component)this.nameLabel, "North");
        jPanel.add((Component)this.descLabel, "Center");
        this.add((Component)jPanel, "North");
        this.thumbsRow = new JPanel(new FlowLayout(0, 6, 0));
        this.thumbsRow.setOpaque(false);
        this.emptyLabel = new JLabel("Ekran g\u00f6r\u00fcnt\u00fcs\u00fc yok");
        this.emptyLabel.setFont(this.emptyLabel.getFont().deriveFont(11.0f));
        this.emptyLabel.setForeground(Theme.TEXT_MUTED);
        JScrollPane jScrollPane = new JScrollPane(this.thumbsRow, 21, 30);
        jScrollPane.setBorder(null);
        jScrollPane.setOpaque(false);
        jScrollPane.getViewport().setOpaque(false);
        jScrollPane.setPreferredSize(new Dimension(0, 122));
        jScrollPane.getHorizontalScrollBar().setUnitIncrement(24);
        this.add((Component)jScrollPane, "Center");
        this.clear();
    }

    public void clear() {
        this.currentId = null;
        this.nameLabel.setText("Bir \u00f6\u011fe se\u00e7in");
        this.descLabel.setText(" ");
        this.thumbsRow.removeAll();
        this.thumbsRow.add(this.emptyLabel);
        this.thumbsRow.revalidate();
        this.thumbsRow.repaint();
    }

    public void load(String string, String string2, String string3, String string4) {
        this.loadInternal(string, string2, string3, string4, false, null);
    }

    public void loadCurseForge(int n, String string, String string2, String string3, String string4) {
        this.loadInternal(String.valueOf(n), string, string2, string3, true, string4);
    }

    private void loadInternal(String string, String string2, String string3, String string4, boolean bl, String string5) {
        String string6;
        this.currentId = string6 = string;
        this.nameLabel.setText(string2 != null ? string2 : "");
        String string7 = string3 != null ? string3 : "";
        this.descLabel.setText((String)(string7.length() > 110 ? string7.substring(0, 110) + "\u2026" : string7));
        this.thumbsRow.removeAll();
        this.thumbsRow.add(GalleryStrip.makeSkeleton(68, 68));
        this.thumbsRow.add(GalleryStrip.makeSkeleton(192, 108));
        this.thumbsRow.add(GalleryStrip.makeSkeleton(192, 108));
        this.thumbsRow.revalidate();
        this.thumbsRow.repaint();
        POOL.submit(() -> {
            ArrayList<JLabel> arrayList = new ArrayList<JLabel>();
            JLabel iconThumb;
            if (string4 != null && !string4.isBlank() && (iconThumb = this.fetchThumb(string4, 68, 68, true, string6)) != null) {
                arrayList.add(iconThumb);
            }
            ArrayList<String> galleryUrls = new ArrayList<String>();
            try {
                if (bl) {
                    String text = HttpUtil.getText("https://api.curseforge.com/v1/mods/" + string6, string5);
                    JsonObject root = JsonParser.parseString(text).getAsJsonObject();
                    if (root.has("data") && !root.get("data").isJsonNull()) {
                        JsonObject data = root.getAsJsonObject("data");
                        if (data.has("screenshots") && !data.get("screenshots").isJsonNull()) {
                            JsonArray screenshots = data.getAsJsonArray("screenshots");
                            for (int i = 0; i < screenshots.size() && galleryUrls.size() < 8; ++i) {
                                JsonObject shot = screenshots.get(i).getAsJsonObject();
                                if (!shot.has("url") || shot.get("url").isJsonNull()) continue;
                                galleryUrls.add(shot.get("url").getAsString());
                            }
                        }
                        if (galleryUrls.isEmpty() && data.has("logo") && !data.get("logo").isJsonNull()) {
                            JsonObject logo = data.getAsJsonObject("logo");
                            if (logo.has("url") && !logo.get("url").isJsonNull()) {
                                galleryUrls.add(logo.get("url").getAsString());
                            }
                        }
                    }
                } else {
                    byte[] bytes = HttpUtil.getBytes("https://api.modrinth.com/v2/project/" + string6);
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                    if (root.has("gallery") && !root.get("gallery").isJsonNull()) {
                        JsonArray gallery = root.getAsJsonArray("gallery");
                        for (int i = 0; i < gallery.size() && galleryUrls.size() < 8; ++i) {
                            JsonObject item = gallery.get(i).getAsJsonObject();
                            if (!item.has("url") || item.get("url").isJsonNull()) continue;
                            galleryUrls.add(item.get("url").getAsString());
                        }
                    }
                    if (galleryUrls.size() < 2 && root.has("body") && !root.get("body").isJsonNull()) {
                        Matcher matcher = MD_IMG.matcher(root.get("body").getAsString());
                        while (matcher.find() && galleryUrls.size() < 8) {
                            String url = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                            if (url == null || galleryUrls.contains(url)) continue;
                            galleryUrls.add(url);
                        }
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            for (String url : galleryUrls) {
                if (!string6.equals(this.currentId)) {
                    return;
                }
                JLabel thumb = this.fetchThumb(url, 192, 108, false, string6);
                if (thumb == null) continue;
                arrayList.add(thumb);
            }
            SwingUtilities.invokeLater(() -> {
                if (!string6.equals(this.currentId)) {
                    return;
                }
                this.thumbsRow.removeAll();
                if (arrayList.isEmpty()) {
                    this.thumbsRow.add(this.emptyLabel);
                } else {
                    for (JLabel jLabel : arrayList) {
                        this.thumbsRow.add(jLabel);
                    }
                }
                this.thumbsRow.revalidate();
                this.thumbsRow.repaint();
            });
        });
    }

    private JLabel fetchThumb(String string, int n, int n2, boolean bl, String string2) {
        try {
            byte[] byArray = HttpUtil.getBytesWithRetry(string, 3);
            final BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(byArray));
            if (bufferedImage == null) {
                return null;
            }
            BufferedImage bufferedImage2 = GalleryStrip.coverCrop(bufferedImage, n, n2, bl);
            JLabel jLabel = new JLabel(new ImageIcon(bufferedImage2));
            jLabel.setPreferredSize(new Dimension(n, n2));
            jLabel.setCursor(Cursor.getPredefinedCursor(12));
            jLabel.addMouseListener(new MouseAdapter(){

                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    GalleryStrip.showFull(bufferedImage);
                }
            });
            return jLabel;
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static BufferedImage coverCrop(BufferedImage bufferedImage, int n, int n2, boolean bl) {
        int n3;
        int n4;
        int n5;
        int n6;
        BufferedImage bufferedImage2 = new BufferedImage(n, n2, 2);
        Graphics2D graphics2D = bufferedImage2.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int n7 = bl ? 10 : 6;
        graphics2D.setClip(new RoundRectangle2D.Float(0.0f, 0.0f, n, n2, n7, n7));
        double d = (double)bufferedImage.getWidth() / (double)bufferedImage.getHeight();
        double d2 = (double)n / (double)n2;
        if (d > d2) {
            n6 = bufferedImage.getHeight();
            n5 = (int)((double)n6 * d2);
            n4 = 0;
            n3 = (bufferedImage.getWidth() - n5) / 2;
        } else {
            n5 = bufferedImage.getWidth();
            n6 = (int)((double)n5 / d2);
            n3 = 0;
            n4 = (bufferedImage.getHeight() - n6) / 2;
        }
        graphics2D.drawImage(bufferedImage, 0, 0, n, n2, n3, n4, n3 + n5, n4 + n6, null);
        graphics2D.setClip(null);
        graphics2D.setColor(new Color(255, 255, 255, 18));
        graphics2D.setStroke(new BasicStroke(1.0f));
        graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, n7, n7);
        graphics2D.dispose();
        return bufferedImage2;
    }

    private static JLabel makeSkeleton(int n, int n2) {
        BufferedImage bufferedImage = new BufferedImage(n, n2, 2);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(Theme.BG_ELEVATED);
        graphics2D.fillRoundRect(0, 0, n, n2, 6, 6);
        graphics2D.setColor(new Color(255, 255, 255, 12));
        graphics2D.fillRect(0, n2 / 2 - 8, n, 16);
        graphics2D.dispose();
        return new JLabel(new ImageIcon(bufferedImage));
    }

    private static void showFull(BufferedImage bufferedImage) {
        SwingUtilities.invokeLater(() -> {
            JDialog jDialog = new JDialog();
            jDialog.setTitle("\u00d6nizleme");
            jDialog.setModal(false);
            jDialog.setLayout(new BorderLayout());
            int n = Math.min(bufferedImage.getWidth(), 1280);
            int n2 = Math.min(bufferedImage.getHeight(), 800);
            double d = Math.min((double)n / (double)bufferedImage.getWidth(), (double)n2 / (double)bufferedImage.getHeight());
            int n3 = (int)((double)bufferedImage.getWidth() * d);
            int n4 = (int)((double)bufferedImage.getHeight() * d);
            JLabel jLabel = new JLabel(new ImageIcon(bufferedImage.getScaledInstance(n3, n4, 4)));
            jLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            jDialog.add((Component)jLabel, "Center");
            jDialog.setSize(n3 + 20, n4 + 40);
            jDialog.setLocationRelativeTo(null);
            jDialog.setVisible(true);
        });
    }
}

