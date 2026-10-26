package com.railway.gui;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Centralized UI theme and styling helper for the Swing presentation layer.
 * Configures the modern FlatLaf Look and Feel with Indian Railways Navy aesthetic.
 */
public final class UITheme {

    public static final Color PRIMARY_NAVY = new Color(20, 50, 95);
    public static final Color ACCENT_BLUE = new Color(30, 90, 160);
    public static final Color ACCENT_GREEN = new Color(40, 140, 70);
    public static final Color ACCENT_RED = new Color(180, 40, 40);
    public static final Color CARD_BG = new Color(248, 250, 252);
    public static final Color TEXT_DARK = new Color(30, 41, 59);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private UITheme() {
    }

    public static void setupLookAndFeel() {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Table.rowHeight", 26);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
        } catch (Exception e) {
            System.err.println("[UITheme] FlatLaf could not be initialized. Using system default: " + e.getMessage());
        }
    }

    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(PRIMARY_NAVY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return button;
    }

    public static JButton createSuccessButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(ACCENT_GREEN);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return button;
    }

    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(ACCENT_RED);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return button;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        Border line = BorderFactory.createLineBorder(new Color(226, 232, 240), 1);
        Border padding = BorderFactory.createEmptyBorder(16, 16, 16, 16);
        panel.setBorder(BorderFactory.createCompoundBorder(line, padding));
        return panel;
    }

    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(PRIMARY_NAVY);
        return label;
    }
}
