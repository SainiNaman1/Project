package com.railway.gui;

import com.railway.exception.ValidationException;
import com.railway.service.AuthService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Passenger account registration dialog.
 * Features a spacious 2-column responsive layout preventing field clipping on high-DPI displays.
 * Role is strictly locked to PASSENGER.
 */
public class RegisterFrame extends JDialog {

    private final AuthService authService;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;

    public RegisterFrame(JFrame parent, AuthService authService) {
        super(parent, "Passenger Registration", true);
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setTitle("Passenger Registration — Indian Railways");
        setSize(600, 560);
        setMinimumSize(new Dimension(540, 500));
        setLocationRelativeTo(getParent());
        setResizable(true);
        setLayout(new BorderLayout());

        // 1. Top Header Banner
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UITheme.PRIMARY_NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel lblTitle = new JLabel("Create Passenger Account");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        headerPanel.add(Box.createVerticalStrut(4));

        JLabel lblSubtitle = new JLabel("Enter your personal details and desired credentials to register.");
        lblSubtitle.setFont(UITheme.FONT_BODY);
        lblSubtitle.setForeground(new Color(203, 213, 225));
        headerPanel.add(lblSubtitle);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Central Form Panel (2-Column GridBagLayout)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 4, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        // --- Row 0: Section Header: Personal Details ---
        JLabel lblSec1 = new JLabel("Personal Information");
        lblSec1.setFont(UITheme.FONT_HEADER);
        lblSec1.setForeground(UITheme.PRIMARY_NAVY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 10, 8, 10);
        formPanel.add(lblSec1, gbc);

        // --- Row 1: Full Name (col 0) & Email Address (col 1) Labels ---
        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 10, 2, 10);

        JLabel lblFullName = new JLabel("Full Name");
        lblFullName.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblFullName, gbc);

        JLabel lblEmail = new JLabel("Email Address");
        lblEmail.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(lblEmail, gbc);

        // --- Row 2: Full Name & Email Fields ---
        gbc.insets = new Insets(2, 10, 12, 10);

        txtFullName = new JTextField();
        txtFullName.setFont(UITheme.FONT_BODY);
        txtFullName.setPreferredSize(new Dimension(220, 34));
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(txtFullName, gbc);

        txtEmail = new JTextField();
        txtEmail.setFont(UITheme.FONT_BODY);
        txtEmail.setPreferredSize(new Dimension(220, 34));
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(txtEmail, gbc);

        // --- Row 3: Mobile Number (col 0) & Desired Username (col 1) Labels ---
        gbc.insets = new Insets(4, 10, 2, 10);

        JLabel lblPhone = new JLabel("10-Digit Mobile Number");
        lblPhone.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(lblPhone, gbc);

        JLabel lblUser = new JLabel("Desired Username");
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(lblUser, gbc);

        // --- Row 4: Mobile & Username Fields ---
        gbc.insets = new Insets(2, 10, 16, 10);

        txtPhone = new JTextField();
        txtPhone.setFont(UITheme.FONT_BODY);
        txtPhone.setPreferredSize(new Dimension(220, 34));
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(txtPhone, gbc);

        txtUsername = new JTextField();
        txtUsername.setFont(UITheme.FONT_BODY);
        txtUsername.setPreferredSize(new Dimension(220, 34));
        gbc.gridx = 1; gbc.gridy = 4;
        formPanel.add(txtUsername, gbc);

        // --- Row 5: Section Header: Security & Password ---
        JLabel lblSec2 = new JLabel("Account Credentials");
        lblSec2.setFont(UITheme.FONT_HEADER);
        lblSec2.setForeground(UITheme.PRIMARY_NAVY);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 10, 8, 10);
        formPanel.add(lblSec2, gbc);

        // --- Row 6: Password (col 0) & Confirm Password (col 1) Labels ---
        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 10, 2, 10);

        JLabel lblPass = new JLabel("Password (min 6 characters)");
        lblPass.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(lblPass, gbc);

        JLabel lblConfirm = new JLabel("Confirm Password");
        lblConfirm.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 1; gbc.gridy = 6;
        formPanel.add(lblConfirm, gbc);

        // --- Row 7: Password & Confirm Password Fields ---
        gbc.insets = new Insets(2, 10, 14, 10);

        txtPassword = new JPasswordField();
        txtPassword.setFont(UITheme.FONT_BODY);
        txtPassword.setPreferredSize(new Dimension(220, 34));
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(txtPassword, gbc);

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setFont(UITheme.FONT_BODY);
        txtConfirmPassword.setPreferredSize(new Dimension(220, 34));
        gbc.gridx = 1; gbc.gridy = 7;
        formPanel.add(txtConfirmPassword, gbc);

        // Wrap formPanel in a JScrollPane so it never clips regardless of screen DPI
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(new Color(248, 250, 252));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        JLabel lblNote = new JLabel("All fields are mandatory.");
        lblNote.setFont(UITheme.FONT_SMALL);
        lblNote.setForeground(new Color(100, 116, 139));
        actionPanel.add(lblNote, BorderLayout.WEST);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtons.setOpaque(false);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(UITheme.FONT_BODY);
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = UITheme.createPrimaryButton("Register Account");
        btnSubmit.setPreferredSize(new Dimension(150, 36));
        btnSubmit.addActionListener(e -> performRegistration());

        rightButtons.add(btnCancel);
        rightButtons.add(btnSubmit);
        actionPanel.add(rightButtons, BorderLayout.EAST);

        add(actionPanel, BorderLayout.SOUTH);
    }

    private void performRegistration() {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        try {
            authService.register(fullName, email, phone, username, password, confirm);
            JOptionPane.showMessageDialog(this,
                    "Registration successful!\nYou may now sign in using username: " + username,
                    "Registration Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
