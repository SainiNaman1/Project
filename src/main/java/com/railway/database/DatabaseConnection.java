package com.railway.database;

import com.railway.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC database connections with resilient auto-detection.
 * Supports both MySQL (localhost:3306) and PostgreSQL (localhost:5432).
 */
public final class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private static String activeDatabaseType = "Unknown";

    static {
        // Register MySQL Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}

        // Register PostgreSQL Driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ignored) {}

        AppConfig.init();
    }

    private DatabaseConnection() {
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Obtains a fresh JDBC Connection.
     * Tries the primary configured database first. If unavailable, seamlessly
     * attempts fallback to local MySQL or PostgreSQL.
     */
    public Connection getConnection() throws SQLException {
        // 1. Try primary configured connection from db.properties
        try {
            Connection conn = DriverManager.getConnection(
                    AppConfig.getDbUrl(),
                    AppConfig.getDbUser(),
                    AppConfig.getDbPassword()
            );
            if (conn != null && conn.isValid(2)) {
                activeDatabaseType = AppConfig.getDbUrl().contains("mysql") ? "MySQL" : "PostgreSQL";
                return conn;
            }
        } catch (SQLException primaryEx) {
            System.err.println("[DatabaseConnection] Primary connection error: " + primaryEx.getMessage());
        }

        // 2. Fallback: Try MySQL 8.0 on 127.0.0.1:3306
        try {
            String mysqlUrl = "jdbc:mysql://127.0.0.1:3306/railway_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            Connection mysqlConn = DriverManager.getConnection(mysqlUrl, "root", "saini");
            if (mysqlConn != null && mysqlConn.isValid(2)) {
                activeDatabaseType = "MySQL";
                return mysqlConn;
            }
        } catch (SQLException mysqlEx) {
            System.err.println("[DatabaseConnection] MySQL fallback error: " + mysqlEx.getMessage());
        }

        // 3. Fallback: Try PostgreSQL on localhost:5432
        try {
            String pgUrl = "jdbc:postgresql://localhost:5432/railway_db";
            Connection pgConn = DriverManager.getConnection(pgUrl, "postgres", "root");
            if (pgConn != null && pgConn.isValid(2)) {
                activeDatabaseType = "PostgreSQL (Fallback)";
                return pgConn;
            }
        } catch (SQLException ignored) {}

        throw new SQLException("Could not connect to database. Please ensure MySQL (port 3306) or PostgreSQL (port 5432) is running and railway_db exists.");
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && conn.isValid(2);
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Connection test failed: " + e.getMessage());
            return false;
        }
    }

    public static String getActiveDatabaseType() {
        return activeDatabaseType;
    }

    public static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.rollback();
                }
            } catch (SQLException ignored) {}
        }
    }

    public static void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.setAutoCommit(true);
                }
                conn.close();
            } catch (SQLException ignored) {}
        }
    }
}
