package com.railway;

import com.railway.config.ApplicationContext;
import com.railway.database.DatabaseConnection;
import com.railway.database.SchemaInitializer;
import com.railway.gui.LoginFrame;
import com.railway.gui.UITheme;
import javax.swing.SwingUtilities;

/**
 * Entry point for the Railway Management System desktop application.
 * Configures the FlatLaf theme, bootstraps the MySQL 8 schema, and opens the LoginFrame.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Initialize modern Swing Look and Feel
        UITheme.setupLookAndFeel();

        // 2. Pre-warm dependency injection container
        ApplicationContext.getInstance();

        // 3. Bootstrap MySQL database schema in background thread
        new Thread(() -> {
            boolean connected = DatabaseConnection.getInstance().testConnection();
            if (connected) {
                System.out.println("[Main] Connected to MySQL 8.0. Initializing schema if needed...");
                SchemaInitializer.initializeDatabase();
            } else {
                System.err.println("[Main] NOTE: MySQL connection could not be established at 127.0.0.1:3306/railway_db.");
                System.err.println("[Main] Please ensure MySQL server (port 3306) is running and railway_db exists.");
            }
        }).start();

        // 4. Launch UI on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
