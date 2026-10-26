package com.railway;

import com.railway.database.DatabaseConnection;
import com.railway.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ResetPasswords {
    public static void main(String[] args) {
        try {
            String adminHash = PasswordUtil.hashPassword("Admin@123");
            String rahulHash = PasswordUtil.hashPassword("Rahul@123");

            System.out.println("Generated Admin Hash: " + adminHash);
            System.out.println("Generated Rahul Hash: " + rahulHash);

            // Verify they check out
            System.out.println("Admin check: " + PasswordUtil.checkPassword("Admin@123", adminHash));
            System.out.println("Rahul check: " + PasswordUtil.checkPassword("Rahul@123", rahulHash));

            DatabaseConnection db = DatabaseConnection.getInstance();
            try (Connection conn = db.getConnection()) {
                // Update admin
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE username = 'admin'")) {
                    stmt.setString(1, adminHash);
                    int count = stmt.executeUpdate();
                    System.out.println("Updated admin rows: " + count);
                }

                // Update rahul
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE username = 'rahul'")) {
                    stmt.setString(1, rahulHash);
                    int count = stmt.executeUpdate();
                    System.out.println("Updated rahul rows: " + count);
                }
            }
            System.out.println("PASSWORDS RESET SUCCESSFULLY IN DATABASE!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
