/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.ui;

import com.lubv.launcher.auth.MinecraftSession;
import com.lubv.launcher.core.L10n;
import com.lubv.launcher.ui.Theme;
import com.lubv.launcher.ui.UiFx;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class OfflineLoginDialog
extends JDialog {
    private final JTextField nameField;
    private MinecraftSession result;

    public OfflineLoginDialog(Frame frame) {
        super(frame, L10n.get("offline.title"), true);
        this.setSize(360, 200);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setDefaultCloseOperation(2);
        this.setLayout(new BorderLayout());
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, 1));
        jPanel.setBackground(Theme.BG_SURFACE);
        jPanel.setBorder(BorderFactory.createEmptyBorder(28, 28, 24, 28));
        JLabel jLabel = new JLabel(L10n.get("offline.player_name"));
        jLabel.setFont(new Font("SansSerif", 1, 16));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        jLabel.setAlignmentX(0.0f);
        JLabel jLabel2 = new JLabel("<html>" + L10n.get("offline.hint") + "</html>");
        jLabel2.setFont(jLabel2.getFont().deriveFont(11.0f));
        jLabel2.setForeground(Theme.TEXT_MUTED);
        jLabel2.setAlignmentX(0.0f);
        this.nameField = UiFx.searchField(L10n.get("offline.placeholder"));
        this.nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        this.nameField.setAlignmentX(0.0f);
        this.nameField.addActionListener(actionEvent -> this.submit());
        JButton jButton = UiFx.accentButton(L10n.get("offline.login"));
        JButton jButton2 = UiFx.ghostButton(L10n.get("offline.cancel"));
        jButton.addActionListener(actionEvent -> this.submit());
        jButton2.addActionListener(actionEvent -> {
            this.result = null;
            this.dispose();
        });
        JPanel jPanel2 = new JPanel(new FlowLayout(2, 6, 0));
        jPanel2.setOpaque(false);
        jPanel2.setAlignmentX(0.0f);
        jPanel2.add(jButton2);
        jPanel2.add(jButton);
        jPanel.add(jLabel);
        jPanel.add(Box.createVerticalStrut(6));
        jPanel.add(jLabel2);
        jPanel.add(Box.createVerticalStrut(16));
        jPanel.add(this.nameField);
        jPanel.add(Box.createVerticalStrut(16));
        jPanel.add(jPanel2);
        this.add(jPanel);
    }

    private void submit() {
        String string = this.nameField.getText().trim();
        if (string.isEmpty()) {
            JOptionPane.showMessageDialog(this, L10n.get("offline.err_empty"), L10n.get("offline.err_title"), 0);
            return;
        }
        if (string.length() > 16) {
            JOptionPane.showMessageDialog(this, L10n.get("offline.err_too_long"), L10n.get("offline.err_title"), 0);
            return;
        }
        this.result = MinecraftSession.offline(string);
        this.dispose();
    }

    public MinecraftSession showAndLogin() {
        this.setVisible(true);
        return this.result;
    }
}
