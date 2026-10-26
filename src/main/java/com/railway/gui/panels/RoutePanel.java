package com.railway.gui.panels;

import com.railway.gui.UITheme;
import com.railway.model.RouteStop;
import com.railway.model.Train;
import com.railway.service.TrainService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Displays the complete station route itinerary, scheduled halt times,
 * and live platform numbers for a selected train.
 */
public class RoutePanel extends JPanel {

    private final TrainService trainService;
    private JComboBox<Train> cmbTrains;
    private JTable tblStops;
    private DefaultTableModel tableModel;
    private JLabel lblStatus;

    public RoutePanel(TrainService trainService) {
        this.trainService = trainService;
        initUI();
        loadTrains();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(248, 250, 252));

        // Top Filter Bar
        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 6));

        JLabel lblSelect = new JLabel("Select Train:");
        lblSelect.setFont(UITheme.FONT_BODY_BOLD);
        topBar.add(lblSelect);

        cmbTrains = new JComboBox<>();
        cmbTrains.setFont(UITheme.FONT_BODY);
        topBar.add(cmbTrains);

        lblStatus = new JLabel(" Select a train to view its route schedule.");
        lblStatus.setFont(UITheme.FONT_BODY);
        topBar.add(lblStatus);

        add(topBar, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"Stop #", "Station Code", "Station Name", "Arrival", "Departure", "Halt (min)", "Distance (km)", "Platform"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblStops = new JTable(tableModel);
        tblStops.setFont(UITheme.FONT_BODY);
        add(new JScrollPane(tblStops), BorderLayout.CENTER);

        cmbTrains.addActionListener(e -> {
            Train selected = (Train) cmbTrains.getSelectedItem();
            if (selected != null) {
                loadRoute(selected.getId());
            }
        });
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
                } catch (Exception e) {
                    lblStatus.setText("Error loading train list: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void loadRoute(long trainId) {
        lblStatus.setText("Loading itinerary...");
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
                    lblStatus.setText("Showing " + stops.size() + " stops.");
                    for (RouteStop s : stops) {
                        String arr = s.getArrivalTime() != null ? s.getArrivalTime().toString() : "Source";
                        String dep = s.getDepartureTime() != null ? s.getDepartureTime().toString() : "Destination";
                        tableModel.addRow(new Object[]{
                                s.getStopSequence(),
                                s.getStation().getStationCode(),
                                s.getStation().getStationName(),
                                arr,
                                dep,
                                s.getHaltMinutes() + " m",
                                s.getDistanceFromOriginKm() + " km",
                                "Platform " + s.getScheduledPlatform()
                        });
                    }
                } catch (Exception e) {
                    lblStatus.setText("Error loading route: " + e.getMessage());
                }
            }
        }.execute();
    }
}
