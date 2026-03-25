package com.hotel;

import com.hotel.config.DatabaseConfig;
import com.hotel.ui.LoginFrame;
import com.hotel.util.UITheme;

import javax.swing.*;

/**
 * Application entry point.
 *
 * How to run:
 *   1. Set up MySQL and run src/main/resources/schema.sql
 *   2. Edit DatabaseConfig.java with your DB credentials
 *   3. Add mysql-connector-j-8.x.x.jar to the classpath
 *   4. Compile and run this class
 *
 * Default login: username=admin  password=admin123
 */
public class HotelApp {

    public static void main(String[] args) {
        // Apply global theme first (before any Swing component is created)
        UITheme.apply();

        // Verify DB connection
        if (!DatabaseConfig.testConnection()) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to the database.\n\n" +
                "Please ensure:\n" +
                "  1. MySQL is running\n" +
                "  2. The database 'hotel_db' exists (run schema.sql)\n" +
                "  3. Credentials in DatabaseConfig.java are correct",
                "Database Connection Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Launch on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
