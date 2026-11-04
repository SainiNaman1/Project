package com.railway;

import com.railway.config.ApplicationContext;
import com.railway.database.DatabaseConnection;
import com.railway.database.SchemaInitializer;
import com.railway.gui.LoginFrame;
import com.railway.gui.UITheme;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        UITheme.setupLookAndFeel();

        ApplicationContext.getInstance();

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

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
