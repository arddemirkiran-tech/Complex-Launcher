/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.Instance;
import com.lubv.launcher.core.InstanceManager;
import com.lubv.launcher.mods.CurseForgeApi;
import com.lubv.launcher.mods.ModSearchHit;
import com.lubv.launcher.mods.ModpackManager;
import com.lubv.launcher.mods.ModrinthApi;
import com.lubv.launcher.ui.GalleryStrip;
import com.lubv.launcher.ui.Theme;
import com.lubv.launcher.ui.UiFx;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import javax.swing.JLayeredPane;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JLayeredPane;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ModpacksPanel
extends JPanel {
    private static final int ICON_SIZE = 36;
    private static final Map<String, ImageIcon> ICON_CACHE = Collections.synchronizedMap(new HashMap());
    private static final Set<String> ICON_LOADING = Collections.synchronizedSet(new HashSet());
    private static final ImageIcon PLACEHOLDER = ModpacksPanel.makePlaceholder();
    private final JTextField searchField;
    private final JToggleButton sourceToggle = new JToggleButton("CurseForge");
    private GalleryStrip galleryStrip;
    private final DefaultListModel<ModSearchHit> resultsModel = new DefaultListModel();
    private final JList<ModSearchHit> resultsList = new JList<ModSearchHit>(this.resultsModel);
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JLabel progressLabel = new JLabel(" ");
    private final Consumer<String> logConsumer;
    private final Supplier<String> apiKeySupplier;
    private final Runnable onInstalled;
    private static final ExecutorService ICON_POOL = Executors.newFixedThreadPool(3, runnable -> {
        Thread thread = new Thread(runnable, "icon-load");
        thread.setDaemon(true);
        return thread;
    });

    private JLayeredPane rightStack;
    private JPanel detailOverlay;
    // ModsPanel'deki bilgi panelinin paylasilan bileşeni.
    private DetailInfoPanel detailInfo;

    public ModpacksPanel(Supplier<String> supplier, Consumer<String> consumer, Runnable runnable) {
        this.apiKeySupplier = supplier;
        this.logConsumer = consumer;
        this.onInstalled = runnable;
        this.searchField = UiFx.searchField("Modpack ara\u2026");
        this.setLayout(new BorderLayout(0, 0));
        this.setOpaque(true);
        this.setBackground(Theme.BG_BASE);
        JPanel jPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        jPanel.setOpaque(true);
        jPanel.setBackground(Theme.BG_BASE);
        jPanel.setBorder(new EmptyBorder(16, 16, 0, 16));
        jPanel.add(this.buildSearchCard());
        jPanel.add(this.buildInstallCard());
        this.add((Component)jPanel, "Center");
        this.add((Component)this.buildProgressBar(), "South");
    }

    /** V34: "Mod bilgisi oto acilsin" ayari - ModpacksPanel icin de gecerli. */
    private boolean infoAutoOpen() {
        try {
            return com.lubv.launcher.core.Settings.load().modInfoAutoOpen;
        }
        catch (Exception e) {
            return false;
        }
    }

    private JPanel buildProgressBar() {
        JPanel jPanel = new JPanel(new BorderLayout(0, 4));
        jPanel.setOpaque(true);
        jPanel.setBackground(Theme.BG_BASE);
        jPanel.setBorder(new EmptyBorder(10, 16, 10, 16));
        this.progressLabel.setFont(this.progressLabel.getFont().deriveFont(0, 11.0f));
        this.progressLabel.setForeground(Theme.TEXT_MUTED);
        this.progressBar.setBorderPainted(false);
        this.progressBar.setPreferredSize(new Dimension(0, 3));
        this.progressBar.setStringPainted(false);
        jPanel.add((Component)this.progressLabel, "North");
        jPanel.add((Component)this.progressBar, "South");
        return jPanel;
    }

    public void setProgress(int n, String string) {
        SwingUtilities.invokeLater(() -> {
            if (n < 0) {
                this.progressBar.setIndeterminate(true);
            } else {
                this.progressBar.setIndeterminate(false);
                UiFx.animateProgress(this.progressBar, n, 150);
            }
            this.progressLabel.setText(string != null ? string : " ");
        });
    }

    private JPanel buildSearchCard() {
        JPanel jPanel = ModpacksPanel.buildCard();
        jPanel.setLayout(new BorderLayout(0, 12));
        JPanel jPanel2 = new JPanel(new BorderLayout(8, 4));
        jPanel2.setOpaque(true);
        jPanel2.setBackground(Theme.BG_SURFACE);
        JLabel jLabel = new JLabel(com.lubv.launcher.core.L10n.isEnglish() ? "Search Modpacks" : "Modpack Ara");
        jLabel.setFont(new Font("SansSerif", 1, 14));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        jPanel2.add((Component)jLabel, "West");
        JPanel jPanel3 = new JPanel(new BorderLayout(6, 0));
        jPanel3.setOpaque(true);
        jPanel3.setBackground(Theme.BG_SURFACE);
        this.sourceToggle.setToolTipText("Modpack'in aranacagi kaynak");
        this.sourceToggle.addActionListener(actionEvent -> {
            this.sourceToggle.setText(this.sourceToggle.isSelected() ? "Modrinth" : "CurseForge");
            this.resultsList.clearSelection();
            this.resultsModel.clear();
            if (this.galleryStrip != null) {
                this.galleryStrip.clear();
            }
        });
        this.searchField.addActionListener(actionEvent -> this.doSearch());
        JButton jButton = UiFx.accentButton(com.lubv.launcher.core.L10n.isEnglish() ? "Search" : "Ara");
        jButton.addActionListener(actionEvent -> this.doSearch());
        jPanel3.add((Component)this.sourceToggle, "West");
        jPanel3.add((Component)this.searchField, "Center");
        jPanel3.add((Component)jButton, "East");
        JPanel jPanel4 = new JPanel();
        jPanel4.setLayout(new BoxLayout(jPanel4, 1));
        jPanel4.setOpaque(true);
        jPanel4.setBackground(Theme.BG_SURFACE);
        jPanel2.setAlignmentX(0.0f);
        jPanel3.setAlignmentX(0.0f);
        jPanel4.add(jPanel2);
        jPanel4.add(Box.createVerticalStrut(8));
        jPanel4.add(jPanel3);
        this.resultsList.setCellRenderer(new ModpackCellRenderer());
        this.resultsList.setFixedCellHeight(62);
        this.resultsList.setSelectionMode(0);
        this.resultsList.setBackground(Theme.BG_BASE);
        this.resultsList.setOpaque(true);
        this.resultsList.setBorder(BorderFactory.createEmptyBorder());
        this.installHoverTooltip(this.resultsList);
        // Cift tiklayinca mod bilgisi detay panelini ac (Modlar sekmesindeki gibi).
        this.resultsList.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) return;
                int idx = ModpacksPanel.this.resultsList.locationToIndex(e.getPoint());
                if (idx < 0) return;
                ModSearchHit hit = ModpacksPanel.this.resultsList.getModel().getElementAt(idx);
                if (hit == null) return;
                String url = hit.curseforge
                    ? (hit.curseforgeMod != null ? "https://www.curseforge.com/minecraft/modpacks?search=" + ModpacksPanel.esc(hit.curseforgeMod.name) : null)
                    : (hit.modrinth != null ? "https://modrinth.com/modpack/" + hit.modrinth.slug : null);
                // Modrinth slug'i da geciriyoruz: boylece bilgi paneli tam
                // aciklamayi + ikonu + indirme sayisini Modrinth'ten ceker.
                String slug = hit.curseforge || hit.modrinth == null ? null : hit.modrinth.slug;
                ModpacksPanel.this.showDetailOverlay(hit.title, hit.description, url, slug);
            }
        });
        JScrollPane jScrollPane = UiFx.cleanScroll(this.resultsList);
        JButton jButton2 = UiFx.accentButton(com.lubv.launcher.core.L10n.isEnglish() ? "Install Selected Modpack" : "Secili Modpack'i Kur");
        jButton2.addActionListener(actionEvent -> this.installSelected());
        this.galleryStrip = new GalleryStrip();
        this.galleryStrip.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        this.resultsList.addListSelectionListener(listSelectionEvent -> {
            if (listSelectionEvent.getValueIsAdjusting()) {
                return;
            }
            if (this.resultsModel.isEmpty()) {
                return;
            }
            ModSearchHit modSearchHit = this.resultsList.getSelectedValue();
            if (modSearchHit == null) {
                this.galleryStrip.clear();
                return;
            }
            if (modSearchHit.curseforge && modSearchHit.curseforgeMod != null) {
                String string = modSearchHit.curseforgeMod.logoUrl;
                this.galleryStrip.loadCurseForge(modSearchHit.curseforgeMod.id, modSearchHit.title, modSearchHit.description, string, this.apiKeySupplier.get());
            } else if (modSearchHit.modrinth != null) {
                this.galleryStrip.load(modSearchHit.modrinth.slug, modSearchHit.title, modSearchHit.description, modSearchHit.iconUrl);
            } else {
                this.galleryStrip.clear();
            }
            // V34: "Mod bilgisi oto acilsin" ayari Modpacks'te de gecerli -
            // acikken secimle detay overlay'i otomatik acilir.
            if (this.infoAutoOpen()) {
                String url = modSearchHit.curseforge
                    ? (modSearchHit.curseforgeMod != null ? "https://www.curseforge.com/minecraft/modpacks?search=" + ModpacksPanel.esc(modSearchHit.curseforgeMod.name) : null)
                    : (modSearchHit.modrinth != null ? "https://modrinth.com/modpack/" + modSearchHit.modrinth.slug : null);
                String slug = modSearchHit.curseforge || modSearchHit.modrinth == null ? null : modSearchHit.modrinth.slug;
                this.showDetailOverlay(modSearchHit.title, modSearchHit.description, url, slug);
            }
        });
        JPanel jPanel5 = new JPanel(new BorderLayout(0, 8));
        jPanel5.setOpaque(true);
        jPanel5.setBackground(Theme.BG_SURFACE);
        jPanel5.add((Component)jScrollPane, "Center");
        jPanel5.add((Component)this.galleryStrip, "South");
        jPanel.add((Component)jPanel4, "North");
        jPanel.add((Component)jPanel5, "Center");
        jPanel.add((Component)jButton2, "South");
        return jPanel;
    }

    private void buildDetailOverlay() {
        if (this.rightStack != null) return;
        this.rightStack = new JLayeredPane();
        this.rightStack.setOpaque(false);
        this.rightStack.add((Component)this.buildSearchCard(), JLayeredPane.DEFAULT_LAYER);
        this.detailOverlay = this.createDetailOverlayPanel();
        this.detailOverlay.setVisible(false);
        this.rightStack.add((Component)this.detailOverlay, JLayeredPane.PALETTE_LAYER);
        this.add((Component)this.rightStack, "Center");
    }

    private JPanel createDetailOverlayPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setOpaque(true);
        root.setBackground(Theme.BG_SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        // Paylasilan bilgi paneli bileşeni (baslik/ikon/ceviri/link icinde).
        this.detailInfo = new DetailInfoPanel(() -> this.hideDetailOverlay());
        root.add((Component)this.detailInfo, "Center");
        return root;
    }

    private void showDetailOverlay(String title, String description, String url) {
        this.showDetailOverlay(title, description, url, null);
    }

    private void showDetailOverlay(String title, String description, String url, String modrinthSlug) {
        if (this.rightStack == null) {
            this.buildDetailOverlay();
        }
        String body = description != null && !description.isBlank() ? description : (com.lubv.launcher.core.L10n.isEnglish() ? "(No description)" : "(A\u00e7\u0131klama yok)");
        String badge = url != null && url.contains("curseforge") ? "CURSEFORGE" : "MODRINTH";
        this.detailInfo.open(title, body, url, modrinthSlug, badge);
        this.detailOverlay.setVisible(true);
        this.rightStack.moveToFront(this.detailOverlay);
        this.rightStack.revalidate();
        this.rightStack.repaint();
    }

    private void hideDetailOverlay() {
        if (this.detailOverlay != null) {
            this.detailOverlay.setVisible(false);
        }
        if (this.rightStack != null) {
            this.rightStack.revalidate();
            this.rightStack.repaint();
        }
    }

    private JPanel buildInstallCard() {
        JPanel jPanel = ModpacksPanel.buildCard();
        jPanel.setLayout(new BorderLayout(0, 12));
        JLabel jLabel = new JLabel(com.lubv.launcher.core.L10n.isEnglish() ? "Local Import" : "Yerel Ice Aktar");
        jLabel.setFont(new Font("SansSerif", 1, 14));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        JLabel jLabel2 = new JLabel(com.lubv.launcher.core.L10n.isEnglish()
            ? "<html><center>You can import a modpack by selecting a .zip or .mrpack file from your computer.</center></html>"
            : "<html><center>Bilgisayarinizdan .zip veya .mrpack dosyasi secerek modpack ice aktarabilirsiniz.</center></html>");
        jLabel2.setFont(jLabel2.getFont().deriveFont(0, 12.0f));
        jLabel2.setForeground(Theme.TEXT_MUTED);
        jLabel2.setHorizontalAlignment(0);
        JButton jButton = UiFx.ghostButton(com.lubv.launcher.core.L10n.isEnglish() ? "+ Import From Local File" : "+ Yerel Dosyadan Ice Aktar");
        jButton.setToolTipText(com.lubv.launcher.core.L10n.isEnglish() ? "Select a .zip or .mrpack file from your computer" : "Bilgisayarinizdan .zip veya .mrpack dosyasi secin");
        jButton.addActionListener(actionEvent -> this.importLocalModpack());
        JPanel jPanel2 = new JPanel();
        jPanel2.setLayout(new BoxLayout(jPanel2, 1));
        jPanel2.setOpaque(true);
        jPanel2.setBackground(Theme.BG_SURFACE);
        jPanel2.add(Box.createVerticalGlue());
        jLabel2.setAlignmentX(0.5f);
        jButton.setAlignmentX(0.5f);
        jPanel2.add(jLabel2);
        jPanel2.add(Box.createVerticalStrut(12));
        jPanel2.add(jButton);
        jPanel2.add(Box.createVerticalGlue());
        jPanel.add((Component)jLabel, "North");
        jPanel.add((Component)jPanel2, "Center");
        return jPanel;
    }

    private static JPanel buildCard() {
        JPanel jPanel = new JPanel(){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setColor(this.getBackground());
                graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 12, 12);
                graphics2D.dispose();
            }
        };
        jPanel.setOpaque(true);
        jPanel.setBackground(Theme.BG_SURFACE);
        jPanel.setBorder(new EmptyBorder(14, 14, 14, 14));
        return jPanel;
    }

    private boolean isCurseforge() {
        return !this.sourceToggle.isSelected();
    }

    private String requireApiKey() {
        // Kullanicinin kendi anahtari varsa onu kullan; yoksa null
        // donduruyoruz. CurseForgeApi.resolveApiKey bu durumda
        // launcher'a gomulu varsayilan anahtara (varsa) otomatik duser.
        String string = this.apiKeySupplier.get();
        return (string == null || string.isBlank()) ? null : string;
    }

    private void doSearch() {
        String string = this.searchField.getText().trim();
        if (string.isEmpty()) {
            return;
        }
        boolean bl = this.isCurseforge();
        this.resultsModel.clear();
        new Thread(() -> {
            try {
                ArrayList<ModSearchHit> arrayList = new ArrayList<ModSearchHit>();
                if (bl) {
                    String apiKey = this.requireApiKey();
                    for (CurseForgeApi.ModResult modResult : CurseForgeApi.searchModpacks(apiKey, string)) {
                        arrayList.add(ModSearchHit.from(modResult));
                    }
                } else {
                    for (ModrinthApi.ModResult modResult : ModrinthApi.searchModpacks(string)) {
                        arrayList.add(ModSearchHit.from(modResult));
                    }
                }
                List<ModSearchHit> results = arrayList;
                SwingUtilities.invokeLater(() -> {
                    for (ModSearchHit modSearchHit : results) {
                        this.resultsModel.addElement(modSearchHit);
                    }
                    if (results.isEmpty()) {
                        this.logConsumer.accept("Modpack bulunamadi: " + string);
                    }
                });
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.logConsumer.accept("Modpack arama hatasi: " + exception.getMessage()));
            }
        }).start();
    }

    private void importLocalModpack() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setDialogTitle("Modpack Sec (.zip / .mrpack)");
        jFileChooser.setFileFilter(new FileNameExtensionFilter("Modpack dosyalari (.zip, .mrpack)", "zip", "mrpack"));
        if (jFileChooser.showOpenDialog(this) != 0) {
            return;
        }
        File file = jFileChooser.getSelectedFile();
        if (file == null || !file.exists()) {
            return;
        }
        String string = file.getName();
        this.logConsumer.accept("Yerel dosya secildi: " + string);
        this.setProgress(0, "Modpack ayristiriliyor...");
        new Thread(() -> {
            ZipFile zipFile = null;
            try {
                boolean bl;
                zipFile = new ZipFile(file, StandardCharsets.UTF_8);
                Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                ArrayList<String> arrayList = new ArrayList<String>();
                while (enumeration.hasMoreElements()) {
                    ZipEntry zipEntry = enumeration.nextElement();
                    arrayList.add(zipEntry.getName());
                }
                boolean bl2 = arrayList.contains("modrinth.index.json");
                boolean bl3 = arrayList.contains("manifest.json");
                boolean bl4 = bl = !bl2 && !bl3 && this.detectServerPack(arrayList);
                if (!(bl2 || bl3 || bl)) {
                    SwingUtilities.invokeLater(() -> {
                        this.logConsumer.accept("Tanimanmayan dosya: icinde ne manifest.json ne de modrinth.index.json bulundu.");
                        this.setProgress(0, "Hata");
                    });
                    return;
                }
                String formatLabel = bl2 ? "Modrinth" : (bl3 ? "CurseForge" : "Server Pack");
                this.logConsumer.accept(formatLabel + "formati tespit edildi");
                this.setProgress(5, formatLabel + "modpack'i aciliyor...");
                if (bl2) {
                    this.importModrinthLocal(zipFile, arrayList, file.getName());
                } else if (bl3) {
                    this.importCurseforgeLocal(zipFile, arrayList, file.getName());
                } else {
                    this.importServerPackLocal(zipFile, arrayList, file.getName());
                }
                SwingUtilities.invokeLater(() -> {
                    this.setProgress(100, "Ice aktarma tamamlandi!");
                    this.onInstalled.run();
                });
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> {
                    this.logConsumer.accept("Ice aktarma hatasi: " + exception.getMessage());
                    this.setProgress(0, "Hata");
                });
            }
            finally {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    }
                    catch (Exception exception) {}
                }
            }
        }).start();
    }

    private boolean detectServerPack(List<String> list) {
        boolean bl = list.stream().anyMatch(string -> string.startsWith("mods/") && string.endsWith(".jar"));
        boolean bl2 = list.stream().anyMatch(string -> string.startsWith("plugins/") && string.endsWith(".jar"));
        boolean bl3 = list.contains("server.properties");
        boolean bl4 = list.stream().anyMatch(string -> string.contains("eula"));
        boolean bl5 = list.stream().anyMatch(string -> string.startsWith("config/"));
        boolean bl6 = list.stream().anyMatch(string -> string.toLowerCase().endsWith(".jar") && !string.startsWith("mods/") && !string.startsWith("plugins/") && !string.startsWith("libraries/") && (string.toLowerCase().contains("server") || string.toLowerCase().contains("forge") || string.toLowerCase().contains("fabric")));
        boolean bl7 = list.stream().anyMatch(string -> string.startsWith("libraries/"));
        return bl && (bl5 || bl3) || bl2 && bl5 || bl3 && bl4 || bl6 && bl || bl7 && (bl || bl6) || bl3 && bl;
    }

    private void importModrinthLocal(ZipFile zipFile, List<String> list, String string) throws Exception {
        byte[] byArray;
        ZipEntry zipEntry = zipFile.getEntry("modrinth.index.json");
        if (zipEntry == null) {
            throw new IOException("modrinth.index.json bulunamadi");
        }
        try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
            byArray = inputStream.readAllBytes();
        }
        JsonObject indexJson = JsonParser.parseString(new String(byArray, StandardCharsets.UTF_8)).getAsJsonObject();
        String string2 = indexJson.has("name") && !indexJson.get("name").isJsonNull() ? indexJson.get("name").getAsString() : string;
        JsonObject jsonObject = indexJson.has("dependencies") ? indexJson.getAsJsonObject("dependencies") : null;
        String string3 = jsonObject != null && jsonObject.has("minecraft") ? jsonObject.get("minecraft").getAsString() : "";
        String string4 = "Vanilla";
        if (jsonObject != null) {
            if (jsonObject.has("fabric-loader")) {
                string4 = "Fabric";
            } else if (jsonObject.has("neoforge")) {
                string4 = "NeoForge";
            } else if (jsonObject.has("forge")) {
                string4 = "Forge";
            }
        }
        String string5 = string4;
        String string6 = ModpackManagerTest.uniqueName(string2);
        Instance instance = InstanceManager.create(string6);
        instance.lastVersion = string3;
        instance.loader = string5;
        instance.save();
        File file = instance.dir();
        SwingUtilities.invokeLater(() -> this.logConsumer.accept("Modpack: " + string2 + " (MC " + string3 + ", " + string5 + ")"));
        SwingUtilities.invokeLater(() -> this.logConsumer.accept("Ornek olusturuldu: " + string6));
        JsonArray jsonArray = indexJson.has("files") ? indexJson.getAsJsonArray("files") : new JsonArray();
        int[] nArray = new int[]{0};
        for (int i = 0; i < jsonArray.size(); ++i) {
            JsonArray jsonArray2;
            JsonObject jsonObject2 = jsonArray.get(i).getAsJsonObject();
            String string7 = jsonObject2.get("path").getAsString();
            JsonArray jsonArray3 = jsonArray2 = jsonObject2.has("downloads") ? jsonObject2.getAsJsonArray("downloads") : null;
            if (jsonArray2 == null || jsonArray2.size() == 0) continue;
            String string8 = jsonArray2.get(0).getAsString();
            File file2 = this.safeResolve(file, string7);
            int n = jsonArray.size();
            this.setProgress(5 + (int)((double)(i + 1) / (double)n * 85.0), "Indiriliyor: " + string7);
            try {
                HttpUtil.downloadFile(string8, file2);
                nArray[0] = nArray[0] + 1;
                continue;
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.logConsumer.accept("Atlandi: " + string7 + " (" + exception.getMessage() + ")"));
            }
        }
        File file3 = this.extractAndGetDir(zipFile, list, "overrides");
        if (file3 != null) {
            ModpacksPanel.copyRecursive(file3, file);
            ModpacksPanel.deleteRecursive(file3);
        }
        int n = nArray[0];
        SwingUtilities.invokeLater(() -> {
            this.logConsumer.accept("Ice aktarma tamamlandi! " + n + "dosya indirildi -> \"" + string6 + "\"");
            this.setProgress(100, "Tamamlandi");
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void importCurseforgeLocal(ZipFile zipFile, List<String> list, String string) throws Exception {
        byte[] byArray;
        ZipEntry zipEntry = zipFile.getEntry("manifest.json");
        if (zipEntry == null) {
            throw new IOException("manifest.json bulunamadi");
        }
        try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
            byArray = inputStream.readAllBytes();
        }
        JsonObject manifestJson = JsonParser.parseString(new String(byArray, StandardCharsets.UTF_8)).getAsJsonObject();
        String string2 = manifestJson.has("name") && !manifestJson.get("name").isJsonNull() ? manifestJson.get("name").getAsString() : string;
        JsonObject jsonObject = manifestJson.getAsJsonObject("minecraft");
        String string3 = jsonObject != null && jsonObject.has("version") ? jsonObject.get("version").getAsString() : "";
        String string4 = ModpacksPanel.mapCfLoaderLocal(jsonObject);
        String string5 = ModpackManagerTest.uniqueName(string2);
        Instance instance = InstanceManager.create(string5);
        instance.lastVersion = string3;
        instance.loader = string4;
        instance.save();
        File file = instance.modsDir();
        SwingUtilities.invokeLater(() -> this.logConsumer.accept("Modpack: " + string2 + " (MC " + string3 + ", " + string4 + ")"));
        SwingUtilities.invokeLater(() -> this.logConsumer.accept("Ornek olusturuldu: " + string5));
        JsonArray jsonArray = manifestJson.getAsJsonArray("files");
        int n = 0;
        String string6 = this.requireApiKey();
        for (int i = 0; i < jsonArray.size(); ++i) {
            JsonObject jsonObject2 = jsonArray.get(i).getAsJsonObject();
            int n2 = jsonObject2.get("projectID").getAsInt();
            int n3 = jsonObject2.get("fileID").getAsInt();
            String string7 = null;
            try {
                string7 = CurseForgeApi.getDownloadUrl(string6, n2, n3);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (string7 == null) {
                SwingUtilities.invokeLater(() -> this.logConsumer.accept("Atlandi: projectID=" + n2));
                continue;
            }
            String string8 = this.fileNameFromUrl(string7);
            File file2 = new File(file, string8);
            int n4 = jsonArray.size();
            this.setProgress(5 + (int)((double)(i + 1) / (double)n4 * 85.0), "Indiriliyor: " + string8);
            try {
                block32: {
                    if (string6 != null) {
                        HttpURLConnection httpURLConnection = (HttpURLConnection)URI.create(string7).toURL().openConnection();
                        try {
                            httpURLConnection.setRequestProperty("x-api-key", string6);
                            httpURLConnection.setInstanceFollowRedirects(true);
                            try (InputStream inputStream = httpURLConnection.getInputStream();
                                 FileOutputStream fileOutputStream = new FileOutputStream(file2);){
                                int n5;
                                byte[] byArray2 = new byte[8192];
                                while ((n5 = inputStream.read(byArray2)) != -1) {
                                    ((OutputStream)fileOutputStream).write(byArray2, 0, n5);
                                }
                                break block32;
                            }
                        }
                        finally {
                            httpURLConnection.disconnect();
                        }
                    }
                    HttpUtil.downloadFile(string7, file2);
                }
                ++n;
                continue;
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.logConsumer.accept("Atlandi: " + string8 + " (" + exception.getMessage() + ")"));
            }
        }
        File file3 = this.extractAndGetDir(zipFile, list, "overrides");
        if (file3 != null) {
            ModpacksPanel.copyRecursive(file3, instance.dir());
            ModpacksPanel.deleteRecursive(file3);
        }
        int n6 = n;
        SwingUtilities.invokeLater(() -> {
            this.logConsumer.accept("Ice aktarma tamamlandi! " + n6 + "mod indirildi -> \"" + string5 + "\"");
            this.setProgress(100, "Tamamlandi");
        });
    }

    private void importServerPackLocal(ZipFile zipFile, List<String> list, String string) throws Exception {
        HashSet<String> hashSet = new HashSet<String>();
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string22 : list) {
            if (string22.contains("/") && !string22.endsWith("/")) {
                hashSet.add(string22.split("/")[0]);
                continue;
            }
            if (string22.isEmpty() || string22.endsWith("/")) continue;
            arrayList.add(string22);
        }
        Object object = null;
        if (hashSet.size() == 1 && arrayList.isEmpty()) {
            object = (String)hashSet.iterator().next() + "/";
        }
        String string22 = ModpackManagerTest.uniqueName(string.replaceAll("\\.zip$", ""));
        Instance instance = InstanceManager.create(string22);
        instance.save();
        File file = instance.dir();
        new File(file, "mods").mkdirs();
        new File(file, "plugins").mkdirs();
        SwingUtilities.invokeLater(() -> this.logConsumer.accept("Server Pack tespit edildi -> \"" + string22 + "\""));
        int n = 0;
        for (String string3 : list) {
            boolean bl;
            if (string3.endsWith("/")) continue;
            String string4 = string3;
            if (object != null && string4.startsWith((String)object)) {
                string4 = string4.substring(((String)object).length());
            }
            if (string4.isEmpty()) continue;
            boolean bl2 = string4.startsWith("mods/") && string4.endsWith(".jar");
            boolean bl3 = bl = string4.startsWith("plugins/") && string4.endsWith(".jar");
            if (!bl2 && !bl) continue;
            File file2 = this.safeResolve(file, string4);
            ZipEntry zipEntry = zipFile.getEntry(string3);
            if (zipEntry == null) continue;
            try (InputStream inputStream = zipFile.getInputStream(zipEntry);
                 FileOutputStream fileOutputStream = new FileOutputStream(file2);){
                int n2;
                byte[] byArray = new byte[8192];
                while ((n2 = inputStream.read(byArray)) != -1) {
                    ((OutputStream)fileOutputStream).write(byArray, 0, n2);
                }
            }
            this.setProgress(Math.min(95, (int)((double)(++n) / (double)Math.max(list.size(), 1) * 100.0)), "Cikariliyor: " + string4);
        }
        int n3 = n;
        SwingUtilities.invokeLater(() -> {
            this.logConsumer.accept("Server Pack'ten " + n3 + "mod/plugin cikarildi -> \"" + string22 + "\"");
            this.setProgress(100, "Tamamlandi");
        });
    }

    private static String mapCfLoaderLocal(JsonObject jsonObject) {
        if (jsonObject != null && jsonObject.has("modLoaders") && jsonObject.get("modLoaders").isJsonArray()) {
            JsonArray jsonArray = jsonObject.getAsJsonArray("modLoaders");
            for (int i = 0; i < jsonArray.size(); ++i) {
                String string;
                JsonObject jsonObject2 = jsonArray.get(i).getAsJsonObject();
                String string2 = string = jsonObject2.has("id") ? jsonObject2.get("id").getAsString() : "";
                if (string.startsWith("fabric-") || string.startsWith("quilt-")) {
                    return "Fabric";
                }
                if (string.startsWith("neoforge-")) {
                    return "NeoForge";
                }
                if (!string.startsWith("forge-")) continue;
                return "Forge";
            }
        }
        return "Vanilla";
    }

    private String fileNameFromUrl(String string) {
        try {
            URI uRI = URI.create(string);
            String string2 = uRI.getPath();
            String string3 = string2.substring(string2.lastIndexOf(47) + 1);
            return URLDecoder.decode(string3, StandardCharsets.UTF_8);
        }
        catch (Exception exception) {
            return "mod-" + System.currentTimeMillis() + ".jar";
        }
    }

    private File extractAndGetDir(ZipFile zipFile, List<String> list, String string) {
        File file;
        String string3 = string + "/";
        boolean bl = list.stream().anyMatch(string2 -> string2.startsWith(string3) && !string2.equals(string3));
        if (!bl) {
            return null;
        }
        try {
            file = Files.createTempDirectory("modpack-" + string, new FileAttribute[0]).toFile();
        }
        catch (IOException iOException) {
            return null;
        }
        for (String string4 : list) {
            String string5;
            if (!string4.startsWith(string3) || string4.equals(string3) || (string5 = string4.substring(string3.length())).isEmpty()) continue;
            File file2 = new File(file, string5);
            if (string4.endsWith("/")) {
                file2.mkdirs();
                continue;
            }
            file2.getParentFile().mkdirs();
            ZipEntry zipEntry = zipFile.getEntry(string4);
            if (zipEntry == null) continue;
            try {
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                try (FileOutputStream fileOutputStream = new FileOutputStream(file2);){
                    int n;
                    byte[] byArray = new byte[8192];
                    while ((n = inputStream.read(byArray)) != -1) {
                        ((OutputStream)fileOutputStream).write(byArray, 0, n);
                    }
                }
                finally {
                    if (inputStream == null) continue;
                    inputStream.close();
                }
            }
            catch (Exception exception) {}
        }
        return file;
    }

    private static void copyRecursive(File file, File file2) throws IOException {
        File[] fileArray = file.listFiles();
        if (fileArray == null) {
            return;
        }
        for (File file3 : fileArray) {
            File file4 = new File(file2, file3.getName());
            if (file3.isDirectory()) {
                file4.mkdirs();
                ModpacksPanel.copyRecursive(file3, file4);
                continue;
            }
            file4.getParentFile().mkdirs();
            Files.copy(file3.toPath(), file4.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteRecursive(File file) {
        File[] fileArray = file.listFiles();
        if (fileArray != null) {
            for (File file2 : fileArray) {
                ModpacksPanel.deleteRecursive(file2);
            }
        }
        file.delete();
    }

    private File safeResolve(File file, String string) {
        File file2 = new File(file, string).getAbsoluteFile();
        if (!file2.getPath().startsWith(file.getAbsolutePath())) {
            throw new IllegalArgumentException("Gecersiz arsiv yolu: " + string);
        }
        return file2;
    }

    private void installSelected() {
        ModSearchHit modSearchHit = this.resultsList.getSelectedValue();
        if (modSearchHit == null) {
            return;
        }
        int n = JOptionPane.showConfirmDialog(this, "\"" + modSearchHit.title + "\"yeni bir ornek olarak kurulacak. Devam edilsin mi?", "Modpack Kur", 0);
        if (n != 0) {
            return;
        }
        new Thread(() -> {
            try {
                if (modSearchHit.curseforge) {
                    this.installCurseforge(modSearchHit.curseforgeMod);
                } else {
                    this.installModrinth(modSearchHit.modrinth);
                }
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.logConsumer.accept("Modpack kurulamadi: " + exception.getMessage()));
            }
        }).start();
    }

    private void installModrinth(ModrinthApi.ModResult modResult) throws Exception {
        List<ModrinthApi.ModVersion> list = ModrinthApi.getVersions(modResult.id, null, null);
        if (list.isEmpty()) {
            SwingUtilities.invokeLater(() -> this.logConsumer.accept(modResult.title + "icin indirilecek surum bulunamadi"));
            return;
        }
        ModrinthApi.ModVersion modVersion = ModrinthApi.pickBestVersion(list);
        ModpackManager.ModpackInfo modpackInfo = ModpackManager.install(modResult, modVersion, this::log, this::setProgress);
        SwingUtilities.invokeLater(() -> {
            this.logConsumer.accept("Modpack kuruldu: " + modResult.title + " -> ornek \"" + modpackInfo.instanceName + "\" (" + modpackInfo.fileCount + "dosya)");
            this.onInstalled.run();
        });
    }

    private void installCurseforge(CurseForgeApi.ModResult modResult) throws Exception {
        String string = this.requireApiKey();
        List<CurseForgeApi.FileResult> list = CurseForgeApi.getFiles(string, modResult.id, null, null);
        if (list.isEmpty()) {
            SwingUtilities.invokeLater(() -> this.logConsumer.accept(modResult.name + "icin indirilecek dosya bulunamadi"));
            return;
        }
        CurseForgeApi.FileResult fileResult = CurseForgeApi.pickBestFile(list);
        ModpackManager.ModpackInfo modpackInfo = ModpackManager.installCurseforge(string, modResult, fileResult, this::log, this::setProgress);
        SwingUtilities.invokeLater(() -> {
            this.logConsumer.accept("Modpack kuruldu: " + modResult.name + " -> ornek \"" + modpackInfo.instanceName + "\" (" + modpackInfo.fileCount + "dosya)");
            this.onInstalled.run();
        });
    }

    private void log(String string) {
        SwingUtilities.invokeLater(() -> this.logConsumer.accept(string));
    }

    private static ImageIcon iconFor(String string, JList<?> jList) {
        if (string == null || string.isEmpty()) {
            return PLACEHOLDER;
        }
        ImageIcon imageIcon = ICON_CACHE.get(string);
        if (imageIcon != null) {
            return imageIcon;
        }
        if (!ICON_LOADING.contains(string)) {
            ICON_LOADING.add(string);
            ICON_POOL.submit(() -> {
                try {
                    byte[] byArray = HttpUtil.getBytes(string);
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(byArray));
                    if (bufferedImage == null) {
                        throw new IOException("Resim cozulunemedi");
                    }
                    BufferedImage bufferedImage2 = new BufferedImage(36, 36, 2);
                    Graphics2D graphics2D = bufferedImage2.createGraphics();
                    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    graphics2D.setClip(new RoundRectangle2D.Float(0.0f, 0.0f, 36.0f, 36.0f, 8.0f, 8.0f));
                    graphics2D.drawImage(bufferedImage, 0, 0, 36, 36, null);
                    graphics2D.dispose();
                    ICON_CACHE.put(string, new ImageIcon(bufferedImage2));
                }
                catch (Exception exception) {
                    ICON_CACHE.put(string, PLACEHOLDER);
                }
                finally {
                    ICON_LOADING.remove(string);
                    SwingUtilities.invokeLater(jList::repaint);
                }
            });
        }
        return PLACEHOLDER;
    }

    private static ImageIcon makePlaceholder() {
        BufferedImage bufferedImage = new BufferedImage(36, 36, 2);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(Theme.BG_ELEVATED);
        graphics2D.fillRoundRect(0, 0, 36, 36, 8, 8);
        graphics2D.setColor(Theme.TEXT_MUTED);
        graphics2D.setFont(new Font("SansSerif", 1, 18));
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        graphics2D.drawString("?", (36 - fontMetrics.stringWidth("?")) / 2, (36 + fontMetrics.getAscent()) / 2 - 3);
        graphics2D.dispose();
        return new ImageIcon(bufferedImage);
    }

    private void installHoverTooltip(final JList<?> jList) {
        jList.addMouseMotionListener(new MouseMotionAdapter(){

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                int n = jList.locationToIndex(mouseEvent.getPoint());
                if (n < 0) {
                    return;
                }
                Object e = jList.getModel().getElementAt(n);
                if (e instanceof ModSearchHit) {
                    ModSearchHit modSearchHit = (ModSearchHit)e;
                    jList.setToolTipText("<html><b>" + ModpacksPanel.esc(modSearchHit.title) + "</b><br>" + ModpacksPanel.esc(modSearchHit.description) + "</html>");
                }
            }
        });
    }

    private static String esc(String string) {
        if (string == null) {
            return "";
        }
        return string.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String fmtDownloads(long l) {
        if (l >= 1000000L) {
            return String.format("%.1fM", (double)l / 1000000.0);
        }
        if (l >= 1000L) {
            return String.format("%.1fK", (double)l / 1000.0);
        }
        return String.valueOf(l);
    }

    private static class ModpackCellRenderer
    extends JPanel
    implements ListCellRenderer<ModSearchHit> {
        private final JLabel iconLabel = new JLabel();
        private final JLabel titleLabel = new JLabel();
        private final JLabel descLabel = new JLabel();
        private final JLabel metaLabel = new JLabel();

        ModpackCellRenderer() {
            this.setLayout(new BorderLayout(12, 0));
            this.setBorder(new EmptyBorder(10, 12, 10, 12));
            this.iconLabel.setPreferredSize(new Dimension(36, 36));
            this.iconLabel.setMinimumSize(new Dimension(36, 36));
            this.iconLabel.setMaximumSize(new Dimension(36, 36));
            this.iconLabel.setHorizontalAlignment(0);
            JPanel jPanel = new JPanel(new GridLayout(3, 1, 0, 2));
            jPanel.setOpaque(false);
            this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(1, 13.0f));
            this.descLabel.setFont(this.descLabel.getFont().deriveFont(0, 11.0f));
            this.metaLabel.setFont(this.metaLabel.getFont().deriveFont(0, 10.0f));
            jPanel.add(this.titleLabel);
            jPanel.add(this.descLabel);
            jPanel.add(this.metaLabel);
            this.add((Component)this.iconLabel, "West");
            this.add((Component)jPanel, "Center");
            this.setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ModSearchHit> jList, ModSearchHit modSearchHit, int n, boolean bl, boolean bl2) {
            if (modSearchHit == null) {
                this.titleLabel.setText("");
                this.descLabel.setText("");
                this.metaLabel.setText("");
                this.iconLabel.setIcon(null);
                return this;
            }
            this.titleLabel.setText(modSearchHit.title);
            String string = modSearchHit.description == null ? "" : modSearchHit.description;
            this.descLabel.setText((String)(string.length() > 72 ? string.substring(0, 72) + "\u2026" : string));
            this.metaLabel.setText("\u2b07 " + ModpacksPanel.fmtDownloads(modSearchHit.downloads) + "   \u00b7   modpack   \u00b7   " + (modSearchHit.curseforge ? "CurseForge" : "Modrinth"));
            this.iconLabel.setIcon(ModpacksPanel.iconFor(modSearchHit.iconUrl, jList));
            Color color = bl ? new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 60) : Theme.BG_BASE;
            this.setBackground(color);
            this.setOpaque(true);
            this.titleLabel.setForeground(Theme.TEXT_PRIMARY);
            this.descLabel.setForeground(Theme.TEXT_SECONDARY);
            this.metaLabel.setForeground(bl ? Theme.ACCENT_BRIGHT : Theme.TEXT_MUTED);
            return this;
        }
    }

    private static class ModpackManagerTest {
        private ModpackManagerTest() {
        }

        static String uniqueName(String string) {
            String string2 = string.replaceAll("[^\\w\\- .]", "").trim();
            if (string2.isEmpty()) {
                string2 = "Modpack";
            }
            String candidate = string2;
            int n = 2;
            while (InstanceManager.listNames().contains(candidate)) {
                candidate = string2 + " (" + n + ")";
                ++n;
            }
            return candidate;
        }
    }
}

