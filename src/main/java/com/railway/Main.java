package com.railway;

import com.railway.database.DatabaseConnection;
import com.railway.database.SchemaInitializer;
import com.railway.gui.LoginFrame;
import com.railway.gui.UITheme;
import javax.swing.SwingUtilities;

/**
 * Entry point for the Railway Management System desktop application.
 * Configures the FlatLaf theme, bootstraps the database schema, and opens the LoginFrame.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Initialize modern Swing Look and Feel
        UITheme.setupLookAndFeel();

        // 2. Bootstrap database schema in background thread
        new Thread(() -> {
            boolean connected = DatabaseConnection.getInstance().testConnection();
            if (connected) {
                System.out.println("[Main] Connected to PostgreSQL. Initializing schema if needed...");
                SchemaInitializer.initializeDatabase();
            } else {
                System.err.println("[Main] NOTE: PostgreSQL connection could not be established at localhost:5432/railway_db.");
                System.err.println("[Main] Please ensure PostgreSQL is running and credentials in db.properties are correct.");
            }
        }).start();

        // 3. Launch UI on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
