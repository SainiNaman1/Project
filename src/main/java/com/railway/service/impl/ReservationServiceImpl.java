package com.railway.service.impl;

import com.railway.config.SessionContext;
import com.railway.dao.ReservationDAO;
import com.railway.dao.RouteDAO;
import com.railway.dao.SeatDAO;
import com.railway.dao.StationDAO;
import com.railway.dao.TrainDAO;
import com.railway.dao.impl.ReservationDAOImpl;
import com.railway.dao.impl.RouteDAOImpl;
import com.railway.dao.impl.SeatDAOImpl;
import com.railway.dao.impl.StationDAOImpl;
import com.railway.dao.impl.TrainDAOImpl;
import com.railway.database.DatabaseConnection;
import com.railway.exception.BookingException;
import com.railway.exception.CancellationException;
import com.railway.exception.RailwayException;
import com.railway.exception.UnauthorizedException;
import com.railway.model.Reservation;
import com.railway.model.ReservationPassenger;
import com.railway.model.Seat;
import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.dto.BookingRequest;
import com.railway.model.dto.FareBreakdownDTO;
import com.railway.model.enums.BookingStatus;
import com.railway.security.AuthorizationService;
import com.railway.service.FareCalculationService;
import com.railway.service.ReservationService;
import com.railway.util.PNRGenerator;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReservationServiceImpl implements ReservationService {

    @FunctionalInterface
    public interface ConnectionProvider {
        Connection getConnection() throws SQLException;
    }

    private final ReservationDAO reservationDAO;
    private final SeatDAO seatDAO;
    private final TrainDAO trainDAO;
    private final RouteDAO routeDAO;
    private final StationDAO stationDAO;
    private final FareCalculationService fareCalculationService;
    private final ConnectionProvider connectionProvider;

    public ReservationServiceImpl() {
        this(new ReservationDAOImpl(), new SeatDAOImpl(), new TrainDAOImpl(), new RouteDAOImpl(), new StationDAOImpl(), new FareCalculationServiceImpl(), DatabaseConnection.getInstance()::getConnection);
    }

    public ReservationServiceImpl(ReservationDAO reservationDAO, SeatDAO seatDAO, TrainDAO trainDAO, RouteDAO routeDAO, StationDAO stationDAO) {
        this(reservationDAO, seatDAO, trainDAO, routeDAO, stationDAO, new FareCalculationServiceImpl(), DatabaseConnection.getInstance()::getConnection);
    }

    public ReservationServiceImpl(ReservationDAO reservationDAO, SeatDAO seatDAO, TrainDAO trainDAO, RouteDAO routeDAO, StationDAO stationDAO, FareCalculationService fareCalculationService) {
        this(reservationDAO, seatDAO, trainDAO, routeDAO, stationDAO, fareCalculationService, DatabaseConnection.getInstance()::getConnection);
    }

    public ReservationServiceImpl(ReservationDAO reservationDAO, SeatDAO seatDAO, TrainDAO trainDAO, RouteDAO routeDAO, StationDAO stationDAO, FareCalculationService fareCalculationService, ConnectionProvider connectionProvider) {
        this.reservationDAO = reservationDAO;
        this.seatDAO = seatDAO;
        this.trainDAO = trainDAO;
        this.routeDAO = routeDAO;
        this.stationDAO = stationDAO;
        this.fareCalculationService = fareCalculationService;
        this.connectionProvider = connectionProvider != null ? connectionProvider : DatabaseConnection.getInstance()::getConnection;
    }

    @Override
    public Reservation bookTicket(BookingRequest request) throws BookingException {
        // 1. Syntactic Validations
        if (request == null) {
            throw new BookingException("Booking request cannot be null.");
        }
        List<ReservationPassenger> passengers = request.getPassengers();
        if (passengers == null || passengers.isEmpty() || passengers.size() > 6) {
            throw new BookingException("A reservation must include between 1 and 6 passengers.");
        }
        if (request.getJourneyDate() == null || request.getJourneyDate().isBefore(LocalDate.now())) {
            throw new BookingException("Journey date cannot be in the past. Please select today or a future date.");
        }
        for (ReservationPassenger p : passengers) {
            if (p.getPassengerName() == null || p.getPassengerName().trim().length() < 2) {
                throw new BookingException("Passenger name must contain at least 2 characters.");
            }
            if (p.getAge() < 1 || p.getAge() > 120) {
                throw new BookingException("Invalid age (" + p.getAge() + ") for passenger: " + p.getPassengerName());
            }
        }

        Connection conn = null;
        try {
            conn = connectionProvider.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            // 2. Fetch train and stations using active transaction connection
            Optional<Train> trainOpt = trainDAO.findById(conn, request.getTrainId());
            if (trainOpt.isEmpty()) {
                throw new BookingException("Train with ID " + request.getTrainId() + " not found.");
            }
            Train train = trainOpt.get();

            Optional<Station> fromOpt = stationDAO.findById(conn, request.getFromStationId());
            Optional<Station> toOpt = stationDAO.findById(conn, request.getToStationId());
            if (fromOpt.isEmpty() || toOpt.isEmpty()) {
                throw new BookingException("Origin or destination station not found.");
            }
            Station fromStation = fromOpt.get();
            Station toStation = toOpt.get();

            // 3. Validate route sequence and calculate authoritative distance (NO silent fallback!)
            int distanceKm = routeDAO.calculateDistanceKm(conn, train.getId(), fromStation.getId(), toStation.getId());
            if (distanceKm <= 0) {
                conn.rollback();
                throw new BookingException(String.format(
                        "Invalid route selection: Station %s (%s) does not precede station %s (%s) on train %s (%s).",
                        fromStation.getStationName(), fromStation.getStationCode(),
                        toStation.getStationName(), toStation.getStationCode(),
                        train.getTrainNumber(), train.getTrainName()));
            }

            // 4. Atomically lock candidate available seats using SELECT ... FOR UPDATE
            int requestedCount = passengers.size();
            List<Seat> lockedSeats = seatDAO.lockAvailableSeatsForUpdate(
                    conn,
                    train.getId(),
                    request.getJourneyDate(),
                    request.getTravelClass(),
                    requestedCount
            );

            if (lockedSeats.size() < requestedCount) {
                conn.rollback();
                throw new BookingException("Only " + lockedSeats.size() + " seat(s) available in " +
                        request.getTravelClass().getDisplayName() + ". Requested: " + requestedCount);
            }

            // 5. Generate collision-resistant unique 10-digit PNR
            String pnr = PNRGenerator.generatePNR();
            while (reservationDAO.existsByPnr(conn, pnr)) {
                pnr = PNRGenerator.generatePNR();
            }

            // 6. Compute Authoritative Total Fare via FareCalculationService
            FareBreakdownDTO fareBreakdown = fareCalculationService.calculateFareBreakdown(
                    train, request.getTravelClass(), distanceKm, requestedCount);
            BigDecimal totalFare = fareBreakdown.getTotalFare();

            // 7. Save Master Reservation
            Reservation reservation = new Reservation(
                    0L,
                    pnr,
                    request.getUserId(),
                    train.getId(),
                    request.getJourneyDate(),
                    fromStation,
                    toStation,
                    request.getTravelClass(),
                    requestedCount,
                    totalFare,
                    BookingStatus.CONFIRMED,
                    LocalDateTime.now()
            );
            reservation.setTrain(train);

            reservationDAO.save(conn, reservation);

            // 8. Assign locked seats to passengers and save
            for (int i = 0; i < requestedCount; i++) {
                ReservationPassenger passenger = passengers.get(i);
                Seat allocatedSeat = lockedSeats.get(i);

                passenger.setReservationId(reservation.getId());
                passenger.setAssignedCoach(allocatedSeat.getCoachNumber());
                passenger.setAssignedSeatNumber(allocatedSeat.getSeatNumber());
                passenger.setAssignedBerth(allocatedSeat.getBerthType());

                // Persist date-specific occupancy in seat_assignments
                seatDAO.insertSeatAssignment(
                        conn,
                        train.getId(),
                        request.getJourneyDate(),
                        allocatedSeat.getId(),
                        reservation.getId(),
                        "OCCUPIED"
                );
            }

            reservationDAO.savePassengers(conn, reservation.getId(), passengers);
            reservation.setPassengers(passengers);

            // 9. Commit Transaction
            conn.commit();
            return reservation;

        } catch (SQLException e) {
            DatabaseConnection.rollbackQuietly(conn);
            throw new BookingException("Database transaction failed during booking: " + e.getMessage(), e);
        } catch (Exception e) {
            DatabaseConnection.rollbackQuietly(conn);
            if (e instanceof BookingException be) throw be;
            throw new BookingException("Unexpected error during booking: " + e.getMessage(), e);
        } finally {
            DatabaseConnection.closeQuietly(conn);
        }
    }

    @Override
    public boolean cancelReservation(String pnr, long requestingUserId) throws CancellationException {
        if (pnr == null || pnr.isBlank()) {
            throw new CancellationException("PNR cannot be empty.");
        }

        Connection conn = null;
        try {
            conn = connectionProvider.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            Optional<Reservation> resOpt = reservationDAO.findByPnrForUpdate(conn, pnr.trim());
            if (resOpt.isEmpty()) {
                throw new CancellationException("Reservation with PNR " + pnr + " does not exist.");
            }

            Reservation res = resOpt.get();

            // Authorization: User must own the booking OR be an Administrator
            boolean isAdmin = SessionContext.isAdmin();
            if (!isAdmin && res.getUserId() != requestingUserId) {
                conn.rollback();
                throw new CancellationException("Access Denied: You are not authorized to cancel this reservation.");
            }

            if (res.getStatus() == BookingStatus.CANCELLED) {
                conn.rollback();
                throw new CancellationException("Reservation PNR " + pnr + " has already been cancelled.");
            }

            if (res.getJourneyDate().isBefore(LocalDate.now())) {
                conn.rollback();
                throw new CancellationException("Cannot cancel a past journey ticket.");
            }

            // 1. Update reservation status
            reservationDAO.updateStatus(conn, res.getId(), BookingStatus.CANCELLED);

            // 2. Atomically release assigned seats back to inventory
            seatDAO.deleteAssignmentsByReservationId(conn, res.getId());

            conn.commit();
            return true;

        } catch (SQLException e) {
            DatabaseConnection.rollbackQuietly(conn);
            throw new CancellationException("Cancellation failed due to database error: " + e.getMessage());
        } finally {
            DatabaseConnection.closeQuietly(conn);
        }
    }

    @Override
    public Reservation getReservationByPnr(String pnr, long requestingUserId) throws RailwayException {
        try {
            Optional<Reservation> resOpt = reservationDAO.findByPnr(pnr);
            if (resOpt.isEmpty()) {
                throw new RailwayException("Reservation not found for PNR: " + pnr);
            }
            Reservation res = resOpt.get();

            // Security check: only owner or admin can view ticket details
            AuthorizationService.requireReservationAccess(res.getUserId());

            // Attach train details if missing
            if (res.getTrain() == null) {
                trainDAO.findById(res.getTrainId()).ifPresent(res::setTrain);
            }
            return res;
        } catch (SQLException e) {
            throw new RailwayException("Database error looking up reservation: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> getUserReservations(long userId) throws RailwayException {
        try {
            List<Reservation> list = reservationDAO.findByUserId(userId);
            for (Reservation res : list) {
                if (res.getTrain() == null) {
                    trainDAO.findById(res.getTrainId()).ifPresent(res::setTrain);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RailwayException("Error loading your bookings: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> getAllReservations() throws RailwayException {
        AuthorizationService.requireAdmin();
        try {
            List<Reservation> list = reservationDAO.findAll();
            for (Reservation res : list) {
                if (res.getTrain() == null) {
                    trainDAO.findById(res.getTrainId()).ifPresent(res::setTrain);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RailwayException("Error auditing reservations: " + e.getMessage(), e);
        }
    }
}
