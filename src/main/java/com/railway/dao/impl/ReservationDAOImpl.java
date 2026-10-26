package com.railway.dao.impl;

import com.railway.dao.ReservationDAO;
import com.railway.database.DatabaseConnection;
import com.railway.model.Reservation;
import com.railway.model.ReservationPassenger;
import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.enums.BerthType;
import com.railway.model.enums.BookingStatus;
import com.railway.model.enums.Gender;
import com.railway.model.enums.TravelClass;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationDAOImpl implements ReservationDAO {

    private final DatabaseConnection database = DatabaseConnection.getInstance();

    @Override
    public Reservation save(Connection conn, Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservations (pnr_number, user_id, train_id, journey_date, " +
                     "from_station_id, to_station_id, travel_class, total_passengers, total_fare, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, reservation.getPnrNumber());
            stmt.setLong(2, reservation.getUserId());
            stmt.setLong(3, reservation.getTrainId());
            stmt.setDate(4, Date.valueOf(reservation.getJourneyDate()));
            stmt.setLong(5, reservation.getFromStation().getId());
            stmt.setLong(6, reservation.getToStation().getId());
            stmt.setString(7, reservation.getTravelClass().getCode());
            stmt.setInt(8, reservation.getTotalPassengers());
            stmt.setBigDecimal(9, reservation.getTotalFare());
            stmt.setString(10, reservation.getStatus().name());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    reservation.setId(rs.getLong(1));
                }
            }
            reservation.setBookingTimestamp(LocalDateTime.now());
        }
        return reservation;
    }

    @Override
    public void savePassengers(Connection conn, long reservationId, List<ReservationPassenger> passengers) throws SQLException {
        String sql = "INSERT INTO reservation_passengers (reservation_id, passenger_name, age, gender, " +
                     "berth_preference, assigned_coach, assigned_seat_number, assigned_berth) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (ReservationPassenger p : passengers) {
                stmt.setLong(1, reservationId);
                stmt.setString(2, p.getPassengerName().trim());
                stmt.setInt(3, p.getAge());
                stmt.setString(4, p.getGender().name());
                stmt.setString(5, p.getBerthPreference() != null ? p.getBerthPreference().name() : "NO_PREFERENCE");
                stmt.setString(6, p.getAssignedCoach());
                stmt.setInt(7, p.getAssignedSeatNumber());
                stmt.setString(8, p.getAssignedBerth().name());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @Override
    public Optional<Reservation> findByPnr(String pnr) throws SQLException {
        try (Connection conn = database.getConnection()) {
            return findByPnrInternal(conn, pnr, false);
        }
    }

    @Override
    public Optional<Reservation> findByPnrForUpdate(Connection conn, String pnr) throws SQLException {
        return findByPnrInternal(conn, pnr, true);
    }

    private Optional<Reservation> findByPnrInternal(Connection conn, String pnr, boolean forUpdate) throws SQLException {
        String sql = "SELECT r.id, r.pnr_number, r.user_id, r.train_id, r.journey_date, " +
                     "r.travel_class, r.total_passengers, r.total_fare, r.status, r.booking_timestamp, " +
                     "s1.id AS src_id, s1.station_code AS src_code, s1.station_name AS src_name, s1.city AS src_city, s1.state AS src_state, " +
                     "s2.id AS dst_id, s2.station_code AS dst_code, s2.station_name AS dst_name, s2.city AS dst_city, s2.state AS dst_state " +
                     "FROM reservations r " +
                     "JOIN stations s1 ON r.from_station_id = s1.id " +
                     "JOIN stations s2 ON r.to_station_id = s2.id " +
                     "WHERE r.pnr_number = ?" + (forUpdate ? " FOR UPDATE" : "");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pnr.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Reservation res = mapRowToReservation(rs);
                    res.setPassengers(loadPassengersForReservation(conn, res.getId()));
                    return Optional.of(res);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Reservation> findByUserId(long userId) throws SQLException {
        String sql = "SELECT r.id, r.pnr_number, r.user_id, r.train_id, r.journey_date, " +
                     "r.travel_class, r.total_passengers, r.total_fare, r.status, r.booking_timestamp, " +
                     "s1.id AS src_id, s1.station_code AS src_code, s1.station_name AS src_name, s1.city AS src_city, s1.state AS src_state, " +
                     "s2.id AS dst_id, s2.station_code AS dst_code, s2.station_name AS dst_name, s2.city AS dst_city, s2.state AS dst_state " +
                     "FROM reservations r " +
                     "JOIN stations s1 ON r.from_station_id = s1.id " +
                     "JOIN stations s2 ON r.to_station_id = s2.id " +
                     "WHERE r.user_id = ? ORDER BY r.booking_timestamp DESC";

        List<Reservation> list = new ArrayList<>();
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation res = mapRowToReservation(rs);
                    res.setPassengers(loadPassengersForReservation(conn, res.getId()));
                    list.add(res);
                }
            }
        }
        return list;
    }

    @Override
    public List<Reservation> findAll() throws SQLException {
        String sql = "SELECT r.id, r.pnr_number, r.user_id, r.train_id, r.journey_date, " +
                     "r.travel_class, r.total_passengers, r.total_fare, r.status, r.booking_timestamp, " +
                     "s1.id AS src_id, s1.station_code AS src_code, s1.station_name AS src_name, s1.city AS src_city, s1.state AS src_state, " +
                     "s2.id AS dst_id, s2.station_code AS dst_code, s2.station_name AS dst_name, s2.city AS dst_city, s2.state AS dst_state " +
                     "FROM reservations r " +
                     "JOIN stations s1 ON r.from_station_id = s1.id " +
                     "JOIN stations s2 ON r.to_station_id = s2.id " +
                     "ORDER BY r.booking_timestamp DESC LIMIT 100";

        List<Reservation> list = new ArrayList<>();
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Reservation res = mapRowToReservation(rs);
                res.setPassengers(loadPassengersForReservation(conn, res.getId()));
                list.add(res);
            }
        }
        return list;
    }

    @Override
    public void updateStatus(Connection conn, long reservationId, BookingStatus status) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, reservationId);
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean existsByPnr(Connection conn, String pnr) throws SQLException {
        String sql = "SELECT 1 FROM reservations WHERE pnr_number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pnr.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private List<ReservationPassenger> loadPassengersForReservation(Connection conn, long resId) throws SQLException {
        String sql = "SELECT id, passenger_name, age, gender, berth_preference, assigned_coach, assigned_seat_number, assigned_berth " +
                     "FROM reservation_passengers WHERE reservation_id = ? ORDER BY id ASC";

        List<ReservationPassenger> passengers = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, resId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    passengers.add(new ReservationPassenger(
                            rs.getLong("id"),
                            resId,
                            rs.getString("passenger_name"),
                            rs.getInt("age"),
                            Gender.valueOf(rs.getString("gender")),
                            BerthType.valueOf(rs.getString("berth_preference")),
                            rs.getString("assigned_coach"),
                            rs.getInt("assigned_seat_number"),
                            BerthType.valueOf(rs.getString("assigned_berth"))
                    ));
                }
            }
        }
        return passengers;
    }

    private Reservation mapRowToReservation(ResultSet rs) throws SQLException {
        Station src = new Station(
                rs.getLong("src_id"),
                rs.getString("src_code"),
                rs.getString("src_name"),
                rs.getString("src_city"),
                rs.getString("src_state")
        );

        Station dst = new Station(
                rs.getLong("dst_id"),
                rs.getString("dst_code"),
                rs.getString("dst_name"),
                rs.getString("dst_city"),
                rs.getString("dst_state")
        );

        Timestamp ts = rs.getTimestamp("booking_timestamp");
        LocalDateTime bookedAt = ts != null ? ts.toLocalDateTime() : null;

        return new Reservation(
                rs.getLong("id"),
                rs.getString("pnr_number"),
                rs.getLong("user_id"),
                rs.getLong("train_id"),
                rs.getDate("journey_date").toLocalDate(),
                src,
                dst,
                TravelClass.fromCode(rs.getString("travel_class")),
                rs.getInt("total_passengers"),
                rs.getBigDecimal("total_fare"),
                BookingStatus.valueOf(rs.getString("status")),
                bookedAt
        );
    }
}
