package com.railway.gui.panels;

import com.railway.config.SessionContext;
import com.railway.gui.UITheme;
import com.railway.model.Reservation;
import com.railway.model.ReservationPassenger;
import com.railway.model.enums.BookingStatus;
import com.railway.service.ReservationService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Enhanced Personal Bookings History panel.
 * Features PNR search, status filtering, master-detail passenger layout breakdown,
 * and cancellation confirmations with real-time seat releases.
 */
public class MyBookingsPanel extends JPanel {

    private final ReservationService reservationService;
    private final Consumer<Reservation> onViewTicket;

    private JTextField txtSearchPnr;
    private JComboBox<String> cmbStatusFilter;
    private JTable tblBookings;
    private DefaultTableModel bookingTableModel;

    private JTable tblPassengers;
    private DefaultTableModel passengerTableModel;

    private JButton btnViewTicket;
    private JButton btnCancelBooking;
    private JLabel lblStatus;

    private List<Reservation> allUserBookings = new ArrayList<>();
    private List<Reservation> displayedBookings = new ArrayList<>();

    public MyBookingsPanel(ReservationService reservationService, Consumer<Reservation> onViewTicket) {
        this.reservationService = reservationService;
        this.onViewTicket = onViewTicket;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // 1. Top Search and Filter Card
        JPanel topCard = UITheme.createCardPanel();
        topCard.setLayout(new GridBagLayout());
        topCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = UITheme.createHeaderLabel("My Reservation History");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        topCard.add(lblTitle, gbc);

        // Search by PNR
        JLabel lblSearch = new JLabel("Search PNR:");
        lblSearch.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        topCard.add(lblSearch, gbc);

        txtSearchPnr = new JTextField(12);
        txtSearchPnr.setFont(UITheme.FONT_BODY);
        txtSearchPnr.addActionListener(e -> applyFilters());
        gbc.gridx = 1; gbc.gridy = 1;
        topCard.add(txtSearchPnr, gbc);

        // Filter by Status
        JLabel lblFilter = new JLabel("Status:");
        lblFilter.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 2; gbc.gridy = 1;
        topCard.add(lblFilter, gbc);

        cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "CONFIRMED", "CANCELLED"});
        cmbStatusFilter.setFont(UITheme.FONT_BODY);
        cmbStatusFilter.addActionListener(e -> applyFilters());
        gbc.gridx = 3; gbc.gridy = 1;
        topCard.add(cmbStatusFilter, gbc);

        JButton btnApply = UITheme.createPrimaryButton("Filter");
        btnApply.addActionListener(e -> applyFilters());
        gbc.gridx = 4; gbc.gridy = 1;
        topCard.add(btnApply, gbc);

        JButton btnRefresh = new JButton("Reload");
        btnRefresh.setFont(UITheme.FONT_BODY);
        btnRefresh.addActionListener(e -> refreshBookings());
        gbc.gridx = 5; gbc.gridy = 1;
        topCard.add(btnRefresh, gbc);

        add(topCard, BorderLayout.NORTH);

        // 2. Master-Detail Split: Top Bookings Table, Bottom Passenger Breakdown
        String[] bookCols = {"PNR Number", "Train No & Name", "Journey Date", "Route", "Class", "Passengers", "Total Fare", "Status"};
        bookingTableModel = new DefaultTableModel(bookCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblBookings = new JTable(bookingTableModel);
        tblBookings.setFont(UITheme.FONT_BODY);
        tblBookings.setRowHeight(26);
        tblBookings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBookings.getSelectionModel().addListSelectionListener(e -> onBookingSelected());

        JScrollPane bookScroll = new JScrollPane(tblBookings);
        bookScroll.setBorder(BorderFactory.createTitledBorder("Master Bookings"));

        // Bottom Detail: Passengers in Selected Booking
        String[] passCols = {"S.No", "Passenger Name", "Age", "Gender", "Assigned Coach", "Seat Number", "Berth Type"};
        passengerTableModel = new DefaultTableModel(passCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblPassengers = new JTable(passengerTableModel);
        tblPassengers.setFont(UITheme.FONT_BODY);
        tblPassengers.setRowHeight(24);
        JScrollPane passScroll = new JScrollPane(tblPassengers);
        passScroll.setBorder(BorderFactory.createTitledBorder("Passengers & Allocated Coach/Seats for Selected Booking"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bookScroll, passScroll);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        lblStatus = new JLabel("Select a reservation to view allocated seats or request cancellation.");
        lblStatus.setFont(UITheme.FONT_BODY);
        bottomBar.add(lblStatus, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

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
        lblStatus.setText("Loading personal reservations...");
        bookingTableModel.setRowCount(0);
        passengerTableModel.setRowCount(0);
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
                    allUserBookings = get();
                    applyFilters();
                } catch (Exception ex) {
                    lblStatus.setText("Error loading reservations: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void applyFilters() {
        if (allUserBookings == null) return;

        String searchPnr = txtSearchPnr.getText().trim().toUpperCase();
        String selectedStatus = (String) cmbStatusFilter.getSelectedItem();

        displayedBookings = allUserBookings.stream()
                .filter(r -> searchPnr.isEmpty() || r.getPnrNumber().toUpperCase().contains(searchPnr))
                .filter(r -> "ALL".equals(selectedStatus) || r.getStatus().name().equalsIgnoreCase(selectedStatus))
                .collect(Collectors.toList());

        bookingTableModel.setRowCount(0);
        passengerTableModel.setRowCount(0);

        for (Reservation r : displayedBookings) {
            String trainStr = r.getTrain() != null ? r.getTrain().getTrainNumber() + " - " + r.getTrain().getTrainName() : "ID: " + r.getTrainId();
            String routeStr = (r.getFromStation() != null ? r.getFromStation().getStationCode() : "") + " -> " +
                              (r.getToStation() != null ? r.getToStation().getStationCode() : "");

            bookingTableModel.addRow(new Object[]{
                    r.getPnrNumber(),
                    trainStr,
                    r.getJourneyDate().toString(),
                    routeStr,
                    r.getTravelClass().getCode(),
                    r.getTotalPassengers(),
                    "INR " + r.getTotalFare(),
                    r.getStatus().name()
            });
        }

        lblStatus.setText(String.format("Showing %d of %d total reservation(s).", displayedBookings.size(), allUserBookings.size()));
        btnViewTicket.setEnabled(false);
        btnCancelBooking.setEnabled(false);
    }

    private void onBookingSelected() {
        Reservation res = getSelectedReservation();
        boolean hasSelection = res != null;
        btnViewTicket.setEnabled(hasSelection);
        btnCancelBooking.setEnabled(hasSelection && res.isCancellable());

        passengerTableModel.setRowCount(0);
        if (res != null && res.getPassengers() != null) {
            int sNo = 1;
            for (ReservationPassenger p : res.getPassengers()) {
                passengerTableModel.addRow(new Object[]{
                        sNo++,
                        p.getPassengerName(),
                        p.getAge(),
                        p.getGender().name(),
                        p.getAssignedCoach(),
                        p.getAssignedSeatNumber(),
                        p.getAssignedBerth().name()
                });
            }
        }
    }

    private Reservation getSelectedReservation() {
        int row = tblBookings.getSelectedRow();
        if (row >= 0 && displayedBookings != null && row < displayedBookings.size()) {
            return displayedBookings.get(row);
        }
        return null;
    }

    private void performCancellation() {
        Reservation selected = getSelectedReservation();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("Are you sure you want to cancel booking PNR %s?\nAllocated seats will be released immediately.", selected.getPnrNumber()),
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            long userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getId() : 1L;
            lblStatus.setText("Cancelling reservation and releasing inventory...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return reservationService.cancelReservation(selected.getPnrNumber(), userId);
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        get();
                        JOptionPane.showMessageDialog(MyBookingsPanel.this,
                                "Reservation PNR " + selected.getPnrNumber() + " successfully cancelled.\nSeats have been released back to general inventory.",
                                "Cancellation Confirmed",
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
