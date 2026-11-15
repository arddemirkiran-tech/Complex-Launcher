/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.core.RconClient;
import com.lubv.launcher.game.OsRules;
import com.lubv.launcher.mods.CurseForgeApi;
import com.lubv.launcher.mods.ModrinthApi;
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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class ServersPanel
extends JPanel {
    private static final String SERVERS_DIR = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "servers";
    private static final String META_FILE = "servers_meta.json";
    private static final String[] LOADERS = new String[]{"Vanilla", "Paper", "Fabric", "Forge", "NeoForge", "OptiFine"};
    private static final String[] MC_VERSIONS = new String[]{"26.2", "26.1", "1.21.11", "1.21.10", "1.21.9", "1.21.8", "1.21.7", "1.21.6", "1.21.5", "1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21", "1.20.6", "1.20.5", "1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20", "1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.19", "1.18.2", "1.18.1", "1.18", "1.17.1", "1.17", "1.16.5", "1.16.4", "1.16.3", "1.16.2", "1.16.1", "1.16", "1.15.2", "1.15.1", "1.15", "1.14.4", "1.14.3", "1.14.2", "1.14.1", "1.14", "1.13.2", "1.13.1", "1.13", "1.12.2", "1.12.1", "1.12"};
    private static final String[] DIFFICULTIES = new String[]{"peaceful", "easy", "normal", "hard"};
    private static final String[] GAMEMODES = new String[]{"survival", "creative", "adventure", "spectator"};
    private static final String[] LEVEL_TYPES = new String[]{"default", "flat", "largeBiomes", "amplified"};
    private static final String CURSEFORGE_API_KEY = "$2a$10$9d8G2Q5rS.xB6MdD3X0NlefGcjZlt8eLfL6osBAQcsct3HfLglskq";
    private JPanel serverListPanel;
    private JPanel configPanel;
    private JLabel statusLabel;
    private JProgressBar createProgress;
    private JTextArea createLog;
    private String selectedServer = null;
    private boolean advancedMode = false;
    private final Map<String, JsonObject> serversMeta = new LinkedHashMap<String, JsonObject>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public ServersPanel() {
        this.setLayout(new BorderLayout(16, 16));
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(24, 24, 24, 24));
        this.loadMeta();
        this.buildUI();
        this.refreshServerList();
    }

    private void buildUI() {
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setOpaque(false);
        JLabel jLabel = new JLabel("Sunucular");
        jLabel.setFont(new Font("SansSerif", 1, 22));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        jPanel.add((Component)jLabel, "West");
        JButton jButton = UiFx.ghostButton("Yenile");
        jButton.addActionListener(actionEvent -> this.refreshServerList());
        jPanel.add((Component)jButton, "East");
        this.add((Component)jPanel, "North");
        JPanel jPanel2 = new JPanel(new BorderLayout(0, 12));
        jPanel2.setOpaque(false);
        this.serverListPanel = new JPanel();
        this.serverListPanel.setLayout(new BoxLayout(this.serverListPanel, 1));
        this.serverListPanel.setOpaque(false);
        JScrollPane jScrollPane = UiFx.cleanScroll(this.serverListPanel);
        jScrollPane.setPreferredSize(new Dimension(280, 0));
        jPanel2.add((Component)jScrollPane, "Center");
        JPanel jPanel3 = new JPanel(new GridLayout(1, 2, 6, 0));
        jPanel3.setOpaque(false);
        JButton jButton2 = UiFx.accentButton("+ Olustur");
        jButton2.addActionListener(actionEvent -> this.showCreateDialog());
        JButton jButton3 = UiFx.ghostButton("Yardim");
        jButton3.addActionListener(actionEvent -> this.showHelpDialog());
        jPanel3.add(jButton2);
        jPanel3.add(jButton3);
        jPanel2.add((Component)jPanel3, "South");
        this.add((Component)jPanel2, "West");
        JPanel jPanel4 = new JPanel(new BorderLayout(0, 8));
        jPanel4.setOpaque(false);
        JPanel jPanel5 = new JPanel(new FlowLayout(2));
        jPanel5.setOpaque(false);
        JToggleButton jToggleButton = new JToggleButton("Gelismis Mod");
        jToggleButton.setFont(new Font("SansSerif", 1, 11));
        jToggleButton.setBackground(Theme.ACCENT);
        jToggleButton.setForeground(Color.WHITE);
        jToggleButton.setFocusPainted(false);
        jToggleButton.addActionListener(actionEvent -> {
            this.advancedMode = jToggleButton.isSelected();
            jToggleButton.setText(this.advancedMode ? "Gelismis Mod: ACIK" : "Gelismis Mod");
            if (this.selectedServer != null) {
                this.showServerConfig(this.selectedServer);
            }
        });
        jPanel5.add(jToggleButton);
        jPanel4.add((Component)jPanel5, "North");
        this.configPanel = new JPanel();
        this.configPanel.setLayout(new BoxLayout(this.configPanel, 1));
        this.configPanel.setOpaque(false);
        JScrollPane jScrollPane2 = UiFx.cleanScroll(this.configPanel);
        jPanel4.add((Component)jScrollPane2, "Center");
        JPanel jPanel6 = new JPanel(new BorderLayout(0, 6));
        jPanel6.setOpaque(false);
        this.statusLabel = new JLabel("Bir sunucu secin");
        this.statusLabel.setFont(new Font("SansSerif", 0, 13));
        this.statusLabel.setForeground(Theme.TEXT_MUTED);
        jPanel6.add((Component)this.statusLabel, "North");
        this.createProgress = UiFx.progressPill();
        this.createProgress.setVisible(false);
        jPanel6.add((Component)this.createProgress, "South");
        this.createLog = new JTextArea(5, 50);
        this.createLog.setEditable(false);
        this.createLog.setBackground(Theme.BG_BASE);
        this.createLog.setForeground(Theme.TEXT_SECONDARY);
        this.createLog.setFont(new Font("Consolas", 0, 11));
        this.createLog.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane jScrollPane3 = UiFx.cleanScroll(this.createLog);
        jScrollPane3.setPreferredSize(new Dimension(0, 120));
        jPanel6.add((Component)jScrollPane3, "Center");
        jPanel4.add((Component)jPanel6, "South");
        this.add((Component)jPanel4, "Center");
    }

    private void refreshServerList() {
        this.serverListPanel.removeAll();
        this.loadMeta();
        if (this.serversMeta.isEmpty()) {
            JLabel jLabel = new JLabel("Henuz sunucu yok");
            jLabel.setForeground(Theme.TEXT_MUTED);
            jLabel.setAlignmentX(0.0f);
            this.serverListPanel.add(jLabel);
        } else {
            for (Map.Entry<String, JsonObject> entry : this.serversMeta.entrySet()) {
                this.serverListPanel.add(this.createServerCard(entry.getKey(), entry.getValue()));
                this.serverListPanel.add(Box.createVerticalStrut(6));
            }
        }
        this.serverListPanel.revalidate();
        this.serverListPanel.repaint();
    }

    private JPanel createServerCard(final String string, JsonObject jsonObject) {
        final boolean bl = string.equals(this.selectedServer);
        final boolean[] blArray = new boolean[]{false};
        final JPanel jPanel = new JPanel(new BorderLayout(8, 4)){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int n = this.getWidth();
                int n2 = this.getHeight();
                Color color = bl ? UiFx.lerp(Theme.BG_ELEVATED, Theme.ACCENT_MUTED, 0.3f) : (blArray[0] ? Theme.BG_ELEVATED : Theme.BG_SURFACE);
                graphics2D.setColor(color);
                graphics2D.fillRoundRect(0, 0, n, n2, 12, 12);
                if (bl) {
                    graphics2D.setColor(Theme.ACCENT);
                    graphics2D.fillRoundRect(0, 2, 3, n2 - 4, 3, 3);
                }
                if (blArray[0] && !bl) {
                    graphics2D.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 40));
                    graphics2D.setStroke(new BasicStroke(1.0f));
                    graphics2D.drawRoundRect(0, 0, n - 1, n2 - 1, 12, 12);
                }
                graphics2D.dispose();
                super.paintComponent(graphics);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        jPanel.setBorder(new EmptyBorder(8, 12, 8, 10));
        jPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        jPanel.setAlignmentX(0.0f);
        jPanel.setCursor(Cursor.getPredefinedCursor(12));
        jPanel.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                blArray[0] = true;
                jPanel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                blArray[0] = false;
                jPanel.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                ServersPanel.this.selectServer(string);
            }
        });
        JPanel jPanel2 = new JPanel(new BorderLayout(8, 0));
        jPanel2.setOpaque(false);
        JPanel jPanel3 = new JPanel(){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setColor(bl ? Theme.ACCENT : Theme.BG_BORDER);
                graphics2D.fillOval(0, 0, 32, 32);
                graphics2D.setColor(bl ? Color.WHITE : Theme.TEXT_MUTED);
                graphics2D.setFont(new Font("SansSerif", 1, 14));
                FontMetrics fontMetrics = graphics2D.getFontMetrics();
                String string2 = string.isEmpty() ? "?" : string.substring(0, 1).toUpperCase();
                graphics2D.drawString(string2, (32 - fontMetrics.stringWidth(string2)) / 2, (32 + fontMetrics.getAscent()) / 2 - 2);
                graphics2D.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        jPanel3.setPreferredSize(new Dimension(32, 32));
        jPanel2.add((Component)jPanel3, "West");
        JPanel jPanel4 = new JPanel(new GridLayout(2, 1, 0, 1));
        jPanel4.setOpaque(false);
        JLabel jLabel = new JLabel(string);
        jLabel.setFont(new Font("SansSerif", 1, 13));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        String string2 = jsonObject.has("version") ? jsonObject.get("version").getAsString() : "?";
        String string3 = jsonObject.has("loader") ? jsonObject.get("loader").getAsString() : "?";
        JLabel jLabel2 = new JLabel(string3 + " \u00b7 " + string2);
        jLabel2.setFont(new Font("SansSerif", 0, 10));
        jLabel2.setForeground(Theme.TEXT_MUTED);
        jPanel4.add(jLabel);
        jPanel4.add(jLabel2);
        jPanel2.add((Component)jPanel4, "Center");
        jPanel.add((Component)jPanel2, "Center");
        JPanel jPanel5 = new JPanel(new FlowLayout(2, 4, 0));
        jPanel5.setOpaque(false);
        JButton jButton = UiFx.accentButton("Baslat");
        jButton.setFont(new Font("SansSerif", 1, 10));
        jButton.setPreferredSize(new Dimension(70, 28));
        jButton.addActionListener(actionEvent -> this.startServer(string));
        JButton jButton2 = UiFx.ghostButton("...");
        jButton2.setToolTipText("Klasoru goruntule");
        jButton2.setPreferredSize(new Dimension(32, 28));
        jButton2.addActionListener(actionEvent -> {
            try {
                Object object = jsonObject.has("dir") ? jsonObject.get("dir").getAsString() : SERVERS_DIR + File.separator + string;
                Desktop.getDesktop().open(new File((String)object));
            }
            catch (Exception exception) {
                this.statusLabel.setText("Klasor acilamadi: " + exception.getMessage());
            }
        });
        JButton jButton3 = UiFx.dangerButton(com.lubv.launcher.core.L10n.isEnglish() ? "Delete" : "Sil");
        jButton3.setFont(new Font("SansSerif", 0, 10));
        jButton3.setPreferredSize(new Dimension(50, 28));
        jButton3.addActionListener(actionEvent -> this.deleteServer(string));
        JLabel jLabel3 = new JLabel("...");
        jLabel3.setFont(new Font("SansSerif", 0, 10));
        jLabel3.setForeground(Theme.TEXT_MUTED);
        jLabel3.setPreferredSize(new Dimension(55, 28));
        jLabel3.setHorizontalAlignment(0);
        String string4 = jsonObject.has("ip") ? jsonObject.get("ip").getAsString() : "127.0.0.1";
        String string5 = jsonObject.has("port") ? jsonObject.get("port").getAsString() : "25565";
        int n = 25565;
        try {
            n = Integer.parseInt(string5.trim());
        }
        catch (Exception exception) {
            // empty catch block
        }
        String string6 = string4.contains(":") ? string4.split(":")[0] : string4;
        int n2 = string4.contains(":") ? Integer.parseInt(string4.split(":")[1]) : n;
        JLabel jLabel4 = jLabel3;
        this.executor.submit(() -> {
            try {
                long l = System.currentTimeMillis();
                try (Socket socket = new Socket();){
                    socket.connect(new InetSocketAddress(string6, n2), 2000);
                }
                long l2 = System.currentTimeMillis() - l;
                String pingColor = l2 < 80L ? "#4CAF50" : (l2 < 150L ? "#FFC107" : "#F44336");
                SwingUtilities.invokeLater(() -> jLabel4.setText("<html><font color='" + pingColor + "'>" + l2 + "ms</font></html>"));
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> jLabel4.setText("<html><font color='#666'>offline</font></html>"));
            }
        });
        jPanel5.add(jLabel3);
        jPanel5.add(jButton);
        jPanel5.add(jButton2);
        jPanel5.add(jButton3);
        jPanel.add((Component)jPanel5, "East");
        return jPanel;
    }

    private void selectServer(String string) {
        this.selectedServer = string;
        this.refreshServerList();
        this.showServerConfig(string);
    }

    private void showServerConfig(String string) {
        this.configPanel.removeAll();
        JsonObject jsonObject = this.serversMeta.get(string);
        if (jsonObject == null) {
            this.configPanel.revalidate();
            return;
        }
        Object object = jsonObject.has("dir") ? jsonObject.get("dir").getAsString() : SERVERS_DIR + File.separator + string;
        Map<String, String> map = this.readProps((String)object);
        this.configPanel.add(this.sec("Genel Ayarlar"));
        this.configPanel.add(this.txt("MOTD:", map, "motd", string));
        this.configPanel.add(this.txt("Port:", map, "server-port", "25565"));
        this.configPanel.add(this.txt("Max Oyuncu:", map, "max-players", "20"));
        this.configPanel.add(this.txt("Seed:", map, "level-seed", ""));
        this.configPanel.add(this.combo("Level Type:", map, "level-type", LEVEL_TYPES, "default"));
        this.configPanel.add(this.txt("Level Name:", map, "level-name", string));
        this.configPanel.add(this.txt("Server IP:", map, "server-ip", ""));
        this.configPanel.add(Box.createVerticalStrut(8));
        this.configPanel.add(this.sec("Oyun Ayarlari"));
        this.configPanel.add(this.combo("Gamemode:", map, "gamemode", GAMEMODES, "survival"));
        this.configPanel.add(this.combo("Difficulty:", map, "difficulty", DIFFICULTIES, "normal"));
        this.configPanel.add(this.chk("Hardcore:", map, "hardcore", false));
        this.configPanel.add(this.chk("PVP:", map, "pvp", true));
        this.configPanel.add(this.chk("Allow Command Block:", map, "command-block", false));
        this.configPanel.add(this.chk("Allow Nether:", map, "allow-nether", true));
        this.configPanel.add(this.chk("Generate Structures:", map, "generate-structures", true));
        this.configPanel.add(this.chk("Spawn Monsters:", map, "spawn-monsters", true));
        this.configPanel.add(this.chk("Spawn Animals:", map, "spawn-animals", true));
        this.configPanel.add(this.txt("Max Tick Time:", map, "max-tick-time", "60000"));
        this.configPanel.add(Box.createVerticalStrut(8));
        this.configPanel.add(this.sec("World Ayarlari"));
        this.configPanel.add(this.slider("View Distance:", map, "view-distance", 2, 32, 10));
        this.configPanel.add(this.slider("Simulation Distance:", map, "simulation-distance", 2, 32, 10));
        this.configPanel.add(this.slider("Spawn Protection:", map, "spawn-protection", 0, 100, 0));
        this.configPanel.add(Box.createVerticalStrut(8));
        this.configPanel.add(this.sec("Guvenlik & Ag"));
        this.configPanel.add(this.chk("Online Mode (Cracked Kapat):", map, "online-mode", true));
        this.configPanel.add(this.chk("White List:", map, "white-list", false));
        this.configPanel.add(this.txt("Rate Limit:", map, "rate-limit", "0"));
        this.configPanel.add(this.txt("Player Idle Timeout:", map, "player-idle-timeout", "0"));
        this.configPanel.add(Box.createVerticalStrut(8));
        this.configPanel.add(this.sec("RCON"));
        this.configPanel.add(this.chk("RCON Aktif:", map, "enable-rcon", true));
        this.configPanel.add(this.txt("RCON Port:", map, "rcon.port", "25575"));
        this.configPanel.add(this.txt("RCON Password:", map, "rcon.password", ""));
        this.configPanel.add(Box.createVerticalStrut(8));
        if (this.advancedMode) {
            this.configPanel.add(this.sec("GELISMIS AYARLAR"));
            this.configPanel.add(this.txt("Op Permission Level:", map, "op-permission-level", "4"));
            this.configPanel.add(this.txt("Sync Chunk Writes:", map, "sync-chunk-writes", "true"));
            this.configPanel.add(this.txt("Entity Broadcast Range:", map, "entity-broadcast-range-percentage", "100"));
            this.configPanel.add(this.txt("Arrow Despawn Rate:", map, "arrow-despawn-rate", "300"));
            this.configPanel.add(this.txt("Allow Flight:", map, "allow-flight", "false"));
            this.configPanel.add(this.txt("Resource Pack URL:", map, "resource-pack", ""));
            this.configPanel.add(this.txt("Resource Pack SHA1:", map, "resource-pack-sha1", ""));
            this.configPanel.add(Box.createVerticalStrut(8));
        }
        this.configPanel.add(this.sec("Oyuncu Yonetimi"));
        JPanel jPanel = new JPanel(new FlowLayout(0, 6, 4));
        jPanel.setOpaque(false);
        jPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        jPanel.setAlignmentX(0.0f);
        JTextField jTextField = new JTextField(15);
        JButton jButton = UiFx.accentButton("OP Ver");
        jButton.addActionListener(actionEvent -> {
            String opName = jTextField.getText().trim();
            if (!opName.isEmpty()) {
                this.statusLabel.setText(opName + " -> OP verildi");
            }
        });
        jPanel.add(this.lbl("OP Ver:"));
        jPanel.add(jTextField);
        jPanel.add(jButton);
        this.configPanel.add(jPanel);
        JPanel jPanel2 = new JPanel(new FlowLayout(0, 6, 4));
        jPanel2.setOpaque(false);
        jPanel2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        jPanel2.setAlignmentX(0.0f);
        JTextField jTextField2 = new JTextField(15);
        JButton jButton2 = UiFx.dangerButton("Banla");
        jButton2.addActionListener(actionEvent -> {
            String banName = jTextField2.getText().trim();
            if (!banName.isEmpty()) {
                this.statusLabel.setText(banName + " -> Banlandi");
            }
        });
        jPanel2.add(this.lbl("Banla:"));
        jPanel2.add(jTextField2);
        jPanel2.add(jButton2);
        this.configPanel.add(jPanel2);
        JPanel jPanel3 = new JPanel(new FlowLayout(0, 6, 4));
        jPanel3.setOpaque(false);
        jPanel3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        jPanel3.setAlignmentX(0.0f);
        JTextField jTextField3 = new JTextField(8);
        JComboBox<String> jComboBox = new JComboBox<String>(GAMEMODES);
        JButton jButton3 = UiFx.ghostButton("Gamemode Degistir");
        jButton3.addActionListener(actionEvent -> {
            String playerName = jTextField3.getText().trim();
            String gameMode = (String)jComboBox.getSelectedItem();
            if (!playerName.isEmpty()) {
                this.statusLabel.setText(playerName + " -> " + gameMode);
            }
        });
        jPanel3.add(this.lbl("Oyuncu:"));
        jPanel3.add(jTextField3);
        jPanel3.add(jComboBox);
        jPanel3.add(jButton3);
        this.configPanel.add(jPanel3);
        this.configPanel.add(Box.createVerticalStrut(8));
        this.configPanel.add(this.sec("Mod / Plugin Yukle"));
        JPanel jPanel4 = new JPanel(new FlowLayout(0, 6, 2));
        jPanel4.setOpaque(false);
        jPanel4.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        jPanel4.setAlignmentX(0.0f);
        JComboBox<String> jComboBox2 = new JComboBox<String>(new String[]{"Modrinth", "CurseForge"});
        JComboBox<String> jComboBox3 = new JComboBox<String>(new String[]{"Mod", "Plugin"});
        jPanel4.add(this.lbl("Kaynak:"));
        jPanel4.add(jComboBox2);
        jPanel4.add(this.lbl("Tur:"));
        jPanel4.add(jComboBox3);
        this.configPanel.add(jPanel4);
        JPanel jPanel5 = new JPanel(new BorderLayout(6, 0));
        jPanel5.setOpaque(false);
        jPanel5.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        jPanel5.setAlignmentX(0.0f);
        JTextField jTextField4 = new JTextField(20);
        jTextField4.setFont(new Font("Consolas", 0, 12));
        JButton jButton4 = UiFx.accentButton(com.lubv.launcher.core.L10n.isEnglish() ? "Search" : "Ara");
        jPanel5.add((Component)jTextField4, "Center");
        jPanel5.add((Component)jButton4, "East");
        this.configPanel.add(jPanel5);
        DefaultListModel defaultListModel = new DefaultListModel();
        JList jList = new JList(defaultListModel);
        jList.setFont(new Font("SansSerif", 0, 11));
        jList.setBackground(Theme.BG_BASE);
        jList.setSelectionMode(0);
        JScrollPane jScrollPane = UiFx.cleanScroll(jList);
        jScrollPane.setPreferredSize(new Dimension(0, 120));
        this.configPanel.add(jScrollPane);
        JPanel jPanel6 = new JPanel(new FlowLayout(0, 6, 2));
        jPanel6.setOpaque(false);
        jPanel6.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        jPanel6.setAlignmentX(0.0f);
        JButton jButton5 = UiFx.accentButton(com.lubv.launcher.core.L10n.isEnglish() ? "Install Selected" : "Seciliyi Kur");
        jButton5.setEnabled(false);
        jPanel6.add(jButton5);
        this.configPanel.add(jPanel6);
        String[] stringArray = new String[]{jsonObject.has("loader") ? jsonObject.get("loader").getAsString() : "Vanilla"};
        String serverVersion = jsonObject.has("version") ? jsonObject.get("version").getAsString() : "";
        List list = Collections.synchronizedList(new ArrayList());
        jButton4.addActionListener(actionEvent -> {
            String searchQuery = jTextField4.getText().trim();
            if (searchQuery.isEmpty()) {
                return;
            }
            boolean bl = "Plugin".equals(jComboBox3.getSelectedItem());
            boolean bl2 = "CurseForge".equals(jComboBox2.getSelectedItem());
            defaultListModel.clear();
            list.clear();
            jButton5.setEnabled(false);
            this.statusLabel.setText("Araniyor: " + searchQuery + "...");
            this.executor.submit(() -> {
                try {
                    if (bl2) {
                        if (bl) {
                            for (CurseForgeApi.ModResult modResult : CurseForgeApi.searchPlugins(CURSEFORGE_API_KEY, searchQuery, serverVersion)) {
                                list.add(new String[]{String.valueOf(modResult.id), modResult.name, "curseforge", null, null, "true"});
                            }
                        } else {
                            for (CurseForgeApi.ModResult modResult : CurseForgeApi.searchMods(CURSEFORGE_API_KEY, searchQuery, stringArray[0], serverVersion)) {
                                list.add(new String[]{String.valueOf(modResult.id), modResult.name, "curseforge", null, null, "false"});
                            }
                        }
                    } else if (bl) {
                        for (ModrinthApi.ModResult modResult : ModrinthApi.searchPlugins(searchQuery, serverVersion)) {
                            list.add(new String[]{modResult.id, modResult.title, "modrinth", null, null, "true"});
                        }
                    } else {
                        for (ModrinthApi.ModResult modResult : ModrinthApi.search(searchQuery, stringArray[0] != null ? stringArray[0].toLowerCase() : "", serverVersion)) {
                            list.add(new String[]{modResult.id, modResult.title, "modrinth", null, null, "false"});
                        }
                    }
                    ArrayList<String[]> found = new ArrayList<String[]>(list);
                    SwingUtilities.invokeLater(() -> {
                        for (String[] result : found) {
                            defaultListModel.addElement(result[1]);
                        }
                        if (!found.isEmpty()) {
                            jButton5.setEnabled(true);
                        }
                        this.statusLabel.setText(found.size() + " sonuc bulundu");
                    });
                }
                catch (Exception exception) {
                    SwingUtilities.invokeLater(() -> this.statusLabel.setText("Arama hatasi: " + exception.getMessage()));
                }
            });
        });
        jTextField4.addActionListener(actionEvent -> jButton4.doClick());
        jButton5.addActionListener(arg_0 -> this.lambda$showServerConfig$27(jList, list, (String)object, jButton5, serverVersion, stringArray, arg_0));
        this.configPanel.add(this.sec("RCON Konsol"));
        JPanel jPanel7 = new JPanel(new BorderLayout(6, 0));
        jPanel7.setOpaque(false);
        jPanel7.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        jPanel7.setAlignmentX(0.0f);
        JTextField jTextField5 = new JTextField(20);
        jTextField5.setFont(new Font("Consolas", 0, 12));
        jTextField5.putClientProperty("JTextField.placeholderText", "Komut girin (stop, time set day, ...)");
        JButton jButton6 = UiFx.accentButton("Gonder");
        jPanel7.add((Component)jTextField5, "Center");
        jPanel7.add((Component)jButton6, "East");
        this.configPanel.add(jPanel7);
        Runnable runnable = () -> {
            String rconCmd = jTextField5.getText().trim();
            if (rconCmd.isEmpty()) {
                return;
            }
            String rconPort = map.getOrDefault("rcon.port", "25575");
            String rconPass = map.getOrDefault("rcon.password", "");
            if (rconPass.isEmpty()) {
                this.statusLabel.setText("RCON sifresi bos!");
                return;
            }
            this.executor.submit(() -> {
                try (RconClient rconClient = new RconClient("127.0.0.1", Integer.parseInt(rconPort));){
                    rconClient.authenticate(rconPass);
                    String cmdReply = rconClient.sendCommand(rconCmd);
                    SwingUtilities.invokeLater(() -> {
                        this.statusLabel.setText("[RCON] " + (cmdReply.isEmpty() ? "Komut gonderildi." : cmdReply));
                        this.log("[RCON] " + rconCmd + " -> " + (cmdReply.isEmpty() ? "OK" : cmdReply));
                        jTextField5.setText("");
                    });
                }
                catch (Exception exception) {
                    SwingUtilities.invokeLater(() -> this.statusLabel.setText("RCON hatasi: " + exception.getMessage()));
                }
            });
        };
        jButton6.addActionListener(actionEvent -> runnable.run());
        jTextField5.addActionListener(actionEvent -> runnable.run());
        this.configPanel.add(Box.createVerticalStrut(8));
        this.configPanel.add(Box.createVerticalStrut(8));
        JPanel jPanel8 = new JPanel(new FlowLayout(0, 8, 0));
        jPanel8.setOpaque(false);
        jPanel8.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        jPanel8.setAlignmentX(0.0f);
        JButton jButton7 = UiFx.accentButton("Kaydet");
        jButton7.addActionListener(arg_0 -> this.lambda$showServerConfig$34((String)object, map, string, arg_0));
        JButton jButton8 = UiFx.ghostButton("Klasoru Goster");
        jButton8.addActionListener(arg_0 -> this.lambda$showServerConfig$35((String)object, arg_0));
        JButton jButton9 = UiFx.ghostButton("Yeniden Baslat");
        jButton9.addActionListener(actionEvent -> this.startServer(string));
        jPanel8.add(jButton7);
        jPanel8.add(jButton8);
        jPanel8.add(jButton9);
        this.configPanel.add(jPanel8);
        this.configPanel.revalidate();
        this.configPanel.repaint();
    }

    private JLabel sec(String string) {
        JLabel jLabel = new JLabel("  " + string);
        jLabel.setFont(new Font("SansSerif", 1, 13));
        jLabel.setForeground(Theme.ACCENT);
        jLabel.setAlignmentX(0.0f);
        return jLabel;
    }

    private JLabel lbl(String string) {
        JLabel jLabel = new JLabel(string);
        jLabel.setFont(new Font("SansSerif", 0, 12));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        return jLabel;
    }

    private JPanel txt(String string, final Map<String, String> map, final String string2, String string3) {
        JPanel jPanel = new JPanel(new BorderLayout(8, 0));
        jPanel.setOpaque(false);
        jPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jPanel.setAlignmentX(0.0f);
        JLabel jLabel = new JLabel(string);
        jLabel.setFont(new Font("SansSerif", 0, 12));
        jLabel.setPreferredSize(new Dimension(180, 24));
        jPanel.add((Component)jLabel, "West");
        final JTextField jTextField = new JTextField(map.getOrDefault(string2, string3), 18);
        jTextField.setFont(new Font("Consolas", 0, 12));
        jTextField.addFocusListener(new FocusAdapter(){

            @Override
            public void focusLost(FocusEvent focusEvent) {
                map.put(string2, jTextField.getText());
            }
        });
        jPanel.add((Component)jTextField, "Center");
        return jPanel;
    }

    private JPanel combo(String string, Map<String, String> map, String string2, String[] stringArray, String string3) {
        JPanel jPanel = new JPanel(new BorderLayout(8, 0));
        jPanel.setOpaque(false);
        jPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jPanel.setAlignmentX(0.0f);
        JLabel jLabel = new JLabel(string);
        jLabel.setFont(new Font("SansSerif", 0, 12));
        jLabel.setPreferredSize(new Dimension(180, 24));
        jPanel.add((Component)jLabel, "West");
        JComboBox<String> jComboBox = new JComboBox<String>(stringArray);
        jComboBox.setSelectedItem(map.getOrDefault(string2, string3));
        jComboBox.setFont(new Font("SansSerif", 0, 12));
        jComboBox.addActionListener(actionEvent -> {
            if (jComboBox.getSelectedItem() != null) {
                map.put(string2, (String)jComboBox.getSelectedItem());
            }
        });
        jPanel.add(jComboBox, "Center");
        return jPanel;
    }

    private JPanel chk(String string, Map<String, String> map, String string2, boolean bl) {
        JPanel jPanel = new JPanel(new BorderLayout(8, 0));
        jPanel.setOpaque(false);
        jPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jPanel.setAlignmentX(0.0f);
        JLabel jLabel = new JLabel(string);
        jLabel.setFont(new Font("SansSerif", 0, 12));
        jLabel.setPreferredSize(new Dimension(220, 24));
        jPanel.add((Component)jLabel, "West");
        JCheckBox jCheckBox = new JCheckBox();
        jCheckBox.setSelected(Boolean.parseBoolean(map.getOrDefault(string2, String.valueOf(bl))));
        jCheckBox.addActionListener(actionEvent -> map.put(string2, String.valueOf(jCheckBox.isSelected())));
        jPanel.add((Component)jCheckBox, "Center");
        return jPanel;
    }

    private JPanel slider(String string, Map<String, String> map, String string2, int n, int n2, int n3) {
        JPanel jPanel = new JPanel(new BorderLayout(8, 0));
        jPanel.setOpaque(false);
        jPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        jPanel.setAlignmentX(0.0f);
        JLabel jLabel = new JLabel(string);
        jLabel.setFont(new Font("SansSerif", 0, 12));
        jLabel.setPreferredSize(new Dimension(180, 24));
        jPanel.add((Component)jLabel, "West");
        int n4 = n3;
        try {
            n4 = Integer.parseInt(map.getOrDefault(string2, String.valueOf(n3)));
        }
        catch (Exception exception) {
            // empty catch block
        }
        JSlider jSlider = new JSlider(n, n2, n4);
        jSlider.setMajorTickSpacing(5);
        jSlider.setMinorTickSpacing(1);
        jSlider.setPaintTicks(true);
        jSlider.setOpaque(false);
        JLabel jLabel2 = new JLabel(String.valueOf(n4));
        jLabel2.setFont(new Font("Consolas", 1, 12));
        jLabel2.setPreferredSize(new Dimension(30, 24));
        jSlider.addChangeListener(changeEvent -> {
            int sliderVal = jSlider.getValue();
            jLabel2.setText(String.valueOf(sliderVal));
            map.put(string2, String.valueOf(sliderVal));
        });
        JPanel jPanel2 = new JPanel(new BorderLayout(4, 0));
        jPanel2.setOpaque(false);
        jPanel2.add((Component)jSlider, "Center");
        jPanel2.add((Component)jLabel2, "East");
        jPanel.add((Component)jPanel2, "Center");
        return jPanel;
    }

    private void showHelpDialog() {
        JDialog jDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Sunucu Rehberi", false);
        jDialog.setSize(720, 640);
        jDialog.setLocationRelativeTo(this);
        JTextArea jTextArea = new JTextArea();
        jTextArea.setEditable(false);
        jTextArea.setMargin(new Insets(16, 20, 16, 20));
        jTextArea.setFont(new Font("SansSerif", 0, 13));
        jTextArea.setForeground(Theme.TEXT_PRIMARY);
        jTextArea.setBackground(Theme.BG_BASE);
        jTextArea.setLineWrap(true);
        jTextArea.setWrapStyleWord(true);
        jTextArea.setText(this.getHelpText());
        JScrollPane jScrollPane = UiFx.cleanScroll(jTextArea);
        jDialog.setContentPane(jScrollPane);
        jDialog.setVisible(true);
    }

    private String getHelpText() {
        return "=== MINECRAFT SUNUCU REHBERI ===\n\n1. SUNUCU OLUSTURMA\n   - '+ Olustur' butonuna tiklayin\n   - Sunucu adi girin (sadece harf, rakam, tire)\n   - Loader secin: Vanilla / Paper / Fabric / Forge / NeoForge\n   - MC surumunu secin\n   - RAM miktarini ayarlayin (en az 1024 MB onerilir)\n   - Gamemode, Difficulty, Seed ayarlarini yapin\n   - Hardcore, Cracked, PVP secelenekleri mevcut\n   - 'Olustur' ile islem baslar\n\n2. LOADER LAR HAKKINDA\nVanilla  - Orijinal Minecraft, mod yok\nPaper    - Hizli ve plugin destekli (Bukkit/Spigot tabanli)\nFabric   - Hizli, guncel mod yukleyici (mod destekli)\nForge    - Klasik mod yukleyici (buyuk mod kitlesi)\nNeoForge - Forge'in yeni nesil fork'u\n\n3. ILK BASLATMA (ONEMLI)\n   - Forge/NeoForge: Ilk baslatmada installer otomatik calisir\nlibraries/ klasoru olusur. Sabirla bekleyin.\n   - Vanilla/Paper/Fabric: EULA ve temel dosyalar olusur.\n   - run.bat (Windows) veya run.sh (Linux) ile sunucuyu baslatin.\n\n4. SUNUCU AYARLARI (server.properties)\nSecili sunucuyu tikladiginizda sag panelden ayarlari gorebilirsiniz:\n   - MOTD: Sunucu adi (oyuncu listesinde gorunen yazi)\n   - Port: Varsayilan 25565. Baska sunucu varsa farkli yapin.\n   - Max Oyuncu: Sunucuya girebilecek maksimum oyuncu sayisi\n   - Gamemode: survival/creative/adventure/spectator\n   - Difficulty: peaceful/easy/normal/hard\n   - Hardcore: Olumden sonra spectator. Ciddi oyun icin.\n   - PVP: Oyuncular birbirini vurabilir mi?\n   - Online Mode: true=Premium (Microsoft hesap), false=Cracked\n   - View Distance: Gorunme mesafesi (2-32, 10 ideal)\n   - RCON: uzaktan komut calistirma (panel icin gerekli)\nAyarlari degistirdikten sonra 'Kaydet' butonuna basin.\n\n5. MOD / PLUGIN YUKLEME\nSag panelden 'Mod / Plugin Yukle' bolumunu kullanin:\n   - Kaynak: Modrinth (ucretsiz, API key gerekmez) veya CurseForge\n   - Tur: Mod (Fabric/Forge icin) veya Plugin (Paper icin)\n   - Arama yapin, secin, 'Seciliyi Kur' ile indirin\n   - Modlar mods/ klasorune, Pluginler plugins/ klasorune iner\n   - Sunucuyu baslatmadan once yukleyin!\n\n6. DOSYA KONUMLARI\nSunucu klasorundeki onemli dosyalar:\n   - server.properties  : Sunucu ayarlari\n   - eula.txt           : EULA kabulu (otomatik olusur)\n   - mods/              : Mod jar dosyalari (Fabric/Forge/NeoForge)\n   - plugins/           : Plugin jar dosyalari (Paper/Spigot)\n   - config/            : Mod/plugin konfigurasyonlari\n   - world/             : Dunya dosyalari\n   - run.bat            : Windows baslatma scripti\n   - run.sh             : Linux baslatma scripti\n   - libraries/         : Forge/NeoForge kutuphaneleri (otomatik)\n\n7. SUNUCUYU BASLATMA / DURDURMA\n   - 'Baslat' butonu: run.bat/run.sh calistirir\n   - VBS ile baslatma: arka planda konsol acilmadan baslatir\n   - Sunucuyu durdurmak icin konsol penceresini kapat\u0131n\n   - Veya RCON ile 'stop' komutu gonderin\n\n8. SORUN GIDERME\n   - Sunucu acilmiyorsa: run.bat/run.sh'yi acarak hata mesajini gorun\n   - RAM yetersiz: 'Ay Karti' bolumunden artirin\n   - Port hatasi: Baska program portu kullaniyor olabilir\n   - Java hatasi: Dogru surum gerekli (Fabric=Java 8+, Forge=Java 8+, 1.20.5+=Java 17+)\n   - Mod uyumsuzlugu: Mod ve loader surumlerinin eslestiginden emin olun\n   - Cracked giris: Online Mode false olmali\n\n9. RCON (Uzaktan Komut)\nPanel, sunucuyla RCON uzerinden iletisim kurar.\nVarsayilan port: 25575\nAyarlardan RCON sifresini degistirebilirsiniz.\nRCON kapaliyse sunucu durumunu goremzsiniz.\n\n10. IPUCLARI\n   - Paper en hizli sunucu secenegidir (1.13+)\n   - Fabric en guncel surumlerde en hizli mod yukleyicidir\n   - Forge buyuk mod kitlesine sahiptir ama daha yavas olabilir\n   - Sunucu dosyalarini duzenlemeden once yedekleyin\n   - Bellek: 4GB RAM onerilir (modlu sunucu icin 6-8GB)\n   - ZeroTier/IP ile arkadaslarinizi sunucunuza davet edebilirsiniz\n";
    }

    private void showCreateDialog() {
        JDialog jDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Yeni Sunucu Olustur", true);
        jDialog.setSize(460, 520);
        jDialog.setLocationRelativeTo(this);
        jDialog.setResizable(false);
        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.setBackground(Theme.BG_BASE);
        jPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(4, 6, 4, 6);
        gridBagConstraints.anchor = 17;
        gridBagConstraints.fill = 2;
        int n = 0;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = n++;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label("Sunucu Adi:"), gridBagConstraints);
        JTextField jTextField = new JTextField(20);
        jTextField.setFont(new Font("Consolas", 0, 12));
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        jPanel.add((Component)jTextField, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = n++;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label("Loader:"), gridBagConstraints);
        JComboBox<String> jComboBox = new JComboBox<String>(LOADERS);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        jPanel.add(jComboBox, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = n++;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label("Surum:"), gridBagConstraints);
        JComboBox<String> jComboBox2 = new JComboBox<String>(MC_VERSIONS);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        jPanel.add(jComboBox2, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = n++;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label("RAM (MB):"), gridBagConstraints);
        JSpinner jSpinner = new JSpinner(new SpinnerNumberModel(4096, 512, 32768, 512));
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        jPanel.add((Component)jSpinner, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = n++;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label("Gamemode:"), gridBagConstraints);
        JComboBox<String> jComboBox3 = new JComboBox<String>(GAMEMODES);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        jPanel.add(jComboBox3, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = n++;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label("Difficulty:"), gridBagConstraints);
        JComboBox<String> jComboBox4 = new JComboBox<String>(DIFFICULTIES);
        jComboBox4.setSelectedItem("normal");
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        jPanel.add(jComboBox4, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = n++;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)UiFx.label("Seed:"), gridBagConstraints);
        JTextField jTextField2 = new JTextField(20);
        jTextField2.setFont(new Font("Consolas", 0, 12));
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        jPanel.add((Component)jTextField2, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = n++;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 0.0;
        JLabel jLabel = new JLabel("Ayarlar");
        jLabel.setFont(new Font("SansSerif", 1, 11));
        jLabel.setForeground(Theme.ACCENT_BRIGHT);
        jPanel.add((Component)jLabel, gridBagConstraints);
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = n++;
        gridBagConstraints.weightx = 0.0;
        JCheckBox jCheckBox = new JCheckBox("Hardcore");
        jCheckBox.setForeground(Theme.TEXT_PRIMARY);
        jPanel.add((Component)jCheckBox, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = n++;
        gridBagConstraints.weightx = 0.0;
        JCheckBox jCheckBox2 = new JCheckBox("Cracked (Online Mode Kapali)");
        jCheckBox2.setSelected(true);
        jCheckBox2.setForeground(Theme.TEXT_PRIMARY);
        jPanel.add((Component)jCheckBox2, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = n++;
        gridBagConstraints.weightx = 0.0;
        JCheckBox jCheckBox3 = new JCheckBox("PVP");
        jCheckBox3.setSelected(true);
        jCheckBox3.setForeground(Theme.TEXT_PRIMARY);
        jPanel.add((Component)jCheckBox3, gridBagConstraints);
        JPanel jPanel2 = new JPanel(new FlowLayout(2, 8, 0));
        jPanel2.setOpaque(false);
        jPanel2.setBorder(new EmptyBorder(8, 0, 0, 0));
        JButton jButton = UiFx.ghostButton("Iptal");
        JButton jButton2 = UiFx.accentButton("Olustur");
        jButton.addActionListener(actionEvent -> jDialog.dispose());
        jButton2.addActionListener(actionEvent -> {
            String string = jTextField.getText().trim();
            if (string.isEmpty() || !string.matches("[a-zA-Z0-9_\\-]+")) {
                JOptionPane.showMessageDialog(jDialog, "Gecersiz isim!");
                return;
            }
            if (this.serversMeta.containsKey(string)) {
                JOptionPane.showMessageDialog(jDialog, "Bu isimde sunucu var!");
                return;
            }
            jDialog.dispose();
            LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
            linkedHashMap.put("level-seed", jTextField2.getText().trim());
            linkedHashMap.put("pvp", String.valueOf(jCheckBox3.isSelected()));
            linkedHashMap.put("allow-nether", "true");
            linkedHashMap.put("command-block", "false");
            linkedHashMap.put("spawn-monsters", "true");
            linkedHashMap.put("spawn-animals", "true");
            linkedHashMap.put("level-type", "default");
            this.createServer(string, (String)jComboBox2.getSelectedItem(), (String)jComboBox.getSelectedItem(), (Integer)jSpinner.getValue(), (String)jComboBox3.getSelectedItem(), (String)jComboBox4.getSelectedItem(), jCheckBox.isSelected(), jCheckBox2.isSelected(), linkedHashMap);
        });
        jPanel2.add(jButton);
        jPanel2.add(jButton2);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = n;
        gridBagConstraints.gridwidth = 2;
        jPanel.add((Component)jPanel2, gridBagConstraints);
        jDialog.setContentPane(jPanel);
        jDialog.setVisible(true);
    }

    private void createServer(String string, String string2, String string3, int n, String string4, String string5, boolean bl, boolean bl2, Map<String, String> map) {
        this.selectedServer = string;
        this.statusLabel.setText("Sunucu olusturuluyor: " + string);
        this.createProgress.setValue(0);
        this.createProgress.setVisible(true);
        this.createLog.setText("");
        this.executor.submit(() -> {
            try {
                String string6 = SERVERS_DIR + File.separator + string;
                new File(string6).mkdirs();
                new File(string6, "mods").mkdirs();
                new File(string6, "plugins").mkdirs();
                new File(string6, "config").mkdirs();
                new File(string6, "world").mkdirs();
                this.log("Klasor: " + string6);
                this.progress(5);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", string);
                jsonObject.addProperty("version", string2);
                jsonObject.addProperty("loader", string3);
                jsonObject.addProperty("ram_mb", n);
                jsonObject.addProperty("dir", string6);
                jsonObject.addProperty("setup_status", "creating");
                jsonObject.addProperty("jar_filename", "server.jar");
                this.serversMeta.put(string, jsonObject);
                this.saveMeta();
                String string7 = "server.jar";
                String string8 = null;
                String[] stringArray = this.getDl(string3, string2);
                if (stringArray != null) {
                    string7 = stringArray[0];
                    string8 = stringArray[1];
                    this.log(string3 + " " + string2 + " -> " + string7);
                }
                if (string8 == null) {
                    this.log("JAR indirilemedi!");
                    return;
                }
                this.log("JAR indiriliyor...");
                this.progress(10);
                HttpUtil.downloadFile(string8, new File(string6 + File.separator + string7));
                jsonObject.addProperty("jar_filename", string7);
                this.log("JAR indirildi: " + string7);
                this.progress(45);
                this.writeRunBat(string6, string, string2, string3, n, string7);
                this.log("run.bat OK");
                this.progress(55);
                this.writeRunSh(string6, string, string2, string3, n, string7);
                this.log("run.sh OK");
                this.progress(60);
                this.writeVbs(string6);
                this.log("sunucu_baslat.vbs OK");
                this.progress(63);
                Files.writeString(Path.of(string6, "eula.txt"), (CharSequence)"eula=true", StandardCharsets.UTF_8, new OpenOption[0]);
                this.writeProps(string6, string, string4, string5, bl, bl2, map);
                this.log("server.properties OK");
                this.progress(70);
                this.log("Ilk baslatma...");
                this.progress(80);
                boolean bl3 = string3.equalsIgnoreCase("Forge") || string3.equalsIgnoreCase("NeoForge");
                boolean bl4 = OsRules.CURRENT_OS == OsRules.Os.LINUX;
                try {
                    ProcessBuilder processBuilder = bl3 ? new ProcessBuilder("java", "-Djava.awt.headless=true", "-Xmx256M", "-jar", string7, "--installServer") : new ProcessBuilder("java", "-Xmx256M", "-jar", string7, " nogui");
                    processBuilder.directory(new File(string6));
                    processBuilder.redirectErrorStream(true);
                    Process process = processBuilder.start();
                    try (InputStream inputStream = process.getInputStream();
                         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));){
                        String string9;
                        while ((string9 = bufferedReader.readLine()) != null) {
                            this.log("  > " + string9);
                        }
                    }
                    int n2 = process.waitFor();
                    if (n2 == 0) {
                        this.log("Tamam - ilk baslatma tamamlandi (exit: 0)");
                    } else {
                        this.log("Ilk baslatma tamamlandi (exit: " + n2 + ") - run.bat ile baslatin");
                    }
                }
                catch (Exception exception) {
                    this.log("Ilk baslatma atlandi: " + exception.getMessage());
                }
                jsonObject.addProperty("setup_status", "ready");
                this.saveMeta();
                this.progress(100);
                this.log("Sunucu hazir!");
                SwingUtilities.invokeLater(() -> {
                    this.refreshServerList();
                    this.selectServer(string);
                });
            }
            catch (Exception exception) {
                this.log("HATA: " + exception.getMessage());
                this.progress(0);
            }
        });
    }

    private void writeRunBat(String string, String string2, String string3, String string4, int n, String string5) throws IOException {
        boolean bl;
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("@echo off");
        arrayList.add("setlocal enabledelayedexpansion");
        arrayList.add("title " + string2 + " - " + string4 + " " + string3);
        arrayList.add("cd /d \"%~dp0\"");
        arrayList.add("set \"JAVA_CMD=java\"");
        arrayList.add("if not exist \"eula.txt\"echo eula=true > eula.txt");
        boolean bl2 = bl = string4.equalsIgnoreCase("Forge") || string4.equalsIgnoreCase("NeoForge");
        if (bl) {
            arrayList.add("if not exist \"libraries\\\" (for %%f in (*-installer.jar) do (\"!JAVA_CMD!\" -Djava.awt.headless=true -jar \"%%f\" --installServer))");
            arrayList.add("set \"ARGS_FILE=\"");
            arrayList.add("if exist \"libraries\\net\\minecraftforge\\forge\" (for /d %%v in (\"libraries\\net\\minecraftforge\\forge\\*\") do (if exist \"%%v\\win_args.txt\"set \"ARGS_FILE=%%v\\win_args.txt\"))");
            arrayList.add("if \"!ARGS_FILE!\"==\"\"if exist \"libraries\\net\\neoforged\\neoforge\" (for /d %%v in (\"libraries\\net\\neoforged\\neoforge\\*\") do (if exist \"%%v\\win_args.txt\"set \"ARGS_FILE=%%v\\win_args.txt\"))");
            arrayList.add("if not \"!ARGS_FILE!\"==\"\" (\"!JAVA_CMD!\" -Xms" + n / 2 + "M -Xmx" + n + "M @!ARGS_FILE! nogui) else (\"!JAVA_CMD!\" -Xms" + n / 2 + "M -Xmx" + n + "M -jar \"" + string5 + "\" nogui)");
        } else {
            arrayList.add("\"!JAVA_CMD!\" -Xms" + n / 2 + "M -Xmx" + n + "M -jar \"" + string5 + "\" nogui");
        }
        arrayList.add("pause");
        Files.writeString(Path.of(string, "run.bat"), String.join((CharSequence)"\r\n", arrayList), StandardCharsets.UTF_8, new OpenOption[0]);
    }

    private void writeRunSh(String string, String string2, String string3, String string4, int n, String string5) throws IOException {
        boolean bl;
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("#!/usr/bin/env bash");
        arrayList.add("set -e");
        arrayList.add("cd \"$(dirname \"$0\")\"");
        arrayList.add("JAVA_CMD=\"${JAVA_CMD:-java}\"");
        arrayList.add("[ -f eula.txt ] || echo 'eula=true' > eula.txt");
        boolean bl2 = bl = string4.equalsIgnoreCase("Forge") || string4.equalsIgnoreCase("NeoForge");
        if (bl) {
            arrayList.add("if [ ! -d libraries ]; then");
            arrayList.add("for f in *-installer.jar; do \"$JAVA_CMD\" -Djava.awt.headless=true -jar \"$f\" --installServer; done");
            arrayList.add("fi");
            arrayList.add("ARGS_FILE=\"\"");
            arrayList.add("if [ -d libraries/net/minecraftforge/forge ]; then");
            arrayList.add("ARGS_FILE=$(find libraries/net/minecraftforge/forge -maxdepth 2 -name unix_args.txt | head -n1)");
            arrayList.add("fi");
            arrayList.add("if [ -z \"$ARGS_FILE\" ] && [ -d libraries/net/neoforged/neoforge ]; then");
            arrayList.add("ARGS_FILE=$(find libraries/net/neoforged/neoforge -maxdepth 2 -name unix_args.txt | head -n1)");
            arrayList.add("fi");
            arrayList.add("if [ -n \"$ARGS_FILE\" ]; then");
            arrayList.add("  \"$JAVA_CMD\" -Xms" + n / 2 + "M -Xmx" + n + "M @\"$ARGS_FILE\" nogui");
            arrayList.add("else");
            arrayList.add("  \"$JAVA_CMD\" -Xms" + n / 2 + "M -Xmx" + n + "M -jar \"" + string5 + "\" nogui");
            arrayList.add("fi");
        } else {
            arrayList.add("\"$JAVA_CMD\" -Xms" + n / 2 + "M -Xmx" + n + "M -jar \"" + string5 + "\" nogui");
        }
        Path path = Path.of(string, "run.sh");
        Files.writeString(path, String.join((CharSequence)"\n", arrayList) + "\n", StandardCharsets.UTF_8, new OpenOption[0]);
        try {
            path.toFile().setExecutable(true, false);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void writeVbs(String string) throws IOException {
        Files.writeString(Path.of(string, "sunucu_baslat.vbs"), (CharSequence)("Set WshShell = CreateObject(\"WScript.Shell\")\r\nWshShell.CurrentDirectory = \"" + string + "\"\r\nWshShell.Run \"cmd.exe /c run.bat\", 0, False\r\n"), StandardCharsets.UTF_8, new OpenOption[0]);
    }

    private void writeProps(String string, String string2, String string3, String string4, boolean bl, boolean bl2, Map<String, String> map) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("server-port=25565\nlevel-name=").append(string2).append("\nmotd=").append(string2);
        stringBuilder.append("\nmax-players=20\nonline-mode=").append(!bl2);
        stringBuilder.append("\ngamemode=").append(string3).append("\ndifficulty=").append(string4);
        stringBuilder.append("\nhardcore=").append(bl).append("\npvp=").append(map.getOrDefault("pvp", "true"));
        stringBuilder.append("\ncommand-block=").append(map.getOrDefault("command-block", "false"));
        stringBuilder.append("\nallow-nether=").append(map.getOrDefault("allow-nether", "true"));
        stringBuilder.append("\nspawn-monsters=").append(map.getOrDefault("spawn-monsters", "true"));
        stringBuilder.append("\nspawn-animals=").append(map.getOrDefault("spawn-animals", "true"));
        stringBuilder.append("\nspawn-npcs=true\ngenerate-structures=true");
        stringBuilder.append("\nlevel-seed=").append(map.getOrDefault("level-seed", ""));
        stringBuilder.append("\nlevel-type=").append(map.getOrDefault("level-type", "default"));
        stringBuilder.append("\nforce-gamemode=false\nspawn-protection=0");
        stringBuilder.append("\nview-distance=10\nsimulation-distance=10");
        stringBuilder.append("\nmax-world-size=29999984");
        stringBuilder.append("\nwhite-list=false\nenable-rcon=true");
        stringBuilder.append("\nrcon.port=25575\nrcon.password=1823401248712948071298041282471098247109247809");
        stringBuilder.append("\nenable-query=false\nquery.port=25565");
        stringBuilder.append("\nrate-limit=0\nnetwork-compression-threshold=256");
        stringBuilder.append("\nprevent-proxy-connections=false\nenforce-secure-profile=true");
        stringBuilder.append("\nplayer-idle-timeout=0\nmax-tick-time=60000");
        stringBuilder.append("\nallow-flight=false\nenable-status=true");
        stringBuilder.append("\nresource-pack=\nresource-pack-sha1=");
        stringBuilder.append("\nrequire-resource-pack=false");
        stringBuilder.append("\nhide-online-players=false\nuse-native-transport=true");
        stringBuilder.append("\nlog-ips=true\n");
        Files.writeString(Path.of(string, "server.properties"), (CharSequence)stringBuilder.toString(), StandardCharsets.UTF_8, new OpenOption[0]);
    }

    private Map<String, String> readProps(String string) {
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
        File file = new File(string, "server.properties");
        if (!file.exists()) {
            return linkedHashMap;
        }
        try {
            for (String string2 : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                if ((string2 = string2.trim()).startsWith("#") || string2.isEmpty() || !string2.contains("=")) continue;
                int n = string2.indexOf(61);
                linkedHashMap.put(string2.substring(0, n).trim(), string2.substring(n + 1).trim());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return linkedHashMap;
    }

    private void writePropsMap(String string, Map<String, String> map) {
        File file = new File(string, "server.properties");
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        try {
            Files.writeString(file.toPath(), (CharSequence)stringBuilder.toString(), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception exception) {
            this.statusLabel.setText("Kayit hatasi: " + exception.getMessage());
        }
    }

    private String[] getDl(String string, String string2) {
        try {
            if (string.equalsIgnoreCase("Paper")) {
                JsonObject jsonObject;
                JsonArray jsonArray;
                String string3 = HttpUtil.getText("https://api.papermc.io/v3/projects/paper/versions/" + string2 + "/builds");
                if (string3 == null) {
                    return null;
                }
                JsonObject jsonObject2 = JsonParser.parseString(string3).getAsJsonObject();
                JsonArray jsonArray2 = jsonArray = jsonObject2.has("builds") ? jsonObject2.getAsJsonArray("builds") : jsonObject2.getAsJsonArray();
                if (jsonArray == null || jsonArray.size() == 0) {
                    return null;
                }
                JsonObject jsonObject3 = jsonArray.get(jsonArray.size() - 1).getAsJsonObject();
                JsonObject jsonObject4 = jsonObject3.getAsJsonObject("downloads");
                JsonObject downloadInfo = jsonObject4.has("application") ? jsonObject4.getAsJsonObject("application") : (jsonObject4.has("server:default") ? jsonObject4.getAsJsonObject("server:default") : (jsonObject4.has("server:mojang") ? jsonObject4.getAsJsonObject("server:mojang") : null));
                if (downloadInfo == null) {
                    return null;
                }
                String string4 = downloadInfo.get("url").getAsString();
                return new String[]{string4.substring(string4.lastIndexOf(47) + 1).split("\\?")[0], string4};
            }
            if (string.equalsIgnoreCase("Vanilla")) {
                String string5 = HttpUtil.getText("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
                for (JsonElement jsonElement : JsonParser.parseString(string5).getAsJsonObject().getAsJsonArray("versions")) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    if (!string2.equals(jsonObject.get("id").getAsString())) continue;
                    String string6 = HttpUtil.getText(jsonObject.get("url").getAsString());
                    String string7 = JsonParser.parseString(string6).getAsJsonObject().getAsJsonObject("downloads").getAsJsonObject("server").get("url").getAsString();
                    return new String[]{"minecraft_server." + string2 + ".jar", string7};
                }
                return null;
            }
            if (string.equalsIgnoreCase("Fabric")) {
                String string8 = HttpUtil.getText("https://meta.fabricmc.net/v2/versions/installer");
                String string9 = JsonParser.parseString(string8).getAsJsonArray().get(0).getAsJsonObject().get("version").getAsString();
                String string10 = HttpUtil.getText("https://meta.fabricmc.net/v2/versions/loader/" + string2);
                String string11 = JsonParser.parseString(string10).getAsJsonArray().get(0).getAsJsonObject().getAsJsonObject("loader").get("version").getAsString();
                return new String[]{"fabric-server-mc." + string2 + "-loader." + string11 + ".jar", "https://meta.fabricmc.net/v2/versions/loader/" + string2 + "/" + string11 + "/" + string9 + "/server/jar"};
            }
            if (string.equalsIgnoreCase("NeoForge")) {
                String[] stringArray = string2.split("\\.");
                String string12 = Integer.parseInt(stringArray[0]) >= 26 ? stringArray[0] + "." + stringArray[1] + (String)(stringArray.length > 2 ? "." + stringArray[2] : ".0") + "." : Integer.parseInt(stringArray[1]) + "." + (stringArray.length > 2 ? Integer.parseInt(stringArray[2]) : 0) + ".";
                String string13 = HttpUtil.getText("https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml");
                String string14 = null;
                for (String string15 : string13.split("\n")) {
                    String string16;
                    if (!(string15 = string15.trim()).startsWith("<version>") || !string15.endsWith("</version>") || !(string16 = string15.replace("<version>", "").replace("</version>", "")).startsWith(string12)) continue;
                    string14 = string16;
                }
                if (string14 == null) {
                    return null;
                }
                return new String[]{"neoforge-" + string14 + "-installer.jar", "https://maven.neoforged.net/releases/net/neoforged/neoforge/" + string14 + "/neoforge-" + string14 + "-installer.jar"};
            }
            if (string.equalsIgnoreCase("Forge")) {
                String string17 = HttpUtil.getText("https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml");
                String string18 = null;
                for (String string19 : string17.split("\n")) {
                    String string20;
                    if (!(string19 = string19.trim()).startsWith("<version>") || !string19.endsWith("</version>") || !(string20 = string19.replace("<version>", "").replace("</version>", "")).startsWith(string2 + "-")) continue;
                    string18 = string20;
                }
                if (string18 == null) {
                    return null;
                }
                return new String[]{"forge-" + string18 + "-installer.jar", "https://maven.minecraftforge.net/net/minecraftforge/forge/" + string18 + "/forge-" + string18 + "-installer.jar"};
            }
        }
        catch (Exception exception) {
            return null;
        }
        return null;
    }

    private void startServer(String string) {
        boolean bl;
        JsonObject jsonObject = this.serversMeta.get(string);
        if (jsonObject == null) {
            this.statusLabel.setText("Sunucu bulunamadi: " + string);
            return;
        }
        Object object = jsonObject.has("dir") ? jsonObject.get("dir").getAsString() : SERVERS_DIR + File.separator + string;
        File file = new File((String)object);
        if (!file.exists()) {
            this.statusLabel.setText("Sunucu klasoru bulunamadi: " + (String)object);
            return;
        }
        File file2 = new File((String)object, "sunucu_baslat.vbs");
        File file3 = new File((String)object, "run.bat");
        File file4 = new File((String)object, "run.sh");
        boolean bl2 = bl = OsRules.CURRENT_OS == OsRules.Os.LINUX;
        if (bl && !file4.exists()) {
            this.statusLabel.setText("run.sh bulunamadi!");
            return;
        }
        if (!(bl || file2.exists() || file3.exists())) {
            this.statusLabel.setText("run.bat bulunamadi!");
            return;
        }
        this.statusLabel.setText(string + " baslatiliyor...");
        this.executor.submit(() -> {
            try {
                if (bl) {
                    ProcessBuilder processBuilder = new ProcessBuilder("bash", file4.getAbsolutePath());
                    processBuilder.directory(file);
                    processBuilder.redirectErrorStream(true);
                    processBuilder.start();
                    SwingUtilities.invokeLater(() -> this.statusLabel.setText(string + " baslatildi (run.sh)!"));
                } else if (file2.exists()) {
                    ProcessBuilder processBuilder = new ProcessBuilder("wscript.exe", file2.getAbsolutePath());
                    processBuilder.directory(file);
                    processBuilder.start();
                    SwingUtilities.invokeLater(() -> this.statusLabel.setText(string + " baslatildi (VBS)!"));
                } else {
                    ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "run.bat");
                    processBuilder.directory(file);
                    processBuilder.start();
                    SwingUtilities.invokeLater(() -> this.statusLabel.setText(string + " baslatildi (BAT)!"));
                }
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.statusLabel.setText("Baslatma hatasi: " + exception.getMessage()));
            }
        });
    }

    private void deleteServer(String string) {
        if (JOptionPane.showConfirmDialog(this, "'" + string + "' silinsin mi?", "Sil", 0) != 0) {
            return;
        }
        this.serversMeta.remove(string);
        this.saveMeta();
        this.refreshServerList();
        if (string.equals(this.selectedServer)) {
            this.selectedServer = null;
            this.configPanel.removeAll();
            this.configPanel.revalidate();
        }
    }

    private File getMetaFile() {
        return new File(Paths.GAME_DIR, META_FILE);
    }

    private void loadMeta() {
        this.serversMeta.clear();
        File file = this.getMetaFile();
        if (!file.exists()) {
            return;
        }
        try {
            for (Map.Entry<String, JsonElement> entry : JsonParser.parseString(Files.readString(file.toPath(), StandardCharsets.UTF_8)).getAsJsonObject().entrySet()) {
                this.serversMeta.put(entry.getKey(), entry.getValue().getAsJsonObject());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void saveMeta() {
        try {
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<String, JsonObject> entry : this.serversMeta.entrySet()) {
                jsonObject.add(entry.getKey(), entry.getValue());
            }
            Files.writeString(this.getMetaFile().toPath(), (CharSequence)jsonObject.toString(), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void log(String string) {
        SwingUtilities.invokeLater(() -> {
            this.createLog.append(string + "\n");
            this.createLog.setCaretPosition(this.createLog.getDocument().getLength());
        });
    }

    private void progress(int n) {
        SwingUtilities.invokeLater(() -> this.createProgress.setValue(n));
    }

    private /* synthetic */ void lambda$showServerConfig$35(String string, ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().open(new File(string));
        }
        catch (Exception exception) {
            this.statusLabel.setText("Klasor acilamadi: " + exception.getMessage());
        }
    }

    private /* synthetic */ void lambda$showServerConfig$34(String string, Map map, String string2, ActionEvent actionEvent) {
        this.writePropsMap(string, map);
        this.statusLabel.setText(string2 + " ayarlari kaydedildi!");
    }

    private /* synthetic */ void lambda$showServerConfig$27(JList jList, List list, String string, JButton jButton, String serverVer, String[] stringArray, ActionEvent actionEvent) {
        int n = jList.getSelectedIndex();
        if (n < 0 || n >= list.size()) {
            return;
        }
        String[] stringArray2 = (String[])list.get(n);
        String string3 = stringArray2[0];
        String string4 = stringArray2[1];
        String string5 = stringArray2[2];
        boolean bl = "true".equals(stringArray2[5]);
        File file = new File(string, bl ? "plugins" : "mods");
        file.mkdirs();
        this.statusLabel.setText(string4 + " indiriliyor...");
        jButton.setEnabled(false);
        this.executor.submit(() -> {
            try {
                if ("curseforge".equals(string5)) {
                    int cfId = Integer.parseInt(string3);
                    List<CurseForgeApi.FileResult> cfFiles = CurseForgeApi.getFiles(CURSEFORGE_API_KEY, cfId, null, serverVer);
                    if (!cfFiles.isEmpty()) {
                        CurseForgeApi.FileResult fileResult = CurseForgeApi.pickBestFile(cfFiles);
                        if (fileResult != null) {
                            String dlUrl = CurseForgeApi.getDownloadUrl(CURSEFORGE_API_KEY, cfId, fileResult.id);
                            if (dlUrl != null) {
                                HttpUtil.downloadFile(dlUrl, new File(file, fileResult.fileName));
                                SwingUtilities.invokeLater(() -> this.statusLabel.setText(string4 + " kuruldu -> " + (bl ? "plugins" : "mods")));
                            } else {
                                SwingUtilities.invokeLater(() -> this.statusLabel.setText(string4 + " indirme linki alinamadi"));
                            }
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> this.statusLabel.setText(string4 + " icin uyumlu dosya bulunamadi"));
                    }
                } else {
                    List<ModrinthApi.ModVersion> modVersions = ModrinthApi.getVersions(string3, bl ? null : (stringArray[0] != null ? stringArray[0].toLowerCase() : null), serverVer);
                    if (!modVersions.isEmpty()) {
                        ModrinthApi.ModVersion modVersion = ModrinthApi.pickBestVersion(modVersions);
                        if (modVersion != null && modVersion.downloadUrl != null && modVersion.fileName != null) {
                            HttpUtil.downloadFile(modVersion.downloadUrl, new File(file, modVersion.fileName));
                            SwingUtilities.invokeLater(() -> this.statusLabel.setText(string4 + " kuruldu -> " + (bl ? "plugins" : "mods")));
                        } else {
                            SwingUtilities.invokeLater(() -> this.statusLabel.setText(string4 + " icin uygun surum bulunamadi"));
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> this.statusLabel.setText(string4 + " icin uyumlu surum bulunamadi"));
                    }
                }
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> this.statusLabel.setText(string4 + " kurulamadi: " + exception.getMessage()));
            }
            SwingUtilities.invokeLater(() -> jButton.setEnabled(true));
        });
    }
}

