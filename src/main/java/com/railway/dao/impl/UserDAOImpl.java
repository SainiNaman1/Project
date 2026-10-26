package com.railway.dao.impl;

import com.railway.dao.UserDAO;
import com.railway.database.DatabaseConnection;
import com.railway.model.AdminUser;
import com.railway.model.PassengerUser;
import com.railway.model.User;
import com.railway.model.enums.Role;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * PostgreSQL JDBC implementation of the UserDAO interface.
 * Strictly uses PreparedStatement for SQL injection defense.
 */
public class UserDAOImpl implements UserDAO {

    private final DatabaseConnection database = DatabaseConnection.getInstance();

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, full_name, email, phone_number, role, created_at " +
                     "FROM users WHERE LOWER(username) = LOWER(?)";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(long id) throws SQLException {
        String sql = "SELECT id, username, password_hash, full_name, email, phone_number, role, created_at " +
                     "FROM users WHERE id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public User save(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, phone_number, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername().trim());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName().trim());
            stmt.setString(4, user.getEmail().trim().toLowerCase());
            
            String phone = (user instanceof PassengerUser p) ? p.getPhoneNumber() : null;
            stmt.setString(5, phone);
            stmt.setString(6, user.getRole().name());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                }
            }
            user.setCreatedAt(LocalDateTime.now());
        }
        return user;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone_number");
        Role role = Role.valueOf(rs.getString("role"));
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        if (role == Role.ADMIN) {
            return new AdminUser(id, username, passwordHash, fullName, email, "Operations", createdAt);
        } else {
            return new PassengerUser(id, username, passwordHash, fullName, email, phone, createdAt);
        }
    }
}
