package com.railway.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
                System.err.println("[AppConfig] WARNING: db.properties not found in classpath. Using defaults/env.");
            }
        } catch (IOException e) {
            System.err.println("[AppConfig] ERROR loading db.properties: " + e.getMessage());
        }
        initialized = true;
    }

    public static String getProperty(String key, String defaultValue) {
        if (!initialized) init();

        String envKey = key.toUpperCase().replace('.', '_');
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) {
            return envVal.trim();
        }

        String propVal = properties.getProperty(key);
        if (propVal != null && !propVal.isBlank()) {
            return propVal.trim();
        }

        return defaultValue;
    }

    public static String getDbDriver() {
        return getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }

    public static String getDbUrl() {
        return getProperty("db.url", "jdbc:mysql://127.0.0.1:3306/railway_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    }

    public static String getDbUser() {
        return getProperty("db.user", "root");
    }

    public static String getDbPassword() {
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null) {
            return envPass;
        }
        if (!initialized) init();
        String propVal = properties.getProperty("db.password");
        if (propVal != null) {
            return propVal.trim();
        }
        return "";
    }

    public static boolean isDemoAutofillEnabled() {
        String val = getProperty("app.demo.autofill", "false");
        return Boolean.parseBoolean(val);
    }
}
