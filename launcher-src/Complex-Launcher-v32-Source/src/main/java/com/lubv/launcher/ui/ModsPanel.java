/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.ui;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.L10n;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.mods.CurseForgeApi;
import com.lubv.launcher.mods.ModManager;
import com.lubv.launcher.mods.ModSearchHit;
import com.lubv.launcher.mods.ModrinthApi;
import com.lubv.launcher.ui.GalleryStrip;
import com.lubv.launcher.ui.Theme;
import com.lubv.launcher.ui.UiFx;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ModsPanel
extends JPanel {
    private static final int ICON_SIZE = 38;
    private static final Map<String, ImageIcon> ICON_CACHE = Collections.synchronizedMap(new HashMap());
    private static final Set<String> ICON_LOADING = Collections.synchronizedSet(new HashSet());
    private static final ImageIcon PLACEHOLDER = ModsPanel.makePlaceholder();
    private GalleryStrip galleryStrip;
    private final JTextField searchField;
    private final JToggleButton sourceToggle = new JToggleButton("Modrinth");
    private final DefaultListModel<ModSearchHit> resultsModel = new DefaultListModel();
    private final JList<ModSearchHit> resultsList = new JList<ModSearchHit>(this.resultsModel);
    private final JLabel filterChip = new JLabel();
    private final DefaultListModel<Object> installedModel = new DefaultListModel();
    private final JList<Object> installedList = new JList<Object>(this.installedModel);
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JLabel progressLabel = new JLabel(" ");
    /** Devam eden kurulumu iptal etmek icin: buton + bayrak + isci thread'i. */
    private javax.swing.JButton cancelInstallBtn;
    private volatile java.util.concurrent.atomic.AtomicBoolean installCancelled;
    private volatile Thread installWorker;
    private final Supplier<String> loaderSupplier;
    private final Supplier<String> versionSupplier;
    private final Supplier<File> modsDirSupplier;
    private final Supplier<String> apiKeySupplier;
    private final Consumer<String> log;
    private JPanel detailSidePanel;
    private boolean detailVisible = false;
    private static final ExecutorService ICON_POOL = Executors.newFixedThreadPool(3, runnable -> {
        Thread thread = new Thread(runnable, "icon-load");
        thread.setDaemon(true);
        return thread;
    });
    private static final String[] SURPRISE_QUERIES = new String[]{"adventure", "magic", "tech", "storage", "decoration", "farming", "rpg", "exploration", "utility", "combat", "building", "automation", "food", "dimension", "creatures", "transportation", "economy", "quests"};
    private JButton modInfoBtn;
    // Modspanel'in kendi bilgi paneli de artık DİGER SEKMELERLE AYNI
    // paylaşılan bileşendir (DetailInfoPanel) - 4 sekmede pixel-pixel
    // aynı yapı ve aynı dil-duyarlı çeviri davranışı.
    private DetailInfoPanel sideDetail;
    private JButton sideInstallBtn = UiFx.accentButton(com.lubv.launcher.core.L10n.isEnglish() ? "Install" : "Kur");
    private String sideCurrentProjectId = null;
    /** "Mod bilgisi oto acilsin" ayari: her secimde Settings'ten taze okunur. */
    private boolean modInfoAutoOpen() {
        try {
            return com.lubv.launcher.core.Settings.load().modInfoAutoOpen;
        } catch (Exception e) {
            return false;
        }
    }

    // Sag kolonu barindiran katmanli panel: kurulu modlar listesi altta
    // tam boyutta durur, detay paneli acildiginda ustune TAM olarak
    // binerek butun alani kaplar (eskiden sabit 290px genislikte, sikismis
    // bir yan panel olarak sadece bir kismi kapliyordu).
    private JLayeredPane rightStack;
    private JPanel installedWrap;

    public ModsPanel(Supplier<String> supplier, Supplier<String> supplier2, Supplier<File> supplier3, Supplier<String> supplier4, Consumer<String> consumer) {
        this.loaderSupplier = supplier;
        this.versionSupplier = supplier2;
        this.modsDirSupplier = supplier3;
        this.apiKeySupplier = supplier4;
        this.log = consumer;
        this.searchField = UiFx.searchField(L10n.isEnglish() ? "Search mods\u2026" : "Mod ara\u2026");
        this.setLayout(new BorderLayout(0, 0));
        this.setOpaque(true);
        this.setBackground(Theme.BG_BASE);

        this.installedWrap = new JPanel(new BorderLayout());
        this.installedWrap.setOpaque(false);
        this.installedWrap.add((Component)this.buildInstalledPanel(), "Center");

        this.detailSidePanel = this.buildSideDetailPanel();
        this.detailSidePanel.setVisible(false);

        this.rightStack = new JLayeredPane();
        this.rightStack.setOpaque(false);
        this.rightStack.add((Component)this.installedWrap, JLayeredPane.DEFAULT_LAYER);
        this.rightStack.add((Component)this.detailSidePanel, JLayeredPane.PALETTE_LAYER);
        this.rightStack.addComponentListener(new java.awt.event.ComponentAdapter(){
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                ModsPanel.this.layoutRightStack();
            }
        });

        JPanel jPanel2 = new JPanel(new GridLayout(1, 2, 12, 0));
        jPanel2.setOpaque(true);
        jPanel2.setBackground(Theme.BG_BASE);
        jPanel2.setBorder(new EmptyBorder(16, 16, 0, 16));
        jPanel2.add(this.buildSearchPanel());
        jPanel2.add(this.rightStack);
        this.add((Component)jPanel2, "Center");
        this.add((Component)this.buildProgressBar(), "South");
        this.refreshInstalled();
        this.updateFilterLabel();
        this.loadDefaultMods(this.loaderSupplier.get(), this.versionSupplier.get());
    }

    /** installedWrap ve detailSidePanel'i her zaman rightStack'in tam boyutuna oturtur. */
    private void layoutRightStack() {
        if (this.rightStack == null) {
            return;
        }
        int w = this.rightStack.getWidth();
        int h = this.rightStack.getHeight();
        if (this.installedWrap != null) {
            this.installedWrap.setBounds(0, 0, w, h);
        }
        if (this.detailSidePanel != null) {
            this.detailSidePanel.setBounds(0, 0, w, h);
        }
    }

    /** Detay panelini kurulu modlar alaninin TAMAMINI kaplayacak sekilde acar. */
    private void showDetailOverlay() {
        if (this.detailSidePanel == null) {
            return;
        }
        this.layoutRightStack();
        this.detailSidePanel.setVisible(true);
        this.rightStack.moveToFront(this.detailSidePanel);
        this.rightStack.revalidate();
        this.rightStack.repaint();
    }

    private void hideDetailOverlay() {
        if (this.detailSidePanel == null) {
            return;
        }
        this.detailSidePanel.setVisible(false);
        this.rightStack.revalidate();
        this.rightStack.repaint();
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
        // Iptal butonu: sadece kurulum/indirme surerken aktif. Indirme
        // HttpUtil'a AtomicBoolean bayragiyla gider, bir sonraki buffer
        // okumasinda kesilir ve yarim .part dosyasi silinir.
        JButton cancelBtn = new JButton(L10n.isEnglish() ? "Cancel" : "\u0130ptal");
        this.cancelInstallBtn = cancelBtn;
        cancelBtn.setFont(cancelBtn.getFont().deriveFont(0, 10.0f));
        cancelBtn.setMargin(new Insets(1, 8, 1, 8));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setEnabled(false);
        cancelBtn.setToolTipText(L10n.isEnglish() ? "Stop the running install/download" : "Devam eden kurulumu/indirmeyi durdurur");
        cancelBtn.addActionListener(actionEvent -> this.cancelInstall());
        JPanel progressEast = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        progressEast.setOpaque(false);
        progressEast.add(cancelBtn);
        jPanel.add((Component)progressEast, "East");
        return jPanel;
    }

    /** Iptal istegi: bayragi set eder, indirme bir sonraki okumada kesilir. */
    private void cancelInstall() {
        java.util.concurrent.atomic.AtomicBoolean c = this.installCancelled;
        if (c != null) {
            c.set(true);
        }
        this.setProgress(-1, L10n.isEnglish() ? "Cancelling\u2026" : "\u0130ptal ediliyor\u2026");
        this.log.accept(L10n.isEnglish() ? "Install cancelled by user." : "Kurulum kullanici tarafindan iptal edildi.");
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

    private JPanel buildSearchPanel() {
        JPanel jPanel = ModsPanel.buildCard();
        jPanel.setLayout(new BorderLayout(0, 12));
        JPanel jPanel2 = new JPanel(new BorderLayout(8, 4));
        jPanel2.setOpaque(true);
        jPanel2.setBackground(Theme.BG_SURFACE);
        JLabel jLabel = new JLabel("Mod Ara");
        jLabel.setFont(new Font("SansSerif", 1, 14));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        this.filterChip.setFont(this.filterChip.getFont().deriveFont(0, 11.0f));
        this.filterChip.setForeground(Theme.ACCENT_BRIGHT);
        this.filterChip.setOpaque(true);
        this.filterChip.setBackground(Theme.ACCENT_MUTED);
        this.filterChip.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        jPanel2.add((Component)jLabel, "West");
        jPanel2.add((Component)this.filterChip, "East");
        JPanel jPanel3 = new JPanel(new BorderLayout(6, 0));
        jPanel3.setOpaque(true);
        jPanel3.setBackground(Theme.BG_SURFACE);
        this.sourceToggle.setSelected(true);
        this.sourceToggle.addActionListener(actionEvent -> {
            this.sourceToggle.setText(this.sourceToggle.isSelected() ? "Modrinth" : "CurseForge");
            this.resultsList.clearSelection();
            this.resultsModel.clear();
            if (this.galleryStrip != null) {
                this.galleryStrip.clear();
            }
            if (!this.sourceToggle.isSelected() && this.searchField.getText().trim().isEmpty()) {
                this.loadDefaultMods(this.loaderSupplier.get(), this.versionSupplier.get());
            }
        });
        this.searchField.addActionListener(actionEvent -> this.doSearch());
        // V34 CANLI ARAMA: yazarken 450ms sustuktan sonra otomatik arar.
        // ESC temizler + populer listeye döner.
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
                ModsPanel.this.searchField.setText("");
                ModsPanel.this.doSearch();
            }
        });
        JButton jButton = UiFx.accentButton(com.lubv.launcher.core.L10n.isEnglish() ? "Search" : "Ara");
        jButton.addActionListener(actionEvent -> this.doSearch());
        JButton jButton2 = UiFx.ghostButton("Sasirt Beni");
        jButton2.addActionListener(actionEvent -> this.doSurprise());
        JPanel jPanel4 = new JPanel(new GridLayout(1, 2, 4, 0));
        jPanel4.setOpaque(true);
        jPanel4.setBackground(Theme.BG_SURFACE);
        jPanel4.add(jButton);
        jPanel4.add(jButton2);
        jPanel3.add((Component)this.sourceToggle, "West");
        jPanel3.add((Component)this.searchField, "Center");
        jPanel3.add((Component)jPanel4, "East");
        JPanel jPanel5 = new JPanel();
        jPanel5.setLayout(new BoxLayout(jPanel5, 1));
        jPanel5.setOpaque(true);
        jPanel5.setBackground(Theme.BG_SURFACE);
        jPanel2.setAlignmentX(0.0f);
        jPanel3.setAlignmentX(0.0f);
        jPanel5.add(jPanel2);
        jPanel5.add(Box.createVerticalStrut(8));
        jPanel5.add(jPanel3);
        this.resultsList.setCellRenderer(new SearchResultRenderer());
        this.resultsList.setFixedCellHeight(62);
        this.resultsList.setSelectionMode(0);
        this.resultsList.setBackground(Theme.BG_BASE);
        this.resultsList.setOpaque(true);
        this.resultsList.setBorder(BorderFactory.createEmptyBorder());
        this.installHoverTooltip(this.resultsList);
        this.resultsList.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger()) {
                    ModsPanel.this.showSearchMenu(mouseEvent);
                }
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger()) {
                    ModsPanel.this.showSearchMenu(mouseEvent);
                }
            }

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                // Double-click a search result = install it. The standard
                // interaction every other launcher uses; previously install
                // was hidden behind the bottom button / right-click menu only.
                if (mouseEvent.getClickCount() == 2 && !mouseEvent.isConsumed()) {
                    int idx = ModsPanel.this.resultsList.locationToIndex(mouseEvent.getPoint());
                    if (idx >= 0) {
                        ModsPanel.this.resultsList.setSelectedIndex(idx);
                        ModsPanel.this.installSelected();
                    }
                }
            }
        });
        JScrollPane jScrollPane = UiFx.cleanScroll(this.resultsList);
        JButton jButton3 = UiFx.accentButton(com.lubv.launcher.core.L10n.isEnglish() ? "Install Selected Mod" : "Secili Modu Kur");
        jButton3.addActionListener(actionEvent -> this.installSelected());
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
            // Detay paneli SADECE "mod bilgisi oto acilsin" ayari acikken
            // secimle otomatik acilir; kapaliyken kullanici butonla acar.
            if (this.detailSidePanel != null && ModsPanel.this.modInfoAutoOpen()) {
                this.showDetailOverlay();
                this.loadSearchHitSideDetail(modSearchHit);
            }
        });
        JPanel jPanel6 = new JPanel(new BorderLayout(0, 8));
        jPanel6.setOpaque(true);
        jPanel6.setBackground(Theme.BG_SURFACE);
        jPanel6.add((Component)jScrollPane, "Center");
        jPanel6.add((Component)this.galleryStrip, "South");
        jPanel.add((Component)jPanel5, "North");
        jPanel.add((Component)jPanel6, "Center");
        jPanel.add((Component)jButton3, "South");
        return jPanel;
    }

    private JPanel buildInstalledPanel() {
        JPanel jPanel = ModsPanel.buildCard();
        jPanel.setLayout(new BorderLayout(0, 12));
        JPanel jPanel2 = new JPanel(new BorderLayout(8, 0));
        jPanel2.setOpaque(false);
        JLabel jLabel = new JLabel(L10n.isEnglish() ? "Installed Mods" : "Kurulu Modlar");
        jLabel.setFont(new Font("SansSerif", 1, 14));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        this.modInfoBtn = new JButton(L10n.isEnglish() ? "Select a mod" : "Mod se\u00e7in"){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // TEMA BUG FIXI: sabit (139,92,246) moru yerine temanin vurgu rengi.
                Color ma = Theme.ACCENT != null ? Theme.ACCENT : new Color(139, 92, 246);
                graphics2D.setColor(new Color(ma.getRed(), ma.getGreen(), ma.getBlue(), 30));
                graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 10, 10);
                graphics2D.setColor(new Color(ma.getRed(), ma.getGreen(), ma.getBlue(), 160));
                graphics2D.setStroke(new BasicStroke(1.5f));
                graphics2D.drawRoundRect(1, 1, this.getWidth() - 2, this.getHeight() - 2, 10, 10);
                graphics2D.setColor(new Color(ma.getRed(), ma.getGreen(), ma.getBlue(), 40));
                graphics2D.setStroke(new BasicStroke(3.0f));
                graphics2D.drawRoundRect(0, 0, this.getWidth(), this.getHeight(), 12, 12);
                graphics2D.dispose();
                super.paintComponent(graphics);
            }
        };
        this.modInfoBtn.setOpaque(false);
        this.modInfoBtn.setContentAreaFilled(false);
        this.modInfoBtn.setBorderPainted(false);
        this.modInfoBtn.setFocusPainted(false);
        this.modInfoBtn.setForeground(Theme.ACCENT_BRIGHT != null ? Theme.ACCENT_BRIGHT : Theme.ACCENT);
        this.modInfoBtn.setFont(new Font("SansSerif", 1, 11));
        this.modInfoBtn.setCursor(Cursor.getPredefinedCursor(12));
        this.modInfoBtn.setToolTipText(L10n.isEnglish() ? "Click to open mod details panel" : "Mod bilgi panelini a\u00e7/kapat");
        this.modInfoBtn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        this.modInfoBtn.addActionListener(actionEvent -> {
            Object selected = this.installedList.getSelectedValue();
            if (selected == null) {
                return;
            }
            this.showDetailOverlay();
            this.loadSideDetail(selected);
        });
        jPanel2.add((Component)jLabel, "West");
        jPanel2.add((Component)this.modInfoBtn, "East");
        jPanel.add((Component)jPanel2, "North");
        this.installedList.setCellRenderer(new InstalledRenderer());
        this.installedList.setFixedCellHeight(52);
        this.installedList.setSelectionMode(2);
        this.installedList.setBackground(Theme.BG_BASE);
        this.installedList.setBorder(BorderFactory.createEmptyBorder());
        this.installHoverTooltip(this.installedList);
        this.installedList.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger()) {
                    ModsPanel.this.showInstalledMenu(mouseEvent);
                }
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger()) {
                    ModsPanel.this.showInstalledMenu(mouseEvent);
                }
            }
        });
        this.installedList.addListSelectionListener(listSelectionEvent -> {
            if (listSelectionEvent.getValueIsAdjusting()) {
                return;
            }
            Object object = this.installedList.getSelectedValue();
            if (this.modInfoBtn != null) {
                if (object instanceof ModManager.InstalledMod) {
                    Object object2;
                    ModManager.InstalledMod installedMod = (ModManager.InstalledMod)object;
                    Object object3 = object2 = installedMod.projectTitle != null ? installedMod.projectTitle : installedMod.fileName;
                    if (object2 != null && ((String)object2).length() > 22) {
                        object2 = ((String)object2).substring(0, 20) + "\u2026";
                    }
                    this.modInfoBtn.setText((String)(object2 != null ? object2 : "?"));
                } else {
                    this.modInfoBtn.setText(L10n.isEnglish() ? "Select a mod" : "Mod se\u00e7in");
                }
                this.modInfoBtn.repaint();
            }
            if (object == null) {
                return;
            }
            // Oto-acma ayari: mod secimi degistince detay paneli otomatik acilir.
            if (ModsPanel.this.modInfoAutoOpen() && object instanceof ModManager.InstalledMod) {
                ModsPanel.this.showDetailOverlay();
                ModsPanel.this.loadSideDetail(object);
            }
        });

        JPanel jPanel3 = new JPanel(new BorderLayout(0, 8));
        jPanel3.setOpaque(false);
        jPanel3.add((Component)UiFx.cleanScroll(this.installedList), "Center");

        jPanel.add((Component)jPanel3, "Center");
        this.installDragAndDrop(jPanel3);
        this.installDragAndDrop(this.installedList);
        JButton jButton = UiFx.ghostButton(L10n.isEnglish() ? "Toggle" : "A\u00e7/Kapat");
        JButton jButton2 = UiFx.dangerButton(L10n.isEnglish() ? "Remove" : "Kald\u0131r");
        JButton jButton3 = UiFx.ghostButton(L10n.isEnglish() ? "+ Local JAR" : "+ Yerel JAR");
        JButton jButton4 = UiFx.accentButton(L10n.isEnglish() ? "Update" : "G\u00fcncelle");
        jButton.addActionListener(actionEvent -> this.toggleSelected());
        jButton2.addActionListener(actionEvent -> this.removeSelected());
        jButton3.addActionListener(actionEvent -> this.addLocalJar());
        jButton4.setToolTipText("Kurulu modlari guncelleme icin kontrol et");
        jButton4.addActionListener(actionEvent -> this.checkModUpdates());
        JButton jButton5 = UiFx.ghostButton(L10n.isEnglish() ? "Select All" : "Hepsini Se\u00e7");
        jButton5.addActionListener(actionEvent -> {
            int n = this.installedModel.size();
            if (n > 0) {
                this.installedList.setSelectionInterval(0, n - 1);
            }
        });
        // V33 CAKISMA COZUCU: ayni bagimliligi farkli surumde isteyen mod
        // ciftlerini bulur, sicrama penceresiyle raporlar, tek tikla onerar.
        JButton conflictBtn = UiFx.ghostButton(L10n.isEnglish() ? "\u26a0 Conflicts" : "\u26a0 Cak\u0131\u015fma \u00c7\u00f6z");
        conflictBtn.setToolTipText(L10n.isEnglish() ? "Detect mods that need the same dependency at different versions" : "Ayn\u0131 ba\u011f\u0131ml\u0131l\u0131\u011f\u0131 farkl\u0131 s\u00fcr\u00fcmlerde isteyen modlar\u0131 bul");
        conflictBtn.addActionListener(actionEvent -> this.showConflictResolver());
        JPanel jPanel4 = new JPanel(new GridLayout(3, 2, 6, 6));
        jPanel4.setOpaque(true);
        jPanel4.setBackground(Theme.BG_SURFACE);
        jPanel4.add(jButton);
        jPanel4.add(jButton2);
        jPanel4.add(jButton3);
        jPanel4.add(jButton4);
        jPanel4.add(jButton5);
        jPanel4.add(conflictBtn);
        jPanel.add((Component)jPanel4, "South");
        return jPanel;
    }

    private JPanel buildSideDetailPanel() {
        // Paylasilan bileşen: diger sekmelerdekiyle birebir ayni panel.
        this.sideDetail = new DetailInfoPanel(() -> {
            this.hideDetailOverlay();
            this.sideCurrentProjectId = null;
        });
        // 4. hucre: Kur / Modrinth'te Ac aksiyonu.
        this.sideInstallBtn.addActionListener(actionEvent -> {
            ModSearchHit modSearchHit = this.resultsList.getSelectedValue();
            if (modSearchHit != null) {
                this.installSelected();
            } else {
                Object object = this.installedList.getSelectedValue();
                if (object instanceof ModManager.InstalledMod) {
                    Desktop desktop = Desktop.getDesktop();
                    if (this.sideCurrentProjectId != null) {
                        try {
                            desktop.browse(new URI("https://modrinth.com/mod/" + this.sideCurrentProjectId));
                        }
                        catch (Exception exception) {
                            this.log.accept("A\u00e7\u0131lamad\u0131: " + exception.getMessage());
                        }
                    }
                }
            }
        });
        this.sideDetail.setActionButton(this.sideInstallBtn);
        return this.sideDetail;
    }

    private void loadSideDetail(Object object) {
        if (!(object instanceof ModManager.InstalledMod)) {
            this.sideDetail.openLocal(com.lubv.launcher.core.L10n.isEnglish() ? "Select a mod" : "Mod se\u00e7in", "", "");
            this.sideInstallBtn.setVisible(false);
            return;
        }
        ModManager.InstalledMod installedMod = (ModManager.InstalledMod)object;
        this.sideCurrentProjectId = installedMod.projectId;
        boolean en = com.lubv.launcher.core.L10n.isEnglish();
        boolean isCf = installedMod.projectId != null && installedMod.projectId.startsWith("curseforge:");
        String displayTitle = installedMod.projectTitle != null ? installedMod.projectTitle : installedMod.fileName;
        String pageUrl = isCf
            ? "https://www.curseforge.com/minecraft/mc-mods/" + installedMod.projectId.replace("curseforge:", "")
            : (installedMod.projectId != null ? "https://modrinth.com/mod/" + installedMod.projectId : null);
        String slug = isCf || installedMod.projectId == null ? null : installedMod.projectId;
        this.sideDetail.open(displayTitle, en ? "Loading\u2026" : "Y\u00fckleniyor\u2026", pageUrl, slug,
            installedMod.enabled ? (en ? "ENABLED" : "ETK\u0130N") : (en ? "DISABLED" : "DEVRE DI\u015eI"));
        this.sideDetail.setMeta((installedMod.versionNumber != null ? "v" + installedMod.versionNumber : "") + "  \u00b7  " + installedMod.fileName);
        this.sideInstallBtn.setText(en ? "Open in Modrinth" : "Modrinth'te A\u00e7");
        this.sideInstallBtn.setVisible(true);
        // Govde + ikon DetailInfoPanel tarafindan Modrinth'ten cekilir.
    }

    private void loadSearchHitSideDetail(ModSearchHit modSearchHit) {
        if (modSearchHit == null) {
            return;
        }
        this.sideCurrentProjectId = modSearchHit.modrinth != null ? modSearchHit.modrinth.slug : null;
        boolean en = com.lubv.launcher.core.L10n.isEnglish();
        String pageUrl = modSearchHit.curseforge
            ? (modSearchHit.curseforgeMod != null ? "https://www.curseforge.com/minecraft/mc-mods?search=" + modSearchHit.curseforgeMod.id : null)
            : (modSearchHit.modrinth != null ? "https://modrinth.com/mod/" + modSearchHit.modrinth.slug : null);
        String slug = (!modSearchHit.curseforge && modSearchHit.modrinth != null) ? modSearchHit.modrinth.slug : null;
        this.sideDetail.open(modSearchHit.title != null ? modSearchHit.title : "",
            modSearchHit.description != null ? modSearchHit.description : "", pageUrl, slug,
            modSearchHit.curseforge ? "CURSEFORGE" : "MODRINTH");
        this.sideInstallBtn.setText(en ? "Install" : "Kur");
        this.sideInstallBtn.setVisible(true);
        // Tam aciklama + ikon DetailInfoPanel tarafindan cekilir.
    }

    private void openBrowser(String string) {
        try {
            Desktop.getDesktop().browse(new URI(string));
        }
        catch (Exception exception) {
            this.log.accept("Baglanti acilamadi: " + exception.getMessage());
        }
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

    private boolean isCurseForge() {
        return !this.sourceToggle.isSelected();
    }

    private String requireApiKey() {
        // Kullanicinin kendi anahtari varsa onu kullan. Yoksa null
        // donduruyoruz - CurseForgeApi.resolveApiKey bu durumda
        // launcher'a gomulu varsayilan anahtara (varsa) otomatik
        // duser, Prism Launcher/HMCL gibi diger launcher'larda oldugu
        // gibi. Boylece kullanicinin ayrica bir anahtar girmesi
        // GEREKMEZ; bu metot artik bir engel degil, sadece kullanicinin
        // ozel anahtarini iletmek icin var.
        String string = this.apiKeySupplier.get();
        return (string == null || string.isBlank()) ? null : string;
    }

    private void doSearch() {
        String string = this.searchField.getText().trim();
        this.updateFilterLabel();
        String string2 = this.loaderSupplier.get();
        String string3 = this.versionSupplier.get();
        boolean bl = this.isCurseForge();
        this.resultsModel.clear();
        if (this.galleryStrip != null) {
            this.galleryStrip.clear();
        }
        if (string.isEmpty() && !bl) {
            this.loadDefaultMods(string2, string3);
            return;
        }
        // V34: CurseForge'ta bos arama da populer modlari getirsin -
        // artık sessizce bos dondurmuyor (sortField=2 zaten indirme
        // sayisina gore sirali, query yalnizca filtre).
        if (string.isEmpty()) {
            final String loaderF = string2;
            final String verF = string3;
            final String key = this.requireApiKey();
            new Thread(() -> {
                try {
                    ArrayList<ModSearchHit> popular = new ArrayList<ModSearchHit>();
                    for (CurseForgeApi.ModResult modResult : CurseForgeApi.searchMods(key, "", loaderF, verF)) {
                        popular.add(ModSearchHit.from(modResult));
                    }
                    SwingUtilities.invokeLater(() -> {
                        for (ModSearchHit modSearchHit : popular) {
                            this.resultsModel.addElement(modSearchHit);
                        }
                        this.log.accept(popular.size() + (L10n.isEnglish() ? " popular mods loaded (CurseForge)." : " popüler mod yüklendi (CurseForge)."));
                    });
                }
                catch (Exception exception) {
                    SwingUtilities.invokeLater(() -> this.log.accept((L10n.isEnglish() ? "CurseForge popular list failed: " : "CurseForge popüler liste hatası: ") + exception.getMessage()));
                }
            }, "cf-default-load").start();
            return;
        }
        new Thread(() -> {
            try {
                ArrayList<ModSearchHit> arrayList = new ArrayList<ModSearchHit>();
                if (bl) {
                    // requireApiKey() null donerse bile devam ediyoruz -
                    // CurseForgeApi kendi icinde gomulu varsayilan anahtara
                    // (varsa) otomatik duser. Kullanicinin anahtar
                    // girmesi artik zorunlu degil.
                    String string4 = this.requireApiKey();
                    for (CurseForgeApi.ModResult modResult : CurseForgeApi.searchMods(string4, string, string2, string3)) {
                        arrayList.add(ModSearchHit.from(modResult));
                    }
                } else {
                    String string5 = string2 != null ? string2.toLowerCase() : "";
                    for (ModrinthApi.ModResult modResult : ModrinthApi.search(string, string5, string3)) {
                        arrayList.add(ModSearchHit.from(modResult));
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    for (ModSearchHit modSearchHit : arrayList) {
                        this.resultsModel.addElement(modSearchHit);
                    }
                    if (arrayList.isEmpty()) {
                        this.log.accept((L10n.isEnglish() ? "No results: " : "Sonu\u00e7 bulunamad\u0131: ") + string);
                    }
                });
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.log.accept("Arama hatas\u0131: " + exception.getMessage()));
            }
        }, "mod-search").start();
    }

    private void loadDefaultMods(String string, String string2) {
        String string3;
        String string4 = switch (string3 = string != null ? string.toLowerCase() : "") {
            case "fabric" -> "fabric";
            case "forge" -> "forge";
            case "neoforge" -> "neoforge";
            case "optifine" -> "forge";
            default -> "";
        };
        new Thread(() -> {
            try {
                List<ModrinthApi.ModResult> list = ModrinthApi.search("", string4, string2);
                if (list.isEmpty() && !string4.isEmpty()) {
                    list = ModrinthApi.search("", string4, null);
                }
                if (list.isEmpty()) {
                    list = ModrinthApi.search("", "", null);
                }
                List<ModrinthApi.ModResult> list2 = list;
                SwingUtilities.invokeLater(() -> {
                    this.resultsModel.clear();
                    for (ModrinthApi.ModResult modResult : list2) {
                        this.resultsModel.addElement(ModSearchHit.from(modResult));
                    }
                    if (!list2.isEmpty()) {
                        this.log.accept(list2.size() + (L10n.isEnglish() ? " popular mods loaded." : " pop\u00fcler mod y\u00fcklendi."));
                    }
                });
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.log.accept("Mod listesi y\u00fcklenemedi: " + exception.getMessage()));
            }
        }, "mod-default-load").start();
    }

    /**
     * Install-progress progress-bar bridge. Previously setProgress was only
     * driven by the update checker, so a manual install appeared frozen:
     * no bar, no status, just silence until "Kuruldu" or an error. These
     * helpers give every install step live feedback (0-100% during download,
     * indeterminate pulse during metadata lookups).
     */
    private void setInstallProgress(int percent, String message) {
        this.setProgress(percent, message);
    }

    private void setInstallProgressAsync(int percent, String message) {
        SwingUtilities.invokeLater(() -> this.setProgress(percent, message));
    }

    /** Human-readable byte counter for the progress label. */
    private static String fmtBytes(long v) {
        if (v >= 1048576L) return String.format("%.1f MB", v / 1048576.0);
        if (v >= 1024L) return String.format("%.0f KB", v / 1024.0);
        return v + " B";
    }

    private void installSelected() {
        ModSearchHit modSearchHit = this.resultsList.getSelectedValue();
        if (modSearchHit == null) {
            // Previously a silent return - clicking Kur with no selection looked
            // like "install is broken" because NOTHING happened, no message at
            // all. Now we tell the user what to do.
            this.log.accept(L10n.isEnglish()
                ? "Select a mod from the search results first, then click Install."
                : "Once kurulacak modu arama listesinden se\u00e7, sonra Kur'a bas.");
            this.setProgress(0, L10n.isEnglish() ? "No mod selected" : "Mod se\u00e7ilmedi");
            Timer t = new Timer(2500, ev -> this.setProgress(0, " "));
            t.setRepeats(false);
            t.start();
            return;
        }
        String string = this.loaderSupplier.get();
        String string2 = this.versionSupplier.get();
        File file = this.modsDirSupplier.get();
        String displayName = modSearchHit.title != null ? modSearchHit.title : "mod";
        // Kurulum takibi: iptal bayragi + buton durumu. finally ile her
        // yoldan (basari, hata, iptal) temizlenir.
        java.util.concurrent.atomic.AtomicBoolean cancelled = new java.util.concurrent.atomic.AtomicBoolean(false);
        this.installCancelled = cancelled;
        this.installWorker = null;
        if (this.cancelInstallBtn != null) {
            this.cancelInstallBtn.setEnabled(true);
        }
        this.setInstallProgressAsync(-1, (L10n.isEnglish() ? "Installing " : "Kuruluyor: ") + displayName + "\u2026");
        new Thread(() -> {
            this.installWorker = Thread.currentThread();
            try {
                if (modSearchHit.curseforge) {
                    this.installCurseForgeMod(modSearchHit.curseforgeMod, string, string2, file, cancelled);
                } else {
                    this.installModrinthMod(modSearchHit.modrinth, string, string2, file, cancelled);
                }
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> {
                    this.log.accept("Mod kurulamadi: " + exception.getMessage());
                    this.setProgress(0, (L10n.isEnglish() ? "Install failed: " : "Kurulum basarisiz: ") + exception.getMessage());
                    Timer t = new Timer(4000, ev -> this.setProgress(0, " "));
                    t.setRepeats(false);
                    t.start();
                });
            }
            finally {
                this.installCancelled = null;
                this.installWorker = null;
                SwingUtilities.invokeLater(() -> {
                    if (this.cancelInstallBtn != null) {
                        this.cancelInstallBtn.setEnabled(false);
                    }
                });
            }
        }, "mod-install").start();
    }

    private void installModrinthMod(ModrinthApi.ModResult modResult, String string, String string2, File file, java.util.concurrent.atomic.AtomicBoolean cancelled) throws Exception {
        try {
            this.setInstallProgressAsync(-1, (L10n.isEnglish() ? "Checking versions on Modrinth\u2026" : "Modrinth s\u00fcr\u00fcmleri kontrol ediliyor\u2026"));
            // Guvenlik katmani: id bos kaldiysa slug ile dene (Modrinth ikisini de kabul eder).
            String pid = (modResult.id != null && !modResult.id.isBlank()) ? modResult.id
                : (modResult.slug != null ? modResult.slug : "");
            if (pid.isBlank()) {
                SwingUtilities.invokeLater(() -> {
                    this.log.accept("Mod kimligi okunamadi (id + slug bos) - aramayi yenile ve tekrar dene.");
                    this.setProgress(0, L10n.isEnglish() ? "Broken search result" : "Bozuk arama sonucu");
                });
                return;
            }
            // game_versions facet'ine cop degeri gonderme (örn. "2.22"):
            // MC surumu gibi gorunmuyorsa surum filtresini dusur - boylece
            // en azindan modun bir surumu iner, "uyumlu surum bulunamadi"
            // yerine kurulmus olur.
            String safeVersion = ModrinthApi.looksLikeMcVersion(string2) ? string2 : null;
            List<ModrinthApi.ModVersion> list = ModrinthApi.getVersions(pid, string, safeVersion);
            if (list.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    this.log.accept(modResult.title + " i\u00e7in uyumlu s\u00fcr\u00fcm bulunamad\u0131");
                    this.setProgress(0, (L10n.isEnglish() ? "No compatible version" : "Uyumlu s\u00fcr\u00fcm yok"));
                });
                return;
            }
            String string3 = string2;
            ArrayList<ModrinthApi.ModVersion> arrayList = new ArrayList<ModrinthApi.ModVersion>();
            if (safeVersion != null) {
                for (ModrinthApi.ModVersion object2 : list) {
                    if (object2.gameVersions == null || !object2.gameVersions.contains(string3)) continue;
                    arrayList.add(object2);
                }
            }
            // V29.7 SIKI SURUM GUVENLIGI: filtreli liste bossa artik TAM
            // LISTEDEN secip yanlis MC surumunu sessizce kurmuyoruz - o MC
            // surumu icin surum YOK demektir ve net hata veriyoruz. Eski
            // davranis (arrayList.isEmpty() ? list : arrayList) Sodium 26.2
            // istenirken 1.21.11 jar'i indiriyordu.
            if (safeVersion != null && arrayList.isEmpty()) {
                // V29.7.1: "yol goster" - en yakin desteklenen MC surumunu bul,
                // tek secimlik onay ile kurulumu tamamla. Kullanici isterse ESC
                // ile iptal eder; yanlis surum yine ASLA sessiz kurulmaz.
                ModrinthApi.ModVersion nearest = null;
                int nearestGap = Integer.MAX_VALUE;
                for (ModrinthApi.ModVersion cand : list) {
                    if (cand.gameVersions == null) continue;
                    for (String gv : cand.gameVersions) {
                        int gap = ModrinthApi.versionGap(safeVersion, gv);
                        if (gap >= 0 && gap < nearestGap) {
                            nearestGap = gap;
                            nearest = cand;
                        }
                    }
                }
                if (nearest != null && !nearest.gameVersions.isEmpty()) {
                    String nearestMc = nearest.gameVersions.iterator().next();
                    final ModrinthApi.ModVersion fNearest = nearest;
                    final boolean[] installNearest = {false};
                    final String fTitle = modResult.title;
                    final String fNearestMc = nearestMc;
                    final String fWantVer = safeVersion;
                    SwingUtilities.invokeAndWait(() -> {
                        String msg = L10n.isEnglish()
                            ? "<html><b>" + fTitle + "</b> has no build for MC " + fWantVer + ".<br>Closest supported: <b>MC " + fNearestMc + "</b><br><br>Install that version instead?</html>"
                            : "<html><b>" + fTitle + "</b> icin MC " + fWantVer + " surumu yok.<br>En yakin desteklenen: <b>MC " + fNearestMc + "</b><br><br>Yine de o surumu kurmak ister misin?</html>";
                        int c = JOptionPane.showConfirmDialog(this, msg,
                            L10n.isEnglish() ? "No version for this MC" : "Bu MC surumu icin surum yok",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        installNearest[0] = c == JOptionPane.YES_OPTION;
                    });
                    if (!installNearest[0]) {
                        final String fT = modResult.title;
                        SwingUtilities.invokeLater(() -> {
                            this.log.accept(fT + " kurulmadi - MC " + fWantVer + " icin surum yok (kullanici vazgecmisti). Hicbir sey kurulmadi.");
                            this.setProgress(0, (L10n.isEnglish() ? "Not installed" : "Kurulmadi"));
                            Timer t = new Timer(3000, ev -> this.setProgress(0, " "));
                            t.setRepeats(false);
                            t.start();
                        });
                        return;
            }
                    // Kullanici en yakin surumu secirse installWithDependencies'i
                    // o MC surumuyle calistir (siki gecitler onu da dogrular).
                    safeVersion = nearestMc;
                    string2 = nearestMc;
                    // Yeniden filtrele: pickBestVersion simdedi en az bir
                    // aday bulacagi garantili (nearest bu surumu destekliyor).
                    arrayList.clear();
                    for (ModrinthApi.ModVersion object2 : list) {
                        if (object2.gameVersions != null && object2.gameVersions.contains(nearestMc)) arrayList.add(object2);
                    }
                } else {
                    final String wantVer = safeVersion;
                    final String modTitle = modResult.title;
                    SwingUtilities.invokeLater(() -> {
                        this.log.accept(modTitle + " icin hicbir MC surumu bulunamadi (MC " + wantVer + " dahil) - kurulum iptal.");
                        this.setProgress(0, (L10n.isEnglish() ? "No version at all" : "Hic surum yok"));
                        Timer t = new Timer(4000, ev -> this.setProgress(0, " "));
                        t.setRepeats(false);
                        t.start();
                    });
                    return;
                }
            }
            ModrinthApi.ModVersion modVersion = ModrinthApi.pickBestVersion(arrayList.isEmpty() ? list : arrayList, safeVersion);
            if (modVersion == null || modVersion.fileName == null || modVersion.downloadUrl == null) {
                SwingUtilities.invokeLater(() -> {
                    this.log.accept(modResult.title + " i\u00e7in uygun s\u00fcr\u00fcm bulunamad\u0131");
                    this.setProgress(0, (L10n.isEnglish() ? "No suitable version" : "Uygun s\u00fcr\u00fcm yok"));
                });
                return;
            }
            // TAM BAGIMLILIK ZINCIRI: ana mod + tum required deps TEK KOD
            // YOLUNDA kurulur (ModManager.installWithDependencies): SHA-1
            // dogrulamali indirme, iptal, registry, recursive dep cozumleme.
            // Eskiden panel sadece ana modu indiriyordu; fabric-api gibi
            // zorunlu bagimliliklar hic kurulmuyordu -> mod oyunda yuklenmezdi.
            this.setInstallProgressAsync(-1, (L10n.isEnglish() ? "Installing " : "Kuruluyor: ") + modResult.title + "\u2026");
            ModManager.InstallResult res = ModManager.installWithDependencies(file, modResult, modVersion, string, string2, (pct, stage) -> {
                if (pct >= 0) {
                    this.setInstallProgressAsync(Math.min(99, pct), stage);
                } else {
                    this.setInstallProgressAsync(-1, stage);
                }
            }, cancelled);
            SwingUtilities.invokeLater(() -> {
                for (String dep : res.installedDependencies) {
                    this.log.accept("Gerekli mod kuruldu: " + dep);
                }
                for (String dep : res.skippedDependencies) {
                    this.log.accept("UYARI - Bagimlilik surumu bulunamadi: " + dep);
                }
                this.log.accept("Kuruldu: " + modResult.title + " (" + modVersion.versionNumber + ")" + (res.installedDependencies.isEmpty() ? "" : " +" + res.installedDependencies.size() + " bagimlilik"));
                this.setProgress(100, (L10n.isEnglish() ? "Installed: " : "Kuruldu: ") + modResult.title);
                this.refreshInstalled();
                Timer t = new Timer(2000, ev -> this.setProgress(0, " "));
                t.setRepeats(false);
                t.start();
            });
        }
        catch (Exception exception) {
            final boolean wasCancelled = cancelled != null && cancelled.get();
            SwingUtilities.invokeLater(() -> {
                if (wasCancelled) {
                    this.log.accept("Kurulum iptal edildi.");
                    this.setProgress(0, L10n.isEnglish() ? "Cancelled" : "\u0130ptal edildi");
                    Timer t = new Timer(2500, ev -> this.setProgress(0, " "));
                    t.setRepeats(false);
                    t.start();
                } else {
                    this.log.accept("Mod kurulamadi: " + exception.getMessage());
                    this.setProgress(0, (L10n.isEnglish() ? "Install failed: " : "Kurulum basarisiz: ") + exception.getMessage());
                    Timer t = new Timer(4000, ev -> this.setProgress(0, " "));
                    t.setRepeats(false);
                    t.start();
                }
            });
        }
    }

    private void installCurseForgeMod(CurseForgeApi.ModResult modResult, String string, String string2, File file, java.util.concurrent.atomic.AtomicBoolean cancelled) throws Exception {
        try {
            this.setInstallProgressAsync(-1, (L10n.isEnglish() ? "Checking files on CurseForge\u2026" : "CurseForge dosyalari kontrol ediliyor\u2026"));
            String string3 = this.requireApiKey();
            // Cop MC surumu ("2.22" gibi) gameVersion filtresine girerse
            // hicbir dosya donmez - once filtreli dene, bos gelirse filtresiz tekrar dene.
            String safeCfVersion = ModrinthApi.looksLikeMcVersion(string2) ? string2 : null;
            List<CurseForgeApi.FileResult> list = CurseForgeApi.getFiles(string3, modResult.id, null, safeCfVersion);
            if (list.isEmpty() && safeCfVersion != null) {
                list = CurseForgeApi.getFiles(string3, modResult.id, null, null);
            }
            if (list.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    this.log.accept(modResult.name + " i\u00e7in uyumlu dosya bulunamad\u0131");
                    this.setProgress(0, (L10n.isEnglish() ? "No compatible file" : "Uyumlu dosya yok"));
                });
                return;
            }
            // V29.7: siki surum gecidi - MC surumu biliniyorsa baska surum
            // icin derlenmis dosya secilemez (pickBestCompatibleFile null
            // doner); sadece cop surum degerinde (safeCfVersion == null)
            // eski heuristik devreye girer.
            CurseForgeApi.FileResult fileResult = safeCfVersion != null
                ? CurseForgeApi.pickBestCompatibleFile(list, safeCfVersion)
                : CurseForgeApi.pickBestFile(list);
            if (fileResult == null) {
                SwingUtilities.invokeLater(() -> {
                    this.log.accept(modResult.name + " i\u00e7in uygun dosya bulunamad\u0131");
                    this.setProgress(0, (L10n.isEnglish() ? "No suitable file" : "Uygun dosya yok"));
                });
                return;
            }
            String string4 = CurseForgeApi.getDownloadUrl(string3, modResult.id, fileResult.id);
            if (string4 == null) {
                SwingUtilities.invokeLater(() -> {
                    this.log.accept(modResult.name + " indirme linki al\u0131namad\u0131");
                    this.setProgress(0, (L10n.isEnglish() ? "Could not get download link" : "\u0130ndirme linki al\u0131namad\u0131"));
                });
                return;
            }
            // TAM BAGIMLILIK ZINCIRI (CurseForge): dosya + tum required deps
            // ModManager.installCurseforgeWithDependencies ile kurulur.
            // Dep filtreli listede yoksa filtresiz liste + uyumluluk secimi
            // devreye girer; hala yoksa SKIPPED olarak raporlanir (sessiz degil).
            // V29.7 DUZELTME: yukleyici artik dep zincirine tasiyor - once
            // null geciriliyordu, dep dosyalari yukleyici filtresiz ariyordu.
            ModManager.CurseForgeResult res = ModManager.installCurseforgeWithDependencies(string3, file, modResult, fileResult, string, safeCfVersion, (pct, stage) -> {
                if (pct >= 0) {
                    this.setInstallProgressAsync(Math.min(99, pct), stage);
                } else {
                    this.setInstallProgressAsync(-1, stage);
                }
            }, cancelled);
            SwingUtilities.invokeLater(() -> {
                for (String dep : res.installedDependencies) {
                    this.log.accept("Gerekli mod kuruldu: " + dep);
                }
                for (String dep : res.skippedDependencies) {
                    this.log.accept("UYARI - Bagimlilik surumu bulunamadi: " + dep);
                }
                this.log.accept("Kuruldu: " + modResult.name);
                this.setProgress(100, (L10n.isEnglish() ? "Installed: " : "Kuruldu: ") + modResult.name);
                this.refreshInstalled();
                Timer t = new Timer(2000, ev -> this.setProgress(0, " "));
                t.setRepeats(false);
                t.start();
            });
        }
        catch (Exception exception) {
            final boolean wasCancelled = cancelled != null && cancelled.get();
            SwingUtilities.invokeLater(() -> {
                if (wasCancelled) {
                    this.log.accept("Kurulum iptal edildi.");
                    this.setProgress(0, L10n.isEnglish() ? "Cancelled" : "\u0130ptal edildi");
                    Timer t = new Timer(2500, ev -> this.setProgress(0, " "));
                    t.setRepeats(false);
                    t.start();
                } else {
                    this.log.accept("Mod kurulamadi: " + exception.getMessage());
                    this.setProgress(0, (L10n.isEnglish() ? "Install failed: " : "Kurulum basarisiz: ") + exception.getMessage());
                    Timer t = new Timer(4000, ev -> this.setProgress(0, " "));
                    t.setRepeats(false);
                    t.start();
                }
            });
        }
    }

    private void toggleSelected() {
        List<Object> list = this.installedList.getSelectedValuesList();
        if (list.isEmpty()) {
            return;
        }
        File file = this.modsDirSupplier.get();
        for (Object object : list) {
            if (!(object instanceof ModManager.InstalledMod)) continue;
            ModManager.InstalledMod installedMod = (ModManager.InstalledMod)object;
            ModManager.setEnabled(file, installedMod, !installedMod.enabled);
        }
        this.refreshInstalled();
    }

    private void removeSelected() {
        List<Object> list = this.installedList.getSelectedValuesList();
        if (list.isEmpty()) {
            return;
        }
        boolean bl = L10n.isEnglish();
        String singleName = null;
        if (list.size() == 1 && list.get(0) instanceof ModManager.InstalledMod) {
            ModManager.InstalledMod only = (ModManager.InstalledMod) list.get(0);
            singleName = only.projectTitle != null ? only.projectTitle : only.fileName;
        }
        String message = singleName != null
            ? (bl ? "Remove \"" + singleName + "\"?" : "\"" + singleName + "\" kald\u0131r\u0131ls\u0131n m\u0131?")
            : (bl ? "Remove " + list.size() + " mods?" : list.size() + " mod kald\u0131r\u0131ls\u0131n m\u0131?");
        if (JOptionPane.showConfirmDialog(this, message, bl ? "Confirm" : "Onay", 0) != 0) {
            return;
        }
        File file = this.modsDirSupplier.get();
        for (Object object4 : list) {
            if (!(object4 instanceof ModManager.InstalledMod)) continue;
            ModManager.InstalledMod installedMod = (ModManager.InstalledMod)object4;
            ModManager.removeMod(file, installedMod);
        }
        this.refreshInstalled();
    }

    private void showInstalledMenu(MouseEvent mouseEvent) {
        int n = this.installedList.locationToIndex(mouseEvent.getPoint());
        if (n >= 0 && !this.installedList.isSelectedIndex(n)) {
            this.installedList.setSelectedIndex(n);
        }
        JPopupMenu jPopupMenu = new JPopupMenu();
        jPopupMenu.setBackground(Theme.BG_ELEVATED);
        jPopupMenu.setBorder(BorderFactory.createLineBorder(Theme.BG_BORDER, 1));
        ModsPanel.addCtx(jPopupMenu, L10n.isEnglish() ? "Toggle Enabled" : "A\u00e7/Kapat", Theme.TEXT_PRIMARY, actionEvent -> this.toggleSelected());
        // TEMA BUG FIXI: sabit (220,60,60) yerine temanin RED'i.
        ModsPanel.addCtx(jPopupMenu, L10n.isEnglish() ? "Remove Selected" : "Se\u00e7ilenleri Kald\u0131r", Theme.RED != null ? Theme.RED : new Color(220, 60, 60), actionEvent -> this.removeSelected());
        jPopupMenu.addSeparator();
        ModsPanel.addCtx(jPopupMenu, L10n.isEnglish() ? "Select All" : "Hepsini Se\u00e7", Theme.TEXT_PRIMARY, actionEvent -> {
            int sz = this.installedModel.size();
            if (sz > 0) {
                this.installedList.setSelectionInterval(0, sz - 1);
            }
        });
        ModsPanel.addCtx(jPopupMenu, L10n.isEnglish() ? "Remove ALL" : "T\u00fcm\u00fcn\u00fc Kald\u0131r", Theme.RED != null ? Theme.RED : new Color(220, 60, 60), actionEvent -> {
            int sz;
            boolean bl = L10n.isEnglish();
            if (JOptionPane.showConfirmDialog(this, bl ? "Remove ALL mods?" : "T\u00fcm modlar kald\u0131r\u0131ls\u0131n m\u0131?", bl ? "Remove All" : "Hepsini", 0, 2) == 0 && (sz = this.installedModel.size()) > 0) {
                this.installedList.setSelectionInterval(0, sz - 1);
                this.removeSelected();
            }
        });
        jPopupMenu.show(this.installedList, mouseEvent.getX(), mouseEvent.getY());
    }

    private void showSearchMenu(MouseEvent mouseEvent) {
        int n = this.resultsList.locationToIndex(mouseEvent.getPoint());
        if (n < 0) {
            return;
        }
        this.resultsList.setSelectedIndex(n);
        ModSearchHit modSearchHit = this.resultsModel.getElementAt(n);
        if (modSearchHit == null) {
            return;
        }
        JPopupMenu jPopupMenu = new JPopupMenu();
        jPopupMenu.setBackground(Theme.BG_ELEVATED);
        jPopupMenu.setBorder(BorderFactory.createLineBorder(Theme.BG_BORDER, 1));
        JMenuItem jMenuItem = new JMenuItem(L10n.isEnglish() ? "Install" : "Kur");
        jMenuItem.setBackground(Theme.BG_ELEVATED);
        jMenuItem.setForeground(Theme.ACCENT_BRIGHT);
        jMenuItem.setFont(jMenuItem.getFont().deriveFont(1));
        jMenuItem.addActionListener(actionEvent -> this.installSelected());
        jPopupMenu.add(jMenuItem);
        jPopupMenu.addSeparator();
        boolean bl = modSearchHit.modrinth != null && modSearchHit.modrinth.slug != null;
        JMenuItem jMenuItem2 = new JMenuItem(L10n.isEnglish() ? "Add to Auto-Install" : "Otomatik Listeye Ekle");
        jMenuItem2.setBackground(Theme.BG_ELEVATED);
        jMenuItem2.setForeground(bl ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED);
        jMenuItem2.setEnabled(bl);
        if (bl) {
            jMenuItem2.addActionListener(actionEvent -> this.addToAutoInstallList(modSearchHit.modrinth.slug, modSearchHit.title));
        }
        jPopupMenu.add(jMenuItem2);
        jPopupMenu.show(this.resultsList, mouseEvent.getX(), mouseEvent.getY());
    }

    private static void addCtx(JPopupMenu jPopupMenu, String string, Color color, ActionListener actionListener) {
        JMenuItem jMenuItem = new JMenuItem(string);
        jMenuItem.setBackground(Theme.BG_ELEVATED);
        jMenuItem.setForeground(color);
        jMenuItem.addActionListener(actionListener);
        jPopupMenu.add(jMenuItem);
    }

    private void addToAutoInstallList(String string, String string2) {
        try {
            Closeable closeable;
            File file = new File(Paths.GAME_DIR, "mod_prefs.json");
            JsonObject jsonObject = new JsonObject();
            if (file.exists()) {
                closeable = new FileReader(file);
                try {
                    JsonElement jsonElement = JsonParser.parseReader((Reader)closeable);
                    if (jsonElement.isJsonObject()) {
                        jsonObject = jsonElement.getAsJsonObject();
                    }
                }
                finally {
                    ((InputStreamReader)closeable).close();
                }
            }
            jsonObject.addProperty(string, true);
            file.getParentFile().mkdirs();
            closeable = new FileWriter(file);
            try {
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)jsonObject, (Appendable)((Object)closeable));
            }
            finally {
                ((OutputStreamWriter)closeable).close();
            }
            this.log.accept("\u2713 " + string2 + " otomatik listeye eklendi (" + string + ")");
            JOptionPane.showMessageDialog(this, L10n.isEnglish() ? string2 + " added. Key: " + string : string2 + " eklendi. Anahtar: " + string, "Auto-Install", 1);
        }
        catch (Exception exception) {
            this.log.accept("[HATA] Otomatik listeye eklenemedi: " + exception.getMessage());
        }
    }

    private void addLocalJar() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileFilter(new FileNameExtensionFilter("JAR", "jar"));
        jFileChooser.setMultiSelectionEnabled(true);
        if (jFileChooser.showOpenDialog(this) == 0) {
            this.installLocalJars(jFileChooser.getSelectedFiles());
        }
    }

    private void installLocalJars(File[] fileArray) {
        if (fileArray == null || fileArray.length == 0) {
            return;
        }
        File file = this.modsDirSupplier.get();
        file.mkdirs();
        int n = 0;
        int n2 = 0;
        for (File file2 : fileArray) {
            if (file2 == null || !file2.isFile() || !file2.getName().toLowerCase().endsWith(".jar")) {
                ++n2;
                continue;
            }
            try {
                ModManager.installedManualJar(file, file2);
                ++n;
            }
            catch (Exception exception) {
                String string = file2.getName();
                String string2 = exception.getMessage();
                SwingUtilities.invokeLater(() -> this.log.accept("Eklenemedi (" + string + "): " + string2));
            }
        }
        int n3 = n;
        int n4 = n2;
        SwingUtilities.invokeLater(() -> {
            if (n3 > 0) {
                this.log.accept(n3 + " yerel mod eklendi" + (String)(n4 > 0 ? " (" + n4 + " dosya .jar olmadigi icin atlandi)" : ""));
                this.refreshInstalled();
            } else if (n4 > 0) {
                this.log.accept("Hicbir .jar dosyasi bulunamadi \u2014 sadece .jar dosyalarini suruklebilirsiniz.");
            }
        });
    }

    private void installDragAndDrop(JComponent jComponent) {
        jComponent.setTransferHandler(new TransferHandler(){

            @Override
            public boolean canImport(TransferHandler.TransferSupport transferSupport) {
                return transferSupport.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport transferSupport) {
                if (!this.canImport(transferSupport)) {
                    return false;
                }
                try {
                    List<File> list = (List<File>)transferSupport.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    ModsPanel.this.installLocalJars(list.toArray(new File[0]));
                    return true;
                }
                catch (Exception exception) {
                    SwingUtilities.invokeLater(() -> ModsPanel.this.log.accept("Surukle-birak hatasi: " + exception.getMessage()));
                    return false;
                }
            }
        });
    }

    /**
     * V33 CAKISMA COZUCU: kurulu modlari tarar, ayni bagimlilik kimligini
     * farkli surumlerde isteyen ciftleri bulur ve oneri sunar. Tarama thread'de
     * (jar metadata okuma disk IO), raporlama EDT'de. Tek tikla: eski surumu
     * sil + sicrama penceresini yenile.
     */
    private void showConflictResolver() {
        final File modsDir = this.modsDirSupplier.get();
        this.setProgress(-1, L10n.isEnglish() ? "Scanning installed mods for conflicts\u2026" : "Kurulu modlar \u00e7ak\u0131\u015fma i\u00e7in taran\u0131yor\u2026");
        Thread worker = new Thread(() -> {
            List<ModManager.DepConflict> conflicts;
            try {
                conflicts = ModManager.findDepVersionConflicts(modsDir);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    this.setProgress(0, " ");
                    JOptionPane.showMessageDialog(this, L10n.isEnglish() ? "Conflict scan failed: " : "\u00c7ak\u0131\u015fma taramas\u0131 ba\u015far\u0131s\u0131z: " + e.getMessage(), "\u26a0", 2);
                });
                return;
            }
            final List<ModManager.DepConflict> fConflicts = conflicts;
            SwingUtilities.invokeLater(() -> {
                this.setProgress(0, " ");
                if (fConflicts.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        L10n.isEnglish() ? "No dependency version conflicts found. All installed mods are compatible." : "\u00c7ak\u0131\u015fma bulunamad\u0131. Kurulu modlar\u0131n hepsi uyumlu.",
                        L10n.isEnglish() ? "\u2714 Conflict Check" : "\u2714 \u00c7ak\u0131\u015fma Kontrol\u00fc", 1);
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("<html><body style='width:420px'>");
                sb.append(L10n.isEnglish()
                    ? "<b>" + fConflicts.size() + " dependency conflict(s) found:</b><br><br>"
                    : "<b>" + fConflicts.size() + " \u00e7ak\u0131\u015fma bulundu:</b><br><br>");
                for (ModManager.DepConflict c : fConflicts) {
                    sb.append("\u2022 <b>").append(escapeHtml(c.provideId)).append("</b>: ")
                      .append(escapeHtml(c.title(c.modA))).append(" (").append(c.versionA == null ? "?" : c.versionA).append(") ")
                      .append(L10n.isEnglish() ? "vs" : "ile").append(" ")
                      .append(escapeHtml(c.title(c.modB))).append(" (").append(c.versionB == null ? "?" : c.versionB).append(")<br>")
                      .append("&nbsp;&nbsp;<font color='#88aa88'>")
                      .append(L10n.isEnglish() ? "Keep " : "Kalacak: ").append(escapeHtml(c.title(c.keepMod))).append(" (").append(c.keepVersion == null ? "?" : c.keepVersion).append(")")
                      .append("</font><br><br>");
                }
                sb.append(L10n.isEnglish()
                    ? "Resolve by keeping the NEWER version of each and removing the old jar?"
                    : "Her \u00e7ak\u0131\u015fmada YEN\u0130 s\u00fcr\u00fcm tutulup eski jar kald\u0131r\u0131lacak. \u00c7\u00f6z\u00fclsin mi?");
                sb.append("</body></html>");
                int choice = JOptionPane.showConfirmDialog(this, sb.toString(),
                    L10n.isEnglish() ? "\u26a0 Resolve Conflicts" : "\u26a0 \u00c7ak\u0131\u015fma \u00c7\u00f6z\u00fcc\u00fc",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice != 0) return;
                int resolved = 0;
                for (ModManager.DepConflict c : fConflicts) {
                    try {
                        ModManager.removeMod(modsDir, c.removeMod);
                        this.log.accept(L10n.isEnglish()
                            ? "Conflict resolved: removed " + c.title(c.removeMod) + " (" + c.removeVersion + "), kept " + c.title(c.keepMod) + " (" + c.keepVersion + ")"
                            : "\u00c7ak\u0131\u015fma \u00e7\u00f6z\u00fcld\u00fc: " + c.title(c.removeMod) + " (" + c.removeVersion + ") kald\u0131r\u0131ld\u0131, " + c.title(c.keepMod) + " (" + c.keepVersion + ") kald\u0131");
                        resolved++;
                    } catch (Exception e) {
                        this.log.accept("\u26a0 " + e.getMessage());
                    }
                }
                this.refreshInstalled();
                JOptionPane.showMessageDialog(this,
                    L10n.isEnglish() ? resolved + " conflict(s) resolved. Restart the game to apply." : resolved + " \u00e7ak\u0131\u015fma \u00e7\u00f6z\u00fcld\u00fc. Uygulamak i\u00e7in oyunu yeniden ba\u015flat.",
                    L10n.isEnglish() ? "\u2714 Resolved" : "\u2714 \u00c7\u00f6z\u00fcld\u00fc", 1);
            });
        }, "conflict-scan");
        worker.setDaemon(true);
        worker.start();
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public void refreshInstalled() {
        ArrayList<ModManager.InstalledMod> arrayList = new ArrayList<ModManager.InstalledMod>(ModManager.loadRegistry(this.modsDirSupplier.get()));
        Runnable runnable = () -> {
            this.installedModel.clear();
            for (ModManager.InstalledMod installedMod : arrayList) {
                this.installedModel.addElement(installedMod);
            }
            if (this.installedModel.isEmpty()) {
                this.installedModel.addElement(this.createEmptyPlaceholder("Henuz mod yok. Yukaridaki Arama bolumunden mod ekleyin."));
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    /** Ctrl+F: arama kutusuna odaklan (MainWindow kisayolu cagirir). */
    public void focusSearch() {
        this.searchField.requestFocusInWindow();
        this.searchField.selectAll();
    }

    private ModManager.InstalledMod createEmptyPlaceholder(String string) {
        ModManager.InstalledMod installedMod = new ModManager.InstalledMod();
        installedMod.fileName = "";
        installedMod.projectTitle = string;
        installedMod.versionNumber = "";
        installedMod.enabled = false;
        return installedMod;
    }

    public void updateFilterLabel() {
        String string = this.loaderSupplier.get();
        String string2 = this.versionSupplier.get();
        String string3 = string2 == null || string2.isBlank() ? "?" : string2;
        this.filterChip.setText("Mod  \u00b7  " + string + "  \u00b7  MC " + string3);
    }

    // Kalici olarak basarisiz sayilan URL'ler icin son deneme zamani (ms).
    // Boylece bir CDN gecici olarak 403/5xx donse bile bir sure sonra
    // otomatik tekrar denenir; placeholder sonsuza kadar yapismaz.
    private static final Map<String, Long> ICON_FAIL_TIME = Collections.synchronizedMap(new HashMap());
    private static final long ICON_RETRY_COOLDOWN_MS = 30000L;

    private static ImageIcon iconFor(String string, JList<?> jList) {
        if (string == null || string.isEmpty()) {
            return PLACEHOLDER;
        }
        ImageIcon imageIcon = ICON_CACHE.get(string);
        if (imageIcon != null) {
            return imageIcon;
        }
        Long lastFail = ICON_FAIL_TIME.get(string);
        boolean coolingDown = lastFail != null && (System.currentTimeMillis() - lastFail) < ICON_RETRY_COOLDOWN_MS;
        if (!coolingDown && !ICON_LOADING.contains(string)) {
            ICON_LOADING.add(string);
            ICON_POOL.submit(() -> {
                try {
                    byte[] byArray = HttpUtil.getBytesWithRetry(string, 3);
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(byArray));
                    if (bufferedImage == null) {
                        throw new IOException("decode failed");
                    }
                    BufferedImage bufferedImage2 = new BufferedImage(38, 38, 2);
                    Graphics2D graphics2D = bufferedImage2.createGraphics();
                    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    graphics2D.setClip(new RoundRectangle2D.Float(0.0f, 0.0f, 38.0f, 38.0f, 8.0f, 8.0f));
                    graphics2D.drawImage(bufferedImage, 0, 0, 38, 38, null);
                    graphics2D.dispose();
                    ICON_CACHE.put(string, new ImageIcon(bufferedImage2));
                    ICON_FAIL_TIME.remove(string);
                }
                catch (Exception exception) {
                    // Kalici olarak placeholder'a yazmiyoruz - sadece bir
                    // "cooldown" damgasi birakiyoruz. Sure dolunca bir
                    // sonraki cizimde otomatik olarak tekrar denenecek.
                    ICON_FAIL_TIME.put(string, System.currentTimeMillis());
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
        BufferedImage bufferedImage = new BufferedImage(38, 38, 2);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(Theme.BG_ELEVATED);
        graphics2D.fillRoundRect(0, 0, 38, 38, 8, 8);
        graphics2D.setColor(Theme.TEXT_MUTED);
        graphics2D.setFont(new Font("SansSerif", 1, 18));
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        String string = "?";
        graphics2D.drawString(string, (38 - fontMetrics.stringWidth(string)) / 2, (38 + fontMetrics.getAscent()) / 2 - 3);
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
                    jList.setToolTipText("<html><b>" + ModsPanel.esc(modSearchHit.title) + "</b><br>" + ModsPanel.esc(modSearchHit.description) + "</html>");
                } else if (e instanceof ModManager.InstalledMod) {
                    ModManager.InstalledMod installedMod = (ModManager.InstalledMod)e;
                    jList.setToolTipText("<html><b>" + ModsPanel.esc(installedMod.projectTitle) + "</b><br>" + installedMod.versionNumber + "</html>");
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

    private static void applyColors(JPanel jPanel, boolean bl, JLabel jLabel, JLabel jLabel2, JLabel jLabel3) {
        // TEMA BUG FIXI: sabit (31,63,120) mavi secim rengi yerine temanin
        // vurgu koyusu - her temada dogru gorunur.
        Color color = bl ? Theme.ACCENT_DARK : Theme.BG_BASE;
        jPanel.setBackground(color);
        jPanel.setOpaque(true);
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        jLabel2.setForeground(Theme.TEXT_SECONDARY);
        jLabel3.setForeground(bl ? Theme.ACCENT_BRIGHT : Theme.TEXT_MUTED);
    }

    private void checkModUpdates() {
        File file = this.modsDirSupplier.get();
        List<ModManager.InstalledMod> list = ModManager.loadRegistry(file);
        ArrayList<ModManager.InstalledMod> arrayList = new ArrayList<ModManager.InstalledMod>();
        for (ModManager.InstalledMod object2 : list) {
            if (object2.projectId == null || object2.projectId.startsWith("curseforge:") || !object2.enabled) continue;
            arrayList.add(object2);
        }
        if (arrayList.isEmpty()) {
            this.log.accept("G\u00fcncellenebilir Modrinth modu bulunamad\u0131.");
            return;
        }
        String string = this.loaderSupplier.get();
        String string2 = this.versionSupplier.get();
        this.log.accept("Mod g\u00fcncellemeleri kontrol ediliyor (" + arrayList.size() + "mod)...");
        this.setProgress(-1, "G\u00fcncellemeler aran\u0131yor...");
        new Thread(() -> {
            int n = 0;
            int n2 = 0;
            ArrayList<Object> arrayList2 = new ArrayList<Object>();
            for (Object object : arrayList) {
                try {
                    Object object2;
                    List<ModrinthApi.ModVersion> versions = ModrinthApi.getVersions(((ModManager.InstalledMod)object).projectId, string, string2);
                    if (versions.isEmpty() || (object2 = ModrinthApi.pickBestVersion(versions)) == null || ((ModrinthApi.ModVersion)object2).versionNumber != null && ((ModrinthApi.ModVersion)object2).versionNumber.equals(((ModManager.InstalledMod)object).versionNumber)) continue;
                    String oldVersion = ((ModManager.InstalledMod)object).versionNumber;
                    String string4 = ((ModrinthApi.ModVersion)object2).versionNumber;
                    String string5 = ((ModManager.InstalledMod)object).projectTitle != null ? ((ModManager.InstalledMod)object).projectTitle : ((ModManager.InstalledMod)object).fileName;
                    SwingUtilities.invokeLater(() -> this.log.accept(" " + string5 + ": " + oldVersion + "  " + string4));
                    if (((ModManager.InstalledMod)object).fileName != null && !((ModManager.InstalledMod)object).fileName.isEmpty()) {
                        new File(file, ((ModManager.InstalledMod)object).fileName).delete();
                        new File(file, ((ModManager.InstalledMod)object).fileName + ".disabled").delete();
                    }
                    File file2 = new File(file, ((ModrinthApi.ModVersion)object2).fileName);
                    HttpUtil.downloadFile(((ModrinthApi.ModVersion)object2).downloadUrl, file2);
                    ((ModManager.InstalledMod)object).fileName = ((ModrinthApi.ModVersion)object2).fileName;
                    ((ModManager.InstalledMod)object).versionNumber = ((ModrinthApi.ModVersion)object2).versionNumber;
                    arrayList2.add(object);
                    ++n;
                }
                catch (Exception exception) {
                    ++n2;
                    String failedName = ((ModManager.InstalledMod)object).projectTitle != null ? ((ModManager.InstalledMod)object).projectTitle : ((ModManager.InstalledMod)object).fileName;
                    SwingUtilities.invokeLater(() -> this.log.accept(" " + failedName + "g\u00fcncellenemedi: " + exception.getMessage()));
                }
            }
            List<ModManager.InstalledMod> updatedRegistry = ModManager.loadRegistry(file);
            for (Object object2 : arrayList2) {
                updatedRegistry.removeIf(arg_0 -> ModsPanel.lambda$checkModUpdates$74((ModManager.InstalledMod)object2, arg_0));
                updatedRegistry.add((ModManager.InstalledMod)object2);
            }
            ModManager.saveRegistry(file, updatedRegistry);
            int n3 = n;
            int n4 = n2;
            SwingUtilities.invokeLater(() -> {
                this.setProgress(100, "G\u00fcncelleme tamamland\u0131");
                this.log.accept(" " + n3 + "mod g\u00fcncellendi" + (String)(n4 > 0 ? ", " + n4 + "hata" : "") + ".");
                this.refreshInstalled();
                new Timer(2000, actionEvent -> this.setProgress(0, " ")).start();
            });
        }, "mod-update-check").start();
    }

    private void doSurprise() {
        String string = this.loaderSupplier.get();
        String string2 = this.versionSupplier.get();
        boolean bl = this.isCurseForge();
        this.resultsList.clearSelection();
        this.resultsModel.clear();
        this.galleryStrip.clear();
        String string3 = SURPRISE_QUERIES[(int)(Math.random() * (double)SURPRISE_QUERIES.length)];
        this.searchField.setText(string3);
        new Thread(() -> {
            try {
                ArrayList<ModSearchHit> results;
                ArrayList<ModSearchHit> arrayList2 = new ArrayList<ModSearchHit>();
                if (bl) {
                    String string4 = this.requireApiKey();
                    for (CurseForgeApi.ModResult modResult : CurseForgeApi.searchMods(string4, string3, string, string2)) {
                        arrayList2.add(ModSearchHit.from(modResult));
                    }
                } else {
                    String string5 = string != null && !string.equalsIgnoreCase("Vanilla") ? "\"categories:" + string.toLowerCase() + "\"" : null;
                    String versionFacet = string2 != null && !string2.isEmpty() ? "\"versions:" + string2 + "\"" : null;
                    String string6 = null;
                    if (string5 != null && versionFacet != null) {
                        string6 = "[[" + string5 + "],[" + versionFacet + "]]";
                    } else if (string5 != null) {
                        string6 = "[[" + string5 + "]]";
                    } else if (versionFacet != null) {
                        string6 = "[[" + versionFacet + "]]";
                    }
                    // Once toplam sonuc sayisini ogreniyoruz (limit=1 ile ucuz bir
                    // sorgu), boylece rastgele offset gecerli araligin disina
                    // tasip bos sonuc donmesin - "Sasirt beni" bazen bos donuyordu.
                    StringBuilder probeBuilder = new StringBuilder("https://api.modrinth.com/v2/search?query=").append(URLEncoder.encode(string3, "UTF-8")).append("&limit=1&offset=0&project_type=mod");
                    if (string6 != null) {
                        probeBuilder.append("&facets=").append(URLEncoder.encode(string6, "UTF-8"));
                    }
                    byte[] probeBytes = HttpUtil.getBytesWithRetry(probeBuilder.toString(), 2);
                    JsonObject probeObj = JsonParser.parseString(new String(probeBytes, StandardCharsets.UTF_8)).getAsJsonObject();
                    int totalHits = probeObj.has("total_hits") ? probeObj.get("total_hits").getAsInt() : 0;
                    int maxOffset = Math.max(0, Math.min(80, totalHits - 1));
                    int n = maxOffset <= 0 ? 0 : (int)(Math.random() * (maxOffset / 20 + 1)) * 20;
                    StringBuilder stringBuilder = new StringBuilder("https://api.modrinth.com/v2/search?query=").append(URLEncoder.encode(string3, "UTF-8")).append("&limit=20&offset=").append(n).append("&project_type=mod");
                    if (string6 != null) {
                        stringBuilder.append("&facets=").append(URLEncoder.encode(string6, "UTF-8"));
                    }
                    byte[] byArray = HttpUtil.getBytesWithRetry(stringBuilder.toString(), 3);
                    String string7 = new String(byArray, StandardCharsets.UTF_8);
                    JsonObject jsonObject = JsonParser.parseString(string7).getAsJsonObject();
                    JsonArray jsonArray = jsonObject.getAsJsonArray("hits");
                    // Bu offset'te de bos gelirse (yarisma kosulu, veri
                    // guncellenmesi vb.) bastan (offset=0) bir kez daha dene.
                    if (jsonArray.size() == 0 && n != 0) {
                        StringBuilder retryBuilder = new StringBuilder("https://api.modrinth.com/v2/search?query=").append(URLEncoder.encode(string3, "UTF-8")).append("&limit=20&offset=0&project_type=mod");
                        if (string6 != null) {
                            retryBuilder.append("&facets=").append(URLEncoder.encode(string6, "UTF-8"));
                        }
                        byte[] retryBytes = HttpUtil.getBytesWithRetry(retryBuilder.toString(), 3);
                        jsonObject = JsonParser.parseString(new String(retryBytes, StandardCharsets.UTF_8)).getAsJsonObject();
                        jsonArray = jsonObject.getAsJsonArray("hits");
                    }
                    for (int i = 0; i < jsonArray.size(); ++i) {
                        JsonObject jsonObject2 = jsonArray.get(i).getAsJsonObject();
                        ModrinthApi.ModResult modResult = new ModrinthApi.ModResult();
                        // KRITIK: project_id BURADA set edilmeliyordu - eski kod
                        // slug/title/description aliyordu ama id'yi hic okumuyor,
                        // bu yuzden bu listelerden kurulan modlar
                        // "project/null/version" URL'iyle 404 yiyordu.
                        modResult.id = jsonObject2.has("project_id") && !jsonObject2.get("project_id").isJsonNull()
                            ? jsonObject2.get("project_id").getAsString() : "";
                        modResult.slug = jsonObject2.has("slug") ? jsonObject2.get("slug").getAsString() : "";
                        modResult.title = jsonObject2.has("title") ? jsonObject2.get("title").getAsString() : "";
                        modResult.description = jsonObject2.has("description") ? jsonObject2.get("description").getAsString() : "";
                        modResult.iconUrl = jsonObject2.has("icon_url") && !jsonObject2.get("icon_url").isJsonNull() ? jsonObject2.get("icon_url").getAsString() : "";
                        modResult.downloads = jsonObject2.has("downloads") ? (int)jsonObject2.get("downloads").getAsLong() : 0;
                        modResult.categories = new ArrayList<String>();
                        if (jsonObject2.has("categories")) {
                            JsonArray jsonArray2 = jsonObject2.getAsJsonArray("categories");
                            for (int j = 0; j < jsonArray2.size(); ++j) {
                                modResult.categories.add(jsonArray2.get(j).getAsString());
                            }
                        }
                        arrayList2.add(ModSearchHit.from(modResult));
                    }
                }
                if (arrayList2.isEmpty()) {
                    SwingUtilities.invokeLater(() -> this.log.accept("Hic uyumlu mod bulunamadi - tekrar dene!"));
                    return;
                }
                Collections.shuffle(arrayList2);
                results = arrayList2;
                SwingUtilities.invokeLater(() -> {
                    for (ModSearchHit modSearchHit : results) {
                        this.resultsModel.addElement(modSearchHit);
                    }
                    this.resultsList.setSelectedIndex(0);
                    this.resultsList.ensureIndexIsVisible(0);
                    this.log.accept(results.get(0).title + " - " + string + " \u00b7 " + string2);
                });
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.log.accept("Hata: " + exception.getMessage()));
            }
        }).start();
    }

    private static /* synthetic */ boolean lambda$checkModUpdates$74(ModManager.InstalledMod installedMod, ModManager.InstalledMod installedMod2) {
        return installedMod.projectId != null && installedMod.projectId.equals(installedMod2.projectId);
    }

    // setSideBodyText + toggleSideBodyTranslation kaldirildi -
    // ceviri artik paylasilan DetailInfoPanel icinde, dil-duyarli calisiyor.

    private static class SearchResultRenderer
    extends JPanel
    implements ListCellRenderer<ModSearchHit> {
        private final JLabel iconLabel = new JLabel();
        private final JLabel titleLabel = new JLabel();
        private final JLabel descLabel = new JLabel();
        private final JLabel metaLabel = new JLabel();

        SearchResultRenderer() {
            this.setLayout(new BorderLayout(12, 0));
            this.setBorder(new EmptyBorder(10, 12, 10, 12));
            this.iconLabel.setPreferredSize(new Dimension(38, 38));
            this.iconLabel.setMinimumSize(new Dimension(38, 38));
            this.iconLabel.setMaximumSize(new Dimension(38, 38));
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
            String string2 = modSearchHit.curseforge ? "CurseForge" : (modSearchHit.modrinth != null && !modSearchHit.modrinth.categories.isEmpty() ? String.join((CharSequence)", ", modSearchHit.modrinth.categories) : "Modrinth");
            this.metaLabel.setText("\u2b07 " + ModsPanel.fmtDownloads(modSearchHit.downloads) + "   \u00b7   " + string2);
            this.iconLabel.setIcon(ModsPanel.iconFor(modSearchHit.iconUrl, jList));
            ModsPanel.applyColors(this, bl, this.titleLabel, this.descLabel, this.metaLabel);
            return this;
        }
    }

    private static class InstalledRenderer
    extends JPanel
    implements ListCellRenderer<Object> {
        private final JLabel iconLabel = new JLabel();
        private final JLabel titleLabel = new JLabel();
        private final JLabel subLabel = new JLabel();

        InstalledRenderer() {
            this.setLayout(new BorderLayout(12, 0));
            this.setBorder(new EmptyBorder(8, 12, 8, 12));
            this.iconLabel.setPreferredSize(new Dimension(38, 38));
            this.iconLabel.setMinimumSize(new Dimension(38, 38));
            this.iconLabel.setMaximumSize(new Dimension(38, 38));
            this.iconLabel.setHorizontalAlignment(0);
            JPanel jPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            jPanel.setOpaque(false);
            this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(1, 13.0f));
            this.subLabel.setFont(this.subLabel.getFont().deriveFont(0, 11.0f));
            jPanel.add(this.titleLabel);
            jPanel.add(this.subLabel);
            this.add((Component)this.iconLabel, "West");
            this.add((Component)jPanel, "Center");
            this.setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Object> jList, Object object, int n, boolean bl, boolean bl2) {
            if (!(object instanceof ModManager.InstalledMod)) {
                this.titleLabel.setText(object != null ? object.toString() : "");
                this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(1, 13.0f));
                this.subLabel.setText("");
                this.iconLabel.setIcon(null);
                return this;
            }
            ModManager.InstalledMod installedMod = (ModManager.InstalledMod)object;
            if (installedMod.fileName != null && installedMod.fileName.isEmpty() && installedMod.versionNumber != null && installedMod.versionNumber.isEmpty()) {
                this.titleLabel.setText(installedMod.projectTitle);
                this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(2, 12.0f));
                this.titleLabel.setForeground(Theme.TEXT_MUTED);
                this.subLabel.setText("");
                this.subLabel.setForeground(Theme.TEXT_MUTED);
                this.iconLabel.setIcon(null);
                this.setBackground(bl ? Theme.BG_ELEVATED : Theme.BG_SURFACE);
                this.setOpaque(true);
                return this;
            }
            this.titleLabel.setText(installedMod.projectTitle != null ? installedMod.projectTitle : installedMod.fileName);
            this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(1, 13.0f));
            this.subLabel.setText(installedMod.versionNumber + "   \u00b7   " + (installedMod.enabled ? "Etkin" : "Devre disi"));
            this.subLabel.setForeground(installedMod.enabled ? Theme.GREEN : Theme.TEXT_MUTED);
            this.iconLabel.setIcon(ModsPanel.iconFor(installedMod.iconUrl, jList));
            ModsPanel.applyColors(this, bl, this.titleLabel, this.subLabel, this.subLabel);
            return this;
        }
    }
}

