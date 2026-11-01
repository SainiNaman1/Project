package com.railway.gui;

import com.railway.config.ApplicationContext;
import com.railway.config.SessionContext;
import com.railway.database.DatabaseConnection;
import com.railway.gui.panels.PlatformManagementPanel;
import com.railway.gui.panels.RulesPanel;
import com.railway.gui.panels.TrainManagementPanel;
import com.railway.model.Reservation;
import com.railway.model.Train;
import com.railway.model.User;
import com.railway.model.enums.BookingStatus;
import com.railway.service.AdminService;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Modern Operations & Administration Console.
 * Features a Persistent Left Navigation Sidebar, Executive Overview metrics,
 * Fleet management, live platform reassignment, and administrative audit trails.
 */
public class AdminDashboard extends JFrame {

    public static final String CARD_OVERVIEW = "OVERVIEW";
    public static final String CARD_FLEET = "FLEET";
    public static final String CARD_PLATFORM = "PLATFORM";
    public static final String CARD_RESERVATIONS = "RESERVATIONS";
    public static final String CARD_AUDIT_LOGS = "AUDIT_LOGS";
    public static final String CARD_RULES = "RULES";

    private final AdminService adminService;
    private final TrainService trainService;
    private final ReservationService reservationService;

    private CardLayout cardLayout;
    private JPanel cardContainer;

    // Overview metric labels
    private JLabel lblTotalTrains;
    private JLabel lblActiveTrains;
    private JLabel lblTotalReservations;
    private JLabel lblConfirmedBookings;
    private JLabel lblCancelledBookings;

    private JPanel overviewPanel;
    private TrainManagementPanel fleetPanel;
    private PlatformManagementPanel platformPanel;
    private JPanel reservationsAuditPanel;
    private DefaultTableModel resAuditTableModel;
    private JPanel auditLogPanel;
    private DefaultTableModel auditLogTableModel;
    private RulesPanel rulesPanel;

    private final Map<String, JButton> sidebarButtons = new HashMap<>();

    public AdminDashboard() {
        this(ApplicationContext.getInstance().getAdminService(),
             ApplicationContext.getInstance().getTrainService(),
             ApplicationContext.getInstance().getReservationService());
    }

    public AdminDashboard(AdminService adminService, TrainService trainService, ReservationService reservationService) {
        this.adminService = adminService;
        this.trainService = trainService;
        this.reservationService = reservationService;
        initUI();
    }

    private void initUI() {
        setTitle("Railway Management System — Operations & Administration Console");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1020, 700));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // 1. Persistent Left Navigation Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Central Content Area (CardLayout)
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);

        overviewPanel = createOverviewPanel();
        fleetPanel = new TrainManagementPanel(adminService, trainService);
        platformPanel = new PlatformManagementPanel(adminService, trainService);
        reservationsAuditPanel = createReservationsAuditPanel();
        auditLogPanel = createAuditLogsPanel();
        rulesPanel = new RulesPanel(trainService);

        cardContainer.add(overviewPanel, CARD_OVERVIEW);
        cardContainer.add(fleetPanel, CARD_FLEET);
        cardContainer.add(platformPanel, CARD_PLATFORM);
        cardContainer.add(reservationsAuditPanel, CARD_RESERVATIONS);
        cardContainer.add(auditLogPanel, CARD_AUDIT_LOGS);
        cardContainer.add(rulesPanel, CARD_RULES);

        // Top Header
        JPanel mainWrapper = new JPanel(new BorderLayout());
        JPanel topHeader = createTopHeader();
        mainWrapper.add(topHeader, BorderLayout.NORTH);
        mainWrapper.add(cardContainer, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel statusBar = createStatusBar();
        mainWrapper.add(statusBar, BorderLayout.SOUTH);

        add(mainWrapper, BorderLayout.CENTER);

        navigateToCard(CARD_OVERVIEW);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(15, 23, 42)); // Dark Slate Admin Theme
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        JLabel lblLogo = new JLabel("OPERATIONS CONSOLE");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblLogo.setForeground(new Color(251, 191, 36)); // Gold Accent
        lblLogo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblTag = new JLabel("High-Privilege Admin Portal");
        lblTag.setFont(UITheme.FONT_SMALL);
        lblTag.setForeground(new Color(148, 163, 184));
        lblTag.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(lblLogo);
        sidebar.add(lblTag);
        sidebar.add(Box.createRigidArea(new Dimension(0, 24)));

        addSidebarButton(sidebar, "Dashboard Overview", CARD_OVERVIEW);
        addSidebarButton(sidebar, "Fleet Management", CARD_FLEET);
        addSidebarButton(sidebar, "Platform Reassignment", CARD_PLATFORM);
        addSidebarButton(sidebar, "Reservation Oversight", CARD_RESERVATIONS);
        addSidebarButton(sidebar, "System Audit Logs", CARD_AUDIT_LOGS);
        addSidebarButton(sidebar, "Guidelines & Notices", CARD_RULES);

        sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UITheme.FONT_BODY);
        btnLogout.setForeground(new Color(254, 202, 202));
        btnLogout.setBackground(new Color(30, 41, 59));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btnLogout.setFocusPainted(false);
        btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> performLogout());
        sidebar.add(btnLogout);

        return sidebar;
    }

    private void addSidebarButton(JPanel sidebar, String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setForeground(new Color(226, 232, 240));
        btn.setBackground(new Color(15, 23, 42));
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

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));

        User user = SessionContext.getCurrentUser();
        String greeting = user != null ? "Administrator: " + user.getFullName() + " (" + user.getUsername() + ")" : "System Administrator";

        JLabel lblTitle = new JLabel("Railway Operations & Fleet Supervisory Console");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        header.add(lblTitle, BorderLayout.WEST);

        JLabel lblUser = new JLabel(greeting);
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        lblUser.setForeground(new Color(180, 83, 9));
        header.add(lblUser, BorderLayout.EAST);

        return header;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));

        JLabel lblDb = new JLabel(String.format(" Database: %s | Mode: Full Operational Authority | Isolation: SERIALIZABLE",
                DatabaseConnection.getActiveDatabaseType()));
        lblDb.setFont(UITheme.FONT_SMALL);
        lblDb.setForeground(new Color(100, 116, 139));
        statusBar.add(lblDb, BorderLayout.WEST);

        return statusBar;
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Metric Cards Grid
        JPanel metricsGrid = new JPanel(new GridLayout(1, 5, 14, 14));
        metricsGrid.setOpaque(false);

        JPanel card1 = createStatCard("Total Trains", "—", new Color(15, 23, 42));
        lblTotalTrains = (JLabel) card1.getClientProperty("valueLabel");

        JPanel card2 = createStatCard("Active Services", "—", UITheme.ACCENT_GREEN);
        lblActiveTrains = (JLabel) card2.getClientProperty("valueLabel");

        JPanel card3 = createStatCard("Total Bookings", "—", new Color(30, 41, 59));
        lblTotalReservations = (JLabel) card3.getClientProperty("valueLabel");

        JPanel card4 = createStatCard("Confirmed", "—", UITheme.ACCENT_GREEN);
        lblConfirmedBookings = (JLabel) card4.getClientProperty("valueLabel");

        JPanel card5 = createStatCard("Cancelled", "—", UITheme.ACCENT_RED);
        lblCancelledBookings = (JLabel) card5.getClientProperty("valueLabel");

        metricsGrid.add(card1);
        metricsGrid.add(card2);
        metricsGrid.add(card3);
        metricsGrid.add(card4);
        metricsGrid.add(card5);
        panel.add(metricsGrid, BorderLayout.NORTH);

        // Center Quick Actions & Guidelines
        JPanel centerCard = UITheme.createCardPanel();
        centerCard.setLayout(new BorderLayout(12, 12));
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel lblQuickTitle = new JLabel("Administrative Quick Actions");
        lblQuickTitle.setFont(UITheme.FONT_HEADER);
        centerCard.add(lblQuickTitle, BorderLayout.NORTH);

        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        actionsRow.setOpaque(false);

        JButton btnAddTrain = UITheme.createPrimaryButton("+ Add New Train to Fleet");
        btnAddTrain.addActionListener(e -> navigateToCard(CARD_FLEET));

        JButton btnPlatformChange = UITheme.createSecondaryButton("Reassign Scheduled Platform");
        btnPlatformChange.addActionListener(e -> navigateToCard(CARD_PLATFORM));

        JButton btnAuditReservations = UITheme.createSecondaryButton("Audit All Reservations");
        btnAuditReservations.addActionListener(e -> navigateToCard(CARD_RESERVATIONS));

        JButton btnViewLogs = UITheme.createSecondaryButton("View Audit Trail");
        btnViewLogs.addActionListener(e -> navigateToCard(CARD_AUDIT_LOGS));

        actionsRow.add(btnAddTrain);
        actionsRow.add(btnPlatformChange);
        actionsRow.add(btnAuditReservations);
        actionsRow.add(btnViewLogs);
        centerCard.add(actionsRow, BorderLayout.CENTER);

        panel.add(centerCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatCard(String title, String initialValue, Color valueColor) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(6, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(new Color(100, 116, 139));

        JLabel lblVal = new JLabel(initialValue);
        lblVal.setFont(UITheme.FONT_TITLE);
        lblVal.setForeground(valueColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        card.putClientProperty("valueLabel", lblVal);
        return card;
    }

    private JPanel createReservationsAuditPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new BorderLayout());
        JLabel lblTitle = UITheme.createHeaderLabel("System-Wide Passenger Reservation Audit");
        topBar.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Refresh Audit Records");
        btnRefresh.setFont(UITheme.FONT_BODY);
        btnRefresh.addActionListener(e -> loadReservationsAuditData());
        topBar.add(btnRefresh, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"PNR", "User ID", "Train", "Date", "Route", "Class", "Passengers", "Fare", "Status", "Booked At"};
        resAuditTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = new JTable(resAuditTableModel);
        tbl.setFont(UITheme.FONT_BODY);
        tbl.setRowHeight(26);
        panel.add(new JScrollPane(tbl), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAuditLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new BorderLayout());
        JLabel lblTitle = UITheme.createHeaderLabel("Administrative Action Audit Trail");
        topBar.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Refresh Logs");
        btnRefresh.setFont(UITheme.FONT_BODY);
        btnRefresh.addActionListener(e -> loadSystemAuditLogs());
        topBar.add(btnRefresh, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Admin / User ID", "Action", "Entity Type", "Entity ID", "Details", "Timestamp"};
        auditLogTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = new JTable(auditLogTableModel);
        tbl.setFont(UITheme.FONT_BODY);
        tbl.setRowHeight(26);
        panel.add(new JScrollPane(tbl), BorderLayout.CENTER);

        return panel;
    }

    public void navigateToCard(String cardName) {
        sidebarButtons.forEach((name, btn) -> {
            if (name.equals(cardName)) {
                btn.setBackground(new Color(51, 65, 85));
                btn.setForeground(new Color(251, 191, 36));
            } else {
                btn.setBackground(new Color(15, 23, 42));
                btn.setForeground(new Color(226, 232, 240));
            }
        });

        if (CARD_OVERVIEW.equals(cardName)) {
            refreshOverviewMetrics();
        } else if (CARD_RESERVATIONS.equals(cardName)) {
            loadReservationsAuditData();
        } else if (CARD_AUDIT_LOGS.equals(cardName)) {
            loadSystemAuditLogs();
        }

        cardLayout.show(cardContainer, cardName);
    }

    private void refreshOverviewMetrics() {
        new SwingWorker<Void, Void>() {
            int totalTrains = 0, activeTrains = 0, totalRes = 0, confirmedRes = 0, cancelledRes = 0;

            @Override
            protected Void doInBackground() throws Exception {
                List<Train> trains = trainService.getAllActiveTrains();
                totalTrains = trains.size();
                activeTrains = (int) trains.stream().filter(Train::isActive).count();

                List<Reservation> resList = reservationService.getAllReservations();
                totalRes = resList.size();
                confirmedRes = (int) resList.stream().filter(r -> r.getStatus() == BookingStatus.CONFIRMED).count();
                cancelledRes = (int) resList.stream().filter(r -> r.getStatus() == BookingStatus.CANCELLED).count();
                return null;
            }

            @Override
            protected void done() {
                lblTotalTrains.setText(String.valueOf(totalTrains));
                lblActiveTrains.setText(String.valueOf(activeTrains));
                lblTotalReservations.setText(String.valueOf(totalRes));
                lblConfirmedBookings.setText(String.valueOf(confirmedRes));
                lblCancelledBookings.setText(String.valueOf(cancelledRes));
            }
        }.execute();
    }

    private void loadReservationsAuditData() {
        resAuditTableModel.setRowCount(0);
        new SwingWorker<List<Reservation>, Void>() {
            @Override
            protected List<Reservation> doInBackground() throws Exception {
                return reservationService.getAllReservations();
            }

            @Override
            protected void done() {
                try {
                    List<Reservation> list = get();
                    for (Reservation r : list) {
                        String trainStr = r.getTrain() != null ? r.getTrain().getTrainNumber() : "Train #" + r.getTrainId();
                        String routeStr = (r.getFromStation() != null ? r.getFromStation().getStationCode() : "") + " -> " +
                                          (r.getToStation() != null ? r.getToStation().getStationCode() : "");
                        resAuditTableModel.addRow(new Object[]{
                                r.getPnrNumber(),
                                r.getUserId(),
                                trainStr,
                                r.getJourneyDate().toString(),
                                routeStr,
                                r.getTravelClass().getCode(),
                                r.getTotalPassengers(),
                                "INR " + r.getTotalFare(),
                                r.getStatus().name(),
                                r.getBookingTimestamp() != null ? r.getBookingTimestamp().toString() : "N/A"
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Audit query failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void loadSystemAuditLogs() {
        auditLogTableModel.setRowCount(0);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String sql = "SELECT id, user_id, action, entity_type, entity_id, details, timestamp FROM audit_logs ORDER BY id DESC LIMIT 100";
                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        long id = rs.getLong("id");
                        long userId = rs.getLong("user_id");
                        String action = rs.getString("action");
                        String entityType = rs.getString("entity_type");
                        String entityId = rs.getString("entity_id");
                        String details = rs.getString("details");
                        String ts = rs.getString("timestamp");
                        auditLogTableModel.addRow(new Object[]{id, userId, action, entityType, entityId, details, ts});
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                // Done loading
            }
        }.execute();
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Log out from administrative console?",
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
