package com.railway.gui.panels;

import com.railway.gui.UITheme;
import com.railway.model.RouteStop;
import com.railway.model.Train;
import com.railway.service.AdminService;
import com.railway.service.TrainService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Administrative panel for managing live platform assignments for train station stops.
 */
public class PlatformManagementPanel extends JPanel {

    private final AdminService adminService;
    private final TrainService trainService;

    private JComboBox<Train> cmbTrains;
    private JComboBox<RouteStop> cmbStops;
    private JSpinner spinPlatform;
    private JButton btnUpdatePlatform;
    private JTable tblPlatforms;
    private DefaultTableModel tableModel;
    private JLabel lblStatus;

    public PlatformManagementPanel(AdminService adminService, TrainService trainService) {
        this.adminService = adminService;
        this.trainService = trainService;
        initUI();
        loadTrains();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(248, 250, 252));

        // Top Control Card
        JPanel controlCard = UITheme.createCardPanel();
        controlCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = UITheme.createHeaderLabel("Live Station Platform Management");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        controlCard.add(lblTitle, gbc);

        gbc.gridwidth = 1;

        // Train Selector
        JLabel lblTrain = new JLabel("Select Train:");
        lblTrain.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 0; gbc.gridy = 1;
        controlCard.add(lblTrain, gbc);

        cmbTrains = new JComboBox<>();
        cmbTrains.setFont(UITheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = 2;
        controlCard.add(cmbTrains, gbc);

        // Station Stop Selector
        JLabel lblStop = new JLabel("Select Station Stop:");
        lblStop.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 1; gbc.gridy = 1;
        controlCard.add(lblStop, gbc);

        cmbStops = new JComboBox<>();
        cmbStops.setFont(UITheme.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 2;
        controlCard.add(cmbStops, gbc);

        // Platform Number Spinner
        JLabel lblPlat = new JLabel("Platform Number (1-24):");
        lblPlat.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 2; gbc.gridy = 1;
        controlCard.add(lblPlat, gbc);

        spinPlatform = new JSpinner(new SpinnerNumberModel(1, 1, 24, 1));
        spinPlatform.setFont(UITheme.FONT_BODY);
        gbc.gridx = 2; gbc.gridy = 2;
        controlCard.add(spinPlatform, gbc);

        // Update Button
        btnUpdatePlatform = UITheme.createPrimaryButton("Update Platform");
        gbc.gridx = 3; gbc.gridy = 2;
        controlCard.add(btnUpdatePlatform, gbc);

        add(controlCard, BorderLayout.NORTH);

        // Center Table showing current itinerary and platforms
        String[] columns = {"Stop #", "Station Code", "Station Name", "Arrival", "Departure", "Assigned Platform"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblPlatforms = new JTable(tableModel);
        tblPlatforms.setFont(UITheme.FONT_BODY);
        add(new JScrollPane(tblPlatforms), BorderLayout.CENTER);

        // Status Bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(Color.WHITE);
        lblStatus = new JLabel(" Ready.");
        lblStatus.setFont(UITheme.FONT_BODY);
        statusBar.add(lblStatus);
        add(statusBar, BorderLayout.SOUTH);

        // Event Listeners
        cmbTrains.addActionListener(e -> {
            Train t = (Train) cmbTrains.getSelectedItem();
            if (t != null) {
                loadRouteForTrain(t.getId());
            }
        });

        btnUpdatePlatform.addActionListener(e -> performPlatformUpdate());
    }

    private void loadTrains() {
        new SwingWorker<List<Train>, Void>() {
            @Override
            protected List<Train> doInBackground() throws Exception {
                return trainService.getAllActiveTrains();
            }

            @Override
            protected void done() {
                try {
                    List<Train> trains = get();
                    cmbTrains.removeAllItems();
                    for (Train t : trains) {
                        cmbTrains.addItem(t);
                    }
                } catch (Exception ex) {
                    lblStatus.setText("Failed loading trains: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadRouteForTrain(long trainId) {
        lblStatus.setText("Loading route...");
        tableModel.setRowCount(0);

        new SwingWorker<List<RouteStop>, Void>() {
            @Override
            protected List<RouteStop> doInBackground() throws Exception {
                return trainService.getRouteForTrain(trainId);
            }

            @Override
            protected void done() {
                try {
                    List<RouteStop> stops = get();
                    cmbStops.removeAllItems();
                    for (RouteStop s : stops) {
                        cmbStops.addItem(s);
                        tableModel.addRow(new Object[]{
                                s.getStopSequence(),
                                s.getStation().getStationCode(),
                                s.getStation().getStationName(),
                                s.getArrivalTime() != null ? s.getArrivalTime().toString() : "Origin",
                                s.getDepartureTime() != null ? s.getDepartureTime().toString() : "Dest",
                                "Platform " + s.getScheduledPlatform()
                        });
                    }
                    lblStatus.setText("Loaded " + stops.size() + " stops.");
                } catch (Exception ex) {
                    lblStatus.setText("Failed loading route: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void performPlatformUpdate() {
        Train train = (Train) cmbTrains.getSelectedItem();
        RouteStop stop = (RouteStop) cmbStops.getSelectedItem();
        int newPlatform = (Integer) spinPlatform.getValue();

        if (train == null || stop == null) {
            JOptionPane.showMessageDialog(this, "Please select both a train and a station stop.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnUpdatePlatform.setEnabled(false);
        lblStatus.setText("Updating platform...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                adminService.updatePlatform(train.getId(), stop.getStation().getId(), newPlatform);
                return null;
            }

            @Override
            protected void done() {
                btnUpdatePlatform.setEnabled(true);
                try {
                    get();
                    JOptionPane.showMessageDialog(PlatformManagementPanel.this,
                            String.format("Platform for %s at %s updated to Platform %d.",
                                    train.getTrainNumber(), stop.getStation().getStationCode(), newPlatform),
                            "Platform Updated",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadRouteForTrain(train.getId());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PlatformManagementPanel.this,
                            "Failed to update platform: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
