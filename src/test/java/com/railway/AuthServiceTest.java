package com.railway;

import com.railway.config.SessionContext;
import com.railway.dao.UserDAO;
import com.railway.exception.AuthenticationException;
import com.railway.exception.ValidationException;
import com.railway.model.PassengerUser;
import com.railway.model.User;
import com.railway.model.enums.Role;
import com.railway.service.AuthService;
import com.railway.service.impl.AuthServiceImpl;
import com.railway.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private AuthService authService;
    private MockUserDAO mockUserDAO;

    // In-memory fake DAO for isolated testing without live database
    static class MockUserDAO implements UserDAO {
        private final Map<String, User> usersByUsername = new HashMap<>();
        private final Map<String, User> usersByEmail = new HashMap<>();
        private long idSequence = 1;

        @Override
        public Optional<User> findByUsername(String username) throws SQLException {
            return Optional.ofNullable(usersByUsername.get(username.toLowerCase()));
        }

        @Override
        public Optional<User> findById(long id) throws SQLException {
            return usersByUsername.values().stream().filter(u -> u.getId() == id).findFirst();
        }

        @Override
        public boolean existsByUsername(String username) throws SQLException {
            return usersByUsername.containsKey(username.toLowerCase());
        }

        @Override
        public boolean existsByEmail(String email) throws SQLException {
            return usersByEmail.containsKey(email.toLowerCase());
        }

        @Override
        public User save(User user) throws SQLException {
            user.setId(idSequence++);
            user.setCreatedAt(LocalDateTime.now());
            usersByUsername.put(user.getUsername().toLowerCase(), user);
            usersByEmail.put(user.getEmail().toLowerCase(), user);
            return user;
        }
    }

    @BeforeEach
    void setUp() {
        mockUserDAO = new MockUserDAO();
        authService = new AuthServiceImpl(mockUserDAO);
        SessionContext.logout();
    }

    @Test
    @DisplayName("TC-01: Successful passenger registration with role PASSENGER")
    void testSuccessfulRegistration() throws Exception {
        PassengerUser user = authService.register(
                "Amit Kumar",
                "amit@example.com",
                "9876543210",
                "amitk",
                "Password@123",
                "Password@123"
        );

        assertNotNull(user);
        assertEquals("amitk", user.getUsername());
        assertEquals(Role.PASSENGER, user.getRole(), "New registered user must strictly have PASSENGER role");
        assertFalse(user.hasAdminPrivileges());
        assertTrue(PasswordUtil.checkPassword("Password@123", user.getPasswordHash()));
    }

    @Test
    @DisplayName("TC-02: Duplicate username registration must be rejected")
    void testDuplicateUsernameRejection() throws Exception {
        authService.register("User One", "user1@example.com", "9876543210", "duplicateUser", "Pass123", "Pass123");

        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.register("User Two", "user2@example.com", "9876543211", "duplicateUser", "Pass123", "Pass123")
        );
        assertTrue(ex.getMessage().contains("already taken"));
    }

    @Test
    @DisplayName("TC-03: Duplicate email registration must be rejected")
    void testDuplicateEmailRejection() throws Exception {
        authService.register("User One", "same@example.com", "9876543210", "user1", "Pass123", "Pass123");

        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.register("User Two", "same@example.com", "9876543211", "user2", "Pass123", "Pass123")
        );
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("TC-04: Invalid email format must be rejected")
    void testInvalidEmailFormat() {
        assertThrows(ValidationException.class, () ->
                authService.register("User", "notanemail", "9876543210", "testuser", "Pass123", "Pass123")
        );
    }

    @Test
    @DisplayName("TC-05: Invalid phone number must be rejected")
    void testInvalidPhone() {
        assertThrows(ValidationException.class, () ->
                authService.register("User", "test@test.com", "12345", "testuser", "Pass123", "Pass123")
        );
    }

    @Test
    @DisplayName("TC-06: Password mismatch must be rejected")
    void testPasswordMismatch() {
        assertThrows(ValidationException.class, () ->
                authService.register("User", "test@test.com", "9876543210", "testuser", "Pass123", "DifferentPass123")
        );
    }

    @Test
    @DisplayName("TC-07: Successful login sets SessionContext")
    void testSuccessfulLogin() throws Exception {
        authService.register("Test User", "test@test.com", "9876543210", "loginuser", "Secret@123", "Secret@123");

        User loggedIn = authService.login("loginuser", "Secret@123");
        assertNotNull(loggedIn);
        assertEquals("loginuser", loggedIn.getUsername());
        assertTrue(SessionContext.isAuthenticated());
        assertEquals("loginuser", SessionContext.getCurrentUser().getUsername());
    }

    @Test
    @DisplayName("TC-08: Login with wrong password must throw AuthenticationException")
    void testWrongPasswordLogin() throws Exception {
        authService.register("Test User", "test@test.com", "9876543210", "loginuser", "Secret@123", "Secret@123");

        assertThrows(AuthenticationException.class, () ->
                authService.login("loginuser", "WrongPassword")
        );
        assertFalse(SessionContext.isAuthenticated());
    }

    @Test
    @DisplayName("TC-09: Login with unknown username must throw AuthenticationException")
    void testUnknownUserLogin() {
        assertThrows(AuthenticationException.class, () ->
                authService.login("nonexistent", "AnyPassword")
        );
    }
}
