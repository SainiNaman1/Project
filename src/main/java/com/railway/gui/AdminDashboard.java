package com.railway.gui;

import com.railway.config.SessionContext;
import com.railway.gui.panels.PlatformManagementPanel;
import com.railway.gui.panels.RulesPanel;
import com.railway.gui.panels.TrainManagementPanel;
import com.railway.model.Reservation;
import com.railway.model.User;
import com.railway.service.AdminService;
import com.railway.service.ReservationService;
import com.railway.service.TrainService;
import com.railway.service.impl.AdminServiceImpl;
import com.railway.service.impl.ReservationServiceImpl;
import com.railway.service.impl.TrainServiceImpl;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Administrative control console for managing train schedules, live platform assignments,
 * and system-wide reservation audits.
 */
public class AdminDashboard extends JFrame {

    private static final String CARD_FLEET = "FLEET";
    private static final String CARD_PLATFORM = "PLATFORM";
    private static final String CARD_AUDIT = "AUDIT";
    private static final String CARD_RULES = "RULES";

    private final AdminService adminService;
    private final TrainService trainService;
    private final ReservationService reservationService;

    private JPanel cardContainer;
    private CardLayout cardLayout;

    private TrainManagementPanel fleetPanel;
    private PlatformManagementPanel platformPanel;
    private RulesPanel rulesPanel;
    private JPanel auditPanel;
    private DefaultTableModel auditTableModel;

    public AdminDashboard() {
        this.adminService = new AdminServiceImpl();
        this.trainService = new TrainServiceImpl();
        this.reservationService = new ReservationServiceImpl();
        initUI();
    }

    private void initUI() {
        setTitle("Railway Management System - Operations Administration Console");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 740);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Top Header Banner
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(new Color(15, 23, 42)); // Slate dark admin banner
        topHeader.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblBrand = new JLabel("INDIAN RAILWAYS SIMULATION - ADMIN OPERATIONS");
        lblBrand.setFont(UITheme.FONT_TITLE);
        lblBrand.setForeground(Color.WHITE);
        topHeader.add(lblBrand, BorderLayout.WEST);

        User currentUser = SessionContext.getCurrentUser();
        String userGreeting = currentUser != null 
                ? "Admin: " + currentUser.getFullName() + " (" + currentUser.getUsername() + ")" 
                : "Administrator";

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userPanel.setOpaque(false);

        JLabel lblUser = new JLabel(userGreeting);
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        lblUser.setForeground(new Color(251, 191, 36)); // Gold admin badge

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

        JButton btnFleet = createNavButton("Fleet Management", CARD_FLEET);
        JButton btnPlatform = createNavButton("Live Platform Control", CARD_PLATFORM);
        JButton btnAudit = createNavButton("Reservation Audit", CARD_AUDIT);
        JButton btnRules = createNavButton("Guidelines & Notices", CARD_RULES);

        navBar.add(btnFleet);
        navBar.add(btnPlatform);
        navBar.add(btnAudit);
        navBar.add(btnRules);

        // 3. Central Cards
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);

        fleetPanel = new TrainManagementPanel(adminService, trainService);
        platformPanel = new PlatformManagementPanel(adminService, trainService);
        rulesPanel = new RulesPanel(trainService);
        auditPanel = createAuditPanel();

        cardContainer.add(fleetPanel, CARD_FLEET);
        cardContainer.add(platformPanel, CARD_PLATFORM);
        cardContainer.add(auditPanel, CARD_AUDIT);
        cardContainer.add(rulesPanel, CARD_RULES);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(navBar, BorderLayout.NORTH);
        centerWrapper.add(cardContainer, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // 4. Status Bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(new Color(241, 245, 249));
        JLabel lblStatus = new JLabel(" Database: " + com.railway.database.DatabaseConnection.getActiveDatabaseType() + " Connected | Role: ADMINISTRATOR | High Privilege Session");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(new Color(71, 85, 105));
        statusBar.add(lblStatus);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            if (CARD_AUDIT.equals(cardName)) {
                loadAuditData();
            }
            cardLayout.show(cardContainer, cardName);
        });
        return btn;
    }

    private JPanel createAuditPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(248, 250, 252));

        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new BorderLayout());
        JLabel lblTitle = UITheme.createHeaderLabel("System-Wide Reservation Audit Trail");
        topBar.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Refresh Audit Trail");
        btnRefresh.setFont(UITheme.FONT_BODY);
        btnRefresh.addActionListener(e -> loadAuditData());
        topBar.add(btnRefresh, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"PNR", "User ID", "Train", "Date", "Route", "Class", "Passengers", "Fare", "Status", "Booked At"};
        auditTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = new JTable(auditTableModel);
        tbl.setFont(UITheme.FONT_BODY);
        panel.add(new JScrollPane(tbl), BorderLayout.CENTER);

        return panel;
    }

    private void loadAuditData() {
        auditTableModel.setRowCount(0);
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
                        String routeStr = r.getFromStation().getStationCode() + " -> " + r.getToStation().getStationCode();
                        auditTableModel.addRow(new Object[]{
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

    private void performLogout() {
        SessionContext.logout();
        dispose();
        new LoginFrame().setVisible(true);
    }
}
