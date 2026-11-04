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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class TrainSearchPanel extends JPanel {

    @FunctionalInterface
    public interface BookingTransitionListener {
        void onProceedToBook(Train train, Station fromStation, Station toStation, LocalDate journeyDate, int distanceKm);
    }

    private static final Station ALL_STATIONS = new Station(0L, "ALL", "-- All Stations (Fleet View) --", "All", "All");

    private final TrainService trainService;
    private final BiConsumer<Train, LocalDate> onProceedToBook;
    private final BookingTransitionListener bookingTransitionListener;

    private JComboBox<Station> cmbSource;
    private JComboBox<Station> cmbDest;
    private JTextField txtJourneyDate;
    private JTextField txtQuickFilter;
    private JTable tblResults;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JButton btnSearch;
    private JButton btnShowAll;
    private JButton btnBookNow;
    private JLabel lblStatus;

    private List<TrainSearchResult> currentResults = new ArrayList<>();

    public TrainSearchPanel(TrainService trainService, BiConsumer<Train, LocalDate> onProceedToBook) {
        this.trainService = trainService;
        this.onProceedToBook = onProceedToBook;
        this.bookingTransitionListener = null;
        initUI();
        loadStationsAndInitialFleet();
    }

    public TrainSearchPanel(TrainService trainService, BookingTransitionListener bookingListener) {
        this.trainService = trainService;
        this.onProceedToBook = null;
        this.bookingTransitionListener = bookingListener;
        initUI();
        loadStationsAndInitialFleet();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(248, 250, 252));

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblSrc = new JLabel("From Station:");
        lblSrc.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        searchCard.add(lblSrc, gbc);

        cmbSource = new JComboBox<>();
        cmbSource.setFont(UITheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = 1;
        searchCard.add(cmbSource, gbc);

        JLabel lblDst = new JLabel("To Station:");
        lblDst.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.3;
        searchCard.add(lblDst, gbc);

        cmbDest = new JComboBox<>();
        cmbDest.setFont(UITheme.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 1;
        searchCard.add(cmbDest, gbc);

        JLabel lblDate = new JLabel("Journey Date (YYYY-MM-DD):");
        lblDate.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.2;
        searchCard.add(lblDate, gbc);

        txtJourneyDate = new JTextField(LocalDate.now().plusDays(1).toString(), 10);
        txtJourneyDate.setFont(UITheme.FONT_BODY);
        gbc.gridx = 2; gbc.gridy = 1;
        searchCard.add(txtJourneyDate, gbc);

        btnSearch = UITheme.createPrimaryButton("Search / Filter");
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.1;
        searchCard.add(btnSearch, gbc);

        btnShowAll = UITheme.createSecondaryButton("Show All Trains");
        gbc.gridx = 4; gbc.gridy = 1; gbc.weightx = 0.1;
        searchCard.add(btnShowAll, gbc);

        topContainer.add(searchCard);
        topContainer.add(Box.createVerticalStrut(6));

        JPanel quickBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        quickBar.setBackground(Color.WHITE);
        quickBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JLabel lblQuick = new JLabel("Quick Search (Train / Station / City):");
        lblQuick.setFont(UITheme.FONT_BODY_BOLD);
        quickBar.add(lblQuick);

        txtQuickFilter = new JTextField(22);
        txtQuickFilter.setFont(UITheme.FONT_BODY);
        txtQuickFilter.putClientProperty("JTextField.placeholderText", "e.g. 12952, Rajdhani, Mumbai, Howrah...");
        quickBar.add(txtQuickFilter);

        topContainer.add(quickBar);
        add(topContainer, BorderLayout.NORTH);

        String[] columns = {
                "Train No", "Train Name", "Type", "From Station", "To Station",
                "Departure", "Arrival", "Distance", "Available Seats (2A/3A/SL/CC)", "Min Fare"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblResults = new JTable(tableModel);
        tblResults.setFont(UITheme.FONT_BODY);
        tblResults.setRowHeight(28);
        tblResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        rowSorter = new TableRowSorter<>(tableModel);
        tblResults.setRowSorter(rowSorter);

        tblResults.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = tblResults.getSelectedRow() >= 0;
            btnBookNow.setEnabled(selected);
        });

        tblResults.getColumnModel().getColumn(0).setPreferredWidth(85);
        tblResults.getColumnModel().getColumn(1).setPreferredWidth(190);
        tblResults.getColumnModel().getColumn(2).setPreferredWidth(130);
        tblResults.getColumnModel().getColumn(3).setPreferredWidth(150);
        tblResults.getColumnModel().getColumn(4).setPreferredWidth(150);
        tblResults.getColumnModel().getColumn(5).setPreferredWidth(80);
        tblResults.getColumnModel().getColumn(6).setPreferredWidth(80);
        tblResults.getColumnModel().getColumn(7).setPreferredWidth(85);
        tblResults.getColumnModel().getColumn(8).setPreferredWidth(210);
        tblResults.getColumnModel().getColumn(9).setPreferredWidth(95);

        JScrollPane scrollPane = new JScrollPane(tblResults);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));

        lblStatus = new JLabel(" Loading all available trains across the network...");
        lblStatus.setFont(UITheme.FONT_BODY);
        bottomBar.add(lblStatus, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);
        btnBookNow = UITheme.createSuccessButton("Book Selected Train");
        btnBookNow.setEnabled(false);
        rightActions.add(btnBookNow);

        bottomBar.add(rightActions, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> performSearch());
        btnShowAll.addActionListener(e -> {
            cmbSource.setSelectedItem(ALL_STATIONS);
            cmbDest.setSelectedItem(ALL_STATIONS);
            txtQuickFilter.setText("");
            loadAllTrains();
        });

        txtQuickFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilter(); }

            private void applyFilter() {
                String text = txtQuickFilter.getText().trim();
                if (text.isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
                }
            }
        });

        btnBookNow.addActionListener(e -> {
            int viewRow = tblResults.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = tblResults.convertRowIndexToModel(viewRow);
                if (modelRow >= 0 && modelRow < currentResults.size()) {
                    TrainSearchResult result = currentResults.get(modelRow);
                    Train train = result.getTrain();

                    LocalDate journeyDate;
                    try {
                        journeyDate = LocalDate.parse(txtJourneyDate.getText().trim());
                    } catch (Exception ex) {
                        journeyDate = LocalDate.now().plusDays(1);
                    }

                    if (bookingTransitionListener != null) {
                        bookingTransitionListener.onProceedToBook(
                                train,
                                result.getSourceStop().getStation(),
                                result.getDestStop().getStation(),
                                journeyDate,
                                result.getDistanceKm()
                        );
                    } else if (onProceedToBook != null) {
                        onProceedToBook.accept(train, journeyDate);
                    }
                }
            }
        });
    }

    private void loadStationsAndInitialFleet() {
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

                    cmbSource.addItem(ALL_STATIONS);
                    cmbDest.addItem(ALL_STATIONS);

                    for (Station s : stations) {
                        cmbSource.addItem(s);
                        cmbDest.addItem(s);
                    }

                    cmbSource.setSelectedItem(ALL_STATIONS);
                    cmbDest.setSelectedItem(ALL_STATIONS);

                    loadAllTrains();

                } catch (Exception ex) {
                    lblStatus.setText("Failed to load station list: " + ex.getMessage());
                }
            }
        }.execute();
    }

    public void loadAllTrains() {
        String dateText = txtJourneyDate.getText().trim();
        LocalDate journeyDate;
        try {
            journeyDate = LocalDate.parse(dateText);
        } catch (Exception e) {
            journeyDate = LocalDate.now().plusDays(1);
            txtJourneyDate.setText(journeyDate.toString());
        }

        final LocalDate queryDate = journeyDate;
        lblStatus.setText("Fetching all available trains in the network...");
        btnBookNow.setEnabled(false);

        new SwingWorker<List<TrainSearchResult>, Void>() {
            @Override
            protected List<TrainSearchResult> doInBackground() throws Exception {
                return trainService.getAllAvailableTrains(queryDate);
            }

            @Override
            protected void done() {
                try {
                    currentResults = get();
                    populateTableWithResults(currentResults);
                    lblStatus.setText(String.format("Displaying all %d active trains across the network. Select a train to book, or filter by route above.", currentResults.size()));
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblStatus.setText("Error loading fleet: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void performSearch() {
        Station from = (Station) cmbSource.getSelectedItem();
        Station to = (Station) cmbDest.getSelectedItem();
        String dateText = txtJourneyDate.getText().trim();

        if (from == null || to == null || (from.equals(ALL_STATIONS) && to.equals(ALL_STATIONS))) {
            loadAllTrains();
            return;
        }

        if (from.equals(ALL_STATIONS) || to.equals(ALL_STATIONS)) {
            JOptionPane.showMessageDialog(this,
                    "To filter by a route, please select both boarding and destination stations,\nor choose '-- All Stations --' to view the entire fleet.",
                    "Route Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (from.getId() == to.getId()) {
            JOptionPane.showMessageDialog(this,
                    "Boarding and destination stations cannot be the same.",
                    "Invalid Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate journeyDate;
        try {
            journeyDate = LocalDate.parse(dateText);
            if (journeyDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this,
                        "Journey date cannot be in the past.",
                        "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYY-MM-DD.",
                    "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        lblStatus.setText(String.format("Searching direct trains between %s and %s...", from.getStationCode(), to.getStationCode()));
        btnBookNow.setEnabled(false);

        new SwingWorker<List<TrainSearchResult>, Void>() {
            @Override
            protected List<TrainSearchResult> doInBackground() throws Exception {
                return trainService.searchTrains(from.getId(), to.getId(), journeyDate);
            }

            @Override
            protected void done() {
                try {
                    currentResults = get();
                    if (currentResults.isEmpty()) {
                        tableModel.setRowCount(0);
                        lblStatus.setText(String.format("No direct trains found from %s to %s on %s. Click 'Show All Trains' to view all services.",
                                from.getStationCode(), to.getStationCode(), journeyDate));
                        return;
                    }

                    populateTableWithResults(currentResults);
                    lblStatus.setText(String.format("Found %d available direct train%s between %s and %s on %s.",
                            currentResults.size(), currentResults.size() > 1 ? "s" : "", from.getStationCode(), to.getStationCode(), journeyDate));

                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblStatus.setText("Search Error: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void populateTableWithResults(List<TrainSearchResult> results) {
        tableModel.setRowCount(0);

        for (TrainSearchResult res : results) {
            Train t = res.getTrain();
            String fromName = res.getSourceStop() != null && res.getSourceStop().getStation() != null ?
                    res.getSourceStop().getStation().getStationName() + " (" + res.getSourceStop().getStation().getStationCode() + ")" : "N/A";
            String toName = res.getDestStop() != null && res.getDestStop().getStation() != null ?
                    res.getDestStop().getStation().getStationName() + " (" + res.getDestStop().getStation().getStationCode() + ")" : "N/A";

            String depTime = res.getSourceStop() != null && res.getSourceStop().getDepartureTime() != null ?
                    res.getSourceStop().getDepartureTime().toString() : "N/A";
            String arrTime = res.getDestStop() != null && res.getDestStop().getArrivalTime() != null ?
                    res.getDestStop().getArrivalTime().toString() : "N/A";

            StringBuilder seatsSummary = new StringBuilder();
            for (TravelClass tc : new TravelClass[]{TravelClass.FIRST_AC, TravelClass.SECOND_AC, TravelClass.THIRD_AC, TravelClass.SLEEPER, TravelClass.CHAIR_CAR}) {
                Integer count = res.getAvailableSeats().get(tc);
                if (count != null && count > 0) {
                    if (seatsSummary.length() > 0) seatsSummary.append(" | ");
                    seatsSummary.append(tc.getCode()).append(": ").append(count);
                }
            }
            if (seatsSummary.length() == 0) {
                seatsSummary.append("Checking...");
            }

            String minFare = res.getEstimatedFares().values().stream()
                    .filter(f -> f != null && f.compareTo(BigDecimal.ZERO) > 0)
                    .min(BigDecimal::compareTo)
                    .map(f -> "INR " + f)
                    .orElse("N/A");

            tableModel.addRow(new Object[]{
                    t.getTrainNumber(),
                    t.getTrainName(),
                    t.getTrainType() != null ? t.getTrainType().getDescription() : "Standard",
                    fromName,
                    toName,
                    depTime,
                    arrTime,
                    res.getDistanceKm() + " km",
                    seatsSummary.toString(),
                    minFare
            });
        }
    }
}
