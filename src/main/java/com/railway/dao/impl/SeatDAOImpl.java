package com.railway.dao.impl;

import com.railway.dao.SeatDAO;
import com.railway.database.DatabaseConnection;
import com.railway.model.Seat;
import com.railway.model.enums.BerthType;
import com.railway.model.enums.TravelClass;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SeatDAOImpl implements SeatDAO {

    private final DatabaseConnection database = DatabaseConnection.getInstance();

    @Override
    public List<Seat> findSeatsByTrainAndClass(long trainId, TravelClass travelClass) throws SQLException {
        String sql = "SELECT id, train_id, travel_class, coach_number, seat_number, berth_type " +
                     "FROM seats WHERE train_id = ? AND travel_class = ? ORDER BY id ASC";

        List<Seat> seats = new ArrayList<>();
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, trainId);
            stmt.setString(2, travelClass.getCode());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapRowToSeat(rs));
                }
            }
        }
        return seats;
    }

    @Override
    public int getAvailableSeatCount(long trainId, LocalDate journeyDate, TravelClass travelClass) throws SQLException {
        String sql = "SELECT COUNT(s.id) AS available_count " +
                     "FROM seats s " +
                     "WHERE s.train_id = ? AND s.travel_class = ? " +
                     "  AND s.id NOT IN (" +
                     "      SELECT sa.seat_id FROM seat_assignments sa " +
                     "      WHERE sa.train_id = ? AND sa.journey_date = ? AND sa.status = 'OCCUPIED'" +
                     "  )";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, trainId);
            stmt.setString(2, travelClass.getCode());
            stmt.setLong(3, trainId);
            stmt.setDate(4, Date.valueOf(journeyDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("available_count");
                }
            }
        }
        return 0;
    }

    @Override
    public List<Seat> lockAvailableSeatsForUpdate(Connection conn, long trainId, LocalDate journeyDate, TravelClass travelClass, int count) throws SQLException {
        // Concurrency defense: FOR UPDATE row-locks these available seats within the caller's transaction
        String sql = "SELECT s.id, s.train_id, s.travel_class, s.coach_number, s.seat_number, s.berth_type " +
                     "FROM seats s " +
                     "WHERE s.train_id = ? AND s.travel_class = ? " +
                     "  AND s.id NOT IN (" +
                     "      SELECT sa.seat_id FROM seat_assignments sa " +
                     "      WHERE sa.train_id = ? AND sa.journey_date = ? AND sa.status = 'OCCUPIED'" +
                     "  ) " +
                     "ORDER BY s.id ASC LIMIT ? FOR UPDATE";

        List<Seat> lockedSeats = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, trainId);
            stmt.setString(2, travelClass.getCode());
            stmt.setLong(3, trainId);
            stmt.setDate(4, Date.valueOf(journeyDate));
            stmt.setInt(5, count);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lockedSeats.add(mapRowToSeat(rs));
                }
            }
        }
        return lockedSeats;
    }

    @Override
    public void insertSeatAssignment(Connection conn, long trainId, LocalDate journeyDate, long seatId, long reservationId, String status) throws SQLException {
        String sql = "INSERT INTO seat_assignments (train_id, journey_date, seat_id, reservation_id, status) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, trainId);
            stmt.setDate(2, Date.valueOf(journeyDate));
            stmt.setLong(3, seatId);
            stmt.setLong(4, reservationId);
            stmt.setString(5, status);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteAssignmentsByReservationId(Connection conn, long reservationId) throws SQLException {
        // Upon cancellation, seat assignment records are safely deleted (or updated to RELEASED)
        // allowing seats to be re-booked immediately
        String sql = "DELETE FROM seat_assignments WHERE reservation_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, reservationId);
            stmt.executeUpdate();
        }
    }

    private Seat mapRowToSeat(ResultSet rs) throws SQLException {
        return new Seat(
                rs.getLong("id"),
                rs.getLong("train_id"),
                TravelClass.fromCode(rs.getString("travel_class")),
                rs.getString("coach_number"),
                rs.getInt("seat_number"),
                BerthType.valueOf(rs.getString("berth_type"))
        );
    }
}
