package com.railway.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads and caches application and database configuration properties.
 * Reads db.properties from the classpath.
 */
public final class AppConfig {

    private static final Properties properties = new Properties();
    private static boolean initialized = false;

    private AppConfig() {
    }

    public static synchronized void init() {
        if (initialized) return;

        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                properties.load(is);
            } else {
                System.err.println("[AppConfig] WARNING: db.properties not found in classpath. Using defaults.");
            }
        } catch (IOException e) {
            System.err.println("[AppConfig] ERROR loading db.properties: " + e.getMessage());
        }
        initialized = true;
    }

    public static String getProperty(String key, String defaultValue) {
        if (!initialized) init();
        return properties.getProperty(key, defaultValue);
    }

    public static String getDbDriver() {
        return getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }

    public static String getDbUrl() {
        return getProperty("db.url", "jdbc:mysql://localhost:3306/railway_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    }

    public static String getDbUser() {
        return getProperty("db.user", "root");
    }

    public static String getDbPassword() {
        return getProperty("db.password", "saini");
    }
}
