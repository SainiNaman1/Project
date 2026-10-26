package com.railway.dao;

import com.railway.model.Seat;
import com.railway.model.enums.TravelClass;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object (DAO) interface for physical seat layouts and date-specific availability.
 */
public interface SeatDAO {
    List<Seat> findSeatsByTrainAndClass(long trainId, TravelClass travelClass) throws SQLException;
    int getAvailableSeatCount(long trainId, LocalDate journeyDate, TravelClass travelClass) throws SQLException;
    List<Seat> lockAvailableSeatsForUpdate(Connection conn, long trainId, LocalDate journeyDate, TravelClass travelClass, int count) throws SQLException;
    void insertSeatAssignment(Connection conn, long trainId, LocalDate journeyDate, long seatId, long reservationId, String status) throws SQLException;
    void deleteAssignmentsByReservationId(Connection conn, long reservationId) throws SQLException;
}
