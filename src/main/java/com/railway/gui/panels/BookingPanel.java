package com.railway.gui.panels;

import com.railway.config.SessionContext;
import com.railway.exception.BookingException;
import com.railway.gui.UITheme;
import com.railway.model.Reservation;
import com.railway.model.ReservationPassenger;
import com.railway.model.Train;
import com.railway.model.dto.BookingRequest;
import com.railway.model.enums.BerthType;
import com.railway.model.enums.Gender;
import com.railway.model.enums.TravelClass;
import com.railway.service.ReservationService;
import com.railway.service.TrainService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Passenger booking interface for specifying passenger details, accommodation class,
 * fare review, and atomic ticket reservation.
 */
public class BookingPanel extends JPanel {

    private final ReservationService reservationService;
    private final TrainService trainService;
    private final Consumer<Reservation> onBookingSuccess;

    private Train selectedTrain;
    private LocalDate journeyDate;

    private JLabel lblTrainSummary;
    private JComboBox<TravelClass> cmbClass;
    private JTable tblPassengers;
    private DefaultTableModel passengerTableModel;
    private JLabel lblTotalFare;
    private JButton btnConfirmBooking;

    public BookingPanel(ReservationService reservationService, TrainService trainService, Consumer<Reservation> onBookingSuccess) {
        this.reservationService = reservationService;
        this.trainService = trainService;
        this.onBookingSuccess = onBookingSuccess;
        initUI();
    }

    public void setBookingContext(Train train, LocalDate date) {
        this.selectedTrain = train;
        this.journeyDate = date;

        if (train != null && date != null) {
            lblTrainSummary.setText(String.format("Booking: %s (%s) | %s -> %s | Date: %s",
                    train.getTrainNumber(),
                    train.getTrainName(),
                    train.getSourceStation().getStationCode(),
                    train.getDestStation().getStationCode(),
                    date.toString()));
        }

        // Reset passenger table with 1 default passenger
        passengerTableModel.setRowCount(0);
        passengerTableModel.addRow(new Object[]{"Passenger 1", 28, Gender.MALE, BerthType.NO_PREFERENCE});
        updateFareCalculation();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(248, 250, 252));

        // Top Summary Card
        JPanel topCard = UITheme.createCardPanel();
        topCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblTrainSummary = new JLabel("Please select a train from Search Trains first.");
        lblTrainSummary.setFont(UITheme.FONT_TITLE);
        lblTrainSummary.setForeground(UITheme.PRIMARY_NAVY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        topCard.add(lblTrainSummary, gbc);

        JLabel lblClassPrompt = new JLabel("Select Travel Class:");
        lblClassPrompt.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        topCard.add(lblClassPrompt, gbc);

        cmbClass = new JComboBox<>(TravelClass.values());
        cmbClass.setSelectedItem(TravelClass.THIRD_AC);
        cmbClass.setFont(UITheme.FONT_BODY);
        cmbClass.addActionListener(e -> updateFareCalculation());
        gbc.gridx = 1; gbc.gridy = 1;
        topCard.add(cmbClass, gbc);

        add(topCard, BorderLayout.NORTH);

        // Center: Dynamic Passenger Table
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel passengerHeader = new JPanel(new BorderLayout());
        passengerHeader.setBackground(Color.WHITE);
        JLabel lblPassTitle = new JLabel("Passenger Details (1 to 6 Passengers per Booking)");
        lblPassTitle.setFont(UITheme.FONT_HEADER);
        passengerHeader.add(lblPassTitle, BorderLayout.WEST);

        JPanel passengerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        passengerButtons.setBackground(Color.WHITE);

        JButton btnAddPassenger = new JButton("+ Add Passenger");
        btnAddPassenger.setFont(UITheme.FONT_BODY);
        btnAddPassenger.addActionListener(e -> {
            if (passengerTableModel.getRowCount() >= 6) {
                JOptionPane.showMessageDialog(this, "Maximum 6 passengers allowed per reservation.", "Limit Reached", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int nextIndex = passengerTableModel.getRowCount() + 1;
            passengerTableModel.addRow(new Object[]{"Passenger " + nextIndex, 25, Gender.MALE, BerthType.NO_PREFERENCE});
            updateFareCalculation();
        });

        JButton btnRemovePassenger = new JButton("- Remove Passenger");
        btnRemovePassenger.setFont(UITheme.FONT_BODY);
        btnRemovePassenger.addActionListener(e -> {
            int count = passengerTableModel.getRowCount();
            if (count > 1) {
                passengerTableModel.removeRow(count - 1);
                updateFareCalculation();
            } else {
                JOptionPane.showMessageDialog(this, "At least 1 passenger is required.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        passengerButtons.add(btnAddPassenger);
        passengerButtons.add(btnRemovePassenger);
        passengerHeader.add(passengerButtons, BorderLayout.EAST);

        centerPanel.add(passengerHeader, BorderLayout.NORTH);

        // Table Model
        String[] columns = {"Full Name", "Age", "Gender", "Berth Preference"};
        passengerTableModel = new DefaultTableModel(columns, 0);

        tblPassengers = new JTable(passengerTableModel);
        tblPassengers.setFont(UITheme.FONT_BODY);

        // Setup Editors for Gender and Berth
        tblPassengers.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox<>(Gender.values())));
        tblPassengers.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JComboBox<>(BerthType.values())));

        centerPanel.add(new JScrollPane(tblPassengers), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Fare and Confirmation Bar
        JPanel bottomBar = UITheme.createCardPanel();
        bottomBar.setLayout(new BorderLayout());

        lblTotalFare = new JLabel("Total Fare: INR 0.00");
        lblTotalFare.setFont(UITheme.FONT_TITLE);
        lblTotalFare.setForeground(UITheme.ACCENT_GREEN);
        bottomBar.add(lblTotalFare, BorderLayout.WEST);

        btnConfirmBooking = UITheme.createSuccessButton("Confirm & Pay (Simulated)");
        btnConfirmBooking.setFont(UITheme.FONT_TITLE);
        btnConfirmBooking.addActionListener(e -> executeBooking());
        bottomBar.add(btnConfirmBooking, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private void updateFareCalculation() {
        if (selectedTrain == null) return;
        TravelClass travelClass = (TravelClass) cmbClass.getSelectedItem();
        int passengerCount = passengerTableModel.getRowCount();
        int defaultDistance = 500; // estimated average distance

        BigDecimal fare = trainService.calculateFare(selectedTrain, travelClass, defaultDistance);
        BigDecimal total = fare.multiply(BigDecimal.valueOf(passengerCount));
        lblTotalFare.setText(String.format("Total Estimated Fare: INR %s (%d Passenger%s)", total, passengerCount, passengerCount > 1 ? "s" : ""));
    }

    private void executeBooking() {
        if (selectedTrain == null || journeyDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a train and journey date from the search screen first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Stop any active cell editing in the table to capture inputs
        if (tblPassengers.isEditing()) {
            tblPassengers.getCellEditor().stopCellEditing();
        }

        int rowCount = passengerTableModel.getRowCount();
        List<ReservationPassenger> passengers = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            String name = (String) passengerTableModel.getValueAt(i, 0);
            Object ageObj = passengerTableModel.getValueAt(i, 1);
            Gender gender = (Gender) passengerTableModel.getValueAt(i, 2);
            BerthType berth = (BerthType) passengerTableModel.getValueAt(i, 3);

            if (name == null || name.trim().isBlank()) {
                JOptionPane.showMessageDialog(this, "Passenger #" + (i + 1) + " name cannot be blank.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageObj.toString().trim());
                if (age < 1 || age > 120) throw new NumberFormatException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid age (1-120) for passenger #" + (i + 1), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ReservationPassenger p = new ReservationPassenger();
            p.setPassengerName(name.trim());
            p.setAge(age);
            p.setGender(gender != null ? gender : Gender.MALE);
            p.setBerthPreference(berth != null ? berth : BerthType.NO_PREFERENCE);
            passengers.add(p);
        }

        long userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getId() : 1L;
        TravelClass travelClass = (TravelClass) cmbClass.getSelectedItem();

        BookingRequest request = new BookingRequest(
                userId,
                selectedTrain.getId(),
                selectedTrain.getSourceStation().getId(),
                selectedTrain.getDestStation().getId(),
                journeyDate,
                travelClass,
                passengers
        );

        btnConfirmBooking.setEnabled(false);
        btnConfirmBooking.setText("Processing Booking...");

        new SwingWorker<Reservation, Void>() {
            @Override
            protected Reservation doInBackground() throws Exception {
                return reservationService.bookTicket(request);
            }

            @Override
            protected void done() {
                btnConfirmBooking.setEnabled(true);
                btnConfirmBooking.setText("Confirm & Pay (Simulated)");
                try {
                    Reservation confirmed = get();
                    JOptionPane.showMessageDialog(BookingPanel.this,
                            "Booking Confirmed!\nYour PNR is: " + confirmed.getPnrNumber() +
                            "\nTotal Fare: INR " + confirmed.getTotalFare(),
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    onBookingSuccess.accept(confirmed);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(BookingPanel.this,
                            "Booking Failed: " + cause.getMessage(),
                            "Booking Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
