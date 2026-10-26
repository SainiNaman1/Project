package com.railway.dao;

import com.railway.model.Reservation;
import com.railway.model.ReservationPassenger;
import com.railway.model.enums.BookingStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for Reservations and Ticket Records.
 * Designed to participate in external JDBC transactions.
 */
public interface ReservationDAO {
    Reservation save(Connection conn, Reservation reservation) throws SQLException;
    void savePassengers(Connection conn, long reservationId, List<ReservationPassenger> passengers) throws SQLException;
    Optional<Reservation> findByPnr(String pnr) throws SQLException;
    Optional<Reservation> findByPnrForUpdate(Connection conn, String pnr) throws SQLException;
    List<Reservation> findByUserId(long userId) throws SQLException;
    List<Reservation> findAll() throws SQLException;
    void updateStatus(Connection conn, long reservationId, BookingStatus status) throws SQLException;
    boolean existsByPnr(Connection conn, String pnr) throws SQLException;
}
