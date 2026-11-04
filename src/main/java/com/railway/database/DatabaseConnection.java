package com.railway.database;

import com.railway.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private static final String DATABASE_TYPE = "MySQL 8.0";

    static {
        try {
            Class.forName(AppConfig.getDbDriver());
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] Warning: MySQL JDBC driver class not found on classpath: " + e.getMessage());
        }
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

    public Connection getConnection() throws SQLException {
        String url = AppConfig.getDbUrl();
        String user = AppConfig.getDbUser();
        String pass = AppConfig.getDbPassword();

        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            if (conn != null && conn.isValid(2)) {
                return conn;
            }
        } catch (SQLException ex) {
            throw new SQLException(
                    String.format("Could not connect to MySQL database at %s. Please ensure MySQL server is running and database exists. Error: %s",
                            url, ex.getMessage()), ex);
        }
        throw new SQLException("Unable to establish valid MySQL connection to " + url);
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
        return DATABASE_TYPE;
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
