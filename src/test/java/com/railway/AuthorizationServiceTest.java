package com.railway;

import com.railway.config.SessionContext;
import com.railway.exception.UnauthorizedException;
import com.railway.model.AdminUser;
import com.railway.model.PassengerUser;
import com.railway.security.AuthorizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Authorization and RBAC Security Tests")
class AuthorizationServiceTest {

    private PassengerUser passenger1;
    private PassengerUser passenger2;
    private AdminUser adminUser;

    @BeforeEach
    void setUp() {
        passenger1 = new PassengerUser(10L, "rahul", "hash", "Rahul Sharma", "rahul@example.com", "9123456780", LocalDateTime.now());
        passenger2 = new PassengerUser(20L, "priya", "hash", "Priya Verma", "priya@example.com", "9123456781", LocalDateTime.now());
        adminUser = new AdminUser(1L, "admin", "hash", "System Admin", "admin@railway.gov.in", "9876543210", LocalDateTime.now());
        SessionContext.logout();
    }

    @AfterEach
    void tearDown() {
        SessionContext.logout();
    }

    @Test
    @DisplayName("Unauthenticated session must throw UnauthorizedException")
    void testRequireAuthenticatedFailsWhenLoggedOut() {
        assertThrows(UnauthorizedException.class, AuthorizationService::requireAuthenticated);
        assertThrows(UnauthorizedException.class, AuthorizationService::requireAdmin);
        assertThrows(UnauthorizedException.class, () -> AuthorizationService.requireReservationAccess(10L));
    }

    @Test
    @DisplayName("Passenger session cannot invoke administrative operations")
    void testPassengerCannotInvokeAdmin() {
        SessionContext.setCurrentUser(passenger1);
        assertDoesNotThrow(AuthorizationService::requireAuthenticated);
        assertThrows(UnauthorizedException.class, AuthorizationService::requireAdmin);
    }

    @Test
    @DisplayName("Admin session can invoke administrative operations")
    void testAdminCanInvokeAdmin() {
        SessionContext.setCurrentUser(adminUser);
        assertDoesNotThrow(AuthorizationService::requireAdmin);
    }

    @Test
    @DisplayName("Passenger can only access own reservation; other passengers are rejected")
    void testReservationAccessBoundaries() {
        SessionContext.setCurrentUser(passenger1);
        // Own booking (user id 10)
        assertDoesNotThrow(() -> AuthorizationService.requireReservationAccess(10L));
        // Another passenger's booking (user id 20) -> must throw
        assertThrows(UnauthorizedException.class, () -> AuthorizationService.requireReservationAccess(20L));

        // Admin can access anyone's booking
        SessionContext.setCurrentUser(adminUser);
        assertDoesNotThrow(() -> AuthorizationService.requireReservationAccess(10L));
        assertDoesNotThrow(() -> AuthorizationService.requireReservationAccess(20L));
    }
}
