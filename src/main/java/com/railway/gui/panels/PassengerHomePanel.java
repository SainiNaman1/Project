package com.railway.gui.panels;

import com.railway.config.SessionContext;
import com.railway.gui.UITheme;
import com.railway.model.Reservation;
import com.railway.model.User;
import com.railway.model.enums.BookingStatus;
import com.railway.service.ReservationService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class PassengerHomePanel extends JPanel {

    private final ReservationService reservationService;
    private final Runnable onSearchTrains;
    private final Runnable onBookTicket;
    private final Runnable onMyBookings;
    private final Consumer<Reservation> onViewTicket;

    private JLabel lblWelcome;
    private JLabel lblTotalBookings;
    private JLabel lblConfirmedBookings;
    private JLabel lblUpcomingDetails;
    private JButton btnViewUpcoming;
    private Reservation nextUpcomingReservation;

    private DefaultTableModel activityTableModel;
    private JTable tblActivity;
    private List<Reservation> cachedReservations;

    public PassengerHomePanel(ReservationService reservationService, Runnable onSearchTrains,
                              Runnable onBookTicket, Runnable onMyBookings, Consumer<Reservation> onViewTicket) {
        this.reservationService = reservationService;
        this.onSearchTrains = onSearchTrains;
        this.onBookTicket = onBookTicket;
        this.onMyBookings = onMyBookings;
        this.onViewTicket = onViewTicket;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel welcomeCard = UITheme.createCardPanel();
        welcomeCard.setLayout(new BorderLayout(12, 12));
        welcomeCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        User user = SessionContext.getCurrentUser();
        String name = user != null ? user.getFullName() : "Passenger";
        String email = user != null ? user.getEmail() : "";

        lblWelcome = new JLabel(String.format("Welcome back, %s!", name));
        lblWelcome.setFont(UITheme.FONT_TITLE);
        lblWelcome.setForeground(UITheme.PRIMARY_NAVY);

        JLabel lblSub = new JLabel(String.format("Account: %s | Explore train routes, book tickets, and manage reservations seamlessly.", email));
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(new Color(100, 116, 139));

        JPanel welcomeText = new JPanel(new GridLayout(2, 1, 0, 4));
        welcomeText.setOpaque(false);
        welcomeText.add(lblWelcome);
        welcomeText.add(lblSub);
        welcomeCard.add(welcomeText, BorderLayout.WEST);

        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        quickActions.setOpaque(false);

        JButton btnQuickSearch = UITheme.createPrimaryButton("Search Trains");
        btnQuickSearch.addActionListener(e -> onSearchTrains.run());

        JButton btnQuickBook = UITheme.createSuccessButton("+ Book Ticket");
        btnQuickBook.addActionListener(e -> onBookTicket.run());

        quickActions.add(btnQuickSearch);
        quickActions.add(btnQuickBook);
        welcomeCard.add(quickActions, BorderLayout.EAST);

        JPanel middleRow = new JPanel(new GridLayout(1, 3, 16, 16));
        middleRow.setOpaque(false);

        JPanel cardTotal = createStatCard("Total Reservations", "0", new Color(30, 41, 59));
        lblTotalBookings = (JLabel) cardTotal.getClientProperty("valueLabel");

        JPanel cardConfirmed = createStatCard("Confirmed Bookings", "0", UITheme.ACCENT_GREEN);
        lblConfirmedBookings = (JLabel) cardConfirmed.getClientProperty("valueLabel");

        JPanel cardUpcoming = UITheme.createCardPanel();
        cardUpcoming.setLayout(new BorderLayout(8, 8));
        cardUpcoming.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel lblUpcomingTitle = new JLabel("Next Upcoming Journey");
        lblUpcomingTitle.setFont(UITheme.FONT_BODY_BOLD);
        lblUpcomingTitle.setForeground(UITheme.PRIMARY_NAVY);
        cardUpcoming.add(lblUpcomingTitle, BorderLayout.NORTH);

        lblUpcomingDetails = new JLabel("<html><body style='color:#64748B;'>No upcoming journeys scheduled.</body></html>");
        lblUpcomingDetails.setFont(UITheme.FONT_BODY);
        cardUpcoming.add(lblUpcomingDetails, BorderLayout.CENTER);

        btnViewUpcoming = new JButton("View Ticket");
        btnViewUpcoming.setFont(UITheme.FONT_SMALL);
        btnViewUpcoming.setVisible(false);
        btnViewUpcoming.addActionListener(e -> {
            if (nextUpcomingReservation != null) {
                onViewTicket.accept(nextUpcomingReservation);
            }
        });
        cardUpcoming.add(btnViewUpcoming, BorderLayout.SOUTH);

        middleRow.add(cardTotal);
        middleRow.add(cardConfirmed);
        middleRow.add(cardUpcoming);

        JPanel topContainer = new JPanel(new BorderLayout(0, 16));
        topContainer.setOpaque(false);
        topContainer.add(welcomeCard, BorderLayout.NORTH);
        topContainer.add(middleRow, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        JPanel activityPanel = UITheme.createCardPanel();
        activityPanel.setLayout(new BorderLayout(8, 8));
        activityPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JPanel activityHeader = new JPanel(new BorderLayout());
        activityHeader.setOpaque(false);
        JLabel lblActivityTitle = new JLabel("Recent Booking Activity");
        lblActivityTitle.setFont(UITheme.FONT_HEADER);
        lblActivityTitle.setForeground(UITheme.TEXT_DARK);
        activityHeader.add(lblActivityTitle, BorderLayout.WEST);

        JButton btnAllBookings = new JButton("View All Bookings →");
        btnAllBookings.setFont(UITheme.FONT_SMALL);
        btnAllBookings.setFocusPainted(false);
        btnAllBookings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAllBookings.addActionListener(e -> onMyBookings.run());
        activityHeader.add(btnAllBookings, BorderLayout.EAST);
        activityPanel.add(activityHeader, BorderLayout.NORTH);

        String[] columns = {"PNR Number", "Train", "Route", "Date", "Class", "Passengers", "Total Fare", "Status"};
        activityTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblActivity = new JTable(activityTableModel);
        tblActivity.setFont(UITheme.FONT_BODY);
        tblActivity.setRowHeight(28);
        tblActivity.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblActivity.getSelectedRow();
                    if (row >= 0 && cachedReservations != null && row < cachedReservations.size()) {
                        onViewTicket.accept(cachedReservations.get(row));
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblActivity);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        activityPanel.add(scrollPane, BorderLayout.CENTER);

        add(activityPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, String initialValue, Color valueColor) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(6, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_BODY_BOLD);
        lblTitle.setForeground(new Color(100, 116, 139));

        JLabel lblVal = new JLabel(initialValue);
        lblVal.setFont(UITheme.FONT_TITLE);
        lblVal.setForeground(valueColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        card.putClientProperty("valueLabel", lblVal);
        return card;
    }

    public void refreshDashboard() {
        User user = SessionContext.getCurrentUser();
        if (user == null) return;

        lblWelcome.setText(String.format("Welcome back, %s!", user.getFullName()));

        SwingWorker<List<Reservation>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Reservation> doInBackground() throws Exception {
                return reservationService.getUserReservations(user.getId());
            }

            @Override
            protected void done() {
                try {
                    List<Reservation> list = get();
                    cachedReservations = list;
                    int total = list.size();
                    long confirmed = list.stream().filter(r -> r.getStatus() == BookingStatus.CONFIRMED).count();

                    lblTotalBookings.setText(String.valueOf(total));
                    lblConfirmedBookings.setText(String.valueOf(confirmed));

                    LocalDate today = LocalDate.now();
                    Reservation upcoming = list.stream()
                            .filter(r -> r.getStatus() == BookingStatus.CONFIRMED && !r.getJourneyDate().isBefore(today))
                            .min(Comparator.comparing(Reservation::getJourneyDate))
                            .orElse(null);

                    nextUpcomingReservation = upcoming;
                    if (upcoming != null) {
                        String trainName = upcoming.getTrain() != null ? upcoming.getTrain().getTrainName() : "Train " + upcoming.getTrainId();
                        String from = upcoming.getFromStation() != null ? upcoming.getFromStation().getStationCode() : "";
                        String to = upcoming.getToStation() != null ? upcoming.getToStation().getStationCode() : "";
                        lblUpcomingDetails.setText(String.format(
                                "<html><b>PNR: %s</b><br>%s (%s &rarr; %s)<br>Date: <b>%s</b> | Class: %s</html>",
                                upcoming.getPnrNumber(), trainName, from, to,
                                upcoming.getJourneyDate(), upcoming.getTravelClass().getCode()));
                        btnViewUpcoming.setVisible(true);
                    } else {
                        lblUpcomingDetails.setText("<html><body style='color:#64748B;'>No upcoming journeys scheduled.</body></html>");
                        btnViewUpcoming.setVisible(false);
                    }

                    activityTableModel.setRowCount(0);
                    list.stream().limit(10).forEach(r -> {
                        String trainStr = r.getTrain() != null ? r.getTrain().getTrainNumber() + " - " + r.getTrain().getTrainName() : "ID: " + r.getTrainId();
                        String routeStr = (r.getFromStation() != null ? r.getFromStation().getStationCode() : "") + " -> " +
                                          (r.getToStation() != null ? r.getToStation().getStationCode() : "");
                        activityTableModel.addRow(new Object[]{
                                r.getPnrNumber(),
                                trainStr,
                                routeStr,
                                r.getJourneyDate(),
                                r.getTravelClass().getCode(),
                                r.getTotalPassengers(),
                                "INR " + r.getTotalFare(),
                                r.getStatus()
                        });
                    });

                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }
}
