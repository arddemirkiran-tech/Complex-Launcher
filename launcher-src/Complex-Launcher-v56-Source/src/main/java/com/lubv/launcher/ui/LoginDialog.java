/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.ui;

import com.lubv.launcher.auth.MicrosoftAuth;
import com.lubv.launcher.auth.MinecraftSession;
import com.lubv.launcher.core.L10n;
import com.lubv.launcher.ui.Theme;
import com.lubv.launcher.ui.UiFx;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.net.URI;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LoginDialog
extends JDialog {
    private final JLabel statusLabel = new JLabel(L10n.get("msa.opening_browser"));
    private final JTextField urlField;
    private final JButton continueBtn = UiFx.accentButton(L10n.get("msa.continue"));
    private final JButton browserBtn = UiFx.ghostButton(L10n.get("msa.reopen_browser"));
    private final JProgressBar spinner = new JProgressBar();
    private MinecraftSession result;
    private MicrosoftAuth.AuthRequest request;

    public LoginDialog(Frame frame) {
        super(frame, L10n.get("msa.title"), true);
        this.setSize(520, 360);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setDefaultCloseOperation(0);
        this.setLayout(new BorderLayout());
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, 1));
        jPanel.setBackground(Theme.BG_SURFACE);
        jPanel.setBorder(BorderFactory.createEmptyBorder(28, 28, 24, 28));
        JLabel jLabel = new JLabel(L10n.get("msa.heading"));
        jLabel.setFont(new Font("SansSerif", 1, 18));
        jLabel.setForeground(Theme.TEXT_PRIMARY);
        jLabel.setAlignmentX(0.0f);
        JLabel jLabel2 = new JLabel("<html><div style='width:430px'>" + L10n.get("msa.instructions") + "</div></html>");
        jLabel2.setFont(jLabel2.getFont().deriveFont(12.0f));
        jLabel2.setForeground(Theme.TEXT_SECONDARY);
        jLabel2.setAlignmentX(0.0f);
        this.statusLabel.setFont(this.statusLabel.getFont().deriveFont(12.0f));
        this.statusLabel.setForeground(Theme.ACCENT);
        this.statusLabel.setAlignmentX(0.0f);
        this.urlField = UiFx.searchField(L10n.get("msa.url_placeholder"));
        this.urlField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        this.urlField.setAlignmentX(0.0f);
        this.spinner.setIndeterminate(true);
        this.spinner.setPreferredSize(new Dimension(300, 3));
        this.spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
        this.spinner.setBorderPainted(false);
        this.spinner.setVisible(false);
        this.spinner.setAlignmentX(0.0f);
        JPanel jPanel2 = new JPanel(new FlowLayout(2, 6, 0));
        jPanel2.setOpaque(false);
        jPanel2.setAlignmentX(0.0f);
        JButton jButton = UiFx.ghostButton(L10n.get("msa.cancel"));
        jButton.addActionListener(actionEvent -> {
            this.result = null;
            this.dispose();
        });
        this.browserBtn.addActionListener(actionEvent -> this.openAuthUrl());
        this.continueBtn.addActionListener(actionEvent -> this.submitCode());
        jPanel2.add(jButton);
        jPanel2.add(this.browserBtn);
        jPanel2.add(this.continueBtn);
        jPanel.add(jLabel);
        jPanel.add(Box.createVerticalStrut(8));
        jPanel.add(jLabel2);
        jPanel.add(Box.createVerticalStrut(16));
        jPanel.add(this.statusLabel);
        jPanel.add(Box.createVerticalStrut(8));
        jPanel.add(this.urlField);
        jPanel.add(Box.createVerticalStrut(8));
        jPanel.add(this.spinner);
        jPanel.add(Box.createVerticalGlue());
        jPanel.add(UiFx.separator());
        jPanel.add(Box.createVerticalStrut(12));
        jPanel.add(jPanel2);
        this.add(jPanel);
    }

    public MinecraftSession showAndLogin() {
        this.request = MicrosoftAuth.beginLogin();
        this.openAuthUrl();
        this.setVisible(true);
        return this.result;
    }

    private void openAuthUrl() {
        if (this.request == null) {
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(this.request.authUrl));
            this.statusLabel.setText(L10n.get("msa.browser_opened"));
        }
        catch (Exception exception) {
            this.statusLabel.setText(L10n.get("msa.browser_failed"));
            this.urlField.setText(this.request.authUrl);
        }
    }

    private void submitCode() {
        String string = this.urlField.getText().trim();
        if (string.isEmpty()) {
            JOptionPane.showMessageDialog(this, L10n.get("msa.err_no_url"), L10n.get("msa.warn_title"), 2);
            return;
        }
        this.setBusy(true);
        new Thread(() -> {
            try {
                MinecraftSession minecraftSession;
                this.result = minecraftSession = MicrosoftAuth.completeLogin(this.request, string, msg -> SwingUtilities.invokeLater(() -> this.statusLabel.setText((String)msg)));
                SwingUtilities.invokeLater(() -> {
                    minecraftSession.saveToDisk();
                    this.dispose();
                });
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> {
                    this.setBusy(false);
                    JOptionPane.showMessageDialog(this, L10n.get("msa.err_login_failed") + exception.getMessage(), L10n.get("msa.err_title"), 0);
                });
            }
        }).start();
    }

    private void setBusy(boolean bl) {
        this.spinner.setVisible(bl);
        this.continueBtn.setEnabled(!bl);
        this.urlField.setEnabled(!bl);
        this.browserBtn.setEnabled(!bl);
        if (bl) {
            this.statusLabel.setText(L10n.get("msa.completing"));
        }
    }
}
