package com.railway.gui;

import com.railway.config.AppConfig;
import com.railway.config.ApplicationContext;
import com.railway.exception.AuthenticationException;
import com.railway.model.User;
import com.railway.model.enums.Role;
import com.railway.service.AuthService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

public class LoginFrame extends JFrame {

    private final AuthService authService;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnTogglePassword;
    private JLabel lblInlineError;
    private JButton btnLogin;
    private JButton btnRegister;
    private boolean passwordVisible = false;

    public LoginFrame() {
        this(ApplicationContext.getInstance().getAuthService());
    }

    public LoginFrame(AuthService authService) {
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setTitle("Railway Management System — Account Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        JLabel lblTitle = new JLabel("INDIAN RAILWAYS SIMULATION", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Passenger Portal & Operational Console", SwingConstants.CENTER);
        lblSubtitle.setFont(UITheme.FONT_BODY);
        lblSubtitle.setForeground(new Color(203, 213, 225));

        headerPanel.add(lblTitle, BorderLayout.CENTER);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(new Color(241, 245, 249));

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(400, 390));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblCardTitle = new JLabel("Sign In to Your Account");
        lblCardTitle.setFont(UITheme.FONT_HEADER);
        lblCardTitle.setForeground(UITheme.TEXT_DARK);
        gbc.gridy = 0;
        card.add(lblCardTitle, gbc);

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridy = 1;
        card.add(lblUser, gbc);

        txtUsername = new JTextField(20);
        txtUsername.setFont(UITheme.FONT_BODY);
        txtUsername.setPreferredSize(new Dimension(320, 34));
        gbc.gridy = 2;
        card.add(txtUsername, gbc);

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridy = 3;
        card.add(lblPass, gbc);

        JPanel passwordRow = new JPanel(new BorderLayout(4, 0));
        passwordRow.setOpaque(false);

        txtPassword = new JPasswordField(16);
        txtPassword.setFont(UITheme.FONT_BODY);
        txtPassword.setPreferredSize(new Dimension(250, 34));
        passwordRow.add(txtPassword, BorderLayout.CENTER);

        btnTogglePassword = new JButton("Show");
        btnTogglePassword.setFont(UITheme.FONT_SMALL);
        btnTogglePassword.setPreferredSize(new Dimension(65, 34));
        btnTogglePassword.setFocusPainted(false);
        btnTogglePassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTogglePassword.addActionListener(e -> togglePasswordVisibility());
        passwordRow.add(btnTogglePassword, BorderLayout.EAST);

        gbc.gridy = 4;
        card.add(passwordRow, gbc);

        lblInlineError = new JLabel(" ");
        lblInlineError.setFont(UITheme.FONT_SMALL);
        lblInlineError.setForeground(UITheme.ACCENT_RED);
        gbc.gridy = 5;
        card.add(lblInlineError, gbc);

        btnLogin = UITheme.createPrimaryButton("Sign In");
        btnLogin.setPreferredSize(new Dimension(320, 38));
        gbc.gridy = 6;
        card.add(btnLogin, gbc);

        if (AppConfig.isDemoAutofillEnabled()) {
            JPanel autofillPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
            autofillPanel.setOpaque(false);
            JLabel lblQuick = new JLabel("Demo fill:");
            lblQuick.setFont(UITheme.FONT_SMALL);
            lblQuick.setForeground(new Color(100, 116, 139));

            JButton btnAdmin = new JButton("Admin");
            btnAdmin.setFont(UITheme.FONT_SMALL);
            btnAdmin.addActionListener(e -> {
                txtUsername.setText("admin");
                txtPassword.setText("Admin@123");
            });

            JButton btnPax = new JButton("Passenger");
            btnPax.setFont(UITheme.FONT_SMALL);
            btnPax.addActionListener(e -> {
                txtUsername.setText("rahul");
                txtPassword.setText("Rahul@123");
            });

            autofillPanel.add(lblQuick);
            autofillPanel.add(btnAdmin);
            autofillPanel.add(btnPax);
            gbc.gridy = 7;
            card.add(autofillPanel, gbc);
        }

        btnRegister = new JButton("Create New Passenger Account");
        btnRegister.setFont(UITheme.FONT_BODY);
        btnRegister.setFocusPainted(false);
        btnRegister.setPreferredSize(new Dimension(320, 34));
        gbc.gridy = 8;
        card.add(btnRegister, gbc);

        centerContainer.add(card);
        add(centerContainer, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> performAsyncLogin());
        txtPassword.addActionListener(e -> performAsyncLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());

        btnRegister.addActionListener(e -> {
            RegisterFrame regFrame = new RegisterFrame(this, authService);
            regFrame.setVisible(true);
        });
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            txtPassword.setEchoChar((char) 0);
            btnTogglePassword.setText("Hide");
        } else {
            txtPassword.setEchoChar('•');
            btnTogglePassword.setText("Show");
        }
    }

    private void performAsyncLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty()) {
            showInlineError("Please enter your username.");
            txtUsername.requestFocusInWindow();
            return;
        }
        if (password.isEmpty()) {
            showInlineError("Please enter your password.");
            txtPassword.requestFocusInWindow();
            return;
        }

        setLoadingState(true);

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    setLoadingState(false);
                    lblInlineError.setForeground(UITheme.ACCENT_GREEN);
                    lblInlineError.setText("Login successful! Redirecting...");

                    dispose();
                    if (user.getRole() == Role.ADMIN) {
                        new AdminDashboard().setVisible(true);
                    } else {
                        new PassengerDashboard().setVisible(true);
                    }
                } catch (Exception ex) {
                    setLoadingState(false);
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    showInlineError(cause.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void showInlineError(String message) {
        lblInlineError.setForeground(UITheme.ACCENT_RED);
        lblInlineError.setText(message != null ? message : "Authentication failed.");
    }

    private void setLoadingState(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnRegister.setEnabled(!loading);
        txtUsername.setEnabled(!loading);
        txtPassword.setEnabled(!loading);
        if (loading) {
            lblInlineError.setForeground(new Color(15, 23, 42));
            lblInlineError.setText("Connecting to database & verifying credentials...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}
