/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.ui;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.ui.Theme;
import com.lubv.launcher.ui.UiFx;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class MyServersPanel
extends JPanel {
    private static final String SAVE_FILE = "my_servers.json";
    private final PlayCallback playCallback;
    private final Runnable onChangeName;
    private final Runnable onSecondClient;
    private final Supplier<String[]> versionSupplier;
    private final ExecutorService pool = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "my-servers");
        thread.setDaemon(true);
        return thread;
    });
    private final List<ServerEntry> servers = new ArrayList<ServerEntry>();
    private JPanel listPanel;
    private static final String[] VERSIONS = new String[]{"1.21.5", "1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21", "1.20.6", "1.20.4", "1.20.2", "1.20.1", "1.20", "1.19.4", "1.19.2", "1.19", "1.18.2", "1.18", "1.17.1", "1.16.5", "1.15.2", "1.14.4", "1.12.2"};
    private static final String[] LOADERS = new String[]{"Vanilla", "Fabric", "Forge", "NeoForge", "OptiFine"};
    // V33.2: "Modlar" dugmesi icin callback. Sunucunun kendi instance'ina
    // mod kurma ekranini (Mods sekmesi) acar. Null olabilir (eski cagri
    // yerleri).
    private final java.util.function.Consumer<String> onManageMods;

    public MyServersPanel(PlayCallback playCallback) {
        this(playCallback, null, null, null);
    }

    public MyServersPanel(PlayCallback playCallback, Runnable runnable, Runnable runnable2) {
        this(playCallback, runnable, runnable2, null);
    }

    public MyServersPanel(PlayCallback playCallback, Runnable runnable2, Runnable runnable3, Supplier<String[]> supplier) {
        this(playCallback, runnable2, runnable3, supplier, null);
    }

    public MyServersPanel(PlayCallback playCallback, Runnable runnable2, Runnable runnable3, Supplier<String[]> supplier, java.util.function.Consumer<String> onManageMods) {
        this.playCallback = playCallback;
        this.onChangeName = runnable2;
        this.onSecondClient = runnable3;
        this.versionSupplier = supplier;
        this.onManageMods = onManageMods;
        this.setLayout(new BorderLayout(0, 16));
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(24, 24, 24, 24));
        this.loadServers();
        this.buildUI();
    }

    private File saveFile() {
        return new File(Paths.GAME_DIR, SAVE_FILE);
    }

    private void loadServers() {
        try {
            File file = this.saveFile();
            if (!file.exists()) {
                return;
            }
            String string = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            JsonArray jsonArray = JsonParser.parseString(string).getAsJsonArray();
            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                ServerEntry entry = new ServerEntry(jsonObject.has("name") ? jsonObject.get("name").getAsString() : "Sunucu", jsonObject.has("ip") ? jsonObject.get("ip").getAsString() : "", jsonObject.has("version") ? jsonObject.get("version").getAsString() : "1.20.1", jsonObject.has("loader") ? jsonObject.get("loader").getAsString() : "Vanilla", jsonObject.has("password") ? jsonObject.get("password").getAsString() : "");
                if (jsonObject.has("icon") && !jsonObject.get("icon").isJsonNull()) {
                    entry.iconBase64 = jsonObject.get("icon").getAsString();
                }
                this.servers.add(entry);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void saveServers() {
        try {
            JsonArray jsonArray = new JsonArray();
            for (ServerEntry serverEntry : this.servers) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", serverEntry.name);
                jsonObject.addProperty("ip", serverEntry.ip);
                jsonObject.addProperty("version", serverEntry.version);
                jsonObject.addProperty("loader", serverEntry.loader);
                jsonObject.addProperty("password", serverEntry.password);
                if (serverEntry.iconBase64 != null) {
                    jsonObject.addProperty("icon", serverEntry.iconBase64);
                }
                jsonArray.add(jsonObject);
            }
            Files.writeString(this.saveFile().toPath(), (CharSequence)new GsonBuilder().setPrettyPrinting().create().toJson(jsonArray), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void buildUI() {
        JButton jButton;
        JPanel jPanel = new JPanel(new BorderLayout(12, 0));
        jPanel.setOpaque(false);
        JLabel jLabel = new JLabel("Sunucu Listem");
        jLabel.setFont(new Font("SansSerif", 1, 22));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        jPanel.add((Component)jLabel, "West");
        JPanel jPanel2 = new JPanel(new FlowLayout(2, 8, 0));
        jPanel2.setOpaque(false);
        if (this.onChangeName != null) {
            jButton = UiFx.ghostButton(com.lubv.launcher.core.L10n.isEnglish() ? "\u270f Rename" : "\u270f \u0130sim De\u011fi\u015ftir");
            jButton.setToolTipText("Oyuncu ismini de\u011fi\u015ftir (F8 k\u0131sayolu oyun i\u00e7inde de \u00e7al\u0131\u015f\u0131r)");
            jButton.addActionListener(actionEvent -> this.onChangeName.run());
            jPanel2.add(jButton);
        }
        if (this.onSecondClient != null) {
            jButton = UiFx.accentButton("2. Client");
            jButton.setToolTipText("Farkl\u0131 isimle ikinci Minecraft penceresi a\u00e7 (F9 k\u0131sayolu)");
            jButton.addActionListener(actionEvent -> this.onSecondClient.run());
            jPanel2.add(jButton);
        }
        jButton = UiFx.accentButton(com.lubv.launcher.core.L10n.isEnglish() ? "+ Add Server" : "+ Sunucu Ekle");
        jButton.addActionListener(actionEvent -> this.showAddDialog());
        jPanel2.add(jButton);
        jPanel.add((Component)jPanel2, "East");
        this.add((Component)jPanel, "North");
        this.listPanel = new JPanel();
        this.listPanel.setLayout(new BoxLayout(this.listPanel, 1));
        this.listPanel.setOpaque(false);
        JScrollPane jScrollPane = UiFx.cleanScroll(this.listPanel);
        this.add((Component)jScrollPane, "Center");
        this.refreshList();
    }

    private void refreshList() {
        this.listPanel.removeAll();
        if (this.servers.isEmpty()) {
            JLabel jLabel = new JLabel("Henuz sunucu eklenmedi. \"+ Sunucu Ekle\" butonuna basin.");
            jLabel.setForeground(Theme.TEXT_MUTED);
            jLabel.setAlignmentX(0.0f);
            jLabel.setBorder(new EmptyBorder(20, 8, 0, 0));
            this.listPanel.add(jLabel);
        } else {
            for (int i = 0; i < this.servers.size(); ++i) {
                this.listPanel.add(this.buildServerCard(i, this.servers.get(i)));
                this.listPanel.add(Box.createVerticalStrut(8));
            }
        }
        this.listPanel.revalidate();
        this.listPanel.repaint();
    }

    private JPanel buildServerCard(int n, ServerEntry serverEntry) {
        JPanel jPanel = new JPanel(new BorderLayout(12, 0));
        jPanel.setOpaque(true);
        jPanel.setBackground(Theme.BG_SURFACE);
        jPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, Theme.ACCENT), new EmptyBorder(12, 14, 12, 14)));
        jPanel.setAlignmentX(0.0f);
        jPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        // --- Sunucu kapak fotografi (favicon) ---
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(48, 48));
        iconLabel.setHorizontalAlignment(0);
        this.applyServerIcon(iconLabel, serverEntry);
        jPanel.add((Component)iconLabel, "West");
        if (serverEntry.iconBase64 == null) {
            // Ikon henuz cekilmemis - arka planda ping atip favicon'u getir,
            // gelince hem etiketi hem kayitli veriyi guncelle.
            this.fetchServerIcon(n, serverEntry, iconLabel);
        }
        JPanel jPanel2 = new JPanel(new GridLayout(3, 1, 0, 2));
        jPanel2.setOpaque(false);
        JLabel jLabel = new JLabel(serverEntry.name);
        jLabel.setFont(jLabel.getFont().deriveFont(1, 14.0f));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        JLabel jLabel2 = new JLabel(serverEntry.ip);
        jLabel2.setFont(jLabel2.getFont().deriveFont(0, 12.0f));
        jLabel2.setForeground(Theme.TEXT_SECONDARY);
        JLabel jLabel3 = new JLabel(serverEntry.version + " / " + serverEntry.loader);
        jLabel3.setFont(jLabel3.getFont().deriveFont(0, 11.0f));
        jLabel3.setForeground(Theme.TEXT_MUTED);
        jPanel2.add(jLabel);
        jPanel2.add(jLabel2);
        jPanel2.add(jLabel3);
        jPanel.add((Component)jPanel2, "Center");
        JPanel jPanel3 = new JPanel(new FlowLayout(2, 6, 0));
        jPanel3.setOpaque(false);
        JLabel jLabel4 = new JLabel("...");
        jLabel4.setFont(jLabel4.getFont().deriveFont(0, 11.0f));
        jLabel4.setForeground(Theme.TEXT_MUTED);
        jLabel4.setPreferredSize(new Dimension(60, 24));
        jLabel4.setHorizontalAlignment(0);
        this.doPing(serverEntry.ip, jLabel4);
        JButton jButton = UiFx.accentButton("Oyna");
        jButton.setPreferredSize(new Dimension(70, 28));
        jButton.addActionListener(actionEvent -> {
            if (this.playCallback != null) {
                if (serverEntry.password != null && !serverEntry.password.isBlank()) {
                    String string = "/login " + serverEntry.password;
                    try {
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(string), null);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    JOptionPane.showMessageDialog(this, "/login komutu clipboard'a kopyaland\u0131!\nSunucuya ba\u011flan\u0131nca Ctrl+V yap\u0131n.", "Giri\u015f", 1);
                }
                this.playCallback.play(serverEntry.ip, serverEntry.version, serverEntry.loader, serverEntry.password);
            }
        });
        JButton jButton2 = UiFx.ghostButton(com.lubv.launcher.core.L10n.isEnglish() ? "Edit" : "Duzenle");
        jButton2.setPreferredSize(new Dimension(70, 28));
        jButton2.addActionListener(actionEvent -> this.showEditDialog(n, serverEntry));
        // V33.2: Sunucuya ozel mod yonetimi. Loader Vanilla degilse (veya
        // kullanici Vanilla'yai secmek istiyorsa) Mods sekmesi o sunucunun
        // Server-<ip> instance'ina yonlendirilir.
        JButton jButtonMods = UiFx.ghostButton(com.lubv.launcher.core.L10n.isEnglish() ? "Mods" : "Modlar");
        jButtonMods.setPreferredSize(new Dimension(70, 28));
        jButtonMods.setToolTipText(com.lubv.launcher.core.L10n.isEnglish()
            ? "Manage mods/shaders/resource packs for THIS server"
            : "Bu sunucuya ozel mod/shader/kaynak paketi yonetimi");
        jButtonMods.addActionListener(actionEvent -> {
            if (this.onManageMods != null) {
                String host = serverEntry.ip.contains(":") ? serverEntry.ip.split(":")[0] : serverEntry.ip;
                this.onManageMods.accept(host);
            }
        });
        JButton jButton3 = UiFx.dangerButton(com.lubv.launcher.core.L10n.isEnglish() ? "Delete" : "Sil");
        jButton3.setPreferredSize(new Dimension(50, 28));
        jButton3.addActionListener(actionEvent -> {
            int n2 = JOptionPane.showConfirmDialog(this, "\"" + serverEntry.name + "\" silinsin mi?", "Sunucu Sil", 0);
            if (n2 == 0) {
                this.servers.remove(n);
                this.saveServers();
                this.refreshList();
            }
        });
        jPanel3.add(jLabel4);
        jPanel3.add(jButton);
        jPanel3.add(jButtonMods);
        jPanel3.add(jButton2);
        jPanel3.add(jButton3);
        jPanel.add((Component)jPanel3, "East");
        return jPanel;
    }

    /** iconLabel'a serverEntry.iconBase64'teki gorseli (varsa) uygular, yoksa jenerik bir sunucu ikonu gosterir. */
    private void applyServerIcon(JLabel iconLabel, ServerEntry serverEntry) {
        if (serverEntry.iconBase64 != null && !serverEntry.iconBase64.isBlank()) {
            try {
                byte[] bytes = java.util.Base64.getDecoder().decode(serverEntry.iconBase64);
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bytes));
                if (img != null) {
                    java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(48, 48, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g = scaled.createGraphics();
                    g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(img, 0, 0, 48, 48, null);
                    g.dispose();
                    iconLabel.setIcon(new javax.swing.ImageIcon(scaled));
                    iconLabel.setText(null);
                    return;
                }
            }
            catch (Exception ignored) {
                // asagida jenerik ikona dusulur
            }
        }
        iconLabel.setIcon(null);
        iconLabel.setText("\ud83d\udda5");
        iconLabel.setFont(iconLabel.getFont().deriveFont(22f));
    }

    /**
     * Sunucuya Minecraft "server list ping" protokolu ile baglanip
     * favicon'unu ceker. Basarili olursa hem ServerEntry'yi (ve diski)
     * hem de goruntudeki etiketi gunceller. Onceden bu ozellik hic
     * yoktu - sunucu eklendiginde/karti gosterildiginde herhangi bir
     * kapak fotografi cekilmiyordu.
     */
    private void fetchServerIcon(int index, ServerEntry serverEntry, JLabel iconLabel) {
        this.pool.submit(() -> {
            try {
                String host = serverEntry.ip.contains(":") ? serverEntry.ip.split(":")[0] : serverEntry.ip;
                int port = serverEntry.ip.contains(":") ? Integer.parseInt(serverEntry.ip.split(":")[1]) : 25565;
                com.lubv.launcher.core.ServerPing.PingResult result = com.lubv.launcher.core.ServerPing.ping(host, port, 3000);
                if (result != null && result.faviconBase64 != null) {
                    SwingUtilities.invokeLater(() -> {
                        // Kullanici bu sirada sunucuyu silmis olabilir - index'in
                        // hala gecerli ve ayni sunucuya ait oldugunu dogrula.
                        if (index >= 0 && index < MyServersPanel.this.servers.size() && MyServersPanel.this.servers.get(index) == serverEntry) {
                            serverEntry.iconBase64 = result.faviconBase64;
                            MyServersPanel.this.saveServers();
                            MyServersPanel.this.applyServerIcon(iconLabel, serverEntry);
                        }
                    });
                }
            }
            catch (Exception ignored) {
                // sunucu kapali/favicon sunmuyor olabilir - sessizce gec
            }
        });
    }

    private void doPing(String string, JLabel jLabel) {
        this.pool.submit(() -> {
            try {
                String string2 = string.contains(":") ? string.split(":")[0] : string;
                int n = string.contains(":") ? Integer.parseInt(string.split(":")[1]) : 25565;
                long l = System.currentTimeMillis();
                try (Socket socket = new Socket();){
                    socket.connect(new InetSocketAddress(string2, n), 2000);
                }
                long l2 = System.currentTimeMillis() - l;
                String string3 = l2 < 80L ? "#4CAF50" : (l2 < 150L ? "#FFC107" : "#F44336");
                SwingUtilities.invokeLater(() -> jLabel.setText("<html><font color='" + string3 + "'>" + l2 + "ms</font></html>"));
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> jLabel.setText("<html><font color='#666'>offline</font></html>"));
            }
        });
    }

    private void showAddDialog() {
        this.showServerDialog("Sunucu Ekle", "", "", VERSIONS[0], "Vanilla", "", (string, string2, string3, string4, string5) -> {
            if (string2.isBlank()) {
                this.showErr("IP bos olamaz!");
                return;
            }
            this.servers.add(new ServerEntry(string.isBlank() ? string2 : string, string2, string3, string4, string5));
            this.saveServers();
            this.refreshList();
        });
    }

    private void showEditDialog(int n, ServerEntry serverEntry) {
        this.showServerDialog("Sunucu Duzenle", serverEntry.name, serverEntry.ip, serverEntry.version, serverEntry.loader, serverEntry.password, (string, string2, string3, string4, string5) -> {
            if (string2.isBlank()) {
                this.showErr("IP bos olamaz!");
                return;
            }
            serverEntry.name = string.isBlank() ? string2 : string;
            serverEntry.ip = string2;
            serverEntry.version = string3;
            serverEntry.loader = string4;
            serverEntry.password = string5;
            this.saveServers();
            this.refreshList();
        });
    }

    private void showServerDialog(String string, String string2, String string3, String string4, String string5, String string6, ServerDialogCallback serverDialogCallback) {
        JDialog jDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), string, true);
        jDialog.setLayout(new BorderLayout(12, 12));
        jDialog.getRootPane().setBorder(new EmptyBorder(16, 16, 16, 16));
        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.setOpaque(false);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)new JLabel("Sunucu Adi:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        JTextField jTextField = new JTextField(string2, 22);
        jPanel.add((Component)jTextField, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)new JLabel("IP Adresi (host:port):"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        JTextField jTextField2 = new JTextField(string3, 22);
        jTextField2.setToolTipText("Ornek: play.hypixel.net veya 192.168.1.5:25565");
        jPanel.add((Component)jTextField2, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)new JLabel("Minecraft Surumu:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        String[] stringArray = this.versionSupplier != null ? this.versionSupplier.get() : VERSIONS;
        JComboBox<String> jComboBox = new JComboBox<String>(stringArray);
        jComboBox.setLightWeightPopupEnabled(true);
        jComboBox.setEditable(true);
        jComboBox.setSelectedItem(string4);
        jPanel.add(jComboBox, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)new JLabel("Mod Loader:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        JToggleButton[] jToggleButtonArray = new JToggleButton[LOADERS.length];
        ButtonGroup buttonGroup = new ButtonGroup();
        JPanel jPanel2 = new JPanel(new GridLayout(1, LOADERS.length, 4, 0));
        jPanel2.setOpaque(false);
        for (int i = 0; i < LOADERS.length; ++i) {
            jToggleButtonArray[i] = new JToggleButton(LOADERS[i]);
            jToggleButtonArray[i].setFocusPainted(false);
            if (LOADERS[i].equals(string5)) {
                jToggleButtonArray[i].setSelected(true);
            }
            buttonGroup.add(jToggleButtonArray[i]);
            jPanel2.add(jToggleButtonArray[i]);
        }
        jPanel.add((Component)jPanel2, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)new JLabel("Sunucu \u015eifresi:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        JPasswordField jPasswordField = new JPasswordField(string6, 22);
        jPasswordField.setToolTipText("Opsiyonel: Sunucu \u015fifresi (/login i\u00e7in)");
        jPanel.add((Component)jPasswordField, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        JLabel jLabel = new JLabel("<html><font color='#888'>Sunucu surumu ile launcher surumu eslesmeli.<br>Port belirtilmezse 25565 kullanilir.<br>\u015eifre girilirse, sunucuya ba\u011flan\u0131rken /login komutu otomatik kopyalan\u0131r.</font></html>");
        jLabel.setFont(jLabel.getFont().deriveFont(0, 11.0f));
        jPanel.add((Component)jLabel, gridBagConstraints);
        jDialog.add((Component)jPanel, "Center");
        JPanel jPanel3 = new JPanel(new FlowLayout(2, 8, 0));
        jPanel3.setOpaque(false);
        JButton jButton = new JButton("Tamam");
        JButton jButton2 = new JButton("Iptal");
        jButton.addActionListener(actionEvent -> {
            String selectedLoader = "Vanilla";
            for (int i = 0; i < LOADERS.length; ++i) {
                if (!jToggleButtonArray[i].isSelected()) continue;
                selectedLoader = LOADERS[i];
                break;
            }
            serverDialogCallback.onConfirm(jTextField.getText().trim(), jTextField2.getText().trim(), (String)jComboBox.getSelectedItem(), selectedLoader, new String(jPasswordField.getPassword()));
            jDialog.dispose();
        });
        jButton2.addActionListener(actionEvent -> jDialog.dispose());
        jPanel3.add(jButton);
        jPanel3.add(jButton2);
        jDialog.add((Component)jPanel3, "South");
        jDialog.getRootPane().setDefaultButton(jButton);
        jDialog.pack();
        jDialog.setMinimumSize(new Dimension(380, jDialog.getHeight()));
        jDialog.setLocationRelativeTo(this);
        jDialog.setVisible(true);
    }

    private void showErr(String string) {
        JOptionPane.showMessageDialog(this, string, "Hata", 0);
    }

    public static interface PlayCallback {
        public void play(String var1, String var2, String var3, String var4);
    }

    private static class ServerEntry {
        String name;
        String ip;
        String version;
        String loader;
        String password;
        // Sunucudan server-list-ping ile cekilen favicon (64x64 base64
        // PNG, "data:image/png;base64," on-eki olmadan). Null ise henuz
        // cekilmemis ya da sunucu favicon sunmuyor demektir.
        String iconBase64;

        ServerEntry(String string, String string2, String string3, String string4, String string5) {
            this.name = string;
            this.ip = string2;
            this.version = string3;
            this.loader = string4 != null ? string4 : "Vanilla";
            this.password = string5 != null ? string5 : "";
        }
    }

    @FunctionalInterface
    static interface ServerDialogCallback {
        public void onConfirm(String var1, String var2, String var3, String var4, String var5);
    }
}

