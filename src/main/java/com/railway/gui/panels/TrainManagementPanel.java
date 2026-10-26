package com.railway.gui.panels;

import com.railway.gui.UITheme;
import com.railway.model.MailExpressTrain;
import com.railway.model.RajdhaniExpress;
import com.railway.model.ShatabdiExpress;
import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.enums.TrainType;
import com.railway.service.AdminService;
import com.railway.service.TrainService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;
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
 * Administrative panel for managing train fleet entries and toggling operational statuses.
 */
public class TrainManagementPanel extends JPanel {

    private final AdminService adminService;
    private final TrainService trainService;

    private JTextField txtTrainNumber;
    private JTextField txtTrainName;
    private JComboBox<TrainType> cmbTrainType;
    private JComboBox<Station> cmbSource;
    private JComboBox<Station> cmbDest;
    private JTextField txtBaseFare;
    private JButton btnAddTrain;
    private JButton btnToggleStatus;
    private JTable tblTrains;
    private DefaultTableModel tableModel;
    private JLabel lblStatus;
    private List<Train> currentFleet;

    public TrainManagementPanel(AdminService adminService, TrainService trainService) {
        this.adminService = adminService;
        this.trainService = trainService;
        initUI();
        loadStationsAndFleet();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(248, 250, 252));

        // Top Form Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = UITheme.createHeaderLabel("Fleet Management: Add Train");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 6;
        formCard.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        // Fields
        txtTrainNumber = addField(formCard, "Train Number:", 0, 1, gbc);
        txtTrainName = addField(formCard, "Train Name:", 1, 1, gbc);

        JLabel lblType = new JLabel("Train Type:");
        lblType.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 2; gbc.gridy = 1;
        formCard.add(lblType, gbc);

        cmbTrainType = new JComboBox<>(TrainType.values());
        cmbTrainType.setFont(UITheme.FONT_BODY);
        gbc.gridx = 2; gbc.gridy = 2;
        formCard.add(cmbTrainType, gbc);

        JLabel lblSrc = new JLabel("Origin Station:");
        lblSrc.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 3; gbc.gridy = 1;
        formCard.add(lblSrc, gbc);

        cmbSource = new JComboBox<>();
        cmbSource.setFont(UITheme.FONT_BODY);
        gbc.gridx = 3; gbc.gridy = 2;
        formCard.add(cmbSource, gbc);

        JLabel lblDst = new JLabel("Dest Station:");
        lblDst.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 4; gbc.gridy = 1;
        formCard.add(lblDst, gbc);

        cmbDest = new JComboBox<>();
        cmbDest.setFont(UITheme.FONT_BODY);
        gbc.gridx = 4; gbc.gridy = 2;
        formCard.add(cmbDest, gbc);

        txtBaseFare = addField(formCard, "Base Fare/km (INR):", 5, 1, gbc);
        txtBaseFare.setText("1.50");

        // Action Button
        btnAddTrain = UITheme.createPrimaryButton("+ Register Train");
        gbc.gridx = 5; gbc.gridy = 3;
        formCard.add(btnAddTrain, gbc);

        add(formCard, BorderLayout.NORTH);

        // Center Fleet Table
        String[] columns = {"ID", "Train No", "Train Name", "Type", "Origin", "Destination", "Base Fare/km", "Operational"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblTrains = new JTable(tableModel);
        tblTrains.setFont(UITheme.FONT_BODY);
        tblTrains.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTrains.getSelectionModel().addListSelectionListener(e -> btnToggleStatus.setEnabled(tblTrains.getSelectedRow() >= 0));

        add(new JScrollPane(tblTrains), BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(Color.WHITE);

        lblStatus = new JLabel(" Fleet management console.");
        lblStatus.setFont(UITheme.FONT_BODY);
        bottomBar.add(lblStatus, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightActions.setBackground(Color.WHITE);

        btnToggleStatus = UITheme.createDangerButton("Toggle Active/Inactive");
        btnToggleStatus.setEnabled(false);
        btnToggleStatus.addActionListener(e -> toggleSelectedTrainStatus());
        rightActions.add(btnToggleStatus);

        bottomBar.add(rightActions, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        btnAddTrain.addActionListener(e -> performAddTrain());
    }

    private JTextField addField(JPanel panel, String labelText, int col, int row, GridBagConstraints gbc) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = col; gbc.gridy = row;
        panel.add(lbl, gbc);

        JTextField field = new JTextField(12);
        field.setFont(UITheme.FONT_BODY);
        gbc.gridx = col; gbc.gridy = row + 1;
        panel.add(field, gbc);
        return field;
    }

    private void loadStationsAndFleet() {
        new SwingWorker<Void, Void>() {
            private List<Station> stations;
            private List<Train> trains;

            @Override
            protected Void doInBackground() throws Exception {
                stations = trainService.getAllStations();
                trains = trainService.getAllActiveTrains();
                return null;
            }

            @Override
            protected void done() {
                try {
                    cmbSource.removeAllItems();
                    cmbDest.removeAllItems();
                    for (Station s : stations) {
                        cmbSource.addItem(s);
                        cmbDest.addItem(s);
                    }
                    if (stations.size() >= 2) cmbDest.setSelectedIndex(1);

                    currentFleet = trains;
                    tableModel.setRowCount(0);
                    for (Train t : trains) {
                        tableModel.addRow(new Object[]{
                                t.getId(),
                                t.getTrainNumber(),
                                t.getTrainName(),
                                t.getTrainType().getDescription(),
                                t.getSourceStation().getStationCode(),
                                t.getDestStation().getStationCode(),
                                "INR " + t.getBaseFarePerKm(),
                                t.isActive() ? "ACTIVE" : "INACTIVE"
                        });
                    }
                } catch (Exception ex) {
                    lblStatus.setText("Error loading fleet: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void performAddTrain() {
        String number = txtTrainNumber.getText().trim();
        String name = txtTrainName.getText().trim();
        TrainType type = (TrainType) cmbTrainType.getSelectedItem();
        Station src = (Station) cmbSource.getSelectedItem();
        Station dst = (Station) cmbDest.getSelectedItem();
        String fareStr = txtBaseFare.getText().trim();

        if (number.isBlank() || name.isBlank() || src == null || dst == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all train fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal baseFare;
        try {
            baseFare = new BigDecimal(fareStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid base fare number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Train newTrain = switch (type) {
            case RAJDHANI -> new RajdhaniExpress(0L, number, name, src, dst, baseFare, true);
            case SHATABDI -> new ShatabdiExpress(0L, number, name, src, dst, baseFare, true);
            default -> new MailExpressTrain(0L, number, name, src, dst, baseFare, true);
        };

        new SwingWorker<Train, Void>() {
            @Override
            protected Train doInBackground() throws Exception {
                return adminService.addTrain(newTrain, null);
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(TrainManagementPanel.this, "Train " + number + " registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    txtTrainNumber.setText("");
                    txtTrainName.setText("");
                    loadStationsAndFleet();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TrainManagementPanel.this, "Failed to register train: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void toggleSelectedTrainStatus() {
        int row = tblTrains.getSelectedRow();
        if (row >= 0 && currentFleet != null && row < currentFleet.size()) {
            Train t = currentFleet.get(row);
            boolean newStatus = !t.isActive();

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    adminService.toggleTrainStatus(t.getId(), newStatus);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadStationsAndFleet();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(TrainManagementPanel.this, "Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }
}
