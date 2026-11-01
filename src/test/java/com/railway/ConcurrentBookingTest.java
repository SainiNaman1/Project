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
import com.railway.model.enums.BookingStatus;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Concurrent Booking & Seat Allocation Tests")
class ConcurrentBookingTest {

    private Station ndls;
    private Station bct;
    private Train testTrain;

    @BeforeEach
    void setUp() {
        ndls = new Station(1L, "NDLS", "New Delhi", "New Delhi", "Delhi");
        bct = new Station(2L, "BCT", "Mumbai Central", "Mumbai", "Maharashtra");
        testTrain = new MailExpressTrain(55L, "12954", "August Kranti Rajdhani", ndls, bct, new BigDecimal("1.20"), true);
    }

    @Test
    @DisplayName("Two concurrent requests for last available seat: exactly 1 succeeds and 1 fails")
    void testConcurrentBookingForSingleSeat() throws Exception {
        // Shared mutable inventory representing DB table state
        AtomicBoolean seatAllocated = new AtomicBoolean(false);
        AtomicInteger successfulBookings = new AtomicInteger(0);
        AtomicInteger failedBookings = new AtomicInteger(0);
        List<Reservation> savedReservations = Collections.synchronizedList(new ArrayList<>());

        TrainDAO trainDAO = new TrainDAO() {
            public List<Train> findAllActive() { return List.of(testTrain); }
            public Optional<Train> findById(long id) { return Optional.of(testTrain); }
            public Optional<Train> findByNumber(String n) { return Optional.of(testTrain); }
            public List<Train> findTrainsBetweenStations(long s, long d) { return List.of(testTrain); }
            public Train save(Train t) { return t; }
            public void updateStatus(long id, boolean a) {}
        };

        StationDAO stationDAO = new StationDAO() {
            public List<Station> findAll() { return List.of(ndls, bct); }
            public Optional<Station> findById(long id) { return id == 1L ? Optional.of(ndls) : Optional.of(bct); }
            public Optional<Station> findByCode(String c) { return Optional.empty(); }
            public Station save(Station s) { return s; }
        };

        RouteDAO routeDAO = new RouteDAO() {
            public List<RouteStop> findStopsByTrainId(long trainId) { return Collections.emptyList(); }
            public Optional<RouteStop> findStop(long trainId, long stationId) { return Optional.empty(); }
            public void updatePlatform(long trainId, long stationId, int platformNumber) {}
            public int calculateDistanceKm(long trainId, long from, long to) { return 1384; }
            public void saveStops(long trainId, List<RouteStop> stops) {}
        };

        // Thread-safe SeatDAO enforcing atomic row-locking
        SeatDAO seatDAO = new SeatDAO() {
            public List<Seat> findSeatsByTrainAndClass(long t, TravelClass c) { return Collections.emptyList(); }
            public int getAvailableSeatCount(long t, LocalDate d, TravelClass c) {
                return seatAllocated.get() ? 0 : 1;
            }

            public synchronized List<Seat> lockAvailableSeatsForUpdate(Connection conn, long t, LocalDate d, TravelClass c, int count) {
                // Simulates InnoDB SELECT ... FOR UPDATE pessimistic row-lock
                if (!seatAllocated.get()) {
                    seatAllocated.set(true); // Atomically acquire candidate seat
                    return List.of(new Seat(101L, t, c, "B1", 12, BerthType.LOWER));
                }
                return Collections.emptyList(); // Already locked / allocated
            }

            public void insertSeatAssignment(Connection conn, long t, LocalDate d, long s, long r, String st) {}
            public void deleteAssignmentsByReservationId(Connection conn, long r) {}
        };

        ReservationDAO resDAO = new ReservationDAO() {
            public Reservation save(Connection conn, Reservation r) {
                r.setId(savedReservations.size() + 1L);
                savedReservations.add(r);
                return r;
            }
            public void savePassengers(Connection conn, long r, List<ReservationPassenger> p) {}
            public Optional<Reservation> findByPnr(String pnr) { return Optional.empty(); }
            public Optional<Reservation> findByPnrForUpdate(Connection conn, String pnr) { return Optional.empty(); }
            public List<Reservation> findByUserId(long u) { return Collections.emptyList(); }
            public List<Reservation> findAll() { return new ArrayList<>(savedReservations); }
            public void updateStatus(Connection conn, long r, BookingStatus s) {}
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

        ReservationService service = new ReservationServiceImpl(
                resDAO, seatDAO, trainDAO, routeDAO, stationDAO,
                new FareCalculationServiceImpl(),
                () -> mockConn
        );

        // Run 2 simultaneous threads competing for the 1 remaining seat
        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final long userId = 100L + i;
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await(); // Simultaneous release

                List<ReservationPassenger> pax = List.of(
                        new ReservationPassenger(0L, 0L, "Passenger " + userId, 30, Gender.FEMALE, BerthType.LOWER, "", 0, BerthType.LOWER)
                );
                BookingRequest req = new BookingRequest(
                        userId,
                        testTrain.getId(),
                        ndls.getId(),
                        bct.getId(),
                        LocalDate.now().plusDays(3),
                        TravelClass.THIRD_AC,
                        pax
                );

                try {
                    Reservation res = service.bookTicket(req);
                    if (res != null) {
                        successfulBookings.incrementAndGet();
                    }
                } catch (BookingException e) {
                    failedBookings.incrementAndGet();
                }
                return null;
            }));
        }

        readyLatch.await();
        startLatch.countDown(); // Fire both threads at the exact same instant

        for (Future<Void> f : futures) {
            f.get();
        }
        executor.shutdown();

        // Exactly 1 must have succeeded, and exactly 1 must have failed with BookingException
        assertEquals(1, successfulBookings.get(), "Exactly 1 booking must succeed for the last seat");
        assertEquals(1, failedBookings.get(), "Exactly 1 booking must fail due to seat exhaustion");
        assertEquals(1, savedReservations.size(), "Only 1 reservation record must be persisted");
    }
}
