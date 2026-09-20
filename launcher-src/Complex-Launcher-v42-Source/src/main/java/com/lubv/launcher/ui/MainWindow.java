/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.ui;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.auth.AccountManager;
import com.lubv.launcher.auth.MicrosoftAuth;
import com.lubv.launcher.auth.MinecraftSession;
import com.lubv.launcher.core.Instance;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.InstanceManager;
import com.lubv.launcher.core.InstanceZip;
import com.lubv.launcher.core.L10n;
import com.lubv.launcher.core.NameChangeServer;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.core.SessionServerMock;
import com.lubv.launcher.core.Settings;
import com.lubv.launcher.game.ArgumentBuilder;
import com.lubv.launcher.game.GameLauncher;
import com.lubv.launcher.game.JavaRuntime;
import com.lubv.launcher.game.PerformanceFlags;
import com.lubv.launcher.game.VersionManifest;
import com.lubv.launcher.mods.ModManager;
import com.lubv.launcher.mods.ModrinthApi;
import com.lubv.launcher.mods.ResourcepackManager;
import com.lubv.launcher.mods.ShaderManager;
import com.lubv.launcher.ui.DesktopShortcut;
import com.lubv.launcher.ui.DiscordRpc;
import com.lubv.launcher.ui.LoginDialog;
import com.lubv.launcher.ui.LogoRes;
import com.lubv.launcher.ui.ModpacksPanel;
import com.lubv.launcher.ui.ModsPanel;
import com.lubv.launcher.ui.MyServersPanel;
import com.lubv.launcher.ui.OfflineLoginDialog;
import com.lubv.launcher.ui.ResourcepacksPanel;
import com.lubv.launcher.ui.ServersPanel;
import com.lubv.launcher.ui.ShaderPanel;
import com.lubv.launcher.ui.Theme;
import com.lubv.launcher.ui.UiFx;
import com.lubv.launcher.update.UpdateManager;
import com.lubv.launcher.update.UpdatePanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.invoke.CallSite;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

public class MainWindow
extends JFrame {
    private final Settings settings = Settings.load();
    private MinecraftSession session;
    private Instance currentInstance;
    private VersionManifest manifest;
    private JComboBox<String> instanceCombo;
    private JLabel accountLabel;
    private JButton offlineLoginButton;
    private JButton msLoginButton;
    private JComboBox<String> versionCombo;
    private JComboBox<String> loaderCombo;
    private JToggleButton[] loaderButtons;
    private JCheckBox showSnapshotsBox;
    private JSlider ramSlider;
    private JLabel ramValueLabel;
    private JTextArea logArea;
    // V35.3: Maks Performans kurulum dongusu ayni anda iki kez calismasin.
    final java.util.concurrent.atomic.AtomicBoolean maxPerfInstallBusy = new java.util.concurrent.atomic.AtomicBoolean(false);
    private JProgressBar progressBar;
    private JLabel progressStageLabel;
    private JButton launchButton;
    private JButton secondClientButton;
    private JButton terminateButton;
    private volatile Process currentGameProcess;
    private ModsPanel modsPanel;
    private ShaderPanel shaderPanel;
    private ModpacksPanel modpacksPanel;
    private ResourcepacksPanel resourcepacksPanel;
    private JTabbedPane tabs;
    private JPanel instancesGridPanel;
    private JPanel homeRoot;
    // V36.1 KISISEL MARKA: ust-sol logo etiketi (canli yenileme icin referans)
    private JLabel sidebarLogoLabel;
    // V36.2: oto-kurulum opt-out listesini tazeleyen islev (checkbox degisiminde cagrılır)
    private Runnable syncModPrefsOnChange;
    // V36.1: ana sayfa arka plan fotorafinin cache'lenmis hali
    private volatile java.awt.image.BufferedImage homeBgImage;
    // V40: tum pencere arka plan fotosu (butun sekmelerde)
    private volatile java.awt.image.BufferedImage appBgImage;
    private JPanel settingsRoot;
    private UpdatePanel updatePanel;
    private JPanel skinCard;
    private JPanel editSidebar;
    private Instance editingInstance;
    private boolean comboEventsEnabled = true;
    private static final int EDIT_SIDEBAR_WIDTH = 300;
    private boolean launching = false;
    private JTextField editNameField;
    private JComboBox<String> editVersionCombo;
    private JComboBox<String> editLoaderCombo;
    private JSlider editRamSlider;
    private JLabel editRamValueLabel;
    private JTextArea editJvmArgsField;
    private JTextField editJavaPathField;
    private JComboBox<String> editAccountCombo;
    private VersionManifest cachedManifest;
    private JButton _secondClientBtnRef;
    private JTextField javaPathField;
    private JTextField jvmArgsField;
    private JTextField preLaunchField;
    private JTextField gameDirField;
    private JLabel playTimeLabel;
    private JLabel versionHistoryLabel;
    private JTextField curseforgeKeyField;
    private JComboBox<String> gpuCombo;
    JComboBox<String> themeCombo;
    private DefaultListModel<String> accountsModel;
    private JList<String> accountsList;
    private final List<MinecraftSession> accountsCache = new ArrayList<MinecraftSession>();
    private JLabel avatarLabel;
    private static final String SODIUM_PROJECT_ID = "AANobbMI";
    private static final String IRIS_PROJECT_ID = "YL57xq9U";
    private static final String EMBEDDIUM_PROJECT_ID = "sk9rgfiA";
    private static final String OCULUS_PROJECT_ID = "GchcoXML";
    // ONEMLI DUZELTME: Bu ID'lerin bir kismi Modrinth'te artik gecersiz
    // (404 donduruyor). Eski ID'ler mod kaldirilinca degistigi icin,
    // API cagrilarina gonderilmeden once resolveProjectId() ile
    // dogrulanir; gecersizse slug ile sorgu yapilir ve guncel ID
    // bulunur. Boylece "otomatik kurulum listesinden shader secince
    // kurulmuyor" sorunu kokten cozulur.
    private static final String JEI_PROJECT_ID = "jei";                  // slug - resolveProjectId ile dogrulanir
    private static final String VULKANMOD_PROJECT_ID = "vulkanmod";      // slug
    private static final String JOURNEYMAP_PROJECT_ID = "journeymap";    // slug
    private static final String REPLAYMOD_PROJECT_ID = "replaymod";      // slug
    private static final String AMUSEMOD_PROJECT_ID = "sodium-extra";    // eski "amuse" projesi Modrinth'ten kalkti
    private static final String TWEAKEROO_PROJECT_ID = "tweakeroo";      // slug
    private static final String BSL_SHADER_ID = "bsl-shaders";           // slug - eski ID (Ksm5eocV) 404 veriyordu
    private static final String COMPLEMENTARY_SHADER_ID = "complementary-reimagined"; // slug - eski ID (DaoVbUKF) 404 veriyordu
    private String _pendingServerHost = null;
    private int _pendingServerPort = 25565;
    private String _pendingServerPassword = null;
    private static final File MOD_PREFS_FILE;
    private static final File CUSTOM_AUTO_MODS_FILE;

    static void relaunch(JFrame jFrame) {
        jFrame.dispose();
        SwingUtilities.invokeLater(() -> {
            try {
                Settings settings = Settings.load();
                Theme.apply(settings.theme != null ? settings.theme : "Nebula");
                MainWindow mainWindow = new MainWindow();
                mainWindow.setVisible(true);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public MainWindow() {
        Object object;
        this.setTitle("Complex Launcher v" + com.lubv.launcher.update.UpdateManager.CURRENT_VERSION);
        activeWindowRef = new java.lang.ref.WeakReference<>(this);
        // V35 OTO-KURULUM GUVENLIGI: ModManager'in otomatik kurulum kapisi.
        // Oto-kurulum anahtari kapaliysa ilgili mod HICBIR otomatik yoldan
        // kurulamaz ("secmedigim halde kuruluyor" bug'unun son savunma
        // hatti). Manuel panel kurulumlari bu kapidan gecmez.
        // V36.2 KOK COZUM: Maks Performans acikken bile kullanacinin
        // OTO-KURULUM LISTESINDE TIKINI KALDIRDIGI modlar kurulamaz.
        // "sodium/iris tikini kaldirdim ama yine indirdi" bugunun sonunu
        // getiren kontrol: opt-out listesi ModManager'a da bildirilir ki
        // gate lambda'si hicbir sebeple bypass edilemesin.
        Runnable syncOptOut = () -> {
            java.util.Set<String> off = new java.util.HashSet<>();
            JsonObject prefs0 = MainWindow.loadModPrefs();
            for (String k : new String[]{"sodium", "iris", "vulkanmod", "embeddium", "oculus", "bsl", "complementary", "jei", "journeymap", "replaymod", "amusemod", "tweakeroo"}) {
                boolean enabled = prefs0.has(k) ? prefs0.get(k).getAsBoolean() : false;
                if (!enabled) off.add(k);
            }
            com.lubv.launcher.mods.ModManager.setUserOptedOutKeys(off);
        };
        syncOptOut.run();
        com.lubv.launcher.mods.ModManager.setAutoInstallGate((prefKey, modsDir) -> {
            // V36.2: opt-out kontrolu artik ModManager tarafinda da var
            // (savunma katmani). Burada Maks Performans yalnizca
            // kullanacinin OSMADIGI anahtarlara izin verir.
            if (this.settings.maxPerformanceMode) {
                return !com.lubv.launcher.mods.ModManager.getUserOptedOutKeys().contains(prefKey);
            }
            return MainWindow.isModEnabled(prefKey, false);
        });
        this.syncModPrefsOnChange = syncOptOut;
        this.getRootPane().putClientProperty("JRootPane.titleBarBackground", Theme.TITLE_BAR);
        this.getRootPane().putClientProperty("JRootPane.titleBarForeground", Theme.TEXT_PRIMARY);
        this.getRootPane().putClientProperty("FlatLaf.titleBarStyle", "plain");
        this.getRootPane().putClientProperty("TitlePane.background", Theme.TITLE_BAR);
        // V36.1 KISISEL MARKA: kayitli ozel logo yolunu yukle - UI insa
        // edilmeden once uygulanir ki tum yuzeyler dogru gorselle acilsin.
        try {
            if (this.settings.customLogoPath != null && !this.settings.customLogoPath.isBlank()) {
                LogoRes.setCustomPath(this.settings.customLogoPath);
            }
        }
        catch (Throwable brandingErr) {
            // bozuk yol: varsayilan logayla devam
        }
        // V38 FIX: ana sayfa arka plan fotosunu da BASLANGICTA yukle.
        // Eskiden yalnizca Ayarlar'dan secim aninda yukleniyordu; launcher
        // kapatilip acilinca fotosu kayboluyordu ("ac kapa yapinca gidiyo").
        try {
            if (this.settings.homeBgPath != null && !this.settings.homeBgPath.isBlank()) {
                java.io.File bgFile = new java.io.File(this.settings.homeBgPath);
                if (bgFile.isFile()) {
                    this.homeBgImage = javax.imageio.ImageIO.read(bgFile);
                }
            }
        }
        catch (Throwable bgErr) {
            this.homeBgImage = null; // bozuk yol: varsayilan efektyle devam
        }
        try {
            if (this.settings.accountPhotoPath != null && !this.settings.accountPhotoPath.isBlank()) {
                this.loadAccountPhoto(); // V38: hesap fotosu da acilista cache'lensin
            }
        }
        catch (Throwable photoErr) {
            // bozuk yol: bas harfle devam
        }
        try {
            object = LogoRes.master();
            if (object != null) {
                this.setIconImage((Image)object);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.setDefaultCloseOperation(3);
        this.setSize(this.settings.windowWidth, this.settings.windowHeight);
        this.setMinimumSize(new Dimension(980, 660));
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setLocationRelativeTo(null);
        // V35 TASARIM MODU: "modern" seciliyken arka plan efekt motoru acilir.
        // Classic modunda motor hicbaslamaz (sifir maliyet).
        if (BackgroundFx.isFxMode(this.settings.designMode)) {
            BackgroundFx.start(this.getRootPane(), this.settings.designMode);
        }
        // V41: yagan foto katmanini ayarlardan yukle
        this.applyFallingPhotosFromSettings();
        // V40: tum pencere arka plan fotosunu acilista yukle
        try {
            if (this.settings.appBgPath != null && !this.settings.appBgPath.isBlank()) {
                java.io.File abf = new java.io.File(this.settings.appBgPath);
                if (abf.isFile()) {
                    this.appBgImage = javax.imageio.ImageIO.read(abf);
                }
            }
        }
        catch (Throwable appBgErr) {
            this.appBgImage = null;
        }
        // onceki oturum tam ekranda kapatildiysa ayniyle devam et
        if (this.settings.startFullscreen) {
            this.dispose();
            this.setUndecorated(true);
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
            this.setVisible(true);
        }
        // --- TAM EKRAN (F11) ---
        // Root pane uzerinden klavye kisayolu: F11 ac/kapat, ESC yalnizca
        // tam ekrandan cikar. Dugme de ust barda (buildTopBar).
        javax.swing.InputMap imF11 = this.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        imF11.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0), "toggleFullscreen");
        imF11.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "exitFullscreen");
        // F1: kisayol yardim penceresi + Ctrl+F: mod aramasina odak
        imF11.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0), "showShortcutHelp");
        imF11.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK), "focusModSearch");
        // F5: kurulu modlari yenile | F2: secili instance'i yeniden adlandir
        imF11.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0), "refreshMods");
        imF11.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0), "renameInstance");
        // V34: Ctrl+1..9 sekmeler arasi hizli gecis (1=Instances, 2=Home, ...)
        for (int ti = 1; ti <= 9; ti++) {
            final int tabIdx = ti - 1;
            imF11.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0 + ti, java.awt.event.InputEvent.CTRL_DOWN_MASK), "gotoTab" + ti);
            this.getRootPane().getActionMap().put("gotoTab" + ti, new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                    if (MainWindow.this.tabs != null && tabIdx < MainWindow.this.tabs.getTabCount()) {
                        MainWindow.this.tabs.setSelectedIndex(tabIdx);
                    }
                }
            });
        }
        this.getRootPane().getActionMap().put("refreshMods", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                MainWindow.this.tabs.setSelectedComponent(MainWindow.this.modsPanel);
                MainWindow.this.modsPanel.refreshInstalled();
                MainWindow.this.log(L10n.isEnglish() ? "Mods refreshed (F5)." : "Modlar yenilendi (F5).");
            }
        });
        this.getRootPane().getActionMap().put("renameInstance", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                if (MainWindow.this.currentInstance != null) {
                    MainWindow.this.renameInstanceDialog(MainWindow.this.currentInstance);
                }
            }
        });
        this.getRootPane().getActionMap().put("focusModSearch", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                MainWindow.this.tabs.setSelectedComponent(MainWindow.this.modsPanel);
                SwingUtilities.invokeLater(() -> MainWindow.this.modsPanel.focusSearch());
            }
        });
        this.getRootPane().getActionMap().put("showShortcutHelp", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                MainWindow.this.showShortcutHelp();
            }
        });
        this.getRootPane().getActionMap().put("toggleFullscreen", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                MainWindow.this.toggleFullscreen();
            }
        });
        this.getRootPane().getActionMap().put("exitFullscreen", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                if (MainWindow.this.isUndecorated()) {
                    MainWindow.this.toggleFullscreen();
                }
            }
        });
        Paths.ensureAll();
        object = InstanceManager.ensureDefault(this.settings.activeInstance);
        this.settings.activeInstance = (String)object;
        this.settings.save();
        this.currentInstance = Instance.load((String)object);
        InstanceManager.migrateLegacyMods((String)object);
        this.setLayout(new BorderLayout());
        this.add((Component)this.buildTopBar(), "North");
        this.tabs = new JTabbedPane();
        this.tabs.setTabPlacement(1);
        this.tabs.addTab(L10n.get("tab.instances"), this.buildInstancesTab());
        this.tabs.addTab(L10n.get("tab.home"), this.buildHomeTab());
        this.modsPanel = new ModsPanel(this::currentLoader, this::currentMcVersion, () -> this.currentInstance != null ? this.currentInstance.modsDir() : new File(Paths.GAME_DIR, "mods"), this::curseforgeKey, this::log);
        this.tabs.addTab(L10n.get("tab.mods"), this.modsPanel);
        this.shaderPanel = new ShaderPanel(() -> this.currentInstance != null ? this.currentInstance.shaderpacksDir() : new File(Paths.GAME_DIR, "shaderpacks"), this::log);
        this.tabs.addTab(L10n.get("tab.shaders"), this.shaderPanel);
        this.resourcepacksPanel = new ResourcepacksPanel(() -> this.currentInstance != null ? this.currentInstance.resourcepacksDir() : Paths.RESOURCEPACKS_DIR, this::log);
        this.tabs.addTab(L10n.get("tab.resourcepacks"), this.resourcepacksPanel);
        this.modpacksPanel = new ModpacksPanel(this::curseforgeKey, this::log, this::refreshInstancesAfterModpack);
        this.tabs.addTab(L10n.get("tab.modpacks"), this.modpacksPanel);
        this.tabs.addTab(L10n.get("tab.servers"), new ServersPanel());
        this.tabs.addTab(L10n.get("tab.my_servers"), this.buildMyServersTab());
        this.tabs.addTab(L10n.get("tab.accounts"), this.buildAccountsTab());
        this.tabs.addTab(L10n.get("tab.settings"), this.buildSettingsTab());
        // V35 TASARIM: Modern modda sekmeler yari-saydam bir pane'in uzerine
        // oturur; pane'in paint'i BackgroundFx efektlerini cizer (orbit
        // gradyanlar + yildiz alani). Classic modda birebir eski gorunum.
        // V35.1 FIX: FlatLaf JTabbedPane OPAQUE oldugu icin efekt hic
        // gorunmuyordu (TabbedPane.background = BG_BASE tam dolum). Modern
        // modda tab bileşeni + icerik koklerini seffaf yapiyoruz; ayrica
        // efekti dogrudan JTabbedPane ustune cizmek icin paintOrder
        // tekniği kullanıyoruz (children'dan ÖNCE background, sonra fx).
        // V40: animasyonlu arka plan modlari (modern/minecraft/herobrine)
        // V41 CANLI TASARIM GECISI: host her zaman FX-capabilite'li kurulur
        // (paintComponent icinde isRunning() kontrolu var). Boylece Tasarim
        // kombo'sundan mod degistirince RESTART GEREKMEZ — timer baslar,
        // opakliklar ayarlanir, ayni host aninda boyar. "Classic"te motor
        // zaten DURUR (0 maliyet). Eski yapiya gore fark: host yapisi
        // boot'ta sabit; mod sadece motor durumudur.
        // V41.2: host HER ZAMAN FX-capable kurulur — boot'ta classic secili
        // olsa bile. Motor kapaliyken paintComponent yalnizca app-bg fotografi
        // basar; kullanici Tasarim kombo'sundan baska bir mode secince
        // applyDesignMode() motoru baslatir ve AYNI host aninda FX boyamaya
        // baslar. Böylece mod gecisi HICBIR ZAMAN restart gerektirmez.
        // Opaklik applyDesignMode icinde iki yonlu senkronlanir.
        final boolean modernFx = BackgroundFx.isFxMode(this.settings.designMode);
        JComponent centerHost;
        {
            this.tabs.setOpaque(false);
            this.tabs.putClientProperty("ComplexFx.host", Boolean.TRUE);
            // Icerik kokleri: tum sekme kok panelleri seffaf degilse bile
            // FlatLaf 'Panel.background' ile doldurur; kok panelleri zaten
            // setOpaque(false). Scroll viewport'leri cleanScroll ile seffaf.
            centerHost = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (BackgroundFx.isRunning()) {
                        BackgroundFx.paint((Graphics2D) g, getWidth(), getHeight());
                    }
                    // V40: tum pencere arka plan fotosu (efektin USTUNE
                    // yumusak karartma ile cizilir; icerik okunur kalir).
                    java.awt.image.BufferedImage abg = MainWindow.this.appBgImage;
                    if (abg != null && abg.getWidth() > 0 && abg.getHeight() > 0) {
                        int bw = abg.getWidth(), bh = abg.getHeight();
                        double sc = Math.max((double) getWidth() / bw, (double) getHeight() / bh);
                        int dw = (int) Math.ceil(bw * sc), dh = (int) Math.ceil(bh * sc);
                        g.drawImage(abg, (getWidth() - dw) / 2, (getHeight() - dh) / 2, dw, dh, null);
                        g.setColor(new Color(0, 0, 0, 120));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            };
            centerHost.setLayout(new BorderLayout());
            centerHost.add(this.tabs, "Center");
            // V41.4 JUMPSCARE: herobrine modunda pencereye cift tik ->
            // 700ms dev Herobrine yuzu. Sadece herobrine modunda (maliyet 0).
            centerHost.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2
                            && "herobrine".equalsIgnoreCase(MainWindow.this.settings.designMode)) {
                        BackgroundFx.triggerJumpscare();
                        MainWindow.this.getRootPane().repaint();
                    }
                }
            });
        }
        this.add((Component)centerHost, "Center");
        {
            // Sekme icerik koklerini seffaf yap (buildXxxTab panelleri).
            SwingUtilities.invokeLater(() -> MainWindow.makeTabContentTransparent(this.tabs));
        }
        // V34: son acik sekmeyi hatirla - bir sonraki acilista ayni sekme
        this.tabs.setSelectedIndex(Math.min(Math.max(this.settings.lastTab, 0), this.tabs.getTabCount() - 1));
        this.tabs.addChangeListener(changeEvent -> {
            // V34: acik sekmeyi ayarlara isle
            this.settings.lastTab = this.tabs.getSelectedIndex();
            this.settings.save();
            Component component = this.tabs.getSelectedComponent();
            if (component == this.modsPanel) {
                this.modsPanel.updateFilterLabel();
                SwingUtilities.invokeLater(() -> this.modsPanel.refreshInstalled());
            }
            if (component instanceof JComponent) {
                JComponent jComponent = (JComponent)component;
                float[] fArray = new float[]{0.6f};
                long l = System.currentTimeMillis();
                Timer timer = new Timer(14, null);
                timer.addActionListener(actionEvent -> {
                    float f = Math.min(1.0f, (float)(System.currentTimeMillis() - l) / 180.0f);
                    fArray[0] = 0.6f + 0.4f * UiFx.easeOutCubic(f);
                    jComponent.repaint();
                    if (f >= 1.0f) {
                        timer.stop();
                    }
                });
                timer.start();
            }
        });
        AccountManager.migrateLegacy();
        MinecraftSession minecraftSession = AccountManager.getActive();
        if (minecraftSession != null) {
            this.session = minecraftSession;
            this.updateAccountLabel();
            if (minecraftSession.msRefreshToken != null) {
                new Thread(() -> {
                    try {
                        MinecraftSession minecraftSession2 = MicrosoftAuth.loginWithRefreshToken(minecraftSession.msRefreshToken, this::log);
                        AccountManager.add(minecraftSession2);
                        SwingUtilities.invokeLater(() -> {
                            this.session = minecraftSession2;
                            this.onSessionChanged();
                        });
                    }
                    catch (Exception exception) {
                        this.log("Oturum yenilenemedi: " + exception.getMessage());
                    }
                }).start();
            }
        }
        new Thread(this::loadVersions).start();
        new Thread(() -> {
            try {
                String string = ProcessHandle.current().info().command().orElse("");
                DesktopShortcut.createIfNeeded(string);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }).start();
        DiscordRpc.start(this.currentInstance != null ? this.currentInstance.name : "Complex Launcher");
        new Timer(3000, actionEvent -> {
            ((Timer)actionEvent.getSource()).stop();
            this.checkForLauncherUpdates();
        }).start();
    }

    private JPanel buildTopBar() {
        JPanel jPanel = new JPanel(new BorderLayout()){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = this.getWidth();
                int n2 = this.getHeight();
                graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, Theme.BG_SURFACE, 0.0f, n2, Theme.BG_BASE));
                graphics2D.fillRect(0, 0, n, n2);
                graphics2D.setPaint(new GradientPaint(0.0f, n2 - 2, new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 80), (float)n * 0.6f, n2 - 2, new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 0)));
                graphics2D.fillRect(0, n2 - 2, n, 2);
                graphics2D.dispose();
            }
        };
        jPanel.setOpaque(false);
        jPanel.setPreferredSize(new Dimension(0, 52));
        JPanel jPanel2 = new JPanel(new FlowLayout(0, 14, 0));
        jPanel2.setOpaque(false);
        this.sidebarLogoLabel = new JLabel(MainWindow.makeLogo());
        JLabel jLabel = this.sidebarLogoLabel;
        jLabel.setCursor(Cursor.getPredefinedCursor(12));
        jLabel.setToolTipText("\ud83d\udc31");
        jLabel.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                MainWindow.this.spawnCatEasterEgg();
            }
        });
        JPanel jPanel3 = new JPanel();
        jPanel3.setLayout(new BoxLayout(jPanel3, 1));
        jPanel3.setOpaque(false);
        JLabel jLabel2 = new JLabel("Complex Launcher v" + com.lubv.launcher.update.UpdateManager.CURRENT_VERSION);
        jLabel2.setFont(new Font("SansSerif", 1, 18));
        jLabel2.setForeground(Theme.TEXT_PRIMARY);
        JLabel jLabel3 = new JLabel("tlauncherdan iyidir");
        jLabel3.setFont(new Font("SansSerif", 2, 10));
        jLabel3.setForeground(Theme.ACCENT_BRIGHT);
        jPanel3.add(jLabel2);
        jPanel3.add(jLabel3);
        jPanel2.add(jLabel);
        jPanel2.add(jPanel3);
        jPanel.add((Component)jPanel2, "West");
        JPanel jPanel4 = new JPanel(new FlowLayout(2, 6, 0));
        jPanel4.setOpaque(false);
        jPanel4.setBorder(new EmptyBorder(0, 0, 0, 10));
        this.instanceCombo = new JComboBox();
        this.instanceCombo.setLightWeightPopupEnabled(true);
        this.instanceCombo.setPreferredSize(new Dimension(160, 28));
        for (String object2 : InstanceManager.listNames()) {
            this.instanceCombo.addItem(object2);
        }
        this.instanceCombo.setSelectedItem(this.currentInstance.name);
        this.instanceCombo.addActionListener(actionEvent -> {
            if (!this.comboEventsEnabled) {
                return;
            }
            Object object = this.instanceCombo.getSelectedItem();
            if (object != null) {
                this.switchInstance((String)object);
            }
        });
        JButton jButton = UiFx.accentButton("");
        jButton.setToolTipText("Yeni \u00f6rnek");
        jButton.addActionListener(actionEvent -> this.createInstance());
        JButton jButton2 = UiFx.ghostButton(com.lubv.launcher.core.L10n.isEnglish() ? "Delete" : "Sil");
        jButton2.addActionListener(actionEvent -> this.deleteInstance());
        this.updatePanel = new UpdatePanel(() -> this.doUpdate());
        jPanel4.add(this.updatePanel);
        jPanel4.add(Box.createHorizontalStrut(4));
        jPanel4.add(this.instanceCombo);
        jPanel4.add(jButton);
        jPanel4.add(jButton2);
        // --- Tam ekran dugmesi ---
        JButton fsBtn = UiFx.ghostButton("\u26f6");
        fsBtn.setPreferredSize(new Dimension(40, 28));
        fsBtn.setToolTipText(L10n.isEnglish() ? "Fullscreen (F11)" : "Tam ekran (F11)");
        fsBtn.addActionListener(actionEvent -> this.toggleFullscreen());
        jPanel4.add(fsBtn);
        jPanel.add((Component)jPanel4, "East");
        return jPanel;
    }

    /**
     * Tam ekran ac/kapat. Swing'te setUndecorated displayable frame'de
     * atistigi icin frame dispose edilip tekrar gosterilir - bilesenler
     * ve durum korunur. Cikista onceki pencere boyutu geri gelir.
     * Durum settings.json'a yazilir: bir sonraki acilista hatırlanir.
     */
    private void toggleFullscreen() {
        boolean goFull = !this.isUndecorated();
        Rectangle prevBounds = this.getBounds();
        int prevState = this.getExtendedState();
        this.dispose();
        this.setUndecorated(goFull);
        if (goFull) {
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            this.setExtendedState(prevState == JFrame.MAXIMIZED_BOTH ? JFrame.NORMAL : prevState);
            this.setSize(prevBounds.width, prevBounds.height);
            this.setLocationRelativeTo(null);
        }
        this.setVisible(true);
        try {
            this.settings.startFullscreen = goFull;
            this.settings.save();
        } catch (Exception ignored) {}
    }

    /**
     * F1 kisayol yardimi: tum klavye kisayollari ve ipuclari tek pencerede.
     * Tema renkleriyle, kapatma butonlu modal dialog.
     */
    private void showShortcutHelp() {
        boolean en = L10n.isEnglish();
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='width:440px;padding:6px'>");
        sb.append("<b style='font-size:13px'>").append(en ? "\u2328 Keyboard Shortcuts" : "\u2328 Klavye Kisayollari")
          .append("</b><br><br>");
        sb.append("<b>F11</b> \u2014 ").append(en ? "Toggle fullscreen" : "Tam ekran a\u00e7/kapat").append("<br>");
        sb.append("<b>ESC</b> \u2014 ").append(en ? "Exit fullscreen" : "Tam ekrandan \u00e7\u0131k").append("<br>");
        sb.append("<b>F1</b> \u2014 ").append(en ? "This help window" : "Bu yard\u0131m penceresi").append("<br>");
        sb.append("<b>Ctrl+F</b> \u2014 ").append(en ? "(Mods tab) focus search box" : "(Modlar sekmesi) arama kutusuna odak").append("<br>");
        sb.append("<b>F5</b> \u2014 ").append(en ? "Refresh installed mods" : "Kurulu modlari yenile").append("<br>");
        sb.append("<b>F2</b> \u2014 ").append(en ? "Rename the selected instance" : "Secili instance'i yeniden adlandir").append("<br>");
        sb.append("<b>Ctrl+1-9</b> \u2014 ").append(en ? "Jump directly to a tab" : "Dogrudan sekmeye gec").append("<br><br>");
        sb.append("<b>").append(en ? "Mouse tips" : "Fare ipu\u00e7lar\u0131").append(":</b><br>");
        sb.append("\u2022 ").append(en ? "Double-click a search result \u2192 install it" : "Arama sonucuna \u00e7ift t\u0131k \u2192 kurar").append("<br>");
        sb.append("\u2022 ").append(en ? "Right-click a mod \u2192 context menu (toggle/remove/update)" : "Mode saf t\u0131k \u2192 men\u00fc (a\u00e7/kapat, kald\u0131r, g\u00fcncelle)").append("<br>");
        sb.append("\u2022 ").append(en ? "Drag & drop .jar mods / .zip shader & resource packs onto their tab" : ".jar modlari ve .zip shader/doku paketlerini ilgili sekmeye sürükle").append("<br>");
        sb.append("\u2022 ").append(en ? "Click the logo \ud83d\udc31 \u2192 a little surprise" : "Logoya t\u0131kla \ud83d\udc31 \u2192 k\u00fc\u00e7\u00fck bir s\u00fcrpriz").append("<br>");
        sb.append("\u2022 ").append(en ? "Fullscreen button is in the top-right bar" : "Tam ekran butonu \u00fcst bar\u0131n saf\u0131nda").append("<br><br>");            sb.append("<font color='#888888'><i>Complex Launcher v</i></font>").append(com.lubv.launcher.update.UpdateManager.CURRENT_VERSION);
        sb.append("</body></html>");
        JLabel content = new JLabel(sb.toString());
        content.setForeground(Theme.TEXT_PRIMARY);
        content.setBackground(Theme.BG_SURFACE);
        content.setOpaque(true);
        content.setBorder(new EmptyBorder(14, 18, 12, 18));
        javax.swing.JDialog dlg = new javax.swing.JDialog(this, en ? "Keyboard Shortcuts" : "Klavye Kisayollari", true);
        dlg.setContentPane(content);
        dlg.getRootPane().putClientProperty("JRootPane.titleBarBackground", Theme.TITLE_BAR);
        dlg.getRootPane().putClientProperty("JRootPane.titleBarForeground", Theme.TEXT_PRIMARY);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel buildInstancesTab() {
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setOpaque(false);
        JPanel jPanel2 = new JPanel(new BorderLayout());
        jPanel2.setOpaque(false);
        jPanel2.setBorder(new EmptyBorder(16, 18, 8, 18));
        JLabel jLabel = new JLabel(L10n.isEnglish() ? "Instances" : "\u00d6rnekler");
        jLabel.setFont(new Font("SansSerif", 1, 20));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        JLabel jLabel2 = new JLabel(L10n.get("instance.click_hint"));
        jLabel2.setFont(jLabel2.getFont().deriveFont(0, 12.0f));
        jLabel2.setForeground(Theme.TEXT_MUTED);
        jPanel2.add((Component)jLabel, "North");
        jPanel2.add((Component)jLabel2, "South");
        jPanel.add((Component)jPanel2, "North");
        this.instancesGridPanel = new JPanel(new FlowLayout(0, 14, 14));
        this.instancesGridPanel.setOpaque(false);
        JScrollPane jScrollPane = UiFx.cleanScroll(this.instancesGridPanel);
        jScrollPane.setBorder(new EmptyBorder(0, 14, 14, 14));
        jPanel.add((Component)jScrollPane, "Center");
        jPanel.add((Component)this.buildEditSidebar(), "East");
        this.refreshInstancesGrid();
        return jPanel;
    }

    private void refreshInstancesGrid() {
        if (this.instancesGridPanel == null) {
            return;
        }
        this.instancesGridPanel.removeAll();
        for (String string : InstanceManager.listNames()) {
            this.instancesGridPanel.add(new InstanceCard(Instance.load(string)));
        }
        this.instancesGridPanel.add(new NewInstanceCard());
        this.instancesGridPanel.revalidate();
        this.instancesGridPanel.repaint();
    }

    private static String truncate(String string, int n) {
        return string == null ? "" : (string.length() > n ? string.substring(0, n - 1) + "..." : string);
    }

    /**
     * Instance kartlari icin isim-bazli tutarli bir gradyan uretir.
     * ONCEDEN bu fonksiyon CARD_GRADIENTS adinda 8 SABIT renkten
     * (hep ayni mor/mavi/yesil/kirmizi tonlari) birini seciyordu -
     * kullanicinin secitigi tema (Forest, Ocean, Crimson, Ember vb.)
     * ne olursa olsun kartlar hep ayni gorunuyordu. Bu, temalarin
     * "gercekten kullanilamamasi" hissinin ana kaynagiydi.
     *
     * Artik gradyan, o an aktif olan Theme.ACCENT renginin HSB (Hue-
     * Saturation-Brightness) uzayinda hue'su etrafinda -40..+40 derece
     * kaydirilmasiyla turetiliyor. Boylece:
     *  - Her tema kendi karakteristik renk ailesini yansitir (orn.
     *    Forest temasinda kartlar yesil tonlarda, Crimson'da kirmizi
     *    tonlarda olur) ama
     *  - Isimler arasinda hala görsel çeşitlilik korunur (ayni hue
     *    etrafinda farkli kaydirmalar ile 8 farkli varyant).
     */
    private static Color[] gradientFor(String string) {
        int variantIndex = Math.abs(string.hashCode()) % 8;
        Color accent = Theme.ACCENT != null ? Theme.ACCENT : new Color(139, 92, 246);
        float[] hsb = Color.RGBtoHSB(accent.getRed(), accent.getGreen(), accent.getBlue(), null);
        // 8 varyant icin hue'yu asama asama kaydir (-40 ile +40 derece arasi, esit araliklarla).
        float hueShiftDegrees = (variantIndex - 3.5f) * (80f / 8f);
        float newHue = hsb[0] + (hueShiftDegrees / 360f);
        newHue = ((newHue % 1f) + 1f) % 1f; // 0..1 araliginda tut
        float saturation = Math.min(1f, Math.max(0.35f, hsb[1]));
        Color top = Color.getHSBColor(newHue, saturation, Math.min(1f, hsb[2] * 1.05f + 0.05f));
        Color bottom = Color.getHSBColor(newHue, Math.min(1f, saturation * 1.1f), Math.max(0.15f, hsb[2] * 0.55f));
        return new Color[]{top, bottom};
    }

    // V39.1: easter egg sprite boyutlari (CatChaseOverlay.CAT_W/CAT_H ile ayni)
    private static final int EGG_SPRITE_W = 140;
    private static final int EGG_SPRITE_H = 92;

    private static Color brighten(Color color, float f) {
        return new Color(Math.min(255, Math.round((float)color.getRed() + (float)(255 - color.getRed()) * f)), Math.min(255, Math.round((float)color.getGreen() + (float)(255 - color.getGreen()) * f)), Math.min(255, Math.round((float)color.getBlue() + (float)(255 - color.getBlue()) * f)));
    }

    private static Color darken(Color color, float f) {
        return new Color(Math.max(0, Math.round((float)color.getRed() * (1f - f))), Math.max(0, Math.round((float)color.getGreen() * (1f - f))), Math.max(0, Math.round((float)color.getBlue() * (1f - f))));
    }

    private JPanel buildEditSidebar() {
        this.editSidebar = new JPanel(new BorderLayout());
        this.editSidebar.setPreferredSize(new Dimension(0, 0));
        this.editSidebar.setBackground(Theme.BG_SURFACE);
        this.editSidebar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.BG_BORDER));
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, 1));
        jPanel.setBackground(Theme.BG_SURFACE);
        jPanel.setBorder(new EmptyBorder(18, 16, 18, 16));
        JLabel jLabel = new JLabel(L10n.isEnglish() ? "Edit Instance" : "\u00d6rnefi D\u00fczenle");
        jLabel.setFont(new Font("SansSerif", 1, 15));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        jLabel.setAlignmentX(0.0f);
        jPanel.add(jLabel);
        jPanel.add(Box.createVerticalStrut(14));
        jPanel.add(this.sidebarField(L10n.isEnglish() ? "Instance name:" : "\u00d6rnek ad\u0131:"));
        this.editNameField = this.styledField();
        jPanel.add(this.editNameField);
        jPanel.add(Box.createVerticalStrut(10));
        jPanel.add(this.sidebarField(L10n.isEnglish() ? "Minecraft version:" : "Minecraft s\u00fcr\u00fcm\u00fc:"));
        this.editVersionCombo = new JComboBox();
        this.editVersionCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        this.editVersionCombo.setAlignmentX(0.0f);
        jPanel.add(this.editVersionCombo);
        jPanel.add(Box.createVerticalStrut(10));
        jPanel.add(this.sidebarField(L10n.isEnglish() ? "Mod loader:" : "Mod y\u00fckleyici:"));
        this.editLoaderCombo = new JComboBox<String>(new String[]{"Vanilla", "Fabric", "Forge", "NeoForge", "OptiFine"});
        this.editLoaderCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        this.editLoaderCombo.setAlignmentX(0.0f);
        this.editLoaderCombo.addActionListener(actionEvent -> {
            if (this.editingInstance == null) {
                return;
            }
            String string = (String)this.editLoaderCombo.getSelectedItem();
            Object object = this.editVersionCombo.getSelectedItem();
            if (object != null && ("Fabric".equalsIgnoreCase(string) || "Forge".equalsIgnoreCase(string) || "NeoForge".equalsIgnoreCase(string) || "OptiFine".equalsIgnoreCase(string))) {
                File file = this.editingInstance.modsDir();
                String string2 = (String)object;
                new Thread(() -> this.autoInstallForLoader(string, file, string2)).start();
            }
        });
        jPanel.add(this.editLoaderCombo);
        jPanel.add(Box.createVerticalStrut(10));
        jPanel.add(this.sidebarField("RAM:"));
        JPanel jPanel2 = new JPanel(new BorderLayout(8, 0));
        jPanel2.setOpaque(false);
        jPanel2.setAlignmentX(0.0f);
        jPanel2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        this.editRamSlider = new JSlider(1, 16);
        this.editRamSlider.setOpaque(false);
        this.editRamValueLabel = new JLabel("4 GB");
        this.editRamValueLabel.setForeground(Theme.TEXT_PRIMARY);
        this.editRamSlider.addChangeListener(changeEvent -> this.editRamValueLabel.setText(this.editRamSlider.getValue() + "GB"));
        jPanel2.add((Component)this.editRamSlider, "Center");
        jPanel2.add((Component)this.editRamValueLabel, "East");
        jPanel.add(jPanel2);
        jPanel.add(Box.createVerticalStrut(10));
        jPanel.add(this.sidebarField(L10n.isEnglish() ? "Extra JVM arguments:" : "Ekstra JVM arg\u00fcmanlar\u0131:"));
        // ONEMLI DUZELTME: JVM argumanlari tek satirlik bir JTextField'ta
        // tutuluyordu - uzun bir flag listesi (orn. Maks Performans modu
        // acildiginda otomatik eklenen ~20 bayrak) yazi alaninin disina
        // tasip goruntu bozulmasina (ekran goruntusunde gorulen kesilme/
        // bug) yol aciyordu. Artik cok satirli, otomatik kaydiran bir
        // JTextArea + kaydirma cubugu kullaniliyor.
        this.editJvmArgsField = new JTextArea(3, 20);
        this.editJvmArgsField.setLineWrap(true);
        this.editJvmArgsField.setWrapStyleWord(false);
        this.editJvmArgsField.setFont(new Font("Monospaced", 0, 11));
        this.editJvmArgsField.setBackground(Theme.BG_ELEVATED);
        this.editJvmArgsField.setForeground(Theme.TEXT_PRIMARY);
        this.editJvmArgsField.setCaretColor(Theme.TEXT_PRIMARY);
        this.editJvmArgsField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BG_BORDER, 1), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        JScrollPane jvmArgsScroll = new JScrollPane(this.editJvmArgsField, 20, 31);
        // ONEMLI DUZELTME: preferredSize genisligi yanlislikla 0 olarak
        // ayarlanmisti. BoxLayout gercek boyutu maximumSize'a degil
        // preferredSize'a gore hesapladigi icin, bu alan neredeyse hic
        // genislik almiyordu - metin gorunmez/kesik kaliyordu (kullanicinin
        // bildirdigi "parametreler uzun olunca buga giriyor" sorununun
        // gercek sebebi buydu, onceki JTextArea degisikligi tek basina
        // yeterli olmamisti).
        jvmArgsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        jvmArgsScroll.setPreferredSize(new Dimension(260, 70));
        jvmArgsScroll.setMinimumSize(new Dimension(0, 70));
        jvmArgsScroll.setAlignmentX(0.0f);
        jvmArgsScroll.setBorder(BorderFactory.createEmptyBorder());
        jPanel.add(jvmArgsScroll);
        jPanel.add(Box.createVerticalStrut(10));
        jPanel.add(this.sidebarField(L10n.isEnglish() ? "Custom Java path:" : "\u00d6zel Java yolu:"));
        this.editJavaPathField = this.styledField();
        jPanel.add(this.editJavaPathField);
        jPanel.add(Box.createVerticalStrut(10));
        jPanel.add(this.sidebarField(L10n.isEnglish() ? "Account for this instance:" : "Bu instance i\u00e7in hesap:"));
        this.editAccountCombo = new JComboBox<String>();
        this.editAccountCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        this.editAccountCombo.setAlignmentX(0.0f);
        this.editAccountCombo.setToolTipText(L10n.isEnglish()
            ? "Choose a different account (name/skin) to use only for this instance"
            : "Bu instance icin farkli bir hesap (isim/skin) kullanmak icin secin");
        jPanel.add(this.editAccountCombo);
        jPanel.add(Box.createVerticalStrut(16));
        jPanel.add(UiFx.separator());
        jPanel.add(Box.createVerticalStrut(14));
        JButton jButton = UiFx.accentButton(L10n.isEnglish() ? "Save" : "Kaydet");
        JButton jButton2 = UiFx.ghostButton(L10n.isEnglish() ? "Folder" : "Klas\u00f6r");
        JButton jButton3 = UiFx.dangerButton(com.lubv.launcher.core.L10n.isEnglish() ? "Delete" : "Sil");
        JButton jButton4 = UiFx.ghostButton(L10n.isEnglish() ? "Close" : "Kapat");
        jButton.setAlignmentX(0.0f);
        jButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jButton2.setAlignmentX(0.0f);
        jButton2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jButton3.setAlignmentX(0.0f);
        jButton3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jButton4.setAlignmentX(0.0f);
        jButton4.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jButton.addActionListener(actionEvent -> this.saveEditSidebar());
        jButton2.addActionListener(actionEvent -> {
            if (this.editingInstance != null) {
                try {
                    Desktop.getDesktop().open(this.editingInstance.dir());
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        });
        jButton3.addActionListener(actionEvent -> this.deleteEditedInstance());
        jButton4.addActionListener(actionEvent -> this.closeEditSidebar());
        JButton jButton5 = UiFx.ghostButton("Ekran G\u00f6r\u00fcnt\u00fcleri");
        jButton5.setAlignmentX(0.0f);
        jButton5.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jButton5.setToolTipText("screenshots/ klas\u00f6r\u00fcn\u00fc a\u00e7");
        jButton5.addActionListener(actionEvent -> {
            if (this.editingInstance == null) {
                return;
            }
            File file = new File(this.editingInstance.dir(), "screenshots");
            file.mkdirs();
            try {
                Desktop.getDesktop().open(file);
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
        JButton jButton6 = UiFx.ghostButton("Ikon Sec");
        jButton6.setAlignmentX(0.0f);
        jButton6.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jButton6.setToolTipText("Instance ikonunu degistir");
        jButton6.addActionListener(actionEvent2 -> {
            if (this.editingInstance == null) {
                return;
            }
            String[] stringArray = new String[]{"default", "grass", "diamond", "nether", "end", "ocean", "cave", "forest", "desert", "snow"};
            String[] stringArray2 = new String[]{"Varsayilan", "Cim", "Elmas", "Cehennem", "Son", "Okyanus", "Magara", "Orman", "Col", "Kar"};
            JPopupMenu jPopupMenu = new JPopupMenu();
            for (int i = 0; i < stringArray.length; ++i) {
                String string = stringArray[i];
                JMenuItem jMenuItem = new JMenuItem(stringArray2[i]);
                jMenuItem.addActionListener(actionEvent -> {
                    this.editingInstance.iconName = string;
                    this.editingInstance.save();
                    this.refreshInstancesGrid();
                });
                jPopupMenu.add(jMenuItem);
            }
            JMenuItem jMenuItem = new JMenuItem("Ozel PNG yukle...");
            jMenuItem.addActionListener(actionEvent -> {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
                if (jFileChooser.showOpenDialog(this) == 0) {
                    try {
                        File file = new File(this.editingInstance.dir(), "instance_icon.png");
                        Files.copy(jFileChooser.getSelectedFile().toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        this.editingInstance.iconName = "custom";
                        this.editingInstance.save();
                        this.refreshInstancesGrid();
                    }
                    catch (Exception exception) {
                        this.log("Ikon yuklenemedi: " + exception.getMessage());
                    }
                }
            });
            jPopupMenu.add(jMenuItem);
            jPopupMenu.show(jButton6, 0, jButton6.getHeight());
        });
        JButton jButton7 = UiFx.ghostButton(L10n.get("export"));
        jButton7.setAlignmentX(0.0f);
        jButton7.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jButton7.setToolTipText("Bu instance'\u0131 ZIP olarak kaydet");
        jButton7.addActionListener(actionEvent -> {
            if (this.editingInstance == null) {
                return;
            }
            this.exportInstance(this.editingInstance);
        });
        JButton jButton8 = UiFx.ghostButton(L10n.get("skin.title"));
        jButton8.setAlignmentX(0.0f);
        jButton8.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jButton8.setToolTipText(L10n.get("skin.title"));
        jButton8.addActionListener(actionEvent -> this.showSkinUploaderDialog());
        JButton jButton9 = UiFx.ghostButton(L10n.get("world.title"));
        jButton9.setAlignmentX(0.0f);
        jButton9.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jButton9.setToolTipText(L10n.get("world.import") + " / " + L10n.get("world.export"));
        jButton9.addActionListener(actionEvent -> this.showWorldManagerDialog());
        JButton jButton10 = UiFx.ghostButton(L10n.get("crash.history_title"));
        jButton10.setAlignmentX(0.0f);
        jButton10.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jButton10.addActionListener(actionEvent -> this.showCrashHistoryDialog());
        jPanel.add(jButton);
        jPanel.add(Box.createVerticalStrut(6));
        jPanel.add(jButton2);
        jPanel.add(Box.createVerticalStrut(6));
        jPanel.add(jButton5);
        jPanel.add(Box.createVerticalStrut(6));
        jPanel.add(jButton8);
        jPanel.add(Box.createVerticalStrut(6));
        jPanel.add(jButton9);
        jPanel.add(Box.createVerticalStrut(6));
        jPanel.add(jButton10);
        jPanel.add(Box.createVerticalStrut(6));
        jPanel.add(Box.createVerticalStrut(6));
        jPanel.add(jButton6);
        jPanel.add(jButton7);
        jPanel.add(Box.createVerticalStrut(6));
        jPanel.add(jButton3);
        jPanel.add(Box.createVerticalStrut(6));
        jPanel.add(jButton4);
        jPanel.add(Box.createVerticalGlue());
        JScrollPane jScrollPane = new JScrollPane(jPanel);
        jScrollPane.setBorder(null);
        jScrollPane.setHorizontalScrollBarPolicy(31);
        jScrollPane.getVerticalScrollBar().setUnitIncrement(12);
        jScrollPane.setBackground(Theme.BG_SURFACE);
        jScrollPane.getViewport().setBackground(Theme.BG_SURFACE);
        this.editSidebar.add((Component)jScrollPane, "Center");
        return this.editSidebar;
    }

    private JTextField styledField() {
        JTextField jTextField = new JTextField();
        jTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        jTextField.setAlignmentX(0.0f);
        return jTextField;
    }

    private JLabel sidebarField(String string) {
        JLabel jLabel = new JLabel(string);
        jLabel.setFont(jLabel.getFont().deriveFont(1, 11.0f));
        jLabel.setForeground(Theme.TEXT_MUTED);
        jLabel.setAlignmentX(0.0f);
        return jLabel;
    }

    private void openEditSidebar(Instance instance) {
        this.editingInstance = Instance.load(instance.name);
        this.populateEditForm(this.editingInstance);
        this.setSidebarWidth(300);
    }

    private void closeEditSidebar() {
        this.editingInstance = null;
        this.setSidebarWidth(0);
    }

    private void setSidebarWidth(int n) {
        this.editSidebar.setPreferredSize(new Dimension(n, 1));
        this.editSidebar.revalidate();
        this.editSidebar.repaint();
    }

    private void populateEditForm(Instance instance) {
        if (instance == null) {
            return;
        }
        if (this.editNameField != null) {
            this.editNameField.setText(instance.name != null ? instance.name : "");
        }
        if (this.manifest != null) {
            this.populateVersionComboInto(this.editVersionCombo, instance.lastVersion);
        } else if (this.editVersionCombo != null) {
            this.editVersionCombo.removeAllItems();
            if (instance.lastVersion != null && !instance.lastVersion.isEmpty()) {
                this.editVersionCombo.addItem(instance.lastVersion);
            }
        }
        if (this.editLoaderCombo != null) {
            this.editLoaderCombo.setSelectedItem(instance.loader);
        }
        if (this.editRamSlider != null) {
            this.editRamSlider.setValue(instance.ramGB);
        }
        if (this.editRamValueLabel != null) {
            this.editRamValueLabel.setText(instance.ramGB + "GB");
        }
        if (this.editJvmArgsField != null) {
            this.editJvmArgsField.setText(instance.jvmArgs != null ? instance.jvmArgs : "");
        }
        if (this.editJavaPathField != null) {
            this.editJavaPathField.setText(instance.javaPath != null ? instance.javaPath : "");
        }
        if (this.editAccountCombo != null) {
            boolean isEn = L10n.isEnglish();
            this.editAccountCombo.removeAllItems();
            String defaultLabel = isEn ? "(Default - use active account)" : "(Varsay\u0131lan - aktif hesab\u0131 kullan)";
            this.editAccountCombo.addItem(defaultLabel);
            for (MinecraftSession acc : AccountManager.list()) {
                if (acc.username != null && !acc.username.isBlank()) {
                    this.editAccountCombo.addItem(acc.username);
                }
            }
            if (instance.assignedAccountUsername != null && !instance.assignedAccountUsername.isBlank()) {
                this.editAccountCombo.setSelectedItem(instance.assignedAccountUsername);
            } else {
                this.editAccountCombo.setSelectedItem(defaultLabel);
            }
        }
    }

    private void saveEditSidebar() {
        Object object;
        Object object2;
        if (this.editingInstance == null) {
            return;
        }
        String string = this.editNameField != null ? this.editNameField.getText().trim() : "";
        String string2 = string;
        if (string.isEmpty() || !string.matches("[\\w\\- .]+")) {
            JOptionPane.showMessageDialog(this, "Ge\u00e7ersiz ad.", "Hata", 0);
            return;
        }
        boolean bl = !string.equals(this.editingInstance.name);
        boolean bl2 = bl;
        if (bl && InstanceManager.listNames().contains(string)) {
            JOptionPane.showMessageDialog(this, "Bu adda bir \u00f6rnek zaten var.", "Hata", 0);
            return;
        }
        boolean bl3 = this.currentInstance != null && this.currentInstance.name.equals(this.editingInstance.name);
        Instance instance = this.editingInstance;
        String string3 = instance.name;
        if (bl) {
            object2 = instance.dir();
            if (!((File)object2).renameTo((File)(object = new File(Paths.INSTANCES_DIR, string)))) {
                JOptionPane.showMessageDialog(this, "Klas\u00f6r yeniden adland\u0131r\u0131lamad\u0131.", "Hata", 0);
                return;
            }
            instance.name = string;
        }
        if ((object2 = this.editVersionCombo.getSelectedItem()) != null) {
            instance.lastVersion = (String)object2;
        }
        if ((object = this.editLoaderCombo.getSelectedItem()) != null) {
            instance.loader = (String)object;
        }
        instance.ramGB = this.editRamSlider.getValue();
        instance.jvmArgs = this.editJvmArgsField != null ? this.editJvmArgsField.getText().trim() : "";
        instance.javaPath = this.editJavaPathField != null ? this.editJavaPathField.getText().trim() : "";
        if (this.editAccountCombo != null) {
            Object selectedAccount = this.editAccountCombo.getSelectedItem();
            String selectedText = selectedAccount != null ? selectedAccount.toString() : "";
            // Combo'nun ilk ogesi her zaman "Varsayilan" secenegidir - parantezle
            // basladigi icin gercek bir kullanici adiyla cakismaz.
            instance.assignedAccountUsername = selectedText.startsWith("(") ? "" : selectedText;
        }
        instance.save();
        if (bl3) {
            this.currentInstance = instance;
            this.settings.activeInstance = instance.name;
            this.settings.save();
            this.applyInstanceToUi();
            this.modsPanel.refreshInstalled();
            this.shaderPanel.refreshInstalled();
            this.resourcepacksPanel.refreshInstalled();
        }
        this.refreshInstanceCombo(instance.name);
        this.log("Kaydedildi: " + instance.name + (String)(bl ? " (eski: " + string3 + ")" : ""));
        this.closeEditSidebar();
    }

    private void deleteEditedInstance() {
        if (this.editingInstance == null) {
            return;
        }
        String string = this.editingInstance.name;
        if (InstanceManager.listNames().size() <= 1) {
            JOptionPane.showMessageDialog(this, "En az bir \u00f6rnek kalmal\u0131.", "Uyar\u0131", 2);
            return;
        }
        int n = JOptionPane.showConfirmDialog(this, "'" + string + "' silinsin mi? (Modlar, d\u00fcnyalar dahil)", "\u00d6rnefi Sil", 0);
        if (n != 0) {
            return;
        }
        InstanceManager.delete(string);
        if (this.currentInstance != null && this.currentInstance.name.equals(string)) {
            List<String> list = InstanceManager.listNames();
            String string2 = list.isEmpty() ? "default" : list.get(0);
            this.currentInstance = Instance.load(string2);
            this.settings.activeInstance = string2;
            this.settings.save();
            this.applyInstanceToUi();
            this.modsPanel.refreshInstalled();
            this.shaderPanel.refreshInstalled();
            this.resourcepacksPanel.refreshInstalled();
        }
        this.refreshInstanceCombo(this.currentInstance != null ? this.currentInstance.name : null);
        this.log("\u00d6rnek silindi: " + string);
        this.closeEditSidebar();
    }

    private JPanel buildHomeTab() {
        JComponent jComponent;
        Object object;
        this.homeRoot = new JPanel(new BorderLayout(0, 0)){
            private float t;
            private final float[][] particles;
            private Timer animTimer;
            {
                this.t = 0.0f;
                this.particles = new float[25][4];
                for (int i = 0; i < this.particles.length; ++i) {
                    this.particles[i][0] = (float)Math.random();
                    this.particles[i][1] = (float)Math.random();
                    this.particles[i][2] = 0.2f + (float)Math.random() * 0.6f;
                    this.particles[i][3] = 2.0f + (float)Math.random() * 4.0f;
                }
                this.animTimer = new Timer(40, actionEvent -> {
                    if (!this.isShowing()) {
                        this.animTimer.stop();
                        return;
                    }
                    this.t += 0.015f;
                    for (float[] fArray : this.particles) {
                        fArray[1] = fArray[1] - fArray[2] * 0.003f;
                        fArray[0] = fArray[0] + (float)Math.sin(this.t + fArray[1] * 5.0f) * 0.001f;
                        if (!(fArray[1] < -0.05f)) continue;
                        fArray[1] = 1.05f;
                        fArray[0] = (float)Math.random();
                    }
                    this.repaint();
                });
                this.addHierarchyListener(hierarchyEvent -> {
                    if (this.isShowing() && !this.animTimer.isRunning()) {
                        this.animTimer.start();
                    }
                });
                this.animTimer.start();
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = this.getWidth();
                int n2 = this.getHeight();
                // V36.1: kullanici ana sayfa arka planina foto secmisse,
                // kaplayarak (cover) ciz + okunabilirlik icin karartma.
                java.awt.image.BufferedImage bg = MainWindow.this.homeBgImage;
                if (bg != null) {
                    int bw = bg.getWidth(), bh = bg.getHeight();
                    if (bw > 0 && bh > 0) {
                        double scale = Math.max((double)n / bw, (double)n2 / bh);
                        int dw = (int)Math.ceil(bw * scale), dh = (int)Math.ceil(bh * scale);
                        int dx = (n - dw) / 2, dy = (n2 - dh) / 2;
                        graphics2D.drawImage(bg, dx, dy, dw, dh, null);
                        graphics2D.setColor(new Color(0, 0, 0, 90));
                        graphics2D.fillRect(0, 0, n, n2);
                    }
                }
                float f = (float)Math.sin((double)this.t * 0.3) * 0.1f;
                Color color = new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), (int)(8.0 + 6.0 * Math.sin((double)this.t * 0.5)));
                Color color2 = new Color(0, 0, 0, 0);
                graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, color, (float)n * (0.5f + f), n2, color2));
                graphics2D.fillRect(0, 0, n, n2);
                for (float[] fArray : this.particles) {
                    int n3 = (int)(fArray[0] * (float)n);
                    int n4 = (int)(fArray[1] * (float)n2);
                    int n5 = (int)fArray[3];
                    float f2 = 0.15f + 0.15f * (float)Math.sin(this.t * 2.0f + fArray[0] * 10.0f);
                    graphics2D.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), (int)(f2 * 255.0f)));
                    graphics2D.fillOval(n3, n4, n5, n5);
                }
                graphics2D.dispose();
                super.paintComponent(graphics);
            }
        };
        this.homeRoot.setOpaque(false);
        JPanel jPanel = new JPanel(new BorderLayout(20, 0)){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = this.getWidth();
                int n2 = this.getHeight();
                graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 15), 0.0f, n2, new Color(0, 0, 0, 0)));
                graphics2D.fillRoundRect(12, 8, n - 24, n2 - 16, 20, 20);
                graphics2D.dispose();
            }
        };
        jPanel.setOpaque(false);
        jPanel.setBorder(new EmptyBorder(24, 28, 0, 28));
        jPanel.setPreferredSize(new Dimension(0, 230));
        JPanel jPanel2 = new JPanel(){
            private float breathe = 0.0f;
            private List<float[]> particles = new ArrayList<float[]>();
            {
                Random random = new Random();
                for (int i = 0; i < 12; ++i) {
                    this.particles.add(new float[]{random.nextFloat(), random.nextFloat(), random.nextFloat() * 2.0f + 0.5f, random.nextFloat() * 3.0f + 1.0f});
                }
                Timer[] timerArray = new Timer[]{null};
                timerArray[0] = new Timer(30, actionEvent -> {
                    if (!this.isShowing()) {
                        timerArray[0].stop();
                        return;
                    }
                    this.breathe = (float)(Math.sin((double)System.currentTimeMillis() / 1200.0) * 4.0);
                    this.repaint();
                });
                this.addHierarchyListener(hierarchyEvent -> {
                    if (this.isShowing() && !timerArray[0].isRunning()) {
                        timerArray[0].start();
                    }
                });
                timerArray[0].start();
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = this.getWidth();
                int n2 = this.getHeight();
                graphics2D.setColor(Theme.BG_ELEVATED);
                graphics2D.fillRoundRect(0, 0, n, n2, 20, 20);
                graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, new Color(255, 255, 255, 15), 0.0f, (float)n2 * 0.4f, new Color(255, 255, 255, 0)));
                graphics2D.fillRoundRect(0, 0, n, (int)((float)n2 * 0.4f), 20, 20);
                long l = System.currentTimeMillis();
                for (float[] fArray : this.particles) {
                    float f = fArray[0] * (float)n + (float)Math.sin((float)l / (fArray[3] * 500.0f)) * 15.0f;
                    float f2 = (fArray[1] * (float)n2 + (float)l / (fArray[2] * 80.0f)) % (float)n2;
                    float f3 = 0.15f + 0.1f * (float)Math.sin((double)l / 1000.0 + (double)(fArray[0] * 6.0f));
                    graphics2D.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), (int)(f3 * 255.0f)));
                    graphics2D.fillOval((int)f, (int)f2, 4, 4);
                }
                int n3 = n / 2;
                int n4 = n2 / 2 - 10 + (int)this.breathe;
                int n5 = 40;
                graphics2D.setColor(new Color(0, 0, 0, 40));
                graphics2D.fillOval(n3 - n5 + 2, n4 - n5 + 2, n5 * 2, n5 * 2);
                // V36.4: fotograf varken accent daire + parlaklik cizilmez -
                // fotograf DAIRENIN TAMAMINI kaplar (hic mor kenar kalmaz).
                java.awt.image.BufferedImage photo = MainWindow.this.loadAccountPhoto();
                if (photo != null) {
                    Graphics2D ph = (Graphics2D)graphics2D.create();
                    ph.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    ph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    ph.setClip(new java.awt.geom.Ellipse2D.Float(n3 - n5, n4 - n5, n5 * 2, n5 * 2));
                    ph.drawImage(photo, n3 - n5, n4 - n5, n5 * 2, n5 * 2, null);
                    ph.dispose();
                }
                else {
                    graphics2D.setColor(Theme.ACCENT);
                    graphics2D.fillOval(n3 - n5, n4 - n5, n5 * 2, n5 * 2);
                    graphics2D.setColor(new Color(255, 255, 255, 30));
                    graphics2D.fillOval(n3 - n5, n4 - n5, n5 * 2, n5 / 2);
                    String string = MainWindow.this.session != null ? MainWindow.this.session.username : "?";
                    String string2 = string.substring(0, 1).toUpperCase();
                    graphics2D.setColor(Color.WHITE);
                    graphics2D.setFont(new Font("SansSerif", 1, 32));
                    FontMetrics fontMetrics = graphics2D.getFontMetrics();
                    graphics2D.drawString(string2, n3 - fontMetrics.stringWidth(string2) / 2, n4 + fontMetrics.getAscent() / 2 - 2);
                }
                graphics2D.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        jPanel2.setPreferredSize(new Dimension(200, 0));
        jPanel2.setOpaque(false);
        jPanel.add((Component)jPanel2, "West");
        JPanel jPanel3 = new JPanel();
        jPanel3.setLayout(new BoxLayout(jPanel3, 1));
        jPanel3.setOpaque(false);
        jPanel3.setBorder(new EmptyBorder(20, 8, 16, 0));
        this.accountLabel = new JLabel(L10n.get("account.not_logged_in"));
        this.accountLabel.setFont(new Font("SansSerif", 1, 26));
        this.accountLabel.setForeground(Theme.TEXT_PRIMARY);
        this.accountLabel.setAlignmentX(0.0f);
        jPanel3.add(this.accountLabel);
        jPanel3.add(Box.createVerticalStrut(4));
        JLabel jLabel = new JLabel("  ");
        jLabel.setFont(jLabel.getFont().deriveFont(0, 11.0f));
        jLabel.setForeground(Theme.TEXT_MUTED);
        jLabel.setAlignmentX(0.0f);
        jPanel3.add(jLabel);
        jPanel3.add(Box.createVerticalStrut(20));
        JPanel jPanel4 = new JPanel(new GridLayout(1, 2, 10, 0));
        jPanel4.setOpaque(false);
        jPanel4.setMaximumSize(new Dimension(320, 38));
        jPanel4.setAlignmentX(0.0f);
        this.offlineLoginButton = UiFx.ghostButton(com.lubv.launcher.core.L10n.isEnglish() ? "Offline Login" : "\u00c7evrimd\u0131\u015f\u0131 Giri\u015f");
        this.msLoginButton = UiFx.accentButton(com.lubv.launcher.core.L10n.isEnglish() ? "Microsoft Login" : "Microsoft Giri\u015f");
        this.offlineLoginButton.addActionListener(actionEvent -> this.doOfflineLogin());
        this.msLoginButton.addActionListener(actionEvent -> this.doMsLogin());
        jPanel4.add(this.offlineLoginButton);
        jPanel4.add(this.msLoginButton);
        jPanel3.add(jPanel4);
        jPanel3.add(Box.createVerticalStrut(10));
        jPanel3.add(Box.createVerticalStrut(10));
        this.playTimeLabel = new JLabel("Oynanmamis");
        this.playTimeLabel.setFont(this.playTimeLabel.getFont().deriveFont(0, 11.0f));
        this.playTimeLabel.setForeground(Theme.TEXT_MUTED);
        this.playTimeLabel.setAlignmentX(0.0f);
        jPanel3.add(this.playTimeLabel);
        this.versionHistoryLabel = new JLabel("");
        this.versionHistoryLabel.setFont(this.versionHistoryLabel.getFont().deriveFont(0, 11.0f));
        this.versionHistoryLabel.setForeground(Theme.TEXT_MUTED);
        this.versionHistoryLabel.setAlignmentX(0.0f);
        jPanel3.add(this.versionHistoryLabel);
        jPanel3.add(Box.createVerticalStrut(16));
        JPanel jPanel5 = new JPanel(new FlowLayout(0, 0, 0));
        jPanel5.setOpaque(false);
        jPanel5.setMaximumSize(new Dimension(320, 24));
        jPanel5.setAlignmentX(0.0f);
        JLabel jLabel2 = new JLabel("\u25b6 ");
        jLabel2.setFont(jLabel2.getFont().deriveFont(1, 12.0f));
        jLabel2.setForeground(Theme.ACCENT);
        JLabel jLabel3 = new JLabel(this.currentInstance != null ? this.currentInstance.name : "Yok");
        jLabel3.setFont(jLabel3.getFont().deriveFont(1, 12.0f));
        jLabel3.setForeground(Theme.ACCENT_BRIGHT);
        jPanel5.add(jLabel2);
        jPanel5.add(jLabel3);
        jPanel3.add(jPanel5);
        jPanel.add((Component)jPanel3, "Center");
        this.homeRoot.add((Component)jPanel, "North");
        JPanel jPanel6 = new JPanel();
        jPanel6.setLayout(new BoxLayout(jPanel6, 1));
        jPanel6.setOpaque(false);
        jPanel6.setBorder(new EmptyBorder(16, 28, 0, 28));
        JPanel jPanel7 = MainWindow.buildCard();
        jPanel7.setLayout(new BorderLayout(12, 0));
        jPanel7.setBorder(new EmptyBorder(10, 16, 10, 16));
        jPanel7.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        jPanel7.setAlignmentX(0.0f);
        JLabel jLabel4 = UiFx.label("S\u00fcr\u00fcm");
        jLabel4.setFont(jLabel4.getFont().deriveFont(1, 12.0f));
        this.versionCombo = new JComboBox();
        this.versionCombo.setLightWeightPopupEnabled(true);
        this.versionCombo.setPreferredSize(new Dimension(200, 30));
        this.versionCombo.addActionListener(actionEvent -> {
            if (!this.comboEventsEnabled) {
                return;
            }
            String string = (String)this.versionCombo.getSelectedItem();
            if (string == null || string.isBlank() || this.currentInstance == null) {
                return;
            }
            if (string.equals(this.currentInstance.lastVersion)) {
                return;
            }
            this.currentInstance.lastVersion = string;
            this.currentInstance.save();
            this.log("S\u00fcr\u00fcm defi\u015fti: " + string + " \u2014 modlar g\u00fcncelleniyor...");
            // V35.3 YARIS KORUMASI: surum degisim epoch'unu arttir. Devam eden
            // eski-surum kurulumlari (indirme ortasinda bile) iptal olur;
            // yanlis surum jar artik yarista kazanan olamaz.
            ModManager.onMcVersionSwitched();
            // V34.8 HIZ: secilen surumun JSON'unu arkada one tusle - oyuna
            // basinca version JSON ag'dan beklenmez (disk cache'ten gelir).
            new Thread(() -> {
                try {
                    VersionManifest.Entry pe = this.manifest != null ? this.manifest.find(string) : null;
                    if (pe != null && pe.url != null) {
                        com.lubv.launcher.game.GameVersion.prefetchVersionJsonAsync(pe.url, string);
                    }
                }
                catch (Throwable ignored) {
                    // one-yukleme hicbir seyi bozmasin
                }
            }, "version-json-prefetch").start();
            // V36 GERI ALMA: guncellemeden once calisan mod setini snapshot'la.
            // Yeni set crash yaratirsa kullanici tek tikla geri donebilir.
            try {
                File snapModsDir = this.currentInstance.modsDir();
                if (com.lubv.launcher.mods.ModRollbackManager.snapshot(snapModsDir, string)) {
                    this.log("[Geri Alma] Eski mod seti yedeklendi - sorun olursa tek tikla geri donebilirsin.");
                }
            }
            catch (Throwable snapErr) {
                // snapshot basarisizsa guncelleme yine de devam etsin
            }
            new Thread(() -> this.autoUpdateAllForVersion(string)).start();
        });
        this.showSnapshotsBox = new JCheckBox("Snapshot'lar\u0131 g\u00f6ster");
        this.showSnapshotsBox.setOpaque(false);
        this.showSnapshotsBox.setFont(this.showSnapshotsBox.getFont().deriveFont(11.0f));
        this.showSnapshotsBox.addActionListener(actionEvent -> {
            this.comboEventsEnabled = false;
            this.populateVersionCombo();
            this.comboEventsEnabled = true;
        });
        JPanel jPanel8 = new JPanel(new FlowLayout(2, 8, 0));
        jPanel8.setOpaque(false);
        jPanel8.add(this.showSnapshotsBox);
        jPanel8.add(this.versionCombo);
        jPanel7.add((Component)jLabel4, "West");
        jPanel7.add((Component)jPanel8, "East");
        jPanel6.add(jPanel7);
        jPanel6.add(Box.createVerticalStrut(8));
        JPanel jPanel9 = new JPanel(new GridLayout(1, 2, 10, 0));
        jPanel9.setOpaque(false);
        jPanel9.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        jPanel9.setAlignmentX(0.0f);
        JPanel jPanel10 = MainWindow.buildCard();
        jPanel10.setLayout(new BorderLayout(12, 0));
        jPanel10.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel jLabel5 = UiFx.label("Loader");
        jLabel5.setFont(jLabel5.getFont().deriveFont(1, 12.0f));
        String[] stringArray = new String[]{"Vanilla", "Fabric", "Forge", "NeoForge", "OptiFine"};
        JPanel jPanel11 = new JPanel(new GridLayout(1, 5, 3, 0));
        jPanel11.setOpaque(false);
        ButtonGroup buttonGroup = new ButtonGroup();
        this.loaderButtons = new JToggleButton[stringArray.length];
        for (int i = 0; i < stringArray.length; ++i) {
            object = stringArray[i];
            final String loaderName = (String)object;
            int n = i;
            jComponent = new JToggleButton((String)object){
                private float hover;
                private Timer ht;
                {
                    this.hover = 0.0f;
                    this.setContentAreaFilled(false);
                    this.setBorderPainted(false);
                    this.setFocusPainted(false);
                    this.setOpaque(false);
                    this.setCursor(Cursor.getPredefinedCursor(12));
                    this.addMouseListener(new MouseAdapter(){

                        @Override
                        public void mouseEntered(MouseEvent mouseEvent) {
                            anim(1.0f);
                        }

                        @Override
                        public void mouseExited(MouseEvent mouseEvent) {
                            anim(0.0f);
                        }
                    });
                }

                public void anim(float f) {
                    if (this.ht != null) {
                        this.ht.stop();
                    }
                    float f2 = this.hover;
                    long l = System.currentTimeMillis();
                    this.ht = new Timer(16, null);
                    this.ht.addActionListener(actionEvent -> {
                        float f3 = Math.min(1.0f, (float)(System.currentTimeMillis() - l) / 130.0f);
                        this.hover = f2 + (f - f2) * UiFx.easeOutCubic(f3);
                        this.repaint();
                        if (f3 >= 1.0f) {
                            this.ht.stop();
                        }
                    });
                    this.ht.start();
                }

                @Override
                protected void paintComponent(Graphics graphics) {
                    Graphics2D graphics2D = (Graphics2D)graphics.create();
                    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int n = this.getWidth();
                    int n2 = this.getHeight();
                    boolean bl = this.isSelected();
                    // ONCEDEN bu 4 renk sabitti (hep mor) - tema ne olursa
                    // olsun secili loader butonu hep ayni gorunuyordu.
                    // Artik Theme.ACCENT'ten turetiliyor.
                    Color themeAccent = Theme.ACCENT != null ? Theme.ACCENT : new Color(160, 60, 240);
                    Color color = MainWindow.darken(themeAccent, 0.35f);
                    Color color2 = MainWindow.darken(themeAccent, 0.10f);
                    Color color3 = MainWindow.brighten(themeAccent, 0.25f);
                    Color color4 = themeAccent;
                    if (bl) {
                        graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, UiFx.lerp(color2, color3, this.hover * 0.4f), 0.0f, n2, UiFx.lerp(color, color2, this.hover * 0.4f)));
                        graphics2D.fillRoundRect(0, 0, n, n2, 10, 10);
                        graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, new Color(255, 255, 255, 45), 0.0f, (float)n2 * 0.45f, new Color(255, 255, 255, 0)));
                        graphics2D.fillRoundRect(0, 0, n, (int)((float)n2 * 0.5f), 10, 10);
                        graphics2D.setColor(new Color(color4.getRed(), color4.getGreen(), color4.getBlue(), (int)(80.0f + 40.0f * this.hover)));
                        graphics2D.setStroke(new BasicStroke(1.5f));
                        graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 10, 10);
                        if (this.hover > 0.01f) {
                            graphics2D.setColor(new Color(color3.getRed(), color3.getGreen(), color3.getBlue(), (int)(30.0f * this.hover)));
                            graphics2D.setStroke(new BasicStroke(2.5f));
                            graphics2D.drawRoundRect(-2, -2, n + 3, n2 + 3, 12, 12);
                        }
                        graphics2D.dispose();
                        super.paintComponent(graphics);
                    } else {
                        float f = this.hover;
                        graphics2D.setColor(new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), (int)(15.0f + 25.0f * f)));
                        graphics2D.fillRoundRect(0, 0, n, n2, 10, 10);
                        // TEMA BUG FIXI: hover kenari sabit mor (140,80,200) idi -
                        // artik temanin parlak vurgusuna kayiyor.
                        graphics2D.setColor(UiFx.lerp(Theme.BG_BORDER, Theme.ACCENT_BRIGHT != null ? Theme.ACCENT_BRIGHT : Theme.ACCENT, f * 0.8f));
                        graphics2D.setStroke(new BasicStroke(1.0f));
                        graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 10, 10);
                        graphics2D.dispose();
                        super.paintComponent(graphics);
                    }
                }

                @Override
                public boolean isOpaque() {
                    return false;
                }
            };
            jComponent.setFont(jComponent.getFont().deriveFont(0, 11.0f));
            jComponent.setPreferredSize(new Dimension(68, 26));
            ((AbstractButton)jComponent).addActionListener(arg_0 -> this.handleLoaderButtonClick(loaderName, arg_0));
            buttonGroup.add((AbstractButton)jComponent);
            jPanel11.add(jComponent);
            this.loaderButtons[i] = (JToggleButton)jComponent;
            if (!loaderName.equalsIgnoreCase(this.currentInstance.loader)) continue;
            ((AbstractButton)jComponent).setSelected(true);
        }
        this.loaderCombo = new JComboBox<String>(stringArray);
        this.loaderCombo.setSelectedItem(this.currentInstance.loader);
        this.loaderCombo.setVisible(false);
        jPanel10.add((Component)jLabel5, "West");
        jPanel10.add((Component)jPanel11, "East");
        jPanel9.add(jPanel10);
        JPanel jPanel12 = MainWindow.buildCard();
        jPanel12.setLayout(new BorderLayout(12, 0));
        jPanel12.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel jLabel6 = UiFx.label("RAM");
        ((JComponent)jLabel6).setFont(((Component)jLabel6).getFont().deriveFont(1, 12.0f));
        this.ramSlider = new JSlider(1, 16, this.currentInstance.ramGB);
        this.ramSlider.setOpaque(false);
        this.ramSlider.setPreferredSize(new Dimension(120, 30));
        this.ramSlider.addChangeListener(changeEvent -> this.ramValueLabel.setText(this.ramSlider.getValue() + "GB"));
        this.ramValueLabel = new JLabel(this.currentInstance.ramGB + "GB");
        this.ramValueLabel.setForeground(Theme.TEXT_PRIMARY);
        // "Oner" butonu yerine surekli gorunen ONERILEN RAM etiketi:
        // sistem toplam fiziksel RAM'inin guvenli %60'i (2-12GB), slider
        // degistikce de guncellenir.
        final JLabel ramRecLabel = new JLabel();
        long totalRamBytes = 0L;
        try {
            OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
            Method method = operatingSystemMXBean.getClass().getMethod("getTotalPhysicalMemorySize", new Class[0]);
            method.setAccessible(true);
            totalRamBytes = (Long)method.invoke((Object)operatingSystemMXBean, new Object[0]);
        }
        catch (Exception exception) {
            totalRamBytes = Runtime.getRuntime().maxMemory() * 4L;
        }
        final int totalRamGB = (int)(totalRamBytes / 0x40000000L);
        final int recRam = Math.max(2, Math.min(12, (int)((double)totalRamGB * 0.6)));
        ramRecLabel.setText((L10n.isEnglish() ? "Recommended RAM: " : "\u00d6nerilen RAM: ") + recRam + "GB");
        ramRecLabel.setForeground(Theme.TEXT_SECONDARY);
        ramRecLabel.setFont(ramRecLabel.getFont().deriveFont(0, 11.0f));
        ramRecLabel.setToolTipText((L10n.isEnglish() ? "Safe max RAM for your system (" : "Sisteminiz i\u00e7in g\u00fcvenli maksimum RAM (") + totalRamGB + (L10n.isEnglish() ? "GB total, 60% rule)" : "GB toplam, %60 kural\u0131)") );
        jPanel12.add((Component)jLabel6, "West");
        JPanel jPanel13 = new JPanel(new FlowLayout(2, 6, 0));
        jPanel13.setOpaque(false);
        jPanel13.add((Component)ramRecLabel);
        jPanel13.add(this.ramSlider);
        jPanel13.add(this.ramValueLabel);
        jPanel12.add((Component)jPanel13, "East");
        jPanel9.add(jPanel12);
        jPanel6.add(jPanel9);
        this.homeRoot.add((Component)jPanel6, "Center");
        JPanel jPanel14 = new JPanel(){
            private float t = 0.0f;
            private int dotCount = 3;
            private String[] messages = new String[]{"Complex Launcher baslatiliyor...", "Modlar kontrol ediliyor...", "Surumler yukleniyor...", "Minecraft hazirlan\u0131yor...", "Surfing the waves of code..."};
            private int msgIndex = 0;
            private long lastMsgChange = System.currentTimeMillis();
            private Timer timer = new Timer(50, actionEvent -> {
                if (!this.isShowing()) {
                    this.timer.stop();
                    return;
                }
                this.t += 0.02f;
                if (System.currentTimeMillis() - this.lastMsgChange > 3000L) {
                    this.msgIndex = (this.msgIndex + 1) % this.messages.length;
                    this.lastMsgChange = System.currentTimeMillis();
                }
                this.repaint();
            });
            {
                this.addHierarchyListener(hierarchyEvent -> {
                    if (this.isShowing() && !this.timer.isRunning()) {
                        this.timer.start();
                    }
                });
                this.timer.start();
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                int n;
                float f;
                float f2;
                float f3;
                int n2;
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n3 = this.getWidth();
                int n4 = this.getHeight();
                graphics2D.setColor(new Color(Theme.BG_SURFACE.getRed(), Theme.BG_SURFACE.getGreen(), Theme.BG_SURFACE.getBlue(), 120));
                graphics2D.fillRoundRect(0, 0, n3, n4, 14, 14);
                graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, new Color(255, 255, 255, 8), 0.0f, (float)n4 * 0.4f, new Color(255, 255, 255, 0)));
                graphics2D.fillRoundRect(0, 0, n3, (int)((float)n4 * 0.45f), 14, 14);
                for (n2 = 0; n2 < 12; ++n2) {
                    f3 = (float)((double)n3 * (0.05 + 0.9 * (((double)n2 * 0.137 + (double)this.t * 0.2) % 1.0)));
                    f2 = (float)((double)n4 * (0.15 + 0.7 * Math.sin((double)this.t * 1.5 + (double)n2 * 1.2)));
                    f = 2.0f + 2.0f * (float)Math.sin((double)this.t * 2.5 + (double)n2);
                    n = (int)(15.0 + 25.0 * Math.abs(Math.sin((double)this.t * 1.8 + (double)n2 * 0.9)));
                    graphics2D.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), n));
                    graphics2D.fillOval((int)f3, (int)f2, (int)f, (int)f);
                }
                for (n2 = 0; n2 < 3; ++n2) {
                    f3 = this.t * 3.5f + (float)n2 * 1.2f;
                    f2 = 0.25f + 0.75f * (float)Math.abs(Math.sin(f3));
                    f = 0.8f + 0.2f * (float)Math.sin(f3);
                    n = (int)(8.0f * f);
                    int n5 = (int)(f2 * 255.0f);
                    graphics2D.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), n5));
                    int n6 = n3 / 2 - 24 + n2 * 24;
                    int n7 = n4 / 2 - 4;
                    graphics2D.fillOval(n6, n7, n, n);
                }
                float f4 = 0.5f + 0.5f * (float)Math.abs(Math.sin((double)this.t * 0.8));
                graphics2D.setColor(new Color(Theme.TEXT_SECONDARY.getRed(), Theme.TEXT_SECONDARY.getGreen(), Theme.TEXT_SECONDARY.getBlue(), (int)(f4 * 255.0f)));
                graphics2D.setFont(new Font("SansSerif", 0, 11));
                FontMetrics fontMetrics = graphics2D.getFontMetrics();
                String string = this.messages[this.msgIndex];
                graphics2D.drawString(string, (n3 - fontMetrics.stringWidth(string)) / 2, n4 / 2 + 22);
                graphics2D.setColor(Theme.TEXT_MUTED);
                graphics2D.setFont(new Font("SansSerif", 0, 10));
                FontMetrics fontMetrics2 = graphics2D.getFontMetrics();
                // ONEMLI DUZELTME: Burada once "v15" yaziyordu - eski bir
                // hata ayiklama kalintisi. Artik gercek surum numarasi
                // UpdateManager.CURRENT_VERSION'dan aliniyor (v29).
                String string2 = "v" + com.lubv.launcher.update.UpdateManager.CURRENT_VERSION;
                graphics2D.drawString(string2, n3 - fontMetrics2.stringWidth(string2) - 12, n4 - 8);
                graphics2D.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        jPanel14.setPreferredSize(new Dimension(0, 65));
        jPanel14.setBorder(new EmptyBorder(0, 28, 0, 28));
        jComponent = new JPanel(new BorderLayout()){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setColor(new Color(Theme.BG_BASE.getRed(), Theme.BG_BASE.getGreen(), Theme.BG_BASE.getBlue(), 150));
                graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 12, 12);
                graphics2D.dispose();
            }
        };
        jComponent.setOpaque(false);
        jComponent.setBorder(new EmptyBorder(4, 28, 0, 28));
        this.logArea = new JTextArea();
        this.logArea.setEditable(false);
        this.logArea.setFont(new Font("Consolas", 0, 10));
        this.logArea.setBackground(new Color(0, 0, 0, 0));
        this.logArea.setForeground(Theme.TEXT_MUTED);
        this.logArea.setBorder(new EmptyBorder(6, 10, 6, 10));
        this.logArea.setRows(3);
        jComponent.add((Component)UiFx.cleanScroll(this.logArea), "Center");
        JPanel jPanel15 = new JPanel(new BorderLayout());
        jPanel15.setOpaque(false);
        jPanel15.add((Component)jPanel14, "North");
        jPanel15.add((Component)jComponent, "Center");
        this.homeRoot.add((Component)jPanel15, "South");
        JPanel jPanel16 = new JPanel(new BorderLayout(16, 0));
        jPanel16.setOpaque(false);
        jPanel16.setBorder(new EmptyBorder(12, 28, 20, 28));
        JPanel jPanel17 = new JPanel(new BorderLayout(0, 4));
        jPanel17.setOpaque(false);
        this.progressStageLabel = new JLabel(" ");
        this.progressStageLabel.setFont(this.progressStageLabel.getFont().deriveFont(11.0f));
        this.progressStageLabel.setForeground(Theme.TEXT_MUTED);
        this.progressBar = UiFx.progressPill();
        jPanel17.add((Component)this.progressStageLabel, "North");
        jPanel17.add((Component)this.progressBar, "South");
        this.launchButton = UiFx.launchButton("\u25b6  " + L10n.get("launch"));
        this.launchButton.setPreferredSize(new Dimension(180, 48));
        this.launchButton.addActionListener(actionEvent -> this.doLaunch());
        // V34: Ikinci Client - ayni instance'in ikinci bir oyun sureci.
        // Ana LAUNCH'tan tamamen bagimsiz: ilk oyun acikken de
        // kullanilabilir (coklu hesap / cift client). Ana baslatma
        // kilidini (this.launching) ve Stop butonunu etkilemez.
        this.secondClientButton = UiFx.ghostButton(L10n.isEnglish() ? "\u25d6  2nd Client" : "\u25d6  2. Client");
        this.secondClientButton.setPreferredSize(new Dimension(150, 48));
        this.secondClientButton.setToolTipText(L10n.isEnglish()
            ? "Launch a second copy of this instance (multi-client)"
            : "Bu instance'in ikinci bir kopyasini baslatir (coklu client)");
        this.secondClientButton.addActionListener(actionEvent -> this.showSecondClientDialog());
        this.terminateButton = UiFx.dangerButton(L10n.isEnglish() ? "\u25a0  Stop" : "\u25a0  Sonland\u0131r");
        this.terminateButton.setPreferredSize(new Dimension(130, 48));
        this.terminateButton.setVisible(false);
        this.terminateButton.setToolTipText(L10n.isEnglish() ? "Force-quit the running game" : "\u00c7al\u0131\u015fan oyunu zorla kapat");
        this.terminateButton.addActionListener(actionEvent -> {
            boolean isEn = L10n.isEnglish();
            Process process = this.currentGameProcess;
            if (process == null || !process.isAlive()) {
                // ONCEKI HATA: process referansi olduyse buton sessizce hicbir
                // sey yapmiyor ve kullanici "düzgün çalismiyor" saniyordu.
                // Artik net bir geri bildirim veriyoruz ve butonu gizliyoruz.
                this.terminateButton.setVisible(false);
                this.log(isEn ? "[Stop] The game is not running anymore." : "[Sonland\u0131r] Oyun zaten \u00e7al\u0131\u015fm\u0131yor.");
                return;
            }
            int n = JOptionPane.showConfirmDialog(this,
                isEn ? "Force-quit the game? Unsaved progress will be lost." : "Oyunu zorla kapat? Kaydedilmemi\u015f ilerleme kaybolabilir.",
                isEn ? "Stop Game" : "Oyunu Sonland\u0131r", 0, 2);
            if (n != 0) {
                return;
            }
            // ONEMLI DUZELTME: Sadece process.destroyForcibly() cagirilmissa
            // Minecraft'in alt surecleri (crash-handler, bazı loaderlarin
            // baslattigi yardimci JVM'ler) hayatta kaliyor ve oyun
            // kapanmis gibi gorunmuyordu. Once tum alt surecler, sonra
            // ana surec sonlandirilir; 3 sn sonra hala yasiyorsa tekrar
            // deneme yapilir.
            final Process proc = process;
            new Thread(() -> {
                try {
                    proc.descendants().forEach(ProcessHandle::destroyForcibly);
                    proc.destroy();
                    try {
                        if (!proc.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                            proc.descendants().forEach(ProcessHandle::destroyForcibly);
                            proc.destroyForcibly();
                        }
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        proc.destroyForcibly();
                    }
                    this.log(isEn ? "[Stop] Game was force-quit." : "[Sonland\u0131r] Oyun zorla kapat\u0131ld\u0131.");
                }
                catch (Exception ex) {
                    this.log((isEn ? "[Stop] Could not stop game: " : "[Sonland\u0131r] Oyun sonland\u0131r\u0131lamad\u0131: ") + ex.getMessage());
                }
                finally {
                    SwingUtilities.invokeLater(() -> {
                        if (this.terminateButton != null) {
                            this.terminateButton.setVisible(false);
                        }
                    });
                }
            }, "game-stop").start();
        });
        JPanel jPanel18 = new JPanel(new FlowLayout(2, 8, 0));
        jPanel18.setOpaque(false);
        jPanel18.add(this.terminateButton);
        jPanel18.add(this.secondClientButton);
        jPanel18.add(this.launchButton);
        jPanel16.add((Component)jPanel17, "Center");
        jPanel16.add((Component)jPanel18, "East");
        this.homeRoot.add((Component)jPanel16, "South");
        return this.homeRoot;
    }

    private javax.swing.JComponent buildSettingsTab() {
        AbstractButton abstractButton;
        Object object;
        Object object2;
        this.settingsRoot = new JPanel();
        this.settingsRoot.setLayout(new BoxLayout(this.settingsRoot, 1));
        this.settingsRoot.setOpaque(false);
        this.settingsRoot.setBorder(new EmptyBorder(20, 20, 20, 20));
        // V34.7 AYAR ARAMA: ustte arama kutusu - yazilan metinle eslesmeyen
        // satirlar gizlenir (bilesenin preferansli etiket + kendi adina gore).
        // Bos/kisa girdi ( <2 karakter ) = tum ayarlar gorunur. ESC temizler.
        JPanel searchWrap = new JPanel(new BorderLayout(8, 0));
        searchWrap.setOpaque(false);
        searchWrap.setAlignmentX(0.0f);
        searchWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        javax.swing.JTextField settingsSearchField = UiFx.searchField(L10n.isEnglish() ? "Search settings... (java, ram, theme, mods)" : "Ayarlarda ara... (java, ram, tema, modlar)");
        settingsSearchField.putClientProperty("JTextField.placeholderText", L10n.isEnglish() ? "Search settings... (java, ram, theme, mods)" : "Ayarlarda ara... (java, ram, tema, modlar)");
        searchWrap.add((Component)settingsSearchField, "Center");
        this.settingsRoot.add(searchWrap);
        this.settingsRoot.add(Box.createVerticalStrut(8));
        JPanel jPanel = MainWindow.buildCard();
        jPanel.setLayout(new GridBagLayout());
        jPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        jPanel.setAlignmentX(0.0f);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(9, 8, 9, 8);
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        int n = 0;
        boolean settingsEn = L10n.isEnglish();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.gridwidth = 1;
        jPanel.add((Component)UiFx.label(settingsEn ? "Custom Java path (empty = auto):" : "Ozel Java yolu (bos = otomatik):"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        this.javaPathField = new JTextField(this.currentInstance.javaPath, 25);
        jPanel.add((Component)this.javaPathField, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(L10n.isEnglish() ? "Extra JVM arguments:" : "Ekstra JVM arg\u00fcmanlar\u0131:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        this.jvmArgsField = new JTextField(this.currentInstance.jvmArgs, 25);
        jPanel.add((Component)this.jvmArgsField, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(settingsEn ? "JVM Profile:" : "JVM Profili:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        JButton jButton = UiFx.ghostButton(settingsEn ? "Choose Profile" : "Profil Sec");
        jButton.setToolTipText(settingsEn ? "Pick from ready-made JVM argument profiles" : "Hazir JVM arguman profillerinden sec");
        jButton.addActionListener(actionEvent -> this.showJvmProfileMenu(jButton));
        jPanel.add((Component)jButton, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        // V40.1 GRID FIX: "gridy = n" (artirmasiz) ile Profil butonuyla
        // AYNI satira dustu; iki etiket tek satirda cakisiyordu.
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(settingsEn ? "Pre-launch command:" : "Oyun oncesi komut (Pre-launch):"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        this.preLaunchField = new JTextField(this.currentInstance.preLaunchCmd != null ? this.currentInstance.preLaunchCmd : "", 25);
        this.preLaunchField.setToolTipText(settingsEn ? "Command to run before the game starts" : "Oyun baslamadan once calistirilacak komut");
        jPanel.add((Component)this.preLaunchField, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        int n2 = ++n;
        gridBagConstraints.gridy = n2;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(settingsEn ? "Instance folder:" : "Ornek klasoru:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        JPanel jPanel2 = new JPanel(new BorderLayout(6, 0));
        jPanel2.setOpaque(false);
        this.gameDirField = new JTextField(this.currentInstance.dir().getAbsolutePath(), 25);
        this.gameDirField.setEditable(false);
        JButton jButton2 = UiFx.ghostButton(settingsEn ? "Open" : "Ac");
        jButton2.addActionListener(actionEvent -> {
            try {
                Desktop.getDesktop().open(this.currentInstance.dir());
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
        jPanel2.add((Component)this.gameDirField, "Center");
        jPanel2.add((Component)jButton2, "East");
        jPanel.add((Component)jPanel2, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        int n3 = ++n;
        gridBagConstraints.gridy = n3;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(L10n.isEnglish() ? "Theme:" : "Tema:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        // TEMA BUG FIXI: kmb listesi artik Theme.Preset.values()'dan dinamik
        // kuruluyor - eski 11 preset + 10 yeni Minecraft temasi + Custom.
        // Eski kayitli temalar (gorunen ad ile kaydedilmis) fromName ile
        // ayni sekilde bulunur; geriye uyumluluk korunur.
        java.util.List<String> themeNames = new java.util.ArrayList<>();
        for (Theme.Preset p : Theme.Preset.values()) {
            themeNames.add(p.displayName);
        }
        themeNames.add(L10n.isEnglish() ? "\u2605 Custom" : "\u2605 Ozel");
        this.themeCombo = new JComboBox<String>(themeNames.toArray(new String[0]));
        JComboBox<String> jComboBox = this.themeCombo;
        jComboBox.setLightWeightPopupEnabled(true);
        this.themeCombo.setSelectedItem("Custom".equalsIgnoreCase(this.settings.theme)
            ? (L10n.isEnglish() ? "\u2605 Custom" : "\u2605 Ozel")
            : Theme.Preset.fromName((String)this.settings.theme).displayName);
        jPanel.add(this.themeCombo, gridBagConstraints);
        // TEMA BUG FIXI: kmb'den tema secer secmez uygula + kaydet. Onceden
        // listener hic yoktu - secim sadece "Ayarları Kaydet" basilinca ve
        // sadece yazi olarak saklaniyordu; yeni MC temalarinin enum adi
        // ("GRASS_BLOCK") gorunen adla ("Grass Block") eslesmedigi icin
        // yeniden acilista tema yuklenemiyordu. Artik: (1) aninda onizleme,
        // (2) kayit her zaman enum adiyla (fromName tum yazim sekillerini
        // cozer), (3) Chrome kromu aninda tazelenir.
        this.themeCombo.addActionListener(actionEvent -> {
            String sel = (String)this.themeCombo.getSelectedItem();
            if (sel == null) {
                return;
            }
            if (sel.contains("\u2605")) {
                return; // Custom temasi Oluşturucu ile yonetilir
            }
            Theme.Preset preset = Theme.Preset.fromName(sel);
            this.settings.theme = preset.name();
            this.settings.save();
            this.applyThemeLive(() -> Theme.apply(preset));
            this.refreshThemeChrome();
            SwingUtilities.updateComponentTreeUI(this);
        });
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = ++n3;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        JButton themeCreatorBtn = UiFx.ghostButton(L10n.isEnglish() ? "Theme Creator (Export / Import)..." : "Tema Olusturucu (Disa/Ice Aktar)...");
        themeCreatorBtn.setToolTipText(L10n.isEnglish()
            ? "Create your own theme, randomize, export/import as .json"
            : "Kendi temani olustur, rastgele uret, .json olarak disa/ice aktar");
        themeCreatorBtn.addActionListener(actionEvent -> {
            new ThemeCreatorDialog(this, (String)this.themeCombo.getSelectedItem()).setVisible(true);
        });
        jPanel.add(themeCreatorBtn, gridBagConstraints);
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        // DUZELTME (V39.3): tema olusturucu butonu gridy = n3+1'de (kendi
        // satiri). n sayaci artik n3+1'den devam eder — eski "n = n3"
        // sifirlamasi buton satiri ile Tasarim satirini AYNI hucreye
        // dusturup ust uste bindiriyordu (kullanicinin gordugu
        // "ayarlar dupeleniyo/bozuluyo" buradan geliyordu).
        n = n3 + 1;
        // --- V35 TASARIM MODU: Modern (animasyonlu efektler) / Classic (sade) ---
        gridBagConstraints.gridx = 0;
        int nDesign = ++n;
        gridBagConstraints.gridy = nDesign;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(L10n.isEnglish() ? "Design:" : "Tasar\u0131m:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        // V41: 6 tasarim modu — Classic / Modern / Minecraft / Herobrine /
        // Creeper / Nether. Gecis CANLI (restart yok).
        String[] designNames = new String[]{
            L10n.isEnglish() ? "Classic \u2014 clean & light" : "Klasik \u2014 sade & h\u0131zl\u0131",
            L10n.isEnglish() ? "Modern \u2014 animated background" : "Modern \u2014 animasyonlu arka plan",
            L10n.isEnglish() ? "Minecraft world \u2014 day" : "Minecraft d\u00fcnyas\u0131 \u2014 g\u00fcnd\u00fcz",
            L10n.isEnglish() ? "Herobrine \u2014 haunted" : "Herobrine \u2014 perili",
            L10n.isEnglish() ? "Creeper \u2014 charged" : "Creeper \u2014 y\u00fckl\u00fc",
            L10n.isEnglish() ? "Nether \u2014 inferno" : "Nether \u2014 cehennem"
        };
        JComboBox<String> designCombo = new JComboBox<String>(designNames);
        designCombo.setLightWeightPopupEnabled(true);
        String dm = this.settings.designMode == null ? "classic" : this.settings.designMode;
        designCombo.setSelectedIndex(switch (dm) {
            case "modern" -> 1;
            case "minecraft" -> 2;
            case "herobrine" -> 3;
            case "creeper" -> 4;
            case "nether" -> 5;
            default -> 0;
        });
        designCombo.addActionListener(actionEvent2 -> {
            this.applyDesignMode(switch (designCombo.getSelectedIndex()) {
                case 1 -> "modern";
                case 2 -> "minecraft";
                case 3 -> "herobrine";
                case 4 -> "creeper";
                case 5 -> "nether";
                default -> "classic";
            });
        });
        jPanel.add(designCombo, gridBagConstraints);
        // ==================== V36.1 KISISEL MARKA ====================
        // Logo / Ana sayfa arka plani / Hesap fotorafini dosyadan secme.
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(L10n.isEnglish() ? "Custom logo:" : "Özel logo:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        jPanel.add(this.brandingRow("logo"), gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(L10n.isEnglish() ? "Home background:" : "Ana sayfa arka planı:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        jPanel.add(this.brandingRow("bg"), gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(L10n.isEnglish() ? "Account photo:" : "Hesap fotoğrafı:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        jPanel.add(this.brandingRow("photo"), gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(L10n.isEnglish() ? "Easter egg mode:" : "Easter egg modu:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        jPanel.add(this.buildEggModeRow(), gridBagConstraints);
        // V40: TUM PENCERE arka plani (her sekmede gorunur).
        // V40.1 GRID FIX: bu satir daha once "Easter egg modu" etiketi ile
        // degeri ARASINA girmisti; boylece egg degeri appbg degeriyle AYNI
        // grid hucreye dusup ustu uste biniyordu (kullanicinin gordugu
        // "tuslar ic ice girmis"). Etiket+deger ciftleri artik sirali.
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(L10n.isEnglish() ? "App background:" : "Uygulama arka plan\u0131:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        jPanel.add(this.brandingRow("appbg"), gridBagConstraints);
        // V41 FALLING PHOTOS: arka planda yagan fotolar + yogunluk.
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(L10n.isEnglish() ? "Falling photos:" : "Ya\u011fan foto\u011fraflar:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        jPanel.add(this.buildFallingPhotosRow(), gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label("Dil / Language:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        JComboBox<String> jComboBox2 = new JComboBox<String>(new String[]{"T\u00fcrk\u00e7e", "English"});
        jComboBox2.setLightWeightPopupEnabled(true);
        jComboBox2.setSelectedItem(L10n.isEnglish() ? "English" : "T\u00fcrk\u00e7e");
        jComboBox2.addActionListener(actionEvent -> {
            String string = (String)jComboBox2.getSelectedItem();
            String string2 = "English".equals(string) ? "en" : "tr";
            L10n.setLanguage(string2);
            MainWindow.saveLangPref(string2);
            int confirmResult = JOptionPane.showConfirmDialog(this, L10n.get("restart_required"), L10n.get("language"), 0, 1);
            if (confirmResult == 0) {
                MainWindow.relaunch(this);
            }
        });
        jPanel.add(jComboBox2, gridBagConstraints);
        // V40.1 GRID FIX: n++ (post) ile Dil satiriyla AYNI gridy'ye
        // dustu; iki etiket+deger cifti tek satirda ust uste biniyordu.
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(settingsEn ? "Graphics Card:" : "Ekran Karti:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        // ONEMLI: kombonun gorsel etiketleri dile gore cevrilir ama secim
        // degerleri ("Otomatik"/"Dahili GPU"/"Dis GPU") ArgumentBuilder'in
        // bekledigi sekilde korunur - aksi halde GPU secimi bozulurdu.
        this.gpuCombo = new JComboBox<String>(settingsEn
            ? new String[]{"Otomatik", "Dahili GPU", "Dis GPU"}
            : new String[]{"Otomatik", "Dahili GPU", "Dis GPU"});
        this.gpuCombo.setToolTipText(settingsEn
            ? "Auto / Integrated GPU / Dedicated GPU"
            : "Otomatik / Dahili GPU / Dis GPU");
        this.gpuCombo.setLightWeightPopupEnabled(true);
        this.gpuCombo.setSelectedItem(this.settings.gpuSelection);
        jPanel.add(this.gpuCombo, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label(L10n.isEnglish() ? "CurseForge API key:" : "CurseForge API anahtar\u0131:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        this.curseforgeKeyField = new JTextField(this.curseforgeKey(), 25);
        jPanel.add((Component)this.curseforgeKeyField, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        JCheckBox jCheckBox = new JCheckBox(settingsEn ? "Automatically back up saves/ folder when the game closes" : "Oyun kapaninca saves/ klasorunu otomatik yedekle");
        jCheckBox.setOpaque(false);
        jCheckBox.setForeground(Theme.TEXT_SECONDARY);
        jCheckBox.setSelected(this.currentInstance.autoBackup);
        jCheckBox.addActionListener(actionEvent -> {
            this.currentInstance.autoBackup = jCheckBox.isSelected();
            this.currentInstance.save();
        });
        jPanel.add((Component)jCheckBox, gridBagConstraints);
        // --- Maks Performans tuşu ---
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(14, 0, 2, 0);
        boolean isEn = L10n.isEnglish();
        // --- Maks Performans paneli: DIKKAT CEKICI tasarim ---
        // Panel artik nabiz gibi atan (pulse) bir turuncu-parlak cerceveye,
        // isikli bir gradyan arka plana ve buyuk bir "ROKET" rozetine sahip.
        final JPanel maxPerfPanel = new JPanel(new BorderLayout(10, 0)) {
            private final Timer pulseTimer = new Timer(40, null);
            private float pulse = 0f;
            {
                long[] t0 = new long[]{System.currentTimeMillis()};
                pulseTimer.addActionListener(e -> {
                    pulse = (float)((Math.sin((double)(System.currentTimeMillis() - t0[0]) / 450.0) + 1.0) / 2.0);
                    repaint();
                });
                // Panel gorunur oldugunda animasyonu baslat, gorunmezken durdur.
                addHierarchyListener(hierarchyEvent -> {
                    boolean showing = isShowing();
                    if (showing && !pulseTimer.isRunning()) {
                        t0[0] = System.currentTimeMillis();
                        pulseTimer.start();
                    } else if (!showing && pulseTimer.isRunning()) {
                        pulseTimer.stop();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                // Isikli gradyan arka plan (turuncu/amber tonlari).
                // TEMA BUG FIXI: Maks Performans panelinin turuncu parlamasi
                // sabitti - artik Theme.ORANGE'den turetiliyor.
                Color mpGlow = Theme.ORANGE != null ? Theme.ORANGE : new Color(249, 115, 22);
                g2.setPaint(new GradientPaint(0f, 0f, new Color(mpGlow.getRed(), mpGlow.getGreen(), mpGlow.getBlue(), 36), 0f, h, new Color(mpGlow.getRed(), mpGlow.getGreen(), mpGlow.getBlue(), 14)));
                g2.fillRoundRect(0, 0, w, h, 14, 14);
                // Nabiz gibi atan dis parilti. (tema-duyari: ACCENT)
                int glowAlpha = (int)(60 + 60 * pulse);
                g2.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), glowAlpha));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 14, 14);
                g2.setColor(new Color(Theme.ACCENT_BRIGHT.getRed(), Theme.ACCENT_BRIGHT.getGreen(), Theme.ACCENT_BRIGHT.getBlue(), (int)(glowAlpha * 0.45)));
                g2.setStroke(new BasicStroke(5.0f));
                g2.drawRoundRect(-1, -1, w + 1, h + 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        maxPerfPanel.setOpaque(false);
        maxPerfPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JPanel maxPerfTextCol = new JPanel();
        maxPerfTextCol.setOpaque(false);
        maxPerfTextCol.setLayout(new BoxLayout(maxPerfTextCol, 1));
        JLabel maxPerfTitle = new JLabel("\u26a1\ud83d\ude80  " + (isEn ? "Maximum Performance Mode" : "MAKS PERFORMANS MODU"));
        maxPerfTitle.setForeground(Theme.ACCENT_BRIGHT);
        maxPerfTitle.setFont(maxPerfTitle.getFont().deriveFont(1, 14.5f));
        JLabel maxPerfDesc = new JLabel("<html>" + (isEn
            ? "Installs ~48 performance mods on Fabric / ~39 on Forge (Sodium, Lithium,<br>Entity Culling, FerriteCore, ImmediatelyFast and more) + tuned GC/JIT flags"
            : "Sisteminiz i\u00e7in ayarlanm\u0131\u015f GC/JIT bayraklar\u0131 + Fabric'te ~48, Forge'da ~39<br>performans modu (Sodium, Lithium, Entity Culling, FerriteCore vb.) kurar") + "</html>");
        maxPerfDesc.setForeground(Theme.TEXT_SECONDARY);
        maxPerfDesc.setFont(maxPerfDesc.getFont().deriveFont(10.5f));
        maxPerfTextCol.add(maxPerfTitle);
        maxPerfTextCol.add(Box.createVerticalStrut(3));
        maxPerfTextCol.add(maxPerfDesc);
        JToggleButton maxPerfToggle = new JToggleButton(this.settings.maxPerformanceMode ? (isEn ? "ON" : "A\u00c7IK") : (isEn ? "OFF" : "KAPALI")) {
            private float hover = 0.0f;
            private float press = 0.0f;
            private Timer gt;
            {
                this.setPreferredSize(new Dimension(96, 40));
                this.setForeground(Color.WHITE);
                this.setFocusPainted(false);
                this.setContentAreaFilled(false);
                this.setBorderPainted(false);
                this.setFont(new Font("SansSerif", 1, 13));
                this.setCursor(Cursor.getPredefinedCursor(12));
                this.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) { animHover(1.0f); }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) { animHover(0.0f); }
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) { press = 1.0f; }
                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent e) { press = 0.0f; }
                });
            }
            private void animHover(float target) {
                if (this.gt != null) this.gt.stop();
                float from = this.hover;
                long t0 = System.currentTimeMillis();
                this.gt = new Timer(16, null);
                this.gt.addActionListener(e -> {
                    float p = Math.min(1.0f, (float)(System.currentTimeMillis() - t0) / 120.0f);
                    this.hover = from + (target - from) * UiFx.easeOutCubic(p);
                    this.repaint();
                    if (p >= 1.0f) this.gt.stop();
                });
                this.gt.start();
            }
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D)g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                float glowAlpha = (float)(20 + 30 * this.hover + (this.isSelected() ? 40 : 0));
                if (this.isSelected()) {
                    g2.setColor(new java.awt.Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), (int)glowAlpha));
                    g2.setStroke(new java.awt.BasicStroke(4.0f));
                    g2.drawRoundRect(-3, -3, w + 6, h + 6, 16, 16);
                    g2.setColor(new java.awt.Color(Theme.ACCENT_BRIGHT.getRed(), Theme.ACCENT_BRIGHT.getGreen(), Theme.ACCENT_BRIGHT.getBlue(), (int)(glowAlpha * 0.5)));
                    g2.setStroke(new java.awt.BasicStroke(7.0f));
                    g2.drawRoundRect(-4, -4, w + 8, h + 8, 18, 18);
                }
                float sc = 1.0f - press * 0.03f + hover * 0.02f;
                g2.translate(w / 2.0, h / 2.0);
                g2.scale(sc, sc);
                g2.translate(-w / 2.0, -h / 2.0);
                int pad = 4;
                g2.setColor(isSelected() ? Theme.ACCENT : Theme.BG_ELEVATED);
                g2.fillRoundRect(pad, pad, w - pad * 2, h - pad * 2, 12, 12);
                if (isSelected()) {
                    g2.setPaint(new java.awt.GradientPaint(0.0f, 0.0f, Theme.ACCENT_BRIGHT, 0.0f, (float)h, Theme.ACCENT_DARK));
                    g2.fillRoundRect(pad, pad, w - pad * 2, h - pad * 2, 12, 12);
                }
                if (isSelected()) {
                    g2.setPaint(new java.awt.GradientPaint(0.0f, 0.0f, java.awt.Color.WHITE, 0.0f, (float)h * 0.3f, new java.awt.Color(255, 255, 255, 0)));
                    g2.fillRoundRect(pad, pad, w - pad * 2, (int)((float)h * 0.3f), 12, 12);
                    g2.setColor(new java.awt.Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 80));
                    g2.setStroke(new java.awt.BasicStroke(1.5f));
                    g2.drawRoundRect(pad, pad, w - pad * 2, h - pad * 2, 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        maxPerfToggle.setSelected(this.settings.maxPerformanceMode);
        maxPerfToggle.addActionListener(actionEvent -> {
            boolean nowOn = maxPerfToggle.isSelected();
            if (nowOn) {
                // V29.6: cakisan modlar ONCE temizlenir - kullanici uyairililir,
                // onaylarsa silinir, sonra performans seti kurulur. Iptal
                // ederse toggle eski haline doner, hicbir sey kurulmaz.
                if (!this.handleMaxPerfConflictRemoval()) {
                    maxPerfToggle.setSelected(false);
                    maxPerfToggle.setText(isEn ? "OFF" : "KAPALI");
                    this.settings.maxPerformanceMode = false;
                    this.settings.save();
                    return;
                }
            }
            this.settings.maxPerformanceMode = nowOn;
            this.settings.save();
            maxPerfToggle.setText(nowOn ? (isEn ? "ON" : "A\u00c7IK") : (isEn ? "OFF" : "KAPALI"));
            if (nowOn) {
                this.log(isEn ? "Maximum Performance enabled - installing performance mods for the current instance..." : "Maks Performans a\u00e7\u0131ld\u0131 - mevcut instance i\u00e7in performans modlar\u0131 kuruluyor...");
                this.installMaxPerformanceMods();
            } else {
                this.log(isEn ? "Maximum Performance disabled." : "Maks Performans kapat\u0131ld\u0131.");
            }
        });
        maxPerfPanel.add((Component)maxPerfTextCol, "Center");
        maxPerfPanel.add((Component)maxPerfToggle, "East");
        jPanel.add((Component)maxPerfPanel, gridBagConstraints);
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        // --- Mod bilgisi oto-acma ayari (V34: tum sekmelerde) ---
        // Acik: Mods/Shaders/Resource Packs/Modpacks sekmelerinde bir oge
        // secildigi an detay paneli otomatik acilir.
        // Kapali (varsayilan): sadece ilgili buton veya cift tik ile acilir.
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(10, 0, 2, 0);
        JCheckBox modInfoAutoBox = new JCheckBox(
            settingsEn ? "Auto-open info panel when an item is selected (Mods, Shaders, Resource Packs, Modpacks)" : "Bir öfe secilince bilgi panelini otomatik a\u00e7 (Modlar, Shaderlar, Doku Paketleri, Modpack'ler)");
        modInfoAutoBox.setSelected(this.settings.modInfoAutoOpen);
        modInfoAutoBox.setOpaque(false);
        modInfoAutoBox.setForeground(Theme.TEXT_SECONDARY);
        modInfoAutoBox.setFont(modInfoAutoBox.getFont().deriveFont(11.5f));
        modInfoAutoBox.addActionListener(actionEvent2 -> {
            this.settings.modInfoAutoOpen = modInfoAutoBox.isSelected();
            this.settings.save();
            this.log((settingsEn ? "Info panel auto-open: " : "Bilgi paneli oto a\u00e7ma: ") + (this.settings.modInfoAutoOpen ? "ON" : "OFF"));
        });
        jPanel.add((Component)modInfoAutoBox, gridBagConstraints);

        gridBagConstraints.gridy = ++n;
        gridBagConstraints.insets = new Insets(12, 0, 2, 0);
        jPanel.add((Component)UiFx.sectionLabel(settingsEn ? "Auto-Install on Launch" : "Baslarken Otomatik Kurulacaklar"), gridBagConstraints);
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        JsonObject jsonObject = MainWindow.loadModPrefs();
        // V29.5: ilk kurulumda TUM oto-kurulum anahtarlari KAPALI gelir.
        // Kullanici diledigini isaretler; secimi mod_prefs.json'a kaydedilir.
        Object[][] objectArrayArray = new Object[][]{{"sodium", "Sodium (OpenGL performans)", "Sodium (OpenGL performance)", false}, {"iris", "Iris Shaders (shader destefi)", "Iris Shaders (shader support)", false}, {"vulkanmod", "VulkanMod (Vulkan, 1.17+)", "VulkanMod (Vulkan, 1.17+)", false}, {"embeddium", "Embeddium (Forge performans)", "Embeddium (Forge performance)", false}, {"oculus", "Oculus (Forge shader)", "Oculus (Forge shader)", false}, {"bsl", "BSL Shaders", "BSL Shaders", false}, {"complementary", "Complementary Reimagined", "Complementary Reimagined", false}, {"jei", "Just Enough Items", "Just Enough Items", false}, {"journeymap", "JourneyMap (mini harita)", "JourneyMap (minimap)", false}, {"replaymod", "ReplayMod (kay\u0131t/tekrar)", "ReplayMod (replay)", false}, {"amusemod", "AmuseMenu (geli\u015fmi\u015f men\u00fc)", "AmuseMenu", false}, {"tweakeroo", "Tweakeroo (client tweaks)", "Tweakeroo", false}};
        boolean bl = L10n.isEnglish();
        LinkedHashMap<String, JCheckBox> linkedHashMap = new LinkedHashMap<String, JCheckBox>();
        JPanel jPanel3 = new JPanel(new GridLayout(0, 2, 4, 4));
        jPanel3.setOpaque(false);
        for (Object[] object32 : objectArrayArray) {
            object2 = (String)object32[0];
            final String prefKey = (String)object2;
            object = bl ? (String)object32[2] : (String)object32[1];
            boolean jButton3 = (Boolean)object32[3];
            boolean jButton4 = jsonObject.has((String)object2) ? jsonObject.get((String)object2).getAsBoolean() : jButton3;
            abstractButton = new JCheckBox((String)object, jButton4);
            final JCheckBox thisCheckBox = (JCheckBox)abstractButton;
            abstractButton.setOpaque(false);
            abstractButton.setForeground(Theme.TEXT_SECONDARY);
            abstractButton.setFont(abstractButton.getFont().deriveFont(11.0f));
            abstractButton.addActionListener(arg_0 -> MainWindow.handleModPrefCheckbox(jsonObject, prefKey, thisCheckBox, arg_0));
            linkedHashMap.put(prefKey, thisCheckBox);
            jPanel3.add(abstractButton);
        }
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        jPanel.add((Component)jPanel3, gridBagConstraints);
        JCheckBox jCheckBox2 = (JCheckBox)linkedHashMap.get("vulkanmod");
        JCheckBox jCheckBox3 = (JCheckBox)linkedHashMap.get("sodium");
        JCheckBox jCheckBox4 = (JCheckBox)linkedHashMap.get("iris");
        JCheckBox jCheckBox5 = (JCheckBox)linkedHashMap.get("bsl");
        object2 = (JCheckBox)linkedHashMap.get("complementary");
        final JCheckBox complementaryBox = (JCheckBox)object2;
        if (jCheckBox2 != null) {
            jCheckBox2.addActionListener(arg_0 -> MainWindow.onModProfileToggle(jCheckBox2, jCheckBox3, jCheckBox4, jCheckBox5, complementaryBox, linkedHashMap, jsonObject, arg_0));
            if (jCheckBox2.isSelected()) {
                if (jCheckBox3 != null) {
                    jCheckBox3.setEnabled(false);
                    jCheckBox3.setSelected(false);
                }
                if (jCheckBox4 != null) {
                    jCheckBox4.setEnabled(false);
                    jCheckBox4.setSelected(false);
                }
                if (jCheckBox5 != null) {
                    jCheckBox5.setEnabled(false);
                    jCheckBox5.setSelected(false);
                }
                if (object2 != null) {
                    ((AbstractButton)object2).setEnabled(false);
                    ((AbstractButton)object2).setSelected(false);
                }
            }
        }

        // --- Kullanicinin kendi ekleyecegi otomatik kurulacak modlar ---
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(14, 0, 2, 0);
        jPanel.add((Component)UiFx.sectionLabel(bl ? "Your Custom Auto-Install Items" : "Kendi Otomatik Kurulacaklar\u0131n"), gridBagConstraints);
        // DUZELTME: aciklama etiketi ayni hucreye ekleniyordu (bolum basligiyla
        // ust uste binuyordu). Kendi satirina tasindi.
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.insets = new Insets(0, 0, 2, 0);
        jPanel.add((Component)UiFx.label(bl
            ? "(These are installed automatically every time the game launches)"
            : "(Bunlar oyun her baslattiginda otomatik kurulur)"), gridBagConstraints);
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);

        JPanel customModsListPanel = new JPanel();
        customModsListPanel.setLayout(new BoxLayout(customModsListPanel, 1));
        customModsListPanel.setOpaque(false);

        Runnable[] refreshCustomListRef = new Runnable[1];
        refreshCustomListRef[0] = () -> {
            customModsListPanel.removeAll();
            List<CustomAutoMod> customMods = MainWindow.loadCustomAutoMods();
            if (customMods.isEmpty()) {
                JLabel emptyLabel = new JLabel(bl ? "No custom mods added yet." : "Hen\u00fcz \u00f6zel mod eklenmedi.");
                emptyLabel.setForeground(Theme.TEXT_MUTED != null ? Theme.TEXT_MUTED : Theme.TEXT_SECONDARY);
                emptyLabel.setFont(emptyLabel.getFont().deriveFont(11.0f));
                emptyLabel.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
                customModsListPanel.add(emptyLabel);
            } else {
                for (CustomAutoMod m : customMods) {
                    JPanel row = new JPanel(new BorderLayout(6, 0));
                    row.setOpaque(false);
                    String typeLabel = switch (m.type == null ? "mod" : m.type.toLowerCase()) {
                        case "shader" -> bl ? " [Shader]" : " [Shader]";
                        case "resourcepack" -> bl ? " [Resource Pack]" : " [Doku Paketi]";
                        default -> "";
                    };
                    JCheckBox rowCheck = new JCheckBox(m.title + typeLabel, m.enabled);
                    rowCheck.setOpaque(false);
                    rowCheck.setForeground(Theme.TEXT_SECONDARY);
                    rowCheck.setFont(rowCheck.getFont().deriveFont(11.0f));
                    rowCheck.addActionListener(ae -> {
                        List<CustomAutoMod> current = MainWindow.loadCustomAutoMods();
                        for (CustomAutoMod cm : current) {
                            if (cm.slug.equalsIgnoreCase(m.slug) && cm.type.equalsIgnoreCase(m.type)) {
                                cm.enabled = rowCheck.isSelected();
                            }
                        }
                        MainWindow.saveCustomAutoMods(current);
                    });
                    JButton removeBtn = UiFx.ghostButton("\u2715");
                    removeBtn.setForeground(Theme.RED);
                    removeBtn.setToolTipText(bl ? "Remove" : "Kald\u0131r");
                    removeBtn.addActionListener(ae -> {
                        List<CustomAutoMod> current = MainWindow.loadCustomAutoMods();
                        current.removeIf(cm -> cm.slug.equalsIgnoreCase(m.slug) && cm.type.equalsIgnoreCase(m.type));
                        MainWindow.saveCustomAutoMods(current);
                        refreshCustomListRef[0].run();
                        customModsListPanel.revalidate();
                        customModsListPanel.repaint();
                    });
                    row.add((Component)rowCheck, "Center");
                    row.add((Component)removeBtn, "East");
                    customModsListPanel.add(row);
                }
            }
        };
        refreshCustomListRef[0].run();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        jPanel.add((Component)customModsListPanel, gridBagConstraints);

        JPanel addCustomModRow = new JPanel(new FlowLayout(0, 6, 0));
        addCustomModRow.setOpaque(false);
        JButton addCustomModBtn = UiFx.ghostButton("+  " + (bl ? "Add Item (Mod / Shader / Resource Pack)" : "Ekle (Mod / Shader / Doku Paketi)"));
        addCustomModBtn.addActionListener(ae -> this.addCustomAutoModDialog(() -> {
            refreshCustomListRef[0].run();
            customModsListPanel.revalidate();
            customModsListPanel.repaint();
        }));
        addCustomModRow.add(addCustomModBtn);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        jPanel.add((Component)addCustomModRow, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        object = new JPanel(new FlowLayout(0, 14, 8));
        ((JComponent)object).setOpaque(false);
        JButton jButton3 = UiFx.accentButton(settingsEn ? "Save Settings" : "Ayarlar\u0131 Kaydet");
        JButton jButton4 = UiFx.ghostButton(settingsEn ? "Desktop Shortcut" : "Desktop Kisayolu");
        jButton4.addActionListener(actionEvent -> {
            try {
                String string = ProcessHandle.current().info().command().orElse("");
                DesktopShortcut.forceCreate(string);
                this.log(settingsEn ? "Desktop shortcut created." : "Desktop kisayolu olusturuldu.");
            }
            catch (Exception exception) {
                this.log((settingsEn ? "Could not create shortcut: " : "Kisayol olusturulamadi: ") + exception.getMessage());
            }
        });
        abstractButton = UiFx.ghostButton(settingsEn ? "Log Out" : "Cikis Yap");
        abstractButton.addActionListener(actionEvent -> {
            if (this.session != null) {
                AccountManager.remove(this.session.username);
            }
            this.session = AccountManager.getActive();
            this.onSessionChanged();
            this.log(settingsEn ? "Logged out." : "Cikis yapildi.");
        });
        JButton jButton5 = UiFx.ghostButton(settingsEn ? "Error Log" : "Hata Gunlugu");
        jButton5.setToolTipText(settingsEn ? "View launcher_errors.log" : "launcher_errors.log dosyasini goruntule");
        jButton5.addActionListener(actionEvent -> this.showCrashLog());
        jButton3.addActionListener(actionEvent -> {
            this.saveCurrentInstance();
            // TEMA BUG FIXI: temalar aninda uygulandigi icin burada sadece
            // kayit kalir; kayit her zaman enum adiyla yapilir (fromName
            // eski gorunen adlari da cozer - geriye uyumlu).
            String themeSel = (String)this.themeCombo.getSelectedItem();
            this.settings.theme = (themeSel != null && themeSel.contains("\u2605")) ? "Custom" : Theme.Preset.fromName(themeSel).name();
            this.settings.curseforgeApiKey = this.curseforgeKeyField.getText().trim();
            this.settings.gpuSelection = (String)this.gpuCombo.getSelectedItem();
            this.settings.save();
            this.applyThemeAndColor();
            this.log(settingsEn ? "Settings saved." : "Ayarlar kaydedildi.");
            SwingUtilities.updateComponentTreeUI(this);
        });
        ((Container)object).add(jButton3);
        ((Container)object).add(jButton4);
        ((Container)object).add(abstractButton);
        ((Container)object).add(jButton5);
        jPanel.add((Component)object, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = n;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(10, 0, 0, 0);
        JPanel jPanel4 = new JPanel(new FlowLayout(0, 14, 8));
        jPanel4.setOpaque(false);
        JButton jButton6 = UiFx.ghostButton(settingsEn ? "Export Instance (ZIP)" : "Instance Disa Aktar (ZIP)");
        JButton jButton7 = UiFx.ghostButton(settingsEn ? "Import Instance (ZIP)" : "Instance Ice Aktar (ZIP)");
        jButton6.addActionListener(actionEvent -> this.exportCurrentInstance());
        jButton7.addActionListener(actionEvent -> this.importInstance());
        jPanel4.add(jButton6);
        jPanel4.add(jButton7);
        // DUZELTME: jPanel4 "n" satirina ekleniyor ama sonraki bolum basligi
        // "n++" ile AYNI hucreye dusuyordu (ust uste biniyor). Once jPanel4
        // kendi satirina alindi, sonra bolum basligi yeni satira gecti.
        gridBagConstraints.gridy = ++n;
        jPanel.add((Component)jPanel4, gridBagConstraints);
        // --- Tehlikeli Bolge: Launcher'i tamamen kaldirma ---
        boolean isEnglishUi = L10n.isEnglish();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(18, 0, 0, 0);
        jPanel.add((Component)UiFx.sectionLabel(isEnglishUi ? "Danger Zone" : "Tehlikeli B\u00f6lge"), gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++n;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(6, 0, 0, 0);
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        // Yeniden tasarlanan tehlikeli bolge karti: kirmizi kenarlikli, uyari
        // ikonlu, aciklamali ve butonu saga hizli bir kart.
        JPanel dangerZonePanel = new JPanel(new java.awt.BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D)g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(Theme.RED_DARK.getRed(), Theme.RED_DARK.getGreen(), Theme.RED_DARK.getBlue(), 200));
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
                g2.dispose();
            }
        };
        dangerZonePanel.setOpaque(true);
        dangerZonePanel.setBackground(new Color(Theme.RED_DARK.getRed() / 3, Theme.RED_DARK.getGreen() / 3, Theme.RED_DARK.getBlue() / 3));
        dangerZonePanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel dangerIcon = new JLabel("\u26a0");
        dangerIcon.setFont(dangerIcon.getFont().deriveFont(1, 26.0f));
        dangerIcon.setForeground(Theme.RED);
        JPanel dangerTextCol = new JPanel(new java.awt.GridLayout(2, 1, 0, 3));
        dangerTextCol.setOpaque(false);
        JLabel dangerTitle = new JLabel(isEnglishUi ? "Delete Launcher (all data)" : "Launcher'\u0131 Sil (t\u00fcm verilerle)");
        dangerTitle.setFont(dangerTitle.getFont().deriveFont(1, 13.0f));
        dangerTitle.setForeground(Theme.RED);
        JLabel dangerDesc = new JLabel(isEnglishUi
            ? "Removes all instances, mods, accounts and settings, then closes the launcher. This cannot be undone."
            : "T\u00fcm instance'lar\u0131, modlar\u0131, hesaplar\u0131 ve ayarlar\u0131 kal\u0131c\u0131 olarak siler ve launcher'\u0131 kapat\u0131r. Bu i\u015flem geri al\u0131namaz.");
        dangerDesc.setFont(dangerDesc.getFont().deriveFont(0, 11.0f));
        dangerDesc.setForeground(Theme.TEXT_MUTED);
        dangerTextCol.add(dangerTitle);
        dangerTextCol.add(dangerDesc);
        JButton deleteLauncherBtn = new JButton(isEnglishUi ? "\ud83d\uddd1  Delete Launcher" : "\ud83d\uddd1  Launcher'\u0131 Sil");
        deleteLauncherBtn.setBackground(new Color(Theme.RED_DARK.getRed() / 2, Theme.RED_DARK.getGreen() / 2, Theme.RED_DARK.getBlue() / 2));
        deleteLauncherBtn.setForeground(new Color(255, 200, 200));
        deleteLauncherBtn.setFocusPainted(false);
        deleteLauncherBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.RED_DARK, 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        deleteLauncherBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        deleteLauncherBtn.setToolTipText(isEnglishUi
            ? "Removes all instances, mods, accounts and settings, then closes the launcher."
            : "T\u00fcm instance'lar\u0131, modlar\u0131, hesaplar\u0131 ve ayarlar\u0131 kal\u0131c\u0131 olarak siler ve launcher'\u0131 kapat\u0131r.");
        deleteLauncherBtn.addActionListener(actionEvent -> this.confirmAndDeleteLauncherData());
        dangerZonePanel.add((Component)dangerIcon, "West");
        dangerZonePanel.add((Component)dangerTextCol, "Center");
        dangerZonePanel.add((Component)deleteLauncherBtn, "East");
        jPanel.add((Component)dangerZonePanel, gridBagConstraints);
        gridBagConstraints.fill = 0;
        this.settingsRoot.add(jPanel);
        // V34: Ayarlar paneli kucuk pencerede butonlari kirpiyordu -
        // tum panel artik kaydirilabilir scroll icinde.
        this.settingsRoot.setAlignmentX(0.0f);
        javax.swing.JScrollPane settingsScroll = UiFx.cleanScroll(this.settingsRoot);
        settingsScroll.getVerticalScrollBar().setUnitIncrement(18);
        settingsScroll.getHorizontalScrollBar().setUnitIncrement(18);
        // V34.7: canli ayar aramasi - jPanel'in tum cocuklarini tarar;
        // eslesen satirlar gosterilir, eslesmeyenler gizlenir. Arama, hem
        // JLabel metnine hem JCheckBox/JButton/JTextField adlarina bakar.
        settingsSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void applyFilter() {
                String q = settingsSearchField.getText().trim().toLowerCase();
                java.awt.Component[] rows = jPanel.getComponents();
                if (q.length() < 2) {
                    for (java.awt.Component c : rows) {
                        c.setVisible(true);
                    }
                } else {
                    for (java.awt.Component c : rows) {
                        c.setVisible(MainWindow.rowMatchesSearch(c, q));
                    }
                }
                jPanel.revalidate();
                jPanel.repaint();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                this.applyFilter();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                this.applyFilter();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                this.applyFilter();
            }
        });
        javax.swing.InputMap ssIM = settingsSearchField.getInputMap(javax.swing.JComponent.WHEN_FOCUSED);
        ssIM.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "clearSettingsSearch");
        settingsSearchField.getActionMap().put("clearSettingsSearch", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                settingsSearchField.setText("");
            }
        });
        return settingsScroll;
    }

    /** V34.7: bir ayar satiri arama terimiyle eslesiyor mu? Satirin
     *  kendi metni yeterli degilse cocuklarina da bakar (nested paneller). */
    private static boolean rowMatchesSearch(java.awt.Component row, String query) {
        if (row instanceof JLabel jl && jl.getText() != null && jl.getText().toLowerCase().contains(query)) {
            return true;
        }
        if (row instanceof javax.swing.AbstractButton ab) {
            String t = ab.getText();
            String tip = ab.getToolTipText();
            if ((t != null && t.toLowerCase().contains(query)) || (tip != null && tip.toLowerCase().contains(query))) {
                return true;
            }
        }
        if (row instanceof JTextField tf) {
            String tip = tf.getToolTipText();
            if (tip != null && tip.toLowerCase().contains(query)) {
                return true;
            }
        }
        if (row instanceof java.awt.Container cont) {
            for (int i = 0; i < cont.getComponentCount(); i++) {
                if (MainWindow.rowMatchesSearch(cont.getComponent(i), query)) {
                    return true;
                }
            }
        }
        return false;
    }

    private JPanel buildAccountsTab() {
        JPanel jPanel = new JPanel(new BorderLayout(0, 12));
        jPanel.setOpaque(false);
        jPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel jLabel = new JLabel(L10n.get("tab.accounts"));
        jLabel.setFont(new Font("SansSerif", 1, 18));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        JLabel jLabel2 = new JLabel(L10n.isEnglish() ? "Add multiple accounts and switch between them." : "Birden fazla hesap ekleyin ve aralar\u0131nda ge\u00e7i\u015f yap\u0131n.");
        jLabel2.setFont(jLabel2.getFont().deriveFont(12.0f));
        jLabel2.setForeground(Theme.TEXT_MUTED);
        JPanel jPanel2 = new JPanel(new BorderLayout());
        jPanel2.setOpaque(false);
        jPanel2.add((Component)jLabel, "North");
        jPanel2.add((Component)jLabel2, "South");
        jPanel.add((Component)jPanel2, "North");
        this.accountsModel = new DefaultListModel();
        this.accountsList = new JList<String>(this.accountsModel);
        this.accountsList.setFont(this.accountsList.getFont().deriveFont(0, 13.0f));
        this.accountsList.setFixedCellHeight(34);
        this.accountsList.setSelectionMode(0);
        this.accountsList.setBackground(Theme.BG_BASE);
        jPanel.add((Component)UiFx.cleanScroll(this.accountsList), "Center");
        JPanel jPanel3 = new JPanel(new GridLayout(1, 4, 8, 0));
        jPanel3.setOpaque(false);
        JButton jButton = UiFx.ghostButton(L10n.isEnglish() ? "Add Offline" : "\u00c7evrimd\u0131\u015f\u0131 Ekle");
        JButton jButton2 = UiFx.accentButton(com.lubv.launcher.core.L10n.isEnglish() ? "Add Microsoft" : "Microsoft Ekle");
        JButton jButton3 = UiFx.ghostButton(com.lubv.launcher.core.L10n.isEnglish() ? "Set Active" : "Aktif Yap");
        JButton jButton4 = UiFx.dangerButton(com.lubv.launcher.core.L10n.isEnglish() ? "Delete" : "Sil");
        jButton.addActionListener(actionEvent -> this.doOfflineLogin());
        jButton2.addActionListener(actionEvent -> this.doMsLogin());
        jButton3.addActionListener(actionEvent -> this.setActiveAccount());
        jButton4.addActionListener(actionEvent -> this.removeAccount());
        jPanel3.add(jButton);
        jPanel3.add(jButton2);
        jPanel3.add(jButton3);
        jPanel3.add(jButton4);
        jPanel.add((Component)jPanel3, "South");
        this.refreshAccountsList();
        return jPanel;
    }

    private void refreshAccountsList() {
        if (this.accountsModel == null) {
            return;
        }
        this.accountsCache.clear();
        this.accountsCache.addAll(AccountManager.list());
        String string = AccountManager.activeUsername();
        this.accountsModel.clear();
        for (MinecraftSession minecraftSession : this.accountsCache) {
            String string2 = "legacy".equals(minecraftSession.userType) ? (L10n.isEnglish() ? "Offline" : "\u00c7evrimd\u0131\u015f\u0131") : "Microsoft";
            String string3 = minecraftSession.username != null && minecraftSession.username.equals(string) ? " " : "   ";
            this.accountsModel.addElement(string3 + minecraftSession.username + "   (" + string2 + ")");
        }
    }

    private void setActiveAccount() {
        int n = this.accountsList.getSelectedIndex();
        if (n < 0 || n >= this.accountsCache.size()) {
            return;
        }
        MinecraftSession minecraftSession = this.accountsCache.get(n);
        AccountManager.setActive(minecraftSession.username);
        this.session = AccountManager.getActive();
        this.onSessionChanged();
        this.log("Aktif hesap: " + minecraftSession.username);
    }

    private void removeAccount() {
        int n = this.accountsList.getSelectedIndex();
        if (n < 0 || n >= this.accountsCache.size()) {
            return;
        }
        MinecraftSession minecraftSession = this.accountsCache.get(n);
        int n2 = JOptionPane.showConfirmDialog(this, "'" + minecraftSession.username + "' silinsin mi?", "Hesab\u0131 Sil", 0);
        if (n2 != 0) {
            return;
        }
        AccountManager.remove(minecraftSession.username);
        this.session = AccountManager.getActive();
        this.onSessionChanged();
        this.log("Hesap silindi: " + minecraftSession.username);
    }

    private void onSessionChanged() {
        this.updateAccountLabel();
        this.refreshAccountsList();
    }

    private void doOfflineLogin() {
        OfflineLoginDialog offlineLoginDialog = new OfflineLoginDialog(this);
        MinecraftSession minecraftSession = offlineLoginDialog.showAndLogin();
        if (minecraftSession != null) {
            this.session = minecraftSession;
            AccountManager.add(minecraftSession);
            this.onSessionChanged();
            this.log(" \u00c7evrimd\u0131\u015f\u0131: " + minecraftSession.username);
        }
    }

    private void doMsLogin() {
        LoginDialog loginDialog = new LoginDialog(this);
        MinecraftSession minecraftSession = loginDialog.showAndLogin();
        if (minecraftSession != null) {
            this.session = minecraftSession;
            AccountManager.add(minecraftSession);
            this.onSessionChanged();
            this.log("Microsoft: " + minecraftSession.username);
        }
    }

    private void updateAccountLabel() {
        if (this.accountLabel == null) {
            return;
        }
        if (this.session != null) {
            String string = "legacy".equals(this.session.userType) ? "\u00c7evrimd\u0131\u015f\u0131" : "Microsoft";
            this.accountLabel.setText(this.session.username + "  \u00b7  " + string);
            if (this.avatarLabel != null) {
                this.avatarLabel.setIcon(MainWindow.makeAvatar(this.session.username));
            }
        } else {
            this.accountLabel.setText(L10n.get("account.not_logged_in"));
            if (this.avatarLabel != null) {
                this.avatarLabel.setIcon(MainWindow.makeAvatar(""));
            }
        }
    }

    private void loadVersions() {
        try {
            this.log("Mojang s\u00fcr\u00fcm listesi al\u0131n\u0131yor...");
            this.manifest = VersionManifest.fetch();
            SwingUtilities.invokeLater(() -> {
                this.populateVersionCombo();
                if (this.editVersionCombo != null && this.editingInstance != null) {
                    this.populateVersionComboInto(this.editVersionCombo, this.editingInstance.lastVersion);
                }
                this.log(" " + this.manifest.versions.size() + "s\u00fcr\u00fcm y\u00fcklendi. G\u00fcncel: " + this.manifest.latestRelease);
            });
        }
        catch (Exception exception) {
            this.log("HATA: S\u00fcr\u00fcmler al\u0131namad\u0131 \u2014 " + exception.getMessage());
        }
    }

    private void populateVersionCombo() {
        String string = this.currentInstance != null && !this.currentInstance.lastVersion.isEmpty() ? this.currentInstance.lastVersion : null;
        boolean bl = this.comboEventsEnabled;
        this.comboEventsEnabled = false;
        try {
            this.populateVersionComboInto(this.versionCombo, string);
        }
        finally {
            this.comboEventsEnabled = bl;
        }
    }

    private void populateVersionComboInto(JComboBox<String> jComboBox, String string) {
        if (this.manifest == null) {
            return;
        }
        jComboBox.removeAllItems();
        List<VersionManifest.Entry> list = this.showSnapshotsBox.isSelected() ? this.manifest.versions : this.manifest.releasesOnly();
        for (VersionManifest.Entry entry : list) {
            jComboBox.addItem(entry.id);
        }
        if (string != null && !string.isEmpty()) {
            jComboBox.setSelectedItem(string);
        } else {
            jComboBox.setSelectedItem(this.manifest.latestRelease);
        }
    }

    private String currentLoader() {
        if (this.loaderCombo != null && this.loaderCombo.getSelectedItem() != null) {
            return (String)this.loaderCombo.getSelectedItem();
        }
        return this.currentInstance != null ? this.currentInstance.loader : "Vanilla";
    }

    private String currentMcVersion() {
        if (this.versionCombo != null && this.versionCombo.getSelectedItem() != null && !((String)this.versionCombo.getSelectedItem()).isBlank()) {
            return (String)this.versionCombo.getSelectedItem();
        }
        if (this.currentInstance != null && !this.currentInstance.lastVersion.isBlank()) {
            return this.currentInstance.lastVersion;
        }
        if (this.manifest != null) {
            return this.manifest.latestRelease;
        }
        return "";
    }

    private String curseforgeKey() {
        String string = this.settings.curseforgeApiKey;
        return string == null || string.isBlank() ? "$2a$10$9d8G2Q5rS.xB6MdD3X0NlefGcjZlt8eLfL6osBAQcsct3HfLglskq" : string;
    }

    private void launchSecondClient() {
        if (this.session == null) {
            JOptionPane.showMessageDialog(this, "Once giris yapin!", "Giris Gerekli", 2);
            return;
        }
        if (this.currentInstance == null) {
            JOptionPane.showMessageDialog(this, "Once bir instance secin!", "Hata", 2);
            return;
        }
        String string = this.currentInstance.lastVersion;
        if (string == null || string.isBlank()) {
            JOptionPane.showMessageDialog(this, "Bu instance icin henuz bir surum secilmemis.\nOnce ana sayfadan bir surum sec ve baslat.", "Surum Yok", 2);
            return;
        }
        String string2 = this.session.username.length() <= 13 ? this.session.username + "_2" : this.session.username.substring(0, 13) + "_2";
        String string3 = (String)JOptionPane.showInputDialog(this, "Ikinci client icin isim:", "Ikinci Client", 3, null, null, string2);
        if (string3 == null) {
            return;
        }
        if ((string3 = string3.trim()).isEmpty() || !string3.matches("[a-zA-Z0-9_]+") || string3.length() > 16) {
            JOptionPane.showMessageDialog(this, "Gecersiz isim! Sadece harf, rakam ve alt cizgi, en fazla 16 karakter.", "Hata", 0);
            return;
        }
        NameChangeServer nameChangeServer = GameLauncher.getActiveNameServer();
        if (nameChangeServer != null) {
            String string4 = string3;
            nameChangeServer.launchSecondClient(string4);
            this.log("Ikinci client baslatiliyor: " + string4 + " (NCS)");
            return;
        }
        String string5 = string3;
        String string6 = string;
        String string7 = this.currentInstance.loader != null ? this.currentInstance.loader : "Vanilla";
        File file = this.currentInstance.dir();
        int n = this.currentInstance.ramGB;
        String string8 = this.currentInstance.jvmArgs != null ? this.currentInstance.jvmArgs : "";
        String string9 = this.currentInstance.javaPath != null ? this.currentInstance.javaPath : "";
        String string10 = this.currentInstance.preLaunchCmd != null ? this.currentInstance.preLaunchCmd : "";
        String string11 = this.settings != null ? this.settings.gpuSelection : "Otomatik";
        String string12 = string11;
        if (this._secondClientBtnRef != null) {
            this._secondClientBtnRef.setEnabled(false);
        }
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> this.log("Ikinci client baslatiliyor: " + string5 + " / " + string6));
                VersionManifest versionManifest = this.cachedManifest;
                if (versionManifest == null) {
                    this.cachedManifest = versionManifest = VersionManifest.fetch();
                }
                MinecraftSession minecraftSession = MinecraftSession.offline(string5);
                VersionManifest versionManifest2 = versionManifest;
                GameLauncher.launch(versionManifest2, string6, string7, minecraftSession, n, string8, string9, string11, string10, file, new GameLauncher.ProgressCallback(){

                    @Override
                    public void onLog(String string) {
                        SwingUtilities.invokeLater(() -> MainWindow.this.log("[2.Client] " + string));
                    }

                    @Override
                    public void onProgress(int n, String string) {
                    }
                });
                SwingUtilities.invokeLater(() -> this.log("Ikinci client kapandi: " + string5));
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.log("Ikinci client hatasi: " + exception.getMessage()));
            }
            finally {
                if (this._secondClientBtnRef != null) {
                    SwingUtilities.invokeLater(() -> this._secondClientBtnRef.setEnabled(true));
                }
            }
        }, "second-client").start();
    }

    private void analyzeCrashLog(String string) {
        StringBuilder stringBuilder = new StringBuilder("=== HATA ANALIZI ===\n\n");
        if (string.contains("OutOfMemoryError") || string.contains("Java heap space")) {
            stringBuilder.append("[RAM] Bellek yetersiz! Ayarlar'dan RAM artirin.\n");
        }
        if (string.contains("missing mod") || string.contains("ModLoadingException") || string.contains("MixinException")) {
            stringBuilder.append("[MOD] Eksik/uyumsuz mod. Modlari guncellemeyi deneyin.\n");
        }
        if (string.contains("SocketTimeoutException") || string.contains("Connection timed out")) {
            stringBuilder.append("[AG] Sunucu baglantisi zaman asimina ugradi.\n");
        }
        if (string.contains("Failed to verify username") || string.contains("Invalid session")) {
            stringBuilder.append("[OTURUM] Oturum gecersiz. Tekrar giris yapin.\n");
        }
        if (string.contains("Pixel format not accelerated") || string.contains("OpenGL")) {
            stringBuilder.append("[GPU] OpenGL sorunu. GPU suruculerini guncelleyin.\n");
        }
        if (string.contains("Unable to load native") || string.contains("natives")) {
            stringBuilder.append("[SISTEM] Native kutuphaneler yuklenemedi.\n");
        }
        if (string.contains("Incompatible magic value") || string.contains("Unsupported class file")) {
            stringBuilder.append("[JAVA] Yanlis Java surumu. Java 21 kurun.\n");
        }
        if (string.contains("No space left") || string.contains("DiskSpace")) {
            stringBuilder.append("[DISK] Disk alani yetersiz!\n");
        }
        if (stringBuilder.toString().equals("=== HATA ANALIZI ===\n\n")) {
            stringBuilder.append("Bilinen bir sorun tespit edilemedi.\nDiscord'dan yardim alin.\n");
        }
        JOptionPane.showMessageDialog(this, stringBuilder.toString(), "Hata Analizi", 1);
    }

    private MyServersPanel buildMyServersTab() {
        return new MyServersPanel((string, string2, string3, string4) -> SwingUtilities.invokeLater(() -> {
            String[] stringArray;
            if (this.currentInstance == null) {
                JOptionPane.showMessageDialog(this, "Once bir instance secin!", "Hata", 2);
                return;
            }
            this.comboEventsEnabled = false;
            this.versionCombo.setSelectedItem(string2);
            if (this.loaderCombo != null) {
                this.loaderCombo.setSelectedItem(string3);
            }
            this.comboEventsEnabled = true;
            if (this.loaderButtons != null) {
                stringArray = new String[]{"Vanilla", "Fabric", "Forge", "NeoForge", "OptiFine"};
                for (int i = 0; i < this.loaderButtons.length && i < stringArray.length; ++i) {
                    if (this.loaderButtons[i] == null) continue;
                    this.loaderButtons[i].setSelected(stringArray[i].equalsIgnoreCase(string3));
                }
            }
            stringArray = string.split(":");
            String string5 = stringArray[0];
            int n = stringArray.length > 1 ? Integer.parseInt(stringArray[1].trim()) : 25565;
            // V33: Her sunucu KENDI instance'inda oynanir. Bolece Mods /
            // Shaders / Resource Packs sekmeleri o sunucuya ozel calisir:
            // Fabric/Forge modlarini, shader'larini, RP'lerini sadece bu
            // sunucunun instance'ina kurabilirsin. Eski davranis tek global
            // instance'i degistiriyordu ve doLaunch()->saveCurrentInstance()
            // combo UI'daki eski loader'i geri yazarak Fabric secimini
            // eziyordu (loader hicbir zaman oyunai ulasmıyordu).
            String serverLoader = string3 == null || string3.isBlank() ? "Vanilla" : string3;
            String serverInstName = ("Server-" + string5).replaceAll("[^\\w\\-.]", "_");
            if (!InstanceManager.listNames().contains(serverInstName)) {
                Instance serverInst = InstanceManager.create(serverInstName);
                serverInst.lastVersion = string2;
                serverInst.loader = serverLoader;
                serverInst.save();
                this.refreshInstanceCombo(serverInstName);
            }
            this.switchInstance(serverInstName);
            if (this.currentInstance != null) {
                // Sunucu kaydindaki surum/loader bu oynanis icin gecerlidir.
                this.currentInstance.lastVersion = string2;
                this.currentInstance.loader = serverLoader;
                this.currentInstance.save();
                this.applyInstanceToUi();
            }
            this._pendingServerHost = string5;
            this._pendingServerPort = n;
            this._pendingServerPassword = string4;
            this.tabs.setSelectedIndex(1);
            this.doLaunch();
        }), () -> this.doChangeNameDialog(), () -> this.launchSecondClient(), () -> {
            if (this.manifest == null) {
                return new String[]{"1.21.5"};
            }
            List<VersionManifest.Entry> list = this.manifest.releasesOnly();
            String[] stringArray = new String[list.size()];
            for (int i = 0; i < list.size(); ++i) {
                stringArray[i] = list.get((int)i).id;
            }
            return stringArray;
        }, ip -> {
            // V33.2: Sunucunun KENDI instance'ina mod kurma istegi.
            // Server-<ip> instance'ini bulur/olusturur, loader'ina gecer,
            // Mods sekmesini acar. Kullanici oradan mod/shader/RP kurar;
            // hepsi o sunucunun instance'ina gider. Loader Vanilla ise
            // once loader secimine yonlendirir (mod'lar loader'siz
            // yuklenmez).
            String instName = ("Server-" + ip).replaceAll("[^\\w\\-.]", "_");
            if (!InstanceManager.listNames().contains(instName)) {
                Instance inst = InstanceManager.create(instName);
                inst.save();
                this.refreshInstanceCombo(instName);
            }
            this.switchInstance(instName);
            if (this.currentInstance == null) {
                return;
            }
            if ("Vanilla".equalsIgnoreCase(this.currentInstance.loader)) {
                JOptionPane.showMessageDialog(this,
                    L10n.isEnglish()
                        ? "This server instance uses Vanilla.\nMods need a mod loader.\n\nPick a loader (e.g. Fabric) on the Home tab first, then install mods here."
                        : "Bu sunucu instance'i Vanilla.\nMod kurmak icin bir mod loader gerekiyor.\n\nOnce Home sekmesinden bir loader sec (orn. Fabric), sonra buradan mod kur.",
                    L10n.isEnglish() ? "Mod loader needed" : "Mod loader gerekli", 2);
            }
            this.tabs.setSelectedIndex(2); // Mods sekmesi
        });
    }

    private void doChangeNameDialog() {
        if (this.session == null) {
            JOptionPane.showMessageDialog(this, "\u00d6nce giri\u015f yap\u0131n!", "Giri\u015f Gerekli", 2);
            return;
        }
        String string = JOptionPane.showInputDialog(this, "Yeni oyuncu ismini girin (1-16 karakter, a-z A-Z 0-9 _):", this.session.username, 3);
        if (string == null) {
            return;
        }
        if ((string = string.trim()).isEmpty() || string.equals(this.session.username)) {
            return;
        }
        if (string.length() > 16 || !string.matches("[a-zA-Z0-9_]+")) {
            JOptionPane.showMessageDialog(this, "Ge\u00e7ersiz isim! 1-16 karakter, sadece harf/rakam/alt \u00e7izgi.", "Hata", 0);
            return;
        }
        String string2 = this.session.username;
        this.session.username = string;
        this.session.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + string).getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
        this.session.userType = "legacy";
        this.session.accessToken = this.session.uuid;
        AccountManager.add(this.session);
        this.updateAccountLabel();
        this.log("\u0130sim defi\u015ftirildi: " + string2 + " \u2192 " + string);
        NameChangeServer nameChangeServer = GameLauncher.getActiveNameServer();
        if (nameChangeServer != null) {
            nameChangeServer.updateSession(this.session);
            JOptionPane.showMessageDialog(this, "\u0130sim '" + string + "' olarak defi\u015ftirildi!\nSunucudan ayr\u0131l\u0131p tekrar baflan\u0131n.", "\u0130sim Defi\u015ftirildi", 1);
        } else {
            JOptionPane.showMessageDialog(this, "\u0130sim '" + string + "' olarak defi\u015ftirildi!\nYeni isimle oyunu ba\u015flatabilirsiniz.", "\u0130sim Defi\u015ftirildi", 1);
        }
    }

    private static String fmtPlayTime(long l) {
        if (l < 60L) {
            return l + "s";
        }
        if (l < 3600L) {
            return l / 60L + " dk";
        }
        return l / 3600L + " sa " + l % 3600L / 60L + " dk";
    }

    private void showJvmProfileMenu(JButton jButton) {
        JPopupMenu jPopupMenu = new JPopupMenu();
        int n = this.currentInstance != null ? this.currentInstance.ramGB : 4;
        boolean bl = this.currentInstance != null && this.currentInstance.getJavaMajorVersion() >= 21;
        boolean bl2 = this.currentInstance != null && this.currentInstance.getJavaMajorVersion() >= 17;
        String string = "-Xms" + n + "G -Xmx" + n + "G ";
        boolean bl3 = L10n.isEnglish();
        String string2 = n >= 12 ? "32M" : (n >= 6 ? "16M" : "8M");
        String string3 = n >= 8 ? "40" : "30";
        String string4 = n >= 8 ? "50" : "40";
        String string5 = "-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=" + string3 + " -XX:G1MaxNewSizePercent=" + string4 + " -XX:G1HeapRegionSize=" + string2 + " -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1";
        String string6 = bl ? "-XX:+UseZGC -XX:+ZGenerational -XX:+AlwaysPreTouch -XX:+DisableExplicitGC -XX:+PerfDisableSharedMem -XX:+UnlockExperimentalVMOptions" : (bl2 ? "-XX:+UseZGC -XX:+AlwaysPreTouch -XX:+DisableExplicitGC -XX:+PerfDisableSharedMem -XX:+UnlockExperimentalVMOptions" : null);
        String string7 = "-XX:+UseShenandoahGC -XX:ShenandoahGCMode=iu -XX:+AlwaysPreTouch -XX:+DisableExplicitGC -XX:+PerfDisableSharedMem";
        String string8 = "-XX:+UseSerialGC";
        Object[][] objectArrayArray = new Object[][]{{bl3 ? "\u26a1 Aikar's G1GC \u2014 General Purpose (Recommended)" : "\u26a1 Aikar's G1GC \u2014 Genel Ama\u00e7l\u0131 (\u00d6nerilen)", string + string5, true}, {bl3 ? "\ud83d\ude80 ZGC Low Latency \u2014 8GB+ RAM, Java 21" : "\ud83d\ude80 ZGC D\u00fc\u015f\u00fck Gecikme \u2014 8GB+ RAM, Java 21", string6 != null ? string + string6 : null, string6 != null}, {bl3 ? "\ud83c\udf3f Shenandoah \u2014 Low Pause, OpenJDK 11+" : "\ud83c\udf3f Shenandoah \u2014 D\u00fc\u015f\u00fck Pause, OpenJDK 11+", string + string7, true}, {bl3 ? "\ud83d\udce6 Heavy Modpack \u2014 6GB+ RAM, Aikar's + Long GC" : "\ud83d\udce6 Af\u0131r Modpack \u2014 6GB+ RAM, Aikar's + Uzun GC", string + string5 + " -XX:MaxGCPauseMillis=200", true}, {bl3 ? "\u2699 Forge / NeoForge \u2014 Aikar's + Mod Compat" : "\u2699 Forge / NeoForge \u2014 Aikar's + Mod Uyumu", string + string5 + " -XX:MaxGCPauseMillis=75 -Dfml.readTimeout=180", true}, {bl3 ? "\ud83c\udfdb Legacy 1.8.9\u20131.12.2 \u2014 G1 + Low Pause" : "\ud83c\udfdb Eski 1.8.9\u20131.12.2 \u2014 G1 + D\u00fc\u015f\u00fck Pause", string + string5 + " -XX:MaxGCPauseMillis=35", true}, {bl3 ? "\ud83d\udca1 Low RAM (2GB) \u2014 SerialGC" : "\ud83d\udca1 D\u00fc\u015f\u00fck RAM (2GB) \u2014 SerialGC", "-Xms512M -Xmx2G " + string8, true}, {bl3 ? "\ud83c\udfae Competitive / Hypixel \u2014 G1 + Fast GC" : "\ud83c\udfae Rekabet\u00e7i / Hypixel \u2014 G1 + H\u0131zl\u0131 GC", string + string5 + " -XX:MaxGCPauseMillis=50 -XX:+OptimizeStringConcat -XX:+UseStringDeduplication", true}, {bl3 ? "\ud83e\uddea Developer / Debug \u2014 Verbose GC Logging" : "\ud83e\uddea Geli\u015ftirici / Debug \u2014 GC Log Aktif", string + string5 + " -Xlog:gc*:file=gc.log:time:filecount=5,filesize=20m", true}, {bl3 ? "\ud83e\uddf9 Clean \u2014 RAM Only, No Extra Flags" : "\ud83e\uddf9 Temiz \u2014 Sadece RAM, Ekstra Bayrak Yok", string.trim(), true}};
        for (Object[] objectArray : objectArrayArray) {
            Object object;
            String string9 = (String)objectArray[0];
            String string10 = (String)objectArray[1];
            boolean bl4 = (Boolean)objectArray[2];
            if (!bl4 || string10 == null) {
                object = new JMenuItem(string9 + (bl3 ? " (requires Java 17+)" : " (Java 17+ gerekir)"));
                ((JMenuItem)object).setEnabled(false);
                jPopupMenu.add((JMenuItem)object);
                continue;
            }
            object = string10;
            JMenuItem jMenuItem = new JMenuItem(string9);
            jMenuItem.addActionListener(arg_0 -> this.onJvmProfileSelect((String)object, arg_0));
            jPopupMenu.add(jMenuItem);
        }
        jPopupMenu.addSeparator();
        JMenuItem jMenuItem = new JMenuItem(bl3 ? "\u2139 Current RAM: " + n + "GB  |  Java " + String.valueOf(this.currentInstance != null ? Integer.valueOf(this.currentInstance.getJavaMajorVersion()) : "?") : "\u2139 Mevcut RAM: " + n + "GB  |  Java " + String.valueOf(this.currentInstance != null ? Integer.valueOf(this.currentInstance.getJavaMajorVersion()) : "?"));
        jMenuItem.setEnabled(false);
        jPopupMenu.add(jMenuItem);
        jPopupMenu.show(jButton, 0, jButton.getHeight());
    }

    private void doAutoBackup(Instance instance) {
        if (instance == null || !instance.autoBackup) {
            return;
        }
        File file = new File(instance.dir(), "saves");
        if (!file.isDirectory()) {
            return;
        }
        new Thread(() -> {
            try {
                File file3 = new File(instance.dir(), "backups");
                file3.mkdirs();
                String string2 = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());
                File file4 = new File(file3, "backup_" + string2 + ".zip");
                InstanceZip.exportSavesDir(file, file4);
                this.log("Yedekleme tamamlandi: " + file4.getName());
                File[] fileArray = file3.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".zip"));
                if (fileArray != null && fileArray.length > 5) {
                    Arrays.sort(fileArray, Comparator.comparingLong(File::lastModified));
                    for (int i = 0; i < fileArray.length - 5; ++i) {
                        fileArray[i].delete();
                    }
                }
            }
            catch (Exception exception) {
                this.log("Yedekleme hatasi: " + exception.getMessage());
            }
        }, "auto-backup").start();
    }

    private void showCrashLog() {
        Object object;
        Object object2;
        File file = new File(Paths.GAME_DIR, "launcher_errors.log");
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "Hata g\u00fcnl\u00fcf\u00fc bulunamad\u0131.\nlauncher_errors.log hen\u00fcz olu\u015fturulmam\u0131\u015f.", "Hata G\u00fcnl\u00fcf\u00fc", 1);
            return;
        }
        try {
            object2 = Files.readAllBytes(file.toPath());
            object = new String((byte[])object2, StandardCharsets.UTF_8);
            if (((String)object).isEmpty()) {
                object = "(Hata g\u00fcnl\u00fcf\u00fc bo\u015f)";
            }
        }
        catch (Exception exception) {
            object = "G\u00fcnl\u00fck okunamad\u0131: " + exception.getMessage();
        }
        object2 = new JTextArea((String)object, 20, 60);
        ((JTextComponent)object2).setEditable(false);
        ((JTextArea)object2).setFont(new Font("Monospaced", 0, 11));
        JTextArea jTextArea = (JTextArea)object2;
        JScrollPane jScrollPane = new JScrollPane((Component)object2);
        jScrollPane.setPreferredSize(new Dimension(700, 400));
        ((JTextComponent)object2).setCaretPosition(((JTextComponent)object2).getDocument().getLength());
        JPanel jPanel = new JPanel(new BorderLayout(0, 8));
        jPanel.add((Component)jScrollPane, "Center");
        JButton jButton = UiFx.accentButton("Analiz Et");
        jButton.addActionListener(actionEvent -> this.onShowCrashLog0(jTextArea, actionEvent));
        JButton jButton2 = UiFx.dangerButton("G\u00fcnl\u00fcf\u00fc Temizle");
        jButton2.addActionListener(actionEvent -> MainWindow.onShowCrashLog1(file, jTextArea, actionEvent));
        JButton jButton3 = UiFx.ghostButton(com.lubv.launcher.core.L10n.isEnglish() ? "Open File" : "Dosyay\u0131 A\u00e7");
        jButton3.addActionListener(actionEvent -> {
            try {
                Desktop.getDesktop().open(file);
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
        JPanel jPanel2 = new JPanel(new FlowLayout(2));
        jPanel2.add(jButton);
        jPanel2.add(jButton3);
        jPanel2.add(jButton2);
        jPanel.add((Component)jPanel2, "South");
        JDialog jDialog = new JDialog(this, "Hata G\u00fcnl\u00fcf\u00fc \u2014 launcher_errors.log", false);
        jDialog.setContentPane(jPanel);
        jDialog.pack();
        jDialog.setLocationRelativeTo(this);
        jDialog.setVisible(true);
    }

    private void exportCurrentInstance() {
        if (this.currentInstance == null) {
            this.log("Aktif instance yok.");
            return;
        }
        this.exportInstance(this.currentInstance);
    }

    private void exportInstance(Instance instance) {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setSelectedFile(new File(instance.name + ".zip"));
        jFileChooser.setFileFilter(new FileNameExtensionFilter("ZIP", "zip"));
        jFileChooser.setDialogTitle("Instance D\u0131\u015fa Aktar");
        if (jFileChooser.showSaveDialog(this) != 0) {
            return;
        }
        File file = jFileChooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".zip")) {
            file = new File(file.getAbsolutePath() + ".zip");
        }
        File file2 = file;
        new Thread(() -> {
            try {
                this.log(" " + instance.name + "d\u0131\u015fa aktar\u0131l\u0131yor...");
                InstanceZip.export(instance, file2, (n, string) -> SwingUtilities.invokeLater(() -> this.log("[" + n + "%] " + string)));
                SwingUtilities.invokeLater(() -> this.log("D\u0131\u015fa aktar\u0131ld\u0131: " + file2.getAbsolutePath()));
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.log("D\u0131\u015fa aktarma hatas\u0131: " + exception.getMessage()));
            }
        }, "instance-export").start();
    }

    private void importInstance() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileFilter(new FileNameExtensionFilter("ZIP", "zip"));
        jFileChooser.setDialogTitle("Instance \u0130\u00e7e Aktar");
        if (jFileChooser.showOpenDialog(this) != 0) {
            return;
        }
        File file = jFileChooser.getSelectedFile();
        String string = file.getName().replace(".zip", "").replaceAll("[^\\w\\- .]", "_");
        String string2 = JOptionPane.showInputDialog(this, "Yeni instance ad\u0131:", string);
        if (string2 == null || string2.trim().isEmpty()) {
            return;
        }
        if (!(string2 = string2.trim()).matches("[\\w\\- .]+")) {
            JOptionPane.showMessageDialog(this, "Ge\u00e7ersiz ad.", "Hata", 0);
            return;
        }
        if (InstanceManager.listNames().contains(string2)) {
            JOptionPane.showMessageDialog(this, "Bu adda instance zaten var.", "Hata", 0);
            return;
        }
        String string3 = string2;
        new Thread(() -> {
            try {
                this.log(" " + string3 + "i\u00e7e aktar\u0131l\u0131yor...");
                InstanceZip.importZip(file, string3, (n, msg) -> SwingUtilities.invokeLater(() -> this.log("[" + n + "%] " + msg)));
                SwingUtilities.invokeLater(() -> {
                    this.log(" \u0130\u00e7e aktar\u0131ld\u0131: " + string3);
                    this.refreshInstanceCombo(string3);
                    this.refreshInstancesGrid();
                });
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.log(" \u0130\u00e7e aktarma hatas\u0131: " + exception.getMessage()));
            }
        }, "instance-import").start();
    }

    private void switchInstance(String string) {
        if (string == null || this.currentInstance != null && this.currentInstance.name.equals(string)) {
            return;
        }
        this.saveCurrentInstance();
        this.currentInstance = Instance.load(string);
        if (this.currentInstance == null) {
            return;
        }
        this.settings.activeInstance = string;
        this.settings.save();
        this.applyInstanceToUi();
        this.modsPanel.refreshInstalled();
        this.shaderPanel.refreshInstalled();
        this.resourcepacksPanel.refreshInstalled();
        this.log(L10n.isEnglish() ? "Instance: " + string : "\u00d6rnek: " + string);
        DiscordRpc.updateInstance(string);
    }

    private void saveCurrentInstance() {
        if (this.currentInstance == null) {
            return;
        }
        if (this.versionCombo != null && this.versionCombo.getSelectedItem() != null) {
            this.currentInstance.lastVersion = (String)this.versionCombo.getSelectedItem();
        }
        if (this.loaderCombo != null && this.loaderCombo.getSelectedItem() != null) {
            this.currentInstance.loader = (String)this.loaderCombo.getSelectedItem();
        }
        if (this.ramSlider != null) {
            this.currentInstance.ramGB = this.ramSlider.getValue();
        }
        if (this.javaPathField != null) {
            this.currentInstance.javaPath = this.javaPathField.getText().trim();
        }
        if (this.jvmArgsField != null) {
            this.currentInstance.jvmArgs = this.jvmArgsField.getText().trim();
        }
        if (this.preLaunchField != null) {
            this.currentInstance.preLaunchCmd = this.preLaunchField.getText().trim();
        }
        this.currentInstance.save();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void applyInstanceToUi() {
        if (this.currentInstance == null) {
            return;
        }
        this.comboEventsEnabled = false;
        try {
            Object object;
            if (this.loaderCombo != null) {
                this.loaderCombo.setSelectedItem(this.currentInstance.loader);
            }
            if (this.loaderButtons != null) {
                String[] loaderNames = new String[]{"Vanilla", "Fabric", "Forge", "NeoForge", "OptiFine"};
                for (int i = 0; i < this.loaderButtons.length && i < loaderNames.length; ++i) {
                    if (this.loaderButtons[i] == null) continue;
                    this.loaderButtons[i].setSelected(loaderNames[i].equalsIgnoreCase(this.currentInstance.loader));
                }
            }
            if (this.ramSlider != null) {
                this.ramSlider.setValue(this.currentInstance.ramGB);
            }
            if (this.ramValueLabel != null) {
                this.ramValueLabel.setText(this.currentInstance.ramGB + "GB");
            }
            if (this.javaPathField != null) {
                this.javaPathField.setText(this.currentInstance.javaPath != null ? this.currentInstance.javaPath : "");
            }
            if (this.jvmArgsField != null) {
                this.jvmArgsField.setText(this.currentInstance.jvmArgs != null ? this.currentInstance.jvmArgs : "");
            }
            if (this.preLaunchField != null) {
                this.preLaunchField.setText(this.currentInstance.preLaunchCmd != null ? this.currentInstance.preLaunchCmd : "");
            }
            if (this.playTimeLabel != null) {
                long l = this.currentInstance.totalPlaySeconds;
                String string = l <= 0L ? "Hic oynanmamis" : "Toplam sure: " + MainWindow.fmtPlayTime(l);
                if (this.currentInstance.lastPlayedMs > 0L) {
                    Instant instant = Instant.ofEpochMilli(this.currentInstance.lastPlayedMs);
                    LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                    string = string + "  |  Son: " + String.valueOf(localDate);
                }
                this.playTimeLabel.setText(string);
            }
            if (this.versionHistoryLabel != null && this.currentInstance.versionHistory != null && !this.currentInstance.versionHistory.isEmpty()) {
                this.versionHistoryLabel.setText("Gecmis: " + String.join((CharSequence)" > ", this.currentInstance.versionHistory));
            } else if (this.versionHistoryLabel != null) {
                this.versionHistoryLabel.setText("");
            }
            if (this.gameDirField != null && this.currentInstance.dir() != null) {
                this.gameDirField.setText(this.currentInstance.dir().getAbsolutePath());
            }
            if (this.manifest != null) {
                this.populateVersionCombo();
            } else if (this.versionCombo != null && this.currentInstance.lastVersion != null && !this.currentInstance.lastVersion.isEmpty()) {
                this.versionCombo.setSelectedItem(this.currentInstance.lastVersion);
            }
        }
        finally {
            this.comboEventsEnabled = true;
        }
    }

    private void createInstance() {
        String string = JOptionPane.showInputDialog(this, "Yeni \u00f6rnek ad\u0131:", "Yeni \u00d6rnek", 3);
        if (string == null || string.trim().isEmpty()) {
            return;
        }
        if (!(string = string.trim()).matches("[\\w\\- .]+")) {
            JOptionPane.showMessageDialog(this, "Ge\u00e7ersiz ad.", "Hata", 0);
            return;
        }
        if (InstanceManager.listNames().contains(string)) {
            JOptionPane.showMessageDialog(this, "Bu adda bir \u00f6rnek zaten var.", "Hata", 0);
            return;
        }
        InstanceManager.create(string);
        this.refreshInstanceCombo(string);
        this.log("Olu\u015fturuldu: " + string);
    }

    private void deleteInstance() {
        if (this.currentInstance == null) {
            return;
        }
        String string = this.currentInstance.name;
        if (InstanceManager.listNames().size() <= 1) {
            JOptionPane.showMessageDialog(this, "En az bir \u00f6rnek kalmal\u0131.", "Uyar\u0131", 2);
            return;
        }
        int n = JOptionPane.showConfirmDialog(this, "'" + string + "' silinsin mi?", "\u00d6rnefi Sil", 0);
        if (n != 0) {
            return;
        }
        InstanceManager.delete(string);
        this.currentInstance = null;
        List<String> list = InstanceManager.listNames();
        String string2 = list.isEmpty() ? "default" : list.get(0);
        this.refreshInstanceCombo(string2);
        this.log("\u00d6rnek silindi: " + string);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void refreshInstanceCombo(String string) {
        this.comboEventsEnabled = false;
        try {
            this.instanceCombo.removeAllItems();
            for (String string2 : InstanceManager.listNames()) {
                this.instanceCombo.addItem(string2);
            }
            if (string != null) {
                this.instanceCombo.setSelectedItem(string);
            }
        }
        finally {
            this.comboEventsEnabled = true;
        }
        this.refreshInstancesGrid();
    }

    private void refreshInstancesAfterModpack() {
        if (this.currentInstance != null) {
            this.refreshInstanceCombo(this.currentInstance.name);
        }
    }

    private void doLaunch() {
        this.saveCurrentInstance();
        this.launchCurrentInstance();
    }

    /**
     * V34: 2. Client - ayni instance'in ikinci bir oyun surecini baslatir.
     * Ana LAUNCH akisindan bagimsizdir: this.launching kilidine,
     * ilerleme cubuguna ve Stop (Sonlandir) butonuna dokunmaz; ilk oyun
     * acikken de ikinci bir client acilabilir. Giris penceresinde baska
     * bir hesap secilirse iki client birbirinden bagimsiz oynanir.
     */
    /**
     * V34.5: 2nd Client kurulum diyalogu - isim, instance (kendi
     * modlari/shaderlari/doku paketleriyle) ve hesap secilir. Boylece
     * ikinci client tamamen bagimsiz bir profil gibi davranir.
     */
    private void showSecondClientDialog() {
        final boolean en = L10n.isEnglish();
        if (this.manifest == null) {
            this.log(en ? "Version list not loaded yet." : "S\u00fcr\u00fcm listesi hen\u00fcz y\u00fcklenmedi.");
            return;
        }
        this.saveCurrentInstance();
        java.util.ArrayList<String> instNames = new java.util.ArrayList<String>();
        if (this.currentInstance != null) {
            instNames.add(this.currentInstance.name);
        }
        for (String nm : InstanceManager.listNames()) {
            if (!instNames.contains(nm)) {
                instNames.add(nm);
            }
        }
        if (instNames.isEmpty()) {
            this.log(en ? "No instances available." : "Kullanilabilir örnek yok.");
            return;
        }
        // Hesap listesi: aktif hesap + kayitli hesaplar + yeni offline secenegi
        java.util.ArrayList<MinecraftSession> accs = new java.util.ArrayList<MinecraftSession>();
        if (this.session != null) {
            accs.add(this.session);
        }
        for (MinecraftSession acc : AccountManager.list()) {
            boolean dup = false;
            for (MinecraftSession a2 : accs) {
                if (a2.username != null && a2.username.equalsIgnoreCase(acc.username)) {
                    dup = true;
                    break;
                }
            }
            if (!dup) {
                accs.add(acc);
            }
        }
        final javax.swing.JDialog dlg = new javax.swing.JDialog(this, en ? "Launch 2nd Client" : "2. Client Baslat", true);
        dlg.getRootPane().putClientProperty("JRootPane.titleBarBackground", Theme.TITLE_BAR);
        dlg.getRootPane().putClientProperty("JRootPane.titleBarForeground", Theme.TEXT_PRIMARY);
        JPanel root = new JPanel(new java.awt.GridBagLayout());
        root.setBackground(Theme.BG_BASE);
        root.setBorder(new EmptyBorder(18, 20, 16, 20));
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = 2;
        gc.anchor = 17;
        int row = 0;
        JLabel title = new JLabel(en ? "\u25d6  Configure the second client" : "\u25d6  2. client ayarlari");
        title.setFont(title.getFont().deriveFont(1, 15.0f));
        title.setForeground(Theme.TEXT_PRIMARY);
        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        root.add(title, gc);
        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = row;
        root.add(UiFx.label(en ? "Client name:" : "Client adi:"), gc);
        javax.swing.JTextField nameField = new JTextField("2nd Client", 18);
        nameField.setFont(Theme.uiFont(java.awt.Font.PLAIN, 13.0f));
        gc.gridx = 1;
        gc.gridy = row++;
        root.add(nameField, gc);
        gc.gridx = 0;
        gc.gridy = row;
        root.add(UiFx.label(en ? "Instance (mods, shaders, packs):" : "Örnek (modlar, shaderlar, paketler):"), gc);
        javax.swing.JComboBox<String> instCombo = new javax.swing.JComboBox<String>(instNames.toArray(new String[0]));
        gc.gridx = 1;
        gc.gridy = row++;
        root.add(instCombo, gc);
        JLabel instHint = new JLabel(en ? "<html><body style='width:280px'>The client uses this instance's mods, shaders, resource packs, version and loader. Manage them in the tabs first, then pick the instance here.</body></html>" : "<html><body style='width:280px'>Client, bu örnefin modlar\u0131n\u0131, shaderlar\u0131n\u0131, doku paketlerini, sürümünü ve loader\u0131n\u0131 kullan\u0131r. Önce sekmelerden kur, sonra buradan seç.</body></html>");
        instHint.setFont(instHint.getFont().deriveFont(0, 11.0f));
        instHint.setForeground(Theme.TEXT_MUTED);
        gc.gridx = 1;
        gc.gridy = row++;
        root.add(instHint, gc);
        gc.gridx = 0;
        gc.gridy = row;
        root.add(UiFx.label(en ? "Account:" : "Hesap:"), gc);
        javax.swing.JComboBox<String> accCombo = new javax.swing.JComboBox<String>();
        for (MinecraftSession a : accs) {
            accCombo.addItem((a.uuid != null && a.uuid.contains("-") ? "[MS] " : "[Offline] ") + a.username);
        }
        accCombo.addItem(en ? "+ New offline account..." : "+ Yeni çevrimdışı hesap...");
        gc.gridx = 1;
        gc.gridy = row++;
        root.add(accCombo, gc);
        JPanel btnRow = new JPanel(new java.awt.FlowLayout(2, 8, 4));
        btnRow.setOpaque(false);
        JButton cancelBtn = UiFx.ghostButton(en ? "Cancel" : "\u0130ptal");
        cancelBtn.addActionListener(actionEvent -> dlg.dispose());
        JButton launchBtn = UiFx.launchButton(en ? "\u25d6  Launch" : "\u25d6  Baslat");
        launchBtn.setPreferredSize(new Dimension(140, 40));
        final javax.swing.JDialog dlgRef = dlg;
        launchBtn.addActionListener(actionEvent -> {
            String clientName = nameField.getText().trim();
            if (clientName.isEmpty()) {
                clientName = "2nd Client";
            }
            String chosenInst = (String)instCombo.getSelectedItem();
            MinecraftSession chosenAcc;
            int sel = accCombo.getSelectedIndex();
            if (sel >= 0 && sel < accs.size()) {
                chosenAcc = accs.get(sel);
            }
            else {
                // "+ New offline account..." secildi
                OfflineLoginDialog offline = new OfflineLoginDialog(this);
                chosenAcc = offline.showAndLogin();
                if (chosenAcc == null) {
                    return;
                }
            }
            Instance inst2 = Instance.load(chosenInst);
            if (inst2 == null || inst2.name == null) {
                this.log(en ? "Instance not found: " + chosenInst : "Örnek bulunamadi: " + chosenInst);
                return;
            }
            dlgRef.dispose();
            this.launchSecondClientNamed(clientName, inst2, chosenAcc);
        });
        btnRow.add(cancelBtn);
        btnRow.add(launchBtn);
        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 2;
        gc.anchor = 13;
        root.add(btnRow, gc);
        dlg.setContentPane(root);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void launchSecondClientNamed(String clientName, Instance inst, MinecraftSession acc2) {
        if (this.manifest == null) {
            this.log(L10n.isEnglish() ? "Version list not loaded yet." : "S\u00fcr\u00fcm listesi hen\u00fcz y\u00fcklenmedi.");
            return;
        }
        // EDT uzerindeki tum degerleri baslangicta yakala; thread icinde
        // UI'dan okuma yapma (kullanici beklerken combo degistirebilir).
        final var session2 = acc2;
        final var manifest2 = this.manifest;
        String ver = inst.lastVersion;
        if (ver == null || ver.isEmpty()) {
            inst.lastVersion = ver = manifest2.latestRelease;
            inst.save();
        }
        final String fVersion = ver;
        final String fLoader = inst.loader;
        final int fRam = inst.ramGB;
        String jvm = inst.jvmArgs;
        if (this.settings.maxPerformanceMode) {
            int gameJavaMajor = JavaRuntime.detectMajor(inst.javaPath != null && !inst.javaPath.isBlank() ? inst.javaPath : "java");
            String perfFlags = PerformanceFlags.recommendedJvmFlagsAsString(fRam, gameJavaMajor);
            jvm = (jvm == null || jvm.isBlank()) ? perfFlags : jvm.trim() + " " + perfFlags;
        }
        final String fJvm = jvm;
        final String fJavaPath = inst.javaPath;
        final File fDir = inst.dir();
        final String fGpu = this.settings.gpuSelection;
        final String fPre = inst.preLaunchCmd != null ? inst.preLaunchCmd : "";
        final boolean en2 = L10n.isEnglish();
        final String fName2 = clientName == null || clientName.isBlank() ? "2nd Client" : clientName;
        this.log("[" + fName2 + "] " + fVersion + " (" + fLoader + ") " + (en2 ? "launching..." : "baslatiliyor...") + " \u2014 " + (en2 ? "account: " : "hesap: ") + (acc2 != null ? acc2.username : "?"));
        new Thread(() -> {
            try {
                GameLauncher.ProgressCallback cb = new GameLauncher.ProgressCallback() {
                    @Override
                    public void onLog(String s) {
                        MainWindow.this.log("[" + fName2 + "] " + s);
                    }

                    @Override
                    public void onProgress(int p, String s) {
                        MainWindow.this.log("[" + fName2 + "] " + s + " (" + p + "%)");
                    }

                    @Override
                    public void onGameExit(int code, long ms) {
                        MainWindow.this.log(en2
                            ? "[" + fName2 + "] exited (code " + code + ", " + (ms / 1000L) + "s)."
                            : "[" + fName2 + "] kapandi (kod " + code + ", " + (ms / 1000L) + " sn).");
                    }

                    @Override
                    public void onCrash(String a, String b, String c, String d) {
                        MainWindow.this.handleCrash(a, b, c, d);
                    }
                };
                GameLauncher.launch(manifest2, fVersion, fLoader, session2, fRam, fJvm, fJavaPath, fGpu, fPre, fDir, cb);
                MainWindow.this.log("[" + fName2 + "] " + (en2 ? "Second client is running." : "2. client calisiyor."));
            }
            catch (Exception ex) {
                MainWindow.this.log("[" + fName2 + "] " + (en2 ? "Launch failed: " : "Baslatma basarisiz: ") + ex.getMessage());
            }
        }, "second-client-launch").start();
    }

    private void injectPendingServerArgs(ArgumentBuilder.LaunchContext launchContext) {
        if (this._pendingServerHost != null && !this._pendingServerHost.isBlank()) {
            launchContext.extraGameArgs.add("--server");
            launchContext.extraGameArgs.add(this._pendingServerHost);
            launchContext.extraGameArgs.add("--port");
            launchContext.extraGameArgs.add(String.valueOf(this._pendingServerPort));
            this._pendingServerHost = null;
            this._pendingServerPort = 25565;
        }
    }

    private void launchInstanceByName(String string) {
        this.saveCurrentInstance();
        this.currentInstance = Instance.load(string);
        if (this.currentInstance == null) {
            this.log("\u00d6rnek bulunamad\u0131: " + string);
            return;
        }
        this.settings.activeInstance = string;
        this.settings.save();
        this.applyInstanceToUi();
        if (this.instanceCombo != null) {
            this.instanceCombo.setSelectedItem(string);
        }
        this.modsPanel.refreshInstalled();
        this.shaderPanel.refreshInstalled();
        this.resourcepacksPanel.refreshInstalled();
        if (this.tabs != null) {
            this.tabs.setSelectedIndex(1);
        }
        this.launchCurrentInstance();
    }

    private void launchCurrentInstance() {
        if (this.launching) {
            return;
        }
        // Bu instance'a ozel atanmis bir hesap varsa, baslatmadan once
        // gecici olarak o hesaba gecis yap - boylece her instance kendi
        // ismi/skin'i ile oynanabilir.
        if (this.currentInstance != null && this.currentInstance.assignedAccountUsername != null && !this.currentInstance.assignedAccountUsername.isBlank()) {
            for (MinecraftSession acc : AccountManager.list()) {
                if (acc.username != null && acc.username.equalsIgnoreCase(this.currentInstance.assignedAccountUsername)) {
                    this.session = acc;
                    break;
                }
            }
        }
        if (this.session == null) {
            int n = JOptionPane.showConfirmDialog(this, "Giri\u015f yapmal\u0131s\u0131n\u0131z. \u00c7evrimd\u0131\u015f\u0131 giri\u015f yapmak ister misiniz?", "Giri\u015f Gerekli", 0);
            if (n == 0) {
                this.doOfflineLogin();
            }
            if (this.session == null) {
                return;
            }
        }
        if (this.manifest == null) {
            this.log("S\u00fcr\u00fcm listesi hen\u00fcz y\u00fcklenmedi.");
            return;
        }
        if (this.currentInstance == null) {
            this.log("Aktif \u00f6rnek yok.");
            return;
        }
        // Oyun baslatilirken hangi instance aktifse onu isim uzerinden
        // sabitliyoruz - oyun kapaninca sure hesaplarken kullanicinin
        // bu sirada baska bir instance'a gecmis olmasi durumundan
        // etkilenmemesi icin (bkz. handleGameExit).
        final String launchTargetInstanceName = this.currentInstance.name;
        String string = this.currentInstance.lastVersion;
        if (string == null || string.isEmpty()) {
            this.currentInstance.lastVersion = string = this.manifest.latestRelease;
            this.currentInstance.save();
        }
        String string2 = string;
        String string3 = this.currentInstance.loader;
        int n = this.currentInstance.ramGB;
        String string4 = this.currentInstance.jvmArgs;
        if (this.settings.maxPerformanceMode) {
            // Maks Performans acikken, kullanicinin kendi belirledigi JVM
            // argumanlarinin uzerine sistem icin en uygun GC/JIT
            // bayraklarini otomatik ekliyoruz. Kullanicinin zaten -Xmx/-Xms
            // belirtmis olmasi sorun degil - ArgumentBuilder bu ikisini
            // ayrica algilayip tekrar eklemiyor.
            // V29: ZGC karari icin OYUNUN gercek Java major surumunu ver.
            // JavaRuntime.detectMajor burada yeni bir -version proc'u
            // acmamak icin erisilebilir durumda; launcher-in kendi surumu
            // hatali ZGC/ZGenerational kararina yol acabiliyordu.
            int gameJavaMajor = JavaRuntime.detectMajor(this.currentInstance.javaPath != null && !this.currentInstance.javaPath.isBlank() ? this.currentInstance.javaPath : "java");
            String perfFlags = PerformanceFlags.recommendedJvmFlagsAsString(n, gameJavaMajor);
            string4 = (string4 == null || string4.isBlank()) ? perfFlags : string4.trim() + " " + perfFlags;
        }
        final String finalExtraJvmArgs = string4;
        String string5 = this.currentInstance.javaPath;
        File file = this.currentInstance.dir();
        File file2 = this.currentInstance.modsDir();
        // V35.1 FPS MAKSIMUM: Maks Performans acikken options.txt'yi FPS
        // odakli ayarlara ceker (maxFps=260, vsync off, renderDistance 8,
        // particles minimal, vb.). Sadece mevcut degerden IYILESTIRME varsa
        // yazar; kullanicinin daha agresif yaptigi ayar korunur.
        if (this.settings.maxPerformanceMode) {
            try {
                int changed = com.lubv.launcher.game.OptionsTxtOptimizer.optimize(this.currentInstance.dir());
                if (changed > 0) {
                    this.log("[FPS] options.txt performans ayarlari uygulandi (" + changed + " ayar)");
                }
            }
            catch (Throwable ignored) {
                // options optimizasyonu baslatmayi bozmasin
            }
        }
        file2.mkdirs();
        this.launching = true;
        this.launchButton.setEnabled(false);
        this.launchButton.setText(L10n.isEnglish() ? "LAUNCHING..." : "BA\u015eLATILIYOR...");
        this.progressBar.setValue(0);
        String string6 = this._pendingServerHost;
        int n2 = this._pendingServerPort;
        String string7 = this._pendingServerPassword;
        this._pendingServerHost = null;
        this._pendingServerPort = 25565;
        this._pendingServerPassword = null;
        new Thread(() -> {
            this.log("[DEBUG] Launch thread basladi");
            this.log("[DEBUG] version=" + string2 + " loader=" + string3 + " session=" + (this.session != null ? this.session.username : "null"));
            try {
                this.autoInstallForLoader(string3, file2, string2);
            }
            catch (Exception exception) {
                this.log("[AutoInstall] HATA: " + exception.getMessage());
            }
            try {
                this.cachedManifest = this.manifest;
                GameLauncher.ProgressCallback progressCallback = new GameLauncher.ProgressCallback(){

                    @Override
                    public void onLog(String string) {
                        MainWindow.this.log(string);
                    }

                    @Override
                    public void onProgress(int n, String string) {
                        SwingUtilities.invokeLater(() -> {
                            UiFx.animateProgress(MainWindow.this.progressBar, n, 250);
                            MainWindow.this.progressStageLabel.setText(string);
                        });
                    }

                    @Override
                    public void onGameExit(int n, long l) {
                        MainWindow.this.handleGameExit(n, l, launchTargetInstanceName);
                        DiscordRpc.onGameStop();
                        MainWindow.this.currentGameProcess = null;
                        SwingUtilities.invokeLater(() -> {
                            if (MainWindow.this.terminateButton != null) {
                                MainWindow.this.terminateButton.setVisible(false);
                            }
                        });
                    }

                    @Override
                    public void onCrash(String string, String string2, String string3, String string4) {
                        MainWindow.this.handleCrash(string, string2, string3, string4);
                    }
                };
                Object object = this.currentInstance != null ? this.currentInstance.name : "";
                String loaderName = this.currentInstance != null ? this.currentInstance.loader : "";
                int n3 = this.currentInstance != null ? ModManager.loadRegistry(file).size() : 0;
                DiscordRpc.onGameStart((String)object, loaderName, string2, n3);
                this.currentGameProcess = string6 != null && !string6.isBlank()
                    ? GameLauncher.launch(this.manifest, string2, string3, this.session, n, finalExtraJvmArgs, string5, this.settings.gpuSelection, this.currentInstance != null ? (this.currentInstance.preLaunchCmd != null ? this.currentInstance.preLaunchCmd : "") : "", file, string6, n2, string7, progressCallback)
                    : GameLauncher.launch(this.manifest, string2, string3, this.session, n, finalExtraJvmArgs, string5, this.settings.gpuSelection, this.currentInstance != null ? (this.currentInstance.preLaunchCmd != null ? this.currentInstance.preLaunchCmd : "") : "", file, progressCallback);
                SwingUtilities.invokeLater(() -> {
                    if (this.terminateButton != null) {
                        this.terminateButton.setVisible(true);
                    }
                });
                this.finishLaunch();
            }
            catch (Exception exception) {
                this.log("HATA: " + exception.getMessage());
                this.finishLaunch();
            }
        }).start();
    }

    private CatChaseOverlay activeCatOverlay = null;

    private void spawnCatEasterEgg() {
        // Birden fazla tik ile yeni bir overlay acilmasini engelle:
        // ONCEKI KONTROL YETERSIZDI: sadece "timer.isRunning()" kontrol
        // ediliyordu. Kullanici animasyonun son karelerinde (timer durmus
        // ama overlay henuz kaldırilmamisken) tekrar tiklayinca ikinci bir
        // overlay aciliyor ve iki kedi ust uste bindigi icin bozuk
        // gorunuyordu. Artik timer bitmis olsa bile overlay hala ekranda
        // ise (finished bayragi ve getLayeredPane icerigi) tik engellenir;
        // ayrıca eski overlay hala gorunurse aninda temizlenir.
        // V39.5.1: overlay varken logo tiklamasi ONCE onu temizler
        // (mod fark etmez). Pin modunda bu acik/kapali toggle'idir;
        // kedi animasyonunda animasyon bittiyse ekranda kalmaz.
        if (this.activeCatOverlay != null) {
            boolean pin = this.activeCatOverlay.pinMode;
            boolean stillAnimating = !this.activeCatOverlay.finished && this.activeCatOverlay.threadRunning;
            if (pin || !stillAnimating) {
                java.awt.Container parent = this.activeCatOverlay.getParent();
                if (parent != null) {
                    parent.remove(this.activeCatOverlay);
                    parent.repaint();
                }
                this.activeCatOverlay.stopCatThread();
                this.activeCatOverlay = null;
                if (pin) {
                    return; // pin: toggle -> kapandi
                }
            } else {
                return; // kedi animasyonu suruyor -> yeni tik gecersiz
            }
        }
        JLayeredPane jLayeredPane = this.getLayeredPane();
        int w = jLayeredPane.getWidth();
        int h = jLayeredPane.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        // Sahnenin tamami tek bir canvas uzerinde (kedi + balik + su +
        // sicrama parcaciklari) 60fps'te elle animasyonlu olarak
        // cizilir. Hicbir emoji/font kullanilmaz; tum sekiller
        // Graphics2D vektor cizimidir, boylece hicbir sistemde bozuk
        // gorunmez. Her tetiklemede 5 farkli varyanttan biri rastgele
        // secilir, boylece easter egg her seferinde ayni gorunmez.
        // V39.5 PIN MODU: foto = "hicbir sey yapma" modu. Sprite sadece
        // FAREYLE SURUKLENEREK konumlandirilan sabit noktada durur;
        // kovalama/ziplama/su koreografisi tamamen kapali.
        boolean pinMode = "photo".equalsIgnoreCase(this.settings.eggMode)
            || "chicken".equalsIgnoreCase(this.settings.eggMode); // chicken: eski ayar uyumlulugu
        java.io.File eggFile = pinMode && this.settings.eggPhotoPath != null && !this.settings.eggPhotoPath.isBlank()
            ? new java.io.File(this.settings.eggPhotoPath) : null;
        if (pinMode && (eggFile == null || !eggFile.isFile() || eggFile.length() == 0L)) {
            this.log(L10n.isEnglish()
                ? "Photo egg: pick a photo first (Settings -> Easter egg mode)"
                : "Foto egg: once Ayarlar -> Easter egg modu'ndan fotograf sec");
            return;
        }
        CatChaseOverlay overlay = new CatChaseOverlay(w, h, this.settings.eggMode, this.settings.eggPhotoPath,
            this.settings.eggPhotoScale,
            pinMode ? this.settings.eggPinX : -1, pinMode ? this.settings.eggPinY : -1, pinMode);
        if (pinMode) {
            overlay.setPinSettings(this.settings);
        }
        overlay.setBounds(0, 0, w, h);
        jLayeredPane.add((Component)overlay, JLayeredPane.POPUP_LAYER);
        jLayeredPane.repaint();
        this.activeCatOverlay = overlay;
        overlay.start(() -> {
            jLayeredPane.remove(overlay);
            jLayeredPane.repaint();
            this.activeCatOverlay = null;
        });
    }

    /**
     * Kedi-balik easter egg sahnesi. Tek bir JComponent uzerinde 60fps
     * (16ms) Timer ile calisan, delta-time tabanli, kare hizindan
     * bagimsiz bir mini-animasyon. Baslarken 18 varyanttan biri rastgele
     * secilir:
     *   FISHING   - kosarak yaklasir, suya ziplar, baligi yakalar
     *   RAMP_JUMP - bir rampaya tirmanip havada takla atarak iner
     *   DIVE      - suya dogrudan dalip icinden baligi agzinda cikar
     *   DOUBLE_LEAP - ilk siçramada baligi kacirir, ikincide yakalar
     *   CHASE     - balik kacar, kedi birkac kez ziplayarak kovalar
     *   BACKFLIP  - baligi yakaladiktan sonra geri geri donerek kacar
     *   SNEAK     - sinsi sinsi surunerek yaklasir, pat diye ziplar
     *   BUTTERFLY - balik yerine su kenarinda kelebek kovalar ve yakalar
     *   --- V29 Minecraft temali 10 yeni varyant ---
     *   CREEPER    - creeper'i kovalar; creeper suya kacar, suda tisilar
     *                ve korkup PATLAR; patlama baligi havaya firlatir,
     *                kedi havadan yakalar (ekran sarsintisi + duman)
     *   ENDERMAN   - cim blogu tasiyan enderman isinlanir; kedi blogu oyuncak yapar
     *   SKELETON   - oklardan ziplayarak kacar, iskeleti devirir
     *   BLAZE      - fireball'u havada geri vurur, blaze dumana bulanir
     *   SLIME      - slime'i takip eder; buyuk slime ikiye bolunur, ikisini de yakalar
     *   AXOLOTL    - axolotl ile suda oynar, baloncuklar savurur
     *   PARROT     - suzulen papaogani rampadan atlayarak yakalamaya calisir, balikla iner
     *   SNOW_GOLEM - kartopu dodger/yakalar; kardan adam erir, kedi kurulanir
     *   CHICKEN    - tavugu kollar, tuyler savrulur, yumurta suya yuvarlanir
     *   PHANTOM    - gokyuzu kararir, hayaletin kuyrugundan havada yakalar
     */
    private static class CatChaseOverlay extends JComponent {
        private enum Variant { FISHING, RAMP_JUMP, DIVE, DOUBLE_LEAP, CHASE, BACKFLIP, SNEAK, BUTTERFLY,
            CREEPER, ENDERMAN, SKELETON, BLAZE, SLIME, AXOLOTL, PARROT, SNOW_GOLEM, CHICKEN, PHANTOM }
        private enum Phase { RUN, LEAP, AIR_SPIN, SPLASH, DIVE_UNDER, SURFACE, MISS_RECOIL, CHASE_HOP, HAPPY, SNEAK_CROUCH, POUNCE, BUTTERFLY_CHASE, DONE,
            MOB_ENTER, MOB_FLEE, MOB_FIZZLE, MOB_EXPLODE, MOB_PLAY, MOB_TELEPORT, MOB_ARROW, MOB_WIN, MOB_FIREBALL, MOB_SPLIT, MOB_BUBBLE, MOB_SWOOP, MOB_SNOWBALL, MOB_MELT, MOB_FLAP, MOB_EGG, MOB_DARKEN, MOB_GRAB }

        // Onceki surumde kedi/balik/su cok kucuktu - kullanici isteği
        // uzerine hepsi 2x buyutuldu (CAT_W 64->128, vb.)
        private static final int CAT_W = 140;
        private static final int CAT_H = 92;
        private static final int FISH_W = 60;
        private static final int FISH_H = 36;

        private final int sceneW;
        private final int sceneH;
        private final int groundY;
        private final int waterX;
        private final int waterW = 260; // onceki 130'un 2 kati

        private final Variant variant;
        private Phase phase = Phase.RUN;
        private double catX;
        private double catY;
        private double catRotation = 0; // AIR_SPIN icin (radyan)
        private double t = 0;              // faz-ici zaman (saniye)
        private double leapStartX;
        private double leapStartY;
        private double leapDuration;
        private double fishBob = 0;
        private double tailWag = 0;
        private double legPhase = 0;
        private boolean fishCaught = false;
        private boolean fishFleeing = false;
        private double fishX;
        private int hopCount = 0;
        private int maxHops = 2;
        private int rampX;
        private int rampTopY;
        // Yeni varyantlar icin ekstra durum:
        private boolean finished = false;      // spawnCatEasterEgg cift-tik korumasi icin
        private double sneakCrouch = 0;        // SNEAK: 0=dik, 1=tam comak
        private double backflipRotation = 0;   // BACKFLIP: geri takla acisi (radyan)
        private double butterflyX;             // BUTTERFLY: kelebek konumu
        private double butterflyY;
        private double butterflyWing = 0;      // kanat cispma fazı
        private double butterflyCx;            // kelebeğin dolastigi merkez
        private double butterflyCy;
        // --- V29: Minecraft mob motoru ---
        private double mobX;                   // mobun mevcut x konumu
        private double mobY;                   // mobun mevcut y konumu (taban noktasi)
        private double mobVx = 0;              // mobun yatay hizi
        private double mobAnim = 0;            // yurume/ziplama/kanat fazi
        private int mobState = 0;              // varyanta ozel alt durum sayaci
        private boolean mobVisible = false;
        private int fireballCount = 0;         // SKELETON/BLAZE/SNOW_GOLEM mermi sayaci
        private double aftermathCx = 0;        // zafer animasyonu merkez konumu
        // V29.2 kedi rigi pozlari: oturup-yalama, korku, squash/stretch
        private double groom = 0;              // 0=dik, 1=oturmus pati yaliyor
        private double groomTarget = 0;
        private double scared = 0;             // 0=sakin, 1=urkmus (kulaklar yapisir, gozler buyur)
        // V29.2.1 premium rig durumlari
        private double headAim = 0;            // yumusatilmis kafa bakis acisi (agir basli izleme)
        private double breathe = 0;            // nefes fazi (govde/kafa mikro hareketi)
        private double earTwitch = 0;          // kulak titremesi (0..1)
        private double earTwitchIn = 2.5;      // sonraki kulak titremesine kalan sure
        private double tailPuff = 0;           // kuyruk kabarma (korku) 0..1
        private double pupil = 1.0;            // bebek boyutu: 0.65 daralik .. 1.6 genis
        private double lean = 0;               // kosma egimi (-1..1)
        private double prevCatX = 0;           // hiz olcumu icin
        private double lastLegStep = 0;        // adim tozu evre sayaci
        private double projectileX = -1000;    // aktif mermi (ok/fireball/kartopu) konumu
        private double projectileY = -1000;
        private double projectileVx = 0;
        private double projectileVy = 0;
        private boolean projectileActive = false;
        // PHANTOM: gokyuzu kararmasi + havada dolanma
        private double skyDarken = 0;
        private double phantomX;
        private double phantomY;
        private double phantomAngle = Math.PI;
        private double shakeAmp = 0;           // MOB_EXPLODE ekran sarsintisi
        private final List<Splash> splashes = new java.util.ArrayList<>();
        private final List<Ripple> ripples = new java.util.ArrayList<>();
        // V29: mob/ortam etkisi parcaciklari (duman, kor, tuy, kar, baloncuk...)
        private final List<Debris> debris = new java.util.ArrayList<>();
        private long lastNanos = 0;
        /** Eski Swing Timer - TRUE-FPS thread gecisinden sonra kullanilmiyor,
         * spawn korumasindaki null-guvenli isRunning() kontrolu icin tutuluyor. */
        /**
         * TRUE 200+ FPS: animasyon artik Swing Timer yerine ozel thread'de
         * kosuyor. Thread her turda fizigi ilerletir (delta-time bazli, hiz
         * degismez) ve EDT'ye step gonderir. Windows Timer'inin ~10-15ms
         * cozunurlugu devre disi - 240+ fps gercek frekans mumkun.
         * stopCatThread() DONE fazinda cagrilarak thread'i durdurur.
         */
        private volatile boolean threadRunning = false;
        private Thread animThread;

        /** Animasyon thread'ini zarifce durdurur (DONE veya overlay kaldirilirken). */
        void stopCatThread() {
            this.threadRunning = false;
            if (this.animThread != null) {
                this.animThread.interrupt();
                this.animThread = null;
            }
        }
        private Runnable onFinished;
        // V39.1: kullanci gorseli / tavuk sprite (null = normal kedi cizimi)
        private java.awt.image.BufferedImage customSprite;
        // V39.2: sprite buyuklugu yuzdesi (50..200, 100 = varsayilan)
        private int eggPhotoScalePercent = 100;

        // V39.4 PIN: true -> sprite sabit noktada, animasyon/koreografi yok
        private final boolean pinMode;
        private final int pinX;  // sahne yuzdesi 0..100 veya -1=varsayilan
        private final int pinY;
        // V39.5: surukleme sonunda konumu kaydetmek icin (spawn set eder).
        private com.lubv.launcher.core.Settings pinSettings;

        void setPinSettings(com.lubv.launcher.core.Settings s) {
            this.pinSettings = s;
        }

        CatChaseOverlay(int sceneW, int sceneH, String eggMode, String eggPhotoPath, int eggScale,
                        int eggPinX, int eggPinY, boolean pinMode) {
            this.sceneW = sceneW;
            this.sceneH = sceneH;
            this.groundY = sceneH - 70;
            this.waterX = sceneW - waterW - 40;
            this.catX = -CAT_W;
            this.catY = groundY;
            this.fishX = this.waterX + this.waterW * 0.5 - FISH_W / 2.0;
            Variant[] variants = Variant.values();
            this.variant = variants[new java.util.Random().nextInt(variants.length)];
            this.rampX = this.waterX - 90;
            this.rampTopY = this.groundY - 70;
            this.butterflyCx = this.waterX + this.waterW * 0.4;
            this.butterflyCy = this.groundY - 60;
            this.butterflyX = this.butterflyCx;
            this.butterflyY = this.butterflyCy;
            this.setOpaque(false);
            this.pinMode = pinMode;
            // V39.4: pin modunda konum yuzdeleri -> piksele cevrilir.
            // -1 (ayarlanmamis) => varsayilan: sol taraf, zeminin hemen ustu.
            if (eggPinX >= 0) {
                this.pinX = Math.max(0, Math.min(100, eggPinX));
            } else {
                this.pinX = 18;
            }
            if (eggPinY >= 0) {
                this.pinY = Math.max(0, Math.min(100, eggPinY));
            } else {
                this.pinY = 74;
            }
            // V39.5 EASTER EGG MODU: yalnizca kullanici fotosu. Dahili
            // tavuk sprite'i kaldirildi (kullanici istegi).
            if ("photo".equalsIgnoreCase(eggMode) && eggPhotoPath != null && !eggPhotoPath.isBlank()) {
                this.customSprite = MainWindow.loadSpriteImage(eggPhotoPath);
            }
            if (this.customSprite != null && eggScale > 0) {
                this.eggPhotoScalePercent = eggScale;
            }
            if (this.pinMode && this.customSprite != null) {
                double ps = this.spriteScale();
                // PIN: baslangicta konumu yerine oturt (merkez X, alt Y).
                this.catX = this.sceneW * this.pinX / 100.0 - CAT_W * 0.5 * ps;
                this.catY = this.sceneH * this.pinY / 100.0;
                this.phase = Phase.DONE; // koreografi akmasin
                this.installPinDrag();
            }
        }

        /**
         * V39.5 PIN SURUKLEME: sprite fareyle suruklenerek konumlandirilir;
         * birakilinca yuzdelik konum ayarlara kaydedilir. Diger tum
         * noktalar contains() override sayesinde alttaki arayuze gecer
         * (launcher GUI'si artik bloklanmiyor).
         */
        private void installPinDrag() {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            final double[] dragOff = new double[2];
            javax.swing.event.MouseInputAdapter ma = new javax.swing.event.MouseInputAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent me) {
                    dragOff[0] = me.getX() - CatChaseOverlay.this.catX;
                    dragOff[1] = me.getY() - CatChaseOverlay.this.catY;
                }

                @Override
                public void mouseDragged(java.awt.event.MouseEvent me) {
                    if (CatChaseOverlay.this.customSprite == null) {
                        return;
                    }
                    double s = CatChaseOverlay.this.spriteScale();
                    double nx = me.getX() - dragOff[0];
                    double ny = me.getY() - dragOff[1];
                    double vw = CAT_W * s;
                    double vh = CAT_H * s;
                    nx = Math.max(-vw * 0.45, Math.min(CatChaseOverlay.this.sceneW - vw * 0.55, nx));
                    ny = Math.max(vh, Math.min(CatChaseOverlay.this.sceneH, ny));
                    CatChaseOverlay.this.catX = nx;
                    CatChaseOverlay.this.catY = ny;
                    CatChaseOverlay.this.repaint();
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent me) {
                    if (CatChaseOverlay.this.pinSettings == null) {
                        return;
                    }
                    double s = CatChaseOverlay.this.spriteScale();
                    int px = (int) Math.round(Math.max(0, Math.min(100,
                        (CatChaseOverlay.this.catX + CAT_W * 0.5 * s) / CatChaseOverlay.this.sceneW * 100.0)));
                    int py = (int) Math.round(Math.max(0, Math.min(100,
                        CatChaseOverlay.this.catY / (double) CatChaseOverlay.this.sceneH * 100.0)));
                    CatChaseOverlay.this.pinSettings.eggPinX = px;
                    CatChaseOverlay.this.pinSettings.eggPinY = py;
                    CatChaseOverlay.this.pinSettings.save();
                }
            };
            this.addMouseListener(ma);
            this.addMouseMotionListener(ma);
        }

        /** Sprite olcegi (paintCat ile ayni formul). */
        private double spriteScale() {
            return Math.max(0.25, Math.min(3.0,
                (this.eggPhotoScalePercent <= 0 ? 100 : this.eggPhotoScalePercent) / 100.0));
        }

        /** V39.5: pin modunda fare olaylari YALNIZCA sprite uzerinde yakalanir;
         * diger her nokta alttaki bilesenlere gecer (GUI kilitlenme fix'i). */
        @Override
        public boolean contains(int mx, int my) {
            if (this.pinMode && this.customSprite != null) {
                double s = this.spriteScale();
                int sw = (int) Math.ceil(CAT_W * s);
                int sh = (int) Math.ceil(CAT_H * s);
                int sx = (int) Math.round(this.catX);
                int sy = (int) Math.round(this.catY - sh);
                return mx >= sx - 4 && mx <= sx + sw + 4 && my >= sy - 4 && my <= sy + sh + 4;
            }
            return super.contains(mx, my);
        }

        void start(Runnable onFinished) {
            this.onFinished = () -> {
                this.finished = true;
                if (onFinished != null) {
                    onFinished.run();
                }
            };
            this.lastNanos = System.nanoTime();
            // TRUE 200+ FPS: Swing Timer yerine dedike animasyon thread'i.
            // (Eski 16ms Timer -> 5ms denemesi Windows timer cozunurlugu
            // yuzunden ~100-125 fps'te kaliyordu; thread ile 240+ fps.)
            this.threadRunning = true;
            this.animThread = new Thread(() -> {
                while (this.threadRunning && !this.finished) {
                    long frameStart = System.nanoTime();
                    try {
                        SwingUtilities.invokeAndWait(this::step);
                    } catch (Exception broken) {
                        break; // EDT olduyse/overlay gittiyse sessiz cik
                    }
                    // Hedef dongu periyodu 4ms (~250 fps); gercek fps
                    // ekran/EDT hizina gore dogal olarak sinirlanir.
                    long elapsed = (System.nanoTime() - frameStart) / 1_000_000L;
                    if (elapsed < 4L) {
                        try { Thread.sleep(4L - elapsed); } catch (InterruptedException ie) { break; }
                    }
                }
            }, "cat-anim");
            this.animThread.setDaemon(true);
            this.animThread.start();
        }

        private void step() {
            // V39.4 PIN MODU: koreografi yok. Bir kare cizdirip thread'i
            // durdur; sprite sabit noktada gorunur kalir.
            if (this.pinMode && this.customSprite != null) {
                this.threadRunning = false;
                this.repaint();
                return;
            }
            long now = System.nanoTime();
            double dt = Math.min(0.05, (now - this.lastNanos) / 1_000_000_000.0);
            this.lastNanos = now;
            this.t += dt;
            this.tailWag += dt * 9.0;
            this.fishBob += dt * 4.0;
            // V29.2 rig pozlari: hareket fazlarinda oturma hedefi sifirlanir,
            // groom yumusakca hedefe surer; korku patlama sonrasi seller.
            if (this.phase == Phase.RUN || this.phase == Phase.LEAP || this.phase == Phase.AIR_SPIN
                || this.phase == Phase.CHASE_HOP || this.phase == Phase.HAPPY || this.phase == Phase.SPLASH
                || this.phase == Phase.POUNCE || this.phase == Phase.SNEAK_CROUCH) {
                this.groomTarget = 0;
            }
            this.groom += (this.groomTarget - this.groom) * Math.min(1.0, dt * 6.0);
            if (this.scared > 0) {
                this.scared = Math.max(0.0, this.scared - dt * 0.8);
            }
            // Kafa bakisi: hedefe dogru yumusak izleme (agir basli his).
            double aimTgt = 0;
            if (this.mobVisible) {
                aimTgt = Math.atan2((this.mobY - 40) - (this.catY - CAT_H * 0.6), (this.mobX + 14) - (this.catX + 52)) * 0.35;
            } else if (this.projectileActive) {
                aimTgt = Math.atan2(this.projectileY - (this.catY - CAT_H * 0.6), this.projectileX - (this.catX + 52)) * 0.35;
            } else if (this.variant == Variant.BUTTERFLY && !this.fishCaught) {
                aimTgt = Math.atan2(this.butterflyY - (this.catY - CAT_H * 0.6), this.butterflyX - (this.catX + 52)) * 0.35;
            } else if (!this.fishCaught) {
                aimTgt = 0.22; // suya dogru hafif asagi bakis
            }
            aimTgt = Math.max(-0.5, Math.min(0.5, aimTgt));
            this.headAim += (aimTgt - this.headAim) * Math.min(1.0, dt * 7.0);

            // V29.1 GUVENLIK: herhangi bir faz 20 sn'den uzun surerse
            // (ulumasal kilitlenme / dead-end) sahneyi zorla bitir ki
            // overlay asili kalmasin ve cift-tik korumasi butun sonraki
            // logi tiklarini yutmasin. En uzun meşru faz SNEAK'in ~11 snlik
            // surunusu oldugu icin 20 sn guvenli bir esiktir.
            if (this.t > 20.0) {
                this.phase = Phase.DONE;
            }

            Phase phaseBefore = this.phase;
            switch (this.phase) {
                case RUN -> this.stepRun(dt);
                case LEAP -> this.stepLeap(dt);
                case AIR_SPIN -> this.stepAirSpin(dt);
                case SPLASH -> this.stepSplash(dt);
                case DIVE_UNDER -> this.stepDiveUnder(dt);
                case SURFACE -> this.stepSurface(dt);
                case MISS_RECOIL -> this.stepMissRecoil(dt);
                case CHASE_HOP -> this.stepChaseHop(dt);
                case HAPPY -> this.stepHappy(dt);
                case SNEAK_CROUCH -> this.stepSneakCrouch(dt);
                case POUNCE -> this.stepPounce(dt);
                case BUTTERFLY_CHASE -> this.stepButterflyChase(dt);
                // --- V29 mob varyantlari ---
                case MOB_ENTER -> this.stepMobEnter(dt);
                case MOB_FLEE -> this.stepMobFlee(dt);
                case MOB_FIZZLE -> this.stepMobFizzle(dt);
                case MOB_EXPLODE -> this.stepMobExplode(dt);
                case MOB_PLAY -> this.stepMobPlay(dt);
                case MOB_TELEPORT -> this.stepMobTeleport(dt);
                case MOB_ARROW -> this.stepMobProjectilePhase(dt);
                case MOB_WIN -> this.stepMobWin(dt);
                case MOB_FIREBALL -> this.stepMobProjectilePhase(dt);
                case MOB_SPLIT -> this.stepMobPlay(dt);
                case MOB_BUBBLE -> this.stepMobPlay(dt);
                case MOB_SWOOP -> this.stepMobSwoop(dt);
                case MOB_SNOWBALL -> this.stepMobProjectilePhase(dt);
                case MOB_MELT -> this.stepMobMelt(dt);
                case MOB_FLAP -> this.stepMobPlay(dt);
                case MOB_EGG -> this.stepMobPlay(dt);
                case MOB_DARKEN -> this.stepMobDarken(dt);
                case MOB_GRAB -> this.stepMobGrab(dt);
                case DONE -> {
                    this.stopCatThread();
                    if (this.onFinished != null) {
                        this.onFinished.run();
                    }
                    return;
                }
            }

            java.util.Iterator<Splash> sit = this.splashes.iterator();
            while (sit.hasNext()) {
                Splash s = sit.next();
                s.update(dt);
                if (s.dead()) sit.remove();
            }
            java.util.Iterator<Ripple> rit = this.ripples.iterator();
            while (rit.hasNext()) {
                Ripple r = rit.next();
                r.update(dt);
                if (r.dead()) rit.remove();
            }
            java.util.Iterator<Debris> dit = this.debris.iterator();
            while (dit.hasNext()) {
                Debris d = dit.next();
                d.update(dt);
                if (d.dead()) dit.remove();
            }
            if (this.shakeAmp > 0.01) {
                this.shakeAmp *= Math.pow(0.02, dt); // hizli sonumleme
            }

            this.repaint();
        }

        private void stepRun(double dt) {
            // --- V29: mob varyantlari icin ozel kovalama/yaklasma kosusu ---
            if (this.variant.ordinal() >= Variant.CREEPER.ordinal()) {
                double stalkSpeed = 280;
                this.legPhase += dt * stalkSpeed * 0.10;
                double stalkX = this.waterX - CAT_W * 2.7;
                if (this.catX < stalkX) {
                    this.catX += stalkSpeed * dt;
                } else {
                    this.catX = stalkX;
                }
                // Mob sahnenin sagindan yuruyerek girer.
                if (!this.mobVisible) {
                    this.mobVisible = true;
                    this.mobX = this.sceneW + 40;
                    this.mobY = this.groundY;
                    this.mobVx = -110;
                }
                if (this.catX >= stalkX && this.t > 0.5) {
                    this.phase = Phase.MOB_ENTER;
                    this.t = 0;
                }
                return;
            }
            // SNEAK varyantinda kedi kosmaz - sinsi sinsi comak yurur.
            if (this.variant == Variant.SNEAK) {
                double sneakSpeed = 90; // yavas, gizli yaklasma
                this.sneakCrouch = Math.min(1.0, this.sneakCrouch + dt * 4.0);
                this.legPhase += dt * sneakSpeed * 0.06;
                this.catX += sneakSpeed * dt;
                if (this.catX >= this.waterX - CAT_W * 0.55) {
                    this.phase = Phase.SNEAK_CROUCH;
                    this.t = 0;
                }
                return;
            }
            // BUTTERFLY varyantinda kedi kelebegin oldugu yone kosar.
            double speed = 280; // px/sn (2x sahne olcegine uygun kosma hizi)
            this.legPhase += dt * speed * 0.10;
            double runTargetX = switch (this.variant) {
                case RAMP_JUMP -> this.rampX - CAT_W * 0.3;
                case BUTTERFLY -> this.butterflyCx - CAT_W * 1.2;
                default -> this.waterX - CAT_W * 0.55;
            };
            this.catX += speed * dt;
            if (this.catX >= runTargetX) {
                this.t = 0;
                this.leapStartX = this.catX;
                this.leapStartY = this.catY;
                if (this.variant == Variant.RAMP_JUMP) {
                    // Rampaya tirmanma - kisa bir yukselme faz-i
                    this.phase = Phase.LEAP;
                    this.leapDuration = 0.5;
                } else if (this.variant == Variant.DIVE) {
                    this.phase = Phase.LEAP;
                    this.leapDuration = 0.5;
                } else if (this.variant == Variant.BUTTERFLY) {
                    this.phase = Phase.BUTTERFLY_CHASE;
                    this.t = 0;
                } else {
                    this.phase = Phase.LEAP;
                    this.leapDuration = 0.62;
                }
            }
        }

        /** SNEAK: kedi su kenarinda comak, kuyrugu sallar, sonra pat diye ziplar. */
        private void stepSneakCrouch(double dt) {
            this.sneakCrouch = 1.0;
            this.tailWag += dt * 14.0; // heyecanli kuyruk sallama
            if (this.t > 0.9) {
                this.phase = Phase.POUNCE;
                this.t = 0;
                this.leapStartX = this.catX;
                this.leapStartY = this.groundY + 14 * this.sneakCrouch;
                this.leapDuration = 0.55;
            }
        }

        /** SNEAK->POUNCE: comak pozisyonundan yuksek bir atlayis. */
        private void stepPounce(double dt) {
            double progress = Math.min(1.0, this.t / this.leapDuration);
            double targetX = this.waterX + this.waterW * 0.35;
            this.catX = this.leapStartX + (targetX - this.leapStartX) * easeOutCubic(progress);
            double arc = Math.sin(Math.PI * progress);
            // comak pozisyonundan yukseldigi icin zemin biraz daha asagida
            this.catY = (this.groundY + 10) - arc * 130;
            if (!this.fishCaught && progress > 0.6) {
                this.fishCaught = true;
            }
            if (progress >= 1.0) {
                this.phase = Phase.SPLASH;
                this.t = 0;
                this.spawnSplash(this.catX + CAT_W * 0.4, this.groundY + 10, 30);
            }
        }

        /** BUTTERFLY: kedi ziplayarak dolasan kelebegi kovalar. */
        private void stepButterflyChase(double dt) {
            this.butterflyWing += dt * 18.0;
            // Kelebek lgnecek lphada oval bir yol cizer, hafif kacar.
            this.butterflyCx += dt * 60.0;
            this.butterflyX = this.butterflyCx + Math.cos(this.t * 2.2) * 60;
            this.butterflyY = this.butterflyCy + Math.sin(this.t * 3.1) * 34;
            // Kedi kelebegi takip eder (yumuşak takip).
            double dx = this.butterflyX - 20 - this.catX;
            this.catX += dx * Math.min(1.0, dt * 4.0);
            double targetY = Math.min(this.groundY, this.butterflyY + 30);
            this.catY += (targetY - this.catY) * Math.min(1.0, dt * 5.0);
            this.legPhase += dt * 12.0;
            if (this.t > 2.2) {
                // Son bir buyuk ziplama ile yakala
                this.fishCaught = true;
                this.phase = Phase.HAPPY;
                this.t = 0;
                this.catY = this.groundY;
            }
        }

        // =====================================================================
        // --- V29: Minecraft mob varyantlari - motor + 10 senaryo ---
        // =====================================================================

        /** Mob sahne sagindan yuruyerek girer. */
        private void stepMobEnter(double dt) {
            this.legPhase += dt * 220 * 0.10;
            // Mob yavas yavas yurur; kedi bastan sona kadar kovalar.
            if (this.mobVisible) {
                this.mobX += this.mobVx * dt;
                this.mobAnim += dt * 6.0;
            }
            // Kedi kisa bir kovalamica kosturur (ilk varyantlar icin).
            double chaseTarget = this.mobX - CAT_W * 0.9;
            if (this.catX < chaseTarget) {
                this.catX += 300 * dt;
            } else {
                this.catX += (chaseTarget - this.catX) * Math.min(1.0, dt * 6.0);
            }
            // Mob hedef noktasina ulastiginda varyantin ana eylemine gec.
            double stopX = switch (this.variant) {
                case CREEPER -> this.waterX - CAT_W * 0.15;
                case ENDERMAN -> this.waterX - CAT_W * 1.1;
                case SKELETON -> this.waterX - CAT_W * 1.4;
                case BLAZE -> this.waterX - CAT_W * 1.3;
                case SLIME -> this.waterX - CAT_W * 1.2;
                case AXOLOTL -> this.waterX + this.waterW * 0.45;
                case PARROT -> this.waterX + this.waterW * 0.5;
                case SNOW_GOLEM -> this.waterX - CAT_W * 1.2;
                case CHICKEN -> this.waterX - CAT_W * 1.0;
                default -> this.waterX - CAT_W * 1.0; // PHANTOM: yerde mob yok
            };
            boolean arrived = this.mobVisible && (this.variant == Variant.PHANTOM || Math.abs(this.mobX - stopX) < 24 || (this.mobVx < 0 && this.mobX <= stopX) || (this.mobVx > 0 && this.mobX >= stopX));
            if (this.t > 0.4 && arrived) {
                this.beginMobMain();
            }
            // Guvenlik: 6 saniyede bir sekilde ilerle (cikmazolari onler).
            if (this.t > 6.0) {
                this.beginMobMain();
            }
        }

        /** Mob ana eylemi: varyanta gore ilk faza yonlendirir. */
        private void beginMobMain() {
            this.t = 0;
            switch (this.variant) {
                case CREEPER -> {
                    // Kedi comak yurumeye devam eder - creeper fark edip kacar.
                    this.phase = Phase.MOB_FLEE;
                    this.mobVx = -260; // kacis: sahnenin soluna
                }
                case ENDERMAN -> this.phase = Phase.MOB_TELEPORT;
                case SKELETON -> {
                    this.phase = Phase.MOB_ARROW;
                    this.fireballCount = 0;
                    this.mobState = 0;
                }
                case BLAZE -> {
                    this.phase = Phase.MOB_FIREBALL;
                    this.fireballCount = 0;
                }
                case SLIME -> {
                    this.phase = Phase.MOB_FLEE;
                    this.mobVx = -170; // ziplayarak kacar
                }
                case AXOLOTL -> this.phase = Phase.MOB_BUBBLE;
                case PARROT -> {
                    this.phase = Phase.MOB_SWOOP;
                    this.mobY = this.groundY - 150;
                    this.mobVx = -170;
                    // Kedinin rampa atlayisi icin baslangic noktasi (yoksa
                    // stale degerden interpolasyon yapilirdi).
                    this.leapStartX = this.catX;
                    this.leapStartY = this.catY;
                }
                case SNOW_GOLEM -> {
                    this.phase = Phase.MOB_SNOWBALL;
                    this.fireballCount = 0;
                }
                case CHICKEN -> this.phase = Phase.MOB_FLAP;
                case PHANTOM -> this.phase = Phase.MOB_DARKEN;
                default -> this.phase = Phase.HAPPY;
            }
        }

        /** CREEPER/SLIME kacisi: mob sola dogru kosar/ziplar. */
        private void stepMobFlee(double dt) {
            this.mobAnim += dt * (this.variant == Variant.SLIME ? 8.0 : 12.0);
            this.mobX += this.mobVx * dt;
            if (this.variant == Variant.SLIME) {
                // Slime ziplayarak kacar: mobY yerden kalkar.
                double hop = Math.abs(Math.sin(this.mobAnim * 1.4));
                this.mobY = this.groundY - hop * 34;
            } else {
                this.mobY = this.groundY;
            }
            // Kedi mob'u kovalar.
            double target = this.mobX - CAT_W * 0.8;
            this.catX += (target - this.catX) * Math.min(1.0, dt * 5.0);
            this.legPhase += dt * 300 * 0.10;
            if (this.variant == Variant.CREEPER && this.mobX <= this.waterX + 30) {
                // Creeper sig suya girdi - tisilamaya baslar.
                this.phase = Phase.MOB_FIZZLE;
                this.t = 0;
                this.mobX = this.waterX + 30;
                this.mobY = this.groundY + 8; // su icinde hafif batik
                return;
            }
            if (this.variant == Variant.SLIME && this.t > 1.2) {
                // Buyuk slime ikiye bolunur.
                this.phase = Phase.MOB_SPLIT;
                this.t = 0;
                this.mobState = 0;
                return;
            }
            if (this.t > 3.0) {
                this.beginMobMain();
            }
        }

        /** CREEPER tisilama: beyaz yanip donme + duman parcaciklari, sonra PATLAMA. */
        private void stepMobFizzle(double dt) {
            this.mobAnim += dt * 30.0; // tisilama titremesi
            if (Math.random() < dt * 40.0) {
                this.spawnDebris(this.mobX + 16 + Math.random() * 20, this.mobY - 20 - Math.random() * 30, Debris.Kind.SMOKE);
            }
            // Kedi comak halinde izler (sinsi poz).
            this.sneakCrouch = 1.0;
            this.tailWag += dt * 16.0;
            if (this.t > 1.1) {
                // PATLAMA!
                this.phase = Phase.MOB_EXPLODE;
                this.t = 0;
                this.shakeAmp = 14.0;
                this.scared = 1.0; // kedi urkti: kulaklar gerildi, gozler buyudu
                this.mobVisible = false;
                for (int i = 0; i < 26; i++) {
                    this.spawnDebris(this.mobX + 20, this.mobY - 24, Debris.Kind.SMOKE);
                }
                for (int i = 0; i < 12; i++) {
                    this.spawnDebris(this.mobX + 20, this.mobY - 24, Debris.Kind.FLASH);
                }
                this.spawnSplash(this.mobX + 20, this.groundY + 10, 30);
                this.ripples.add(new Ripple(this.mobX + 20, this.groundY + 14));
                // Patlama baligi havaya firlatir.
                this.fishX = this.mobX + 30;
                this.fishCaught = false;
                this.projectileActive = true;
                this.projectileX = this.fishX;
                this.projectileY = this.groundY + 6;
                this.projectileVx = 40;
                this.projectileVy = -560;
            }
        }

        /** MOB_EXPLODE: kedi geri savrulur; balik havada, kedi onu yakalar. */
        private void stepMobExplode(double dt) {
            // Kedi patlama dalgasiyla geriye firlatilir.
            if (this.t < 0.45) {
                this.catX -= 150 * dt * (1.0 - this.t);
                this.catY = this.groundY - Math.sin(Math.min(1.0, this.t / 0.45) * Math.PI) * 60;
                this.catRotation = -this.t * 5.0;
            } else {
                this.catRotation = 0;
                this.catY = this.groundY;
            }
            // Balik balistik ucur ve tepede asagi duser.
            if (this.projectileActive) {
                this.projectileVy += 900 * dt;
                this.projectileX += this.projectileVx * dt;
                this.projectileY += this.projectileVy * dt;
                this.fishX = this.projectileX;
                if (this.t > 0.55 && this.projectileVy > 0) {
                    // Kedi havaya ziplar ve baligi yakalar.
                    this.catX = this.projectileX - CAT_W * 0.35;
                    this.catY = this.projectileY - 10;
                    this.fishCaught = true;
                    this.projectileActive = false;
                }
                if (this.projectileY > this.groundY + 20 && this.projectileVy > 0 && !this.fishCaught) {
                    this.projectileActive = false; // balik suya dustu
                    this.spawnSplash(this.projectileX, this.groundY + 10, 14);
                }
            }
            if (this.t > 1.6 && this.fishCaught) {
                this.phase = Phase.HAPPY;
                this.t = 0;
                this.catY = this.groundY;
            } else if (this.t > 2.4) {
                this.phase = Phase.HAPPY;
                this.t = 0;
            }
        }

        /** ENDERMAN isinlanma: mor parcalar, kedinin arkasinda belirir, blogu dusurur. */
        private void stepMobTeleport(double dt) {
            this.mobAnim += dt * 3.0;
            if (this.mobState == 0 && this.t > 0.7) {
                // Isinlanma! Eski konumda mor patlama, yeni konum kedinin arkasi.
                for (int i = 0; i < 18; i++) {
                    this.spawnDebris(this.mobX + 14, this.groundY - 40 - Math.random() * 60, Debris.Kind.TELEPORT);
                }
                this.mobX = Math.max(30, this.catX - CAT_W * 1.35);
                for (int i = 0; i < 14; i++) {
                    this.spawnDebris(this.mobX + 14, this.groundY - 40 - Math.random() * 60, Debris.Kind.TELEPORT);
                }
                this.mobState = 1;
                this.t = 0;
            } else if (this.mobState == 1 && this.t > 0.9) {
                // Enderman blogu dusurur.
                this.projectileActive = true;
                this.projectileX = this.mobX + 10;
                this.projectileY = this.groundY - 80;
                this.projectileVx = 30;
                this.projectileVy = 0;
                this.mobState = 2;
                this.t = 0;
            } else if (this.mobState == 2) {
                // Blog dusuyor; kedi ona uzanir.
                this.projectileVy += 700 * dt;
                this.projectileY += this.projectileVy * dt;
                this.projectileX += this.projectileVx * dt;
                if (this.projectileY >= this.groundY - 14) {
                    this.projectileY = this.groundY - 14;
                    // V29.1: blok gORUNUR KALIR - kedi onu agzina alacak
                    // (MOB_PLAY koreografisinde agiz konumuna tasinir).
                    this.projectileActive = true;
                    this.phase = Phase.MOB_PLAY;
                    this.mobState = 3;
                    this.t = 0;
                    this.spawnDebris(this.projectileX, this.groundY - 10, Debris.Kind.PUFF);
                }
            }
            // Kedi enderman'a donuk duruyor (gogus ileri, saskin).
        }

        /** Blok/yavru mob ile oynama: kedi vurmalar yapar (ENDERMAN/SLIME/CHICKEN ortak). */
        private void stepMobPlay(double dt) {
            this.legPhase += dt * 160 * 0.10;
            this.mobAnim += dt * 6.0;
            if (this.variant == Variant.SLIME) {
                // V29.1 MOB_SPLIT: iki kucuk slime ziplar; kedi her birine
                // GERCEK POUNCE ile atlar (havaya cikar, ustune iner).
                this.mobY = this.groundY - Math.abs(Math.sin(this.mobAnim * 1.5)) * 26;
                if (this.mobState == 0) {
                    this.mobX -= 110 * dt;
                    if (this.t > 0.35 && this.catY >= this.groundY - 1) {
                        // 1. pounce: havaya firla ve slime'in ustune in
                        this.catX = this.mobX - CAT_W * 0.9;
                        this.catY = this.groundY - 95;
                        this.spawnDebris(this.catX + CAT_W * 0.5, this.catY, Debris.Kind.PUFF);
                    }
                    if (this.t > 0.9) {
                        this.mobState = 1;
                        this.spawnDebris(this.mobX + 14, this.groundY - 16, Debris.Kind.PUFF);
                        this.t = 0;
                    }
                } else {
                    this.mobX -= 90 * dt;
                    if (this.t > 0.30 && this.catY >= this.groundY - 1) {
                        this.catX = this.mobX - CAT_W * 0.9;
                        this.catY = this.groundY - 85;
                        this.spawnDebris(this.catX + CAT_W * 0.5, this.catY, Debris.Kind.PUFF);
                    }
                    if (this.t > 0.8) {
                        this.fishCaught = true;
                        this.phase = Phase.HAPPY;
                        this.t = 0;
                        this.mobVisible = false;
                    }
                }
                return;
            }
            if (this.variant == Variant.AXOLOTL) {
                // V29.1: axolotl yuzer; kedi comak izler, sonra DAЛИS
                // pounce'u ile suya girer, axolotl inler; kedi balikla cikar.
                this.mobY = this.groundY + 6 + Math.sin(this.t * 3.0) * 6;
                this.mobX += Math.sin(this.t * 1.7) * 30 * dt;
                if (Math.random() < dt * 8.0) {
                    this.spawnDebris(this.mobX + 10, this.mobY - 14, Debris.Kind.BUBBLE);
                }
                this.sneakCrouch = Math.min(1.0, this.sneakCrouch + dt * 2.5);
                if (this.t > 1.5) {
                    // Dalis pounce'u: kedi suya atlar, splash + inler.
                    double p = Math.min(1.0, (this.t - 1.5) / 0.45);
                    this.catX = Math.min(this.waterX + this.waterW * 0.35, this.catX + 200 * dt);
                    this.catY = this.groundY - Math.sin(Math.PI * Math.min(1.0, p * 1.4)) * 70 + (p >= 1.0 ? 26 : 0);
                    this.catRotation = p * 1.2;
                    if (p >= 0.6 && this.t < 1.5 + 0.28) {
                        this.spawnSplash(this.catX + CAT_W * 0.4, this.groundY + 10, 10);
                    }
                    this.spawnDebris(this.catX + CAT_W * 0.5, this.catY + 20, Debris.Kind.BUBBLE);
                    if (this.t > 2.6) {
                        this.catRotation = 0;
                        this.catY = this.groundY;
                        this.fishCaught = true;
                        this.phase = Phase.HAPPY;
                        this.t = 0;
                        this.mobVisible = false;
                    }
                    return;
                }
                return;
            }
            if (this.variant == Variant.CHICKEN) {
                // MOB_EGG: yumurta suya yuvarlanir, balik merakla cikar, kedi kapar.
                if (this.mobState == 0 && this.t > 0.6) {
                    this.projectileActive = true;
                    this.projectileX = this.mobX + 10;
                    this.projectileY = this.groundY - 8;
                    this.projectileVx = 90;
                    this.projectileVy = -60;
                    this.mobState = 1;
                }
                if (this.projectileActive) {
                    this.projectileVy += 500 * dt;
                    this.projectileX += this.projectileVx * dt;
                    this.projectileY += this.projectileVy * dt;
                    if (this.projectileX > this.waterX + 20 && this.projectileY > this.groundY) {
                        this.projectileActive = false;
                        this.spawnSplash(this.projectileX, this.groundY + 10, 8);
                    }
                }
                if (this.t > 1.8) {
                    // V29.1: yumurtadan sonra kedi kuyrugunu kovalamaya
                    // baslar (MOB_WIN'deki tavuk ozel animasyonu).
                    this.phase = Phase.MOB_WIN;
                    this.t = 0;
                    this.mobState = 1;
                    this.aftermathCx = this.catX;
                }
                return;
            }
            // V29.1: ENDERMAN artik blogu itmek yerine agzina alip suya
            // tasir ve firlatir (ozel koreografi - asagida).
            this.stepMobPlayEnderman(dt);
        }

        /** ENDERMAN ozel oynama: comak yaklasir, blogu agzina alir, suya
         *  tasir, firlatir, sonra pati yalayarak temizlenir. */
        private void stepMobPlayEnderman(double dt) {
            this.tailWag += dt * 6.0;
            switch (this.mobState) {
                case 3 -> { // blogu agzina alma: comak poz + blok agiza kilitlenir
                    this.sneakCrouch = Math.min(1.0, this.sneakCrouch + dt * 5.0);
                    this.projectileX = this.catX + CAT_W * 0.95;
                    this.projectileY = this.catY - CAT_H * 0.32;
                    this.tailWag += dt * 10.0;
                    if (this.t > 0.5) {
                        this.mobState = 4;
                        this.t = 0;
                    }
                }
                case 4 -> { // blogu agzinda pounce ziplamasi
                    double p = Math.min(1.0, this.t / 0.45);
                    this.catX += 130 * dt;
                    this.catY = this.groundY - Math.sin(Math.PI * p) * 46;
                    this.sneakCrouch = Math.max(0.0, this.sneakCrouch - dt * 6.0);
                    this.projectileX = this.catX + CAT_W * 0.95;
                    this.projectileY = this.catY - CAT_H * 0.32;
                    this.legPhase += dt * 3.0;
                    if (p >= 1.0) {
                        this.mobState = 5;
                        this.t = 0;
                        this.spawnDebris(this.catX + CAT_W * 0.4, this.groundY - 4, Debris.Kind.PUFF);
                    }
                }
                case 5 -> { // blogu suya tasima (yolunur adimlar)
                    this.catX += 150 * dt;
                    this.legPhase += dt * 2.6;
                    this.projectileX = this.catX + CAT_W * 0.95;
                    this.projectileY = this.catY - CAT_H * 0.32 + Math.sin(this.t * 10) * 2;
                    if (this.catX >= this.waterX - CAT_W * 2.2) {
                        // Firlatma balistiği burada hazirlanir (state 6
                        // her karede butunleme yapar, hiz SIFIRLAMAZ).
                        this.projectileVx = 150;
                        this.projectileVy = -300;
                        this.mobState = 6;
                        this.t = 0;
                    }
                }
                case 6 -> { // blogu suya firlatma + kafa sallama
                    this.projectileVy += 700 * dt;
                    this.projectileX += this.projectileVx * dt;
                    this.projectileY += this.projectileVy * dt;
                    this.sneakCrouch = Math.min(1.0, this.sneakCrouch + dt * 4.0);
                    if (this.projectileY >= this.groundY + 4) {
                        this.projectileActive = false;
                        this.spawnSplash(this.projectileX, this.groundY + 10, 12);
                        this.ripples.add(new Ripple(this.projectileX, this.groundY + 14));
                        this.mobState = 7;
                        this.t = 0;
                    }
                }
                default -> { // 7: pati yalama + mutlu kuyruk, sonra odul
                    this.sneakCrouch = 1.0;
                    this.groomTarget = 1.0;
                    this.tailWag += dt * 18.0;
                    if (this.t > 1.1) {
                        this.fishCaught = true;
                        this.mobVisible = false;
                        this.sneakCrouch = 0;
                        this.phase = Phase.HAPPY;
                        this.t = 0;
                    }
                }
            }
        }

        /** SKELETON/BLAZE/SNOW_GOLEM mermi fazlari (ok / fireball / kartopu). */
        private void stepMobProjectilePhase(double dt) {
            this.mobAnim += dt * 4.0;
            if (!this.projectileActive) {
                if (this.t > 0.55) {
                    // Yeni mermi at (3 mermi sonra varyantin sonuc fazina gec).
                    this.fireballCount++;
                    this.projectileActive = true;
                    this.projectileX = this.mobX + 20;
                    this.projectileY = this.groundY - 46;
                    switch (this.variant) {
                        case SKELETON -> {
                            this.projectileVx = -420;
                            this.projectileVy = 30;
                        }
                        case BLAZE -> {
                            // Fireball bir kavisle ucusur (hedef: kedinin ustune dogru).
                            this.projectileVx = -230;
                            this.projectileVy = -160;
                        }
                        default -> {
                            // SNOW_GOLEM: kartopu.
                            this.projectileVx = -260;
                            this.projectileVy = -40;
                        }
                    }
                    this.t = 0;
                }
                return;
            }
            // Mermi ucusu.
            if (this.variant == Variant.BLAZE) {
                this.projectileVy += 240 * dt; // fireball kavisi
            } else if (this.variant == Variant.SKELETON) {
                this.projectileVy += 130 * dt; // okun hafif dususu
            }
            this.projectileX += this.projectileVx * dt;
            this.projectileY += this.projectileVy * dt;
            if (this.variant == Variant.BLAZE && Math.random() < dt * 30.0) {
                this.spawnDebris(this.projectileX, this.projectileY, Debris.Kind.EMBER);
            }
            // Kedi tepkisi: oklardan zıplayarak kacar, kartopunu yakalar,
            // fireball'u havada geri vurur.
            boolean dodge = this.variant == Variant.SKELETON;
            double px = this.projectileX;
            if (px <= this.catX + CAT_W && px > this.catX - 30) {
                if (dodge) {
                    // Kedi oku ziplayarak atlatir (tek seferlik ziplama).
                    if (this.catY >= this.groundY - 1 && this.projectileY > this.groundY - 60) {
                        this.catY = this.groundY - 74;
                    }
                } else if (this.variant == Variant.SNOW_GOLEM) {
                    // Kartopu yakalandi - kar puflu.
                    this.projectileActive = false;
                    this.spawnDebris(this.catX + CAT_W * 0.9, this.groundY - 30, Debris.Kind.SNOW);
                    this.spawnDebris(this.catX + CAT_W * 0.9, this.groundY - 30, Debris.Kind.SNOW);
                } else if (this.variant == Variant.BLAZE) {
                    // Fireball geri vuruldu!
                    this.projectileVx = 480;
                    this.projectileVy = -120;
                    this.catY = this.groundY - 40; // kedi de zipladi
                    this.spawnDebris(this.catX + CAT_W, this.groundY - 40, Debris.Kind.EMBER);
                }
            }
            if (this.catY < this.groundY) {
                this.catY = Math.min(this.groundY, this.catY + 480 * dt); // yere inis
            }
            // Vurulan fireball blaze'a geri doner -> blaze dumana bulanir ve
            // eriyerek kaybolur; kedi ozel BLAZE zafer animasyonuna gecer.
            if (this.variant == Variant.BLAZE && this.projectileActive && this.projectileX > this.mobX + 10 && this.projectileVx > 0) {
                this.projectileActive = false;
                this.phase = Phase.MOB_WIN;
                this.t = 0;
                this.mobState = 1;
                for (int i = 0; i < 16; i++) {
                    this.spawnDebris(this.mobX + 14, this.groundY - 50 - Math.random() * 40, Debris.Kind.SMOKE);
                }
                return;
            }
            if (this.projectileX < -60 || this.projectileX > this.sceneW + 60 || this.projectileY > this.groundY + 30) {
                this.projectileActive = false;
                if (this.projectileY > this.groundY + 25) {
                    this.spawnSplash(this.projectileX, this.groundY + 10, 6);
                }
            }
            // 3 mermiden sonra iskelet/kardan adam devrilir.
            if (this.fireballCount >= 3 && !this.projectileActive) {
                // V29.1: SKELETON/SNOW_GOLEM devrilme + ozel zafer animasyonu;
                // BLAZE artik alev topuyla eriyip MOB_WIN'e gider (asagida).
                this.phase = Phase.MOB_WIN;
                this.t = 0;
                for (int i = 0; i < 10; i++) {
                    this.spawnDebris(this.mobX + 14, this.groundY - 30 - Math.random() * 30, this.variant == Variant.SNOW_GOLEM ? Debris.Kind.SNOW : Debris.Kind.BONE);
                }
            }
        }

        /** Kazanc fazı: iskelet/kardan adam yigina doner; kedi oturur; baligi kapar. */
        /** Kazanc fazi: mob devrilir; kedi HER varyantta baska bir zafer
         *  animasyonu yapar (ortak "iter/cakilir" animasyonu kaldi).
         *  SKELETON: sagli sollu zafer dansi + oturup pati yalama.
         *  SNOW_GOLEM: kar topunun icinde yuvarlanma + silkelenme.
         *  BLAZE: ciftte zafer ziplamasi + gururlu geri yuruyus.
         *  CHICKEN: kuyruk kovalama (yerinde donus, tuyler savrulur). */
        private void stepMobWin(double dt) {
            this.mobAnim += dt * 6.0;
            if (this.mobState == 0) {
                this.mobState = 1;
                this.aftermathCx = this.catX;
            }
            // Mob ilk saniyede devrilir (hafif zipla + alcalt).
            if (this.t < 0.8 && this.mobVisible) {
                this.mobY = this.groundY - Math.max(0.0, 16 - this.t * 40);
            }
            if (this.t > 0.8) {
                this.mobVisible = false;
            }
            switch (this.variant) {
                case SKELETON -> this.stepMobWinSkeleton(dt);
                case SNOW_GOLEM -> this.stepMobWinSnowGolem(dt);
                case CHICKEN -> this.stepMobWinChicken(dt);
                default -> this.stepMobWinBlaze(dt);
            }
        }

        /** SKELETON zaferi: sagli sollu salinma, sonra oturup temizlenme. */
        private void stepMobWinSkeleton(double dt) {
            if (this.t < 1.0) {
                this.catX = this.aftermathCx + Math.sin(this.t * 9.0) * 34;
                this.legPhase += dt * 5.0;
                this.tailWag += dt * 14.0;
            } else if (this.t < 2.2) {
                this.sneakCrouch = Math.min(1.0, this.sneakCrouch + dt * 5.0);
                this.tailWag += dt * 20.0;
            } else {
                this.fishCaught = true;
                this.sneakCrouch = 0;
                this.groomTarget = 1.0; // oturup patisini yalar
                this.phase = Phase.HAPPY;
                this.t = 0;
            }
        }

        /** SNOW_GOLEM zaferi: karda tam bir yuvarlanma + silkelenme. */
        private void stepMobWinSnowGolem(double dt) {
            if (this.t < 1.3) {
                this.catRotation = this.t * 4.8;
                this.catY = this.groundY - Math.abs(Math.sin(this.t * 4.8)) * 10;
                if (Math.random() < dt * 14.0) {
                    this.spawnDebris(this.catX + CAT_W * 0.5, this.catY - CAT_H * 0.5, Debris.Kind.SNOW);
                }
            } else if (this.t < 2.1) {
                this.catRotation = 0;
                this.catY = this.groundY;
                this.catX = this.aftermathCx + Math.sin(this.t * 60.0) * 5;
            } else {
                this.catRotation = 0;
                this.catX = this.aftermathCx;
                this.fishCaught = true;
                this.phase = Phase.HAPPY;
                this.t = 0;
            }
        }

        /** CHICKEN zaferi: kuyruk kovalama - yerinde doner, tuy savrulur. */
        private void stepMobWinChicken(double dt) {
            if (this.t < 1.4) {
                this.catRotation = this.t * 7.0;
                this.legPhase += dt * 6.0;
                if (Math.random() < dt * 6.0) {
                    this.spawnDebris(this.catX + CAT_W * 0.2, this.groundY - 30 - Math.random() * 20, Debris.Kind.FEATHER);
                }
            } else {
                this.catRotation = 0;
                this.fishCaught = true;
                this.mobVisible = false;
                this.phase = Phase.HAPPY;
                this.t = 0;
            }
        }

        /** BLAZE zaferi: ciftte zafer ziplamasi + gururlu geri yuruyus. */
        private void stepMobWinBlaze(double dt) {
            if (this.t < 1.2) {
                this.catY = this.groundY - Math.abs(Math.sin(this.t * 9.0)) * 40;
                this.legPhase += dt * 4.0;
                if (Math.random() < dt * 8.0) {
                    this.spawnDebris(this.catX + CAT_W * 0.6, this.catY, Debris.Kind.EMBER);
                }
            } else if (this.t < 2.0) {
                this.catY = this.groundY;
                this.catX -= 130 * dt;
                this.legPhase += dt * 2.4;
            } else {
                this.fishCaught = true;
                this.phase = Phase.HAPPY;
                this.t = 0;
            }
        }

        /** PARROT: papaogan suzulur; kedi rampadan atlar, kacirir, balikla iner. */
        private void stepMobSwoop(double dt) {
            // PHANTOM: hayalet once tepede suzulur, sonra dalis fazina gecer.
            if (this.variant == Variant.PHANTOM) {
                this.mobAnim += dt * 14.0;
                this.mobX -= 60 * dt;
                this.mobY = this.groundY - 150;
                if (this.t > 1.1) {
                    this.phase = Phase.MOB_GRAB;
                    this.t = 0;
                }
                return;
            }
            this.mobAnim += dt * 22.0; // kanat cispma
            this.mobX += this.mobVx * dt;
            this.mobY = this.groundY - 60 - Math.sin(this.t * 4.0) * 26;
            if (this.mobX < this.waterX - CAT_W) {
                // Papaogan usulce yukselip sahnedan cikar.
                this.mobY -= (this.t * 140) * dt * 10;
                if (this.mobY < -80) {
                    this.mobVisible = false;
                }
            }
            // Kedi rampadan atlar (RAMP_JUMP benzeri ama tek atlayis).
            // V29.1 FIX: inis gecisi daha once "if (this.t < leapDur)"
            // blogunun ICINDEYDI - progress 1.0'a ulastiginda kosul zaten
            // false oldugu icin bu dal HIC CALISMAZ ve PARROT sonsuza
            // kadar MOB_SWOOP'ta takili kalirdi. Timer calismaya devam
            // ettigi icin cift-tik korumasi da butun sonraki tiklari
            // yutuyordu (kullanicinin "sadece kardan adam geliyor" raporunun
            // asil nedeni buydu). Inis artik progress >= 1.0 oldugunda
            // tam olarak bir kez tetikleniyor.
            double leapDur = 0.7;
            double progress = Math.min(1.0, this.t / leapDur);
            if (progress < 1.0) {
                this.catX = this.leapStartX + (this.waterX + this.waterW * 0.4 - this.leapStartX) * easeOutCubic(progress);
                double arc = Math.sin(Math.PI * progress);
                this.catY = this.groundY - arc * 170;
                this.catRotation = progress * Math.PI * 2;
            } else if (!this.fishCaught) {
                this.catRotation = 0;
                this.catX = this.waterX + this.waterW * 0.4;
                this.catY = this.groundY;
                this.phase = Phase.SPLASH;
                this.t = 0;
                this.spawnSplash(this.catX + CAT_W * 0.4, this.groundY + 10, 26);
                this.ripples.add(new Ripple(this.catX + CAT_W * 0.4, this.groundY + 14));
                // Papaogan kacti ama balik sasirdi - kedi onu kapar.
                this.fishCaught = true;
            }
        }

        /** SNOW_GOLEM erime: kardan adam alcalir, su birikintisi birakir. */
        private void stepMobMelt(double dt) {
            this.mobAnim += dt * 2.0;
            this.mobState = Math.min(100, this.mobState + (int)(dt * 70)); // erime yuzdesi
            if (Math.random() < dt * 10.0) {
                this.spawnDebris(this.mobX + 10 + Math.random() * 24, this.groundY - 40 - Math.random() * 30, Debris.Kind.SNOW);
            }
            if (this.mobState >= 100) {
                this.mobVisible = false;
                this.fishCaught = true;
                this.phase = Phase.HAPPY;
                this.t = 0;
            }
        }

        /** PHANTOM: gokyuzu kararir, hayalet ustumuzde daireler cizer. */
        private void stepMobDarken(double dt) {
            this.skyDarken = Math.min(0.55, this.skyDarken + dt * 0.5);
            if (this.t > 0.8) {
                this.phase = Phase.MOB_SWOOP;
                this.t = 0;
                this.mobVisible = true;
                this.mobX = this.waterX + this.waterW * 0.5;
                this.mobY = this.groundY - 200;
            }
        }

        /** PHANTOM kuyruktan yakalama: hayalet dalis yapar, kedi havada kapar. */
        private void stepMobGrab(double dt) {
            this.mobAnim += dt * 20.0;
            this.mobX -= 240 * dt;
            this.mobY += 150 * dt;
            double leapDur = 0.5;
            double progress = Math.min(1.0, this.t / leapDur);
            this.catX += 260 * dt;
            this.catY = this.groundY - Math.sin(Math.PI * progress) * 150;
            if (progress >= 1.0 && !this.fishCaught) {
                // Yakalama!
                this.fishCaught = true;
                this.mobVisible = false;
                this.spawnDebris(this.catX + CAT_W * 0.8, this.catY, Debris.Kind.TELEPORT);
                this.spawnDebris(this.catX + CAT_W * 0.8, this.catY, Debris.Kind.TELEPORT);
            }
            // V29.1: yakalama aninda hayalet TUYLERE AYRILIR (dispersal),
            // kedi bir tumersel atlayisla yerde oturur.
            if (this.t > leapDur && !this.fishCaught) {
                this.fishCaught = true;
                this.mobVisible = false;
                for (int i = 0; i < 22; i++) {
                    this.spawnDebris(this.catX + CAT_W * 0.5 + (Math.random() - 0.5) * 60, this.catY - 20 - Math.random() * 70, Debris.Kind.TELEPORT);
                }
            }
            if (this.catY >= this.groundY - 2 && this.t > leapDur) {
                this.catY = this.groundY;
                this.sneakCrouch = 1.0; // yerde oturur, kuyruk sarilir
                this.groomTarget = 1.0;
                this.phase = Phase.HAPPY;
                this.t = 0;
            }
        }

        /** Debris parcacigi uretir (tur bazli hiz/omur ayarlar). */
        private void spawnDebris(double x, double y, Debris.Kind kind) {
            java.util.Random rnd = new java.util.Random();
            double vx = (rnd.nextDouble() - 0.5) * 160;
            double vy = -60 - rnd.nextDouble() * 120;
            switch (kind) {
                case EMBER -> vy = -80 - rnd.nextDouble() * 140;
                case BUBBLE -> vy = -40 - rnd.nextDouble() * 60;
                case TELEPORT -> {
                    vx = (rnd.nextDouble() - 0.5) * 220;
                    vy = (rnd.nextDouble() - 0.5) * 220;
                }
                case FLASH -> {
                    vx = (rnd.nextDouble() - 0.5) * 320;
                    vy = (rnd.nextDouble() - 0.5) * 320;
                }
                case FEATHER -> {
                    vx = (rnd.nextDouble() - 0.5) * 120;
                    vy = -30 - rnd.nextDouble() * 60;
                }
                default -> {
                }
            }
            this.debris.add(new Debris(x, y, vx, vy, kind));
        }

        private void stepLeap(double dt) {
            this.legPhase += dt * 260 * 0.10;
            double progress = Math.min(1.0, this.t / this.leapDuration);
            double targetX = this.waterX + waterW * 0.30;
            switch (this.variant) {
                case RAMP_JUMP -> {
                    // Rampaya tirmanip tepeden havaya firlar
                    this.catX = this.leapStartX + (this.rampX + CAT_W * 0.2 - this.leapStartX) * easeOutCubic(progress);
                    this.catY = this.leapStartY - (this.leapStartY - this.rampTopY) * easeOutCubic(progress);
                    if (progress >= 1.0) {
                        this.phase = Phase.AIR_SPIN;
                        this.t = 0;
                        this.leapStartX = this.catX;
                        this.leapStartY = this.catY;
                    }
                }
                case DIVE -> {
                    this.catX = this.leapStartX + (this.waterX + waterW * 0.5 - CAT_W * 0.5 - this.leapStartX) * easeOutCubic(progress);
                    double arc = Math.sin(Math.PI * progress);
                    this.catY = this.groundY - arc * 60;
                    if (progress >= 1.0) {
                        this.phase = Phase.DIVE_UNDER;
                        this.t = 0;
                        this.spawnSplash(this.catX + CAT_W * 0.4, this.groundY + 10, 20);
                        this.ripples.add(new Ripple(this.catX + CAT_W * 0.4, this.groundY + 14));
                    }
                }
                default -> {
                    this.catX = this.leapStartX + (targetX - this.leapStartX) * easeOutCubic(progress);
                    double arc = Math.sin(Math.PI * progress);
                    this.catY = this.groundY - arc * 150; // 2x olcek icin yay yuksekligi arttirildi
                    boolean shouldMiss = this.variant == Variant.DOUBLE_LEAP && this.hopCount == 0;
                    if (!this.fishCaught && progress > 0.55 && !shouldMiss) {
                        this.fishCaught = true;
                    }
                    if (progress >= 1.0) {
                        if (shouldMiss) {
                            this.hopCount++;
                            this.phase = Phase.MISS_RECOIL;
                            this.t = 0;
                            this.spawnSplash(this.catX + CAT_W * 0.4, this.groundY + 10, 10);
                        } else if (this.variant == Variant.CHASE && this.hopCount < this.maxHops) {
                            this.hopCount++;
                            this.phase = Phase.CHASE_HOP;
                            this.t = 0;
                            this.fishX += 70; // balik kacip biraz ileri gidiyor
                        } else {
                            this.phase = Phase.SPLASH;
                            this.t = 0;
                            this.spawnSplash(this.catX + CAT_W * 0.4, this.groundY + 10, 26);
                        }
                    }
                }
            }
        }

        private void stepAirSpin(double dt) {
            // BACKFLIP varyanti: SPLASH sonrasi yerinde geri takla atar.
            if (this.variant == Variant.BACKFLIP) {
                double spinDuration = 0.6;
                double progress = Math.min(1.0, this.t / spinDuration);
                this.backflipRotation = progress * Math.PI * 2;
                this.catRotation = -this.backflipRotation; // geri yone takla
                this.catY = this.groundY - Math.sin(Math.PI * progress) * 46;
                if (progress >= 1.0) {
                    this.catRotation = 0;
                    this.backflipRotation = 0;
                    this.phase = Phase.HAPPY;
                    this.t = 0;
                }
                return;
            }
            // Rampadan atlama varyantinda: havada tam bir takla (360 derece) atar
            double spinDuration = 0.55;
            double progress = Math.min(1.0, this.t / spinDuration);
            this.catRotation = progress * Math.PI * 2;
            this.catX = this.leapStartX + (this.waterX + waterW * 0.30 - this.leapStartX) * progress;
            double arc = Math.sin(Math.PI * progress);
            this.catY = Math.min(this.leapStartY, this.groundY - 40) - arc * 70;
            if (!this.fishCaught && progress > 0.6) {
                this.fishCaught = true;
            }
            if (progress >= 1.0) {
                this.catRotation = 0;
                this.phase = Phase.SPLASH;
                this.t = 0;
                this.spawnSplash(this.catX + CAT_W * 0.4, this.groundY + 10, 26);
            }
        }

        private void stepSplash(double dt) {
            double settle = Math.min(1.0, this.t / 0.35);
            this.catY = this.groundY - Math.max(0, (1.0 - settle)) * 18 * Math.cos(settle * Math.PI);
            if (this.t > 0.15 && this.ripples.isEmpty()) {
                this.ripples.add(new Ripple(this.catX + CAT_W * 0.4, this.groundY + 14));
            }
            if (settle >= 1.0) {
                this.catY = this.groundY;
                // BACKFLIP varyanti: baligi kaptiktan sonra yerinde bir geri
                // takla atip oyle kosarak kacar (yeni varyant).
                if (this.variant == Variant.BACKFLIP) {
                    this.phase = Phase.AIR_SPIN;
                    this.t = 0;
                    this.leapStartX = this.catX;
                    this.leapStartY = this.catY;
                    return;
                }
                this.phase = Phase.HAPPY;
                this.t = 0;
            }
        }

        private void stepDiveUnder(double dt) {
            // Kedi kisa bir sure suyun altinda "kayboluyor"
            double duration = 0.5;
            double progress = Math.min(1.0, this.t / duration);
            if (progress >= 1.0) {
                this.phase = Phase.SURFACE;
                this.t = 0;
                this.fishCaught = true;
                this.spawnSplash(this.catX + CAT_W * 0.4, this.groundY + 10, 22);
            }
        }

        private void stepSurface(double dt) {
            double duration = 0.4;
            double progress = Math.min(1.0, this.t / duration);
            this.catY = this.groundY + 14 - 14 * easeOutCubic(progress);
            if (progress >= 1.0) {
                this.catY = this.groundY;
                this.phase = Phase.HAPPY;
                this.t = 0;
            }
        }

        private void stepMissRecoil(double dt) {
            // Baligi kacirdi - kisa bir "sasirma" molasi, sonra tekrar kosup ziplayacak
            double duration = 0.45;
            double progress = Math.min(1.0, this.t / duration);
            this.catY = this.groundY - Math.max(0, 1.0 - progress) * 8;
            if (progress >= 1.0) {
                this.catY = this.groundY;
                this.phase = Phase.RUN;
                this.t = 0;
                this.catX -= 30; // biraz geri cekilip tekrar hamle yapar
            }
        }

        private void stepChaseHop(double dt) {
            double duration = 0.4;
            double progress = Math.min(1.0, this.t / duration);
            double startX = this.catX;
            double targetX = this.fishX - CAT_W * 0.35;
            this.catX = startX + (targetX - startX) * progress;
            double arc = Math.sin(Math.PI * progress);
            this.catY = this.groundY - arc * 50;
            if (progress >= 1.0) {
                this.catY = this.groundY;
                this.phase = Phase.LEAP;
                this.t = 0;
                this.leapStartX = this.catX;
                this.leapStartY = this.catY;
                this.leapDuration = 0.5;
            }
        }

        private void stepHappy(double dt) {
            this.legPhase += dt * 140 * 0.10;
            this.catX += 130 * dt;
            if (this.catX > this.sceneW + CAT_W) {
                this.phase = Phase.DONE;
            }
        }

        private void spawnSplash(double x, double y, int count) {
            java.util.Random rnd = new java.util.Random();
            for (int i = 0; i < count; i++) {
                double angle = Math.toRadians(200 + rnd.nextDouble() * 140); // yukari yelpaze
                double speed = 120 + rnd.nextDouble() * 190; // 2x olcek icin daha guclu sicrama
                this.splashes.add(new Splash(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed));
            }
        }

        private static double easeOutCubic(double x) {
            double f = x - 1;
            return f * f * f + 1;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            // V39.4 PIN MODU: sahne ogeleri (su, balik, rampa) cizilmez;
            // sadece sprite sabit konumunda durur.
            if (this.pinMode && this.customSprite != null) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                paintCat(g, this.catX, this.catY, 0, 0, Phase.DONE, false, 0, 0, this.variant, 0, 0, 0);
                g.dispose();
                return;
            }

            Color waterColor = Theme.CYAN != null ? Theme.CYAN : new Color(80, 190, 220);
            Color waterDeep = waterColor.darker();

            // V29: sahnenin tamamini sarsinti offsetiyle kaydir (CREEPER patlamasi).
            if (this.shakeAmp > 0.01) {
                double sx = (Math.random() - 0.5) * 2 * this.shakeAmp;
                double sy = (Math.random() - 0.5) * 2 * this.shakeAmp;
                g.translate(sx, sy);
            }

            // V29: PHANTOM varyanti - gokyuzu kararmasi (tum sahnenin ustune).
            if (this.skyDarken > 0.01) {
                g.setColor(new Color(10, 6, 24, (int) (this.skyDarken * 255)));
                g.fillRect(-20, -20, this.sceneW + 40, this.sceneH + 40);
            }

            // --- Rampa (sadece RAMP_JUMP varyantinda) ---
            if (this.variant == Variant.RAMP_JUMP) {
                Color plank = new Color(120, 84, 50);
                int[] rxs = {this.rampX - 10, this.rampX + 90, this.rampX + 90};
                int[] rys = {this.groundY + 10, this.groundY + 10, this.rampTopY};
                g.setColor(plank);
                g.fillPolygon(rxs, rys, 3);
                g.setColor(plank.darker());
                g.drawPolygon(rxs, rys, 3);
            }

            // --- Su birikintisi / mini golet (2x buyutuldu) ---
            g.setColor(new Color(waterDeep.getRed(), waterDeep.getGreen(), waterDeep.getBlue(), 140));
            g.fillRoundRect(this.waterX, this.groundY + 8, this.waterW, 44, 30, 30);
            g.setColor(new Color(waterColor.getRed(), waterColor.getGreen(), waterColor.getBlue(), 90));
            for (int i = 0; i < 3; i++) {
                double wave = Math.sin(this.fishBob + i * 1.3) * 5;
                g.drawLine(this.waterX + 16, (int)(this.groundY + 20 + i * 10 + wave), this.waterX + this.waterW - 16, (int)(this.groundY + 20 + i * 10 - wave));
            }

            // --- Dalgalanma halkalari (splash sonrasi) ---
            for (Ripple r : this.ripples) {
                r.paint(g);
            }

            // --- Balik (kedi tarafindan yakalanana/dalinana kadar suda beklar/siçrar) ---
            boolean fishVisible = !this.fishCaught && this.phase != Phase.DIVE_UNDER && this.variant != Variant.BUTTERFLY;
            if (fishVisible) {
                double fx = (this.variant == Variant.CHASE ? this.fishX : this.waterX + this.waterW * 0.5 - FISH_W / 2.0);
                double fy = this.groundY + 6 + Math.sin(this.fishBob * 1.6) * 8;
                paintFish(g, fx, fy, Math.sin(this.fishBob * 1.6), 1.0);
            }

            // --- Kelebek (BUTTERFLY varyanti) ---
            if (this.variant == Variant.BUTTERFLY && !this.fishCaught) {
                paintButterfly(g, this.butterflyX, this.butterflyY, this.butterflyWing);
            }

            // --- V29: Mob cizimi (kedinin arkasinda, baliktan sonra) ---
            if (this.mobVisible) {
                paintMob(g, this.variant, this.mobX, this.mobY, this.mobAnim, this.mobState);
            }
            // --- V29: aktif mermi (ok / fireball / kartopu / blog / yumurta) ---
            if (this.projectileActive) {
                paintProjectile(g, this.variant, this.projectileX, this.projectileY);
            }
            // --- V29: PHANTOM kanatli hayalet (mobX/mobY ile hareket eder) ---
            if (this.variant == Variant.PHANTOM && this.mobVisible) {
                paintPhantom(g, this.mobX, this.mobY, this.mobAnim);
            }

            // --- Kedinin golgesi (derinlik hissi icin) ---
            // ONEMLI DUZELTME: onceki surumde golge groundY + CAT_H
            // konumuna (zeminin ~40-80px altina) ciziliyordu - bu yuzden
            // golge cok asagida gorunuyordu. Golge daima zemin
            // hizasinda (groundY) kalmali, sadece zipladikca (catY
            // yukseldikce) incelip solmali.
            double airborne = Math.max(0, this.groundY - this.catY);
            double shadowScale = Math.max(0.35, 1.0 - airborne / 200.0);
            int shadowAlpha = (int) (70 * shadowScale);
            g.setColor(new Color(0, 0, 0, Math.max(20, shadowAlpha)));
            int shW = (int) (CAT_W * 0.55 * shadowScale);
            int shH = (int) (14 * shadowScale);
            g.fillOval((int) (this.catX + CAT_W * 0.28 - (shW - CAT_W * 0.55 * 0.55) / 2), this.groundY + 4, shW, Math.max(4, shH));

            // --- Su sicramasi parcaciklari + V29 debris (duman, kor, tuy...) ---
            for (Splash s : this.splashes) {
                s.paint(g, waterColor);
            }
            for (Debris d : this.debris) {
                d.paint(g);
            }

            // --- Kedi (bazi varyantlarda takla/rotasyon uygulanir) ---
            // V29: balik her varyantin odulu degil (enderman blogu, slime,
            // kemik vb.) - odul balik olan varyantlarda agizda tasinir.
            boolean fishPrize = this.variant != Variant.ENDERMAN && this.variant != Variant.SLIME
                && this.variant != Variant.SKELETON && this.variant != Variant.SNOW_GOLEM && this.variant != Variant.PHANTOM;
            boolean carryingFish = this.fishCaught && fishPrize && this.phase != Phase.DONE && this.phase != Phase.DIVE_UNDER && this.variant != Variant.BUTTERFLY;
            boolean carryingButterfly = this.variant == Variant.BUTTERFLY && this.fishCaught && this.phase == Phase.HAPPY;
            boolean submerged = this.phase == Phase.DIVE_UNDER;
            if (!submerged) {
                // V29.2: squash & stretch - atlarken boyuna uzar, yere
                // inerken peser; rig bunu torba sekline uygular.
                double poseStretch = 0;
                switch (this.phase) {
                    case LEAP -> poseStretch = Math.sin(Math.min(1.0, this.t / Math.max(0.2, this.leapDuration)) * Math.PI) * 0.16;
                    case AIR_SPIN, CHASE_HOP, POUNCE -> poseStretch = 0.12;
                    case SPLASH -> poseStretch = -0.18 * (1.0 - Math.min(1.0, this.t / 0.3));
                    case MOB_EXPLODE -> poseStretch = 0.15;
                    default -> poseStretch = 0;
                }
                paintCat(g, this.catX, this.catY, this.legPhase, this.tailWag, this.phase, carryingFish || carryingButterfly, this.catRotation, this.sneakCrouch, this.variant, this.groom, this.scared, poseStretch);
            } else {
                // Su altindayken sadece hafif bir siluet/balonculuk hissi verelim
                g.setColor(new Color(waterDeep.getRed(), waterDeep.getGreen(), waterDeep.getBlue(), 90));
                g.fillOval((int) (this.catX + CAT_W * 0.15), (int) (this.groundY - 10), (int) (CAT_W * 0.7), 24);
            }

            g.dispose();
        }

        /** BUTTERFLY varyanti icin kucuk, kanat cirpan bir kelebek cizer. */
        private static void paintButterfly(Graphics2D g, double x, double y, double wingPhase) {
            Graphics2D bg = (Graphics2D) g.create();
            bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            bg.translate(x, y);
            double flap = Math.abs(Math.sin(wingPhase));
            // Sol kanat
            bg.setColor(new Color(255, 190, 70));
            bg.fillOval((int)(-14 - 4 * flap), -12, 14, 20);
            // Sag kanat
            bg.fillOval((int)(0 + 4 * flap), -12, 14, 20);
            // Kanat desenleri
            bg.setColor(new Color(200, 120, 20));
            bg.fillOval((int)(-11 - 3 * flap), -8, 7, 10);
            bg.fillOval((int)(4 + 3 * flap), -8, 7, 10);
            // Govde
            bg.setColor(new Color(70, 45, 20));
            bg.fillRoundRect(-2, -10, 5, 22, 4, 4);
            // Antenler
            bg.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            bg.drawLine(-1, -10, -5, -17);
            bg.drawLine(3, -10, 7, -17);
            bg.dispose();
        }

        private static void paintFish(Graphics2D g, double x, double y, double wag, double scale) {
            Graphics2D fg = (Graphics2D) g.create();
            fg.translate(x, y);
            fg.scale(scale, scale);
            Color fishColor = Theme.CYAN != null ? Theme.CYAN : new Color(80, 190, 220);
            fg.setColor(fishColor);
            fg.fillOval(8, 0, FISH_W - 20, FISH_H);
            double tailSwing = wag * 14;
            int[] xs = {FISH_W - 24, FISH_W - 4, FISH_W - 4};
            int[] ys = {FISH_H / 2, (int)(FISH_H / 2 - 16 + tailSwing), (int)(FISH_H / 2 + 16 + tailSwing)};
            fg.fillPolygon(xs, ys, 3);
            fg.setColor(fishColor.brighter());
            fg.fillOval(14, FISH_H / 2 - 6, FISH_W / 3, FISH_H / 3);
            fg.setColor(Theme.BG_BASE);
            fg.fillOval(16, FISH_H / 2 - 6, 6, 6);
            fg.dispose();
        }

        /** Uzun boylu, ince enderman; mor gozler, cim blogu tasir. */
        private static void paintEnderman(Graphics2D g, double x, double y, double anim) {
            Graphics2D cg = (Graphics2D) g.create();
            cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            cg.translate(x, y);
            Color skin = new Color(16, 12, 22);
            cg.setColor(skin);
            // Bacaklar
            cg.fillRect(10, -34, 8, 34);
            cg.fillRect(30, -34, 8, 34);
            // Govde
            cg.fillRect(8, -74, 32, 40);
            // Kollar
            cg.fillRect(-2, -72, 7, 30);
            cg.fillRect(43, -72, 7, 30);
            // Kafa
            cg.fillRect(6, -100, 36, 26);
            // Mor gozler
            cg.setColor(new Color(190, 120, 250));
            cg.fillRect(10, -93, 11, 7);
            cg.fillRect(27, -93, 11, 7);
            cg.setColor(new Color(240, 220, 255, 220));
            cg.fillRect(14, -91, 4, 3);
            cg.fillRect(31, -91, 4, 3);
            // Cim blogu (elde)
            cg.setColor(new Color(96, 160, 60));
            cg.fillRect(46, -76, 22, 12);
            cg.setColor(new Color(70, 120, 40));
            cg.fillRect(46, -66, 22, 3);
            cg.setColor(new Color(120, 85, 55));
            cg.fillRect(46, -63, 22, 2);
            cg.dispose();
        }

        /** Kemik rengi iskelet; yay cizer. */
        private static void paintSkeleton(Graphics2D g, double x, double y, double anim) {
            Graphics2D cg = (Graphics2D) g.create();
            cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double sway = Math.sin(anim) * 2;
            cg.translate(x, y + sway * 0.3);
            Color bone = new Color(214, 208, 190);
            Color boneDark = new Color(170, 163, 145);
            cg.setColor(bone);
            cg.fillRect(14, -32, 6, 32);
            cg.fillRect(30, -32, 6, 32);
            cg.fillRect(12, -66, 26, 10);
            for (int i = 0; i < 3; i++) {
                cg.fillRect(10, -60 + i * 8, 30, 3);
            }
            cg.fillRect(2, -64, 6, 26);
            cg.fillRect(42, -64, 6, 26);
            cg.setColor(boneDark);
            cg.setStroke(new BasicStroke(2.5f));
            cg.drawArc(44, -70, 20, 34, -60, 120);
            cg.setColor(bone);
            cg.fillRect(10, -94, 30, 26);
            cg.setColor(new Color(40, 38, 34));
            cg.fillRect(15, -88, 7, 7);
            cg.fillRect(28, -88, 7, 7);
            cg.fillRect(18, -76, 14, 3);
            cg.dispose();
        }

        /** Alev ipleriyle donen yuzen blaze. */
        private static void paintBlaze(Graphics2D g, double x, double y, double anim) {
            Graphics2D cg = (Graphics2D) g.create();
            cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double hover = Math.sin(anim * 0.8) * 8;
            cg.translate(x, y - 60 + hover);
            for (int i = 0; i < 4; i++) {
                double a = anim * 1.2 + i * Math.PI / 2;
                int rx = (int) (Math.cos(a) * 34) - 5;
                int ry = (int) (Math.sin(a) * 16) - 14;
                cg.setColor(new Color(255, 140 + (int) (60 * Math.sin(a)), 30));
                cg.fillRoundRect(rx, ry, 10, 28, 5, 5);
                cg.setColor(new Color(255, 220, 120, 160));
                cg.fillRoundRect(rx + 2, ry + 4, 6, 14, 4, 4);
            }
            cg.setColor(new Color(250, 200, 60));
            cg.fillRect(-14, -22, 28, 34);
            cg.setColor(new Color(90, 50, 10));
            cg.fillRect(-9, -16, 6, 6);
            cg.fillRect(4, -16, 6, 6);
            cg.fillRect(-6, -4, 13, 3);
            cg.dispose();
        }

        /** Yari saydam ziplayan slime (state 1: bolunmus kucuk). */
        private static void paintSlime(Graphics2D g, double x, double y, double anim, int state) {
            Graphics2D cg = (Graphics2D) g.create();
            cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = state == 0 ? 44 : 30;
            double squash = 1.0 + Math.sin(anim * 1.4) * 0.16;
            int w = size, h = (int) (size / squash);
            cg.translate(x, y);
            cg.setColor(new Color(110, 220, 90, 170));
            cg.fillRoundRect(-w / 2, -h, w, h, 12, 12);
            cg.setColor(new Color(70, 170, 55, 200));
            cg.drawRoundRect(-w / 2, -h, w, h, 12, 12);
            cg.setColor(new Color(50, 130, 40, 220));
            cg.fillRoundRect(-w / 4, -h + h / 4, w / 2, h / 2, 8, 8);
            cg.setColor(new Color(20, 50, 15));
            cg.fillRect(-w / 4, -h + h / 3, 4, 4);
            cg.fillRect(w / 4 - 4, -h + h / 3, 4, 4);
            cg.fillRect(-3, -h / 3, 6, 3);
            cg.dispose();
        }

        /** Pembe axolotl; solungac dallari sallanir. */
        private static void paintAxolotl(Graphics2D g, double x, double y, double anim) {
            Graphics2D cg = (Graphics2D) g.create();
            cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double bob = Math.sin(anim) * 4;
            cg.translate(x, y + bob);
            Color body = new Color(240, 160, 200);
            cg.setColor(body.darker());
            int[] tx = {-8, -30, -8};
            int[] ty = {-10, -22 + (int) (Math.sin(anim * 2) * 6), -2};
            cg.fillPolygon(tx, ty, 3);
            cg.setColor(body);
            cg.fillRoundRect(-10, -18, 44, 18, 14, 14);
            cg.setColor(new Color(220, 90, 150));
            for (int i = 0; i < 3; i++) {
                int bx = 2 + i * 8;
                int sway = (int) (Math.sin(anim * 3 + i) * 3);
                cg.fillRoundRect(bx, -28 + sway, 4, 12, 3, 3);
                cg.fillRoundRect(bx, -28 - sway, 4, 12, 3, 3);
            }
            cg.setColor(new Color(30, 25, 30));
            cg.fillOval(20, -14, 4, 4);
            cg.fillOval(30, -14, 4, 4);
            cg.setColor(new Color(255, 200, 220));
            cg.fillArc(21, -8, 9, 7, 0, 180);
            cg.dispose();
        }

        /** Kirmizi-mavi papaogan; kanat cispma animasyonu. */
        private static void paintParrot(Graphics2D g, double x, double y, double anim) {
            Graphics2D cg = (Graphics2D) g.create();
            cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double flap = Math.sin(anim);
            cg.translate(x, y);
            cg.setColor(new Color(40, 110, 220));
            int wingY = (int) (-16 - flap * 10);
            cg.fillPolygon(new int[]{-6, -26, -2}, new int[]{wingY, wingY - 12 + (int) (flap * 6), -6}, 3);
            cg.setColor(new Color(220, 60, 50));
            cg.fillRoundRect(-12, -18, 26, 20, 12, 12);
            cg.fillOval(6, -26, 14, 14);
            cg.setColor(new Color(240, 190, 60));
            cg.fillPolygon(new int[]{19, 28, 19}, new int[]{-22, -19, -16}, 3);
            cg.setColor(Color.WHITE);
            cg.fillOval(11, -24, 3, 3);
            cg.setColor(new Color(30, 90, 190));
            cg.fillPolygon(new int[]{-10, -30, -8}, new int[]{-4, 6 + (int) (flap * 4), 2}, 3);
            cg.dispose();
        }

        /** Iki katli kartopu kardan adam; meltPct ile erir. */
        private static void paintSnowGolem(Graphics2D g, double x, double y, double anim, int meltPct) {
            Graphics2D cg = (Graphics2D) g.create();
            cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double melt = meltPct / 100.0;
            double shrink = 1.0 - melt * 0.75;
            double wobble = Math.sin(anim) * 2;
            cg.translate(x + 20, y);
            cg.scale(shrink, shrink);
            cg.setColor(new Color(245, 250, 255));
            cg.fillOval(-18, -30 + (int) wobble, 36, 30);
            cg.fillOval(-12, -54 + (int) wobble, 24, 26);
            cg.setColor(new Color(30, 30, 30));
            cg.fillOval(-6, -46 + (int) wobble, 3, 3);
            cg.fillOval(4, -46 + (int) wobble, 3, 3);
            cg.setColor(new Color(240, 140, 40));
            cg.fillPolygon(new int[]{0, 14, 0}, new int[]{-40 + (int) wobble, -37 + (int) wobble, -35 + (int) wobble}, 3);
            cg.setColor(new Color(110, 80, 45));
            cg.setStroke(new BasicStroke(3f));
            cg.drawLine(-18, -22, -32, -30 + (int) (Math.sin(anim * 2) * 4));
            cg.drawLine(18, -22, 32, -30 - (int) (Math.sin(anim * 2) * 4));
            cg.dispose();
        }

        /** Beyaz tavuk; panik kanat cirpma. */
        private static void paintChicken(Graphics2D g, double x, double y, double anim) {
            Graphics2D cg = (Graphics2D) g.create();
            cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double flap = Math.sin(anim * 2.2);
            cg.translate(x, y);
            cg.setColor(new Color(230, 180, 60));
            cg.drawLine(-2, -8, -2, 0);
            cg.drawLine(10, -8, 10, 0);
            cg.setColor(new Color(245, 245, 240));
            cg.fillRoundRect(-14, -26, 34, 20, 12, 12);
            cg.setColor(new Color(225, 225, 215));
            int wingY = (int) (-24 - flap * 8);
            cg.fillPolygon(new int[]{-4, -22, -2}, new int[]{wingY, wingY - 10 + (int) (flap * 5), -12}, 3);
            cg.setColor(new Color(245, 245, 240));
            cg.fillOval(10, -38, 14, 14);
            cg.setColor(new Color(240, 170, 50));
            cg.fillPolygon(new int[]{23, 32, 23}, new int[]{-34, -31, -28}, 3);
            cg.setColor(new Color(200, 50, 40));
            cg.fillOval(14, -40, 6, 4);
            cg.setColor(new Color(30, 30, 30));
            cg.fillOval(16, -35, 3, 3);
            cg.dispose();
        }

        /** PHANTOM: mavi-gri kanatli hayalet. */
        private static void paintPhantom(Graphics2D g, double x, double y, double anim) {
            Graphics2D cg = (Graphics2D) g.create();
            cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double flap = Math.sin(anim * 1.5);
            cg.translate(x, y);
            cg.setColor(new Color(70, 90, 120, 230));
            int wingTip = (int) (flap * 14);
            cg.fillPolygon(new int[]{-10, -48, -14}, new int[]{0, wingTip, 12}, 3);
            cg.fillPolygon(new int[]{10, 48, 14}, new int[]{0, wingTip, 12}, 3);
            cg.setColor(new Color(90, 110, 140));
            cg.fillRoundRect(-12, -10, 24, 26, 12, 12);
            cg.setColor(new Color(190, 80, 50));
            cg.fillOval(-8, -4, 5, 5);
            cg.fillOval(4, -4, 5, 5);
            cg.setColor(new Color(70, 90, 120, 200));
            cg.fillPolygon(new int[]{-8, 0, 8}, new int[]{14, 26 + wingTip / 2, 14}, 3);
            cg.dispose();
        }

        /** Varyanta gore mermiyi cizer (ok, fireball, kartopu, blog, yumurta). */
        private static void paintProjectile(Graphics2D g, Variant variant, double x, double y) {
            Graphics2D pg = (Graphics2D) g.create();
            pg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            switch (variant) {
                case SKELETON -> {
                    pg.setColor(new Color(200, 190, 160));
                    pg.setStroke(new BasicStroke(3f));
                    pg.drawLine((int) x, (int) y, (int) x - 16, (int) y);
                    pg.setColor(new Color(160, 150, 120));
                    pg.fillPolygon(new int[]{(int) x, (int) x - 7, (int) x - 7}, new int[]{(int) y, (int) y + 4, (int) y - 4}, 3);
                }
                case BLAZE -> {
                    pg.setColor(new Color(255, 160, 40));
                    pg.fillOval((int) x - 7, (int) y - 7, 14, 14);
                    pg.setColor(new Color(255, 240, 160));
                    pg.fillOval((int) x - 3, (int) y - 3, 7, 7);
                }
                case SNOW_GOLEM -> {
                    pg.setColor(new Color(250, 252, 255));
                    pg.fillOval((int) x - 6, (int) y - 6, 12, 12);
                }
                case ENDERMAN -> {
                    pg.setColor(new Color(96, 160, 60));
                    pg.fillRect((int) x - 11, (int) y - 6, 22, 8);
                    pg.setColor(new Color(120, 85, 55));
                    pg.fillRect((int) x - 11, (int) y + 2, 22, 6);
                }
                case CHICKEN -> {
                    pg.setColor(new Color(240, 230, 200));
                    pg.fillOval((int) x - 5, (int) y - 7, 10, 13);
                }
                default -> {
                }
            }
            pg.dispose();
        }

        /**
         * V29.2 RIGGED CAT - tamamen yeniden yazildi. Eski tek-parca govde
         * yerine gercek bir iskelet/rig kullanir:
         *  - SPINE: omuz + kalca merkezleri arasinda kavisli omurga; govde,
         *    bacaklar, kafa ve kuyruk bu egrinin uzerine oturur. Kosarken
         *    omurga sinus dalgasiyla dalgalanir (gait), otururken kalca
         *    kalkar (dik on govde).
         *  - BODY: omuz-kalca arasi kalin, uca dogru incelen kalem cizimi;
         *    gait ile squash/stretch, govde ustunde kirmizi-kahve tabby
         *    seritleri ve gogus kari.
         *  - LEGS: iki bolumlu (ust/alt) bacaklar; sinüs gait ile salinir,
         *    diz bükülmesi gercek cizgi yuruyusune benzetildi; atlarken
         *    toplanir (tuck), otururken kivrak.
         *  - TAIL: 8 halkali zincir, her halka bir oncekini gecikmeyle
         *    izler; incelerek beyaz kutcukla biter.
         *  - HEAD: omurganin ucuna bagli, hedefe (mob/balik/kelebek)
         *    donuk kafa + goz izleme + rastgele goz kirpma + urkme
         *    pozunda yapisik kulaklar/buyuk gozler.
         *  - POSES: groom (otur + yala), scared (urk), sneak, leaping,
         *    squash & stretch (atlarken uzar, inerken peser).
         */
        private static boolean sneaking_default(double sneakCrouch) {
            return sneakCrouch > 0.01;
        }

        private void paintCat(Graphics2D g, double x, double y, double legPhase, double tailWag, Phase phase, boolean carryingFish, double rotation, double sneakCrouch, Variant variant, double groom, double scared, double stretch) {
            Graphics2D cg = (Graphics2D) g.create();
            cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            cg.translate(x, y - CAT_H);
            // Squash & stretch: y-atayda hacim korunur (alan muhafazali).
            if (stretch != 0) {
                cg.translate(0, CAT_H * 0.5);
                cg.scale(1.0 - 0.45 * stretch, 1.0 + stretch);
                cg.translate(0, -CAT_H * 0.5);
            }
            if (rotation != 0) {
                cg.rotate(rotation, CAT_W * 0.5, CAT_H * 0.5);
            }
            // V39.1 FOTO/TAVUK MODU: kullanici gorseli sprite olarak cizilir.
            // V39.2: ANIMASYON YOK — sprite duz bir sekilde kayar (kullanici
            // istegi: tavuk/foto modunda ziplama/bob olmayacak). Buyukluk
            // eggPhotoScale ayarindan gelir (50..200).
            java.awt.image.BufferedImage sprite = this.customSprite;
            if (sprite != null) {
                double scale = java.lang.Math.max(0.25, java.lang.Math.min(4.0,
                    (this.eggPhotoScalePercent <= 0 ? 100 : this.eggPhotoScalePercent) / 100.0));
                if (java.lang.Math.abs(scale - 1.0) > 0.01) {
                    cg.scale(scale, scale);
                    cg.translate(0, CAT_H * (1.0 - scale) / scale); // ayaklar yerde kalsin
                }
                cg.drawImage(sprite, 0, 0, null);
                cg.dispose();
                return;
            }

            boolean leaping = phase == Phase.LEAP || phase == Phase.AIR_SPIN || phase == Phase.CHASE_HOP || phase == Phase.POUNCE;
            boolean happy = phase == Phase.HAPPY || phase == Phase.SPLASH || phase == Phase.SURFACE || carryingFish;
            boolean dazed = phase == Phase.MISS_RECOIL;
            boolean sneaking = sneakCrouch > 0.01
                && (phase == Phase.SNEAK_CROUCH
                    || (phase == Phase.RUN && variant == Variant.SNEAK)
                    || phase == Phase.MOB_PLAY
                    || phase == Phase.MOB_WIN);
            boolean running = phase == Phase.RUN && !sneaking;
            boolean sitPose = groom > 0.35;
            boolean blackCat = variant == Variant.CHASE;

            // Kürk rengi: varyant bazli, ayni kaldi.
            Color body = switch (variant) {
                case RAMP_JUMP -> new Color(150, 160, 175);
                case DIVE -> new Color(90, 125, 200);
                case DOUBLE_LEAP -> new Color(235, 195, 140);
                case CHASE -> new Color(70, 65, 62);
                case BACKFLIP -> new Color(190, 120, 90);
                case SNEAK -> new Color(120, 105, 150);
                case BUTTERFLY -> new Color(240, 210, 150);
                default -> Theme.ACCENT_BRIGHT != null ? Theme.ACCENT_BRIGHT : new Color(255, 170, 60);
            };
            Color bodyDark = body.darker();
            Color bodyLight = MainWindow.brighten(body, 0.30f);
            Color stripeColor = bodyDark.darker();

            // ---------- SPINE ----------
            // DUZELTME (V29.5): omurga aynaliydi — kedi saga kosarken kafa
            // geride (solda) ve ~180 donuk duruyordu. Simdi kafa SAGDA:
            // omuzlar one (buyuk x), kalcalar arkada. Oturusta kalca yere
            // iner, govdenin on ucu kalkar (gercek kedi oturisi).
            double sit = groom;
            // V29.7: govde YATAY ve dengeli — omuz ve kalca AYNI yukseklikte.
            // Kosarken govde sadece hafif ziplar (egim yok, yamuk durus yok).
            // Oturus: on govde kalkar (54->44), arsa yere iner.
            double shoulderX = 86 - 10 * sit;
            double shoulderY = 62 - 10 * sit;
            double hipX = 34 + 6 * sit;
            double hipY = 62 + 8 * sit;
            double spineWave = running ? Math.sin(legPhase) * 1.5 : (sneaking ? Math.sin(tailWag * 0.7) * 1.0 : 0);
            shoulderY += spineWave;
            hipY += spineWave;

            // ---------- TAIL (8 halkali zincir, uca dogru incelir) ----------
            double[] tx = new double[9];
            double[] ty = new double[9];
            tx[0] = hipX - 2; ty[0] = hipY - 10;
            // V29.7: kuyruk kalcanin ARKASINA (sola) dogru — eski kod saga
            // saniyordu, govdenin altine girip kayboluyordu.
            double tailDir = leaping ? -0.9 : (sneaking ? -0.4 : (sitPose ? -0.35 : -0.55));
            double tailLift = leaping ? 1.0 : (sneaking ? 0.4 : (sitPose ? 0.95 : 0.6));
            double tailWave = leaping ? Math.sin(tailWag * 2.0) * 10 : Math.sin(tailWag) * (sneaking ? 20 : 12);
            for (int i = 1; i <= 8; i++) {
                double f = i / 8.0;
                tx[i] = tx[0] + tailDir * (14 + f * 30) + tailWave * f * 0.55;
                ty[i] = ty[0] - tailLift * f * 38 + Math.sin(tailWag * 1.5 + i * 0.9) * (2.5 + 2.2 * i);
                ty[i] = Math.max(ty[i], 6);
            }
            // dis (koyu) katman: kalin, uca dogru incelen
            for (int i = 8; i >= 1; i--) {
                double seg = 11.5 - i * 0.72;
                cg.setStroke(new BasicStroke((float) seg, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                cg.setColor(i >= 7 ? new Color(250, 250, 250) : bodyDark);
                cg.drawLine((int) tx[i - 1], (int) ty[i - 1], (int) tx[i], (int) ty[i]);
            }
            // ic (govde rengi) katman
            for (int i = 8; i >= 1; i--) {
                double seg = 7.8 - i * 0.45;
                cg.setStroke(new BasicStroke((float) seg, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                cg.setColor(body);
                cg.drawLine((int) tx[i - 1], (int) ty[i - 1], (int) tx[i], (int) ty[i]);
            }

            // ---------- LEGS (V29.6: basit ve duzgun) ----------
            // Her bacak vucut altindan YERE inen iki duz cizgi; pati hep
            // yerde. Yuruyuste ayak sadece +/-5px yatay salinir; bacak
            // asla yana acilmaz, havada ucmaz.
            double groundY = CAT_H - 2;
            double swingA = Math.sin(legPhase) * 5;
            double swingB = Math.sin(legPhase + Math.PI) * 5;
            if (sitPose) {
                // Oturus: on bacaklar dik duser, arka bacak katlanir.
                paintStraightLeg(cg, shoulderX - 8, shoulderY + 13, groundY, -2, bodyDark, bodyDark);
                paintStraightLeg(cg, shoulderX + 8, shoulderY + 13, groundY, 2, body, bodyDark);
                paintFoldedHindLeg(cg, hipX - 6, hipY + 13, groundY, bodyDark, bodyDark.darker());
                paintFoldedHindLeg(cg, hipX + 8, hipY + 13, groundY, body, bodyDark);
            } else if (leaping) {
                // Atlis: on bacaklar one, arkadakiler geriye uzanir.
                paintStraightLeg(cg, shoulderX - 8, shoulderY + 13, groundY, -4, bodyDark, bodyDark);
                paintStraightLeg(cg, shoulderX + 8, shoulderY + 13, groundY, 9, body, bodyDark);
                paintStraightLeg(cg, hipX - 8, hipY + 13, groundY, -8, bodyDark, bodyDark);
                paintStraightLeg(cg, hipX + 8, hipY + 13, groundY, -3, body, bodyDark);
            } else {
                // Kosma/yurusma: capraz ciftler faz farkiyla salinir.
                paintStraightLeg(cg, shoulderX - 8, shoulderY + 13, groundY, -2 + swingB, bodyDark, bodyDark);
                paintStraightLeg(cg, shoulderX + 8, shoulderY + 13, groundY, 2 + swingA, body, bodyDark);
                paintStraightLeg(cg, hipX - 8, hipY + 13, groundY, -3 - swingA, bodyDark, bodyDark);
                paintStraightLeg(cg, hipX + 8, hipY + 13, groundY, 3 - swingB, body, bodyDark);
            }

            // ---------- BODY (omuz-kalca kalin gocertili cizim) ----------
            double bodyAngle = Math.atan2(shoulderY - hipY, shoulderX - hipX);
            double bodyLen = Math.hypot(shoulderX - hipX, shoulderY - hipY);
            double bodyThick = 30 + 4 * (1.0 - Math.abs(stretch) * 2.0);
            Graphics2D bg = (Graphics2D) cg.create();
            bg.translate(hipX, hipY);
            bg.rotate(bodyAngle);
            // Ana govde: kalin kalem cizimi (uclarda yuvarlak).
            bg.setStroke(new BasicStroke((float) bodyThick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            bg.setColor(body);
            bg.drawLine(0, 0, (int) bodyLen, 0);
            // Ust parlaklik (isik ustten)
            bg.setColor(new Color(bodyLight.getRed(), bodyLight.getGreen(), bodyLight.getBlue(), 80));
            bg.setStroke(new BasicStroke((float) (bodyThick * 0.45), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            bg.drawLine((int) (bodyLen * 0.15), (int) (-bodyThick * 0.18), (int) (bodyLen * 0.85), (int) (-bodyThick * 0.18));
            // Tabby seritleri - govde eksenine dik kisa koyu cizgiler
            bg.setColor(new Color(stripeColor.getRed(), stripeColor.getGreen(), stripeColor.getBlue(), 120));
            bg.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 3; i++) {
                double sx = bodyLen * (0.30 + i * 0.20);
                bg.drawLine((int) sx, (int) (-bodyThick * 0.42), (int) sx, (int) (-bodyThick * 0.05));
            }
            // Gogus kari - omuza yakin alttaraf
            bg.setColor(new Color(252, 250, 248, 175));
            bg.setStroke(new BasicStroke((float) (bodyThick * 0.5), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            bg.drawLine((int) (bodyLen * 0.82), (int) (bodyThick * 0.28), (int) (bodyLen + 8), (int) (bodyThick * 0.28));
            bg.dispose();

            // ---------- HEAD (spine ucu, SAGDA + hedefe bakar) ----------
            double headDist = 22 + 6 * sit;
            double headAng = Math.atan2(shoulderY - hipY, shoulderX - hipX) + (leaping ? -0.15 : 0);
            double hx = shoulderX + Math.cos(headAng) * headDist;
            double hy = shoulderY + Math.sin(headAng) * headDist - (leaping ? 4 : 0);
            double aim = this.headAim;
            // Goz kirpma: tailWag tabanli deterministik interval (2-3 sn'de bir).
            double blinkT = tailWag % 2.7;
            boolean blinking = !dazed && !happy && !leaping && blinkT < 0.12;
            // Scared: kulaklar yapisir, gozler buyur.
            double earFlatten = scared;
            double eyeScale = 1.0 + scared * 0.5;

            double headR = 24 + 3 * sit;
            double headRot = bodyAngle + aim * 0.5;
            Graphics2D hg = (Graphics2D) cg.create();
            hg.translate(hx, hy);
            hg.rotate(headRot);
            // Kulaklar: korkuda yapisir.
            double earBack = -earFlatten * 14;
            for (int side = -1; side <= 1; side += 2) {
                int exs = (int) (side * (headR - 4));
                int exs2 = (int) (side * (headR + 4) + earBack);
                int eys2 = (int) (-headR - 16 + earBack);
                hg.setColor(body);
                hg.fillPolygon(
                    new int[]{exs, (int) (side * (headR - 2)), exs2},
                    new int[]{(int) (-headR + 4), (int) (-headR - 2), eys2}, 3);
                hg.setColor(new Color(255, 190, 205));
                hg.fillPolygon(
                    new int[]{exs + (int) (side * -2), (int) (side * (headR - 5)), exs2 + (int) (side * -1)},
                    new int[]{(int) (-headR + 6), (int) (-headR - 4), (int) (-headR - 11 + earBack)}, 3);
            }
            // Kafa topu + ust parlaklik
            hg.setColor(body);
            hg.fillOval((int) -headR, (int) -headR, (int) headR * 2, (int) headR * 2);
            hg.setColor(new Color(bodyLight.getRed(), bodyLight.getGreen(), bodyLight.getBlue(), 70));
            hg.fillOval((int) (-headR + 3), (int) (-headR + 3), (int) (headR * 2 - 6), (int) (headR));
            // Alin cizgileri
            hg.setColor(new Color(stripeColor.getRed(), stripeColor.getGreen(), stripeColor.getBlue(), 130));
            hg.fillRoundRect(-9, (int) (-headR + 4), 4, 10, 3, 3);
            hg.fillRoundRect(5, (int) (-headR + 4), 4, 10, 3, 3);
            // Gozler: beyaz + iris + bebek; kirpma ve urkme
            double eyeY = -2, eyeDX = headR * 0.55;
            for (int side = -1; side <= 1; side += 2) {
                double ex = side * eyeDX, ey = eyeY;
                if (dazed) {
                    hg.setColor(Theme.BG_BASE);
                    hg.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    hg.drawLine((int) (ex - 6), (int) (ey - 6), (int) (ex + 6), (int) (ey + 6));
                    hg.drawLine((int) (ex + 6), (int) (ey - 6), (int) (ex - 6), (int) (ey + 6));
                } else if (happy) {
                    hg.setColor(Theme.BG_BASE);
                    hg.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    hg.drawArc((int) (ex - 5), (int) (ey - 5), 10, 10, 0, 180);
                } else if (blinking) {
                    hg.setColor(Theme.BG_BASE);
                    hg.setStroke(new  BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    hg.drawLine((int) (ex - 5), (int) ey, (int) (ex + 5), (int) ey);
                } else {
                    hg.setColor(new Color(255, 255, 255));
                    hg.fillOval((int) (ex - 6 * eyeScale), (int) (ey - 6 * eyeScale), (int) (12 * eyeScale), (int) (12 * eyeScale));
                    hg.setColor(blackCat ? new Color(255, 215, 60) : new Color(150, 220, 90));
                    hg.fillOval((int) (ex - 4 * eyeScale), (int) (ey - 4 * eyeScale), (int) (8 * eyeScale), (int) (8 * eyeScale));
                    hg.setColor(new Color(20, 18, 24));
                    hg.fillOval((int) (ex - 2), (int) (ey - 3 * eyeScale), 4, (int) (6 * eyeScale));
                    hg.setColor(new Color(255, 255, 255, 220));
                    hg.fillOval((int) (ex - 3 * eyeScale), (int) (ey - 5 * eyeScale), 2, 2);
                }
            }
            // Burun + agiz + biyiklar
            hg.setColor(new Color(255, 150, 160));
            hg.fillOval(-3, 2, 6, 5);
            hg.setColor(Theme.BG_BASE);
            hg.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            hg.drawLine(0, 7, 0, 9);
            hg.drawArc(-5, 5, 5, 5, 250, 100);
            hg.drawArc(0, 5, 5, 5, 190, 100);
            // Biyikciklar
            hg.setColor(new Color(255, 255, 255, 150));
            hg.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int side = -1; side <= 1; side += 2) {
                java.awt.geom.QuadCurve2D w1 = new java.awt.geom.QuadCurve2D.Double(
                    side * headR * 0.85, -1,
                    side * headR * 1.35, -5,
                    side * headR * 1.8, -4);
                java.awt.geom.QuadCurve2D w2 = new java.awt.geom.QuadCurve2D.Double(
                    side * headR * 0.85, 4,
                    side * headR * 1.35, 5,
                    side * headR * 1.8, 6);
                hg.draw(w1);
                hg.draw(w2);
            }
            hg.dispose();

            // Agizda tasinan balik (kafa rotasyonu ile ayni acida)
            if (carryingFish) {
                Graphics2D fg = (Graphics2D) cg.create();
                fg.translate(hx + Math.cos(headRot) * 12, hy + Math.sin(headRot) * 12);
                fg.rotate(headRot + Math.toRadians(-20));
                paintFish(fg, -FISH_W * 0.3, -FISH_H * 0.5, 0, 0.6);
                fg.dispose();
            }

            cg.dispose();
        }

        /** V29.6: basit duz bacak — vucuttan yere, pati hep yerde.
         *  footDx = ayagin yatay ofseti (yuruyus salinimi, maks ~5px). */
        private static void paintStraightLeg(Graphics2D cg, double x0, double y0, double groundY, double footDx, Color color, Color pawColor) {
            double footX = x0 + footDx;
            double midX = x0 + footDx * 0.4;
            double midY = y0 + (groundY - y0) * 0.55;
            cg.setStroke(new BasicStroke(8.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            cg.setColor(color);
            cg.drawLine((int) x0, (int) y0, (int) midX, (int) midY);
            cg.drawLine((int) midX, (int) midY, (int) footX, (int) groundY);
            paintPaw(cg, footX, groundY, pawColor);
        }

        /** Oturus pozu arka bacak: uyluk one-iner, alt bacak dik yere iner. */
        private static void paintFoldedHindLeg(Graphics2D cg, double x0, double y0, double groundY, Color color, Color pawColor) {
            double kneeX = x0 + 10;
            double kneeY = y0 + (groundY - y0) * 0.35;
            double footX = kneeX + 8;
            cg.setStroke(new BasicStroke(8.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            cg.setColor(color);
            cg.drawLine((int) x0, (int) y0, (int) kneeX, (int) kneeY);
            cg.drawLine((int) kneeX, (int) kneeY, (int) footX, (int) groundY);
            paintPaw(cg, footX, groundY, pawColor);
        }

        /** Yuvarlak, yumusak bir pati cizer (bacak ucuna eklenir). */
        private static void paintPaw(Graphics2D cg, double x, double y, Color color) {
            cg.setColor(color);
            cg.fillOval((int) x - 6, (int) y - 4, 12, 10);
            cg.setColor(new Color(255, 170, 185));
            cg.fillOval((int) x - 4, (int) y - 2, 5, 5);
            cg.fillOval((int) (x + 1), (int) (y - 1), 3, 3);
        }

        private static void paintMob(Graphics2D g, Variant variant, double x, double y, double anim, int state) {
            switch (variant) {
                case CREEPER -> paintCreeper(g, x, y, anim);
                case ENDERMAN -> paintEnderman(g, x, y, anim);
                case SKELETON -> paintSkeleton(g, x, y, anim);
                case BLAZE -> paintBlaze(g, x, y, anim);
                case SLIME -> paintSlime(g, x, y, anim, state);
                case AXOLOTL -> paintAxolotl(g, x, y, anim);
                case PARROT -> paintParrot(g, x, y, anim);
                case SNOW_GOLEM -> paintSnowGolem(g, x, y, anim, state);
                case CHICKEN -> paintChicken(g, x, y, anim);
                default -> {
                }
            }
        }

        /** Yesil, piksel hissi veren creeper (4 bacak, Cat-Stil govde). */
        private static void paintCreeper(Graphics2D g, double x, double y, double anim) {
            Graphics2D cg = (Graphics2D) g.create();
            cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double hop = Math.abs(Math.sin(anim)) * 3;
            cg.translate(x, y - hop);
            Color skin = new Color(88, 170, 70);
            Color dark = new Color(60, 120, 48);
            // 4 kisa bacak
            cg.setColor(dark);
            cg.fillRect(2, -12, 12, 12);
            cg.fillRect(34, -12, 12, 12);
            cg.fillRect(66, -12, 12, 12);
            cg.fillRect(98, -12, 12, 12);
            // Govde (dikdortgen, MC'deki gibi)
            cg.setColor(skin);
            cg.fillRect(8, -58, 96, 48);
            // Yuz: MC creeper yuzu - iki goz + asagi acilan agiz
            cg.setColor(new Color(20, 40, 16));
            cg.fillRect(28, -50, 16, 14);
            cg.fillRect(68, -50, 16, 14);
            cg.fillRect(44, -38, 24, 10);
            cg.fillRect(36, -30, 12, 14);
            cg.fillRect(64, -30, 12, 14);
            // Piksel benekleri (koyu lekeler, ton varyasyonu)
            cg.setColor(new Color(70, 140, 56, 160));
            java.util.Random rnd = new java.util.Random(7); // sabit tohum: titreme yok
            for (int i = 0; i < 14; i++) {
                int px = 10 + rnd.nextInt(90);
                int py = -54 + rnd.nextInt(40);
                cg.fillRect(px, py, 5, 5);
            }
            cg.dispose();
        }

        /** Dusen/ucan etkiler: duman, kor, tuy, kar, baloncuk, isinlanma parcalari. */
        private static class Debris {
                enum Kind { SMOKE, EMBER, SNOW, BUBBLE, FEATHER, TELEPORT, FLASH, BONE, PUFF }
                double x, y, vx, vy;
                double life = 0;
                double maxLife;
                Kind kind;
                Debris(double x, double y, double vx, double vy, Kind kind) {
                    this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.kind = kind;
                    this.maxLife = switch (kind) {
                        case SMOKE -> 1.1;
                        case FLASH -> 0.35;
                        case BUBBLE -> 1.3;
                        case FEATHER -> 1.6;
                        default -> 0.8;
                    };
                }
                void update(double dt) {
                    switch (this.kind) {
                        case SMOKE -> {
                            this.vy -= 30 * dt; // duman yukselir
                            this.vx *= (1.0 - 0.8 * dt);
                        }
                        case EMBER -> this.vy += 240 * dt;
                        case SNOW -> {
                            this.vy += 60 * dt;
                            this.vx += Math.sin((this.life + this.x) * 3.0) * 40 * dt;
                        }
                        case BUBBLE -> this.vy -= 40 * dt;
                        case FEATHER -> {
                            this.vy += 60 * dt;
                            this.vx += Math.sin((this.life + this.x) * 2.5) * 60 * dt;
                        }
                        case TELEPORT -> {
                            this.vx *= (1.0 - 2.5 * dt);
                            this.vy *= (1.0 - 2.5 * dt);
                        }
                        case FLASH -> {
                            this.vx *= (1.0 - 3.0 * dt);
                            this.vy *= (1.0 - 3.0 * dt);
                        }
                        default -> this.vy += 300 * dt;
                    }
                    this.x += this.vx * dt;
                    this.y += this.vy * dt;
                    this.life += dt;
                }
                boolean dead() { return this.life >= this.maxLife; }
                void paint(Graphics2D g) {
                    double p = this.life / this.maxLife;
                    int alpha = (int) (255 * (1 - p));
                    if (alpha <= 0) return;
                    switch (this.kind) {
                        case SMOKE -> {
                            int s = (int) (8 + p * 18);
                            g.setColor(new Color(60, 60, 60, (int) (alpha * 0.55)));
                            g.fillOval((int) this.x - s / 2, (int) this.y - s / 2, s, s);
                        }
                        case EMBER -> {
                            g.setColor(new Color(255, 120 + (int) (80 * (1 - p)), 40, alpha));
                            int s = (int) (5 + 3 * (1 - p));
                            g.fillRect((int) this.x, (int) this.y, s, s);
                        }
                        case SNOW -> {
                            g.setColor(new Color(255, 255, 255, (int) (alpha * 0.9)));
                            g.fillOval((int) this.x, (int) this.y, 5, 5);
                        }
                        case BUBBLE -> {
                            g.setColor(new Color(210, 240, 255, (int) (alpha * 0.7)));
                            g.drawOval((int) this.x, (int) this.y, 6, 6);
                        }
                        case FEATHER -> {
                            g.setColor(new Color(250, 250, 250, alpha));
                            g.fillOval((int) this.x, (int) this.y, 8, 4);
                        }
                        case TELEPORT -> {
                            g.setColor(new Color(170, 90, 240, alpha));
                            g.fillRect((int) this.x, (int) this.y, 4, 4);
                        }
                        case FLASH -> {
                            g.setColor(new Color(255, 255, 230, (int) (alpha * 0.85)));
                            g.fillOval((int) this.x - 4, (int) this.y - 4, 10, 10);
                        }
                        case BONE -> {
                            g.setColor(new Color(235, 228, 200, alpha));
                            g.fillRoundRect((int) this.x - 6, (int) this.y - 2, 14, 4, 4, 4);
                        }
                        case PUFF -> {
                            int s = (int) (6 + p * 14);
                            g.setColor(new Color(240, 240, 240, (int) (alpha * 0.6)));
                            g.fillOval((int) this.x - s / 2, (int) this.y - s / 2, s, s);
                        }
                    }
                }
            }

        /** Tek bir su damlasi parcacigi: yerçekimi + solma. */
        private static class Splash {
            double x, y, vx, vy;
            double life = 0;
            static final double MAX_LIFE = 0.6;
            Splash(double x, double y, double vx, double vy) {
                this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            }
            void update(double dt) {
                this.vy += 620 * dt; // yer çekimi (2x olcek icin biraz guclu)
                this.x += this.vx * dt;
                this.y += this.vy * dt;
                this.life += dt;
            }
            boolean dead() { return this.life >= MAX_LIFE; }
            void paint(Graphics2D g, Color base) {
                double alpha = Math.max(0, 1.0 - this.life / MAX_LIFE);
                int a = (int)(210 * alpha);
                g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), a));
                double size = 8 * alpha + 2.5; // 2x olcek icin buyutuldu
                g.fill(new java.awt.geom.Ellipse2D.Double(this.x - size / 2, this.y - size / 2, size, size));
            }
        }

        /** Suya inis sonrasi genisleyen halka (dalgalanma) efekti. */
        private static class Ripple {
            double x, y;
            double life = 0;
            static final double MAX_LIFE = 0.55;
            Ripple(double x, double y) { this.x = x; this.y = y; }
            void update(double dt) { this.life += dt; }
            boolean dead() { return this.life >= MAX_LIFE; }
            void paint(Graphics2D g) {
                double progress = this.life / MAX_LIFE;
                double radius = 12 + progress * 52; // 2x olcek icin buyutuldu
                int alpha = (int)(150 * (1 - progress));
                if (alpha <= 0) return;
                g.setColor(new Color(255, 255, 255, alpha));
                g.setStroke(new BasicStroke(3f));
                g.draw(new java.awt.geom.Ellipse2D.Double(this.x - radius, this.y - radius / 3, radius * 2, radius * 2 / 3));
            }
        }
    }

    private void finishLaunch() {
        SwingUtilities.invokeLater(() -> {
            this.launching = false;
            this.launchButton.setEnabled(true);
            this.launchButton.setText("\u25b6  " + L10n.get("launch"));
        });
    }

    private void autoInstallForLoader(String string, File file, String string2) {
        if (string == null || file == null || string2 == null) {
            return;
        }
        file.mkdirs();
        if (string.equalsIgnoreCase("Fabric")) {
            this.autoInstallFabricMods(file, string2);
        } else if (string.equalsIgnoreCase("Forge")) {
            this.autoInstallForgeMods(file, string2);
        } else if (string.equalsIgnoreCase("NeoForge")) {
            this.autoInstallNeoForgeMods(file, string2);
        } else {
            this.log("[AutoInstall] " + string + " i\u00e7in otomatik mod yok.");
        }
    }

    private void autoInstallFabricMods(File file, String string) {
        this.log("[AutoInstall] Fabric (MC " + string + ")...");
        // V29.5: varsayilanlar KAPALI (ilk calistirmada hicbir mod otomatik kurulmaz).
        boolean bl = MainWindow.isModEnabled("vulkanmod", false);
        boolean bl2 = MainWindow.isModEnabled("sodium", false);
        boolean bl3 = MainWindow.isModEnabled("iris", false);
        boolean bl4 = MainWindow.isModEnabled("jei", false);
        boolean bl5 = MainWindow.isModEnabled("bsl", false);
        boolean bl6 = MainWindow.isModEnabled("complementary", false);
        boolean bl7 = MainWindow.isModEnabled("journeymap", false);
        boolean bl8 = MainWindow.isModEnabled("replaymod", false);
        boolean bl9 = MainWindow.isModEnabled("amusemod", false);
        boolean bl10 = MainWindow.isModEnabled("tweakeroo", false);
        boolean bl11 = false;
        if (bl && MainWindow.isVulkanCompatible(string)) {
            try {
                bl11 = ModManager.installProject(file, VULKANMOD_PROJECT_ID, "fabric", string);
                if (bl11) {
                    this.log("+ VulkanMod kuruldu");
                } else {
                    this.log("[VulkanMod] uyumlu yok, Sodium'a geciliyor.");
                }
            }
            catch (Exception exception) {
                this.log("[VulkanMod] " + exception.getMessage());
            }
        }
        if (!bl11 && (bl2 || bl3)) {
            try {
                // V35 FIX: cift kurulum (installShaderPair) her iki modu da
                // indiriyordu - kullanici sadece birini secse bile digeri de
                // geliyordu ("secmedigim mod niye kurulu" bug'unun kaynagi).
                // Artik her iki tercih tek tek denetlenir; secilen neyse
                // SADECE o kurulur (ikisi de seciliyse cift kurulum calisir).
                if (bl2 && bl3) {
                    ModManager.PairResult pairResult = ModManager.installShaderPair(file, IRIS_PROJECT_ID, SODIUM_PROJECT_ID, "fabric", string);
                    this.logChange("Sodium", pairResult.base);
                    this.logChange("Iris", pairResult.shader);
                } else if (bl2) {
                    ModManager.installProject(file, SODIUM_PROJECT_ID, "fabric", string);
                    this.log("+ Sodium kuruldu");
                } else {
                    ModManager.installProject(file, IRIS_PROJECT_ID, "fabric", string);
                    this.log("+ Iris kuruldu");
                }
            }
            catch (Exception exception) {
                this.log("[HATA] Sodium/Iris: " + exception.getMessage());
            }
        }
        if (bl4) {
            this.tryInstall(file, JEI_PROJECT_ID, "fabric", string, "Just Enough Items");
        }
        if (bl7) {
            this.tryInstall(file, JOURNEYMAP_PROJECT_ID, "fabric", string, "JourneyMap");
        }
        if (bl8) {
            this.tryInstall(file, REPLAYMOD_PROJECT_ID, "fabric", string, "ReplayMod");
        }
        if (bl9) {
            this.tryInstall(file, AMUSEMOD_PROJECT_ID, "fabric", string, "AmuseMenu");
        }
        if (bl10) {
            this.tryInstall(file, TWEAKEROO_PROJECT_ID, "fabric", string, "Tweakeroo");
        }
        if (bl5) {
            try {
                this.installShaderPack(BSL_SHADER_ID, "BSL Shaders");
            }
            catch (Exception exception) {
                this.log("[HATA] BSL: " + exception.getMessage());
            }
        }
        if (bl6) {
            try {
                this.installShaderPack(COMPLEMENTARY_SHADER_ID, "Complementary Reimagined");
            }
            catch (Exception exception) {
                this.log("[HATA] Complementary: " + exception.getMessage());
            }
        }
        this.log("[AutoInstall] Fabric tamam." + (bl11 ? " (Vulkan)" : ""));
        this.autoInstallCustomMods(file, "fabric", string);
        SwingUtilities.invokeLater(this.modsPanel::refreshInstalled);
    }

    private void autoInstallForgeMods(File file, String string) {
        this.log("[AutoInstall] Forge (MC " + string + ")...");
        // V29.5: varsayilanlar KAPALI.
        boolean bl = MainWindow.isModEnabled("embeddium", false);
        boolean bl2 = MainWindow.isModEnabled("oculus", false);
        boolean bl3 = MainWindow.isModEnabled("jei", false);
        boolean bl4 = MainWindow.isModEnabled("bsl", false);
        boolean bl5 = MainWindow.isModEnabled("complementary", false);
        boolean bl6 = MainWindow.isModEnabled("journeymap", false);
        boolean bl7 = MainWindow.isModEnabled("replaymod", false);
        boolean bl8 = MainWindow.isModEnabled("amusemod", false);
        boolean bl9 = MainWindow.isModEnabled("tweakeroo", false);
        if (bl || bl2) {
            try {
                // V35 FIX: Fabric yolundakiyla ayni - cift kurulum tek tercih
                // seciliyken digerini de indiriyordu. Secilen neyse sadece o.
                if (bl && bl2) {
                    ModManager.PairResult pairResult = ModManager.installShaderPair(file, OCULUS_PROJECT_ID, EMBEDDIUM_PROJECT_ID, "forge", string);
                    this.logChange("Embeddium", pairResult.base);
                    this.logChange("Oculus", pairResult.shader);
                } else if (bl) {
                    ModManager.installProject(file, EMBEDDIUM_PROJECT_ID, "forge", string);
                    this.log("+ Embeddium kuruldu");
                } else {
                    ModManager.installProject(file, OCULUS_PROJECT_ID, "forge", string);
                    this.log("+ Oculus kuruldu");
                }
            }
            catch (Exception exception) {
                this.log("[HATA] Embeddium/Oculus: " + exception.getMessage());
            }
        }
        if (bl3) {
            this.tryInstall(file, JEI_PROJECT_ID, "forge", string, "Just Enough Items");
        }
        if (bl6) {
            this.tryInstall(file, JOURNEYMAP_PROJECT_ID, "forge", string, "JourneyMap");
        }
        if (bl7) {
            this.tryInstall(file, REPLAYMOD_PROJECT_ID, "forge", string, "ReplayMod");
        }
        if (bl8) {
            this.tryInstall(file, AMUSEMOD_PROJECT_ID, "forge", string, "AmuseMenu");
        }
        if (bl9) {
            this.tryInstall(file, TWEAKEROO_PROJECT_ID, "forge", string, "Tweakeroo");
        }
        if (bl4) {
            try {
                this.installShaderPack(BSL_SHADER_ID, "BSL Shaders");
            }
            catch (Exception exception) {
                this.log("[HATA] BSL: " + exception.getMessage());
            }
        }
        if (bl5) {
            try {
                this.installShaderPack(COMPLEMENTARY_SHADER_ID, "Complementary Reimagined");
            }
            catch (Exception exception) {
                this.log("[HATA] Complementary: " + exception.getMessage());
            }
        }
        this.log("[AutoInstall] Forge tamam.");
        this.autoInstallCustomMods(file, "forge", string);
        SwingUtilities.invokeLater(this.modsPanel::refreshInstalled);
    }

    private void autoInstallNeoForgeMods(File file, String string) {
        this.log("[AutoInstall] NeoForge (MC " + string + ")...");
        // V29.5: varsayilanlar KAPALI (NeoForge).
        boolean bl = MainWindow.isModEnabled("sodium", false);
        boolean bl2 = MainWindow.isModEnabled("iris", false);
        boolean bl3 = MainWindow.isModEnabled("jei", false);
        boolean bl4 = MainWindow.isModEnabled("bsl", false);
        boolean bl5 = MainWindow.isModEnabled("complementary", false);
        boolean bl6 = MainWindow.isModEnabled("journeymap", false);
        boolean bl7 = MainWindow.isModEnabled("replaymod", false);
        boolean bl8 = MainWindow.isModEnabled("amusemod", false);
        boolean bl9 = MainWindow.isModEnabled("tweakeroo", false);
        if (bl || bl2) {
            try {
                // V35 FIX: tek tercih seciliyken cift kurulum digerini de
                // indiriyordu (Forge yoluyla ayni bug). Secilen neyse sadece o.
                if (bl && bl2) {
                    ModManager.PairResult pairResult = ModManager.installShaderPair(file, IRIS_PROJECT_ID, SODIUM_PROJECT_ID, "neoforge", string);
                    this.logChange("Sodium", pairResult.base);
                    this.logChange("Iris", pairResult.shader);
                } else if (bl) {
                    ModManager.installProject(file, SODIUM_PROJECT_ID, "neoforge", string);
                    this.log("+ Sodium kuruldu");
                } else {
                    ModManager.installProject(file, IRIS_PROJECT_ID, "neoforge", string);
                    this.log("+ Iris kuruldu");
                }
            }
            catch (Exception exception) {
                this.log("[HATA] Sodium/Iris: " + exception.getMessage());
            }
        }
        if (bl3) {
            this.tryInstall(file, JEI_PROJECT_ID, "neoforge", string, "Just Enough Items");
        }
        if (bl6) {
            this.tryInstall(file, JOURNEYMAP_PROJECT_ID, "neoforge", string, "JourneyMap");
        }
        if (bl7) {
            this.tryInstall(file, REPLAYMOD_PROJECT_ID, "neoforge", string, "ReplayMod");
        }
        if (bl8) {
            this.tryInstall(file, AMUSEMOD_PROJECT_ID, "neoforge", string, "AmuseMenu");
        }
        if (bl9) {
            this.tryInstall(file, TWEAKEROO_PROJECT_ID, "neoforge", string, "Tweakeroo");
        }
        if (bl4) {
            try {
                this.installShaderPack(BSL_SHADER_ID, "BSL Shaders");
            }
            catch (Exception exception) {
                this.log("[HATA] BSL: " + exception.getMessage());
            }
        }
        if (bl5) {
            try {
                this.installShaderPack(COMPLEMENTARY_SHADER_ID, "Complementary Reimagined");
            }
            catch (Exception exception) {
                this.log("[HATA] Complementary: " + exception.getMessage());
            }
        }
        this.log("[AutoInstall] NeoForge tamam.");
        this.autoInstallCustomMods(file, "neoforge", string);
        SwingUtilities.invokeLater(this.modsPanel::refreshInstalled);
    }

    private void tryInstall(File file, String string, String string2, String string3, String string4) {
        try {
            String resolvedId = resolveProjectId(string);
            if (resolvedId == null) {
                this.log("  " + string4 + " Modrinth'te bulunamadi (" + string + ")");
                return;
            }
            boolean bl = ModManager.installProject(file, resolvedId, string2.toLowerCase(), string3);
            if (bl) {
                this.log("+ " + string4 + " kuruldu");
            } else {
                this.log("  " + string4 + " uyumlu s\u00fcr\u00fcm yok (" + string2 + " " + string3 + ")");
            }
        }
        catch (Exception exception) {
            this.log("[HATA] " + string4 + ": " + exception.getMessage());
        }
    }

    /**
     * Verilen Modrinth ID/slug'ini gercek bir projeye cozumler.
     * Slug'lar API'de stabil oldugu icin dogrudan calisir; eski/kirik
     * numerik ID'ler (proje silinip yeniden yuklenince degisiyor) 404
     * dondurdugu icin bu durumda slug'a yonlenilir. ID zaten gecerliyse
     * aynen dondurulur, hicbir ag cagrisi yapilmaz.
     */
    private static String resolveProjectId(String idOrSlug) {
        if (idOrSlug == null || idOrSlug.isBlank()) {
            return null;
        }
        try {
            byte[] bytes = HttpUtil.getBytesWithRetry("https://api.modrinth.com/v2/project/" + java.net.URLEncoder.encode(idOrSlug, "UTF-8"), 2);
            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(new String(bytes, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            if (obj.has("id")) {
                return obj.get("id").getAsString();
            }
            return idOrSlug;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Proje ID'sinden slug ceker (InstallMeta kaydi icin). Basarisiz olursa
     * verilen degeri oldugu gibi dondurur - Modrinth slug VE id ile calisir.
     */
    private static String resolveSlug(String idOrSlug) {
        if (idOrSlug == null || idOrSlug.isBlank()) {
            return idOrSlug;
        }
        try {
            byte[] bytes = HttpUtil.getBytesWithRetry("https://api.modrinth.com/v2/project/" + java.net.URLEncoder.encode(idOrSlug, "UTF-8"), 2);
            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(new String(bytes, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            if (obj.has("slug")) {
                return obj.get("slug").getAsString();
            }
        } catch (Exception ignored) {
        }
        return idOrSlug;
    }

    private static boolean isVulkanCompatible(String string) {
        try {
            String[] stringArray = string.split("\\.");
            int n = Integer.parseInt(stringArray[0]);
            int n2 = stringArray.length > 1 ? Integer.parseInt(stringArray[1]) : 0;
            return n >= 26 || n == 1 && n2 >= 17;
        }
        catch (Exception exception) {
            return false;
        }
    }

    private void installShaderPack(String string, String string2) throws Exception {
        if (this.currentInstance == null) {
            return;
        }
        String resolvedId = resolveProjectId(string);
        if (resolvedId == null) {
            this.log("  " + string2 + " Modrinth'te bulunamadi (" + string + ")");
            return;
        }
        File file = this.currentInstance.shaderpacksDir();
        file.mkdirs();
        List<ModrinthApi.ModVersion> list = ModrinthApi.getVersions(resolvedId, null, null);
        if (list.isEmpty()) {
            return;
        }
        ModrinthApi.ModVersion modVersion = ModrinthApi.pickBestVersion(list);
        ShaderManager.install(file, modVersion, null);
        // Otomatik kurulumlarda da meta kaydet: kurulu listede cift
        // tiklayinca bilgi paneli TAM dogru projeyi acar.
        try {
            com.lubv.launcher.mods.InstallMeta.Entry meta = new com.lubv.launcher.mods.InstallMeta.Entry();
            meta.slug = resolveSlug(resolvedId);
            meta.title = string2;
            meta.source = "modrinth";
            com.lubv.launcher.mods.InstallMeta.put(file, modVersion.fileName, meta);
        } catch (Exception ignored) {
        }
        this.log(string2 + " kuruldu (" + modVersion.versionNumber + ")");
        SwingUtilities.invokeLater(this.shaderPanel::refreshInstalled);
    }

    private void logChange(String string, ModManager.ChangeType changeType) {
        if (changeType == ModManager.ChangeType.INSTALLED) {
            this.log("+ " + string + " kuruldu");
        } else if (changeType == ModManager.ChangeType.UPDATED) {
            this.log("+ " + string + " guncellendi");
        } else if (changeType == ModManager.ChangeType.NONE) {
            this.log("  " + string + " zaten kurulu veya bulunamadi");
        }
    }

    private static JPanel buildCard() {
        JPanel jPanel = new JPanel(){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setColor(Theme.BG_SURFACE);
                graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 12, 12);
                graphics2D.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        jPanel.setBorder(new EmptyBorder(12, 14, 12, 14));
        return jPanel;
    }

    private static Icon makeLogo() {
        return LogoRes.icon(32);
    }

    /**
     * V36.3 FIX: hesap fotorafini 76x76 avatar olarak cache'ler.
     * ESKI BUG: buyuk PNG'lerde (orn. 2541x1432) 6-arg drawImage crop
     * yolunda hicbir sey cizilmiyordu - daire bos kaliyordu. Simdi
     * getSubimage + getScaledInstance(SCALE_SMOOTH) ile once kucuk kare
     * uretilir; cizim her boyutta garantidir. Ayrica her karede disk
     * okumasi yerine tek seferlik cache kullanilir.
     */
    private java.awt.image.BufferedImage loadAccountPhoto() {
        String path = this.settings != null ? this.settings.accountPhotoPath : null;
        if (path == null || path.isBlank()) {
            return null;
        }
        if (this.accountPhotoImage != null && path.equals(this.accountPhotoLoadedPath)) {
            return this.accountPhotoImage;
        }
        try {
            java.io.File f = new java.io.File(path);
            if (!f.isFile() || f.length() == 0L) {
                return null;
            }
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
            if (img != null) {
                final int TARGET = 76; // n5*2-4 ile ayni
                int pw = img.getWidth(), phh = img.getHeight();
                if (pw > 0 && phh > 0) {
                    int side = Math.min(pw, phh);
                    java.awt.image.BufferedImage cropped = img.getSubimage((pw - side) / 2, (phh - side) / 2, side, side);
                    Image scaled = cropped.getScaledInstance(TARGET, TARGET, Image.SCALE_SMOOTH);
                    java.awt.image.BufferedImage avatar = new java.awt.image.BufferedImage(TARGET, TARGET, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    Graphics2D sg = avatar.createGraphics();
                    sg.drawImage(scaled, 0, 0, null);
                    sg.dispose();
                    this.accountPhotoImage = avatar;
                } else {
                    this.accountPhotoImage = img;
                }
                this.accountPhotoLoadedPath = path;
            }
            return this.accountPhotoImage;
        }
        catch (Exception ignored) {
            return null;
        }
    }

    private volatile java.awt.image.BufferedImage accountPhotoImage;
    private volatile String accountPhotoLoadedPath = "";

    /** V39.1: easter egg foto modu — dis dosyadan sprite yukle (merkez kare, 140x92'ye sigdir). */
    // V39.5.1: sprite onbellegi (ayni dosya icin ImageIO.read bir kez).
    // Overlay her acildiginda resmi yeniden cozmek EDT'yi yormuyor;
    // buyuk webp'lerde tekrar ac/kapa gecikmesi de biter.
    private static final java.util.Map<String, java.awt.image.BufferedImage> SPRITE_CACHE =
        new java.util.concurrent.ConcurrentHashMap<>();

    static java.awt.image.BufferedImage loadSpriteImage(String path) {
        if (path == null || path.isBlank()) return null;
        java.awt.image.BufferedImage cached = SPRITE_CACHE.get(path);
        if (cached != null) {
            return cached;
        }
        try {
            java.io.File f = new java.io.File(path);
            if (!f.isFile() || f.length() == 0L) return null;
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
            if (img == null) return null;
            java.awt.image.BufferedImage fitted = fitSprite(img);
            if (SPRITE_CACHE.size() > 8) {
                SPRITE_CACHE.clear(); // kucuk tablo; buyumesi imkansiz
            }
            SPRITE_CACHE.put(path, fitted);
            return fitted;
        } catch (Throwable t) {
            return null;
        }
    }

    /** Gorseli CAT_W x CAT_H kutusuna orani koruyarak sigdirir (hdc arka plan yok). */
    private static java.awt.image.BufferedImage fitSprite(java.awt.image.BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        if (w <= 0 || h <= 0) return img;
        double scale = Math.min((double) EGG_SPRITE_W / w, (double) EGG_SPRITE_H / h);
        int tw = Math.max(1, (int) Math.round(w * scale));
        int th = Math.max(1, (int) Math.round(h * scale));
        java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(EGG_SPRITE_W, EGG_SPRITE_H, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, (EGG_SPRITE_W - tw) / 2, EGG_SPRITE_H - th, tw, th, null); // alt hizali (ayaklar yerde)
        g.dispose();
        return out;
    }

    /**
     * V39.1: kedi easter egg modu satiri.
     * Kombosu: Kedi (varsayilan) / Tavuk / Foto. "Foto" secilirse gorsel
     * sprite olarak kosar; Seç... butonu ile dosya secilir.
     */
    /**
     * V39.2: kedi easter egg modu satiri — JToggleButton uclusu (combo
     * yerine; bazi LAF'larda combo acilmasi cokmeye neden olabiliyordu).
     * Kedi / Tavuk / Foto. "Foto" secilirse gorsel sprite olarak kosar;
     * buyukluk slider'i ile ayarlanir.
     */
    private JPanel buildEggModeRow() {
        JPanel row = new JPanel(new FlowLayout(0, 8, 0));
        row.setOpaque(false);
        boolean en = L10n.isEnglish();
        javax.swing.JToggleButton catBtn = UiFx.toggleButton(en ? "Cat" : "Kedi");
        javax.swing.JToggleButton photoBtn = UiFx.toggleButton(en ? "Photo" : "Foto");
        javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();
        group.add(catBtn);
        group.add(photoBtn);
        String mode = this.settings.eggMode == null ? "cat" : this.settings.eggMode;
        ("photo".equalsIgnoreCase(mode) || "chicken".equalsIgnoreCase(mode) ? photoBtn : catBtn).setSelected(true);
        // V39.5: tavuk modu kaldirildi — yalnizca Kedi (animasyonlu) ve
        // Foto (fareyle suruklenen sabit sprite). Secim sessizce kaydedilir.
        Runnable applyMode = () -> {
            String m = catBtn.isSelected() ? "cat" : "photo";
            if (!m.equals(this.settings.eggMode)) {
                this.settings.eggMode = m;
                this.settings.save();
            }
        };
        catBtn.addActionListener(e -> applyMode.run());
        photoBtn.addActionListener(e -> applyMode.run());
        javax.swing.JLabel status = UiFx.label(this.settings.eggPhotoPath != null && !this.settings.eggPhotoPath.isBlank()
            ? (en ? "\u2713 photo set" : "\u2713 fotograf secili")
            : (en ? "no photo" : "fotograf yok"));
        status.setForeground(this.settings.eggPhotoPath != null && !this.settings.eggPhotoPath.isBlank() ? Theme.ACCENT_BRIGHT : Theme.TEXT_MUTED);
        status.setFont(status.getFont().deriveFont(11.0f));
        javax.swing.JButton choose = UiFx.ghostButton(en ? "Choose photo..." : "Fotograf sec...");
        choose.addActionListener(e -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            // V39.4: webp de destekleniyor (TwelveMonkeys jar icinde).
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                en ? "Images (png, jpg, jpeg, gif, bmp, webp)" : "G\u00f6rseller (png, jpg, jpeg, gif, bmp, webp)",
                "png", "jpg", "jpeg", "gif", "bmp", "webp"));
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File f = fc.getSelectedFile();
                if (f != null && f.isFile()) {
                    this.settings.eggPhotoPath = f.getAbsolutePath();
                    photoBtn.setSelected(true);
                    this.settings.eggMode = "photo";
                    this.settings.save();
                    status.setText(en ? "\u2713 photo set" : "\u2713 fotograf secili");
                    status.setForeground(Theme.ACCENT_BRIGHT);
                }
            }
        });
        // V39.2: sprite buyuklugu slider'i (50..200)
        javax.swing.JLabel scaleLbl = UiFx.label(en ? "Size:" : "Boyut:");
        scaleLbl.setFont(scaleLbl.getFont().deriveFont(11.0f));
        javax.swing.JSlider scaleSlider = new javax.swing.JSlider(50, 400, java.lang.Math.max(50, java.lang.Math.min(400, this.settings.eggPhotoScale <= 0 ? 100 : this.settings.eggPhotoScale)));
        scaleSlider.setMajorTickSpacing(50);
        scaleSlider.setPaintTicks(true);
        scaleSlider.setPreferredSize(new Dimension(120, 26));
        scaleSlider.setOpaque(false);
        scaleSlider.setToolTipText(en ? "Sprite size (100% = default)" : "Sprite b\u00fcy\u00fckl\u00fcf\u00fc (%100 = varsay\u0131lan)");
        scaleSlider.addChangeListener(e -> {
            this.settings.eggPhotoScale = scaleSlider.getValue();
            this.settings.save();
        });
        // V39.5 KONUM: kaydirici kaldirildi — sprite LOGO TIKLANINCA acilir
        // ve FAREYLE SURUKLENEREK konumlandirilir; birakilinca kaydedilir.
        javax.swing.JLabel posHint = UiFx.label(en ? "Drag sprite to move" : "Sprite'i s\u00fcr\u00fckleyerek yerle\u015ftir");
        posHint.setFont(posHint.getFont().deriveFont(11.0f));
        posHint.setForeground(Theme.TEXT_MUTED);
        row.add(catBtn);
        row.add(photoBtn);
        row.add(choose);
        row.add(scaleLbl);
        row.add(scaleSlider);
        row.add(posHint);
        row.add(status);
        return row;
    }

    /**
     * V36.1: sec + sifirla butonlarindan olusan marka satiri.
     * kind: "logo" | "bg" | "photo"
     */
    /**
     * V41: Yagan foto satiri — coklu dosya secimi (| ile birlestirilir),
     * yogunluk kaydiricisi (0-100). Secim aninda katmana uygulanir.
     */
    private JPanel buildFallingPhotosRow() {
        JPanel row = new JPanel(new FlowLayout(0, 8, 0));
        row.setOpaque(false);
        boolean en = L10n.isEnglish();
        javax.swing.JLabel status = UiFx.label("");
        javax.swing.JSlider density = new javax.swing.JSlider(0, 100, Math.max(0, Math.min(100, this.settings.fallingPhotoDensity)));
        density.setOpaque(false);
        density.setPreferredSize(new Dimension(140, 22));
        javax.swing.JLabel dl = UiFx.label((en ? "Intensity: " : "Yo\u011funluk: ") + density.getValue() + "%");
        dl.setFont(dl.getFont().deriveFont(11.0f));
        Runnable updateStatus = () -> {
            boolean on = this.settings.fallingPhotoPaths != null && !this.settings.fallingPhotoPaths.isBlank()
                && this.settings.fallingPhotoDensity > 0;
            status.setText(on
                ? (en ? "\u2713 " + this.settings.fallingPhotoPaths.split("\\|").length + " photo(s) falling"
                      : "\u2713 " + this.settings.fallingPhotoPaths.split("\\|").length + " foto yag\u0131yor")
                : (en ? "off" : "kapal\u0131"));
            status.setForeground(on ? Theme.ACCENT_BRIGHT : Theme.TEXT_MUTED);
        };
        javax.swing.JButton choose = UiFx.ghostButton(en ? "Choose…" : "Seç…");
        choose.addActionListener(e -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setMultiSelectionEnabled(true);
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                en ? "Images (png, jpg, jpeg, gif, bmp, webp)" : "G\u00f6rseller (png, jpg, jpeg, gif, bmp, webp)",
                "png", "jpg", "jpeg", "gif", "bmp", "webp"));
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File[] fs = fc.getSelectedFiles();
                if (fs != null && fs.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (java.io.File f : fs) {
                        if (sb.length() > 0) {
                            sb.append('|');
                        }
                        sb.append(f.getAbsolutePath());
                    }
                    this.settings.fallingPhotoPaths = sb.toString();
                    this.settings.save();
                    this.applyFallingPhotosFromSettings();
                    updateStatus.run();
                    this.log((en ? "Falling photos set (" : "Ya\u011fan fotolar ayarland\u0131 (") + fs.length + ")");
                }
            }
        });
        javax.swing.JButton reset = UiFx.ghostButton(en ? "Reset" : "S\u0131f\u0131rla");
        reset.addActionListener(e -> {
            this.settings.fallingPhotoPaths = "";
            this.settings.save();
            this.applyFallingPhotosFromSettings();
            updateStatus.run();
            this.log(en ? "Falling photos off." : "Ya\u011fan fotolar kapat\u0131ld\u0131.");
        });
        density.addChangeListener(ev -> {
            this.settings.fallingPhotoDensity = density.getValue();
            this.settings.save();
            dl.setText((en ? "Intensity: " : "Yo\u011funluk: ") + density.getValue() + "%");
            this.applyFallingPhotosFromSettings();
        });
        updateStatus.run();
        row.add(choose);
        row.add(reset);
        row.add(density);
        row.add(dl);
        row.add(status);
        return row;
    }

    private JPanel brandingRow(String kind) {
        JPanel row = new JPanel(new FlowLayout(0, 8, 0));
        row.setOpaque(false);
        boolean en = L10n.isEnglish();
        String current = switch (kind) {
            case "logo" -> this.settings.customLogoPath;
            case "bg" -> this.settings.homeBgPath;
            case "appbg" -> this.settings.appBgPath;
            default -> this.settings.accountPhotoPath;
        };
        javax.swing.JLabel status = UiFx.label(current != null && !current.isBlank()
            ? (en ? "✓ custom image set" : "✓ özel görsel seçili")
            : (en ? "default" : "varsayılan"));
        status.setForeground(current != null && !current.isBlank() ? Theme.ACCENT_BRIGHT : Theme.TEXT_MUTED);
        status.setFont(status.getFont().deriveFont(11.0f));
        javax.swing.JButton choose = UiFx.ghostButton(en ? "Choose…" : "Seç…");
        choose.addActionListener(e -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                en ? "Images (png, jpg, jpeg, gif, bmp)" : "Görseller (png, jpg, jpeg, gif, bmp)",
                "png", "jpg", "jpeg", "gif", "bmp"));
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File f = fc.getSelectedFile();
                if (f != null && f.isFile()) {
                    if ("logo".equals(kind)) {
                        this.settings.customLogoPath = f.getAbsolutePath();
                        LogoRes.setCustomPath(f.getAbsolutePath());
                    }
                    else if ("bg".equals(kind)) {
                        this.settings.homeBgPath = f.getAbsolutePath();
                    }
                    else if ("appbg".equals(kind)) {
                        this.settings.appBgPath = f.getAbsolutePath();
                    }
                    else {
                        this.settings.accountPhotoPath = f.getAbsolutePath();
                    }
                    this.settings.save();
                    this.refreshBranding();
                    status.setText(en ? "✓ custom image set" : "✓ özel görsel seçili");
                    status.setForeground(Theme.ACCENT_BRIGHT);
                    this.log((en ? "Custom image set: " : "Özel görsel seçildi: ") + f.getName());
                }
            }
        });
        javax.swing.JButton reset = UiFx.ghostButton(en ? "Reset" : "Sıfırla");
        reset.addActionListener(e -> {
            if ("logo".equals(kind)) {
                this.settings.customLogoPath = "";
                LogoRes.setCustomPath("");
            }
            else if ("bg".equals(kind)) {
                this.settings.homeBgPath = "";
            }
            else if ("appbg".equals(kind)) {
                this.settings.appBgPath = "";
            }
            else {
                this.settings.accountPhotoPath = "";
            }
            this.settings.save();
            this.refreshBranding();
            status.setText(en ? "default" : "varsayılan");
            status.setForeground(Theme.TEXT_MUTED);
            this.log(en ? "Custom image reset to default." : "Özel görsel varsayılana alındı.");
        });
        // V38: fotodan tema ureteci - secili gorselden (yoksa dosya secimi)
        // palet cikarip ozel tema olarak uygular.
        javax.swing.JButton themeBtn = UiFx.ghostButton(en ? "\uD83C\uDFA8 Theme from photo" : "\uD83C\uDFA8 Fotodan tema");
        themeBtn.setToolTipText(en
            ? "Generate a theme from this photo (or pick one)"
            : "Bu fotodan tema uretir (secili yoksa dosya secer)");
        themeBtn.addActionListener(e -> {
            java.io.File src = (current != null && !current.isBlank()) ? new java.io.File(current) : null;
            if (src == null || !src.isFile()) {
                javax.swing.JFileChooser fc2 = new javax.swing.JFileChooser();
                fc2.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    en ? "Images (png, jpg, jpeg, gif, bmp)" : "G\u00f6rseller (png, jpg, jpeg, gif, bmp)",
                    "png", "jpg", "jpeg", "gif", "bmp"));
                if (fc2.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
                src = fc2.getSelectedFile();
                if (src == null || !src.isFile()) return;
            }
            try {
                CustomTheme t = ThemeFromPhoto.fromFile(src);
                t.save();
                this.applyThemeLive(() -> Theme.applyCustom(t));
                javax.swing.SwingUtilities.updateComponentTreeUI(this);
                this.onCustomThemeApplied();
                this.refreshThemeChrome();
                this.log((en ? "Theme generated from photo: " : "Fotodan tema uretildi: ") + src.getName());
            }
            catch (Exception ex) {
                this.log((en ? "Theme generation failed: " : "Tema uretilemedi: ") + ex.getMessage());
            }
        });
        row.add(choose);
        row.add(reset);
        row.add(themeBtn);
        row.add(status);
        return row;
    }

    /** V36.1: logo/arka plan/foto degistiginde tum yuzeyleri canli tazele. */
    private void refreshBranding() {
        try {
            // ust-sol logo + pencere ikonu
            if (this.sidebarLogoLabel != null) {
                this.sidebarLogoLabel.setIcon(MainWindow.makeLogo());
            }
            try {
                Object master = LogoRes.master();
                if (master != null) {
                    this.setIconImage((Image)master);
                }
            }
            catch (Exception ignored) {
            }
            // V40: tum pencere arka plan fotorafi
            String appBg = this.settings.appBgPath;
            if (appBg != null && !appBg.isBlank()) {
                try {
                    java.io.File af = new java.io.File(appBg);
                    this.appBgImage = af.isFile() ? javax.imageio.ImageIO.read(af) : null;
                }
                catch (Exception ignored) {
                    this.appBgImage = null;
                }
            }
            else {
                this.appBgImage = null;
            }
            // ana sayfa arka plan fotorafi
            String bgPath = this.settings.homeBgPath;
            if (bgPath != null && !bgPath.isBlank()) {
                try {
                    java.io.File f = new java.io.File(bgPath);
                    this.homeBgImage = f.isFile() ? javax.imageio.ImageIO.read(f) : null;
                }
                catch (Exception ignored) {
                    this.homeBgImage = null;
                }
            }
            else {
                this.homeBgImage = null;
            }
            this.accountPhotoImage = null;
            this.accountPhotoLoadedPath = "";
            // V36.3: cache reset sonrasi fotoyu HEMEN yeniden yukle ki ilk
            // karede bos daire gorunmesin.
            this.loadAccountPhoto();
            if (this.homeRoot != null) {
                this.homeRoot.repaint();
            }
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (Throwable t) {
            // marka yenilemesi asla launcher'i dusurmesin
        }
    }

    private static ImageIcon makeAvatar(String string) {
        int n = 36;
        BufferedImage bufferedImage = new BufferedImage(n, n, 2);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(Theme.BG_ELEVATED);
        graphics2D.fillOval(0, 0, n, n);
        graphics2D.setColor(Theme.ACCENT);
        graphics2D.setFont(new Font("SansSerif", 1, 16));
        String string2 = string == null || string.isEmpty() ? "?" : string.substring(0, 1).toUpperCase();
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        graphics2D.drawString(string2, (n - fontMetrics.stringWidth(string2)) / 2, (n + fontMetrics.getAscent()) / 2 - 2);
        graphics2D.dispose();
        return new ImageIcon(bufferedImage);
    }

    private void applyThemeAndColor() {
        try {
            this.applyThemeLive(() -> {
                if ("Custom".equalsIgnoreCase(this.settings.theme)) {
                    Theme.applyCustom(CustomTheme.load());
                } else {
                    Theme.apply(this.settings.theme);
                }
            });
            this.getRootPane().putClientProperty("JRootPane.titleBarBackground", Theme.TITLE_BAR);
            this.getRootPane().putClientProperty("JRootPane.titleBarForeground", Theme.TEXT_PRIMARY);
            this.getRootPane().putClientProperty("TitlePane.background", Theme.TITLE_BAR);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    /**
     * V29.1 CANLI TEMA GUNCELLEME + V34.6 TAM COZUM: Theme.apply() static
     * renkleri yeniler ama yapim sirasinda kopyalanan renkleri degistiremez.
     * Eski yontem yalnizca BIREBIR ayni Color nesnesini tasiyan bilesenleri
     * yakaliyordu; oysa UiFx.withAlpha(ACCENT,60), .brighter()/.darker(),
     * new Color(r,g,b) gibi PALETTEN TURETILMIS renkler yeni nesne oldugu
     * icin "hardcoded" kaliyordu. V34.6: kimlik eslesmesine ek olarak her
     * renk EN YAKIN ESKI PALET GIRISINE eslenir (RGB mesafesi, esik altinda)
     * ve yeni palet rengi alfa korunarak yazilir. Boylece tum tureyenler
     * de tema ile birlikte guncellenir.
     */
    /** Paket ici (ThemeCreatorDialog) cagirilar icin de acik. */
    /**
     * V35.1: Modern tasarim modunda sekme iceriklerini seffaf yapar.
     * FlatLaf JTabbedPane + JPanel'ler opak olup BG_SURFACE/BG_BASE ile
     * dolduruyor; efektin gorunmesi icin kok panellerin opakligi kapatilir
     * ve scroll viewport'lari seffafLANir. Kartlar (buildCard) KORUNUR:
     * kartin kendi animasyonlu gradyani var, onu bozmuyoruz.
     */
    private static void makeTabContentTransparent(java.awt.Container root) {
        if (root == null) {
            return;
        }
        for (int i = 0; i < root.getComponentCount(); i++) {
            java.awt.Component ch = root.getComponent(i);
            if (ch instanceof JComponent jc) {
                boolean isInteractive = jc instanceof javax.swing.JButton
                    || jc instanceof javax.swing.JCheckBox
                    || jc instanceof javax.swing.JComboBox
                    || jc instanceof javax.swing.JList
                    || jc instanceof javax.swing.JTextField
                    || jc instanceof javax.swing.JFormattedTextField
                    || jc instanceof javax.swing.JTextArea
                    || jc instanceof javax.swing.JEditorPane
                    || jc instanceof javax.swing.JProgressBar
                    || jc instanceof javax.swing.JSlider
                    || jc instanceof javax.swing.JToggleButton
                    || jc instanceof javax.swing.JTable
                    || jc instanceof javax.swing.JTree
                    || jc instanceof javax.swing.JSpinner
                    || jc instanceof javax.swing.JPopupMenu
                    || jc instanceof javax.swing.JComboBox;
                if (!isInteractive) {
                    // Kartlar (isOpaque=false override) kendi boyamalarini
                    // korur; setOpaque(false) onlarda sadece 'düz arkaplan
                    // doldurma'yi kaplar - görsel kayip yok.
                    jc.setOpaque(false);
                }
            }
            if (ch instanceof javax.swing.JScrollPane sp) {
                sp.getViewport().setOpaque(false);
                sp.setOpaque(false);
            }
            if (ch instanceof java.awt.Container c2) {
                MainWindow.makeTabContentTransparent(c2);
            }
        }
    }

    /**
     * V41.2: makeTabContentTransparent'in karsiti — classic moda donuste
     * interaktif olmayan bilesenlerin opakligini FlatLaf varsayilanina
     * geri getirir. (FlatLaf UI ile yeniden kurulum yapmadan.) Bilesenin
     * kendi ui degeri korunur; sadece opaklik bayragi eski haline döner.
     */
    private static void makeTabContentOpaque(java.awt.Container root) {
        if (root == null) {
            return;
        }
        for (int i = 0; i < root.getComponentCount(); i++) {
            java.awt.Component ch = root.getComponent(i);
            if (ch instanceof JComponent jc) {
                boolean isInteractive = jc instanceof javax.swing.JButton
                    || jc instanceof javax.swing.JCheckBox
                    || jc instanceof javax.swing.JComboBox
                    || jc instanceof javax.swing.JList
                    || jc instanceof javax.swing.JTextField
                    || jc instanceof javax.swing.JFormattedTextField
                    || jc instanceof javax.swing.JTextArea
                    || jc instanceof javax.swing.JEditorPane
                    || jc instanceof javax.swing.JProgressBar
                    || jc instanceof javax.swing.JSlider
                    || jc instanceof javax.swing.JToggleButton
                    || jc instanceof javax.swing.JTable
                    || jc instanceof javax.swing.JTree
                    || jc instanceof javax.swing.JSpinner
                    || jc instanceof javax.swing.JPopupMenu;
                if (!isInteractive) {
                    jc.setOpaque(true);
                }
            }
            if (ch instanceof javax.swing.JScrollPane sp) {
                sp.getViewport().setOpaque(true);
                sp.setOpaque(true);
            }
            if (ch instanceof java.awt.Container c2) {
                MainWindow.makeTabContentOpaque(c2);
            }
        }
    }

    void applyThemeLive(Runnable themeApplyAction) {

        try {
            java.util.Map<String, Color> before = new java.util.HashMap<>();
            for (java.lang.reflect.Field f : Theme.class.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) && f.getType() == Color.class) {
                    f.setAccessible(true);
                    Color c = (Color)f.get(null);
                    if (c != null) {
                        before.put(f.getName(), c);
                    }
                }
            }
            // Uygulama komutunu calistir (Theme.apply / applyCustom).
            themeApplyAction.run();
            // Eski->yeni haritasi: hem kimlik hem palet-tabanli.
            java.util.IdentityHashMap<Color, Color> oldToNew = new java.util.IdentityHashMap<>();
            java.util.ArrayList<Color> oldPal = new java.util.ArrayList<>();
            java.util.ArrayList<Color> newPal = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, Color> en : before.entrySet()) {
                try {
                    java.lang.reflect.Field f = Theme.class.getDeclaredField(en.getKey());
                    f.setAccessible(true);
                    Color now = (Color)f.get(null);
                    if (now != null) {
                        if (now != en.getValue()) {
                            oldToNew.put(en.getValue(), now);
                        }
                        oldPal.add(en.getValue());
                        newPal.add(now);
                    }
                }
                catch (Exception ignore) {
                    // alan yoksa yok say
                }
            }
            if (oldToNew.isEmpty()) {
                // V38 FIX: renk degismese bile (orn. ayni palet) font
                // degismis olabilir - fontlari yine tazele.
                    refreshTreeFonts(this);
                for (java.awt.Window w : java.awt.Window.getWindows()) {
                    if (w != this && w.isDisplayable()) {
                        refreshTreeFonts(w);
                    }
                }
                return;
            }
            RethemeCtx ctx = new RethemeCtx(oldToNew, oldPal, newPal);
            rethemeTree(this, ctx);
            // Acik pencereler de (Tema Olusturucu vb.) tazelensin.
            for (java.awt.Window w : java.awt.Window.getWindows()) {
                if (w != this && w.isDisplayable()) {
                    rethemeTree(w, ctx);
                }
            }
            // V38 FIX: font ailesi/boyutu degistiyse tum agaci tazele.
            // (Renk haritasi bos donse bile font degismis olabilir.)
            refreshTreeFonts(this);
            for (java.awt.Window w : java.awt.Window.getWindows()) {
                if (w != this && w.isDisplayable()) {
                    refreshTreeFonts(w);
                }
            }
            this.repaint();
        }
        catch (Throwable ignore) {
            // tema guncellemesi asla uygulamayi dusurmesin
        }
    }

    /**
     * V34.6: yeniden tema baglami. Once KIMLIK eslestirmesi (ayni nesne),
     * sonra EN YAKIN ESKI PALET girisi (yalnizca RGB; esik ~60/kanal).
     * Eslesen tureyen, yeni palet renginin RGB'siyle + KENDI alfasıyla
     * yazilir: withAlpha(ACCENT,60) -> withAlpha(yeniACCENT,60).
     * Zaten yeni paletin RGB'sini tasiyan renklere dokunulmaz.
     */
    private static final class RethemeCtx {
        final java.util.IdentityHashMap<Color, Color> exact;
        final java.util.ArrayList<Color> oldPal;
        final java.util.ArrayList<Color> newPal;

        RethemeCtx(java.util.IdentityHashMap<Color, Color> ex, java.util.ArrayList<Color> o, java.util.ArrayList<Color> n) {
            this.exact = ex;
            this.oldPal = o;
            this.newPal = n;
        }

        Color map(Color c) {
            if (c == null) {
                return null;
            }
            Color e = this.exact.get(c);
            if (e != null) {
                return e;
            }
            for (int i = 0; i < this.newPal.size(); i++) {
                if (this.newPal.get(i).getRGB() == c.getRGB()) {
                    return c;
                }
            }
            int best = -1;
            double bd = 60.0 * 60.0 * 3.0;
            for (int i = 0; i < this.oldPal.size(); i++) {
                Color o = this.oldPal.get(i);
                double dr = o.getRed() - c.getRed();
                double dg = o.getGreen() - c.getGreen();
                double db = o.getBlue() - c.getBlue();
                double d = dr * dr + dg * dg + db * db;
                if (d < bd) {
                    bd = d;
                    best = i;
                }
            }
            if (best < 0) {
                return c;
            }
            Color n = this.newPal.get(best);
            return new Color(n.getRed(), n.getGreen(), n.getBlue(), c.getAlpha());
        }
    }

    /** V38 FIX: tema degisince tum agactaki bilesen fontlarini UIManager
     *  degerleriyle tazele. updateComponentTreeUI yalnizca font'u OZEL
     *  SET EDILMEMIS bilesenleri gunceller; deriveFont() ile ozel font
     *  verilmis yuzlerce bilesen eski aile/boyutta kaliyordu. Boyut/ağırlık
     *  korunur, aile+taban boyut yeni temadan gelir. */
    private static void refreshTreeFonts(java.awt.Container root) {
        if (root == null) return;
        try {
            Font base = UIManager.getFont("Label.font");
            if (base == null) return;
            java.util.Deque<Component> q = new java.util.ArrayDeque<>();
            q.add(root);
            while (!q.isEmpty()) {
                Component c = q.poll();
                if (c instanceof JComponent jc && jc.getFont() != null) {
                    Font oldF = jc.getFont();
                    // Aile + taban boyut: UIManager'dan; style: eski fonttan.
                    // Yalnizca gercekten degistiyse set et (repaint maliyeti).
                    Font newF = oldF.getFamily().equals(base.getFamily()) && oldF.getSize() == base.getSize()
                        ? oldF
                        : new Font(base.getFamily(), oldF.getStyle(), Math.max(9, oldF.getSize() * base.getSize() / 12));
                    if (newF != oldF && !newF.equals(oldF)) {
                        jc.setFont(newF);
                    }
                }
                if (c instanceof Container cn) for (Component ch : cn.getComponents()) q.add(ch);
            }
        } catch (Exception ignore) {
        }
    }

    private static void rethemeTree(java.awt.Container root, RethemeCtx ctx) {
        if (root == null) {
            return;
        }
        Color fg2 = ctx.map(root.getForeground());
        if (fg2 != root.getForeground()) {
            root.setForeground(fg2);
        }
        Color bg2 = ctx.map(root.getBackground());
        if (bg2 != root.getBackground()) {
            root.setBackground(bg2);
        }
        if (root instanceof javax.swing.JComponent jc) {
            javax.swing.border.Border b = jc.getBorder();
            javax.swing.border.Border nb = rethemeBorder(b, ctx, 0);
            if (nb != b) {
                jc.setBorder(nb);
            }
        }
        for (int i = 0; i < root.getComponentCount(); i++) {
            java.awt.Component ch = root.getComponent(i);
            Color cfg = ctx.map(ch.getForeground());
            if (cfg != ch.getForeground()) {
                ch.setForeground(cfg);
            }
            Color cbg = ctx.map(ch.getBackground());
            if (cbg != ch.getBackground()) {
                ch.setBackground(cbg);
            }
            if (ch instanceof javax.swing.JComponent jc2) {
                javax.swing.border.Border b2 = jc2.getBorder();
                javax.swing.border.Border nb2 = rethemeBorder(b2, ctx, 0);
                if (nb2 != b2) {
                    jc2.setBorder(nb2);
                }
            }
            if (ch instanceof java.awt.Container c2) {
                rethemeTree(c2, ctx);
            }
            if (ch instanceof javax.swing.JComboBox combo) {
                javax.swing.ListCellRenderer r = combo.getRenderer();
                if (r instanceof java.awt.Component rc) {
                    Color rcfg = ctx.map(rc.getForeground());
                    if (rcfg != rc.getForeground()) {
                        rc.setForeground(rcfg);
                    }
                    Color rcbg = ctx.map(rc.getBackground());
                    if (rcbg != rc.getBackground()) {
                        rc.setBackground(rcbg);
                    }
                }
            }
        }
    }

    private static javax.swing.border.Border rethemeBorder(javax.swing.border.Border b, RethemeCtx ctx, int depth) {
        if (b == null || depth > 4) {
            return b;
        }
        if (b instanceof javax.swing.border.CompoundBorder cb) {
            javax.swing.border.Border outside = rethemeBorder(cb.getOutsideBorder(), ctx, depth + 1);
            javax.swing.border.Border inside = rethemeBorder(cb.getInsideBorder(), ctx, depth + 1);
            if (outside != cb.getOutsideBorder() || inside != cb.getInsideBorder()) {
                return javax.swing.BorderFactory.createCompoundBorder(outside, inside);
            }
            return b;
        }
        if (b instanceof javax.swing.border.LineBorder lb) {
            Color mapped = ctx.map(lb.getLineColor());
            if (mapped != lb.getLineColor()) {
                return new javax.swing.border.LineBorder(mapped, lb.getThickness(), lb.getRoundedCorners());
            }
        }
        return b;
    }

    /** Tema Olusturucu kaydet-uygula yaptiginda cagrilir: ayari "Custom"
     *  olarak isaretler, kombayi gunceller. */
    void onCustomThemeApplied() {
        try {
            this.settings.theme = "Custom";
            this.settings.save();
            if (this.themeCombo != null) {
                this.themeCombo.setSelectedItem(L10n.isEnglish() ? "\u2605 Custom" : "\u2605 Ozel");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    /**
     * V41 CANLI TASARIM GECISI: restart YOK. Motor durumu guncellenir,
     * FX-kisilik host ayni kali; classic'te motor durur, diger modlarda
     * (modern/minecraft/herobrine/creeper/nether) timer ayni host'u boyar.
     */
    public void applyDesignMode(String mode) {
        if (mode == null || mode.isBlank()) {
            mode = "classic";
        }
        this.settings.designMode = mode;
        this.settings.save();
        boolean fx = BackgroundFx.isFxMode(mode);
        if (fx) {
            if (BackgroundFx.isRunning()) {
                BackgroundFx.setMode(mode);
            } else {
                BackgroundFx.start(this.getRootPane(), mode);
            }
        } else {
            BackgroundFx.stop();
        }
        // V41.2 IKI YONLU OPACITY SENKRONU: FX'e gecerken icerik seffaflastirilir,
        // classic'e donerken FlatLaf'in opak arka planlari GERI getirilir.
        // Tek yonlu gecis yuzunden classic'e donus "eski temali" gorunuyordu.
        if (this.tabs != null) {
            if (fx) {
                MainWindow.makeTabContentTransparent(this.tabs);
            } else {
                MainWindow.makeTabContentOpaque(this.tabs);
            }
            this.tabs.repaint();
        }
        // host + tum agac tazelemesi (app-bg fotorafi ve FX aninda degisir)
        if (this.getRootPane() != null) {
            this.getRootPane().repaint();
        }
        javax.swing.SwingUtilities.updateComponentTreeUI(this);
        boolean en = L10n.isEnglish();
        this.log((en ? "Design mode: " : "Tasarim modu: ") + mode);
    }

    /** V41: yagan foto katmanini ayarlardan (yeniden) yukler. */
    public void applyFallingPhotosFromSettings() {
        try {
            java.util.List<String> paths = new java.util.ArrayList<>();
            String raw = this.settings.fallingPhotoPaths == null ? "" : this.settings.fallingPhotoPaths;
            for (String p : raw.split("\\|")) {
                if (p != null && !p.isBlank()) {
                    paths.add(p.trim());
                }
            }
            BackgroundFx.setFallingPhotos(paths, this.settings.fallingPhotoDensity);
        }
        catch (Exception ignored) {
        }
    }

    /** V41: ThemeCreatorDialog'un ayarlara okuma/yazma erisimi. */
    public Settings settings() {
        return this.settings;
    }

    /** Tema degistikten sonra baslik cubugu/krom renklerini tazeler. */
    void refreshThemeChrome() {
        try {
            this.getRootPane().putClientProperty("JRootPane.titleBarBackground", Theme.TITLE_BAR);
            this.getRootPane().putClientProperty("JRootPane.titleBarForeground", Theme.TEXT_PRIMARY);
            this.getRootPane().putClientProperty("TitlePane.background", Theme.TITLE_BAR);
        }
        catch (Exception exception) {
            // empty catch block
        }
        // V35.1: Modern mod tekrar etkinse seffaflik tazele (tema gecisi
        // bilesenleri eski opakliga dondurebilir).
        if (BackgroundFx.isFxMode(this.settings.designMode) && this.tabs != null) {
            MainWindow.makeTabContentTransparent(this.tabs);
        }
    }

    /** Verilen isimdeki temayi canli uygular (Tema Olusturucu preset
     *  yukleme akisi icin). Ayar dosyasini DEGISTIRMEZ. */
    void applyThemeByName(String name) {
        try {
            this.applyThemeLive(() -> {
                if ("Custom".equalsIgnoreCase(name)) {
                    Theme.applyCustom(CustomTheme.load());
                } else {
                    Theme.apply(name);
                }
            });
            this.refreshThemeChrome();
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static boolean isDark(Color color) {
        return 0.299 * (double)color.getRed() + 0.587 * (double)color.getGreen() + 0.114 * (double)color.getBlue() < 128.0;
    }

    private Color cardBackground() {
        Color color = UIManager.getColor("Panel.background");
        if (color == null) {
            return Theme.BG_SURFACE;
        }
        return MainWindow.isDark(color) ? color.brighter() : color.darker();
    }

    private Color cardForeground() {
        Color color = UIManager.getColor("Label.foreground");
        return color != null ? color : Theme.TEXT_PRIMARY;
    }

    private void log(String string) {
        SwingUtilities.invokeLater(() -> {
            this.logArea.append(string + "\n");
            this.logArea.setCaretPosition(this.logArea.getDocument().getLength());
        });
    }

    private void autoUpdateAllForVersion(String string) {
        if (this.currentInstance == null) {
            return;
        }
        String string2 = this.currentLoader();
        File file = this.currentInstance.modsDir();
        try {
            this.autoInstallForLoader(string2, file, string);
        }
        catch (Exception exception) {
            this.log("Otomatik mod g\u00fcncelleme hatas\u0131: " + exception.getMessage());
        }
        try {
            ArrayList<ModManager.InstalledMod> arrayList = new ArrayList<ModManager.InstalledMod>(ModManager.loadRegistry(file));
            int n = 0;
            ArrayList<String> arrayList2 = new ArrayList<String>();
            ArrayList<String> arrayList3 = new ArrayList<String>();
            for (ModManager.InstalledMod installedMod : arrayList) {
                if (installedMod.projectId == null || installedMod.projectId.isBlank()) continue;
                String modLabel = installedMod.projectTitle != null && !installedMod.projectTitle.isBlank() ? installedMod.projectTitle : installedMod.fileName;
                try {
                    ModrinthApi.ModVersion picked;
                    SwingUtilities.invokeLater(() -> this.onAutoUpdateModStart(modLabel));
                    List<ModrinthApi.ModVersion> list = ModrinthApi.getVersions(installedMod.projectId, string2, string);
                    if (list.isEmpty()) {
                        this.log(" " + modLabel + " \u2014 " + string + "ile uyumlu s\u00fcr\u00fcm yok");
                        arrayList2.add(modLabel);
                        continue;
                    }
                    ArrayList<ModrinthApi.ModVersion> arrayList4 = new ArrayList<ModrinthApi.ModVersion>();
                    for (ModrinthApi.ModVersion modVersion2 : list) {
                        if (modVersion2.gameVersions == null || !modVersion2.gameVersions.contains(string)) continue;
                        arrayList4.add(modVersion2);
                    }
                    // V36.2 KOK COZUM: MC-surumu-filtreli liste bos ise HICBIR
                    // SEY KURULMAZ. Eskiden loader-filtreli ham liste kullaniliyordu
                    // -> baska MC surumu icin derlenmis jar sessizce kuruluyordu
                    // ("yanlis surumde indiriyor" bugunun kaynagi). Uyumlu surum
                    // yoksa eski jar diskte zaten yanlis surume ait - temizlenir.
                    if (arrayList4.isEmpty()) {
                        this.log(" " + modLabel + " — " + string + " ile uyumlu sürüm yok, atlandi");
                        arrayList2.add(modLabel);
                        try {
                            com.lubv.launcher.mods.ModManager.removeProjectFiles(file, arrayList, installedMod.projectId, null);
                            arrayList.removeIf(m2 -> installedMod.projectId != null && installedMod.projectId.equals(m2.projectId));
                            com.lubv.launcher.mods.ModManager.saveRegistry(file, arrayList);
                        } catch (Exception cleanupErr) { /* temizlik kurulumu engellemez */ }
                        continue;
                    }
                    List<ModrinthApi.ModVersion> list2 = arrayList4;
                    picked = ModrinthApi.pickBestVersion(list2);
                    if (picked == null || picked.versionNumber.equals(installedMod.versionNumber)) continue;
                    // V35.3: eskiden burada mod ONCEDEN siliniyordu; indirme
                    // yarida kalirsa mod tamamen kayboluyordu. installVersion
                    // zaten yeni jar indikten SONRA eski dosyalari degistirir -
                    // on-silme gereksiz ve tehlikeli, kaldirildi.
                    ModrinthApi.ModResult modResult = new ModrinthApi.ModResult();
                    modResult.slug = installedMod.projectId;
                    modResult.title = modLabel;
                    modResult.description = "";
                    modResult.iconUrl = installedMod.iconUrl != null ? installedMod.iconUrl : "";
                    modResult.downloads = 0;
                    modResult.categories = new ArrayList<String>();
                    ModManager.installWithDependencies(file, modResult, picked, string2, string, (arg_0, arg_1) -> this.onAutoUpdateModProgress(modLabel, arg_0, arg_1));
                    this.log("G\u00fcncellendi: " + modLabel + "  " + picked.versionNumber);
                    ++n;
                }
                catch (Exception exception) {
                    this.log(" " + modLabel + "g\u00fcncellenemedi: " + exception.getMessage());
                    arrayList3.add(modLabel);
                }
            }
            int n2 = n;
            List<String> failedList = new ArrayList<String>(arrayList2);
            List<String> errorList = new ArrayList<String>(arrayList3);
            SwingUtilities.invokeLater(() -> this.onAutoUpdateModDone(n2, string, failedList, errorList));
        }
        catch (Exception exception) {
            this.log("Mod g\u00fcncelleme hatas\u0131: " + exception.getMessage());
        }
        try {
            new ArrayList<String>(ShaderManager.listInstalled(this.currentInstance.shaderpacksDir()));
            SwingUtilities.invokeLater(() -> this.shaderPanel.refreshInstalled());
        }
        catch (Exception exception) {
            SwingUtilities.invokeLater(() -> this.shaderPanel.refreshInstalled());
        }
        SwingUtilities.invokeLater(() -> this.resourcepacksPanel.refreshInstalled());
        SwingUtilities.invokeLater(() -> {
            this.progressStageLabel.setText(" ");
            this.log("S\u00fcr\u00fcm g\u00fcncellemesi tamamland\u0131: " + string);
        });
    }

    private void checkForLauncherUpdates() {
        this.updatePanel.showChecking();
        this.log(L10n.get("update.checking"));
        UpdateManager.checkForUpdates(new UpdateManager.UpdateCallback(){

            @Override
            public void onStatus(String string) {
                SwingUtilities.invokeLater(() -> MainWindow.this.updatePanel.showDownloadProgress(string, 0));
            }

            @Override
            public void onProgress(int n) {
                SwingUtilities.invokeLater(() -> MainWindow.this.updatePanel.showDownloadProgress("\u0130ndiriliyor... " + n + "%", n));
            }

            @Override
            public void onError(String string) {
                SwingUtilities.invokeLater(() -> MainWindow.this.updatePanel.showError(string));
                MainWindow.this.log("G\u00fcncelleme kontrol\u00fc ba\u015far\u0131s\u0131z: " + string);
            }

            @Override
            public void onUpdateReady(UpdateManager.UpdateInfo updateInfo) {
                SwingUtilities.invokeLater(() -> {
                    MainWindow.this.updatePanel.showUpdateAvailable(updateInfo);
                    MainWindow.this.log(L10n.fmt("update.available", "v" + updateInfo.version));
                    if (updateInfo.changelog != null && !updateInfo.changelog.isEmpty()) {
                        MainWindow.this.log(L10n.fmt("update.changelog", updateInfo.changelog));
                    }
                });
            }

            @Override
            public void onUpdateComplete(boolean bl, String string) {
                SwingUtilities.invokeLater(() -> {
                    MainWindow.this.updatePanel.showComplete(string);
                    MainWindow.this.log(" " + string);
                    int n = JOptionPane.showConfirmDialog(MainWindow.this, string + "\nLauncher'\u0131 \u015fimdi yeniden ba\u015flat\u0131ls\u0131n m\u0131?", "G\u00fcncelleme Tamamland\u0131", 0);
                    if (n == 0) {
                        MainWindow.this.restartLauncher();
                    }
                });
            }
        });
    }

    private void doUpdate() {
        UpdateManager.UpdateInfo updateInfo = this.updatePanel.getPendingInfo();
        if (updateInfo == null) {
            return;
        }
        // GitHub release description'u (body) changelog olarak goster.
        // Cok uzun body'leri kisalt - dialog okunabilir kalsin.
        String changelogRaw = updateInfo.changelog != null ? updateInfo.changelog.trim() : "";
        if (changelogRaw.length() > 900) {
            changelogRaw = changelogRaw.substring(0, 900) + "\n...";
        }
        String changelogText = !changelogRaw.isEmpty() ? L10n.fmt("update.changelog", changelogRaw) + "\n" : "";
        int n = JOptionPane.showConfirmDialog(this, L10n.fmt("update.confirm", "v" + updateInfo.version, changelogText), L10n.get("update.confirm_title"), 0);
        if (n != 0) {
            return;
        }
        this.log(L10n.fmt("update.updating", "v" + updateInfo.version));
        this.updatePanel.showDownloadProgress("\u0130ndiriliyor...", 0);
        UpdateManager.downloadUpdate(updateInfo, new UpdateManager.UpdateCallback(){

            @Override
            public void onStatus(String string) {
                SwingUtilities.invokeLater(() -> MainWindow.this.updatePanel.showDownloadProgress(string, 0));
                MainWindow.this.log(string);
            }

            @Override
            public void onProgress(int n) {
                SwingUtilities.invokeLater(() -> MainWindow.this.updatePanel.showDownloadProgress("\u0130ndiriliyor... " + n + "%", n));
            }

            @Override
            public void onError(String string) {
                SwingUtilities.invokeLater(() -> MainWindow.this.updatePanel.showError(string));
                MainWindow.this.log(L10n.fmt("update.update_error", string));
            }

            @Override
            public void onUpdateReady(UpdateManager.UpdateInfo updateInfo) {
            }

            @Override
            public void onUpdateComplete(boolean bl, String string) {
                SwingUtilities.invokeLater(() -> {
                    MainWindow.this.updatePanel.showComplete(string);
                    MainWindow.this.log(" " + string);
                    int n = JOptionPane.showConfirmDialog(MainWindow.this, string + "\n" + L10n.get("update.restart_confirm"), L10n.get("update.restart_title"), 0);
                    if (n == 0) {
                        MainWindow.this.restartLauncher();
                    }
                });
            }
        });
    }

    private void restartLauncher() {
        try {
            String string = ProcessHandle.current().info().command().orElse("");
            if (!string.isEmpty() && string.toLowerCase().endsWith(".exe")) {
                new ProcessBuilder(string).start();
            } else {
                File file = Paths.getLauncherDir();
                File file2 = new File(file, "Complex-Launcher.jar");
                if (file2.exists()) {
                    String string2 = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                    new ProcessBuilder(string2, "-jar", file2.getAbsolutePath()).directory(file).start();
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        System.exit(0);
    }

    private void onAutoUpdateStep3(int n, String string, List<String> list, List<String> list2) {
        this.modsPanel.refreshInstalled();
        this.modsPanel.updateFilterLabel();
        this.progressStageLabel.setText(" ");
        if (n > 0) {
            this.log(" " + n + "mod g\u00fcncellendi  " + string);
        } else {
            this.log("T\u00fcm modlar zaten g\u00fcncel.");
        }
        if (!list.isEmpty() || !list2.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<html><body style='font-family:sans-serif;width:380px'>");
            stringBuilder.append("<b>Minecraft ").append(string).append("</b> s\u00fcr\u00fcm\u00fcne gecildi.<br><br>");
            if (!list.isEmpty()) {
                stringBuilder.append("<b style='color:#e05252'>").append(list.size()).append("mod bu s\u00fcr\u00fcmle uyumlu defil:</b><br><ul>");
                for (String string2 : list) {
                    stringBuilder.append("<li>").append(string2).append("</li>");
                }
                stringBuilder.append("</ul>");
                stringBuilder.append("Bu modlar devre d\u0131\u015f\u0131 b\u0131rak\u0131lmad\u0131 \u2014 ");
                stringBuilder.append("ancak oyun ba\u015flamayabilir veya hata verebilir.<br><br>");
            }
            if (!list2.isEmpty()) {
                stringBuilder.append("<b style='color:#d29922'>").append(list2.size()).append("mod g\u00fcncellenemedi (af hatas\u0131):</b><br><ul>");
                for (String string2 : list2) {
                    stringBuilder.append("<li>").append(string2).append("</li>");
                }
                stringBuilder.append("</ul>");
            }
            stringBuilder.append("</body></html>");
            JOptionPane.showMessageDialog(this, new JLabel(stringBuilder.toString()), "Uyumsuz Modlar \u2014 " + string, 2);
        }
    }

    private void onAutoUpdateStep1(String string, int n, String string2) {
        SwingUtilities.invokeLater(() -> this.progressStageLabel.setText(string + ": " + string2));
    }

    private void onAutoUpdateStep0(String string) {
        this.progressStageLabel.setText("Kontrol ediliyor: " + string);
    }

    private static void onShowCrashLog1(File file, JTextArea jTextArea, ActionEvent actionEvent) {
        try {
            Files.writeString(file.toPath(), (CharSequence)"", new OpenOption[0]);
            jTextArea.setText("(temizlendi)");
        }
        catch (Exception exception) {
            jTextArea.append("\nTemizleme hatas\u0131: " + exception.getMessage());
        }
    }

    private void onShowCrashLog0(JTextArea jTextArea, ActionEvent actionEvent) {
        this.analyzeCrashLog(jTextArea.getText());
    }

    private void onBuildHomeTab5(String string, ActionEvent actionEvent) {
        System.out.println("[Loader] Tiklandi: " + string + " eventsEnabled=" + this.comboEventsEnabled);
        if (!this.comboEventsEnabled) {
            return;
        }
        this.loaderCombo.setSelectedItem(string);
        this.currentInstance.loader = string;
        this.currentInstance.save();
        if (!"Vanilla".equalsIgnoreCase(string)) {
            String string2 = (String)this.versionCombo.getSelectedItem();
            Instance instance = this.currentInstance;
            if (instance != null && string2 != null) {
                File file = instance.modsDir();
                System.out.println("[Loader] autoInstall: loader=" + string + " mcVersion=" + string2 + " modsDir=" + String.valueOf(file));
                new Thread(() -> {
                    try {
                        this.autoInstallForLoader(string, file, string2);
                        SwingUtilities.invokeLater(() -> {
                            this.log("[OK] " + string + " modlari kuruldu!");
                            this.logArea.setRows(6);
                        });
                    }
                    catch (Exception exception) {
                        System.err.println("[Loader] autoInstall HATA: " + String.valueOf(exception));
                        exception.printStackTrace();
                        SwingUtilities.invokeLater(() -> this.log("[HATA] " + exception.getMessage()));
                    }
                }, "loader-install").start();
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static JsonObject loadModPrefs() {
        try {
            if (!MOD_PREFS_FILE.exists()) return new JsonObject();
            try (FileReader fileReader = new FileReader(MOD_PREFS_FILE);){
                JsonElement jsonElement = JsonParser.parseReader(fileReader);
                if (!jsonElement.isJsonObject()) return new JsonObject();
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                return jsonObject;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new JsonObject();
    }

    private static void saveModPrefs(JsonObject jsonObject) {
        try {
            MOD_PREFS_FILE.getParentFile().mkdirs();
            try (FileWriter fileWriter = new FileWriter(MOD_PREFS_FILE);){
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)jsonObject, (Appendable)fileWriter);
            }
        }
        catch (Exception exception) {
            System.err.println("[ModPrefs] Kaydedilemedi: " + exception.getMessage());
        }
    }

    static boolean isModEnabled(String string, boolean bl) {
        try {
            JsonObject jsonObject = MainWindow.loadModPrefs();
            return jsonObject.has(string) ? jsonObject.get(string).getAsBoolean() : bl;
        }
        catch (Exception exception) {
            return bl;
        }
    }

    /**
     * Kullanicinin kendi ekledigi, otomatik kurulacak Modrinth
     * modlarinin listesi. Her kayit {slug, title, enabled} tutar ve
     * her instance olusturuldugunda / guncellendiginde diger
     * oto-kurulum modlariyla birlikte kurulur.
     */
    private static class CustomAutoMod {
        String slug;
        String title;
        boolean enabled = true;
        // "mod" | "shader" | "resourcepack" - hangi kategori icin
        // otomatik kurulacagini belirler. Eski kayitlarda bu alan
        // olmayabilir, o durumda varsayilan "mod" kabul edilir.
        String type = "mod";
        CustomAutoMod(String slug, String title, boolean enabled, String type) {
            this.slug = slug;
            this.title = title;
            this.enabled = enabled;
            this.type = type != null ? type : "mod";
        }
    }

    private static List<CustomAutoMod> loadCustomAutoMods() {
        List<CustomAutoMod> result = new ArrayList<CustomAutoMod>();
        if (!CUSTOM_AUTO_MODS_FILE.exists()) {
            return result;
        }
        try (FileReader fileReader = new FileReader(CUSTOM_AUTO_MODS_FILE);) {
            JsonArray arr = JsonParser.parseReader(fileReader).getAsJsonArray();
            for (int i = 0; i < arr.size(); ++i) {
                JsonObject o = arr.get(i).getAsJsonObject();
                String slug = o.has("slug") ? o.get("slug").getAsString() : null;
                if (slug == null || slug.isBlank()) continue;
                String title = o.has("title") ? o.get("title").getAsString() : slug;
                boolean enabled = !o.has("enabled") || o.get("enabled").getAsBoolean();
                String type = o.has("type") ? o.get("type").getAsString() : "mod";
                result.add(new CustomAutoMod(slug, title, enabled, type));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return result;
    }

    private static void saveCustomAutoMods(List<CustomAutoMod> mods) {
        try {
            CUSTOM_AUTO_MODS_FILE.getParentFile().mkdirs();
            JsonArray arr = new JsonArray();
            for (CustomAutoMod m : mods) {
                JsonObject o = new JsonObject();
                o.addProperty("slug", m.slug);
                o.addProperty("title", m.title);
                o.addProperty("enabled", m.enabled);
                o.addProperty("type", m.type);
                arr.add(o);
            }
            try (FileWriter fileWriter = new FileWriter(CUSTOM_AUTO_MODS_FILE);) {
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)arr, (Appendable)fileWriter);
            }
        }
        catch (Exception exception) {
            System.err.println("[CustomAutoMods] Kaydedilemedi: " + exception.getMessage());
        }
    }

    /**
     * Kullanicinin ozel oto-kurulum listesindeki, verilen tipteki
     * ("mod"/"shader"/"resourcepack") ve etkin olan ogelerin basliklarini
     * dondurur. Shader/Doku paketi sekmelerindeki "bilgi bolumu"nde
     * otomatik kurulacak ogeleri gostermek icin kullanilir.
     */
    public static java.util.List<String> listCustomAutoModsOfType(String type) {
        java.util.List<String> titles = new ArrayList<String>();
        for (CustomAutoMod m : MainWindow.loadCustomAutoMods()) {
            if (m.enabled && type.equalsIgnoreCase(m.type == null ? "mod" : m.type)) {
                titles.add(m.title != null ? m.title : m.slug);
            }
        }
        return titles;
    }

    /**
     * Verilen bir Modrinth slug'ini veya tam proje URL'sini normalize
     * eder ve slug'i dogrular (API'de var mi diye kontrol eder).
     * Basarisiz olursa null doner. type[0] set edilirse, projenin
     * Modrinth'teki gercek "project_type" degeri (mod/shader/resourcepack)
     * oraya yazilir - boylece kullanici yanlis kategoriye eklese bile
     * dogru kategoriye yonlendirilebilir.
     */
    private String resolveModrinthSlug(String input, String[] detectedTypeOut) {
        if (input == null) {
            return null;
        }
        String s = input.trim();
        if (s.isEmpty()) {
            return null;
        }
        // "https://modrinth.com/mod/sodium" gibi bir URL verilmisse slug'i cikar.
        java.util.regex.Matcher urlMatcher = java.util.regex.Pattern.compile("modrinth\\.com/(?:mod|plugin|resourcepack|shader)/([a-zA-Z0-9_-]+)").matcher(s);
        if (urlMatcher.find()) {
            s = urlMatcher.group(1);
        }
        try {
            byte[] bytes = HttpUtil.getBytesWithRetry("https://api.modrinth.com/v2/project/" + java.net.URLEncoder.encode(s, "UTF-8"), 2);
            JsonObject obj = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
            if (detectedTypeOut != null && detectedTypeOut.length > 0 && obj.has("project_type")) {
                String pt = obj.get("project_type").getAsString();
                if ("shader".equalsIgnoreCase(pt) || "resourcepack".equalsIgnoreCase(pt)) {
                    detectedTypeOut[0] = pt;
                } else {
                    detectedTypeOut[0] = "mod";
                }
            }
            if (obj.has("slug")) {
                return obj.get("slug").getAsString();
            }
            return s;
        }
        catch (Exception exception) {
            return null;
        }
    }

    /**
     * Ayarlar sekmesinden cagrilir: kullaniciya once kategori (Mod /
     * Shader / Doku Paketi) sorar, ardindan slug/URL ister, dogrular ve
     * o kategorinin oto-kurulum listesine ekler.
     */
    /**
     * Ayarlar sekmesinden cagrilir. ONCEDEN bu metot sadece kullanicidan
     * tam slug/URL yazmasini istiyordu - kullanici tam ismi bilmiyorsa
     * (orn. "Sodium" yerine tam olarak "sodium" yazmazsa) mod
     * "bulunamiyor" hissi veriyordu. Artik Modlar/Shaderlar sekmesindeki
     * ile ayni desende gercek bir ARAMA penceresi aciliyor: kullanici
     * bir kelime yazar, sonuclar listelenir, listeden secip ekler -
     * tipki mod arama ekranindaki gibi.
     */
    private void addCustomAutoModDialog(Runnable onListChanged) {
        boolean bl = L10n.isEnglish();
        String[] typeOptions = bl
            ? new String[]{"Mod", "Shader Pack", "Resource Pack"}
            : new String[]{"Mod", "Shader Paketi", "Doku Paketi"};
        String[] typeValues = {"mod", "shader", "resourcepack"};
        int choice = JOptionPane.showOptionDialog(this,
            bl ? "What would you like to add to the auto-install list?" : "Otomatik kurulum listesine ne eklemek istersiniz?",
            bl ? "Add Auto-Install Item" : "Otomatik Kurulum Ekle",
            0, 3, null, typeOptions, typeOptions[0]);
        if (choice < 0) {
            return;
        }
        String chosenType = typeValues[choice];
        this.openAutoInstallSearchDialog(chosenType, onListChanged);
    }

    /**
     * Modlar/Shaderlar sekmesindeki arama ekranina benzer, gercek bir
     * arama + secim penceresi. Kullanici yazdikca (Enter'a basinca)
     * Modrinth'te arama yapilir, sonuclar bir listede gosterilir,
     * secilen ogenin kucuk bir aciklama onizlemesi gorunur ve "Ekle"
     * butonuyla otomatik kurulum listesine eklenir.
     */
    private void openAutoInstallSearchDialog(String type, Runnable onListChanged) {
        boolean bl = L10n.isEnglish();
        String typeLabel = switch (type) {
            case "shader" -> bl ? "Shader Pack" : "Shader Paketi";
            case "resourcepack" -> bl ? "Resource Pack" : "Doku Paketi";
            default -> "Mod";
        };
        JDialog dialog = new JDialog(this, (bl ? "Search " : "Ara: ") + typeLabel, true);
        dialog.setSize(560, 520);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Theme.BG_BASE);
        dialog.setLayout(new BorderLayout(0, 10));

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setOpaque(true);
        root.setBackground(Theme.BG_BASE);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        JTextField searchField = UiFx.searchField(bl ? "Search " + typeLabel + "..." : typeLabel + " ara...");
        JButton searchBtn = UiFx.accentButton(bl ? "Search" : "Ara");
        searchBtn.setPreferredSize(new Dimension(80, 30));
        searchRow.add((Component)searchField, "Center");
        searchRow.add((Component)searchBtn, "East");
        root.add((Component)searchRow, "North");

        DefaultListModel<ModrinthApi.ModResult> resultsModel = new DefaultListModel<>();
        JList<ModrinthApi.ModResult> resultsList = new JList<>(resultsModel);
        resultsList.setSelectionMode(0);
        resultsList.setBackground(Theme.BG_SURFACE);
        resultsList.setFixedCellHeight(52);
        resultsList.setCellRenderer(new AutoInstallSearchRenderer());

        JTextArea previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        previewArea.setOpaque(false);
        previewArea.setForeground(Theme.TEXT_SECONDARY);
        previewArea.setFont(new Font("SansSerif", 0, 11));
        previewArea.setText(bl ? "Select a result to preview its description." : "\u00d6nizleme i\u00e7in bir sonu\u00e7 se\u00e7in.");
        JScrollPane previewScroll = UiFx.cleanScroll(previewArea);
        previewScroll.setPreferredSize(new Dimension(0, 110));

        JSplitPane splitPane = new JSplitPane(0, new JScrollPane(resultsList), previewScroll);
        splitPane.setResizeWeight(0.7);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerSize(6);
        root.add((Component)splitPane, "Center");

        JButton addBtn = UiFx.accentButton(bl ? "Add to Auto-Install List" : "Otomatik Kurulum Listesine Ekle");
        addBtn.setEnabled(false);
        root.add((Component)addBtn, "South");

        Runnable doSearch = () -> {
            String query = searchField.getText().trim();
            resultsModel.clear();
            previewArea.setText(bl ? "Searching..." : "Aran\u0131yor...");
            addBtn.setEnabled(false);
            new Thread(() -> {
                try {
                    List<ModrinthApi.ModResult> results = switch (type) {
                        case "shader" -> ModrinthApi.searchShaders(query);
                        case "resourcepack" -> ModrinthApi.searchResourcepacks(query);
                        default -> ModrinthApi.search(query, null, null);
                    };
                    SwingUtilities.invokeLater(() -> {
                        for (ModrinthApi.ModResult r : results) {
                            resultsModel.addElement(r);
                        }
                        previewArea.setText(results.isEmpty()
                            ? (bl ? "No results found." : "Sonu\u00e7 bulunamad\u0131.")
                            : (bl ? "Select a result to preview its description." : "\u00d6nizleme i\u00e7in bir sonu\u00e7 se\u00e7in."));
                    });
                }
                catch (Exception e) {
                    SwingUtilities.invokeLater(() -> previewArea.setText((bl ? "Search error: " : "Arama hatas\u0131: ") + e.getMessage()));
                }
            }).start();
        };
        searchBtn.addActionListener(ae -> doSearch.run());
        searchField.addActionListener(ae -> doSearch.run());
        resultsList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            ModrinthApi.ModResult sel = resultsList.getSelectedValue();
            addBtn.setEnabled(sel != null);
            if (sel != null) {
                previewArea.setText(sel.description != null ? sel.description : "");
                previewArea.setCaretPosition(0);
            }
        });
        addBtn.addActionListener(ae -> {
            ModrinthApi.ModResult sel = resultsList.getSelectedValue();
            if (sel == null) return;
            List<CustomAutoMod> mods = MainWindow.loadCustomAutoMods();
            for (CustomAutoMod m : mods) {
                if (m.slug.equalsIgnoreCase(sel.slug) && m.type.equalsIgnoreCase(type)) {
                    JOptionPane.showMessageDialog(dialog, bl ? "Already added." : "Zaten eklenmi\u015f.", bl ? "Info" : "Bilgi", 1);
                    return;
                }
            }
            mods.add(new CustomAutoMod(sel.slug, sel.title, true, type));
            MainWindow.saveCustomAutoMods(mods);
            this.log((bl ? "Added to auto-install list (" + type + "): " : "Otomatik kurulum listesine eklendi (" + type + "): ") + sel.title);
            if (onListChanged != null) {
                onListChanged.run();
            }
            dialog.dispose();
        });

        dialog.add((Component)root);
        dialog.setVisible(true);
    }

    /** Auto-install arama dialogundaki sonuc listesi icin basit bir renderer (isim + kisa aciklama). */
    private static class AutoInstallSearchRenderer extends JPanel implements ListCellRenderer<ModrinthApi.ModResult> {
        private final JLabel titleLabel = new JLabel();
        private final JLabel descLabel = new JLabel();

        AutoInstallSearchRenderer() {
            this.setLayout(new BorderLayout(0, 2));
            this.setBorder(new EmptyBorder(8, 10, 8, 10));
            this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(1, 13f));
            this.descLabel.setFont(this.descLabel.getFont().deriveFont(0, 10.5f));
            this.add((Component)this.titleLabel, "North");
            this.add((Component)this.descLabel, "Center");
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ModrinthApi.ModResult> list, ModrinthApi.ModResult value, int index, boolean isSelected, boolean cellHasFocus) {
            this.titleLabel.setText(value.title != null ? value.title : value.slug);
            String desc = value.description != null ? value.description : "";
            this.descLabel.setText(desc.length() > 90 ? desc.substring(0, 90) + "..." : desc);
            this.setOpaque(true);
            this.setBackground(isSelected ? Theme.ACCENT_MUTED : Theme.BG_SURFACE);
            this.titleLabel.setForeground(Theme.TEXT_PRIMARY);
            this.descLabel.setForeground(Theme.TEXT_SECONDARY);
            return this;
        }
    }

    /**
     * Kullanicinin ozel oto-kurulum listesindeki tum ogeleri (mod,
     * shader, resource pack) verilen loader/versiyon icin kurmaya
     * calisir. Onceden sadece "mod" tipindeki ogeler kuruluyordu -
     * shader ve doku paketi olarak eklenenler hic kurulmuyordu.
     */
    private void autoInstallCustomMods(File modsDir, String loaderName, String mcVersion) {
        List<CustomAutoMod> mods = MainWindow.loadCustomAutoMods();
        for (CustomAutoMod m : mods) {
            if (!m.enabled) continue;
            try {
                switch (m.type == null ? "mod" : m.type.toLowerCase()) {
                    case "shader" -> {
                        if (this.currentInstance != null) {
                            this.installShaderPack(m.slug, m.title);
                        }
                    }
                    case "resourcepack" -> {
                        if (this.currentInstance != null) {
                            this.installResourcepackBySlug(m.slug, m.title);
                        }
                    }
                    default -> this.tryInstall(modsDir, m.slug, loaderName, mcVersion, m.title);
                }
            }
            catch (Exception exception) {
                this.log("[HATA] " + m.title + ": " + exception.getMessage());
            }
        }
    }

    /** Ozel oto-kurulum listesindeki doku paketlerini kurmak icin kullanilir. */
    private void installResourcepackBySlug(String projectId, String title) throws Exception {
        if (this.currentInstance == null) {
            return;
        }
        String resolvedId = resolveProjectId(projectId);
        if (resolvedId == null) {
            this.log("  " + title + " Modrinth'te bulunamadi (" + projectId + ")");
            return;
        }
        File dir = this.currentInstance.resourcepacksDir();
        dir.mkdirs();
        List<ModrinthApi.ModVersion> list = ModrinthApi.getVersions(resolvedId, null, null);
        if (list.isEmpty()) {
            this.log("  " + title + " uyumlu s\u00fcr\u00fcm yok");
            return;
        }
        ModrinthApi.ModVersion version = ModrinthApi.pickBestVersion(list);
        ResourcepackManager.install(dir, version, null);
        this.log("+ " + title + " kuruldu (" + version.versionNumber + ")");
        SwingUtilities.invokeLater(this.resourcepacksPanel::refreshInstalled);
    }

    private void showInstanceContextMenu(Instance instance, Component component, int n, int n2) {
        boolean bl = L10n.isEnglish();
        JPopupMenu jPopupMenu = new JPopupMenu();
        jPopupMenu.setBackground(Theme.BG_ELEVATED);
        jPopupMenu.setBorder(BorderFactory.createLineBorder(Theme.BG_BORDER, 1));
        JMenuItem jMenuItem = new JMenuItem("\u25b6  " + (bl ? "Launch" : "Ba\u015flat"));
        jMenuItem.setBackground(Theme.BG_ELEVATED);
        jMenuItem.setForeground(Theme.ACCENT_BRIGHT);
        jMenuItem.setFont(jMenuItem.getFont().deriveFont(1, 12.0f));
        jMenuItem.addActionListener(actionEvent -> this.launchInstanceByName(instance.name));
        jPopupMenu.add(jMenuItem);
        jPopupMenu.addSeparator();
        JMenuItem jMenuItem2 = new JMenuItem("\u270f  " + (bl ? "Edit" : "D\u00fczenle"));
        jMenuItem2.setBackground(Theme.BG_ELEVATED);
        jMenuItem2.setForeground(Theme.TEXT_PRIMARY);
        jMenuItem2.addActionListener(actionEvent -> this.openEditSidebar(Instance.load(instance.name)));
        jPopupMenu.add(jMenuItem2);
        JMenuItem jMenuItem2b = new JMenuItem("\ud83d\udcdd  " + (bl ? "Rename" : "Yeniden Adland\u0131r"));
        jMenuItem2b.setBackground(Theme.BG_ELEVATED);
        jMenuItem2b.setForeground(Theme.TEXT_PRIMARY);
        jMenuItem2b.addActionListener(actionEvent -> this.renameInstanceDialog(instance));
        jPopupMenu.add(jMenuItem2b);
        JMenuItem jMenuItem3 = new JMenuItem("\u29c9  " + (bl ? "Clone" : "Klonla"));
        jMenuItem3.setBackground(Theme.BG_ELEVATED);
        jMenuItem3.setForeground(Theme.TEXT_PRIMARY);
        jMenuItem3.addActionListener(actionEvent -> this.cloneInstance(instance));
        jPopupMenu.add(jMenuItem3);
        JMenuItem jMenuItem4 = new JMenuItem("\ud83d\udcc1  " + (bl ? "Open Folder" : "Klas\u00f6r\u00fc A\u00e7"));
        jMenuItem4.setBackground(Theme.BG_ELEVATED);
        jMenuItem4.setForeground(Theme.TEXT_PRIMARY);
        jMenuItem4.addActionListener(actionEvent -> {
            try {
                Desktop.getDesktop().open(new File(Paths.INSTANCES_DIR, instance.name));
            }
            catch (Exception exception) {
                this.log("Klas\u00f6r a\u00e7\u0131lamad\u0131: " + exception.getMessage());
            }
        });
        jPopupMenu.add(jMenuItem4);
        JMenuItem jMenuItem5 = new JMenuItem("\ud83d\uddbc  " + (bl ? "Screenshots" : "Ekran G\u00f6r\u00fcnt\u00fcleri"));
        jMenuItem5.setBackground(Theme.BG_ELEVATED);
        jMenuItem5.setForeground(Theme.TEXT_PRIMARY);
        jMenuItem5.addActionListener(actionEvent -> this.showScreenshotGallery(instance));
        jPopupMenu.add(jMenuItem5);
        JMenuItem jMenuItem6 = new JMenuItem("\ud83d\udcca  " + (bl ? "Statistics" : "\u0130statistikler"));
        jMenuItem6.setBackground(Theme.BG_ELEVATED);
        jMenuItem6.setForeground(Theme.TEXT_PRIMARY);
        jMenuItem6.addActionListener(actionEvent -> this.showInstanceStats(instance));
        jPopupMenu.add(jMenuItem6);
        JMenuItem jMenuItem7 = new JMenuItem("\ud83d\udce6  " + (bl ? "Export" : "D\u0131\u015fa Aktar"));
        jMenuItem7.setBackground(Theme.BG_ELEVATED);
        jMenuItem7.setForeground(Theme.TEXT_PRIMARY);
        jMenuItem7.addActionListener(actionEvent -> this.exportInstance(instance));
        jPopupMenu.add(jMenuItem7);
        jPopupMenu.addSeparator();
        JMenuItem jMenuItem8 = new JMenuItem("\ud83d\uddd1  " + (bl ? "Delete" : "Sil"));
        jMenuItem8.setBackground(Theme.BG_ELEVATED);
        jMenuItem8.setForeground(Theme.RED);
        jMenuItem8.addActionListener(actionEvent -> {
            if (this.currentInstance == instance || this.currentInstance != null && this.currentInstance.name.equals(instance.name)) {
                this.deleteInstance();
            } else {
                int confirmResult = JOptionPane.showConfirmDialog(this, L10n.fmt("instance.delete_confirm", instance.name), L10n.get("instance.delete_title"), 0, 2);
                if (confirmResult == 0) {
                    try {
                        MainWindow.deleteDir(new File(Paths.INSTANCES_DIR, instance.name));
                        this.refreshInstancesGrid();
                        this.log(L10n.get("instance.deleted") + instance.name);
                    }
                    catch (Exception exception) {
                        this.log("Silinemedi: " + exception.getMessage());
                    }
                }
            }
        });
        jPopupMenu.add(jMenuItem8);
        jPopupMenu.show(component, n, n2);
    }

    /** Bir instance icin isim degistirme dialogunu acar ve gerekli tum UI durumunu gunceller. */
    private void renameInstanceDialog(Instance instance) {
        boolean bl = L10n.isEnglish();
        String newName = JOptionPane.showInputDialog(this, bl ? "New name:" : "Yeni isim:", instance.name);
        if (newName == null || newName.isBlank() || newName.trim().equals(instance.name)) {
            return;
        }
        Instance fresh = Instance.load(instance.name);
        String oldName = fresh.name;
        boolean wasActive = this.currentInstance != null && oldName.equals(this.currentInstance.name);
        if (!fresh.renameTo(newName.trim())) {
            JOptionPane.showMessageDialog(this, bl ? "Could not rename - a folder with that name may already exist." : "Yeniden adland\u0131r\u0131lamad\u0131 - bu isimde bir klas\u00f6r zaten olabilir.", bl ? "Rename Failed" : "Yeniden Adland\u0131rma Ba\u015far\u0131s\u0131z", 0);
            return;
        }
        if (wasActive) {
            this.currentInstance = fresh;
            this.settings.activeInstance = fresh.name;
            this.settings.save();
            this.applyInstanceToUi();
        }
        this.refreshInstancesGrid();
        this.log((bl ? "Renamed instance: " : "Instance yeniden adland\u0131r\u0131ld\u0131: ") + oldName + " \u2192 " + fresh.name);
    }

    private void cloneInstance(Instance instance) {
        boolean bl = L10n.isEnglish();
        String string = JOptionPane.showInputDialog(this, bl ? "Name for the cloned instance:" : "Klonlanan instance i\u00e7in isim:", instance.name + "_copy");
        if (string == null || string.isBlank()) {
            return;
        }
        string = string.trim().replaceAll("[^a-zA-Z0-9_\\-]", "_");
        File file = new File(Paths.INSTANCES_DIR, instance.name);
        File file2 = new File(Paths.INSTANCES_DIR, string);
        if (file2.exists()) {
            JOptionPane.showMessageDialog(this, bl ? "An instance with that name already exists." : "Bu isimde bir instance zaten var.", bl ? "Error" : "Hata", 0);
            return;
        }
        try {
            MainWindow.copyDir(file, file2);
            File file3 = new File(file2, "instance.json");
            if (file3.exists()) {
                String string2 = new String(Files.readAllBytes(file3.toPath()));
                string2 = string2.replaceFirst("\"name\"\\s*:\\s*\"[^\"]*\"", "\"name\":\"" + string + "\"");
                Files.write(file3.toPath(), string2.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            }
            this.refreshInstancesGrid();
            this.log((bl ? "Cloned: " : "Klonland\u0131: ") + instance.name + " \u2192 " + string);
        }
        catch (Exception exception) {
            this.log((bl ? "Clone failed: " : "Klonlama ba\u015far\u0131s\u0131z: ") + exception.getMessage());
        }
    }

    private static void deleteDir(File file) {
        if (file.isDirectory()) {
            for (File file2 : file.listFiles()) {
                MainWindow.deleteDir(file2);
            }
        }
        file.delete();
    }

    private void showInstanceStats(Instance instance) {
        boolean bl = L10n.isEnglish();
        ArrayList<JsonObject> arrayList = new ArrayList<JsonObject>();
        File file2 = new File(new File(Paths.INSTANCES_DIR, instance.name), "crash-history.json");
        if (file2.exists()) {
            try {
                FileReader crashReader = new FileReader(file2);
                try {
                    JsonArray crashArray = JsonParser.parseReader(crashReader).getAsJsonArray();
                    for (int i = 0; i < crashArray.size(); ++i) {
                        arrayList.add(crashArray.get(i).getAsJsonObject());
                    }
                }
                finally {
                    crashReader.close();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        Map<String, Long> dailyStats = new LinkedHashMap<String, Long>();
        File statsFile = new File(new File(Paths.INSTANCES_DIR, instance.name), "stats.json");
        if (statsFile.exists()) {
            try (FileReader fileReader = new FileReader(statsFile);){
                JsonObject statsRoot = JsonParser.parseReader(fileReader).getAsJsonObject();
                if (statsRoot.has("daily")) {
                    JsonObject dailyObj = statsRoot.getAsJsonObject("daily");
                    for (String string2 : dailyObj.keySet()) {
                        dailyStats.put(string2, dailyObj.get(string2).getAsLong());
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        JDialog jDialog = new JDialog(this, (bl ? "Statistics \u2014 " : "\u0130statistikler \u2014 ") + instance.name, true);
        jDialog.setSize(620, 480);
        jDialog.setLocationRelativeTo(this);
        jDialog.setLayout(new BorderLayout(0, 0));
        jDialog.getContentPane().setBackground(Theme.BG_BASE);
        JPanel rootPanel = new JPanel(new BorderLayout(0, 12));
        rootPanel.setOpaque(false);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 10, 0));
        statsRow.setOpaque(false);
        long l = instance.totalPlaySeconds;
        long l2 = instance.lastPlayedMs;
        int n = arrayList.size();
        long l3 = dailyStats.getOrDefault(MainWindow.todayKey(), 0L);
        statsRow.add(this.statCard(bl ? "Total Playtime" : "Toplam S\u00fcre", MainWindow.formatPlayTimeLocal(l), Theme.ACCENT));
        statsRow.add(this.statCard(bl ? "Today" : "Bug\u00fcn", MainWindow.formatPlayTimeLocal(l3), Theme.CYAN));
        statsRow.add(this.statCard(bl ? "Crashes" : "Crash", String.valueOf(n), n > 5 ? Theme.RED : Theme.YELLOW));
        statsRow.add(this.statCard(bl ? "Last Played" : "Son Oynama", l2 > 0L ? new SimpleDateFormat("dd MMM").format(new Date(l2)) : (bl ? "Never" : "Hi\u00e7"), Theme.TEXT_SECONDARY));
        JPanel jPanel = this.buildDailyChart(dailyStats, bl);
        JPanel jPanel2 = new JPanel(new BorderLayout(0, 6));
        jPanel2.setOpaque(false);
        JLabel jLabel = UiFx.sectionLabel(bl ? "RECENT CRASHES" : "SON CRASHLER");
        jPanel2.add((Component)jLabel, "North");
        DefaultListModel<Object> defaultListModel = new DefaultListModel<Object>();
        if (arrayList.isEmpty()) {
            defaultListModel.addElement(bl ? "No crashes recorded." : "Kay\u0131tl\u0131 crash yok.");
        } else {
            for (int i = arrayList.size() - 1; i >= Math.max(0, arrayList.size() - 8); --i) {
                JsonObject crashEntry = arrayList.get(i);
                String string3 = crashEntry.has("time") ? crashEntry.get("time").getAsString() : "?";
                String crashType = crashEntry.has("type") ? crashEntry.get("type").getAsString() : "?";
                String crashSummary = crashEntry.has("summary") ? crashEntry.get("summary").getAsString() : "";
                defaultListModel.addElement(string3.substring(0, Math.min(16, string3.length())) + "  [" + crashType + "]  " + crashSummary.substring(0, Math.min(60, crashSummary.length())));
            }
        }
        JList jList = new JList(defaultListModel);
        jList.setBackground(Theme.BG_SURFACE);
        jList.setForeground(Theme.TEXT_SECONDARY);
        jList.setFont(jList.getFont().deriveFont(11.0f));
        jList.setFixedCellHeight(24);
        jPanel2.add((Component)UiFx.cleanScroll(jList), "Center");
        JPanel infoRow = new JPanel(new GridLayout(1, 3, 10, 0));
        infoRow.setOpaque(false);
        infoRow.add(this.infoChip(bl ? "Version" : "S\u00fcr\u00fcm", instance.lastVersion != null ? instance.lastVersion : "?"));
        infoRow.add(this.infoChip(bl ? "Loader" : "Y\u00fckleyici", instance.loader != null && !instance.loader.isEmpty() ? instance.loader : "Vanilla"));
        int n2 = 0;
        try {
            File modsDir = new File(new File(Paths.INSTANCES_DIR, instance.name), "mods");
            if (modsDir.isDirectory()) {
                String[] jarNames = modsDir.list((dir, name) -> name.endsWith(".jar"));
                n2 = jarNames != null ? jarNames.length : 0;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        infoRow.add(this.infoChip(bl ? "Mods" : "Mod Say\u0131s\u0131", String.valueOf(n2)));
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 0, 12));
        bottomPanel.setOpaque(false);
        bottomPanel.add(jPanel2);
        bottomPanel.add(infoRow);
        rootPanel.add((Component)statsRow, "North");
        rootPanel.add((Component)jPanel, "Center");
        rootPanel.add((Component)bottomPanel, "South");
        jDialog.add((Component)rootPanel);
        jDialog.setVisible(true);
    }

    private JPanel statCard(String string, String string2, final Color color) {
        JPanel jPanel = new JPanel(new BorderLayout(0, 4)){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, Theme.BG_ELEVATED, 0.0f, this.getHeight(), Theme.BG_SURFACE));
                graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 12, 12);
                graphics2D.setColor(UiFx.withAlpha(color, 80));
                graphics2D.setStroke(new BasicStroke(1.5f));
                graphics2D.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 12, 12);
                graphics2D.setColor(color);
                graphics2D.fillRoundRect(0, 0, this.getWidth(), 4, 4, 4);
                graphics2D.dispose();
            }
        };
        jPanel.setOpaque(false);
        jPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel jLabel = new JLabel(string2);
        jLabel.setFont(jLabel.getFont().deriveFont(1, 20.0f));
        jLabel.setForeground(color);
        JLabel jLabel2 = new JLabel(string);
        jLabel2.setFont(jLabel2.getFont().deriveFont(0, 10.0f));
        jLabel2.setForeground(Theme.TEXT_MUTED);
        jPanel.add((Component)jLabel, "Center");
        jPanel.add((Component)jLabel2, "South");
        return jPanel;
    }

    private JPanel buildDailyChart(Map<String, Long> map, boolean bl) {
        JPanel jPanel = new JPanel(new BorderLayout(0, 6));
        jPanel.setOpaque(false);
        jPanel.add((Component)UiFx.sectionLabel(bl ? "DAILY PLAYTIME (LAST 14 DAYS)" : "G\u00dcNL\u00dcK OYUN S\u00dcRES\u0130 (SON 14 G\u00dcN)"), "North");
        final ArrayList<String> arrayList = new ArrayList<String>();
        final ArrayList<Long> arrayList2 = new ArrayList<Long>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 13; i >= 0; --i) {
            Calendar calendar2 = (Calendar)calendar.clone();
            calendar2.add(6, -i);
            String string = String.format("%04d-%02d-%02d", calendar2.get(1), calendar2.get(2) + 1, calendar2.get(5));
            String string2 = String.format("%02d/%02d", calendar2.get(2) + 1, calendar2.get(5));
            arrayList.add(string2);
            arrayList2.add(map.getOrDefault(string, 0L));
        }
        long l2 = arrayList2.stream().mapToLong(l -> l).max().orElse(1L);
        if (l2 == 0L) {
            l2 = 1L;
        }
        final long l3 = l2;
        JPanel jPanel2 = new JPanel(null){

            @Override
            protected void paintComponent(Graphics graphics) {
                int n;
                super.paintComponent(graphics);
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n2 = this.getWidth();
                int n3 = this.getHeight();
                int n4 = arrayList2.size();
                int n5 = 28;
                int n6 = 4;
                int n7 = (n2 - n5 * 2 - n6 * (n4 - 1)) / n4;
                int n8 = n3 - 30;
                graphics2D.setColor(UiFx.withAlpha(Theme.BG_BORDER, 80));
                graphics2D.setStroke(new BasicStroke(1.0f, 0, 2, 0.0f, new float[]{4.0f, 4.0f}, 0.0f));
                for (n = 1; n <= 4; ++n) {
                    int n9 = n8 - (int)((double)(n8 * n) / 4.0);
                    graphics2D.drawLine(n5, n9, n2 - n5, n9);
                }
                graphics2D.setStroke(new BasicStroke(1.0f));
                for (n = 0; n < n4; ++n) {
                    Color color;
                    long l = (Long)arrayList2.get(n);
                    int n10 = (int)((double)((long)n8 * l) / (double)l3);
                    if (n10 < 2 && l > 0L) {
                        n10 = 2;
                    }
                    int n11 = n5 + n * (n7 + n6);
                    int n12 = n8 - n10;
                    boolean bl = n == n4 - 1;
                    Color color2 = bl ? Theme.ACCENT_BRIGHT : UiFx.lerp(Theme.ACCENT, Theme.ACCENT_DARK, 0.4f);
                    Color color3 = color = bl ? Theme.ACCENT : Theme.ACCENT_DARK;
                    if (n10 > 0) {
                        graphics2D.setPaint(new GradientPaint(n11, n12, color2, n11, n12 + n10, color));
                        graphics2D.fillRoundRect(n11, n12, n7, n10, 4, 4);
                        graphics2D.setColor(new Color(255, 255, 255, 30));
                        graphics2D.fillRoundRect(n11, n12, n7, Math.min(n10 / 2, 8), 4, 4);
                    } else {
                        graphics2D.setColor(UiFx.withAlpha(Theme.BG_BORDER, 60));
                        graphics2D.fillRoundRect(n11, n8 - 2, n7, 2, 2, 2);
                    }
                    graphics2D.setColor(bl ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED);
                    graphics2D.setFont(graphics2D.getFont().deriveFont(9.0f));
                    FontMetrics fontMetrics = graphics2D.getFontMetrics();
                    String string = (String)arrayList.get(n);
                    graphics2D.drawString(string, n11 + (n7 - fontMetrics.stringWidth(string)) / 2, n3 - 4);
                    if (l <= 0L) continue;
                    String string2 = l < 3600L ? l / 60L + "m" : l / 3600L + "h";
                    graphics2D.setColor(Theme.ACCENT_BRIGHT);
                    graphics2D.drawString(string2, n11 + (n7 - fontMetrics.stringWidth(string2)) / 2, n12 - 2);
                }
                graphics2D.dispose();
            }
        };
        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new Dimension(0, 130));
        jPanel.add((Component)jPanel2, "Center");
        return jPanel;
    }

    private JPanel infoChip(String string, String string2) {
        JPanel jPanel = new JPanel(new BorderLayout(0, 4));
        jPanel.setOpaque(true);
        jPanel.setBackground(Theme.BG_SURFACE);
        jPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BG_BORDER, 1), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JLabel jLabel = new JLabel(string);
        jLabel.setFont(jLabel.getFont().deriveFont(9.0f));
        jLabel.setForeground(Theme.TEXT_MUTED);
        JLabel jLabel2 = new JLabel(string2);
        jLabel2.setFont(jLabel2.getFont().deriveFont(1, 13.0f));
        jLabel2.setForeground(Theme.TEXT_PRIMARY);
        jPanel.add((Component)jLabel, "North");
        jPanel.add((Component)jLabel2, "Center");
        return jPanel;
    }

    private static String todayKey() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%04d-%02d-%02d", calendar.get(1), calendar.get(2) + 1, calendar.get(5));
    }

    private void showScreenshotGallery(Instance instance) {
        boolean bl = L10n.isEnglish();
        File file2 = new File(new File(Paths.INSTANCES_DIR, instance.name), "screenshots");
        final JDialog jDialog = new JDialog(this, (bl ? "Screenshots \u2014 " : "Ekran G\u00f6r\u00fcnt\u00fcleri \u2014 ") + instance.name, false);
        jDialog.setSize(780, 560);
        jDialog.setLocationRelativeTo(this);
        jDialog.getContentPane().setBackground(Theme.BG_BASE);
        jDialog.setLayout(new BorderLayout(0, 0));
        if (!file2.isDirectory()) {
            JLabel jLabel = new JLabel(bl ? "No screenshots found." : "Ekran g\u00f6r\u00fcnt\u00fcs\u00fc bulunamad\u0131.", 0);
            jLabel.setForeground(Theme.TEXT_MUTED);
            jDialog.add(jLabel);
            jDialog.setVisible(true);
            return;
        }
        File[] fileArray = file2.listFiles((file, string) -> string.toLowerCase().endsWith(".png") || string.toLowerCase().endsWith(".jpg"));
        if (fileArray == null || fileArray.length == 0) {
            JLabel jLabel = new JLabel(bl ? "No screenshots found." : "Ekran g\u00f6r\u00fcnt\u00fcs\u00fc bulunamad\u0131.", 0);
            jLabel.setForeground(Theme.TEXT_MUTED);
            jDialog.add(jLabel);
            jDialog.setVisible(true);
            return;
        }
        Arrays.sort(fileArray, Comparator.comparingLong(File::lastModified).reversed());
        int n = 4;
        JPanel jPanel = new JPanel(new GridLayout(0, n, 8, 8));
        jPanel.setOpaque(false);
        jPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        for (File file3 : fileArray) {
            final JPanel jPanel2 = new JPanel(new BorderLayout(0, 4));
            jPanel2.setOpaque(false);
            jPanel2.setCursor(Cursor.getPredefinedCursor(12));
            JLabel jLabel = new JLabel("...", 0);
            jLabel.setPreferredSize(new Dimension(170, 96));
            jLabel.setBackground(Theme.BG_ELEVATED);
            jLabel.setOpaque(true);
            jLabel.setForeground(Theme.TEXT_MUTED);
            JLabel jLabel2 = new JLabel((String)(file3.getName().length() > 20 ? file3.getName().substring(0, 18) + "..." : file3.getName()), 0);
            jLabel2.setFont(jLabel2.getFont().deriveFont(9.0f));
            jLabel2.setForeground(Theme.TEXT_MUTED);
            jPanel2.add((Component)jLabel, "Center");
            jPanel2.add((Component)jLabel2, "South");
            jPanel.add(jPanel2);
            final File file4 = file3;
            new Thread(() -> {
                try {
                    BufferedImage bufferedImage = ImageIO.read(file4);
                    if (bufferedImage != null) {
                        BufferedImage bufferedImage2 = new BufferedImage(170, 96, 1);
                        Graphics2D graphics2D = bufferedImage2.createGraphics();
                        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        graphics2D.drawImage(bufferedImage, 0, 0, 170, 96, null);
                        graphics2D.dispose();
                        SwingUtilities.invokeLater(() -> {
                            jLabel.setIcon(new ImageIcon(bufferedImage2));
                            jLabel.setText(null);
                        });
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }, "ss-thumb").start();
            jPanel2.addMouseListener(new MouseAdapter(){

                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    MainWindow.this.showFullScreenshot(file4, jDialog);
                }

                @Override
                public void mouseEntered(MouseEvent mouseEvent) {
                    jPanel2.setBorder(BorderFactory.createLineBorder(Theme.ACCENT, 2));
                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {
                    jPanel2.setBorder(null);
                }
            });
        }
        JPanel jPanel3 = new JPanel(new BorderLayout(8, 0));
        jPanel3.setOpaque(false);
        jPanel3.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        JLabel jLabel = new JLabel((bl ? "Screenshots" : "Ekran G\u00f6r\u00fcnt\u00fcleri") + " (" + fileArray.length + ")");
        jLabel.setFont(jLabel.getFont().deriveFont(1, 14.0f));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        JButton jButton = UiFx.ghostButton(bl ? "Open Folder" : "Klas\u00f6r\u00fc A\u00e7");
        jButton.addActionListener(actionEvent -> {
            try {
                Desktop.getDesktop().open(file2);
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
        jPanel3.add((Component)jLabel, "West");
        jPanel3.add((Component)jButton, "East");
        jDialog.add((Component)jPanel3, "North");
        jDialog.add((Component)UiFx.cleanScroll(jPanel), "Center");
        jDialog.setVisible(true);
    }

    private void showFullScreenshot(File file, JDialog jDialog) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            if (bufferedImage == null) {
                return;
            }
            final JDialog jDialog2 = new JDialog(jDialog, file.getName(), false);
            Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
            double d = Math.min((double)(dimension.width - 80) / (double)bufferedImage.getWidth(), (double)(dimension.height - 80) / (double)bufferedImage.getHeight());
            d = Math.min(d, 1.0);
            int n = (int)((double)bufferedImage.getWidth() * d);
            int n2 = (int)((double)bufferedImage.getHeight() * d);
            jDialog2.setSize(n + 16, n2 + 40);
            jDialog2.setLocationRelativeTo(jDialog);
            jDialog2.getContentPane().setBackground(Theme.BG_BASE);
            JLabel jLabel = new JLabel(new ImageIcon(bufferedImage.getScaledInstance(n, n2, 4)));
            jLabel.addMouseListener(new MouseAdapter(){

                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    jDialog2.dispose();
                }
            });
            jDialog2.add(jLabel);
            JButton jButton = UiFx.ghostButton("\ud83d\udcc2 " + (L10n.isEnglish() ? "Save As..." : "Farkl\u0131 Kaydet..."));
            jButton.addActionListener(actionEvent -> {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setSelectedFile(new File(file.getName()));
                if (jFileChooser.showSaveDialog(jDialog2) == 0) {
                    try {
                        Files.copy(file.toPath(), jFileChooser.getSelectedFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    catch (Exception exception) {
                        this.log("Kaydedilemedi: " + exception.getMessage());
                    }
                }
            });
            jDialog2.add((Component)jButton, "South");
            jDialog2.setVisible(true);
        }
        catch (Exception exception) {
            this.log("Screenshot a\u00e7\u0131lamad\u0131: " + exception.getMessage());
        }
    }

    private void showAccountManagerDialog() {
        Object object;
        Object object2;
        final boolean bl = L10n.isEnglish();
        final JDialog jDialog = new JDialog(this, bl ? "Account Manager" : "Hesap Y\u00f6neticisi", true);
        jDialog.setSize(480, 400);
        jDialog.setLocationRelativeTo(this);
        jDialog.getContentPane().setBackground(Theme.BG_BASE);
        jDialog.setLayout(new BorderLayout(0, 0));
        JPanel jPanel = new JPanel(new BorderLayout(0, 10));
        jPanel.setOpaque(false);
        jPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        DefaultListModel<CallSite> defaultListModel = new DefaultListModel<CallSite>();
        final List<MinecraftSession> list = AccountManager.list();
        String string = AccountManager.activeUsername();
        for (MinecraftSession object32 : list) {
            object2 = object32.username != null && object32.username.equals(string) ? "\u2713 " : "  ";
            object = object32.uuid != null && object32.uuid.contains("-") ? " [MS]" : " [Offline]";
            defaultListModel.addElement((CallSite)((Object)((String)object2 + object32.username + (String)object)));
        }
        final JList jList = new JList(defaultListModel);
        jList.setBackground(Theme.BG_SURFACE);
        jList.setForeground(Theme.TEXT_PRIMARY);
        jList.setSelectionBackground(UiFx.withAlpha(Theme.ACCENT, 60));
        jList.setFont(jList.getFont().deriveFont(13.0f));
        jList.setFixedCellHeight(36);
        jList.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2 && jList.getSelectedIndex() >= 0) {
                    MinecraftSession minecraftSession = (MinecraftSession)list.get(jList.getSelectedIndex());
                    AccountManager.setActive(minecraftSession.username);
                    MainWindow.this.session = minecraftSession;
                    MainWindow.this.onSessionChanged();
                    MainWindow.this.log((bl ? "Active account: " : "Aktif hesap: ") + minecraftSession.username);
                    jDialog.dispose();
                }
            }
        });
        JButton jButton = UiFx.accentButton(bl ? "Switch" : "Ge\u00e7");
        jButton.addActionListener(actionEvent -> {
            int n = jList.getSelectedIndex();
            if (n < 0 || n >= list.size()) {
                return;
            }
            MinecraftSession minecraftSession = (MinecraftSession)list.get(n);
            AccountManager.setActive(minecraftSession.username);
            this.session = minecraftSession;
            this.onSessionChanged();
            this.log((bl ? "Active account: " : "Aktif hesap: ") + minecraftSession.username);
            jDialog.dispose();
        });
        object2 = UiFx.ghostButton(bl ? "+ Offline" : "+ \u00c7evrimd\u0131\u015f\u0131");
        ((AbstractButton)object2).addActionListener(actionEvent -> {
            String offlineName = JOptionPane.showInputDialog(jDialog, (Object)(bl ? "Username:" : "Kullan\u0131c\u0131 ad\u0131:"));
            if (offlineName == null || offlineName.isBlank()) {
                return;
            }
            this.doOfflineLoginWithName(offlineName.trim(), jDialog);
        });
        object = UiFx.ghostButton("+ Microsoft");
        ((AbstractButton)object).addActionListener(actionEvent -> {
            jDialog.dispose();
            this.doMsLogin();
        });
        JButton jButton2 = UiFx.dangerButton(bl ? "Remove" : "Kald\u0131r");
        jButton2.addActionListener(actionEvent -> {
            int n = jList.getSelectedIndex();
            if (n < 0 || n >= list.size()) {
                return;
            }
            MinecraftSession minecraftSession = (MinecraftSession)list.get(n);
            int n2 = JOptionPane.showConfirmDialog(jDialog, (bl ? "Remove account: " : "Hesab\u0131 kald\u0131r: ") + minecraftSession.username + "?", bl ? "Remove" : "Kald\u0131r", 0);
            if (n2 == 0) {
                AccountManager.remove(minecraftSession.username);
                if (minecraftSession.username != null && minecraftSession.username.equals(this.session != null ? this.session.username : "")) {
                    this.session = AccountManager.getActive();
                    this.onSessionChanged();
                }
                list.remove(n);
                defaultListModel.remove(n);
            }
        });
        JPanel jPanel2 = new JPanel(new FlowLayout(2, 6, 0));
        jPanel2.setOpaque(false);
        jPanel2.add((Component)object2);
        jPanel2.add((Component)object);
        jPanel2.add(jButton2);
        jPanel2.add(jButton);
        JLabel jLabel = new JLabel(bl ? "Double-click to switch account" : "Ge\u00e7mek i\u00e7in \u00e7ift t\u0131klay\u0131n");
        jLabel.setFont(jLabel.getFont().deriveFont(10.0f));
        jLabel.setForeground(Theme.TEXT_MUTED);
        jPanel.add((Component)UiFx.sectionLabel(bl ? "ACCOUNTS" : "HESAPLAR"), "North");
        jPanel.add((Component)UiFx.cleanScroll(jList), "Center");
        JPanel jPanel3 = new JPanel(new BorderLayout(0, 6));
        jPanel3.setOpaque(false);
        jPanel3.add((Component)jLabel, "North");
        jPanel3.add((Component)jPanel2, "South");
        jPanel.add((Component)jPanel3, "South");
        jDialog.add(jPanel);
        jDialog.setVisible(true);
    }

    private void doOfflineLoginWithName(String string, Component component) {
        UUID uUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + string).getBytes(StandardCharsets.UTF_8));
        MinecraftSession minecraftSession = MinecraftSession.offline(string);
        AccountManager.add(minecraftSession);
        this.session = minecraftSession;
        this.onSessionChanged();
        this.log((L10n.isEnglish() ? "Offline login: " : "\u00c7evrimd\u0131\u015f\u0131 giri\u015f: ") + string);
    }

    private boolean checkModConflictsBeforeLaunch() {
        if (this.currentInstance == null) {
            return true;
        }
        File file2 = new File(new File(Paths.INSTANCES_DIR, this.currentInstance.name), "mods");
        if (!file2.isDirectory()) {
            return true;
        }
        boolean bl = L10n.isEnglish();
        LinkedHashMap<String, List> linkedHashMap = new LinkedHashMap<String, List>();
        File[] fileArray = file2.listFiles((file, string) -> string.endsWith(".jar") || string.endsWith(".jar.disabled"));
        if (fileArray == null) {
            return true;
        }
        for (File file3 : fileArray) {
            if (file3.getName().endsWith(".disabled")) continue;
            try (ZipFile zipFile = new ZipFile(file3);){
                Object object;
                Object object2;
                Object object3;
                Object object4;
                ZipEntry zipEntry = zipFile.getEntry("fabric.mod.json");
                if (zipEntry != null) {
                    object4 = zipFile.getInputStream(zipEntry);
                    object3 = new String(((InputStream)object4).readAllBytes(), StandardCharsets.UTF_8);
                    object2 = JsonParser.parseString((String)object3).getAsJsonObject();
                    if (((JsonObject)object2).has("id")) {
                        object = ((JsonObject)object2).get("id").getAsString();
                        linkedHashMap.computeIfAbsent((String)object, string -> new ArrayList()).add(file3.getName());
                    }
                    continue;
                }
                object4 = zipFile.getEntry("META-INF/mods.toml");
                if (object4 == null) continue;
                object3 = zipFile.getInputStream((ZipEntry)object4);
                object2 = new String(((InputStream)object3).readAllBytes(), StandardCharsets.UTF_8);
                object = Pattern.compile("modId\\s*=\\s*\"([^\"]+)\"").matcher((CharSequence)object2);
                while (((Matcher)object).find()) {
                    String string2 = ((Matcher)object).group(1);
                    if (string2.equals("forge") || string2.equals("minecraft")) continue;
                    linkedHashMap.computeIfAbsent(string2, string -> new ArrayList()).add(file3.getName());
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        ArrayList arrayList = new ArrayList();
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            if (((List)entry.getValue()).size() <= 1) continue;
            arrayList.add("\u2022 " + (String)entry.getKey() + ":\n    " + String.join((CharSequence)"\n    ", (Iterable)entry.getValue()));
        }
        if (!arrayList.isEmpty()) {
            String string3 = (bl ? "Mod conflicts detected!\nThe following mod IDs have multiple versions:\n\n" : "Mod \u00e7ak\u0131\u015fmas\u0131 tespit edildi!\nA\u015faf\u0131daki mod ID'lerinin birden fazla versiyonu var:\n\n") + String.join((CharSequence)"\n", arrayList) + "\n\n" + (bl ? "Launch anyway?" : "Yine de ba\u015flat\u0131ls\u0131n m\u0131?");
            int n = JOptionPane.showConfirmDialog(this, string3, bl ? "Mod Conflict" : "Mod \u00c7ak\u0131\u015fmas\u0131", 0, 2);
            return n == 0;
        }
        return true;
    }

    private static void saveLangPref(String string) {
        try {
            File file = new File(Paths.GAME_DIR, "lang.txt");
            file.getParentFile().mkdirs();
            Files.write(file.toPath(), string.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
        }
        catch (Exception exception) {
            System.err.println("[L10n] Dil tercihi kaydedilemedi: " + exception.getMessage());
        }
    }

    /**
     * Ayarlar sekmesindeki "Launcher'i Sil" butonu tarafindan cagrilir.
     * Cift onay ister (yanlislikla tiklanip veri kaybi yasanmasin diye),
     * ardindan tum launcher veri klasorunu (instances, mods, hesaplar,
     * ayarlar - Paths.GAME_DIR) siler ve uygulamayi kapatir.
     */
    /**
     * "Maks Performans" tuşu acildiginda cagrilir: mevcut instance'in
     * loader'i icin en guclu performans mod setini (Sodium, Lithium,
     * Entity Culling, FerriteCore, Starlight, vb.) arka planda otomatik
     * kurar. Uyumsuz olan modlar (orn. Forge instance'inda Fabric-only
     * bir mod) sessizce atlanir, hata loga yazilir ama diger modlarin
     * kurulumunu engellemez.
     */
    /**
     * "Maks Performans" acilmadan ONCE cagrilir: kurulu modlar arasinda
     * performans setiyle CAKISACAK modlari (OptiFine, VulkanMod, karsi
     * yukleyicinin portu vb.) arar. Bulursa kullaniciyi uyairir; onay
     * alirsa bunlari DISKTEN siler (jar + registry kaydi). Iptal edilirse
     * false doner ve Maks Performans hic acilmaz - cakisan modlarla
     * performans setinin ayni anda kurulu kalmasi engellenmis olur.
     */
    private boolean handleMaxPerfConflictRemoval() {
        if (this.currentInstance == null) {
            return true; // kurulu mod taranamadi - kuruluma izin ver
        }
        File modsDir = this.currentInstance.modsDir();
        if (!modsDir.isDirectory()) {
            return true;
        }
        boolean isEn = L10n.isEnglish();
        java.util.List<ModManager.InstalledMod> conflicts = ModManager.findConflictingMods(modsDir, PerformanceFlags.conflictKeywords(this.currentInstance.loader));
        if (conflicts.isEmpty()) {
            return true;
        }
        StringBuilder names = new StringBuilder();
        for (ModManager.InstalledMod m : conflicts) {
            names.append("\u2022 ").append(m.projectTitle != null ? m.projectTitle : m.fileName).append('\n');
        }
        String msg = isEn
            ? "<html><b>" + conflicts.size() + " installed mod(s) conflict with Maximum Performance:</b><br><br>"
              + names.toString().replace("\n", "<br>").replaceAll("<br>$", "")
              + "<br><br>Delete them and enable Maximum Performance?</html>"
            : "<html><b>Maks Performans ile \u00e7ak\u0131\u015fan " + conflicts.size() + " mod bulundu:</b><br><br>"
              + names.toString().replace("\n", "<br>").replaceAll("<br>$", "")
              + "<br><br>Silinsinler ve Maks Performans a\u00e7\u0131ls\u0131n m\u0131?</html>";
        int choice = JOptionPane.showConfirmDialog(this, msg,
            isEn ? "Mod Conflicts" : "Mod \u00c7ak\u0131\u015fmas\u0131",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            this.log(isEn ? "Maximum Performance cancelled - conflicting mods kept." : "Maks Performans iptal edildi - \u00e7ak\u0131\u015fan modlar korundu.");
            return false;
        }
        int removed = 0;
        for (ModManager.InstalledMod m : conflicts) {
            try {
                ModManager.removeMod(modsDir, m);
                this.log("- " + (m.projectTitle != null ? m.projectTitle : m.fileName) + (isEn ? " removed (conflict)" : " silindi (\u00e7ak\u0131\u015fma)"));
                removed++;
            }
            catch (Exception exception) {
                this.log((isEn ? "Could not remove: " : "Silinemedi: ") + m.fileName);
            }
        }
        final int finalRemoved = removed;
        SwingUtilities.invokeLater(() -> this.modsPanel.refreshInstalled());
        this.log(isEn ? "[Maks Performans] " + finalRemoved + " conflicting mod(s) removed." : "[Maks Performans] " + finalRemoved + " \u00e7ak\u0131\u015fan mod silindi.");
        return true;
    }

    private void installMaxPerformanceMods() {
        if (this.currentInstance == null) {
            this.log("Aktif instance yok - performans modlar\u0131 kurulamad\u0131.");
            return;
        }
        String loaderName = this.currentInstance.loader;
        String mcVersion = this.currentInstance.lastVersion;
        File modsDir = this.currentInstance.modsDir();
        modsDir.mkdirs();
// V35.3: ayni anda iki kurulum dizisi calismasin (surum degisimi ile
        // cakisma -> yanlis surum jar). Onceki tur hala calisiyorsa reddet.
        if (this.maxPerfInstallBusy.get()) {
            this.log("[Maks Performans] Onceki kurulum hala suruyor - bekleyin.");
            return;
        }
        this.maxPerfInstallBusy.set(true);
        final long startEpoch = ModManager.epochNow();
        List<PerformanceFlags.PerfMod> mods = PerformanceFlags.recommendedPerformanceMods(loaderName);
        // V34.8 HIZ: tum modlarin Modrinth version listelerini PARALEL one
        // tusle - installProject her mod icin RTT beklemek yerine sicak
        // cache'ten okur. Kurulumun kendisi ayni sira + ayni guvenlik
        // kontrolleriyle yapilir (dogruluk degismez).
        java.util.List<String> warmIds = new ArrayList<>();
        for (PerformanceFlags.PerfMod mod : mods) {
            warmIds.add(mod.slug);
        }
        ModManager.warmVersionCacheAsync(warmIds, loaderName, mcVersion);
        new Thread(() -> {
            int installed = 0;
            int skipped = 0;
            // V39.1: Maks Performans KULLANICININ ACIK ISTEGI — bu dongude
            // oto-kurulum kapisi + opt-out listesi bypass edilir. Boylece
            // kullanici mod panelindeki tiklerden bagimsiz olarak Sodium,
            // Iris, Sodium Extra, More Culling vb. hepsi kurulur. Bypass
            // SADECE bu dongu icin gecerli; baska otomatik yollar etkilenmez.
            ModManager.setForceInstallBypass(true);
            try {
                final boolean isEn = L10n.isEnglish();
                // V35.3 CANLI LISTE: her mod log paneline yazilir - "[1/12] Sodium
                // kuruluyor..." -> "[1/12] Sodium ✓ kuruldu". Kullanici hangi
                // modun ne yaptigini gercek zamanli gorur.
                int total = mods.size();
                int idx = 0;
                for (PerformanceFlags.PerfMod mod : mods) {
                    idx++;
                    final int num = idx;
                    final int tot = total;
                    final String title = mod.title;
                    SwingUtilities.invokeLater(() -> this.log("[" + num + "/" + tot + "] " + title + " - " + (isEn ? "installing..." : "kuruluyor...")));
                    try {
                        if (ModManager.epochNow() != startEpoch) {
                            SwingUtilities.invokeLater(() -> this.log("  " + (isEn ? "Cancelled - MC version changed." : "Iptal - MC surumu degisti.")));
                            return;
                        }
                        boolean ok = ModManager.installProject(modsDir, mod.slug, loaderName, mcVersion);
                        if (ok) {
                            installed++;
                            SwingUtilities.invokeLater(() -> this.log("[" + num + "/" + tot + "] " + title + " - " + (isEn ? "✓ installed" : "✓ kuruldu")));
                        } else {
                            skipped++;
                            SwingUtilities.invokeLater(() -> this.log("[" + num + "/" + tot + "] " + title + " - " + (isEn ? "skipped (gate/already installed)" : "atlandi (kapi/zaten kurulu)")));
                        }
                    }
                    catch (Exception exception) {
                        skipped++;
                        final String errMsg = exception.getMessage();
                        SwingUtilities.invokeLater(() -> this.log("[" + num + "/" + tot + "] " + title + " - " + (isEn ? "✗ FAILED: " : "✗ HATA: ") + errMsg));
                    }
                }
            }
            finally {
                ModManager.setForceInstallBypass(false);
                final int fi = installed;
                final int fs = skipped;
                SwingUtilities.invokeLater(() -> {
                    this.log("[Maks Performans] " + fi + " mod kuruldu, " + fs + " atlandi.");
                    this.modsPanel.refreshInstalled();
                    this.maxPerfInstallBusy.set(false);
                });
            }
        }, "max-performance-install").start();
    }

    private void confirmAndDeleteLauncherData() {
        boolean bl = L10n.isEnglish();
        File dataDir = Paths.GAME_DIR;
        int first = JOptionPane.showConfirmDialog(this,
            bl
                ? "<html>This will permanently delete <b>all instances, mods, accounts and settings</b><br>stored in:<br><code>" + dataDir.getAbsolutePath() + "</code><br><br>This cannot be undone. Continue?</html>"
                : "<html><b>T\u00fcm instance'lar, modlar, hesaplar ve ayarlar</b> kal\u0131c\u0131 olarak silinecek:<br><code>" + dataDir.getAbsolutePath() + "</code><br><br>Bu i\u015flem geri al\u0131namaz. Devam etmek istiyor musunuz?</html>",
            bl ? "Delete Launcher Data" : "Launcher Verilerini Sil",
            2, 2);
        if (first != 0) {
            return;
        }
        String confirmWord = bl ? "DELETE" : "SIL";
        String typed = JOptionPane.showInputDialog(this,
            bl ? "Type \"" + confirmWord + "\" to confirm:" : "Onaylamak i\u00e7in \"" + confirmWord + "\" yaz\u0131n:");
        if (typed == null || !typed.trim().equalsIgnoreCase(confirmWord)) {
            this.log(bl ? "Launcher deletion cancelled." : "Launcher silme i\u015flemi iptal edildi.");
            return;
        }
        try {
            if (this.currentGameProcess != null && this.currentGameProcess.isAlive()) {
                JOptionPane.showMessageDialog(this, bl ? "Please close the running game first." : "L\u00fctfen \u00f6nce a\u00e7\u0131k olan oyunu kapat\u0131n.", bl ? "Game Running" : "Oyun \u00c7al\u0131\u015f\u0131yor", 2);
                return;
            }
            MainWindow.deleteDir(dataDir);
            JOptionPane.showMessageDialog(this,
                bl ? "All launcher data has been deleted. The launcher will now close." : "T\u00fcm launcher verileri silindi. Launcher kapat\u0131l\u0131yor.",
                bl ? "Done" : "Tamamland\u0131", 1);
        }
        catch (Exception exception) {
            JOptionPane.showMessageDialog(this, (bl ? "Could not fully delete data: " : "Veriler tam olarak silinemedi: ") + exception.getMessage(), bl ? "Error" : "Hata", 0);
        }
        finally {
            System.exit(0);
        }
    }

    private void handleGameExit(int n, long l) {
        this.handleGameExit(n, l, this.currentInstance != null ? this.currentInstance.name : null);
    }

    /**
     * Oyun kapandiginda oynanan sureyi ilgili instance'a ekler.
     * ONEMLI DUZELTME: onceden bu metot dogrudan this.currentInstance
     * kullaniyordu. Ama oyun acikken (dakikalarca surebilir) kullanici
     * launcher ekraninda BASKA bir instance'a gecebiliyor - bu durumda
     * sure ya yanlis instance'a ekleniyor ya da hic eklenmiyor gibi
     * gorunuyordu ("oynama suresi olcumu calismiyor" sikayetinin
     * kaynagi buydu). Artik oyun baslatilirken hangi instance
     * calistirildiysa, kapanista suresi ozellikle o instance'a
     * (isim uzerinden, guncel haliyle diskten tekrar okunarak) eklenir.
     */
    private void handleGameExit(int n, long l, String launchedInstanceName) {
        if (launchedInstanceName != null && !launchedInstanceName.isBlank()) {
            Instance target = Instance.load(launchedInstanceName);
            target.totalPlaySeconds += l;
            target.lastPlayedMs = System.currentTimeMillis();
            target.save();
            // Eger kullanici hala ayni instance'i goruntuluyorsa, ekrandaki
            // nesneyi de guncelleyelim ki UI hemen yeni degeri gostersin.
            if (this.currentInstance != null && launchedInstanceName.equals(this.currentInstance.name)) {
                this.currentInstance.totalPlaySeconds = target.totalPlaySeconds;
                this.currentInstance.lastPlayedMs = target.lastPlayedMs;
            }
            this.log("[S\u00fcre] " + launchedInstanceName + " \u2192 +" + MainWindow.formatPlayTimeLocal(l) + "  |  Toplam: " + MainWindow.formatPlayTimeLocal(target.totalPlaySeconds));
        }
        // V36 GERI ALMA: oyun normal kapandiysa (exit 0) mod seti calisiyor
        // demektir - rollback snapshot'i artik gereksiz, temizle.
        if (n == 0 && this.currentInstance != null) {
            try {
                com.lubv.launcher.mods.ModRollbackManager.clearSnapshot(this.currentInstance.modsDir());
            }
            catch (Throwable ignored) {
            }
        }
        this.analyzeGcPerformance(l);
    }

    private static String formatPlayTimeLocal(long l) {
        if (l < 60L) {
            return l + "s";
        }
        if (l < 3600L) {
            return l / 60L + "dk";
        }
        return l / 3600L + "sa " + l % 3600L / 60L + "dk";
    }

    private void analyzeGcPerformance(long l) {
        if (this.currentInstance == null) {
            return;
        }
        File file = new File(new File(Paths.INSTANCES_DIR, this.currentInstance.name), "launcher_game_output.log");
        if (!file.exists()) {
            return;
        }
        new Thread(() -> {
            try {
                String string = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                GcReport gcReport = MainWindow.parseGcLog(string, l, this.currentInstance != null ? this.currentInstance.ramGB : 4);
                if (gcReport != null && gcReport.hasIssue()) {
                    SwingUtilities.invokeLater(() -> this.showGcReport(gcReport));
                } else if (gcReport != null) {
                    this.log("[Performans] GC analizi tamam \u2014 sorun yok (" + gcReport.majorCount + " major GC, max pause: " + gcReport.maxPauseMs + "ms)");
                }
            }
            catch (Exception exception) {
                this.log("[Performans] GC log okunamad\u0131: " + exception.getMessage());
            }
        }, "gc-analyzer").start();
    }

    private static GcReport parseGcLog(String string, long l, int n) {
        GcReport gcReport = new GcReport();
        gcReport.sessionSec = l;
        gcReport.ramGB = n;
        Pattern[] patternArray = new Pattern[]{Pattern.compile("\\[GC pause.*?(\\d+\\.\\d+)\\s*ms\\]"), Pattern.compile("Pause\\s+\\w+\\s+(\\d+)ms"), Pattern.compile("GC\\((\\d+)\\).*?Pause\\s+(\\d+\\.\\d+)ms"), Pattern.compile("\\[GC\\s+(\\d+)ms\\]"), Pattern.compile("\\[Full GC.*?(\\d+\\.\\d+)\\s*ms\\]"), Pattern.compile("Pause Full.*?(\\d+\\.\\d+)\\s*ms")};
        boolean[] blArray = new boolean[]{false, false, false, false, true, true};
        for (int i = 0; i < patternArray.length; ++i) {
            Matcher matcher = patternArray[i].matcher(string);
            while (matcher.find()) {
                try {
                    double d = Double.parseDouble(matcher.group(matcher.groupCount()));
                    gcReport.totalPauseMs = (long)((double)gcReport.totalPauseMs + d);
                    ++gcReport.gcCount;
                    if (blArray[i]) {
                        ++gcReport.fullGcCount;
                    }
                    if (d > (double)gcReport.maxPauseMs) {
                        gcReport.maxPauseMs = (long)d;
                    }
                    if (d > 500.0) {
                        ++gcReport.longPauseCount;
                    }
                    if (!(d > 200.0)) continue;
                    ++gcReport.majorCount;
                }
                catch (Exception exception) {}
            }
        }
        gcReport.oom = string.contains("OutOfMemoryError") || string.contains("java.lang.OutOfMemoryError");
        gcReport.gcOverhead = string.contains("GCOverheadLimit") || string.contains("GC overhead limit exceeded");
        Matcher matcher = Pattern.compile("Heap.*?(\\d+)M->.*?\\((\\d+)M\\)").matcher(string);
        if (matcher.find()) {
            try {
                int n2 = Integer.parseInt(matcher.group(1));
                int n3 = Integer.parseInt(matcher.group(2));
                gcReport.heapUsagePercent = (int)(100.0 * (double)n2 / (double)n3);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return gcReport;
    }

    private void showGcReport(GcReport gcReport) {
        boolean bl = L10n.isEnglish();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(bl ? "Performance Report \u2014 " : "Performans Raporu \u2014 ").append(this.currentInstance != null ? this.currentInstance.name : "").append("\n\n");
        if (gcReport.oom) {
            stringBuilder.append(bl ? "\u26a0 OutOfMemoryError detected! Increase RAM allocation.\n" : "\u26a0 OutOfMemoryError tespit edildi! RAM art\u0131r\u0131n.\n");
        }
        if (gcReport.gcOverhead) {
            stringBuilder.append(bl ? "\u26a0 GC overhead limit exceeded \u2014 game spent too much time on garbage collection.\n" : "\u26a0 GC overhead limit a\u015f\u0131ld\u0131 \u2014 oyun \u00e7ok fazla GC'ye zaman harc\u0131yor.\n");
        }
        if (gcReport.fullGcCount > 0) {
            stringBuilder.append(bl ? "\u26a0 Full GC count: " + gcReport.fullGcCount + " \u2014 this causes severe lag spikes.\n" : "\u26a0 Full GC say\u0131s\u0131: " + gcReport.fullGcCount + " \u2014 ciddi lag spike'a sebep olur.\n");
        }
        if (gcReport.longPauseCount > 3) {
            stringBuilder.append(bl ? "\u26a0 " + gcReport.longPauseCount + " GC pauses > 500ms \u2014 frequent freezes.\n" : "\u26a0 " + gcReport.longPauseCount + " GC pause > 500ms \u2014 s\u0131k donma ya\u015fand\u0131.\n");
        }
        if (gcReport.maxPauseMs > 1000L) {
            stringBuilder.append(bl ? "\u26a0 Longest GC pause: " + gcReport.maxPauseMs + "ms \u2014 game likely froze.\n" : "\u26a0 En uzun GC pause: " + gcReport.maxPauseMs + "ms \u2014 oyun b\u00fcy\u00fck ihtimalle dondu.\n");
        }
        stringBuilder.append("\n").append(bl ? "\ud83d\udca1 Recommendations:\n" : "\ud83d\udca1 \u00d6neriler:\n");
        if (gcReport.oom || gcReport.heapUsagePercent > 85) {
            stringBuilder.append(bl ? "  \u2022 Increase RAM to " + (gcReport.ramGB + 2) + "GB (currently " + gcReport.ramGB + "GB)\n" : "  \u2022 RAM'i " + (gcReport.ramGB + 2) + "GB'a art\u0131r\u0131n (\u015fu an " + gcReport.ramGB + "GB)\n");
        }
        if (gcReport.fullGcCount > 0 || gcReport.majorCount > 10) {
            stringBuilder.append(bl ? "  \u2022 Try 'Aikar's G1GC' profile from JVM Profiles menu\n" : "  \u2022 JVM Profilleri men\u00fcs\u00fcnden 'Aikar's G1GC' profilini deneyin\n");
        }
        if (gcReport.maxPauseMs > 500L && gcReport.ramGB >= 8) {
            stringBuilder.append(bl ? "  \u2022 With 8GB+ RAM, ZGC Low Latency profile may reduce lag\n" : "  \u2022 8GB+ RAM'de ZGC D\u00fc\u015f\u00fck Gecikme profili lag'\u0131 azaltabilir\n");
        }
        stringBuilder.append("\n").append(bl ? "\ud83d\udcca Stats:\n" : "\ud83d\udcca \u0130statistikler:\n");
        stringBuilder.append(bl ? "  \u2022 Session: " : "  \u2022 Oturum: ").append(MainWindow.formatPlayTimeLocal(gcReport.sessionSec)).append("\n");
        stringBuilder.append(bl ? "  \u2022 GC count: " : "  \u2022 GC say\u0131s\u0131: ").append(gcReport.gcCount).append("\n");
        stringBuilder.append(bl ? "  \u2022 Total GC pause: " : "  \u2022 Toplam GC pause: ").append((String)(gcReport.totalPauseMs > 1000L ? String.format("%.1fs", (double)gcReport.totalPauseMs / 1000.0) : gcReport.totalPauseMs + "ms")).append("\n");
        stringBuilder.append(bl ? "  \u2022 Max pause: " : "  \u2022 Max pause: ").append(gcReport.maxPauseMs).append("ms\n");
        if (gcReport.heapUsagePercent > 0) {
            stringBuilder.append(bl ? "  \u2022 Heap usage: " : "  \u2022 Heap kullan\u0131m\u0131: ").append(gcReport.heapUsagePercent).append("%\n");
        }
        this.log("[Performans] " + (bl ? "Report ready" : "Rapor haz\u0131r") + " \u2014 " + gcReport.gcCount + " GC, max " + gcReport.maxPauseMs + "ms");
        JOptionPane.showMessageDialog(this, stringBuilder.toString(), bl ? "Performance Report" : "Performans Raporu", gcReport.oom || gcReport.fullGcCount > 0 ? 2 : 1);
    }

    /**
     * V36 GERI ALMA: crash sonrasi snapshot varsa kullaniciya tek tikla
     * geri donus offered eder. true donerse crash akisi biter (VulkanMod
     * gibi ozel fix'ler veya genel dialog gosterilmez).
     */
    private boolean offerModRollbackOnCrash() {
        try {
            if (this.currentInstance == null) return false;
            File modsDir = this.currentInstance.modsDir();
            if (!com.lubv.launcher.mods.ModRollbackManager.hasSnapshot(modsDir)) return false;
            String snapMc = com.lubv.launcher.mods.ModRollbackManager.snapshotMcVersion(modsDir);
            boolean en = L10n.isEnglish();
            String msg = en
                ? "<html><b>The game crashed.</b><br>A mod-set backup from MC " + (snapMc == null ? "?" : snapMc) + " was taken before the last version switch.<br>Roll back to that working mod set?</html>"
                : "<html><b>Oyun çöktü.</b><br>Son sürüm değişikliğinden önce MC " + (snapMc == null ? "?" : snapMc) + " için alınmış mod yedeği var.<br>Çalışan mod setine geri dönmek ister misin?</html>";
            int choice = JOptionPane.showConfirmDialog(this, msg,
                en ? "Roll back mods?" : "Modlar geri alinsin mi?", 0, 3);
            if (choice != 0) return false;
            int restored;
            try {
                restored = com.lubv.launcher.mods.ModRollbackManager.restore(modsDir);
            }
            catch (Exception rErr) {
                this.log("[Geri Alma] HATA: " + rErr.getMessage());
                JOptionPane.showMessageDialog(this, en ? "Rollback failed: " + rErr.getMessage() : "Geri alma basarisiz: " + rErr.getMessage(),
                    en ? "Rollback" : "Geri Alma", 0);
                return false;
            }
            this.modsPanel.refreshInstalled();
            this.log("[Geri Alma] " + restored + " mod geri yuklendi (MC " + (snapMc == null ? "?" : snapMc) + " seti). Tekrar Oyna'ya bas.");
            return true;
        }
        catch (Throwable t) {
            return false; // geri-alma teklifi hicbir sekilde crash akisini bozmasin
        }
    }

    private void handleCrash(String string, String string2, String string3, String string4) {
        this.log("[CRASH] " + string + " \u2014 " + string2);
        this.saveCrashHistory(string, string2, string3);
        // V33.3 CRASH WATCHDOG: VulkanMod bilinen bir sekilde cokuyor -
        // Vulkan baslatamayan sistemlerde render thread'i
        // "OutOfMemoryError: Out of stack space" ileGAME_CRASH üretir
        // (Vulkan.createInstance -> MemoryStack.nmalloc). Bu imzayi
        // gorursek: VulkanMod pref'ini KAPAT, mods/ klasorundeki
        // VulkanMod jar'ini sil ve kullaniciya net bir Turkce/Ingilizce
        // aciklama goster. Boylece tekrar Oyna'ya basinca Sodium'a duser
        // ve oyun acilir - kullanici hicbir dosya ile ugrasmaz.
        String crashBlob = (string + " " + string2 + " " + string3 + " " + (string4 == null ? "" : string4)).toLowerCase();
        if (crashBlob.contains("vulkanmod") || crashBlob.contains("net.vulkanmod") || (crashBlob.contains("outofmemoryerror") && crashBlob.contains("stack space"))) {
            boolean removed = false;
            try {
                if (this.currentInstance != null) {
                    File modsDir = this.currentInstance.modsDir();
                    File[] jars = modsDir.listFiles((d, nm) -> nm.toLowerCase().startsWith("vulkanmod") && nm.toLowerCase().endsWith(".jar"));
                    if (jars != null) {
                        for (File j : jars) {
                            removed = j.delete() || removed;
                        }
                    }
                }
            }
            catch (Exception ignored) {
            }
            JsonObject prefs = MainWindow.loadModPrefs();
            prefs.addProperty("vulkanmod", false);
            MainWindow.saveModPrefs(prefs);
            final boolean fRemoved = removed;
            SwingUtilities.invokeLater(() -> {
                String msg = L10n.isEnglish()
                    ? "VulkanMod crashed while starting its Vulkan renderer.\nYour GPU/driver may not support Vulkan here.\n\n" + (fRemoved ? "VulkanMod was removed from this instance and its auto-install is now OFF.\n" : "Its auto-install is now OFF.\n") + "Press Play again \u2014 the game will start with the stable renderer (Sodium)."
                    : "VulkanMod, Vulkan render'\u0131n\u0131 ba\u015flat\u0131rken \u00e7\u00f6kt\u00fc.\nEkran kart\u0131n/s\u00fcr\u00fcc\u00fcn burada Vulkan desteklemiyor olabilir.\n\n" + (fRemoved ? "VulkanMod bu instance'dan kald\u0131r\u0131ld\u0131 ve otomatik kurulumu KAPATILDI.\n" : "Otomatik kurulumu KAPATILDI.\n") + "Tekrar Oyna'ya bas \u2014 oyun stabil renderer ile (Sodium) a\u00e7\u0131lacak.";
                JOptionPane.showMessageDialog(this, msg, L10n.isEnglish() ? "VulkanMod disabled" : "VulkanMod kapat\u0131ld\u0131", 2);
            });
            return;
        }
        // V36 GERI ALMA: yakin zamanda surum guncellemesi yapildiysa ve
        // snapshot varsa, crash dialog'unda TEK TIKLA geri donus offered.
        if (this.offerModRollbackOnCrash()) {
            return; // kullaniciya dialog gosterildi; geri aldiysa moda gerek yok
        }
        SwingUtilities.invokeLater(() -> {
            Object[] objectArray;
            String dialogTitle = "JVM_CRASH".equals(string) ? L10n.get("crash.jvm_title") : L10n.get("crash.title");
            String string5 = "<html><b>" + string2 + "</b><br><br>" + string3.replace("\n", "<br>") + "<br><br><small>" + L10n.get("crash.unknown").replace("Bilinmeyen crash sebebi.", "").replace("Unknown crash cause.", "") + "</small></html>";
            int n = JOptionPane.showOptionDialog(this, string5, dialogTitle, -1, 0, null, objectArray = new Object[]{L10n.get("crash.open_report"), L10n.get("crash.history_title"), L10n.get("ok")}, objectArray[2]);
            if (n == 0) {
                this.openCrashReportFile(string2);
            } else if (n == 1) {
                this.showCrashHistoryDialog();
            }
        });
    }

    private void saveCrashHistory(String string, String string2, String string3) {
        if (this.currentInstance == null) {
            return;
        }
        try {
            Object object;
            Object object2;
            File file = new File(new File(Paths.INSTANCES_DIR, this.currentInstance.name), "crash-history.json");
            JsonArray jsonArray = new JsonArray();
            if (file.exists()) {
                try {
                    object2 = new FileReader(file);
                    try {
                        object = JsonParser.parseReader((Reader)object2);
                        if (((JsonElement)object).isJsonArray()) {
                            jsonArray = ((JsonElement)object).getAsJsonArray();
                        }
                    }
                    finally {
                        ((InputStreamReader)object2).close();
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            object2 = new JsonObject();
            ((JsonObject)object2).addProperty("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            ((JsonObject)object2).addProperty("type", string);
            ((JsonObject)object2).addProperty("file", string2);
            ((JsonObject)object2).addProperty("summary", string3);
            jsonArray.add((JsonElement)object2);
            while (jsonArray.size() > 20) {
                jsonArray.remove(0);
            }
            file.getParentFile().mkdirs();
            object = new FileWriter(file);
            try {
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)jsonArray, (Appendable)object);
            }
            finally {
                ((OutputStreamWriter)object).close();
            }
        }
        catch (Exception exception) {
            this.log("[CrashHistory] Kaydedilemedi: " + exception.getMessage());
        }
    }

    private void showCrashHistoryDialog() {
        if (this.currentInstance == null) {
            JOptionPane.showMessageDialog(this, L10n.get("instance.select_first"), L10n.get("error"), 2);
            return;
        }
        File file = new File(new File(Paths.INSTANCES_DIR, this.currentInstance.name), "crash-history.json");
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, L10n.get("crash.no_history"), L10n.get("crash.history_title"), 1);
            return;
        }
        try {
            JsonArray jsonArray;
            try (FileReader fileReader = new FileReader(file);){
                jsonArray = JsonParser.parseReader(fileReader).getAsJsonArray();
            }
            if (jsonArray.size() == 0) {
                JOptionPane.showMessageDialog(this, L10n.get("crash.no_history"), L10n.get("crash.history_title"), 1);
                return;
            }
            StringBuilder tableBuilder = new StringBuilder("<html><table border='0' cellpadding='4'><tr><th>Tarih</th><th>T\u00fcr</th><th>Dosya</th><th>\u00d6zet</th></tr>");
            for (int i = jsonArray.size() - 1; i >= 0; --i) {
                JsonObject crashEntry = jsonArray.get(i).getAsJsonObject();
                tableBuilder.append("<tr><td>").append(crashEntry.has("time") ? crashEntry.get("time").getAsString() : "?").append("</td><td>").append(crashEntry.has("type") ? crashEntry.get("type").getAsString() : "?").append("</td><td>").append(crashEntry.has("file") ? crashEntry.get("file").getAsString() : "?").append("</td><td>").append(crashEntry.has("summary") ? crashEntry.get("summary").getAsString().replace("\n", " ").substring(0, Math.min(60, crashEntry.get("summary").getAsString().length())) : "").append("</td></tr>");
            }
            tableBuilder.append("</table></html>");
            JDialog jDialog = new JDialog(this, L10n.get("crash.history_title"), false);
            jDialog.setSize(700, 340);
            jDialog.setLocationRelativeTo(this);
            JEditorPane editorPane = new JEditorPane("text/html", tableBuilder.toString());
            editorPane.setEditable(false);
            editorPane.setBackground(Theme.BG_SURFACE);
            editorPane.setForeground(Theme.TEXT_PRIMARY);
            jDialog.add(new JScrollPane((Component)editorPane));
            jDialog.setVisible(true);
        }
        catch (Exception exception) {
            this.log("[CrashHistory] Okunamad\u0131: " + exception.getMessage());
        }
    }

    private void openCrashReportFile(String string) {
        if (this.currentInstance == null) {
            return;
        }
        File file = new File(Paths.INSTANCES_DIR, this.currentInstance.name);
        File file2 = new File(file, "crash-reports/" + string);
        if (!file2.exists()) {
            file2 = new File(System.getProperty("user.dir"), string);
        }
        if (!file2.exists()) {
            this.log("[Crash] Dosya bulunamad\u0131: " + string);
            return;
        }
        try {
            Desktop.getDesktop().open(file2);
        }
        catch (Exception exception) {
            this.log("[Crash] Dosya a\u00e7\u0131lamad\u0131: " + exception.getMessage());
        }
    }

    private void showSkinUploaderDialog() {
        if (this.session == null) {
            JOptionPane.showMessageDialog(this, L10n.get("skin.no_account"), L10n.get("skin.title"), 2);
            return;
        }
        JDialog jDialog = new JDialog(this, L10n.get("skin.title"), true);
        jDialog.setSize(360, 200);
        jDialog.setLocationRelativeTo(this);
        jDialog.setLayout(new BorderLayout(10, 10));
        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.setBackground(Theme.BG_SURFACE);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(6, 8, 6, 8);
        gridBagConstraints.fill = 2;
        File[] fileArray = new File[]{null};
        JLabel jLabel = UiFx.label("\u2014");
        JCheckBox jCheckBox = new JCheckBox(L10n.get("skin.slim_model"));
        jCheckBox.setOpaque(false);
        jCheckBox.setForeground(Theme.TEXT_SECONDARY);
        JButton jButton = UiFx.ghostButton(L10n.get("skin.choose_file"));
        jButton.addActionListener(actionEvent -> {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setFileFilter(new FileNameExtensionFilter("PNG Image", "png"));
            if (jFileChooser.showOpenDialog(jDialog) == 0) {
                fileArray[0] = jFileChooser.getSelectedFile();
                jLabel.setText(fileArray[0].getName());
            }
        });
        JButton jButton2 = UiFx.accentButton(L10n.get("skin.apply"));
        jButton2.addActionListener(actionEvent -> {
            if (fileArray[0] == null || !fileArray[0].getName().toLowerCase().endsWith(".png")) {
                JOptionPane.showMessageDialog(jDialog, L10n.get("skin.invalid_file"), L10n.get("error"), 2);
                return;
            }
            try {
                byte[] byArray = Files.readAllBytes(fileArray[0].toPath());
                String string = Base64.getEncoder().encodeToString(byArray);
                boolean bl = jCheckBox.isSelected();
                File file = new File(new File(Paths.INSTANCES_DIR, this.currentInstance != null ? this.currentInstance.name : "default"), "skin.png");
                file.getParentFile().mkdirs();
                Files.copy(fileArray[0].toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                String string2 = "data:image/png;base64," + string;
                SessionServerMock.SkinData skinData = new SessionServerMock.SkinData(this.session.uuid, this.session.username, string2);
                SessionServerMock.setSkin(this.session.uuid, skinData);
                this.log("[Skin] " + L10n.get("skin.applied") + " \u2014 " + fileArray[0].getName());
                JOptionPane.showMessageDialog(jDialog, L10n.get("skin.applied"), L10n.get("skin.title"), 1);
                jDialog.dispose();
            }
            catch (Exception exception) {
                JOptionPane.showMessageDialog(jDialog, exception.getMessage(), L10n.get("error"), 0);
            }
        });
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        jPanel.add((Component)jButton, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        jPanel.add((Component)jLabel, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        jPanel.add((Component)jCheckBox, gridBagConstraints);
        JPanel jPanel2 = new JPanel(new FlowLayout(2));
        jPanel2.setBackground(Theme.BG_SURFACE);
        jPanel2.add(jButton2);
        jDialog.add((Component)jPanel, "Center");
        jDialog.add((Component)jPanel2, "South");
        jDialog.setVisible(true);
    }

    private void showWorldManagerDialog() {
        Object object;
        if (this.currentInstance == null) {
            JOptionPane.showMessageDialog(this, L10n.get("instance.select_first"), L10n.get("error"), 2);
            return;
        }
        File file = new File(new File(Paths.INSTANCES_DIR, this.currentInstance.name), "saves");
        JDialog jDialog = new JDialog(this, L10n.get("world.title"), true);
        jDialog.setSize(440, 320);
        jDialog.setLocationRelativeTo(this);
        jDialog.setLayout(new BorderLayout(8, 8));
        JPanel jPanel = new JPanel(new FlowLayout(0, 8, 8));
        jPanel.setBackground(Theme.BG_SURFACE);
        JButton jButton = UiFx.accentButton(L10n.get("world.import"));
        JButton jButton2 = UiFx.ghostButton(L10n.get("world.export"));
        jPanel.add(jButton);
        jPanel.add(jButton2);
        DefaultListModel<String> defaultListModel = new DefaultListModel<String>();
        File[] worldDirs = file.isDirectory() ? file.listFiles(File::isDirectory) : null;
        if (worldDirs != null) {
            for (File worldDir : worldDirs) {
                defaultListModel.addElement(worldDir.getName());
            }
        }
        object = new JList(defaultListModel);
        ((JComponent)object).setBackground(Theme.BG_SURFACE);
        ((JComponent)object).setForeground(Theme.TEXT_PRIMARY);
        ((JList)object).setSelectionBackground(Theme.ACCENT);
        jButton.addActionListener(actionEvent -> {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setDialogTitle(L10n.get("world.import"));
            jFileChooser.setFileFilter(new FileNameExtensionFilter("ZIP / World Folder", "zip"));
            jFileChooser.setFileSelectionMode(2);
            if (jFileChooser.showOpenDialog(jDialog) != 0) {
                return;
            }
            File file2 = jFileChooser.getSelectedFile();
            try {
                file.mkdirs();
                if (file2.isDirectory()) {
                    File file3 = new File(file, file2.getName());
                    MainWindow.copyDir(file2, file3);
                    defaultListModel.addElement(file3.getName());
                    this.log(L10n.fmt("world.import_success", file3.getName()));
                } else if (file2.getName().endsWith(".zip")) {
                    String string = file2.getName().replace(".zip", "");
                    File file4 = new File(file, string);
                    MainWindow.unzipTo(file2, file4);
                    defaultListModel.addElement(file4.getName());
                    this.log(L10n.fmt("world.import_success", file4.getName()));
                }
            }
            catch (Exception exception) {
                this.log(L10n.fmt("world.import_failed", exception.getMessage()));
            }
        });
        jButton2.addActionListener(arg_0 -> this.onWorldManagerAction((JList)object, jDialog, file, arg_0));
        jDialog.add((Component)jPanel, "North");
        jDialog.add((Component)new JScrollPane((Component)object), "Center");
        jDialog.setVisible(true);
    }

    private static void copyDir(File file, File file2) throws IOException {
        if (file.isDirectory()) {
            file2.mkdirs();
            for (File file3 : file.listFiles()) {
                MainWindow.copyDir(file3, new File(file2, file3.getName()));
            }
        } else {
            file2.getParentFile().mkdirs();
            Files.copy(file.toPath(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void zipDir(File file, File file2) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(file2));){
            MainWindow.zipDirRecursive(file, file.getName(), zipOutputStream);
        }
    }

    private static void zipDirRecursive(File file, String string, ZipOutputStream zipOutputStream) throws IOException {
        if (file.isDirectory()) {
            zipOutputStream.putNextEntry(new ZipEntry(string + "/"));
            zipOutputStream.closeEntry();
            for (File file2 : file.listFiles()) {
                MainWindow.zipDirRecursive(file2, string + "/" + file2.getName(), zipOutputStream);
            }
        } else {
            zipOutputStream.putNextEntry(new ZipEntry(string));
            Files.copy(file.toPath(), zipOutputStream);
            zipOutputStream.closeEntry();
        }
    }

    private static void unzipTo(File file, File file2) throws IOException {
        file2.mkdirs();
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));){
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                File file3 = new File(file2, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    file3.mkdirs();
                } else {
                    file3.getParentFile().mkdirs();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(file3);){
                        int n;
                        byte[] byArray = new byte[8192];
                        while ((n = zipInputStream.read(byArray)) != -1) {
                            fileOutputStream.write(byArray, 0, n);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        }
    }

    private /* synthetic */ void onWorldManagerAction(JList jList, JDialog jDialog, File file, ActionEvent actionEvent) {
        String string = (String)jList.getSelectedValue();
        if (string == null) {
            JOptionPane.showMessageDialog(jDialog, L10n.get("world.select"), L10n.get("world.export"), 1);
            return;
        }
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setDialogTitle(L10n.get("world.export"));
        jFileChooser.setSelectedFile(new File(string + ".zip"));
        if (jFileChooser.showSaveDialog(jDialog) != 0) {
            return;
        }
        File file2 = jFileChooser.getSelectedFile();
        if (!file2.getName().endsWith(".zip")) {
            file2 = new File(file2.getPath() + ".zip");
        }
        try {
            File file3 = new File(file, string);
            MainWindow.zipDir(file3, file2);
            this.log(L10n.fmt("world.export_success", file2.getName()));
            JOptionPane.showMessageDialog(jDialog, L10n.fmt("world.export_success", file2.getAbsolutePath()), L10n.get("world.export"), 1);
        }
        catch (Exception exception) {
            this.log(L10n.fmt("world.export_failed", exception.getMessage()));
        }
    }

    private /* synthetic */ void onAutoUpdateModDone(int n, String string, List list, List list2) {
        this.onAutoUpdateStep3(n, string, list, list2);
    }

    private /* synthetic */ void onAutoUpdateModProgress(String string, int n, String string2) {
        this.onAutoUpdateStep1(string, n, string2);
    }

    private /* synthetic */ void onAutoUpdateModStart(String string) {
        this.onAutoUpdateStep0(string);
    }

    private /* synthetic */ void onJvmProfileSelect(String string, ActionEvent actionEvent) {
        if (this.jvmArgsField != null) {
            this.jvmArgsField.setText(string);
            if (this.currentInstance != null) {
                this.currentInstance.jvmArgs = string;
                this.currentInstance.save();
            }
        }
    }

    private static /* synthetic */ void onModProfileToggle(JCheckBox jCheckBox2, JCheckBox jCheckBox3, JCheckBox jCheckBox4, JCheckBox jCheckBox5, JCheckBox jCheckBox6, Map<String, JCheckBox> map, JsonObject jsonObject, ActionEvent actionEvent) {
        boolean bl = jCheckBox2.isSelected();
        if (jCheckBox3 != null) {
            jCheckBox3.setEnabled(!bl);
            if (bl) {
                jCheckBox3.setSelected(false);
            }
        }
        if (jCheckBox4 != null) {
            jCheckBox4.setEnabled(!bl);
            if (bl) {
                jCheckBox4.setSelected(false);
            }
        }
        if (jCheckBox5 != null) {
            jCheckBox5.setEnabled(!bl);
            if (bl) {
                jCheckBox5.setSelected(false);
            }
        }
        if (jCheckBox6 != null) {
            jCheckBox6.setEnabled(!bl);
            if (bl) {
                jCheckBox6.setSelected(false);
            }
        }
        map.values().forEach(jCheckBox -> jsonObject.addProperty(map.entrySet().stream().filter(entry -> entry.getValue() == jCheckBox).map(Map.Entry::getKey).findFirst().orElse(""), jCheckBox.isSelected()));
        MainWindow.saveModPrefs(jsonObject);
    }

    private static /* synthetic */ void handleModPrefCheckbox(JsonObject jsonObject, String string, JCheckBox jCheckBox, ActionEvent actionEvent) {
        jsonObject.addProperty(string, jCheckBox.isSelected());
        MainWindow.saveModPrefs(jsonObject);
        // V36.2: tik degisti -> opt-out listesini aninda tazele (statik
        // baglamdan erisilebildigi gibi, aktif MainWindow varsa)
        MainWindow.getActiveOptOutSync().ifPresent(r -> r.run());
    }

    /** V36.2: aktif pencerenin opt-out senkron islevini dondur. */
    private static java.util.Optional<Runnable> getActiveOptOutSync() {
        MainWindow mw = activeWindowRef == null ? null : activeWindowRef.get();
        return mw != null && mw.syncModPrefsOnChange != null ? java.util.Optional.of(mw.syncModPrefsOnChange) : java.util.Optional.empty();
    }

    private static volatile java.lang.ref.WeakReference<MainWindow> activeWindowRef;

    private /* synthetic */ void handleLoaderButtonClick(String string, ActionEvent actionEvent) {
        this.onBuildHomeTab5(string, actionEvent);
    }

    static {
        try {
            File file = new File(Paths.GAME_DIR, "lang.txt");
            if (file.exists()) {
                String string = new String(Files.readAllBytes(file.toPath())).trim();
                L10n.setLanguage(string);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        MOD_PREFS_FILE = new File(Paths.GAME_DIR, "mod_prefs.json");
        CUSTOM_AUTO_MODS_FILE = new File(Paths.GAME_DIR, "custom_auto_mods.json");
    }

    private class InstanceCard
    extends JPanel {
        private final Instance inst;
        private final Color c1;
        private final Color c2;
        private boolean hover;
        private boolean pressed;
        private float hoverAnim = 0.0f;
        private Timer hoverTimer;
        private static final int SIZE = 110;
        private static final int RADIUS = 16;

        InstanceCard(final Instance instance) {
            this.inst = instance;
            Color[] colorArray = MainWindow.gradientFor(instance.name);
            this.c1 = colorArray[0];
            this.c2 = colorArray[1];
            this.setOpaque(false);
            this.setLayout(null);
            this.setPreferredSize(new Dimension(110, 110));
            this.setCursor(Cursor.getPredefinedCursor(12));
            String string = instance.lastVersion == null || instance.lastVersion.isEmpty() ? (L10n.isEnglish() ? "latest" : "En g\u00fcncel") : instance.lastVersion;
            this.setToolTipText("<html><b>" + instance.name + "</b><br>" + instance.loader + " \u00b7 " + string + "</html>");
            final JButton jButton = new JButton("\u270f");
            jButton.setFont(jButton.getFont().deriveFont(0, 11.0f));
            jButton.setBounds(82, 4, 24, 20);
            jButton.setOpaque(false);
            jButton.setContentAreaFilled(false);
            jButton.setBorderPainted(false);
            jButton.setForeground(new Color(255, 255, 255, 200));
            jButton.setCursor(Cursor.getPredefinedCursor(12));
            jButton.setToolTipText(L10n.isEnglish() ? "Edit" : "D\u00fczenle");
            jButton.setVisible(false);
            jButton.addActionListener(actionEvent -> MainWindow.this.openEditSidebar(Instance.load(instance.name)));
            this.add(jButton);
            this.addMouseListener(new MouseAdapter(){

                @Override
                public void mouseEntered(MouseEvent mouseEvent) {
                    InstanceCard.this.hover = true;
                    jButton.setVisible(true);
                    InstanceCard.this.animateHover(1.0f);
                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {
                    InstanceCard.this.hover = false;
                    jButton.setVisible(false);
                    InstanceCard.this.animateHover(0.0f);
                }

                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
                        // Tek tik: instance'i sec ve ana sayfaya (Home) git -
                        // OYUN BASLAMAZ. Baslatma sadece Home'daki LAUNCH ile.
                        MainWindow.this.switchInstance(instance.name);
                        MainWindow.this.tabs.setSelectedIndex(1);
                    } else if (SwingUtilities.isRightMouseButton(mouseEvent)) {
                        MainWindow.this.showInstanceContextMenu(instance, InstanceCard.this, mouseEvent.getX(), mouseEvent.getY());
                    }
                }

                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    if (mouseEvent.isPopupTrigger()) {
                        MainWindow.this.showInstanceContextMenu(instance, InstanceCard.this, mouseEvent.getX(), mouseEvent.getY());
                    } else if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
                        InstanceCard.this.pressed = true;
                        InstanceCard.this.repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent mouseEvent) {
                    if (mouseEvent.isPopupTrigger()) {
                        MainWindow.this.showInstanceContextMenu(instance, InstanceCard.this, mouseEvent.getX(), mouseEvent.getY());
                    }
                    InstanceCard.this.pressed = false;
                    InstanceCard.this.repaint();
                }
            });
        }

        /** Hover durumuna yumusak (easing) bir gecis animasyonu uygular - premium his icin. */
        private void animateHover(float target) {
            if (this.hoverTimer != null) {
                this.hoverTimer.stop();
            }
            float start = this.hoverAnim;
            long startTime = System.currentTimeMillis();
            this.hoverTimer = new Timer(16, null);
            this.hoverTimer.addActionListener(e -> {
                float progress = Math.min(1.0f, (System.currentTimeMillis() - startTime) / 160.0f);
                float eased = 1.0f - (float)Math.pow(1.0 - progress, 3);
                this.hoverAnim = start + (target - start) * eased;
                this.repaint();
                if (progress >= 1.0f) {
                    ((Timer)e.getSource()).stop();
                }
            });
            this.hoverTimer.start();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D)graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int n = this.getWidth();
            int n2 = SIZE;
            // Hover'da hafifce yukari kalkma (elevation) hissi verir.
            double lift = this.hoverAnim * 4.0;
            float f = this.pressed ? 0.95f : 1.0f;
            graphics2D.translate((double)n / 2.0, (double)n2 / 2.0 - lift);
            graphics2D.scale(f, f);
            graphics2D.translate((double)(-n) / 2.0, (double)(-n2) / 2.0);
            boolean bl = MainWindow.this.currentInstance != null && this.inst.name.equals(MainWindow.this.currentInstance.name);
            // Golge, hover animasyonuyla birlikte genisleyip koyulasir.
            int shadowAlpha = (int)(30 + this.hoverAnim * 35);
            int shadowSpread = (int)(2 + this.hoverAnim * 3);
            graphics2D.setColor(new Color(0, 0, 0, shadowAlpha));
            graphics2D.fillRoundRect(2, 4 + shadowSpread, n - 2, n2 - 1, 16, 16);
            Color color = MainWindow.brighten(this.c1, 0.12f * this.hoverAnim);
            Color color2 = MainWindow.brighten(this.c2, 0.12f * this.hoverAnim);
            graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, color, 0.0f, n2, color2));
            graphics2D.fillRoundRect(0, 0, n, n2, 16, 16);
            int n3 = (int)(50 + this.hoverAnim * 20);
            graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, new Color(255, 255, 255, n3), 0.0f, (float)n2 * 0.45f, new Color(255, 255, 255, 0)));
            graphics2D.fillRoundRect(0, 0, n, (int)((float)n2 * 0.5f), 16, 16);
            if (bl) {
                graphics2D.setColor(new Color(255, 255, 255, 180));
                graphics2D.setStroke(new BasicStroke(2.0f));
                graphics2D.drawRoundRect(1, 1, n - 2, n2 - 2, 16, 16);
            }
            if (this.hoverAnim > 0.01f && !bl) {
                graphics2D.setColor(new Color(255, 255, 255, (int)(40 * this.hoverAnim)));
                graphics2D.setStroke(new BasicStroke(1.0f));
                graphics2D.drawRoundRect(1, 1, n - 2, n2 - 2, 16, 16);
            }
            int n4 = 42;
            int n5 = (n - n4) / 2;
            int n6 = 18;
            String iconName = this.inst.iconName;
            boolean hasCustomIcon = "custom".equalsIgnoreCase(iconName) && new File(this.inst.dir(), "instance_icon.png").exists();
            boolean hasPresetIcon = iconName != null && !iconName.isBlank() && !"default".equalsIgnoreCase(iconName) && InstanceIcons.isKnown(iconName);
            if (hasCustomIcon || hasPresetIcon) {
                // Hazir/ozel ikon secilmisse onu cizilebilir bir gorsel
                // olarak render edip yuvarlak koseli bir cerceve icine koy.
                File customFile = hasCustomIcon ? new File(this.inst.dir(), "instance_icon.png") : null;
                ImageIcon iconImg = InstanceIcons.render(iconName, n4, customFile);
                java.awt.Shape oldClip = graphics2D.getClip();
                graphics2D.setClip(new java.awt.geom.RoundRectangle2D.Float(n5, n6, n4, n4, 10, 10));
                graphics2D.drawImage(iconImg.getImage(), n5, n6, n4, n4, null);
                graphics2D.setClip(oldClip);
            } else {
                graphics2D.setColor(new Color(255, 255, 255, 30));
                graphics2D.fillOval(n5, n6, n4, n4);
                String string = this.inst.name.isEmpty() ? "?" : this.inst.name.substring(0, 1).toUpperCase();
                graphics2D.setColor(Color.WHITE);
                graphics2D.setFont(new Font("SansSerif", 1, 22));
                FontMetrics fontMetrics = graphics2D.getFontMetrics();
                graphics2D.drawString(string, n5 + (n4 - fontMetrics.stringWidth(string)) / 2, n6 + (n4 + fontMetrics.getAscent()) / 2 - 3);
            }
            String string2 = MainWindow.truncate(this.inst.name, 14);
            graphics2D.setFont(new Font("SansSerif", 1, 11));
            FontMetrics fontMetrics2 = graphics2D.getFontMetrics();
            graphics2D.drawString(string2, (n - fontMetrics2.stringWidth(string2)) / 2, n6 + n4 + 16);
            if (bl) {
                graphics2D.setColor(new Color(34, 197, 94));
                graphics2D.fillOval(n - 14, 6, 10, 10);
                graphics2D.setColor(new Color(34, 197, 94, 80));
                graphics2D.setStroke(new BasicStroke(2.0f));
                graphics2D.drawOval(n - 16, 4, 14, 14);
            }
            graphics2D.dispose();
        }
    }

    private class NewInstanceCard
    extends JPanel {
        private boolean hover;
        private boolean pressed;
        private static final int SIZE = 110;
        private static final int RADIUS = 16;

        NewInstanceCard() {
            this.setOpaque(false);
            this.setPreferredSize(new Dimension(110, 110));
            this.setCursor(Cursor.getPredefinedCursor(12));
            this.setToolTipText("Yeni \u00f6rnek olu\u015ftur");
            this.addMouseListener(new MouseAdapter(){

                @Override
                public void mouseEntered(MouseEvent mouseEvent) {
                    NewInstanceCard.this.hover = true;
                    NewInstanceCard.this.repaint();
                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {
                    NewInstanceCard.this.hover = false;
                    NewInstanceCard.this.repaint();
                }

                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    NewInstanceCard.this.pressed = true;
                    NewInstanceCard.this.repaint();
                }

                @Override
                public void mouseReleased(MouseEvent mouseEvent) {
                    NewInstanceCard.this.pressed = false;
                    NewInstanceCard.this.repaint();
                }

                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
                        MainWindow.this.createInstance();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D)graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int n = this.getWidth();
            int n2 = this.getHeight();
            float f = this.pressed ? 0.93f : 1.0f;
            graphics2D.translate((double)n / 2.0, (double)n2 / 2.0);
            graphics2D.scale(f, f);
            graphics2D.translate((double)(-n) / 2.0, (double)(-n2) / 2.0);
            graphics2D.setColor(new Color(0, 0, 0, this.hover ? 35 : 20));
            graphics2D.fillRoundRect(2, 4, n - 2, n2 - 1, 16, 16);
            graphics2D.setColor(this.hover ? Theme.BG_ELEVATED : Theme.BG_SURFACE);
            graphics2D.fillRoundRect(0, 0, n, n2, 16, 16);
            graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, new Color(255, 255, 255, this.hover ? 15 : 5), 0.0f, (float)n2 * 0.4f, new Color(255, 255, 255, 0)));
            graphics2D.fillRoundRect(0, 0, n, (int)((float)n2 * 0.45f), 16, 16);
            graphics2D.setColor(this.hover ? Theme.ACCENT_BRIGHT : Theme.BG_BORDER);
            graphics2D.setStroke(new BasicStroke(1.5f, 0, 0, 10.0f, new float[]{6.0f, 5.0f}, 0.0f));
            graphics2D.drawRoundRect(1, 1, n - 2, n2 - 2, 16, 16);
            graphics2D.setColor(this.hover ? Theme.ACCENT_BRIGHT : Theme.TEXT_MUTED);
            graphics2D.setFont(new Font("SansSerif", 0, 32));
            FontMetrics fontMetrics = graphics2D.getFontMetrics();
            graphics2D.drawString("+", (n - fontMetrics.stringWidth("+")) / 2, n2 / 2 - 6);
            graphics2D.setFont(new Font("SansSerif", 1, 11));
            FontMetrics fontMetrics2 = graphics2D.getFontMetrics();
            graphics2D.drawString(L10n.isEnglish() ? "New" : "Yeni", (n - fontMetrics2.stringWidth(L10n.isEnglish() ? "New" : "Yeni")) / 2, n2 / 2 + 20);
            graphics2D.dispose();
        }
    }

    static class GcReport {
        int gcCount;
        int fullGcCount;
        int majorCount;
        int longPauseCount;
        long maxPauseMs;
        long totalPauseMs;
        double totalPauseSec;
        int heapUsagePercent;
        int ramGB;
        long sessionSec;
        boolean oom;
        boolean gcOverhead;

        GcReport() {
        }

        boolean hasIssue() {
            return this.oom || this.gcOverhead || this.fullGcCount > 0 || this.longPauseCount > 3 || this.maxPauseMs > 1000L || this.gcCount > 0 && (double)this.totalPauseMs / (double)(this.sessionSec * 1000L) > 0.05;
        }
    }
}

