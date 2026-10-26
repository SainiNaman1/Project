package com.railway.gui.panels;

import com.railway.exception.RailwayException;
import com.railway.gui.UITheme;
import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.dto.TrainSearchResult;
import com.railway.model.enums.TravelClass;
import com.railway.service.TrainService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Allows passengers to search direct trains between two stations on a chosen journey date.
 */
public class TrainSearchPanel extends JPanel {

    private final TrainService trainService;
    private final BiConsumer<Train, LocalDate> onProceedToBook;

    private JComboBox<Station> cmbSource;
    private JComboBox<Station> cmbDest;
    private JTextField txtJourneyDate;
    private JTable tblResults;
    private DefaultTableModel tableModel;
    private JButton btnBookNow;
    private JLabel lblStatus;

    private List<TrainSearchResult> currentResults;

    public TrainSearchPanel(TrainService trainService, BiConsumer<Train, LocalDate> onProceedToBook) {
        this.trainService = trainService;
        this.onProceedToBook = onProceedToBook;
        initUI();
        loadStations();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(248, 250, 252));

        // Top Search Bar Card
        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Source Station
        JLabel lblSrc = new JLabel("From Station:");
        lblSrc.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 0; gbc.gridy = 0;
        searchCard.add(lblSrc, gbc);

        cmbSource = new JComboBox<>();
        cmbSource.setFont(UITheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = 1;
        searchCard.add(cmbSource, gbc);

        // Destination Station
        JLabel lblDst = new JLabel("To Station:");
        lblDst.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 1; gbc.gridy = 0;
        searchCard.add(lblDst, gbc);

        cmbDest = new JComboBox<>();
        cmbDest.setFont(UITheme.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 1;
        searchCard.add(cmbDest, gbc);

        // Journey Date
        JLabel lblDate = new JLabel("Journey Date (YYYY-MM-DD):");
        lblDate.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 2; gbc.gridy = 0;
        searchCard.add(lblDate, gbc);

        txtJourneyDate = new JTextField(LocalDate.now().plusDays(1).toString(), 12);
        txtJourneyDate.setFont(UITheme.FONT_BODY);
        gbc.gridx = 2; gbc.gridy = 1;
        searchCard.add(txtJourneyDate, gbc);

        // Search Button
        JButton btnSearch = UITheme.createPrimaryButton("Search Trains");
        gbc.gridx = 3; gbc.gridy = 1;
        searchCard.add(btnSearch, gbc);

        add(searchCard, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"Train No", "Train Name", "Type", "Departure", "Arrival", "Distance", "Available Seats (2A/3A/SL/CC)", "Min Fare"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblResults = new JTable(tableModel);
        tblResults.setFont(UITheme.FONT_BODY);
        tblResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblResults.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = tblResults.getSelectedRow() >= 0;
            btnBookNow.setEnabled(selected);
        });

        JScrollPane scrollPane = new JScrollPane(tblResults);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(Color.WHITE);

        lblStatus = new JLabel(" Select stations and date, then click 'Search Trains'.");
        lblStatus.setFont(UITheme.FONT_BODY);
        bottomBar.add(lblStatus, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightActions.setBackground(Color.WHITE);
        btnBookNow = UITheme.createSuccessButton("Book Selected Train");
        btnBookNow.setEnabled(false);
        rightActions.add(btnBookNow);

        bottomBar.add(rightActions, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        // Listeners
        btnSearch.addActionListener(e -> performSearch());
        btnBookNow.addActionListener(e -> {
            int row = tblResults.getSelectedRow();
            if (row >= 0 && currentResults != null && row < currentResults.size()) {
                Train train = currentResults.get(row).getTrain();
                LocalDate journeyDate = LocalDate.parse(txtJourneyDate.getText().trim());
                onProceedToBook.accept(train, journeyDate);
            }
        });
    }

    private void loadStations() {
        new SwingWorker<List<Station>, Void>() {
            @Override
            protected List<Station> doInBackground() throws Exception {
                return trainService.getAllStations();
            }

            @Override
            protected void done() {
                try {
                    List<Station> stations = get();
                    cmbSource.removeAllItems();
                    cmbDest.removeAllItems();
                    for (Station s : stations) {
                        cmbSource.addItem(s);
                        cmbDest.addItem(s);
                    }
                    if (stations.size() >= 2) {
                        cmbDest.setSelectedIndex(1);
                    }
                } catch (Exception e) {
                    lblStatus.setText("Could not load station directory: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void performSearch() {
        Station src = (Station) cmbSource.getSelectedItem();
        Station dst = (Station) cmbDest.getSelectedItem();
        String dateStr = txtJourneyDate.getText().trim();

        if (src == null || dst == null) {
            JOptionPane.showMessageDialog(this, "Please select both origin and destination stations.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate journeyDate;
        try {
            journeyDate = LocalDate.parse(dateStr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }

        lblStatus.setText("Searching available trains...");
        btnBookNow.setEnabled(false);
        tableModel.setRowCount(0);

        new SwingWorker<List<TrainSearchResult>, Void>() {
            @Override
            protected List<TrainSearchResult> doInBackground() throws Exception {
                return trainService.searchTrains(src.getId(), dst.getId(), journeyDate);
            }

            @Override
            protected void done() {
                try {
                    currentResults = get();
                    if (currentResults.isEmpty()) {
                        lblStatus.setText("No direct trains found between " + src.getStationCode() + " and " + dst.getStationCode() + ".");
                    } else {
                        lblStatus.setText("Found " + currentResults.size() + " train(s). Select a train to proceed with booking.");
                        for (TrainSearchResult res : currentResults) {
                            String dep = res.getSourceStop().getDepartureTime() != null ? res.getSourceStop().getDepartureTime().toString() : "--";
                            String arr = res.getDestStop().getArrivalTime() != null ? res.getDestStop().getArrivalTime().toString() : "--";
                            
                            // Availability summary
                            String avail = String.format("2A:%d | 3A:%d | SL:%d | CC:%d",
                                    res.getAvailableSeats().getOrDefault(TravelClass.SECOND_AC, 0),
                                    res.getAvailableSeats().getOrDefault(TravelClass.THIRD_AC, 0),
                                    res.getAvailableSeats().getOrDefault(TravelClass.SLEEPER, 0),
                                    res.getAvailableSeats().getOrDefault(TravelClass.CHAIR_CAR, 0));

                            String minFare = "Rs. " + res.getEstimatedFares().getOrDefault(TravelClass.SLEEPER, res.getEstimatedFares().getOrDefault(TravelClass.CHAIR_CAR, res.getEstimatedFares().getOrDefault(TravelClass.THIRD_AC, java.math.BigDecimal.ZERO)));

                            tableModel.addRow(new Object[]{
                                    res.getTrain().getTrainNumber(),
                                    res.getTrain().getTrainName(),
                                    res.getTrain().getTrainType().getDescription(),
                                    dep,
                                    arr,
                                    res.getDistanceKm() + " km",
                                    avail,
                                    minFare
                            });
                        }
                    }
                } catch (Exception e) {
                    lblStatus.setText("Search error: " + e.getMessage());
                    JOptionPane.showMessageDialog(TrainSearchPanel.this, e.getMessage(), "Search Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
