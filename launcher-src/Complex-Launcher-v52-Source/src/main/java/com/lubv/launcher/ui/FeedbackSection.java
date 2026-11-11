/*
 * v46 - Ayarlar sekmesindeki "Oneri & Geri Bildirim" bolumu.
 */
package com.lubv.launcher.ui;

import com.lubv.launcher.core.L10n;
import com.lubv.launcher.core.SuggestionClient;
import com.lubv.launcher.update.UpdateManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * v46: Oneri &amp; Geri Bildirim karti.
 *
 * <p>Buradan gonderilen oneri {@link SuggestionClient} uzerinden sitedeki
 * api.php'ye yazilir ve sitedeki herkese acik "Suggestions" listesinde
 * aninda gorunur. Site formuyla ayni kategori degerleri kullanilir, yani
 * iki yerden gelen oneriler ayni listede karisik gorunur. Butun metinler
 * {@link L10n} uzerinden gelir: arayuz dili Ingilizce iken de calisir.
 */
public class FeedbackSection extends JPanel {
    private final Consumer<String> logger;
    private final JComboBox<String> categoryCombo;
    private final JTextField nameField;
    private final JTextArea textArea;
    private final JLabel countLabel;
    private final JLabel statusLabel;
    private final JButton sendButton;

    public FeedbackSection(Consumer<String> logger) {
        this.logger = logger;
        this.setLayout(new BorderLayout(0, 10));
        this.setOpaque(true);
        this.setBackground(Theme.BG_SURFACE);
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BG_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        // --- baslik + aciklama ---
        JPanel jPanel = new JPanel(new BorderLayout(12, 0));
        jPanel.setOpaque(false);
        JLabel jLabel = new JLabel("\uD83D\uDCA1");
        jLabel.setFont(jLabel.getFont().deriveFont(1, 22.0f));
        jLabel.setForeground(Theme.ACCENT_BRIGHT);
        JPanel jPanel2 = new JPanel(new GridLayout(2, 1, 0, 3));
        jPanel2.setOpaque(false);
        JLabel jLabel2 = new JLabel(L10n.get("feedback.card_title"));
        jLabel2.setFont(jLabel2.getFont().deriveFont(1, 13.5f));
        JLabel jLabel3 = new JLabel(FeedbackSection.wrapHtml(L10n.get("feedback.intro"), 430));
        jLabel3.setFont(jLabel3.getFont().deriveFont(0, 11.5f));
        jLabel3.setForeground(Theme.TEXT_MUTED);
        jPanel2.add(jLabel2);
        jPanel2.add(jLabel3);
        jPanel.add((Component)jLabel, "West");
        jPanel.add((Component)jPanel2, "Center");
        this.add((Component)jPanel, "North");

        // --- alanlar ---
        JPanel jPanel3 = new JPanel(new GridBagLayout());
        jPanel3.setOpaque(false);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 17;
        gridBagConstraints.fill = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.insets = new Insets(8, 0, 4, 8);
        jPanel3.add((Component)UiFx.label(L10n.get("feedback.category")), gridBagConstraints);
        this.categoryCombo = new JComboBox<String>(new String[]{
                L10n.get("feedback.cat.idea"),
                L10n.get("feedback.cat.bug"),
                L10n.get("feedback.cat.feature"),
                L10n.get("feedback.cat.mod"),
                L10n.get("feedback.cat.other")});
        this.categoryCombo.setSelectedIndex(2);
        this.categoryCombo.setToolTipText(L10n.get("feedback.category_tip"));
        gridBagConstraints.gridx = 1;
        jPanel3.add((Component)this.categoryCombo, gridBagConstraints);
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new Insets(8, 18, 4, 8);
        jPanel3.add((Component)UiFx.label(L10n.get("feedback.name")), gridBagConstraints);
        this.nameField = new JTextField(18);
        this.nameField.setToolTipText(L10n.get("feedback.name_tip"));
        gridBagConstraints.gridx = 3;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(8, 0, 4, 0);
        jPanel3.add((Component)this.nameField, gridBagConstraints);

        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.insets = new Insets(10, 0, 4, 0);
        jPanel3.add((Component)UiFx.label(L10n.get("feedback.message")), gridBagConstraints);

        this.textArea = new JTextArea(5, 40);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        this.textArea.setFont(this.textArea.getFont().deriveFont(12.5f));
        JScrollPane jScrollPane = UiFx.cleanScroll(this.textArea);
        jScrollPane.setPreferredSize(new Dimension(420, 104));
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 6, 0);
        jPanel3.add((Component)jScrollPane, gridBagConstraints);

        this.countLabel = new JLabel();
        this.countLabel.setFont(this.countLabel.getFont().deriveFont(0, 11.0f));
        this.countLabel.setForeground(Theme.TEXT_MUTED);
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.insets = new Insets(2, 0, 2, 14);
        jPanel3.add((Component)this.countLabel, gridBagConstraints);

        this.statusLabel = new JLabel(" ");
        this.statusLabel.setFont(this.statusLabel.getFont().deriveFont(0, 11.5f));
        this.statusLabel.setForeground(Theme.TEXT_MUTED);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(2, 0, 2, 14);
        jPanel3.add((Component)this.statusLabel, gridBagConstraints);

        JPanel jPanel4 = new JPanel(new FlowLayout(2, 8, 0));
        jPanel4.setOpaque(false);
        this.sendButton = UiFx.accentButton(L10n.get("feedback.send"));
        this.sendButton.addActionListener(actionEvent -> this.submitSuggestion());
        JButton jButton = UiFx.ghostButton(L10n.get("feedback.open_site"));
        jButton.setToolTipText(SuggestionClient.ideasUrl());
        jButton.addActionListener(actionEvent -> this.openSuggestionsPage());
        jPanel4.add(this.sendButton);
        jPanel4.add(jButton);
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.insets = new Insets(2, 0, 2, 0);
        jPanel3.add((Component)jPanel4, gridBagConstraints);

        JLabel jLabel4 = new JLabel(FeedbackSection.wrapHtml(L10n.get("feedback.tip"), 640));
        jLabel4.setFont(jLabel4.getFont().deriveFont(0, 11.0f));
        jLabel4.setForeground(Theme.TEXT_MUTED);
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(10, 0, 0, 0);
        jPanel3.add((Component)jLabel4, gridBagConstraints);
        this.add((Component)jPanel3, "Center");

        // Karakter sayaci
        this.textArea.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                FeedbackSection.this.updateCount();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                FeedbackSection.this.updateCount();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                FeedbackSection.this.updateCount();
            }
        });
        this.updateCount();
    }

    /** HTML'e gomerken uzun L10n metinlerini sarar (ve kacislar). */
    private static String wrapHtml(String string, int n) {
        String string2 = string == null ? "" : string.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html><body style='width:" + n + "px'>" + string2 + "</body></html>";
    }

    private void updateCount() {
        this.countLabel.setText(L10n.fmt("feedback.chars",
                Integer.valueOf(this.textArea.getText().length()),
                Integer.valueOf(SuggestionClient.TEXT_MAX)));
    }

    private void setStatus(String string, Color color) {
        this.statusLabel.setText(string == null || string.isEmpty() ? " " : string);
        this.statusLabel.setForeground(color == null ? Theme.TEXT_MUTED : color);
    }

    /** Oneriyi arka planda siteye gonderir; sonucu durum satirinda gosterir. */
    private void submitSuggestion() {
        String string = this.textArea.getText().trim();
        if (string.length() < SuggestionClient.TEXT_MIN) {
            this.setStatus(L10n.fmt("feedback.err_short", Integer.valueOf(SuggestionClient.TEXT_MIN)), Theme.RED);
            return;
        }
        if (string.length() > SuggestionClient.TEXT_MAX) {
            string = string.substring(0, SuggestionClient.TEXT_MAX);
        }
        final String string2 = string;
        final String string3 = SuggestionClient.categoryKey(this.categoryCombo.getSelectedIndex());
        final String string4 = this.nameField.getText().trim();
        final String string5 = UpdateManager.getLocalVersion();
        final String string6 = SuggestionClient.currentPlatform();
        this.sendButton.setEnabled(false);
        this.setStatus(L10n.get("feedback.sending"), Theme.TEXT_MUTED);
        new Thread(() -> {
            SuggestionClient.Result result = SuggestionClient.submit(string2, string4, string3, L10n.getLanguage(), string5, string6);
            SwingUtilities.invokeLater(() -> {
                this.sendButton.setEnabled(true);
                if (result.ok) {
                    this.textArea.setText("");
                    this.updateCount();
                    this.setStatus(L10n.get("feedback.sent"), Theme.GREEN);
                    if (this.logger != null) {
                        this.logger.accept(L10n.get("feedback.sent"));
                    }
                }
                else {
                    this.setStatus(this.errorText(result), Theme.RED);
                }
            });
        }, "suggestion-submit").start();
    }

    /** Sunucu/kod hatalarini arayuz diline cevirir. */
    private String errorText(SuggestionClient.Result result) {
        if (result == null) {
            return L10n.get("feedback.err_network");
        }
        if ("rate_limited".equals(result.errorCode)) {
            return L10n.get("feedback.err_rate");
        }
        if ("text_too_short".equals(result.errorCode)) {
            return L10n.fmt("feedback.err_short", Integer.valueOf(SuggestionClient.TEXT_MIN));
        }
        if ("network".equals(result.errorCode)) {
            return L10n.get("feedback.err_network");
        }
        String string = result.message == null ? "" : result.message.trim();
        return string.isEmpty() ? L10n.get("feedback.err_network") : L10n.fmt("feedback.err_reject", string);
    }

    /** Onerilerin listelendigi site bolumunu tarayicida acar. */
    private void openSuggestionsPage() {
        String string = SuggestionClient.ideasUrl();
        try {
            Desktop.getDesktop().browse(java.net.URI.create(string));
        }
        catch (Exception exception) {
            this.setStatus(L10n.fmt("feedback.open_failed", string), Theme.RED);
        }
    }
}
