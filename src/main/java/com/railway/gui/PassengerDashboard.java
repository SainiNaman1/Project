package com.railway.gui;

import com.railway.config.SessionContext;
import com.railway.gui.panels.BookingPanel;
import com.railway.gui.panels.MyBookingsPanel;
import com.railway.gui.panels.RoutePanel;
import com.railway.gui.panels.RulesPanel;
import com.railway.gui.panels.TicketPanel;
import com.railway.gui.panels.TrainSearchPanel;
import com.railway.model.Reservation;
import com.railway.model.Train;
import com.railway.model.User;
import com.railway.service.ReservationService;
import com.railway.service.TrainService;
import com.railway.service.impl.ReservationServiceImpl;
import com.railway.service.impl.TrainServiceImpl;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Main dashboard window for authenticated passengers.
 * Employs CardLayout for seamless single-window transitions across
 * Train Search, Booking, Personal Bookings, and Ticket Printing.
 */
public class PassengerDashboard extends JFrame {

    private static final String CARD_SEARCH = "SEARCH";
    private static final String CARD_ROUTE = "ROUTE";
    private static final String CARD_BOOKING = "BOOKING";
    private static final String CARD_MY_BOOKINGS = "MY_BOOKINGS";
    private static final String CARD_TICKET = "TICKET";
    private static final String CARD_RULES = "RULES";

    private final TrainService trainService;
    private final ReservationService reservationService;

    private JPanel cardContainer;
    private CardLayout cardLayout;

    private TrainSearchPanel searchPanel;
    private RoutePanel routePanel;
    private BookingPanel bookingPanel;
    private MyBookingsPanel myBookingsPanel;
    private TicketPanel ticketPanel;
    private RulesPanel rulesPanel;

    public PassengerDashboard() {
        this.trainService = new TrainServiceImpl();
        this.reservationService = new ReservationServiceImpl();
        initUI();
    }

    private void initUI() {
        setTitle("Railway Management System - Passenger Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Top Header Banner & User Profile
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UITheme.PRIMARY_NAVY);
        topHeader.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblBrand = new JLabel("INDIAN RAILWAYS SIMULATION SYSTEM");
        lblBrand.setFont(UITheme.FONT_TITLE);
        lblBrand.setForeground(Color.WHITE);
        topHeader.add(lblBrand, BorderLayout.WEST);

        User currentUser = SessionContext.getCurrentUser();
        String userGreeting = currentUser != null 
                ? "Logged in: " + currentUser.getFullName() + " (" + currentUser.getUsername() + ")" 
                : "Guest Passenger";

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userPanel.setOpaque(false);

        JLabel lblUser = new JLabel(userGreeting);
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        lblUser.setForeground(new Color(226, 232, 240));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UITheme.FONT_SMALL);
        btnLogout.addActionListener(e -> performLogout());

        userPanel.add(lblUser);
        userPanel.add(btnLogout);
        topHeader.add(userPanel, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);

        // 2. Navigation Bar
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        navBar.setBackground(new Color(241, 245, 249));
        navBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));

        JButton btnNavSearch = createNavButton("Search Trains", CARD_SEARCH);
        JButton btnNavRoute = createNavButton("Train Itinerary", CARD_ROUTE);
        JButton btnNavBooking = createNavButton("Book Ticket", CARD_BOOKING);
        JButton btnNavMyBookings = createNavButton("My Bookings", CARD_MY_BOOKINGS);
        JButton btnNavRules = createNavButton("Travel Guidelines", CARD_RULES);

        navBar.add(btnNavSearch);
        navBar.add(btnNavRoute);
        navBar.add(btnNavBooking);
        navBar.add(btnNavMyBookings);
        navBar.add(btnNavRules);

        // 3. Central Card Container
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);

        // Initialize child panels with inter-panel transition callbacks
        searchPanel = new TrainSearchPanel(trainService, (train, date) -> navigateToBooking(train, date));
        routePanel = new RoutePanel(trainService);
        bookingPanel = new BookingPanel(reservationService, trainService, confirmedRes -> navigateToTicket(confirmedRes));
        myBookingsPanel = new MyBookingsPanel(reservationService, res -> navigateToTicket(res));
        ticketPanel = new TicketPanel(() -> cardLayout.show(cardContainer, CARD_MY_BOOKINGS));
        rulesPanel = new RulesPanel(trainService);

        cardContainer.add(searchPanel, CARD_SEARCH);
        cardContainer.add(routePanel, CARD_ROUTE);
        cardContainer.add(bookingPanel, CARD_BOOKING);
        cardContainer.add(myBookingsPanel, CARD_MY_BOOKINGS);
        cardContainer.add(ticketPanel, CARD_TICKET);
        cardContainer.add(rulesPanel, CARD_RULES);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(navBar, BorderLayout.NORTH);
        centerWrapper.add(cardContainer, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // 4. Bottom System Status Bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        JLabel lblDbStatus = new JLabel(" Database: " + com.railway.database.DatabaseConnection.getActiveDatabaseType() + " Connected | System: Ready | Educational Simulation Mode");
        lblDbStatus.setFont(UITheme.FONT_SMALL);
        lblDbStatus.setForeground(new Color(100, 116, 139));
        statusBar.add(lblDbStatus);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JButton createNavButton(String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            if (CARD_MY_BOOKINGS.equals(cardName)) {
                myBookingsPanel.refreshBookings();
            }
            cardLayout.show(cardContainer, cardName);
        });
        return btn;
    }

    private void navigateToBooking(Train train, LocalDate date) {
        bookingPanel.setBookingContext(train, date);
        cardLayout.show(cardContainer, CARD_BOOKING);
    }

    private void navigateToTicket(Reservation reservation) {
        ticketPanel.displayReservation(reservation);
        cardLayout.show(cardContainer, CARD_TICKET);
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            SessionContext.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
