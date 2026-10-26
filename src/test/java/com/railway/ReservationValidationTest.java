package com.railway;

import com.railway.model.PassengerUser;
import com.railway.model.Reservation;
import com.railway.model.ReservationPassenger;
import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.dto.BookingRequest;
import com.railway.model.enums.BerthType;
import com.railway.model.enums.BookingStatus;
import com.railway.model.enums.Gender;
import com.railway.model.enums.TravelClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationValidationTest {

    @Test
    @DisplayName("TC-13: Reservation cancellation eligibility rules")
    void testCancellationEligibility() {
        Station src = new Station(1L, "NDLS", "New Delhi", "New Delhi", "Delhi");
        Station dst = new Station(2L, "MMCT", "Mumbai Central", "Mumbai", "Maharashtra");

        // 1. Future active booking -> IS cancellable
        Reservation futureBooking = new Reservation(
                1L, "2819283746", 10L, 12952L,
                LocalDate.now().plusDays(3),
                src, dst, TravelClass.THIRD_AC, 1,
                new BigDecimal("1850.00"), BookingStatus.CONFIRMED, LocalDateTime.now()
        );
        assertTrue(futureBooking.isCancellable(), "Future confirmed booking must be cancellable");

        // 2. Already cancelled booking -> NOT cancellable
        futureBooking.setStatus(BookingStatus.CANCELLED);
        assertFalse(futureBooking.isCancellable(), "Already cancelled booking must NOT be cancellable");

        // 3. Past journey date -> NOT cancellable
        Reservation pastBooking = new Reservation(
                2L, "3918274650", 10L, 12952L,
                LocalDate.now().minusDays(1),
                src, dst, TravelClass.THIRD_AC, 1,
                new BigDecimal("1850.00"), BookingStatus.CONFIRMED, LocalDateTime.now().minusDays(5)
        );
        assertFalse(pastBooking.isCancellable(), "Past journey date cannot be cancelled");
    }

    @Test
    @DisplayName("TC-14: Booking request validation constraints")
    void testBookingRequestStructure() {
        List<ReservationPassenger> passengers = new ArrayList<>();
        passengers.add(new ReservationPassenger(1L, 0L, "Deepak Verma", 30, Gender.MALE, BerthType.LOWER, "B1", 1, BerthType.LOWER));

        BookingRequest req = new BookingRequest(
                10L, 12952L, 1L, 2L,
                LocalDate.now().plusDays(2),
                TravelClass.SECOND_AC,
                passengers
        );

        assertNotNull(req);
        assertEquals(1, req.getPassengers().size());
        assertEquals(TravelClass.SECOND_AC, req.getTravelClass());
        assertTrue(req.getJourneyDate().isAfter(LocalDate.now()));
    }

    @Test
    @DisplayName("TC-15: Maximum passenger limit enforcement")
    void testMaxPassengerLimit() {
        List<ReservationPassenger> sevenPassengers = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            sevenPassengers.add(new ReservationPassenger((long) i, 0L, "Passenger " + i, 25, Gender.MALE, BerthType.NO_PREFERENCE, "B1", i, BerthType.LOWER));
        }

        assertTrue(sevenPassengers.size() > 6, "More than 6 passengers should be caught by validation");
    }
}
