/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.ui;

import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.mods.ModrinthApi;
import com.lubv.launcher.mods.ResourcepackManager;
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

public class ResourcepacksPanel
extends JPanel {
    private static final int ICON_SIZE = 36;
    private static final Map<String, ImageIcon> ICON_CACHE = Collections.synchronizedMap(new HashMap());
    private static final Set<String> ICON_LOADING = Collections.synchronizedSet(new HashSet());
    private static final ImageIcon PLACEHOLDER = ResourcepacksPanel.makePlaceholder();
    private final JTextField searchField = new JTextField();
    private GalleryStrip galleryStrip;
    private final DefaultListModel<ModrinthApi.ModResult> searchResultsModel = new DefaultListModel();
    private final JList<ModrinthApi.ModResult> searchResultsList = new JList<ModrinthApi.ModResult>(this.searchResultsModel);
    private final DefaultListModel<Object> installedModel = new DefaultListModel();
    private final JList<Object> installedList = new JList<Object>(this.installedModel);
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JLabel progressLabel = new JLabel(" ");
    private final Supplier<File> dirSupplier;
    private final Consumer<String> logConsumer;
    private static final ExecutorService ICON_POOL = Executors.newFixedThreadPool(3, runnable -> {
        Thread thread = new Thread(runnable, "icon-load");
        thread.setDaemon(true);
        return thread;
    });

    private JLayeredPane installedRightStack;
    private JPanel installedSideWrap;
    private JPanel detailOverlay;
    // ModsPanel'deki bilgi panelinin paylasilan bileşeni (ceviri + tam
    // aciklama + link + ikon icinde).
    private DetailInfoPanel detailInfo;

    public ResourcepacksPanel(Supplier<File> supplier, Consumer<String> consumer) {
        this.dirSupplier = supplier;
        this.logConsumer = consumer;
        this.setLayout(new BorderLayout(0, 0));
        this.setOpaque(true);
        this.setBackground(Theme.BG_BASE);
        JPanel jPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        jPanel.setOpaque(true);
        jPanel.setBackground(Theme.BG_BASE);
        jPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        jPanel.add(this.buildSearchSide());

        // Kurulu doku paketleri listesi de ModsPanel/ShaderPanel'deki
        // ayni overlay mekanizmasini kullanir: bir pakete cift
        // tiklaninca detay paneli listenin tamamini kaplar.
        this.installedSideWrap = new JPanel(new BorderLayout());
        this.installedSideWrap.setOpaque(false);
        this.installedSideWrap.add((Component)this.buildInstalledSide(), "Center");
        this.detailOverlay = this.buildDetailOverlay();
        this.detailOverlay.setVisible(false);
        this.installedRightStack = new JLayeredPane();
        this.installedRightStack.setOpaque(false);
        this.installedRightStack.add((Component)this.installedSideWrap, JLayeredPane.DEFAULT_LAYER);
        this.installedRightStack.add((Component)this.detailOverlay, JLayeredPane.PALETTE_LAYER);
        this.installedRightStack.addComponentListener(new java.awt.event.ComponentAdapter(){
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                ResourcepacksPanel.this.layoutInstalledRightStack();
            }
        });
        jPanel.add(this.installedRightStack);

        this.add((Component)jPanel, "Center");
        this.add((Component)this.buildProgressPane(), "South");
        this.refreshInstalled();
        // V34: Panel acilista populer doku paketleriyle dolu gelsin -
        // bos arama da ayni listeyi getirdigi icin ayni metodu cagir.
        this.doSearch();
        // V34: .zip doku paketlerini dogrudan panelin her yerine surukle-birak
        this.installZipDragAndDrop();
    }

    /** V34: .zip dosyalarini surukleyip birakarak doku paketi kur. */
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
                    File dir = ResourcepacksPanel.this.dirSupplier.get();
                    int ok = 0;
                    for (File f : files) {
                        if (f.getName().toLowerCase().endsWith(".zip")) {
                            java.nio.file.Files.copy(f.toPath(), new File(dir, f.getName()).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            ResourcepacksPanel.this.logConsumer.accept((en ? "Resource pack installed (dropped): " : "Doku paketi kuruldu (sürükle-birak): ") + f.getName());
                            ok++;
                        }
                    }
                    if (ok == 0) {
                        ResourcepacksPanel.this.logConsumer.accept(en ? "No .zip files found — only resource pack .zip files can be dropped here." : ".zip dosyasi bulunamadi — buraya sadece doku paketi .zip dosyalari sürüklenebilir.");
                    }
                    ResourcepacksPanel.this.refreshInstalled();
                    return true;
                }
                catch (Exception ex) {
                    ResourcepacksPanel.this.logConsumer.accept("Drag & drop hatasi: " + ex.getMessage());
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
        this.detailOverlay.setBounds(0, 0, w, h);
    }

    private JPanel buildDetailOverlay() {
        this.detailInfo = new DetailInfoPanel(() -> this.hideDetailOverlay());
        return this.detailInfo;
    }

    private void showDetailOverlay() {
        this.layoutInstalledRightStack();
        this.detailOverlay.setVisible(true);
        this.installedRightStack.moveToFront(this.detailOverlay);
        this.installedRightStack.revalidate();
        this.installedRightStack.repaint();
    }

    private void hideDetailOverlay() {
        this.detailOverlay.setVisible(false);
        this.installedRightStack.revalidate();
        this.installedRightStack.repaint();
    }

    /** Kurulu bir pakete cift tiklaninca cagrilir: Modrinth'ten tam bilgi ceker ve overlay'i acar. */
    private void loadInstalledDetail(String projectSlugOrId, String displayTitle) {
        this.detailInfo.open(displayTitle,
            com.lubv.launcher.core.L10n.isEnglish() ? "Loading\u2026" : "Y\u00fckleniyor\u2026",
            "https://modrinth.com/resourcepack/" + projectSlugOrId,
            projectSlugOrId, "RESOURCE PACK");
        this.showDetailOverlay();
    }

    /** Dosya adindan tahmini arama terimiyle Modrinth'te arar, ilk sonucun detayini gosterir. */
    private void loadInstalledDetailByGuess(String searchTerm, String fallbackTitle) {
        boolean en = com.lubv.launcher.core.L10n.isEnglish();
        this.detailInfo.open(fallbackTitle, en ? "Searching\u2026" : "Aran\u0131yor\u2026", null, null, "RESOURCE PACK");
        this.showDetailOverlay();
        ICON_POOL.submit(() -> {
            try {
                List<ModrinthApi.ModResult> results = ModrinthApi.searchResourcepacks(searchTerm);
                if (results.isEmpty()) {
                    SwingUtilities.invokeLater(() -> this.detailInfo.openLocal(fallbackTitle,
                        en ? "Not found on Modrinth. (Searched: " + searchTerm + ")" : "Bu paket Modrinth'te bulunamad\u0131. (Aranan: " + searchTerm + ")", "RESOURCE PACK"));
                    return;
                }
                ModrinthApi.ModResult best = results.get(0);
                SwingUtilities.invokeLater(() -> this.loadInstalledDetail(best.slug, best.title));
            }
            catch (Exception e) {
                SwingUtilities.invokeLater(() -> this.detailInfo.openLocal(fallbackTitle,
                    (en ? "Search error: " : "Arama hatas\u0131: ") + e.getMessage(), "RESOURCE PACK"));
            }
        });
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
        JPanel jPanel = ResourcepacksPanel.buildCard();
        jPanel.setLayout(new BorderLayout(8, 8));
        JLabel jLabel = new JLabel("Doku Paketi Ara");
        jLabel.setFont(new Font("SansSerif", 1, 14));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        JPanel jPanel2 = new JPanel(new BorderLayout(6, 0));
        jPanel2.setOpaque(false);
        this.searchField.putClientProperty("JTextField.placeholderText", com.lubv.launcher.core.L10n.isEnglish() ? "Type a resource pack name..." : "Doku paketi adi yazin...");
        this.searchField.setPreferredSize(new Dimension(0, 28));
        this.searchField.setMinimumSize(new Dimension(60, 28));
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
                ResourcepacksPanel.this.searchField.setText("");
                ResourcepacksPanel.this.doSearch();
            }
        });
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
        this.searchResultsList.setCellRenderer(new ResourcepackCellRenderer());
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
                this.loadInstalledDetail(modResult.slug, modResult.title);
            }
        });
        JPanel jPanel4 = new JPanel(new BorderLayout(0, 6));
        jPanel4.setOpaque(false);
        jPanel4.add((Component)new JScrollPane(this.searchResultsList), "Center");
        jPanel4.add((Component)this.galleryStrip, "South");
        jPanel.add((Component)jPanel4, "Center");
        JButton jButton2 = UiFx.accentButton(com.lubv.launcher.core.L10n.isEnglish() ? "Install Selected Pack" : "Secili Paketi Kur");
        jButton2.addActionListener(actionEvent -> this.installSelected());
        jPanel.add((Component)jButton2, "South");
        return jPanel;
    }

    /** V34: "Mod bilgisi oto acilsin" ayari - ResourcepacksPanel icin de gecerli. */
    private boolean infoAutoOpen() {
        try {
            return com.lubv.launcher.core.Settings.load().modInfoAutoOpen;
        }
        catch (Exception e) {
            return false;
        }
    }

    private JPanel buildInstalledSide() {
        JPanel jPanel = ResourcepacksPanel.buildCard();
        jPanel.setLayout(new BorderLayout(8, 8));
        boolean isEn = com.lubv.launcher.core.L10n.isEnglish();
        // V34: "Mod bilgisi oto acilsin" ayari - ResourcepacksPanel icin de gecerli.
        JLabel jLabel = new JLabel(isEn ? "Installed Resource Packs" : "Kurulu Doku Paketleri");
        jLabel.setFont(jLabel.getFont().deriveFont(1, 13.0f));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        jPanel.add((Component)jLabel, "North");
        this.installedList.setCellRenderer(new InstalledResourcepackRenderer());
        this.installedList.setFixedCellHeight(44);
        this.installedList.setSelectionMode(0);
        // DUZELTME: kurulu pakete cift tiklayinca bilgi paneli hic acilmiyordu
        // (mouse listener hic baglanmamisti). ModsPanel/ShaderPanel ile ayni
        // davranis eklendi.
        this.installedList.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() < 2) {
                    return;
                }
                Object selected = ResourcepacksPanel.this.installedList.getSelectedValue();
                if (selected == null) {
                    return;
                }
                String fileName = selected.toString();
                // once kalici meta kaydina bak (tam isabet), yoksa tahmin et.
                com.lubv.launcher.mods.InstallMeta.Entry meta = com.lubv.launcher.mods.InstallMeta.get(ResourcepacksPanel.this.dirSupplier.get(), fileName);
                if (meta != null && meta.slug != null && !meta.slug.isBlank()) {
                    ResourcepacksPanel.this.loadInstalledDetail(meta.slug, meta.title != null ? meta.title : fileName);
                    return;
                }
                String searchTerm = fileName.replaceAll("\\.(zip|jar)$", "").replaceAll("[_\\-\\.]", " ").replaceAll("\\d+\\.\\d+(\\.\\d+)?", "").trim();
                ResourcepacksPanel.this.loadInstalledDetailByGuess(searchTerm, fileName);
            }
        });
        jPanel.add((Component)new JScrollPane(this.installedList), "Center");

        // --- Bilgi bolumu: klasor yolu, kurulu paket sayisi ve otomatik
        // kurulacak doku paketleri (modlar sekmesindeki bilgi bolumu gibi). ---
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, 1));
        JLabel infoTitle = new JLabel(isEn ? "\u2139 Resource Pack Info" : "\u2139 Doku Paketi Bilgisi");
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
        Runnable refreshInfo = () -> {
            File dir = this.dirSupplier.get();
            int count = 0;
            if (dir != null && dir.isDirectory()) {
                File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".zip"));
                count = files != null ? files.length : 0;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(isEn ? "Folder: " : "Klasor: ").append(dir != null ? dir.getAbsolutePath() : "?").append("\n");
            sb.append(isEn ? "Installed resource packs: " : "Kurulu doku paketi: ").append(count).append("\n");
            java.util.List<String> autoRps = MainWindow.listCustomAutoModsOfType("resourcepack");
            if (!autoRps.isEmpty()) {
                sb.append(isEn ? "Auto-install packs: " : "Otomatik kurulacak paketler: ").append(String.join(", ", autoRps));
            } else {
                sb.append(isEn ? "No auto-install packs configured (Settings tab)." : "Otomatik kurulacak paket ayarlanmamis (Ayarlar sekmesi).");
            }
            infoArea.setText(sb.toString());
        };
        refreshInfo.run();
        this.rpInfoRefresher = refreshInfo;

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
    private Runnable rpInfoRefresher;

    private void doSearch() {
        String string = this.searchField.getText().trim();
        // V34: bos arama = populer doku paketleri (Modrinth varsayilana gore sirali)
        this.searchResultsModel.clear();
        new Thread(() -> {
            try {
                List<ModrinthApi.ModResult> list = ModrinthApi.searchResourcepacks(string);
                SwingUtilities.invokeLater(() -> {
                    for (ModrinthApi.ModResult modResult : list) {
                        this.searchResultsModel.addElement(modResult);
                    }
                    if (list.isEmpty()) {
                        this.logConsumer.accept("Doku paketi bulunamad\u0131: " + string);
                    }
                });
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.logConsumer.accept("Doku paketi arama hatas\u0131: " + exception.getMessage()));
            }
        }).start();
    }

    private void installSelected() {
        ModrinthApi.ModResult modResult = this.searchResultsList.getSelectedValue();
        if (modResult == null) {
            return;
        }
        File file = this.dirSupplier.get();
        new Thread(() -> {
            try {
                List<ModrinthApi.ModVersion> list = ModrinthApi.getVersions(modResult.id, null, null);
                if (list.isEmpty()) {
                    SwingUtilities.invokeLater(() -> this.logConsumer.accept(" " + modResult.title + "i\u00e7in indirilecek s\u00fcr\u00fcm bulunamad\u0131"));
                    return;
                }
                ModrinthApi.ModVersion modVersion = ModrinthApi.pickBestVersion(list);
                this.setProgress(0, "Doku paketi indiriliyor: " + modResult.title);
                ResourcepackManager.install(file, modVersion, (l, l2) -> this.setProgress(l2 > 0L ? (int)(l * 100L / l2) : -1, "Doku paketi indiriliyor: " + modResult.title));
                // Kurulum meta'sini kaydet: cift tiklayinca TAM isabetli detay.
                com.lubv.launcher.mods.InstallMeta.Entry meta = new com.lubv.launcher.mods.InstallMeta.Entry();
                meta.slug = modResult.slug;
                meta.title = modResult.title;
                meta.iconUrl = modResult.iconUrl;
                meta.source = "modrinth";
                com.lubv.launcher.mods.InstallMeta.put(file, modVersion.fileName, meta);
                SwingUtilities.invokeLater(() -> {
                    this.logConsumer.accept("Doku paketi kuruldu: " + modResult.title + " (" + modVersion.versionNumber + ")");
                    this.refreshInstalled();
                });
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.logConsumer.accept("Doku paketi kurulamad\u0131: " + exception.getMessage()));
            }
        }).start();
    }

    private void removeSelected() {
        Object object = this.installedList.getSelectedValue();
        if (object == null || !(object instanceof String)) {
            return;
        }
        String string = (String)object;
        // BUG DUZELTMESI: placeholder satiri (bos liste) dosya adi sanilmasin.
        if (!string.toLowerCase().endsWith(".zip")) {
            return;
        }
        boolean en = com.lubv.launcher.core.L10n.isEnglish();
        int n = JOptionPane.showConfirmDialog(this,
            en ? "Remove \"" + string + "\"?" : "\"" + string + "\" kaldirilsin mi?",
            en ? "Confirm" : "Onay", 0);
        if (n == 0) {
            ResourcepackManager.remove(this.dirSupplier.get(), string);
            com.lubv.launcher.mods.InstallMeta.remove(this.dirSupplier.get(), string);
            this.logConsumer.accept("Doku paketi kald\u0131r\u0131ld\u0131: " + string);
            this.refreshInstalled();
        }
    }

    private void openFolder() {
        File file = this.dirSupplier.get();
        file.mkdirs();
        try {
            Desktop.getDesktop().open(file);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void refreshInstalled() {
        ArrayList<String> arrayList = new ArrayList<String>(ResourcepackManager.listInstalled(this.dirSupplier.get()));
        Runnable runnable = () -> {
            this.installedModel.clear();
            for (String string : arrayList) {
                this.installedModel.addElement(string);
            }
            if (this.installedModel.isEmpty()) {
                this.installedModel.addElement(com.lubv.launcher.core.L10n.isEnglish()
                    ? "No resource packs yet. Add one from the search section."
                    : "Henuz doku paketi yok. Arama bolumunden paket ekleyin.");
            }
            // Bilgi bolumunu de guncelle.
            if (this.rpInfoRefresher != null) {
                this.rpInfoRefresher.run();
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
        graphics2D.setFont(new Font("SansSerif", 1, 22));
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
                    jList.setToolTipText("<html><b>" + ResourcepacksPanel.escapeHtml(modResult.title) + "</b><br>" + ResourcepacksPanel.escapeHtml(modResult.description) + "</html>");
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

    private static class ResourcepackCellRenderer
    extends JPanel
    implements ListCellRenderer<ModrinthApi.ModResult> {
        private final JLabel iconLabel = new JLabel();
        private final JLabel titleLabel = new JLabel();
        private final JLabel descLabel = new JLabel();
        private final JLabel metaLabel = new JLabel();

        ResourcepackCellRenderer() {
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
            this.metaLabel.setText(" " + ResourcepacksPanel.formatDownloads(modResult.downloads) + "   \u2022   doku paketi");
            this.iconLabel.setIcon(ResourcepacksPanel.iconFor(modResult.iconUrl, jList));
            ResourcepacksPanel.colorize(this, bl, this.titleLabel, this.descLabel, this.metaLabel);
            return this;
        }
    }

    private static class InstalledResourcepackRenderer
    extends JPanel
    implements ListCellRenderer<Object> {
        private final JLabel nameLabel = new JLabel();

        InstalledResourcepackRenderer() {
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
            this.setBackground(bl ? new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 60) : Theme.BG_BASE);
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

