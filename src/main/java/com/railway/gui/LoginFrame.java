package com.railway.gui;

import com.railway.exception.AuthenticationException;
import com.railway.model.User;
import com.railway.model.enums.Role;
import com.railway.service.AuthService;
import com.railway.service.impl.AuthServiceImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Main application authentication window.
 * Routes authenticated passengers to PassengerDashboard and administrators to AdminDashboard.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblStatus;

    public LoginFrame() {
        this.authService = new AuthServiceImpl();
        initUI();
    }

    private void initUI() {
        setTitle("Railway Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header Banner
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UITheme.PRIMARY_NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));
        headerPanel.setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("INDIAN RAILWAYS SIMULATION", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Educational Desktop Passenger Portal", SwingConstants.CENTER);
        lblSubtitle.setFont(UITheme.FONT_BODY);
        lblSubtitle.setForeground(new Color(203, 213, 225));

        headerPanel.add(lblTitle, BorderLayout.CENTER);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Center Login Card
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(new Color(241, 245, 249));

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(420, 410));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Card Title
        JLabel lblLoginHeader = new JLabel("Account Sign In");
        lblLoginHeader.setFont(UITheme.FONT_HEADER);
        lblLoginHeader.setForeground(UITheme.TEXT_DARK);
        gbc.gridy = 0;
        card.add(lblLoginHeader, gbc);

        // Username Field
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridy = 1;
        card.add(lblUser, gbc);

        txtUsername = new JTextField("admin", 20);
        txtUsername.setFont(UITheme.FONT_BODY);
        gbc.gridy = 2;
        card.add(txtUsername, gbc);

        // Password Field
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridy = 3;
        card.add(lblPass, gbc);

        txtPassword = new JPasswordField("Admin@123", 20);
        txtPassword.setFont(UITheme.FONT_BODY);
        gbc.gridy = 4;
        card.add(txtPassword, gbc);

        // Status Error Label
        lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.ACCENT_RED);
        gbc.gridy = 5;
        card.add(lblStatus, gbc);

        // Quick Autofill Demo Bar
        JPanel autofillPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        autofillPanel.setOpaque(false);
        JLabel lblHint = new JLabel("Quick Autofill:");
        lblHint.setFont(UITheme.FONT_SMALL);
        lblHint.setForeground(new Color(100, 116, 139));

        JButton btnFillAdmin = new JButton("Admin");
        btnFillAdmin.setFont(UITheme.FONT_SMALL);
        btnFillAdmin.setFocusPainted(false);
        btnFillAdmin.addActionListener(e -> {
            txtUsername.setText("admin");
            txtPassword.setText("Admin@123");
            lblStatus.setText("Filled Admin credentials (admin / Admin@123)");
        });

        JButton btnFillPassenger = new JButton("Passenger");
        btnFillPassenger.setFont(UITheme.FONT_SMALL);
        btnFillPassenger.setFocusPainted(false);
        btnFillPassenger.addActionListener(e -> {
            txtUsername.setText("rahul");
            txtPassword.setText("Rahul@123");
            lblStatus.setText("Filled Passenger credentials (rahul / Rahul@123)");
        });

        autofillPanel.add(lblHint);
        autofillPanel.add(btnFillAdmin);
        autofillPanel.add(btnFillPassenger);
        gbc.gridy = 6;
        card.add(autofillPanel, gbc);

        // Login Button
        JButton btnLogin = UITheme.createPrimaryButton("Sign In");
        gbc.gridy = 7;
        card.add(btnLogin, gbc);

        // Register Button
        JButton btnRegister = new JButton("Create New Passenger Account");
        btnRegister.setFont(UITheme.FONT_BODY);
        btnRegister.setFocusPainted(false);
        gbc.gridy = 8;
        card.add(btnRegister, gbc);

        centerContainer.add(card);
        add(centerContainer, BorderLayout.CENTER);

        // Event Listeners
        btnLogin.addActionListener(e -> performLogin());
        txtPassword.addActionListener(e -> performLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());

        btnRegister.addActionListener(e -> {
            RegisterFrame regFrame = new RegisterFrame(this, authService);
            regFrame.setVisible(true);
        });
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please enter both username and password.");
            return;
        }

        lblStatus.setText("Authenticating credentials...");
        SwingUtilities.invokeLater(() -> {
            try {
                User user = authService.login(username, password);
                lblStatus.setText("Login successful!");

                dispose(); // Close login window
                if (user.getRole() == Role.ADMIN) {
                    new AdminDashboard().setVisible(true);
                } else {
                    new PassengerDashboard().setVisible(true);
                }
            } catch (AuthenticationException ex) {
                lblStatus.setText(ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        ex.getMessage() + "\n\nTip: You can use the Quick Autofill buttons above,\n" +
                        "or click 'Create New Passenger Account' below.",
                        "Authentication Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
