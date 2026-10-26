package com.railway;

import com.railway.model.AdminUser;
import com.railway.model.PassengerUser;
import com.railway.model.Reservation;
import com.railway.model.ReservationPassenger;
import com.railway.model.Station;
import com.railway.model.User;
import com.railway.model.enums.BerthType;
import com.railway.model.enums.BookingStatus;
import com.railway.model.enums.Gender;
import com.railway.model.enums.Role;
import com.railway.model.enums.TravelClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainModelTest {

    @Test
    @DisplayName("OOP Polymorphism: User role authorization checks")
    void testUserRolePolymorphism() {
        User passenger = new PassengerUser(1L, "rahul", "hash", "Rahul", "rahul@test.com", "9876543210", LocalDateTime.now());
        User admin = new AdminUser(2L, "admin", "hash", "Admin", "admin@test.com", "Operations", LocalDateTime.now());

        assertEquals(Role.PASSENGER, passenger.getRole());
        assertFalse(passenger.hasAdminPrivileges(), "Passenger must NOT have administrative privileges");

        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.hasAdminPrivileges(), "Admin MUST have administrative privileges");
    }

    @Test
    @DisplayName("OOP Composition: Reservation encapsulates passenger collection")
    void testReservationComposition() {
        Station src = new Station(1L, "NDLS", "New Delhi", "New Delhi", "Delhi");
        Station dst = new Station(2L, "MMCT", "Mumbai Central", "Mumbai", "Maharashtra");

        Reservation res = new Reservation(
                101L,
                "2839182736",
                1L,
                12952L,
                LocalDate.now().plusDays(2),
                src,
                dst,
                TravelClass.THIRD_AC,
                2,
                new BigDecimal("3250.00"),
                BookingStatus.CONFIRMED,
                LocalDateTime.now()
        );

        ReservationPassenger p1 = new ReservationPassenger(1L, 101L, "Amit Sharma", 32, Gender.MALE, BerthType.LOWER, "B1", 12, BerthType.LOWER);
        ReservationPassenger p2 = new ReservationPassenger(2L, 101L, "Pooja Sharma", 29, Gender.FEMALE, BerthType.UPPER, "B1", 14, BerthType.UPPER);

        res.addPassenger(p1);
        res.addPassenger(p2);

        assertEquals(2, res.getPassengers().size());
        assertTrue(res.isCancellable(), "Future confirmed booking must be eligible for cancellation");

        res.setStatus(BookingStatus.CANCELLED);
        assertFalse(res.isCancellable(), "Cancelled booking must not be cancellable again");
    }
}
