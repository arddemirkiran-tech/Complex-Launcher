package com.lubv.launcher.ui;

import com.lubv.launcher.core.L10n;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Tema Olusturucu v2 - tamamen yeniden yazildi.
 *
 * Ozellikler:
 *  - Canli uygulama: "Dene" butonu temayi tum launcher'a aninda uygular,
 *    kapatinca eski temaya doner (kaydetmezseniz).
 *  - 3 temel renk secip diger 10 rengi otomatik turetme (derive).
 *  - Hazir temalardan baslayip uzerinde oynama (preset yukle).
 *  - Tam launcher maketi (sidebar, instance kartlari, PLAY, log) canli
 *    onizleme - degisiklik anında cumleyle gorunur.
 *  - Rastgele uyumlu tema ureteci, .json export/import, isimlendirme.
 */
public class ThemeCreatorDialog extends JDialog {
    private CustomTheme draft;
    private final CustomTheme original;
    private boolean saved = false;

    private final List<SwatchRow> coreRows = new ArrayList<>();
    private final List<SwatchRow> allRows = new ArrayList<>();
    private PreviewPanel preview;
    private JTextField nameField;

    // V29.6: köşe/odak/typografi kontrolcileri
    private javax.swing.JSlider arcSlider, focusSlider, sizeSlider;
    private javax.swing.JLabel arcVal, focusVal, sizeVal, fontSample;
    private javax.swing.JComboBox<String> fontCombo, headingCombo, buttonCombo;

    private static final String[] PRESET_NAMES = {
        "Nebula", "Obsidian", "Royal", "Cyberpunk", "Nord", "Sakura", "Mocha",
        "Dusk", "Aurora", "Midnight", "Storm", "Forest", "Crimson", "Ember",
        "Ocean", "Golden", "Light"
    };

    public ThemeCreatorDialog(Window owner) {
        this(owner, null);
    }

    public ThemeCreatorDialog(Window owner, String startFrom) {
        super(owner, L10n.isEnglish() ? "Theme Creator" : "Tema Olusturucu", ModalityType.APPLICATION_MODAL);
        this.original = CustomTheme.snapshotFromCurrent();
        if (startFrom != null && startFrom.contains("\u2605")) startFrom = "Custom";
        if (startFrom != null && "Custom".equalsIgnoreCase(startFrom) && CustomTheme.file().exists()) {
            this.draft = CustomTheme.load();
        } else {
            this.draft = CustomTheme.snapshotFromCurrent();
        }
        buildUi();
        pack();
        setSize(Math.max(880, getWidth()), Math.max(640, getHeight()));
        // V41: yagan fotolar bolumunu alt banda ekle (varsa content pane)
        try {
            java.awt.Container cp = getContentPane();
            java.awt.LayoutManager lm = cp.getLayout();
            if (lm instanceof java.awt.BorderLayout) {
                JPanel fp = sectionFallingPhotos();
                JPanel wrap = new JPanel(new java.awt.BorderLayout());
                wrap.setOpaque(false);
                wrap.add(fp, java.awt.BorderLayout.CENTER);
                cp.add(wrap, java.awt.BorderLayout.SOUTH);
                // dialog yuksekligine sigdir
                pack();
            }
        }
        catch (Exception ignored) {
        }
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeAndRestore();
            }
        });
    }

    /* ------------ renk alanlari ------------ */

    private interface Getter { String get(CustomTheme t); }
    private interface Setter { void set(CustomTheme t, String v); }

    private static final String[][] CORE_FIELDS = {
        {"accent",      "core.accent"},
        {"bgBase",      "core.bgBase"},
        {"textPrimary", "core.text"}
    };

    private static final String[][] ADVANCED_FIELDS = {
        {"accentBright",    "adv.accentBright"},
        {"accentDark",      "adv.accentDark"},
        {"bgSurface",       "adv.bgSurface"},
        {"bgElevated",      "adv.bgElevated"},
        {"bgBorder",        "adv.bgBorder"},
        {"titleBar",        "adv.titleBar"},
        {"textSecondary",   "adv.textSecondary"},
        {"textMuted",       "adv.textMuted"},
        {"cardGradientTop", "adv.cardTop"},
        {"cardGradientBot", "adv.cardBot"}
    };

    private class SwatchRow extends JPanel {
        final String field;
        final javax.swing.JLabel colorLabel;
        final javax.swing.JButton pickBtn;

        SwatchRow(String field, String labelKey) {
            this.field = field;
            setOpaque(false);
            setLayout(new BorderLayout(8, 0));
            // V36.1 CANLI ONIZLEME: satirin uzerine gelince onizleme o rengin
            // uygulanmis haliyle aninda boyanir; cikinca eskisine doner.
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    hoverPreview(field);
                }
            });
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hoverPreview(field);
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    endHoverPreview();
                }
            });
            JLabel name = UiFx.label(fieldLabel(labelKey));
            name.setPreferredSize(new Dimension(200, 24));
            add(name, "West");
            this.colorLabel = UiFx.label("");
            this.colorLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            add(this.colorLabel, "Center");
            this.pickBtn = UiFx.ghostButton(L10n.isEnglish() ? "Pick" : "Sec");
            add(this.pickBtn, "East");
            refresh();
            this.pickBtn.addActionListener(e -> {
                endHoverPreview();
                Color chosen = JColorChooser.showDialog(ThemeCreatorDialog.this,
                    L10n.isEnglish() ? "Pick color" : "Renk sec", colorOf(draft, field));
                if (chosen != null) {
                    applyField(draft, field, String.format("#%02X%02X%02X", chosen.getRed(), chosen.getGreen(), chosen.getBlue()));
                    // Cekirdek degistiyse yuzeyler yeni cekirdege gore aninda
                    // turetilir (v3: yuzeyler asla bayat kalmaz).
                    if (field.equals("accent") || field.equals("bgBase")) {
                        if (field.equals("bgBase")) draft.autoDetectLight();
                        draft.coreChanged();
                    }
                    refreshAll();
                    // V38 FIX: secim sonrasi bayat hover snapshot'i gecersiz kil.
                    // Aksi halde sonraki hover, secilen rengi ESKI degerlerle
                    // eziyordu ("sectigim renk otomatik degisiyo" bug'u).
                    hoverSaved = null;
                    hoverApplied = false;
                }
            });
        }

        void refresh() {
            Color c = colorOf(draft, field);
            colorLabel.setText(String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()));
            colorLabel.setForeground(c);
            pickBtn.setForeground(c);
            repaint();
        }
    }

    private void refreshAll() {
        // V38 FIX: draft degisti -> hover snapshot artik gecersiz.
        this.hoverSaved = null;
        this.hoverApplied = false;
        for (SwatchRow r : allRows) r.refresh();
        nameField.setText(draft.name);
        refreshControls();
        if (preview != null) preview.repaint();
    }

    /** Kontrolcileri taslaktan yeniden okur (preset/random/import sonrasi). */
    private void refreshControls() {
        if (arcSlider == null) return;
        arcSlider.setValue(draft.uiArc);
        arcVal.setText(draft.uiArc + " px");
        focusSlider.setValue(draft.focusWidth);
        focusVal.setText(draft.focusWidth + " px");
        sizeSlider.setValue(draft.fontSize);
        sizeVal.setText(draft.fontSize + " px");
        for (int i = 0; i < CustomTheme.FONT_CHOICES.length; i++) {
            if (java.util.Objects.equals(CustomTheme.FONT_CHOICES[i], draft.fontFamily)) {
                fontCombo.setSelectedIndex(i);
                break;
            }
        }
        headingCombo.setSelectedIndex(draft.headingWeight == 1 ? 1 : 0);
        buttonCombo.setSelectedIndex(draft.buttonWeight == 1 ? 1 : 0);
        fontSample.setFont(fontFor(draft.fontSize + 2.0f, Font.PLAIN));
    }

    private JPanel sliderRow(String label, JSlider slider, javax.swing.JLabel val) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        javax.swing.JLabel l = UiFx.label(label);
        l.setPreferredSize(new Dimension(150, 22));
        row.add(l, "West");
        row.add(slider, "Center");
        val.setPreferredSize(new Dimension(48, 22));
        row.add(val, "East");
        return row;
    }

    /** Tema taslagina gore yazı tipi (aile + boyut + stil). */
    private Font fontFor(float size, int style) {
        String fam = draft.fontFamily != null && !draft.fontFamily.isBlank() ? draft.fontFamily : "Dialog";
        return new Font(fam, style, Math.max(9, Math.round(size)));
    }

    private String fieldLabel(String key) {
        boolean en = L10n.isEnglish();
        switch (key) {
            case "core.accent":       return en ? "Accent (main color)" : "Vurgu (ana renk)";
            case "core.bgBase":       return en ? "Background" : "Arka plan";
            case "core.text":         return en ? "Text" : "Yazi";
            case "adv.accentBright":  return en ? "Accent (bright)" : "Vurgu (parlak)";
            case "adv.accentDark":    return en ? "Accent (dark)" : "Vurgu (koyu)";
            case "adv.bgSurface":     return en ? "Surface" : "Yuzey";
            case "adv.bgElevated":    return en ? "Elevated" : "Yuksek yuzey";
            case "adv.bgBorder":      return en ? "Borders" : "Kenarliklar";
            case "adv.titleBar":      return en ? "Title bar" : "Baslik cubugu";
            case "adv.textSecondary": return en ? "Text (secondary)" : "Yazi (ikincil)";
            case "adv.textMuted":     return en ? "Text (muted)" : "Yazi (soluk)";
            case "adv.cardTop":       return en ? "Card gradient (top)" : "Kart degradesi (ust)";
            case "adv.cardBot":       return en ? "Card gradient (bottom)" : "Kart degradesi (alt)";
            default:                  return key;
        }
    }

    /* ------------------- UI ------------------- */

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(16, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Theme.BG_BASE, getWidth(), getHeight(), Theme.BG_SURFACE));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // --- Ust serit: isim + preset yukleme ---
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        top.add(UiFx.label(L10n.isEnglish() ? "Name:" : "Isim:"));
        nameField = new JTextField(draft.name, 16);
        nameField.addActionListener(e -> draft.name = nameField.getText());
        top.add(nameField);
        top.add(UiFx.label(L10n.isEnglish() ? "Start from preset:" : "Hazir temadan basla:"));
        JComboBox<String> presetCombo = new JComboBox<String>(PRESET_NAMES);
        presetCombo.setLightWeightPopupEnabled(true);
        top.add(presetCombo);
        JButton loadPresetBtn = UiFx.ghostButton(L10n.isEnglish() ? "Load (live)" : "Yukle (canli)");
        loadPresetBtn.addActionListener(e -> loadPreset((String) presetCombo.getSelectedItem()));
        top.add(loadPresetBtn);
        root.add(top, "North");

        // --- Sol: renk editoeru ---
        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);

        JPanel corePanel = new JPanel(new GridBagLayout());
        corePanel.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;
        gc.insets = new Insets(4, 0, 4, 0);
        for (String[] f : CORE_FIELDS) {
            SwatchRow row = new SwatchRow(f[0], f[1]);
            coreRows.add(row); allRows.add(row);
            corePanel.add(row, gc);
            gc.gridy++;
        }

        JPanel advPanel = new JPanel(new GridBagLayout());
        advPanel.setOpaque(false);
        GridBagConstraints gc2 = new GridBagConstraints();
        gc2.gridx = 0; gc2.gridy = 0; gc2.fill = GridBagConstraints.HORIZONTAL; gc2.weightx = 1.0;
        gc2.insets = new Insets(3, 0, 3, 0);
        for (String[] f : ADVANCED_FIELDS) {
            SwatchRow row = new SwatchRow(f[0], f[1]);
            allRows.add(row);
            advPanel.add(row, gc2);
            gc2.gridy++;
        }
        advPanel.setVisible(false);

        JButton deriveBtn = UiFx.accentButton(L10n.isEnglish() ? "\u2728 Auto-derive other colors" : "\u2728 Diger renkleri otomatik turet");
        deriveBtn.setToolTipText(L10n.isEnglish()
            ? "Builds the full palette from accent + background"
            : "Vurgu + arka plandan tum paleti uyumlu sekilde uretir");
        deriveBtn.addActionListener(e -> {
            draft.autoDetectLight();
            // Tam turetme: elle duzenlenen gelismis alanlar da yenilenir.
            draft.userEdited.clear();
            draft.derive();
            refreshAll();
        });

        JCheckBox advToggle = new JCheckBox(L10n.isEnglish() ? "Advanced colors (edit all 13)" : "Gelismis renkler (13'unu duzenle)");
        advToggle.setOpaque(false);
        advToggle.setForeground(Theme.TEXT_SECONDARY);
        advToggle.addActionListener(e -> advPanel.setVisible(advToggle.isSelected()));
        advToggle.addActionListener(e -> pack());

        JPanel leftWrap = new JPanel(new BorderLayout(0, 8));
        leftWrap.setOpaque(false);
        leftWrap.add(corePanel, "North");
        leftWrap.add(deriveBtn, "Center");
        leftWrap.add(advToggle, "South");
        left.add(leftWrap, "North");
        left.add(advPanel, "Center");
        root.add(left, "West");

        // --- V29.6: Dugme & yazı stili editoru (sol altta) ---
        JPanel shapePanel = new JPanel(new GridBagLayout());
        shapePanel.setOpaque(false);
        shapePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Theme.BG_BORDER),
            L10n.isEnglish() ? "Buttons & Typography" : "D\u00fcgmeler & Yaz\u0131 Stili",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            Theme.uiFont(Font.BOLD, 11.0f), Theme.TEXT_SECONDARY));
        GridBagConstraints gs = new GridBagConstraints();
        gs.gridx = 0; gs.gridy = 0; gs.fill = GridBagConstraints.HORIZONTAL; gs.weightx = 1.0;
        gs.insets = new Insets(3, 6, 3, 6);

        // Köşe yuvarlaklığı
        arcSlider = new JSlider(0, 24, draft.uiArc);
        arcSlider.setPaintTicks(false);
        arcSlider.setOpaque(false);
        arcVal = UiFx.label(draft.uiArc + " px");
        arcSlider.addChangeListener(e -> {
            draft.uiArc = arcSlider.getValue();
            arcVal.setText(draft.uiArc + " px");
            if (preview != null) preview.repaint();
        });
        shapePanel.add(sliderRow(L10n.isEnglish() ? "Corner radius" : "K\u00f6\u015fe yuvarlakl\u0131\u011f\u0131", arcSlider, arcVal), gs);
        gs.gridy++;

        // Odak halkası
        focusSlider = new JSlider(0, 3, draft.focusWidth);
        focusSlider.setOpaque(false);
        focusVal = UiFx.label(draft.focusWidth + " px");
        focusSlider.addChangeListener(e -> {
            draft.focusWidth = focusSlider.getValue();
            focusVal.setText(draft.focusWidth + " px");
            if (preview != null) preview.repaint();
        });
        shapePanel.add(sliderRow(L10n.isEnglish() ? "Focus ring" : "Odak halkas\u0131", focusSlider, focusVal), gs);
        gs.gridy++;

        // Yazı tipi ailesi
        JPanel fontRow = new JPanel(new BorderLayout(6, 0));
        fontRow.setOpaque(false);
        javax.swing.JLabel fontLabel = UiFx.label(L10n.isEnglish() ? "Font family" : "Yaz\u0131 tipi");
        fontLabel.setPreferredSize(new Dimension(150, 22));
        fontRow.add(fontLabel, "West");
        String[] fontNames = new String[CustomTheme.FONT_CHOICES.length];
        for (int i = 0; i < fontNames.length; i++) fontNames[i] = CustomTheme.FONT_CHOICES[i] != null ? CustomTheme.FONT_CHOICES[i] : (L10n.isEnglish() ? "Default" : "\u00d6ntan\u0131ml\u0131");
        fontCombo = new JComboBox<>(fontNames);
        fontCombo.setLightWeightPopupEnabled(true);
        // V34: Her font kendi ailesiyle yazilsin - kapanmis kutuda da,
        // acilir listedeki her satirda da.
        fontCombo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                java.awt.Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof String) {
                    String fam = ((String)value).trim();
                    boolean isDefault = (L10n.isEnglish() ? "Default" : "\u00d6ntan\u0131ml\u0131").equals(fam);
                    if (!isDefault) c.setFont(new Font(fam, Font.PLAIN, c.getFont().getSize() + 1));
                }
                return c;
            }
        });
        fontCombo.setFont(new Font(draft.fontFamily != null && !draft.fontFamily.isBlank() ? draft.fontFamily : "Dialog", Font.PLAIN, 13));
        fontSample = UiFx.label("");
        fontSample.setFont(fontFor(draft.fontSize + 2.0f, Font.PLAIN));
        fontSample.setText("Aa\u0130\u015f\u00d6\u00e7\u011f\u00fc 123");
        fontCombo.addActionListener(e -> {
            int i = fontCombo.getSelectedIndex();
            draft.fontFamily = i <= 0 ? null : CustomTheme.FONT_CHOICES[i];
            fontSample.setFont(fontFor(draft.fontSize + 2.0f, Font.PLAIN));
            fontCombo.setFont(new Font(draft.fontFamily != null && !draft.fontFamily.isBlank() ? draft.fontFamily : "Dialog", Font.PLAIN, 13));
            if (preview != null) preview.repaint();
        });
        JPanel fontCol = new JPanel(new java.awt.GridLayout(2, 1, 0, 2));
        fontCol.setOpaque(false);
        fontCol.add(fontCombo);
        fontCol.add(fontSample);
        fontRow.add(fontCol, "Center");
        shapePanel.add(fontRow, gs);
        gs.gridy++;

        // Yazı boyutu
        sizeSlider = new JSlider(10, 18, draft.fontSize);
        sizeSlider.setOpaque(false);
        sizeVal = UiFx.label(draft.fontSize + " px");
        sizeSlider.addChangeListener(e -> {
            draft.fontSize = sizeSlider.getValue();
            sizeVal.setText(draft.fontSize + " px");
            fontSample.setFont(fontFor(draft.fontSize + 2.0f, Font.PLAIN));
            if (preview != null) preview.repaint();
        });
        shapePanel.add(sliderRow(L10n.isEnglish() ? "Font size" : "Yaz\u0131 boyutu", sizeSlider, sizeVal), gs);
        gs.gridy++;

        // Başlık kalınlığı
        JPanel hRow = new JPanel(new BorderLayout(6, 0));
        hRow.setOpaque(false);
        javax.swing.JLabel hLabel = UiFx.label(L10n.isEnglish() ? "Title weight" : "Ba\u015fl\u0131k kal\u0131nl\u0131\u011f\u0131");
        hLabel.setPreferredSize(new Dimension(150, 22));
        hRow.add(hLabel, "West");
        headingCombo = new JComboBox<>(new String[]{L10n.isEnglish() ? "Normal" : "Normal", L10n.isEnglish() ? "Bold" : "Kal\u0131n"});
        headingCombo.setLightWeightPopupEnabled(true);
        headingCombo.setSelectedIndex(draft.headingWeight == 1 ? 1 : 0);
        headingCombo.addActionListener(e -> {
            draft.headingWeight = headingCombo.getSelectedIndex();
            if (preview != null) preview.repaint();
        });
        hRow.add(headingCombo, "Center");
        shapePanel.add(hRow, gs);
        gs.gridy++;

        // Düğme metni kalınlığı
        JPanel bRow = new JPanel(new BorderLayout(6, 0));
        bRow.setOpaque(false);
        javax.swing.JLabel bLabel = UiFx.label(L10n.isEnglish() ? "Button text" : "D\u00fc\u011fme yaz\u0131s\u0131");
        bLabel.setPreferredSize(new Dimension(150, 22));
        bRow.add(bLabel, "West");
        buttonCombo = new JComboBox<>(new String[]{L10n.isEnglish() ? "Normal" : "Normal", L10n.isEnglish() ? "Bold" : "Kal\u0131n"});
        buttonCombo.setLightWeightPopupEnabled(true);
        buttonCombo.setSelectedIndex(draft.buttonWeight == 1 ? 1 : 0);
        buttonCombo.addActionListener(e -> {
            draft.buttonWeight = buttonCombo.getSelectedIndex();
            if (preview != null) preview.repaint();
        });
        bRow.add(buttonCombo, "Center");
        shapePanel.add(bRow, gs);

        // Sol sütunu 2 satıra böl: ustte renk editoru, altta sekil editoru.
        JPanel leftColumn = new JPanel(new BorderLayout(0, 8));
        leftColumn.setOpaque(false);
        leftColumn.add(left, "North");
        leftColumn.add(shapePanel, "Center");
        root.add(leftColumn, "West");

        // --- Sag: canli onizleme + eylem butonlari ---
        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);
        preview = new PreviewPanel();
        preview.setPreferredSize(new Dimension(430, 380));
        preview.setBorder(BorderFactory.createLineBorder(Theme.BG_BORDER, 1));
        right.add(preview, "Center");

        JPanel actions = new JPanel(new java.awt.GridLayout(0, 2, 8, 8));
        actions.setOpaque(false);

        JButton tryBtn = UiFx.accentButton(L10n.isEnglish() ? "\u26A1 Try Live" : "\u26A1 Canli Dene");
        tryBtn.setToolTipText(L10n.isEnglish()
            ? "Applies instantly to the whole launcher; close without saving to revert"
            : "Tum launcher'a aninda uygular; kaydetmeden kapatirsaniz eski temaya doner");
        tryBtn.addActionListener(e -> {
            // V29.1: ozel tema da tum launcher'a CANLI uygulanir - yapim
            // sirasinda kopyalanan renkler (setForeground(Theme.X) vb.)
            // kimlik eslestirmesiyle yenileriyle degistirilir. Boylece
            // "tema sadece bir kismi boyuyor" sorunu ozel temalarda da olmaz.
            if (getOwner() instanceof MainWindow mw) {
                mw.applyThemeLive(() -> Theme.applyCustom(draft));
                javax.swing.SwingUtilities.updateComponentTreeUI(mw);
                mw.refreshThemeChrome();
            } else {
                Theme.applyCustom(draft);
            }
        });

        JButton randomBtn = UiFx.ghostButton(L10n.isEnglish() ? "\uD83C\uDFB2 Randomize" : "\uD83C\uDFB2 Rastgele");
        randomBtn.addActionListener(e -> {
            draft = CustomTheme.random();
            refreshAll();
        });

        JButton saveBtn = UiFx.accentButton(L10n.isEnglish() ? "\u2714 Save & Apply" : "\u2714 Kaydet ve Uygula");
        saveBtn.addActionListener(e -> saveAndApply());

        JButton exportBtn = UiFx.ghostButton(L10n.isEnglish() ? "Export (.json)" : "Disa Aktar (.json)");
        exportBtn.addActionListener(e -> exportTheme());

        JButton importBtn = UiFx.ghostButton(L10n.isEnglish() ? "Import (.json)" : "Ice Aktar (.json)");
        importBtn.addActionListener(e -> importTheme());

        JButton resetBtn = UiFx.ghostButton(L10n.isEnglish() ? "Reset" : "Sifirla");
        resetBtn.addActionListener(e -> {
            draft = CustomTheme.snapshotFromCurrent();
            refreshAll();
        });

        actions.add(tryBtn);
        actions.add(randomBtn);
        actions.add(saveBtn);
        actions.add(exportBtn);
        actions.add(importBtn);
        actions.add(resetBtn);
        right.add(actions, "South");

        root.add(right, "Center");
        // V29.7: tum icerik kaydirilabilir - gelismis renkler/sekil editoru
        // acilinca dialog ekrandan tasarsa bile asagi scroll yapilabilir.
        // Eski yapi scroll'suzdu; buyuyen sol sutun alttaki kontrollere
        // ulasilamaz hale geliyordu.
        javax.swing.JScrollPane scroller = new javax.swing.JScrollPane(root,
            javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setBorder(null);
        scroller.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(scroller);
        refreshAll();
    }

    private void loadPreset(String name) {
        if (name == null) return;
        try {
            // Preset'i canli olarak tum launcher'a yansit, renklerini
            // taslagi al, sonra draft temaya geri don.
            if (getOwner() instanceof MainWindow mw) {
                mw.applyThemeByName(name);
            } else {
                Theme.apply(name);
            }
            CustomTheme loaded = CustomTheme.snapshotFromCurrent();
            loaded.name = draft.name;                // kullanicinin ismini koru
            Theme.applyCustom(draft);                // draft'a geri don
            if (getOwner() instanceof MainWindow mw2) {
                mw2.applyThemeLive(() -> Theme.applyCustom(draft));
                javax.swing.SwingUtilities.updateComponentTreeUI(mw2);
                mw2.refreshThemeChrome();
            }
            draft = loaded;
            refreshAll();
        } catch (Exception ex) {
            // preset yuklenemediyse sessizce devam
        }
    }

    private void saveAndApply() {
        draft.name = nameField.getText().trim();
        if (draft.name.isEmpty()) draft.name = "My Theme";
        draft.save();
        saved = true;
        if (getOwner() instanceof MainWindow mw) {
            mw.applyThemeLive(() -> Theme.applyCustom(draft));
            javax.swing.SwingUtilities.updateComponentTreeUI(mw);
            mw.onCustomThemeApplied();
            mw.refreshThemeChrome();
        } else {
            Theme.applyCustom(draft);
        }
        refreshAll();
        JOptionPane.showMessageDialog(this,
            L10n.isEnglish() ? "Theme saved and applied.\nIt will load on every startup." : "Tema kaydedildi ve uygulandi.\nHer acilista yuklenecek.",
            L10n.isEnglish() ? "Saved" : "Kaydedildi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void closeAndRestore() {
        if (!saved) {
            // Kaydedilmediyse launcher'i acilistaki temaya geri dondur.
            if (getOwner() instanceof MainWindow mw) {
                mw.applyThemeLive(() -> Theme.applyCustom(original));
                javax.swing.SwingUtilities.updateComponentTreeUI(mw);
                mw.refreshThemeChrome();
            } else {
                Theme.applyCustom(original);
            }
        }
        dispose();
    }

    private void exportTheme() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File((draft.name == null || draft.name.isBlank() ? "theme" : draft.name).replaceAll("[^A-Za-z0-9-_ ]", "").trim() + ".json"));
        fc.setFileFilter(new FileNameExtensionFilter("JSON", "json"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File out = fc.getSelectedFile();
            if (!out.getName().toLowerCase().endsWith(".json")) out = new File(out.getParentFile(), out.getName() + ".json");
            if (draft.saveTo(out)) {
                JOptionPane.showMessageDialog(this,
                    (L10n.isEnglish() ? "Theme exported:\n" : "Tema disa aktarildi:\n") + out.getAbsolutePath(),
                    L10n.isEnglish() ? "Exported" : "Disa Aktarildi", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    L10n.isEnglish() ? "Export failed." : "Disa aktarma basarisiz.",
                    L10n.isEnglish() ? "Error" : "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importTheme() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("JSON", "json"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            CustomTheme loaded = CustomTheme.loadFrom(fc.getSelectedFile());
            if (loaded != null) {
                draft = loaded;
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(this,
                    L10n.isEnglish() ? "Invalid theme file." : "Gecersiz tema dosyasi.",
                    L10n.isEnglish() ? "Error" : "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /* ------------------- Canli launcher maketi ------------------- */

    /** Temanin gercek launcher'daki halini birebir taklit eden canli
     *  onizleme: baslik cubugu, sidebar sekmeleri, instance kartlari,
     *  PLAY butonu ve log alani. */
    /* ------------ V36.1 canli hover onizleme ------------ */

    /** Hover edilen rengi onizlemeye gecici olarak uygula. */
    private void hoverPreview(String field) {
        if (hoverApplied || field == null || field.isBlank()) return;
        if (hoverSaved == null) {
            hoverSaved = new java.util.HashMap<>();
            for (SwatchRow r : allRows) {
                hoverSaved.put(r.field, colorOf(draft, r.field));
            }
        }
        if ("accent".equals(field) || "bgBase".equals(field)) {
            // V38 FIX: autoDetectLight hover sirasinda draft.light'i KALICI
            // flipliyordu; dialog birden FlatLightLaf'a gecip bozuluyordu.
            // Onceki degeri kaydet, hover bitince geri yukle.
            this.hoverLightBefore = draft.light;
            if ("bgBase".equals(field)) draft.autoDetectLight();
            draft.coreChanged();
        }
        refreshAllHoverSafe();
        hoverApplied = true;
    }

    /** Onizlemeyi hover oncesi renklere geri getir. */
    private void endHoverPreview() {
        if (!hoverApplied) return;
        if (hoverSaved != null) {
            for (java.util.Map.Entry<String, Color> en : hoverSaved.entrySet()) {
                setFieldColor(draft, en.getKey(), en.getValue());
            }
        }
        if (this.hoverLightBefore != null) {
            draft.light = this.hoverLightBefore; // LAF modunu geri al
            this.hoverLightBefore = null;
        }
        if (preview != null) preview.repaint();
        hoverApplied = false;
    }

    private boolean hoverApplied = false;
    private java.util.Map<String, Color> hoverSaved;
    /** Hover sirasinda kaydedilen draft.light (LAF flip'onlemi). */
    private java.lang.Boolean hoverLightBefore = null;

    /** Hover sirasinda kontrolleri kirletmeden onizlemeyi tazele. */
    private void refreshAllHoverSafe() {
        for (SwatchRow r : allRows) r.refresh();
        if (preview != null) preview.repaint();
    }

    /** Draft alanina dogrudan Color yazar (markEdited cagirmadan). */
    private static void setFieldColor(CustomTheme t, String field, Color c) {
        String hex = String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
        switch (field) {
            case "accent" -> t.accent = hex;
            case "accentBright" -> t.accentBright = hex;
            case "accentDark" -> t.accentDark = hex;
            case "bgBase" -> t.bgBase = hex;
            case "bgSurface" -> t.bgSurface = hex;
            case "bgElevated" -> t.bgElevated = hex;
            case "bgBorder" -> t.bgBorder = hex;
            case "titleBar" -> t.titleBar = hex;
            case "textPrimary" -> t.textPrimary = hex;
            case "textSecondary" -> t.textSecondary = hex;
            case "textMuted" -> t.textMuted = hex;
            case "cardGradientTop" -> t.cardGradientTop = hex;
            case "cardGradientBot" -> t.cardGradientBot = hex;
            default -> { }
        }
    }

    private class PreviewPanel extends JPanel {
        PreviewPanel() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            // pencere zemini
            g2.setPaint(new GradientPaint(0, 0, draft.bgBaseColor(), 0, h, draft.bgSurfaceColor()));
            g2.fillRoundRect(0, 0, w, h, 14, 14);

            // baslik cubugu + pencere butonlari
            g2.setColor(draft.titleBarColor());
            g2.fillRoundRect(0, 0, w, 30, 14, 14);
            g2.fillRect(0, 15, w, 15);
            g2.setColor(draft.textPrimaryColor());
            g2.setFont(fontFor(12f, draft.headingWeight == 1 ? Font.BOLD : Font.PLAIN));
            g2.drawString("Complex Launcher", 12, 20);
            g2.setColor(draft.textMutedColor());
            g2.fillRoundRect(w - 66, 9, 14, 12, 4, 4);
            g2.fillRoundRect(w - 46, 9, 14, 12, 4, 4);
            g2.setColor(new Color(239, 68, 68));
            g2.fillRoundRect(w - 26, 9, 14, 12, 4, 4);

            // sidebar
            int sx = 10, sy = 38, sw = 108;
            g2.setColor(draft.bgSurfaceColor());
            g2.fillRoundRect(sx, sy, sw, h - sy - 12, 10, 10);
            String[] tabs = {L10n.isEnglish() ? "Home" : "Ana Sayfa", L10n.isEnglish() ? "Mods" : "Modlar", L10n.isEnglish() ? "Shaders" : "Shaderlar", L10n.isEnglish() ? "Servers" : "Sunucular"};
            for (int i = 0; i < tabs.length; i++) {
                int ty = sy + 10 + i * 30;
                if (i == 0) {
                    g2.setColor(new Color(draft.accentColor().getRed(), draft.accentColor().getGreen(), draft.accentColor().getBlue(), 70));
                    g2.fillRoundRect(sx + 5, ty - 3, sw - 10, 24, 8, 8);
                    g2.setColor(draft.accentColor());
                    g2.fillRoundRect(sx + 5, ty - 3, 3, 24, 3, 3);
                }
                g2.setColor(i == 0 ? draft.textPrimaryColor() : draft.textSecondaryColor());
                g2.setFont(fontFor(11f, Font.PLAIN));
                g2.drawString(tabs[i], sx + 16, ty + 14);
            }

            // ana alan - instance karti (gradyan)
            int cx = sx + sw + 8, cw = w - cx - 12;
            g2.setPaint(new GradientPaint(cx, sy, draft.cardTopColor(), cx, sy + 96, draft.cardBotColor()));
            g2.fillRoundRect(cx, sy, cw, 96, 12, 12);
            g2.setColor(draft.accentColor());
            g2.fillRoundRect(cx + 10, sy + 12, 52, 52, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 15f));
            g2.drawString("M", cx + 24, sy + 46);
            g2.setColor(draft.textPrimaryColor());
            g2.setFont(fontFor(13f, draft.headingWeight == 1 ? Font.BOLD : Font.PLAIN));
            g2.drawString(L10n.isEnglish() ? "My Instance" : "Instance'im", cx + 74, sy + 26);
            g2.setColor(draft.textSecondaryColor());
            g2.setFont(fontFor(11f, Font.PLAIN));
            g2.drawString("Fabric 1.21.1  \u2022  32 " + (L10n.isEnglish() ? "mods" : "mod"), cx + 74, sy + 44);
            g2.setColor(draft.textMutedColor());
            g2.drawString(L10n.isEnglish() ? "2h 14m played" : "2s 14d oynandi", cx + 74, sy + 62);

            // PLAY butonu - tema köşe yuvarlaklığı + odak halkası ile
            int pArc = Math.min(draft.uiArc, 15);
            g2.setColor(draft.accentColor());
            g2.fillRoundRect(cx + 10, sy + 110, 120, 30, pArc * 2 >= 30 ? 30 : pArc, pArc * 2 >= 30 ? 30 : pArc);
            if (draft.focusWidth > 0) {
                // odak halkası demosu: butonun etrafinca accent cizgi
                Graphics2D f2 = (Graphics2D) g2.create();
                f2.setColor(draft.accentBrightColor());
                f2.setStroke(new java.awt.BasicStroke(draft.focusWidth));
                f2.drawRoundRect(cx + 10 - draft.focusWidth - 1, sy + 110 - draft.focusWidth - 1,
                    120 + 2 * (draft.focusWidth + 1), 30 + 2 * (draft.focusWidth + 1), pArc + 2, pArc + 2);
                f2.dispose();
            }
            g2.setColor(Color.WHITE);
            g2.setFont(fontFor(13f, draft.buttonWeight == 1 ? Font.BOLD : Font.PLAIN));
            g2.drawString("\u25B6 " + (L10n.isEnglish() ? "PLAY" : "OYNA"), cx + 42, sy + 130);

            // ikincil butonlar - ayni tema yuvarlakligi ile
            int sArc = Math.min(pArc, 15);
            g2.setColor(draft.bgElevatedColor());
            g2.fillRoundRect(cx + 140, sy + 110, 76, 30, sArc * 2 >= 30 ? 30 : sArc * 2, sArc * 2 >= 30 ? 30 : sArc * 2);
            g2.setColor(draft.textSecondaryColor());
            g2.drawRoundRect(cx + 140, sy + 110, 76, 30, sArc * 2 >= 30 ? 30 : sArc * 2, sArc * 2 >= 30 ? 30 : sArc * 2);
            g2.setFont(fontFor(12f, draft.buttonWeight == 1 ? Font.BOLD : Font.PLAIN));
            g2.drawString(L10n.isEnglish() ? "Mods" : "Modlar", cx + 158, sy + 130);
            g2.setColor(draft.bgElevatedColor());
            g2.fillRoundRect(cx + 224, sy + 110, 76, 30, sArc * 2 >= 30 ? 30 : sArc * 2, sArc * 2 >= 30 ? 30 : sArc * 2);
            g2.setColor(draft.textSecondaryColor());
            g2.drawRoundRect(cx + 224, sy + 110, 76, 30, sArc * 2 >= 30 ? 30 : sArc * 2, sArc * 2 >= 30 ? 30 : sArc * 2);
            g2.drawString(L10n.isEnglish() ? "Add" : "Ekle", cx + 248, sy + 130);

            // log alani
            int ly = sy + 150;
            g2.setColor(draft.bgElevatedColor());
            g2.fillRoundRect(cx, ly, cw, h - ly - 12, Math.max(2, draft.uiArc), Math.max(2, draft.uiArc));
            g2.setFont(fontFor(11f, Font.PLAIN));
            g2.setColor(draft.accentBrightColor());
            g2.drawString("[" + (L10n.isEnglish() ? "OK" : "TM") + "] sodium.jar", cx + 10, ly + 18);
            g2.setColor(draft.textSecondaryColor());
            g2.drawString("[" + (L10n.isEnglish() ? "OK" : "TM") + "] lithium.jar", cx + 10, ly + 36);
            g2.setColor(draft.textMutedColor());
            g2.drawString("[" + (L10n.isEnglish() ? "OK" : "TM") + "] ferrite-core.jar", cx + 10, ly + 54);

            // alt vurgu cubugu
            g2.setColor(draft.accentColor());
            g2.fillRoundRect(10, h - 8, w - 20, 4, 4, 4);
            g2.dispose();
        }
    }

    /* ------------------- alan erisimi ------------------- */

    private static Color colorOf(CustomTheme t, String field) {
        return switch (field) {
            case "accent" -> t.accentColor();
            case "accentBright" -> t.accentBrightColor();
            case "accentDark" -> t.accentDarkColor();
            case "bgBase" -> t.bgBaseColor();
            case "bgSurface" -> t.bgSurfaceColor();
            case "bgElevated" -> t.bgElevatedColor();
            case "bgBorder" -> t.bgBorderColor();
            case "titleBar" -> t.titleBarColor();
            case "textPrimary" -> t.textPrimaryColor();
            case "textSecondary" -> t.textSecondaryColor();
            case "textMuted" -> t.textMutedColor();
            case "cardGradientTop" -> t.cardTopColor();
            case "cardGradientBot" -> t.cardBotColor();
            default -> Color.GRAY;
        };
    }

    private static void applyField(CustomTheme t, String field, String hex) {
        switch (field) {
            case "accent" -> t.accent = hex;
            case "accentBright" -> t.accentBright = hex;
            case "accentDark" -> t.accentDark = hex;
            case "bgBase" -> t.bgBase = hex;
            case "bgSurface" -> t.bgSurface = hex;
            case "bgElevated" -> t.bgElevated = hex;
            case "bgBorder" -> t.bgBorder = hex;
            case "titleBar" -> t.titleBar = hex;
            case "textPrimary" -> t.textPrimary = hex;
            case "textSecondary" -> t.textSecondary = hex;
            case "textMuted" -> t.textMuted = hex;
            case "cardGradientTop" -> t.cardGradientTop = hex;
            case "cardGradientBot" -> t.cardGradientBot = hex;
            default -> { }
        }
        // Kullanicinin elle duzenledigi alan olarak isaretle: yalnizca
        // bunlar theme'e override olarak uygulanir.
        t.markEdited(field);
    }
    /**
     * V41: Tema Olusturucu icinde arka plana yagan fotograflar bolumu.
     * Ayarlarla paylasilir (Settings.fallingPhotoPaths/Density); canli uygulanir.
     */
    private JPanel sectionFallingPhotos() {
        JPanel p = new JPanel(new java.awt.GridBagLayout());
        p.setOpaque(false);
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1.0;
        gc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gc.insets = new java.awt.Insets(6, 8, 2, 8);
        boolean en = L10n.isEnglish();
        javax.swing.JLabel title = new javax.swing.JLabel(en ? "Falling background photos" : "Arka plana yağan fotoğraflar");
        title.setForeground(getForeground());
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 13.0f));
        p.add(title, gc);

        com.lubv.launcher.core.Settings st = null;
        if (getOwner() instanceof MainWindow mw) {
            st = mw.settings();
        }
        final com.lubv.launcher.core.Settings settings = st;

        gc.gridy = 1;
        gc.insets = new java.awt.Insets(2, 8, 6, 8);
        JPanel row = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        javax.swing.JLabel status = new javax.swing.JLabel();
        javax.swing.JSlider density = new javax.swing.JSlider(0, 100,
            settings == null ? 40 : Math.max(0, Math.min(100, settings.fallingPhotoDensity)));
        density.setOpaque(false);
        density.setPreferredSize(new java.awt.Dimension(140, 22));
        javax.swing.JLabel dl = new javax.swing.JLabel((en ? "Intensity: " : "Yoğunluk: ") + density.getValue() + "%");
        Runnable updateStatus = () -> {
            if (settings == null) return;
            boolean on = settings.fallingPhotoPaths != null && !settings.fallingPhotoPaths.isBlank()
                && settings.fallingPhotoDensity > 0;
            int cnt = on ? settings.fallingPhotoPaths.split("\\|").length : 0;
            status.setText(on ? ((en ? "\u2713 " : "\u2713 ") + cnt + (en ? " photo(s)" : " foto")) : (en ? "off" : "kapalı"));
        };
        javax.swing.JButton choose = new javax.swing.JButton(en ? "Choose photos…" : "Fotoğraf seç…");
        choose.addActionListener(e -> {
            if (settings == null) return;
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setMultiSelectionEnabled(true);
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                en ? "Images (png, jpg, jpeg, gif, bmp, webp)" : "Görseller (png, jpg, jpeg, gif, bmp, webp)",
                "png", "jpg", "jpeg", "gif", "bmp", "webp"));
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File[] fs = fc.getSelectedFiles();
                if (fs != null && fs.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (java.io.File f : fs) {
                        if (sb.length() > 0) sb.append('|');
                        sb.append(f.getAbsolutePath());
                    }
                    settings.fallingPhotoPaths = sb.toString();
                    settings.save();
                    if (getOwner() instanceof MainWindow mw) {
                        mw.applyFallingPhotosFromSettings();
                    }
                    updateStatus.run();
                }
            }
        });
        javax.swing.JButton reset = new javax.swing.JButton(en ? "Reset" : "Sıfırla");
        reset.addActionListener(e -> {
            if (settings == null) return;
            settings.fallingPhotoPaths = "";
            settings.save();
            if (getOwner() instanceof MainWindow mw) {
                mw.applyFallingPhotosFromSettings();
            }
            updateStatus.run();
        });
        density.addChangeListener(e -> {
            if (settings == null) return;
            settings.fallingPhotoDensity = density.getValue();
            settings.save();
            dl.setText((en ? "Intensity: " : "Yoğunluk: ") + density.getValue() + "%");
            if (getOwner() instanceof MainWindow mw) {
                mw.applyFallingPhotosFromSettings();
            }
        });
        updateStatus.run();
        row.add(choose);
        row.add(reset);
        row.add(density);
        row.add(dl);
        row.add(status);
        p.add(row, gc);
        return p;
    }

}
