/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.ui;

import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.mods.ModrinthApi;
import com.lubv.launcher.mods.ShaderManager;
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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class ShaderPanel
extends JPanel {
    private static final int ICON_SIZE = 36;
    private static final Map<String, ImageIcon> ICON_CACHE = Collections.synchronizedMap(new HashMap());
    private static final Set<String> ICON_LOADING = Collections.synchronizedSet(new HashSet());
    private static final ImageIcon PLACEHOLDER = ShaderPanel.makePlaceholder();
    private final JTextField searchField = new JTextField();
    private GalleryStrip galleryStrip;
    private final DefaultListModel<ModrinthApi.ModResult> searchResultsModel = new DefaultListModel();
    private final JList<ModrinthApi.ModResult> searchResultsList = new JList<ModrinthApi.ModResult>(this.searchResultsModel);
    private final DefaultListModel<Object> installedModel = new DefaultListModel();
    private final JList<Object> installedList = new JList<Object>(this.installedModel);
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JLabel progressLabel = new JLabel(" ");
    private final Supplier<File> shaderDirSupplier;
    private final Consumer<String> logConsumer;
    private static final ExecutorService ICON_POOL = Executors.newFixedThreadPool(3, runnable -> {
        Thread thread = new Thread(runnable, "icon-load");
        thread.setDaemon(true);
        return thread;
    });

    private JLayeredPane installedRightStack;
    private JPanel installedSideWrap;
    private JPanel shaderDetailOverlay;
    // ModsPanel'deki bilgi panelinin paylasilan bileşeni (ceviri + tam
    // aciklama + link + ikon hepsi icinde). Eski elle yazilmis overlay
    // bu bileşenle degistirildi - tahminî arama ve ceviri artik dogru
    // calisiyor.
    private DetailInfoPanel detailInfo;

    public ShaderPanel(Supplier<File> supplier, Consumer<String> consumer) {
        this.shaderDirSupplier = supplier;
        this.logConsumer = consumer;
        this.setLayout(new BorderLayout(0, 0));
        this.setOpaque(true);
        this.setBackground(Theme.BG_BASE);
        JPanel jPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        jPanel.setOpaque(true);
        jPanel.setBackground(Theme.BG_BASE);
        jPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        jPanel.add(this.buildSearchSide());

        // Kurulu shaderlar listesi artik bir JLayeredPane icinde: mod
        // bilgisine (kurulu shadera cift tiklaninca) basildiginda acilan
        // detay paneli listenin TAMAMINI kaplar - ModsPanel'deki ayni
        // mekanizma burada da uygulanmis oldu.
        this.installedSideWrap = new JPanel(new BorderLayout());
        this.installedSideWrap.setOpaque(false);
        this.installedSideWrap.add((Component)this.buildInstalledSide(), "Center");
        this.shaderDetailOverlay = this.buildShaderDetailOverlay();
        this.shaderDetailOverlay.setVisible(false);
        this.installedRightStack = new JLayeredPane();
        this.installedRightStack.setOpaque(false);
        this.installedRightStack.add((Component)this.installedSideWrap, JLayeredPane.DEFAULT_LAYER);
        this.installedRightStack.add((Component)this.shaderDetailOverlay, JLayeredPane.PALETTE_LAYER);
        this.installedRightStack.addComponentListener(new java.awt.event.ComponentAdapter(){
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                ShaderPanel.this.layoutInstalledRightStack();
            }
        });
        jPanel.add(this.installedRightStack);

        this.add((Component)jPanel, "Center");
        this.add((Component)this.buildProgressPane(), "South");
        this.refreshInstalled();
        // V34: Panel acilista populer shaderlarla dolu gelsin -
        // bos arama da ayni listeyi getirdigi icin ayni metodu cagir.
        this.doSearch();
        // V34: .zip shader dosyalarini dogrudan panelin her yerine surukle-birak
        this.installZipDragAndDrop();
    }

    /** V34: .zip dosyalarini surukleyip birakarak shader kur. */
    private void installZipDragAndDrop() {
        javax.swing.TransferHandler th = new javax.swing.TransferHandler() {

            @Override
            public boolean canImport(javax.swing.TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(javax.swing.TransferHandler.TransferSupport support) {
                if (!this.canImport(support)) {
                    return false;
                }
                try {
                    java.util.List<File> files = (java.util.List<File>)support.getTransferable().getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                    boolean en = com.lubv.launcher.core.L10n.isEnglish();
                    File dir = ShaderPanel.this.shaderDirSupplier.get();
                    int ok = 0;
                    for (File f : files) {
                        if (f.getName().toLowerCase().endsWith(".zip")) {
                            java.nio.file.Files.copy(f.toPath(), new File(dir, f.getName()).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            ShaderPanel.this.logConsumer.accept((en ? "Shader installed (dropped): " : "Shader kuruldu (sürükle-birak): ") + f.getName());
                            ok++;
                        }
                    }
                    if (ok == 0) {
                        ShaderPanel.this.logConsumer.accept(en ? "No .zip files found — only shader .zip packs can be dropped here." : ".zip dosyasi bulunamadi — buraya sadece shader .zip paketleri sürüklenebilir.");
                    }
                    ShaderPanel.this.refreshInstalled();
                    return true;
                }
                catch (Exception ex) {
                    ShaderPanel.this.logConsumer.accept("Drag & drop hatasi: " + ex.getMessage());
                    return false;
                }
            }
        };
        this.setTransferHandler(th);
        this.searchResultsList.setTransferHandler(th);
        this.installedList.setTransferHandler(th);
    }

    private void layoutInstalledRightStack() {
        int w = this.installedRightStack.getWidth();
        int h = this.installedRightStack.getHeight();
        this.installedSideWrap.setBounds(0, 0, w, h);
        this.shaderDetailOverlay.setBounds(0, 0, w, h);
    }

    /** Kurulu bir shader'in Modrinth aciklamasini/linkini gosteren overlay - paylasilan DetailInfoPanel bileşeni. */
    private JPanel buildShaderDetailOverlay() {
        this.detailInfo = new DetailInfoPanel(() -> this.hideShaderDetailOverlay());
        return this.detailInfo;
    }

    /** V34: "Mod bilgisi oto acilsin" ayari - ShaderPanel icin de gecerli. */
    private boolean infoAutoOpen() {
        try {
            return com.lubv.launcher.core.Settings.load().modInfoAutoOpen;
        }
        catch (Exception e) {
            return false;
        }
    }

    private void showShaderDetailOverlay() {
        this.layoutInstalledRightStack();
        this.shaderDetailOverlay.setVisible(true);
        this.installedRightStack.moveToFront(this.shaderDetailOverlay);
        this.installedRightStack.revalidate();
        this.installedRightStack.repaint();
    }

    private void hideShaderDetailOverlay() {
        this.shaderDetailOverlay.setVisible(false);
        this.installedRightStack.revalidate();
        this.installedRightStack.repaint();
    }

    /** Dosya adindan tahmini bir arama terimiyle Modrinth'te arama yapip ilk sonucun detayini gosterir. */
    private void loadInstalledShaderDetailByGuess(String searchTerm, String fallbackTitle) {
        boolean en = com.lubv.launcher.core.L10n.isEnglish();
        this.detailInfo.open(fallbackTitle, en ? "Searching\u2026" : "Aran\u0131yor\u2026", null, null, "SHADER");
        this.showShaderDetailOverlay();
        ICON_POOL.submit(() -> {
            try {
                List<ModrinthApi.ModResult> results = ModrinthApi.searchShaders(searchTerm);
                if (results.isEmpty()) {
                    SwingUtilities.invokeLater(() -> this.detailInfo.openLocal(fallbackTitle,
                        en ? "Not found on Modrinth. (Searched: " + searchTerm + ")" : "Bu shader Modrinth'te bulunamad\u0131. (Aranan: " + searchTerm + ")", "SHADER"));
                    return;
                }
                ModrinthApi.ModResult best = results.get(0);
                SwingUtilities.invokeLater(() -> this.loadInstalledShaderDetail(best.slug, best.title));
            }
            catch (Exception e) {
                SwingUtilities.invokeLater(() -> this.detailInfo.openLocal(fallbackTitle,
                    (en ? "Search error: " : "Arama hatas\u0131: ") + e.getMessage(), "SHADER"));
            }
        });
    }

    /** Kurulu bir shader'a cift tiklaninca cagrilir: Modrinth'ten tam bilgi ceker ve overlay'i acar. */
    private void loadInstalledShaderDetail(String projectSlugOrId, String displayTitle) {
        this.detailInfo.open(displayTitle,
            com.lubv.launcher.core.L10n.isEnglish() ? "Loading\u2026" : "Y\u00fckleniyor\u2026",
            "https://modrinth.com/shader/" + projectSlugOrId,
            projectSlugOrId, "SHADER");
        this.showShaderDetailOverlay();
    }

    private JPanel buildProgressPane() {
        JPanel jPanel = new JPanel(new BorderLayout(0, 4));
        jPanel.setOpaque(true);
        jPanel.setBackground(Theme.BG_BASE);
        jPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        this.progressLabel.setFont(this.progressLabel.getFont().deriveFont(0, 11.0f));
        this.progressLabel.setForeground(Theme.TEXT_MUTED);
        this.progressBar.setStringPainted(false);
        this.progressBar.setBorderPainted(false);
        this.progressBar.setPreferredSize(new Dimension(0, 3));
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

    private JPanel buildSearchSide() {
        JPanel jPanel = ShaderPanel.buildCard();
        jPanel.setLayout(new BorderLayout(8, 8));
        JLabel jLabel = new JLabel("Modrinth'te Shader Ara");
        jLabel.setFont(new Font("SansSerif", 1, 14));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        JPanel jPanel2 = new JPanel(new BorderLayout(6, 0));
        jPanel2.setOpaque(false);
        this.searchField.putClientProperty("JTextField.placeholderText", com.lubv.launcher.core.L10n.isEnglish() ? "Type a shader name..." : "Shader adi yazin...");
        this.searchField.addActionListener(actionEvent -> this.doSearch());
        // V34 CANLI ARAMA + ESC temizle
        javax.swing.Timer liveTimer = new javax.swing.Timer(450, actionEvent -> this.doSearch());
        liveTimer.setRepeats(false);
        this.searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void schedule() {
                liveTimer.restart();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                this.schedule();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                this.schedule();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                this.schedule();
            }
        });
        javax.swing.InputMap sfIM = this.searchField.getInputMap(javax.swing.JComponent.WHEN_FOCUSED);
        sfIM.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "clearSearch");
        this.searchField.getActionMap().put("clearSearch", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                ShaderPanel.this.searchField.setText("");
                ShaderPanel.this.doSearch();
            }
        });
        this.searchField.setPreferredSize(new Dimension(0, 28));
        this.searchField.setMinimumSize(new Dimension(60, 28));
        JButton jButton = UiFx.accentButton(com.lubv.launcher.core.L10n.isEnglish() ? "Search" : "Ara");
        jButton.setPreferredSize(new Dimension(60, 28));
        jButton.addActionListener(actionEvent -> this.doSearch());
        jPanel2.add((Component)this.searchField, "Center");
        jPanel2.add((Component)jButton, "East");
        JPanel jPanel3 = new JPanel();
        jPanel3.setLayout(new BoxLayout(jPanel3, 1));
        jPanel3.setOpaque(false);
        jLabel.setAlignmentX(0.0f);
        jPanel2.setAlignmentX(0.0f);
        jPanel3.add(jLabel);
        jPanel3.add(Box.createVerticalStrut(6));
        jPanel3.add(jPanel2);
        jPanel3.add(Box.createVerticalStrut(4));
        jPanel.add((Component)jPanel3, "North");
        this.searchResultsList.setCellRenderer(new ShaderCellRenderer());
        this.searchResultsList.setFixedCellHeight(56);
        this.searchResultsList.setSelectionMode(0);
        this.installHoverTooltip(this.searchResultsList);
        this.galleryStrip = new GalleryStrip();
        this.galleryStrip.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        this.searchResultsList.addListSelectionListener(listSelectionEvent -> {
            if (listSelectionEvent.getValueIsAdjusting()) {
                return;
            }
            ModrinthApi.ModResult modResult = this.searchResultsList.getSelectedValue();
            if (modResult == null) {
                this.galleryStrip.clear();
                return;
            }
            this.galleryStrip.load(modResult.slug, modResult.title, modResult.description, modResult.iconUrl);
            // V34: bilgi paneli SADECE "oto ac" ayari acikken secimle acilir;
            // kapaliyken cift tik / Install yoluyla acilir (ModsPanel ile ayni).
            if (this.infoAutoOpen()) {
                this.loadInstalledShaderDetail(modResult.slug, modResult.title);
            }
        });
        JPanel jPanel4 = new JPanel(new BorderLayout(0, 6));
        jPanel4.setOpaque(false);
        jPanel4.add((Component)new JScrollPane(this.searchResultsList), "Center");
        jPanel4.add((Component)this.galleryStrip, "South");
        jPanel.add((Component)jPanel4, "Center");
        JButton jButton2 = UiFx.accentButton(com.lubv.launcher.core.L10n.isEnglish() ? "Install Selected Shader" : "Secili Shader'i Kur");
        jButton2.addActionListener(actionEvent -> this.installSelected());
        jPanel.add((Component)jButton2, "South");
        return jPanel;
    }

    private JPanel buildInstalledSide() {
        JPanel jPanel = ShaderPanel.buildCard();
        jPanel.setLayout(new BorderLayout(8, 8));
        JLabel jLabel = new JLabel(com.lubv.launcher.core.L10n.isEnglish() ? "Installed Shaders" : "Kurulu Shaderlar");
        jLabel.setFont(jLabel.getFont().deriveFont(1, 13.0f));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        jPanel.add((Component)jLabel, "North");
        this.installedList.setCellRenderer(new InstalledShaderRenderer());
        this.installedList.setFixedCellHeight(44);
        this.installedList.setSelectionMode(0);
        this.installedList.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) {
                    return;
                }
                Object selected = ShaderPanel.this.installedList.getSelectedValue();
                if (selected == null) {
                    return;
                }
                // once kalici meta kaydina bak: bu dosya hangi Modrinth
                // projesinden kurulduysa BILEBILECEGIMIZ sekilde saklaniyor.
                // Boylece bilgi paneli tahminle degil, TAM isabetle acilir
                // (ModsPanel'deki davranisin aynisi).
                String fileName = selected.toString();
                com.lubv.launcher.mods.InstallMeta.Entry meta = com.lubv.launcher.mods.InstallMeta.get(ShaderPanel.this.shaderDirSupplier.get(), fileName);
                if (meta != null && meta.slug != null && !meta.slug.isBlank()) {
                    ShaderPanel.this.loadInstalledShaderDetail(meta.slug, meta.title != null ? meta.title : fileName);
                    return;
                }
                // Meta yoksa (eski kurulum) dosya adindan tahmin et.
                String searchTerm = fileName.replaceAll("\\.(zip|jar)$", "").replaceAll("[_\\-\\.]", " ").replaceAll("\\d+\\.\\d+(\\.\\d+)?", "").trim();
                ShaderPanel.this.loadInstalledShaderDetailByGuess(searchTerm, fileName);
            }
        });
        jPanel.add((Component)new JScrollPane(this.installedList), "Center");

        // --- Bilgi bolumu (modlar sekmesindeki gibi): shader klasorunu ve
        // otomatik kurulacak shaderlari gosterir. ---
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, 1));
        boolean isEn = com.lubv.launcher.core.L10n.isEnglish();
        JLabel infoTitle = new JLabel(isEn ? "\u2139 Shader Info" : "\u2139 Shader Bilgisi");
        infoTitle.setFont(infoTitle.getFont().deriveFont(1, 11.0f));
        infoTitle.setForeground(Theme.ACCENT_BRIGHT);
        infoTitle.setAlignmentX(0.0f);
        infoTitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 2, 0));
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setOpaque(false);
        infoArea.setFont(infoArea.getFont().deriveFont(0, 10.5f));
        infoArea.setForeground(Theme.TEXT_MUTED);
        infoArea.setAlignmentX(0.0f);
        infoPanel.add(infoTitle);
        infoPanel.add(infoArea);
        Runnable refreshShaderInfo = () -> {
            File dir = this.shaderDirSupplier.get();
            int count = dir != null && dir.isDirectory() ? dir.listFiles((d, name) -> name.toLowerCase().endsWith(".zip")) != null
                ? java.util.Objects.requireNonNull(dir.listFiles((d, name) -> name.toLowerCase().endsWith(".zip"))).length : 0 : 0;
            StringBuilder sb = new StringBuilder();
            sb.append(isEn ? "Folder: " : "Klasor: ").append(dir != null ? dir.getAbsolutePath() : "?").append("\n");
            sb.append(isEn ? "Installed shader packs: " : "Kurulu shader paketi: ").append(count).append("\n");
            // Otomatik kurulum listesindeki shaderlari goster.
            java.util.List<String> autoShaders = MainWindow.listCustomAutoModsOfType("shader");
            if (!autoShaders.isEmpty()) {
                sb.append(isEn ? "Auto-install shaders: " : "Otomatik kurulacak shaderlar: ").append(String.join(", ", autoShaders));
            } else {
                sb.append(isEn ? "No auto-install shaders configured (Settings tab)." : "Otomatik kurulacak shader ayarlanmamis (Ayarlar sekmesi).");
            }
            infoArea.setText(sb.toString());
        };
        refreshShaderInfo.run();
        this.shaderInfoRefresher = refreshShaderInfo;
        jPanel.add((Component)infoPanel, "South");

        JPanel jPanel2 = new JPanel(new GridLayout(1, 2, 6, 0));
        JButton jButton = UiFx.dangerButton(isEn ? "Remove" : "Kaldir");
        jButton.addActionListener(actionEvent -> this.removeSelected());
        JButton jButton2 = UiFx.ghostButton(isEn ? "Open Folder" : "Klasoru Ac");
        jButton2.addActionListener(actionEvent -> this.openFolder());
        jPanel2.add(jButton);
        jPanel2.add(jButton2);
        JPanel bottomWrap = new JPanel(new BorderLayout(0, 6));
        bottomWrap.setOpaque(false);
        bottomWrap.add((Component)infoPanel, "North");
        bottomWrap.add((Component)jPanel2, "South");
        jPanel.add((Component)bottomWrap, "South");
        return jPanel;
    }

    /** Bilgi bolumunu yeniden doldurur (kurulum sonrasi cagrilir). */
    private Runnable shaderInfoRefresher;

    private void refreshShaderInfo() {
        if (this.shaderInfoRefresher != null) {
            this.shaderInfoRefresher.run();
        }
    }

    private void doSearch() {
        String string = this.searchField.getText().trim();
        // V34: bos arama = populer shaderlar (Modrinth varsayilana gore sirali)
        this.searchResultsModel.clear();
        new Thread(() -> {
            try {
                List<ModrinthApi.ModResult> list = ModrinthApi.searchShaders(string);
                SwingUtilities.invokeLater(() -> {
                    for (ModrinthApi.ModResult modResult : list) {
                        this.searchResultsModel.addElement(modResult);
                    }
                    if (list.isEmpty()) {
                        this.logConsumer.accept("Shader bulunamad\u0131: " + string);
                    }
                });
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.logConsumer.accept("Shader arama hatas\u0131: " + exception.getMessage()));
            }
        }).start();
    }

    private void installSelected() {
        ModrinthApi.ModResult modResult = this.searchResultsList.getSelectedValue();
        if (modResult == null) {
            return;
        }
        File file = this.shaderDirSupplier.get();
        new Thread(() -> {
            try {
                List<ModrinthApi.ModVersion> list = ModrinthApi.getVersions(modResult.id, null, null);
                if (list.isEmpty()) {
                    boolean en = com.lubv.launcher.core.L10n.isEnglish();
                    SwingUtilities.invokeLater(() -> this.logConsumer.accept(
                        (en ? "No downloadable version found for " : " ") + modResult.title + (en ? "" : " i\u00e7in indirilecek s\u00fcr\u00fcm bulunamad\u0131")));
                    return;
                }
                ModrinthApi.ModVersion modVersion = ModrinthApi.pickBestVersion(list);
                this.setProgress(0, "Shader indiriliyor: " + modResult.title);
                ShaderManager.install(file, modVersion, (l, l2) -> this.setProgress(l2 > 0L ? (int)(l * 100L / l2) : -1, "Shader indiriliyor: " + modResult.title));
                // Kurulum meta'sini kaydet: dosya adi -> Modrinth projesi.
                // Boylece kurulu listede cift tiklayinca bilgi paneli TAM
                // dogru projeyi acar (tahmin yok).
                com.lubv.launcher.mods.InstallMeta.Entry meta = new com.lubv.launcher.mods.InstallMeta.Entry();
                meta.slug = modResult.slug;
                meta.title = modResult.title;
                meta.iconUrl = modResult.iconUrl;
                meta.source = "modrinth";
                com.lubv.launcher.mods.InstallMeta.put(file, modVersion.fileName, meta);
                SwingUtilities.invokeLater(() -> {
                    this.logConsumer.accept("Shader kuruldu: " + modResult.title + " (" + modVersion.versionNumber + ")");
                    this.refreshInstalled();
                });
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.logConsumer.accept("Shader kurulamad\u0131: " + exception.getMessage()));
            }
        }).start();
    }

    private void removeSelected() {
        Object object = this.installedList.getSelectedValue();
        if (object == null || !(object instanceof String)) {
            return;
        }
        String string = (String)object;
        // BUG DUZELTMESI: bos-liste placeholder'i da String oldugu icin
        // Ingilizce modda ("No shaders yet...") Kaldir'a basincak placeholder
        // metnini dosya adi sanip silmeye calisiyordu. Sadece gercek .zip
        // dosyalarini kabul et.
        if (!string.toLowerCase().endsWith(".zip")) {
            return;
        }
        boolean en = com.lubv.launcher.core.L10n.isEnglish();
        int n = JOptionPane.showConfirmDialog(this,
            en ? "Remove \"" + string + "\"?" : "\"" + string + "\" kaldirilsin mi?",
            en ? "Confirm" : "Onay", 0);
        if (n == 0) {
            ShaderManager.remove(this.shaderDirSupplier.get(), string);
            com.lubv.launcher.mods.InstallMeta.remove(this.shaderDirSupplier.get(), string);
            this.logConsumer.accept("Shader kald\u0131r\u0131ld\u0131: " + string);
            this.refreshInstalled();
        }
    }

    private void openFolder() {
        File file = this.shaderDirSupplier.get();
        file.mkdirs();
        try {
            Desktop.getDesktop().open(file);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void refreshInstalled() {
        ArrayList<String> arrayList = new ArrayList<String>(ShaderManager.listInstalled(this.shaderDirSupplier.get()));
        Runnable runnable = () -> {
            this.installedModel.clear();
            for (String string : arrayList) {
                this.installedModel.addElement(string);
            }
            if (this.installedModel.isEmpty()) {
                this.installedModel.addElement(com.lubv.launcher.core.L10n.isEnglish()
                    ? "No shaders yet. Add one from the search section."
                    : "Henuz shader yok. Arama bolumunden shader ekleyin.");
            }
            // Bilgi bolumunu de guncelle.
            if (this.shaderInfoRefresher != null) {
                this.shaderInfoRefresher.run();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
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
        graphics2D.setFont(new Font("SansSerif", 1, 28));
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        String string = "?";
        graphics2D.drawString(string, (36 - fontMetrics.stringWidth(string)) / 2, (36 + fontMetrics.getAscent()) / 2 - 3);
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
                if (e instanceof ModrinthApi.ModResult) {
                    ModrinthApi.ModResult modResult = (ModrinthApi.ModResult)e;
                    jList.setToolTipText("<html><b>" + ShaderPanel.escapeHtml(modResult.title) + "</b><br>" + ShaderPanel.escapeHtml(modResult.description) + "</html>");
                }
            }
        });
    }

    private static String escapeHtml(String string) {
        if (string == null) {
            return "";
        }
        return string.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String formatDownloads(int n) {
        if (n >= 1000000) {
            return String.format("%.1fM", (double)n / 1000000.0);
        }
        if (n >= 1000) {
            return String.format("%.1fK", (double)n / 1000.0);
        }
        return String.valueOf(n);
    }

    private static void colorize(JPanel jPanel, boolean bl, JLabel jLabel, JLabel jLabel2, JLabel jLabel3) {
        Color color = bl ? new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 60) : Theme.BG_BASE;
        jPanel.setBackground(color);
        jPanel.setOpaque(true);
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        jLabel2.setForeground(bl ? Theme.TEXT_SECONDARY : Theme.TEXT_SECONDARY);
        jLabel3.setForeground(bl ? Theme.ACCENT_BRIGHT : Theme.TEXT_MUTED);
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
        jPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        return jPanel;
    }

    private static class ShaderCellRenderer
    extends JPanel
    implements ListCellRenderer<ModrinthApi.ModResult> {
        private final JLabel iconLabel = new JLabel();
        private final JLabel titleLabel = new JLabel();
        private final JLabel descLabel = new JLabel();
        private final JLabel metaLabel = new JLabel();

        ShaderCellRenderer() {
            this.setLayout(new BorderLayout(10, 0));
            this.setBorder(new EmptyBorder(8, 10, 8, 10));
            this.iconLabel.setPreferredSize(new Dimension(36, 36));
            this.iconLabel.setHorizontalAlignment(0);
            this.iconLabel.setOpaque(false);
            JPanel jPanel = new JPanel(new GridLayout(3, 1, 0, 2));
            jPanel.setOpaque(false);
            this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(1, 13.0f));
            this.titleLabel.setOpaque(false);
            this.descLabel.setFont(this.descLabel.getFont().deriveFont(0, 11.0f));
            this.descLabel.setOpaque(false);
            this.metaLabel.setFont(this.metaLabel.getFont().deriveFont(0, 10.0f));
            this.metaLabel.setOpaque(false);
            jPanel.add(this.titleLabel);
            jPanel.add(this.descLabel);
            jPanel.add(this.metaLabel);
            this.add((Component)this.iconLabel, "West");
            this.add((Component)jPanel, "Center");
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ModrinthApi.ModResult> jList, ModrinthApi.ModResult modResult, int n, boolean bl, boolean bl2) {
            this.titleLabel.setText(modResult.title);
            String string = modResult.description == null ? "" : modResult.description;
            this.descLabel.setText((String)(string.length() > 60 ? string.substring(0, 60) + "\u2026" : string));
            this.metaLabel.setText(ShaderPanel.formatDownloads(modResult.downloads) + "indirme  |  shader");
            this.iconLabel.setIcon(ShaderPanel.iconFor(modResult.iconUrl, jList));
            ShaderPanel.colorize(this, bl, this.titleLabel, this.descLabel, this.metaLabel);
            return this;
        }
    }

    private static class InstalledShaderRenderer
    extends JPanel
    implements ListCellRenderer<Object> {
        private final JLabel nameLabel = new JLabel();

        InstalledShaderRenderer() {
            this.setLayout(new BorderLayout(10, 0));
            this.setBorder(new EmptyBorder(6, 12, 6, 12));
            this.nameLabel.setFont(this.nameLabel.getFont().deriveFont(0, 13.0f));
            this.add((Component)this.nameLabel, "Center");
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Object> jList, Object object, int n, boolean bl, boolean bl2) {
            String string = String.valueOf(object);
            if (string.toLowerCase().endsWith(".zip")) {
                string = string.substring(0, string.length() - 4);
            }
            this.nameLabel.setText(string);
            // TEMA BUG FIXI: sabit (31,63,120) mavi secim rengi yerine temanin
            // vurgu koyusu - her temada dogru gorunur.
            this.setBackground(bl ? Theme.ACCENT_DARK : Theme.BG_BASE);
            this.setOpaque(true);
            if (string.contains("Henuz")) {
                this.nameLabel.setFont(this.nameLabel.getFont().deriveFont(2, 12.0f));
                this.nameLabel.setForeground(Theme.TEXT_MUTED);
            } else {
                this.nameLabel.setFont(this.nameLabel.getFont().deriveFont(0, 13.0f));
                this.nameLabel.setForeground(Theme.TEXT_PRIMARY);
            }
            return this;
        }
    }
}

