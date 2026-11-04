package com.railway;

import com.railway.dao.ReservationDAO;
import com.railway.dao.RouteDAO;
import com.railway.dao.SeatDAO;
import com.railway.dao.StationDAO;
import com.railway.dao.TrainDAO;
import com.railway.exception.BookingException;
import com.railway.model.MailExpressTrain;
import com.railway.model.Reservation;
import com.railway.model.ReservationPassenger;
import com.railway.model.RouteStop;
import com.railway.model.Seat;
import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.dto.BookingRequest;
import com.railway.model.enums.BerthType;
import com.railway.model.enums.Gender;
import com.railway.model.enums.TravelClass;
import com.railway.service.ReservationService;
import com.railway.service.impl.FareCalculationServiceImpl;
import com.railway.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Route Ordering and Distance Validation Tests")
class RouteOrderingValidationTest {

    private ReservationService reservationService;
    private Station ndls;
    private Station bct;
    private Train sampleTrain;

    @BeforeEach
    void setUp() {
        ndls = new Station(1L, "NDLS", "New Delhi", "New Delhi", "Delhi");
        bct = new Station(2L, "BCT", "Mumbai Central", "Mumbai", "Maharashtra");
        sampleTrain = new MailExpressTrain(10L, "12904", "Golden Temple Mail", ndls, bct, new BigDecimal("1.20"), true);

        TrainDAO trainDAO = new TrainDAO() {
            public List<Train> findAllActive() { return List.of(sampleTrain); }
            public Optional<Train> findById(long id) { return Optional.of(sampleTrain); }
            public Optional<Train> findById(Connection conn, long id) { return Optional.of(sampleTrain); }
            public Optional<Train> findByNumber(String n) { return Optional.of(sampleTrain); }
            public List<Train> findTrainsBetweenStations(long s, long d) { return List.of(sampleTrain); }
            public Train save(Train t) { return t; }
            public void updateStatus(long id, boolean a) {}
        };

        StationDAO stationDAO = new StationDAO() {
            public List<Station> findAll() { return List.of(ndls, bct); }
            public Optional<Station> findById(long id) { return id == 1L ? Optional.of(ndls) : Optional.of(bct); }
            public Optional<Station> findById(Connection conn, long id) { return id == 1L ? Optional.of(ndls) : Optional.of(bct); }
            public Optional<Station> findByCode(String c) { return "NDLS".equals(c) ? Optional.of(ndls) : Optional.of(bct); }
            public Station save(Station s) { return s; }
        };

        RouteDAO routeDAO = new RouteDAO() {
            public List<RouteStop> findStopsByTrainId(long trainId) { return Collections.emptyList(); }
            public Optional<RouteStop> findStop(long trainId, long stationId) { return Optional.empty(); }
            public void updatePlatform(long trainId, long stationId, int platformNumber) {}
            public int calculateDistanceKm(long trainId, long from, long to) {
                return (from == 1L && to == 2L) ? 1384 : -1;
            }
            public int calculateDistanceKm(Connection conn, long trainId, long from, long to) {
                return (from == 1L && to == 2L) ? 1384 : -1;
            }
            public void saveStops(long trainId, List<RouteStop> stops) {}
        };

        SeatDAO seatDAO = new SeatDAO() {
            public List<Seat> findSeatsByTrainAndClass(long t, TravelClass c) { return Collections.emptyList(); }
            public int getAvailableSeatCount(long t, LocalDate d, TravelClass c) { return 10; }
            public List<Seat> lockAvailableSeatsForUpdate(Connection conn, long t, LocalDate d, TravelClass c, int count) {
                List<Seat> seats = new ArrayList<>();
                for (int i = 1; i <= count; i++) {
                    seats.add(new Seat(i, t, c, "S1", i, BerthType.LOWER));
                }
                return seats;
            }
            public void insertSeatAssignment(Connection conn, long t, LocalDate d, long s, long r, String st) {}
            public void deleteAssignmentsByReservationId(Connection conn, long r) {}
        };

        ReservationDAO resDAO = new ReservationDAO() {
            public Reservation save(Connection conn, Reservation r) { r.setId(999L); return r; }
            public void savePassengers(Connection conn, long r, List<ReservationPassenger> p) {}
            public Optional<Reservation> findByPnr(String pnr) { return Optional.empty(); }
            public Optional<Reservation> findByPnrForUpdate(Connection conn, String pnr) { return Optional.empty(); }
            public List<Reservation> findByUserId(long u) { return Collections.emptyList(); }
            public List<Reservation> findAll() { return Collections.emptyList(); }
            public void updateStatus(Connection conn, long r, com.railway.model.enums.BookingStatus s) {}
            public boolean existsByPnr(Connection conn, String p) { return false; }
        };

        Connection mockConn = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("getAutoCommit".equals(method.getName())) return false;
                    return null;
                }
        );

        reservationService = new ReservationServiceImpl(
                resDAO, seatDAO, trainDAO, routeDAO, stationDAO,
                new FareCalculationServiceImpl(),
                () -> mockConn
        );
    }

    @Test
    @DisplayName("Reversed route stations must throw explicit BookingException and NOT fall back to 100km")
    void testReversedRouteThrowsBookingException() {
        List<ReservationPassenger> passengers = List.of(
                new ReservationPassenger(0L, 0L, "Rahul Sharma", 28, Gender.MALE, BerthType.LOWER, null, 0, null)
        );

        BookingRequest reversedRequest = new BookingRequest(
                1L,
                sampleTrain.getId(),
                bct.getId(),
                ndls.getId(),
                LocalDate.now().plusDays(2),
                TravelClass.SLEEPER,
                passengers
        );

        BookingException ex = assertThrows(BookingException.class, () ->
                reservationService.bookTicket(reversedRequest));

        assertTrue(ex.getMessage().contains("Invalid route"),
                "Expected error message mentioning invalid route, but got: " + ex.getMessage());
    }
}
