package com.railway.gui;

import com.railway.config.ApplicationContext;
import com.railway.config.SessionContext;
import com.railway.database.DatabaseConnection;
import com.railway.gui.panels.BookingPanel;
import com.railway.gui.panels.MyBookingsPanel;
import com.railway.gui.panels.PassengerHomePanel;
import com.railway.gui.panels.RoutePanel;
import com.railway.gui.panels.RulesPanel;
import com.railway.gui.panels.TicketPanel;
import com.railway.gui.panels.TrainSearchPanel;
import com.railway.model.Reservation;
import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.User;
import com.railway.model.enums.TravelClass;
import com.railway.service.ReservationService;
import com.railway.service.TrainService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Modern Passenger Portal featuring a Persistent Navigation Sidebar
 * and smooth CardLayout context panel transitions inside a single JFrame.
 */
public class PassengerDashboard extends JFrame {

    public static final String CARD_HOME = "HOME";
    public static final String CARD_SEARCH = "SEARCH";
    public static final String CARD_BOOKING = "BOOKING";
    public static final String CARD_MY_BOOKINGS = "MY_BOOKINGS";
    public static final String CARD_ROUTE = "ROUTE";
    public static final String CARD_RULES = "RULES";
    public static final String CARD_TICKET = "TICKET";

    private final TrainService trainService;
    private final ReservationService reservationService;

    private CardLayout cardLayout;
    private JPanel cardContainer;

    private PassengerHomePanel homePanel;
    private TrainSearchPanel searchPanel;
    private BookingPanel bookingPanel;
    private MyBookingsPanel myBookingsPanel;
    private RoutePanel routePanel;
    private RulesPanel rulesPanel;
    private TicketPanel ticketPanel;

    private final Map<String, JButton> sidebarButtons = new HashMap<>();
    private String currentCard = CARD_HOME;

    public PassengerDashboard() {
        this(ApplicationContext.getInstance().getTrainService(),
             ApplicationContext.getInstance().getReservationService());
    }

    public PassengerDashboard(TrainService trainService, ReservationService reservationService) {
        this.trainService = trainService;
        this.reservationService = reservationService;
        initUI();
    }

    private void initUI() {
        setTitle("Railway Management System — Passenger Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // 1. Persistent Left Navigation Sidebar (Navy & Slate identity)
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Central Content Area (CardLayout)
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);

        homePanel = new PassengerHomePanel(
                reservationService,
                () -> navigateToCard(CARD_SEARCH),
                () -> navigateToCard(CARD_BOOKING),
                () -> navigateToCard(CARD_MY_BOOKINGS),
                this::navigateToTicket
        );

        searchPanel = new TrainSearchPanel(trainService, (train, from, to, date, dist) -> {
            bookingPanel.setBookingContext(train, from, to, date, TravelClass.THIRD_AC, dist);
            navigateToCard(CARD_BOOKING);
        });

        bookingPanel = new BookingPanel(reservationService, trainService, this::navigateToTicket);
        myBookingsPanel = new MyBookingsPanel(reservationService, this::navigateToTicket);
        routePanel = new RoutePanel(trainService);
        rulesPanel = new RulesPanel(trainService);
        ticketPanel = new TicketPanel(() -> navigateToCard(CARD_MY_BOOKINGS));

        cardContainer.add(homePanel, CARD_HOME);
        cardContainer.add(searchPanel, CARD_SEARCH);
        cardContainer.add(bookingPanel, CARD_BOOKING);
        cardContainer.add(myBookingsPanel, CARD_MY_BOOKINGS);
        cardContainer.add(routePanel, CARD_ROUTE);
        cardContainer.add(rulesPanel, CARD_RULES);
        cardContainer.add(ticketPanel, CARD_TICKET);

        // Top Header above main content
        JPanel mainWrapper = new JPanel(new BorderLayout());
        JPanel topHeader = createTopHeader();
        mainWrapper.add(topHeader, BorderLayout.NORTH);
        mainWrapper.add(cardContainer, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel statusBar = createStatusBar();
        mainWrapper.add(statusBar, BorderLayout.SOUTH);

        add(mainWrapper, BorderLayout.CENTER);

        // Initial view
        navigateToCard(CARD_HOME);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.PRIMARY_NAVY);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        // Brand Banner
        JLabel lblLogo = new JLabel("INDIAN RAILWAYS");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblTag = new JLabel("Passenger Portal");
        lblTag.setFont(UITheme.FONT_SMALL);
        lblTag.setForeground(new Color(148, 163, 184));
        lblTag.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(lblLogo);
        sidebar.add(lblTag);
        sidebar.add(Box.createRigidArea(new Dimension(0, 24)));

        // Navigation Items
        addSidebarButton(sidebar, "Dashboard", CARD_HOME);
        addSidebarButton(sidebar, "Search Trains", CARD_SEARCH);
        addSidebarButton(sidebar, "Book Ticket", CARD_BOOKING);
        addSidebarButton(sidebar, "My Bookings", CARD_MY_BOOKINGS);
        addSidebarButton(sidebar, "Journey Info", CARD_ROUTE);
        addSidebarButton(sidebar, "Travel Guidelines", CARD_RULES);

        sidebar.add(Box.createVerticalGlue());

        // My Profile button
        JButton btnProfile = createSidebarActionButton("My Profile", e -> showProfileDialog());
        sidebar.add(btnProfile);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));

        // Logout Button
        JButton btnLogout = createSidebarActionButton("Logout", e -> performLogout());
        btnLogout.setForeground(new Color(254, 202, 202));
        sidebar.add(btnLogout);

        return sidebar;
    }

    private void addSidebarButton(JPanel sidebar, String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setForeground(new Color(226, 232, 240));
        btn.setBackground(UITheme.PRIMARY_NAVY);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> navigateToCard(cardName));
        sidebarButtons.put(cardName, btn);
        sidebar.add(btn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private JButton createSidebarActionButton(String label, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(label);
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(new Color(30, 41, 59));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(listener);
        return btn;
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));

        User user = SessionContext.getCurrentUser();
        String greeting = user != null ? "Logged in as " + user.getFullName() + " (" + user.getUsername() + ")" : "Guest Passenger";

        JLabel lblTitle = new JLabel("Online Reservation & Passenger Services");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        header.add(lblTitle, BorderLayout.WEST);

        JLabel lblUser = new JLabel(greeting);
        lblUser.setFont(UITheme.FONT_BODY);
        lblUser.setForeground(new Color(71, 85, 105));
        header.add(lblUser, BorderLayout.EAST);

        return header;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));

        JLabel lblDb = new JLabel(String.format(" Database: %s | Host: 127.0.0.1:3306 | Status: Ready",
                DatabaseConnection.getActiveDatabaseType()));
        lblDb.setFont(UITheme.FONT_SMALL);
        lblDb.setForeground(new Color(100, 116, 139));
        statusBar.add(lblDb, BorderLayout.WEST);

        JLabel lblMode = new JLabel("Educational Desktop Simulation Mode ");
        lblMode.setFont(UITheme.FONT_SMALL);
        lblMode.setForeground(new Color(100, 116, 139));
        statusBar.add(lblMode, BorderLayout.EAST);

        return statusBar;
    }

    public void navigateToCard(String cardName) {
        this.currentCard = cardName;

        // Update button visual selection
        sidebarButtons.forEach((name, btn) -> {
            if (name.equals(cardName)) {
                btn.setBackground(UITheme.SECONDARY_SLATE);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(UITheme.PRIMARY_NAVY);
                btn.setForeground(new Color(226, 232, 240));
            }
        });

        // Trigger refreshes on navigation
        if (CARD_HOME.equals(cardName)) {
            homePanel.refreshDashboard();
        } else if (CARD_MY_BOOKINGS.equals(cardName)) {
            myBookingsPanel.refreshBookings();
        }

        cardLayout.show(cardContainer, cardName);
    }

    private void navigateToTicket(Reservation res) {
        ticketPanel.displayReservation(res);
        navigateToCard(CARD_TICKET);
    }

    private void showProfileDialog() {
        User user = SessionContext.getCurrentUser();
        if (user == null) return;

        String msg = String.format(
                "Full Name: %s\nUsername: %s\nEmail: %s\nRole: %s\nAccount Created: %s",
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : "N/A"
        );
        JOptionPane.showMessageDialog(this, msg, "Passenger Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of your session?",
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
