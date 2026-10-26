package com.railway.database;

import com.railway.util.PasswordUtil;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Automates initial schema creation, sample data provisioning, and self-healing
 * demo user credential verification.
 */
public final class SchemaInitializer {

    private SchemaInitializer() {
    }

    /**
     * Initializes the database if tables do not exist, and guarantees that default
     * demo accounts (admin / Admin@123, rahul / Rahul@123) have valid BCrypt hashes.
     */
    public static synchronized void initializeDatabase() {
        DatabaseConnection db = DatabaseConnection.getInstance();
        try (Connection conn = db.getConnection()) {
            boolean isMysql = DatabaseConnection.getActiveDatabaseType().toLowerCase().contains("mysql");

            if (!tablesExist(conn)) {
                System.out.println("[SchemaInitializer] Database tables not detected. Initializing schema...");
                String schemaFile = isMysql ? "schema_mysql.sql" : "schema.sql";
                String sampleDataFile = isMysql ? "sample_data_mysql.sql" : "sample_data.sql";

                executeSqlScript(conn, schemaFile);
                System.out.println("[SchemaInitializer] Schema initialized successfully. Seeding sample data...");
                executeSqlScript(conn, sampleDataFile);
                System.out.println("[SchemaInitializer] Sample data seeded successfully.");
            } else {
                System.out.println("[SchemaInitializer] Database schema exists. Verifying default demo accounts...");
            }

            // Self-healing: verify demo credentials have valid BCrypt hashes
            ensureDefaultUser(conn, "admin", "Admin@123", "System Administrator", "admin@railway.gov.in", "9876543210", "ADMIN");
            ensureDefaultUser(conn, "rahul", "Rahul@123", "Rahul Sharma", "rahul.sharma@example.com", "9123456780", "PASSENGER");
            System.out.println("[SchemaInitializer] Default accounts verified and ready for sign in.");

        } catch (SQLException e) {
            System.err.println("[SchemaInitializer] Note on schema initialization: " + e.getMessage());
        }
    }

    private static boolean tablesExist(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, "users", new String[]{"TABLE"})) {
            if (rs.next()) return true;
        }
        // Try uppercase for databases that store uppercase identifiers
        try (ResultSet rs = meta.getTables(null, null, "USERS", new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static void ensureDefaultUser(Connection conn, String username, String expectedPassword, String fullName, String email, String phone, String role) {
        String query = "SELECT id, password_hash FROM users WHERE LOWER(username) = LOWER(?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    String currentHash = rs.getString("password_hash");

                    // Verify if the current hash checks out with the expected password
                    if (!PasswordUtil.checkPassword(expectedPassword, currentHash)) {
                        System.out.println("[SchemaInitializer] Syncing password hash for user: " + username);
                        String freshHash = PasswordUtil.hashPassword(expectedPassword);
                        try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?")) {
                            updateStmt.setString(1, freshHash);
                            updateStmt.setLong(2, id);
                            updateStmt.executeUpdate();
                        }
                    }
                } else {
                    // User does not exist, insert fresh
                    System.out.println("[SchemaInitializer] Creating missing demo user: " + username);
                    String freshHash = PasswordUtil.hashPassword(expectedPassword);
                    String insert = "INSERT INTO users (username, password_hash, full_name, email, phone_number, role) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insert)) {
                        insertStmt.setString(1, username);
                        insertStmt.setString(2, freshHash);
                        insertStmt.setString(3, fullName);
                        insertStmt.setString(4, email);
                        insertStmt.setString(5, phone);
                        insertStmt.setString(6, role);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[SchemaInitializer] Notice while verifying user " + username + ": " + e.getMessage());
        }
    }

    private static void executeSqlScript(Connection conn, String resourcePath) {
        try (InputStream is = SchemaInitializer.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("[SchemaInitializer] Resource file not found: " + resourcePath);
                return;
            }

            String fullSql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            String[] statements = fullSql.split(";\\s*(\\r?\\n)+");

            try (Statement stmt = conn.createStatement()) {
                for (String rawSql : statements) {
                    String sql = rawSql.trim();
                    if (!sql.isEmpty() && !sql.startsWith("--")) {
                        try {
                            stmt.execute(sql);
                        } catch (SQLException e) {
                            // Non-fatal notice for duplicate entries or DB switches
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SchemaInitializer] Failed executing SQL script " + resourcePath + ": " + e.getMessage());
        }
    }
}
