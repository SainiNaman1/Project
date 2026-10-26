package com.railway;

import com.railway.config.SessionContext;
import com.railway.dao.RailwayRuleDAO;
import com.railway.dao.RouteDAO;
import com.railway.dao.TrainDAO;
import com.railway.exception.UnauthorizedException;
import com.railway.exception.ValidationException;
import com.railway.model.AdminUser;
import com.railway.model.PassengerUser;
import com.railway.model.RailwayRule;
import com.railway.model.RouteStop;
import com.railway.model.Train;
import com.railway.service.AdminService;
import com.railway.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminServiceTest {

    private AdminService adminService;
    private MockTrainDAO mockTrainDAO;
    private MockRouteDAO mockRouteDAO;
    private MockRuleDAO mockRuleDAO;

    static class MockTrainDAO implements TrainDAO {
        long toggledTrainId = -1;
        boolean toggledStatus = false;

        @Override public List<Train> findAllActive() { return new ArrayList<>(); }
        @Override public Optional<Train> findById(long id) { return Optional.empty(); }
        @Override public Optional<Train> findByNumber(String trainNumber) { return Optional.empty(); }
        @Override public List<Train> findTrainsBetweenStations(long s, long d) { return new ArrayList<>(); }
        @Override public Train save(Train train) { train.setId(10L); return train; }
        @Override public void updateStatus(long trainId, boolean active) {
            this.toggledTrainId = trainId;
            this.toggledStatus = active;
        }
    }

    static class MockRouteDAO implements RouteDAO {
        int updatedPlatform = -1;

        @Override public List<RouteStop> findStopsByTrainId(long trainId) { return new ArrayList<>(); }
        @Override public Optional<RouteStop> findStop(long trainId, long stationId) { return Optional.empty(); }
        @Override public void updatePlatform(long trainId, long stationId, int platformNumber) {
            this.updatedPlatform = platformNumber;
        }
        @Override public int calculateDistanceKm(long trainId, long fromStationId, long toStationId) { return 0; }
        @Override public void saveStops(long trainId, List<RouteStop> stops) {}
    }

    static class MockRuleDAO implements RailwayRuleDAO {
        @Override public List<RailwayRule> findAllActive() { return new ArrayList<>(); }
        @Override public RailwayRule save(RailwayRule rule) { rule.setId(1L); return rule; }
    }

    @BeforeEach
    void setUp() {
        mockTrainDAO = new MockTrainDAO();
        mockRouteDAO = new MockRouteDAO();
        mockRuleDAO = new MockRuleDAO();
        adminService = new AdminServiceImpl(mockTrainDAO, mockRouteDAO, mockRuleDAO);
        SessionContext.logout();
    }

    @AfterEach
    void tearDown() {
        SessionContext.logout();
    }

    @Test
    @DisplayName("TC-10: Passenger session attempting admin operation must throw UnauthorizedException")
    void testPassengerDeniedAdminOperations() {
        // Set session user as standard passenger
        PassengerUser passenger = new PassengerUser(1L, "rahul", "hash", "Rahul", "r@test.com", "9876543210", LocalDateTime.now());
        SessionContext.setCurrentUser(passenger);

        assertThrows(UnauthorizedException.class, () ->
                adminService.updatePlatform(1L, 1L, 3),
                "Passenger must be blocked from updating platforms"
        );

        assertThrows(UnauthorizedException.class, () ->
                adminService.toggleTrainStatus(1L, false),
                "Passenger must be blocked from toggling train status"
        );
    }

    @Test
    @DisplayName("TC-11: Admin session successfully updates live platform")
    void testAdminUpdatesPlatform() {
        AdminUser admin = new AdminUser(2L, "admin", "hash", "Admin", "a@test.com", "Ops", LocalDateTime.now());
        SessionContext.setCurrentUser(admin);

        assertDoesNotThrow(() -> adminService.updatePlatform(12952L, 1L, 5));
        assertEquals(5, mockRouteDAO.updatedPlatform);
    }

    @Test
    @DisplayName("TC-12: Invalid platform number rejected")
    void testInvalidPlatform() {
        AdminUser admin = new AdminUser(2L, "admin", "hash", "Admin", "a@test.com", "Ops", LocalDateTime.now());
        SessionContext.setCurrentUser(admin);

        assertThrows(ValidationException.class, () -> adminService.updatePlatform(1L, 1L, 0));
        assertThrows(ValidationException.class, () -> adminService.updatePlatform(1L, 1L, 30));
    }
}
