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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Passenger account registration dialog.
 * Crucial security constraint: Role is locked to PASSENGER; no admin selection permitted.
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
        setSize(450, 520);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UITheme.PRIMARY_NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JLabel lblTitle = new JLabel("Create Passenger Account");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // Center Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        txtFullName = addFormField(formPanel, "Full Name", 0, gbc);
        txtEmail = addFormField(formPanel, "Email Address", 2, gbc);
        txtPhone = addFormField(formPanel, "10-Digit Mobile Number", 4, gbc);
        txtUsername = addFormField(formPanel, "Desired Username", 6, gbc);

        // Passwords
        JLabel lblPass = new JLabel("Password (min 6 characters)");
        lblPass.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridy = 8;
        formPanel.add(lblPass, gbc);

        txtPassword = new JPasswordField(20);
        txtPassword.setFont(UITheme.FONT_BODY);
        gbc.gridy = 9;
        formPanel.add(txtPassword, gbc);

        JLabel lblConfirm = new JLabel("Confirm Password");
        lblConfirm.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridy = 10;
        formPanel.add(lblConfirm, gbc);

        txtConfirmPassword = new JPasswordField(20);
        txtConfirmPassword.setFont(UITheme.FONT_BODY);
        gbc.gridy = 11;
        formPanel.add(txtConfirmPassword, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Bottom Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        actionPanel.setBackground(new Color(241, 245, 249));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(UITheme.FONT_BODY);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = UITheme.createPrimaryButton("Register Account");
        btnSubmit.addActionListener(e -> performRegistration());

        actionPanel.add(btnCancel);
        actionPanel.add(btnSubmit);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private JTextField addFormField(JPanel panel, String labelText, int gridY, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridy = gridY;
        panel.add(label, gbc);

        JTextField field = new JTextField(20);
        field.setFont(UITheme.FONT_BODY);
        gbc.gridy = gridY + 1;
        panel.add(field, gbc);
        return field;
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
                    "Registration successful! You may now sign in with username: " + username,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
