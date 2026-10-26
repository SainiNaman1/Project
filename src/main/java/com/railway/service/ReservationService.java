package com.railway.service;

import com.railway.exception.BookingException;
import com.railway.exception.CancellationException;
import com.railway.exception.RailwayException;
import com.railway.model.Reservation;
import com.railway.model.dto.BookingRequest;
import java.util.List;

/**
 * Service interface governing atomic ticket reservations, seat allocation concurrency,
 * and reservation cancellations.
 */
public interface ReservationService {
    Reservation bookTicket(BookingRequest request) throws BookingException;
    boolean cancelReservation(String pnr, long requestingUserId) throws CancellationException;
    Reservation getReservationByPnr(String pnr, long requestingUserId) throws RailwayException;
    List<Reservation> getUserReservations(long userId) throws RailwayException;
    List<Reservation> getAllReservations() throws RailwayException;
}
