package com.railway.gui.panels;

import com.railway.config.SessionContext;
import com.railway.gui.UITheme;
import com.railway.model.Reservation;
import com.railway.model.enums.BookingStatus;
import com.railway.service.ReservationService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Displays the authenticated passenger's personal bookings history,
 * allowing ticket viewing, printing, and eligible cancellations.
 */
public class MyBookingsPanel extends JPanel {

    private final ReservationService reservationService;
    private final Consumer<Reservation> onViewTicket;

    private JTable tblBookings;
    private DefaultTableModel tableModel;
    private JButton btnViewTicket;
    private JButton btnCancelBooking;
    private JLabel lblStatus;
    private List<Reservation> currentBookings;

    public MyBookingsPanel(ReservationService reservationService, Consumer<Reservation> onViewTicket) {
        this.reservationService = reservationService;
        this.onViewTicket = onViewTicket;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(248, 250, 252));

        // Top Header
        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new BorderLayout());

        JLabel lblTitle = UITheme.createHeaderLabel("My Reservation History");
        topBar.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(UITheme.FONT_BODY);
        btnRefresh.addActionListener(e -> refreshBookings());
        topBar.add(btnRefresh, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"PNR Number", "Train No & Name", "Journey Date", "From -> To", "Class", "Passengers", "Total Fare", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblBookings = new JTable(tableModel);
        tblBookings.setFont(UITheme.FONT_BODY);
        tblBookings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBookings.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        add(new JScrollPane(tblBookings), BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(Color.WHITE);

        lblStatus = new JLabel(" Select a booking to view details or cancel.");
        lblStatus.setFont(UITheme.FONT_BODY);
        bottomBar.add(lblStatus, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        rightActions.setBackground(Color.WHITE);

        btnViewTicket = UITheme.createPrimaryButton("View / Print Ticket");
        btnViewTicket.setEnabled(false);
        btnViewTicket.addActionListener(e -> {
            Reservation selected = getSelectedReservation();
            if (selected != null) {
                onViewTicket.accept(selected);
            }
        });

        btnCancelBooking = UITheme.createDangerButton("Cancel Booking");
        btnCancelBooking.setEnabled(false);
        btnCancelBooking.addActionListener(e -> performCancellation());

        rightActions.add(btnViewTicket);
        rightActions.add(btnCancelBooking);
        bottomBar.add(rightActions, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void refreshBookings() {
        long userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getId() : 1L;
        lblStatus.setText("Loading your bookings...");
        tableModel.setRowCount(0);
        btnViewTicket.setEnabled(false);
        btnCancelBooking.setEnabled(false);

        new SwingWorker<List<Reservation>, Void>() {
            @Override
            protected List<Reservation> doInBackground() throws Exception {
                return reservationService.getUserReservations(userId);
            }

            @Override
            protected void done() {
                try {
                    currentBookings = get();
                    if (currentBookings.isEmpty()) {
                        lblStatus.setText("No bookings found in your account.");
                    } else {
                        lblStatus.setText("Loaded " + currentBookings.size() + " reservation(s).");
                        for (Reservation r : currentBookings) {
                            String trainStr = r.getTrain() != null ? r.getTrain().getTrainNumber() + " - " + r.getTrain().getTrainName() : "Train #" + r.getTrainId();
                            String routeStr = r.getFromStation().getStationCode() + " -> " + r.getToStation().getStationCode();

                            tableModel.addRow(new Object[]{
                                    r.getPnrNumber(),
                                    trainStr,
                                    r.getJourneyDate().toString(),
                                    routeStr,
                                    r.getTravelClass().getCode(),
                                    r.getTotalPassengers(),
                                    "INR " + r.getTotalFare(),
                                    r.getStatus().getDescription()
                            });
                        }
                    }
                } catch (Exception ex) {
                    lblStatus.setText("Error loading bookings: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateButtonStates() {
        Reservation selected = getSelectedReservation();
        boolean hasSelection = selected != null;
        btnViewTicket.setEnabled(hasSelection);
        btnCancelBooking.setEnabled(hasSelection && selected.isCancellable());
    }

    private Reservation getSelectedReservation() {
        int row = tblBookings.getSelectedRow();
        if (row >= 0 && currentBookings != null && row < currentBookings.size()) {
            return currentBookings.get(row);
        }
        return null;
    }

    private void performCancellation() {
        Reservation selected = getSelectedReservation();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel PNR " + selected.getPnrNumber() + "?\nThis action cannot be undone.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            long userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getId() : 1L;
            lblStatus.setText("Cancelling reservation...");

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return reservationService.cancelReservation(selected.getPnrNumber(), userId);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(MyBookingsPanel.this,
                                "Reservation PNR " + selected.getPnrNumber() + " successfully cancelled.\nSeats have been released.",
                                "Cancelled",
                                JOptionPane.INFORMATION_MESSAGE);
                        refreshBookings();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(MyBookingsPanel.this,
                                "Cancellation Failed: " + cause.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        refreshBookings();
                    }
                }
            }.execute();
        }
    }
}
