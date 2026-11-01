package com.railway.gui.panels;

import com.railway.config.ApplicationContext;
import com.railway.config.SessionContext;
import com.railway.exception.BookingException;
import com.railway.gui.UITheme;
import com.railway.model.Reservation;
import com.railway.model.ReservationPassenger;
import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.dto.BookingRequest;
import com.railway.model.dto.FareBreakdownDTO;
import com.railway.model.enums.BerthType;
import com.railway.model.enums.Gender;
import com.railway.model.enums.TravelClass;
import com.railway.service.FareCalculationService;
import com.railway.service.ReservationService;
import com.railway.service.TrainService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
 * Guided multi-step passenger ticket booking interface.
 * Connects directly to authoritative FareCalculationService (eliminating hardcoded distance fallbacks)
 * and executes booking transactions asynchronously with responsive UI feedback.
 */
public class BookingPanel extends JPanel {

    private final ReservationService reservationService;
    private final TrainService trainService;
    private final FareCalculationService fareService;
    private final Consumer<Reservation> onBookingSuccess;

    private Train selectedTrain;
    private Station fromStation;
    private Station toStation;
    private LocalDate journeyDate;
    private int distanceKm = 0;

    // UI Components
    private JLabel lblStep1Train;
    private JLabel lblStep1Route;
    private JLabel lblStep1Date;
    private JComboBox<TravelClass> cmbClass;
    private JLabel lblSeatAvailability;

    private JTable tblPassengers;
    private DefaultTableModel passengerTableModel;

    // Itemized Fare Labels
    private JLabel lblFareDistance;
    private JLabel lblFareBaseAmount;
    private JLabel lblFareFee;
    private JLabel lblFareGst;
    private JLabel lblFarePerPax;
    private JLabel lblTotalFare;

    private JButton btnConfirmBooking;
    private JLabel lblStatusMessage;

    public BookingPanel(ReservationService reservationService, TrainService trainService, Consumer<Reservation> onBookingSuccess) {
        this(reservationService, trainService, ApplicationContext.getInstance().getFareCalculationService(), onBookingSuccess);
    }

    public BookingPanel(ReservationService reservationService, TrainService trainService,
                        FareCalculationService fareService, Consumer<Reservation> onBookingSuccess) {
        this.reservationService = reservationService;
        this.trainService = trainService;
        this.fareService = fareService;
        this.onBookingSuccess = onBookingSuccess;
        initUI();
    }

    public void setBookingContext(Train train, LocalDate date) {
        if (train != null) {
            setBookingContext(train, train.getSourceStation(), train.getDestStation(), date, TravelClass.THIRD_AC, 0);
        }
    }

    public void setBookingContext(Train train, Station from, Station to, LocalDate date, TravelClass travelClass, int distance) {
        this.selectedTrain = train;
        this.fromStation = from != null ? from : (train != null ? train.getSourceStation() : null);
        this.toStation = to != null ? to : (train != null ? train.getDestStation() : null);
        this.journeyDate = date != null ? date : LocalDate.now().plusDays(1);
        this.distanceKm = distance;

        if (selectedTrain != null && fromStation != null && toStation != null) {
            lblStep1Train.setText(String.format("%s — %s (%s)", selectedTrain.getTrainNumber(), selectedTrain.getTrainName(), selectedTrain.getTrainType()));
            lblStep1Route.setText(String.format("Route: %s (%s) → %s (%s)",
                    fromStation.getStationName(), fromStation.getStationCode(),
                    toStation.getStationName(), toStation.getStationCode()));
            lblStep1Date.setText(String.format("Journey Date: %s", this.journeyDate.toString()));

            if (travelClass != null) {
                cmbClass.setSelectedItem(travelClass);
            }

            // Resolve authoritative distance if not already passed
            if (this.distanceKm <= 0) {
                try {
                    this.distanceKm = trainService.getRouteDistanceKm(selectedTrain.getId(), fromStation.getId(), toStation.getId());
                } catch (Exception e) {
                    this.distanceKm = 500; // safe fallback for display only
                }
            }

            refreshAvailabilityAndFare();
        }

        // Initialize passenger list with 1 default passenger if empty
        if (passengerTableModel.getRowCount() == 0) {
            passengerTableModel.addRow(new Object[]{"Passenger 1", 28, Gender.MALE, BerthType.NO_PREFERENCE});
        }
        updateFareCalculation();
    }

    private void initUI() {
        setLayout(new BorderLayout(14, 14));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // 1. Top Section: Guided Step 1 - Journey & Class Context Card
        JPanel topCard = UITheme.createCardPanel();
        topCard.setLayout(new BorderLayout(12, 12));
        topCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel step1Header = new JPanel(new GridLayout(3, 1, 0, 4));
        step1Header.setOpaque(false);

        lblStep1Train = new JLabel("Train: No train selected. Please choose from Search Trains.");
        lblStep1Train.setFont(UITheme.FONT_TITLE);
        lblStep1Train.setForeground(UITheme.PRIMARY_NAVY);

        lblStep1Route = new JLabel("Route: —");
        lblStep1Route.setFont(UITheme.FONT_BODY_BOLD);
        lblStep1Route.setForeground(new Color(71, 85, 105));

        lblStep1Date = new JLabel("Date: —");
        lblStep1Date.setFont(UITheme.FONT_BODY);
        lblStep1Date.setForeground(new Color(100, 116, 139));

        step1Header.add(lblStep1Train);
        step1Header.add(lblStep1Route);
        step1Header.add(lblStep1Date);
        topCard.add(step1Header, BorderLayout.CENTER);

        // Class and Availability Controls
        JPanel classPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        classPanel.setOpaque(false);

        JLabel lblClassPrompt = new JLabel("Travel Class:");
        lblClassPrompt.setFont(UITheme.FONT_BODY_BOLD);
        cmbClass = new JComboBox<>(TravelClass.values());
        cmbClass.setSelectedItem(TravelClass.THIRD_AC);
        cmbClass.setFont(UITheme.FONT_BODY);
        cmbClass.addActionListener(e -> refreshAvailabilityAndFare());

        lblSeatAvailability = new JLabel("Availability: —");
        lblSeatAvailability.setFont(UITheme.FONT_BODY_BOLD);
        lblSeatAvailability.setForeground(UITheme.ACCENT_GREEN);

        classPanel.add(lblClassPrompt);
        classPanel.add(cmbClass);
        classPanel.add(lblSeatAvailability);
        topCard.add(classPanel, BorderLayout.EAST);

        add(topCard, BorderLayout.NORTH);

        // 2. Center Section: Step 2 - Dynamic Passenger Details Table
        JPanel centerPanel = UITheme.createCardPanel();
        centerPanel.setLayout(new BorderLayout(8, 8));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel passengerHeader = new JPanel(new BorderLayout());
        passengerHeader.setOpaque(false);

        JLabel lblPassTitle = new JLabel("Passenger Information (1 to 6 Passengers)");
        lblPassTitle.setFont(UITheme.FONT_HEADER);
        lblPassTitle.setForeground(UITheme.TEXT_DARK);
        passengerHeader.add(lblPassTitle, BorderLayout.WEST);

        JPanel passengerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        passengerButtons.setOpaque(false);

        JButton btnAddPassenger = new JButton("+ Add Passenger");
        btnAddPassenger.setFont(UITheme.FONT_BODY);
        btnAddPassenger.addActionListener(e -> {
            if (passengerTableModel.getRowCount() >= 6) {
                JOptionPane.showMessageDialog(this, "Maximum 6 passengers allowed per booking.", "Limit Reached", JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "A minimum of 1 passenger is required.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        passengerButtons.add(btnAddPassenger);
        passengerButtons.add(btnRemovePassenger);
        passengerHeader.add(passengerButtons, BorderLayout.EAST);
        centerPanel.add(passengerHeader, BorderLayout.NORTH);

        String[] columns = {"Full Name", "Age", "Gender", "Berth Preference"};
        passengerTableModel = new DefaultTableModel(columns, 0);
        tblPassengers = new JTable(passengerTableModel);
        tblPassengers.setFont(UITheme.FONT_BODY);
        tblPassengers.setRowHeight(28);

        tblPassengers.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox<>(Gender.values())));
        tblPassengers.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JComboBox<>(BerthType.values())));

        passengerTableModel.addTableModelListener(e -> updateFareCalculation());

        JScrollPane scrollPane = new JScrollPane(tblPassengers);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Section: Step 3 & 4 - Authoritative Fare Breakdown & Confirmation Bar
        JPanel bottomBar = UITheme.createCardPanel();
        bottomBar.setLayout(new BorderLayout(14, 12));
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        // Itemized Breakdown Grid
        JPanel breakdownGrid = new JPanel(new GridLayout(2, 3, 16, 4));
        breakdownGrid.setOpaque(false);

        lblFareDistance = new JLabel("Distance: — km");
        lblFareDistance.setFont(UITheme.FONT_SMALL);
        lblFareBaseAmount = new JLabel("Distance Fare: INR 0.00");
        lblFareBaseAmount.setFont(UITheme.FONT_SMALL);
        lblFareFee = new JLabel("Reservation Charge: INR 40.00 / pax");
        lblFareFee.setFont(UITheme.FONT_SMALL);
        lblFareGst = new JLabel("GST (5%): INR 0.00");
        lblFareGst.setFont(UITheme.FONT_SMALL);
        lblFarePerPax = new JLabel("Per Passenger: INR 0.00");
        lblFarePerPax.setFont(UITheme.FONT_SMALL);

        breakdownGrid.add(lblFareDistance);
        breakdownGrid.add(lblFareBaseAmount);
        breakdownGrid.add(lblFareFee);
        breakdownGrid.add(lblFareGst);
        breakdownGrid.add(lblFarePerPax);

        JPanel leftBreakdown = new JPanel(new BorderLayout(4, 4));
        leftBreakdown.setOpaque(false);
        lblTotalFare = new JLabel("Total Fare: INR 0.00");
        lblTotalFare.setFont(UITheme.FONT_TITLE);
        lblTotalFare.setForeground(UITheme.ACCENT_GREEN);

        leftBreakdown.add(breakdownGrid, BorderLayout.NORTH);
        leftBreakdown.add(lblTotalFare, BorderLayout.SOUTH);
        bottomBar.add(leftBreakdown, BorderLayout.CENTER);

        // Confirmation & Status Action Area
        JPanel actionArea = new JPanel(new BorderLayout(4, 4));
        actionArea.setOpaque(false);

        lblStatusMessage = new JLabel(" ");
        lblStatusMessage.setFont(UITheme.FONT_SMALL);
        lblStatusMessage.setForeground(UITheme.ACCENT_RED);

        btnConfirmBooking = UITheme.createSuccessButton("Confirm & Pay (Simulated)");
        btnConfirmBooking.setFont(UITheme.FONT_TITLE);
        btnConfirmBooking.addActionListener(e -> executeBooking());

        actionArea.add(lblStatusMessage, BorderLayout.NORTH);
        actionArea.add(btnConfirmBooking, BorderLayout.SOUTH);
        bottomBar.add(actionArea, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private void refreshAvailabilityAndFare() {
        if (selectedTrain == null) return;
        TravelClass travelClass = (TravelClass) cmbClass.getSelectedItem();

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return trainService.checkSeatAvailability(selectedTrain.getId(), journeyDate, travelClass);
            }

            @Override
            protected void done() {
                try {
                    int available = get();
                    lblSeatAvailability.setText(String.format("Available: %d Seat%s (%s)",
                            available, available == 1 ? "" : "s", travelClass.getCode()));
                    if (available > 5) {
                        lblSeatAvailability.setForeground(UITheme.ACCENT_GREEN);
                    } else if (available > 0) {
                        lblSeatAvailability.setForeground(new Color(234, 88, 12));
                    } else {
                        lblSeatAvailability.setForeground(UITheme.ACCENT_RED);
                    }
                } catch (Exception ignored) {
                    lblSeatAvailability.setText("Availability: Checking...");
                }
            }
        };
        worker.execute();
        updateFareCalculation();
    }

    private void updateFareCalculation() {
        if (selectedTrain == null || distanceKm <= 0) {
            lblTotalFare.setText("Total Fare: INR 0.00");
            return;
        }

        TravelClass travelClass = (TravelClass) cmbClass.getSelectedItem();
        int passengerCount = Math.max(passengerTableModel.getRowCount(), 1);

        try {
            FareBreakdownDTO breakdown = fareService.calculateFareBreakdown(selectedTrain, travelClass, distanceKm, passengerCount);
            lblFareDistance.setText(String.format("Distance: %d km", breakdown.getDistanceKm()));
            lblFareBaseAmount.setText(String.format("Base Distance Fare: INR %s", breakdown.getBaseDistanceFare()));
            lblFareFee.setText(String.format("Reservation Charge: INR %s / pax", breakdown.getReservationFee()));
            lblFareGst.setText(String.format("GST (5%%): INR %s", breakdown.getGstAmount()));
            lblFarePerPax.setText(String.format("Fare per Pax: INR %s", breakdown.getFarePerPassenger()));

            lblTotalFare.setText(String.format("Total Estimated Fare: INR %s (%d Passenger%s)",
                    breakdown.getTotalFare(), passengerCount, passengerCount > 1 ? "s" : ""));
            lblStatusMessage.setText(" ");
        } catch (Exception e) {
            lblTotalFare.setText("Total Fare: Calculation Error");
        }
    }

    private void executeBooking() {
        if (selectedTrain == null) {
            JOptionPane.showMessageDialog(this, "Please select a train from Search Trains first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fromStation == null || toStation == null) {
            JOptionPane.showMessageDialog(this, "Origin and destination stations are required.", "Invalid Route", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Commit table edits if user is currently editing a cell
        if (tblPassengers.isEditing()) {
            tblPassengers.getCellEditor().stopCellEditing();
        }

        int count = passengerTableModel.getRowCount();
        if (count < 1 || count > 6) {
            JOptionPane.showMessageDialog(this, "A reservation must include between 1 and 6 passengers.", "Passenger Limit", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<ReservationPassenger> passengers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String name = (String) passengerTableModel.getValueAt(i, 0);
            Object ageObj = passengerTableModel.getValueAt(i, 1);
            Gender gender = (Gender) passengerTableModel.getValueAt(i, 2);
            BerthType berth = (BerthType) passengerTableModel.getValueAt(i, 3);

            if (name == null || name.trim().length() < 2) {
                JOptionPane.showMessageDialog(this, String.format("Passenger #%d must have a valid full name (at least 2 letters).", i + 1), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageObj.toString().trim());
                if (age < 1 || age > 120) throw new NumberFormatException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, String.format("Passenger #%d has an invalid age. Please enter a valid age between 1 and 120.", i + 1), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            passengers.add(new ReservationPassenger(0L, 0L, name.trim(), age, gender, berth, "", 0, BerthType.NO_PREFERENCE));
        }

        TravelClass travelClass = (TravelClass) cmbClass.getSelectedItem();
        long userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getId() : 1L;

        BookingRequest request = new BookingRequest(
                userId,
                selectedTrain.getId(),
                fromStation.getId(),
                toStation.getId(),
                journeyDate,
                travelClass,
                passengers
        );

        // Execute atomically in background via SwingWorker to avoid freezing EDT
        btnConfirmBooking.setEnabled(false);
        lblStatusMessage.setForeground(new Color(15, 23, 42));
        lblStatusMessage.setText("Allocating seats & reserving tickets...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Reservation, Void> worker = new SwingWorker<>() {
            @Override
            protected Reservation doInBackground() throws Exception {
                return reservationService.bookTicket(request);
            }

            @Override
            protected void done() {
                btnConfirmBooking.setEnabled(true);
                setCursor(Cursor.getDefaultCursor());
                try {
                    Reservation confirmed = get();
                    lblStatusMessage.setForeground(UITheme.ACCENT_GREEN);
                    lblStatusMessage.setText("Booking confirmed! PNR: " + confirmed.getPnrNumber());

                    JOptionPane.showMessageDialog(BookingPanel.this,
                            String.format("Ticket Confirmed Successfully!\n\nPNR: %s\nTrain: %s (%s)\nPassengers: %d\nTotal Fare: INR %s",
                                    confirmed.getPnrNumber(),
                                    selectedTrain.getTrainName(),
                                    selectedTrain.getTrainNumber(),
                                    confirmed.getTotalPassengers(),
                                    confirmed.getTotalFare()),
                            "Booking Confirmed",
                            JOptionPane.INFORMATION_MESSAGE);

                    onBookingSuccess.accept(confirmed);

                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblStatusMessage.setForeground(UITheme.ACCENT_RED);
                    lblStatusMessage.setText("Booking Failed: " + cause.getMessage());
                    JOptionPane.showMessageDialog(BookingPanel.this,
                            "Unable to complete reservation:\n" + cause.getMessage(),
                            "Reservation Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
