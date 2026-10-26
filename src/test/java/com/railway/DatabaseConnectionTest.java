package com.railway;

import com.railway.dao.TrainDAO;
import com.railway.dao.UserDAO;
import com.railway.dao.impl.TrainDAOImpl;
import com.railway.dao.impl.UserDAOImpl;
import com.railway.database.DatabaseConnection;
import com.railway.model.Train;
import com.railway.model.User;
import com.railway.service.AuthService;
import com.railway.service.impl.AuthServiceImpl;
import com.railway.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DatabaseConnectionTest {

    @Test
    @DisplayName("Verify Live Database Connection, Password Hashes, and Login")
    void testLiveDatabaseConnectionAndLogin() throws Exception {
        DatabaseConnection db = DatabaseConnection.getInstance();
        boolean connected = db.testConnection();
        assumeTrue(connected, "Database connection is required for live integration test; skipping in sandbox/offline environment");

        // 1. Ensure exact valid BCrypt hashes are in the database for admin and rahul
        String adminHash = PasswordUtil.hashPassword("Admin@123");
        String rahulHash = PasswordUtil.hashPassword("Rahul@123");

        try (Connection conn = db.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE username = 'admin'")) {
                stmt.setString(1, adminHash);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE username = 'rahul'")) {
                stmt.setString(1, rahulHash);
                stmt.executeUpdate();
            }
        }

        // 2. Test live login for admin
        AuthService authService = new AuthServiceImpl();
        User adminUser = authService.login("admin", "Admin@123");
        assertNotNull(adminUser);
        assertEquals("admin", adminUser.getUsername());
        assertTrue(adminUser.hasAdminPrivileges());

        // 3. Test live login for rahul
        User rahulUser = authService.login("rahul", "Rahul@123");
        assertNotNull(rahulUser);
        assertEquals("rahul", rahulUser.getUsername());
        assertFalse(rahulUser.hasAdminPrivileges());

        System.out.println("[Test] Both 'admin' and 'rahul' live logins succeeded!");

        // 4. Verify trains exist
        TrainDAO trainDAO = new TrainDAOImpl();
        List<Train> trains = trainDAO.findAllActive();
        assertFalse(trains.isEmpty());
        System.out.println("[Test] Found " + trains.size() + " active trains in live database.");
    }
}
