package com.railway.gui.panels;

import com.railway.gui.UITheme;
import com.railway.model.Reservation;
import com.railway.model.dto.TicketDTO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Renders an official ASCII/document-style journey ticket preview,
 * supporting local printing via Java PrinterJob.
 */
public class TicketPanel extends JPanel {

    private final Runnable onBackAction;
    private JTextArea txtTicketContent;
    private JButton btnPrint;
    private Reservation currentReservation;

    public TicketPanel(Runnable onBackAction) {
        this.onBackAction = onBackAction;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(248, 250, 252));

        // Top Navigation Bar
        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new BorderLayout());

        JButton btnBack = new JButton("<< Back to Bookings");
        btnBack.setFont(UITheme.FONT_BODY_BOLD);
        btnBack.addActionListener(e -> {
            if (onBackAction != null) onBackAction.run();
        });
        topBar.add(btnBack, BorderLayout.WEST);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setBackground(Color.WHITE);

        btnPrint = UITheme.createPrimaryButton("Print Ticket");
        btnPrint.addActionListener(e -> printTicket());
        rightButtons.add(btnPrint);

        topBar.add(rightButtons, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center: Formatted Ticket Text Area
        txtTicketContent = new JTextArea();
        txtTicketContent.setEditable(false);
        txtTicketContent.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtTicketContent.setBackground(new Color(254, 254, 254));
        txtTicketContent.setForeground(UITheme.TEXT_DARK);
        txtTicketContent.setMargin(new java.awt.Insets(16, 24, 16, 24));

        JScrollPane scrollPane = new JScrollPane(txtTicketContent);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void displayReservation(Reservation reservation) {
        this.currentReservation = reservation;
        if (reservation != null) {
            TicketDTO dto = new TicketDTO(reservation);
            txtTicketContent.setText(dto.generatePrintableText());
            txtTicketContent.setCaretPosition(0);
            btnPrint.setEnabled(true);
        } else {
            txtTicketContent.setText("No ticket selected for preview.");
            btnPrint.setEnabled(false);
        }
    }

    private void printTicket() {
        if (currentReservation == null) return;

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(txtTicketContent.getPrintable(null, null));

        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, "Ticket sent to printer successfully.", "Print Completed", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
