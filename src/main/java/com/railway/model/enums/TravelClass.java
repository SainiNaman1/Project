package com.railway.model.enums;

import java.math.BigDecimal;

/**
 * Represents the accommodation classes available on Indian Railways trains.
 * Each class carries an official code and a specific fare multiplier applied
 * against base train distance rates.
 */
public enum TravelClass {
    FIRST_AC("1A", "AC First Class", new BigDecimal("3.50")),
    SECOND_AC("2A", "AC 2-Tier", new BigDecimal("2.25")),
    THIRD_AC("3A", "AC 3-Tier", new BigDecimal("1.60")),
    SLEEPER("SL", "Sleeper Class", new BigDecimal("1.00")),
    CHAIR_CAR("CC", "AC Chair Car", new BigDecimal("1.20"));

    private final String code;
    private final String displayName;
    private final BigDecimal fareMultiplier;

    TravelClass(String code, String displayName, BigDecimal fareMultiplier) {
        this.code = code;
        this.displayName = displayName;
        this.fareMultiplier = fareMultiplier;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getFareMultiplier() {
        return fareMultiplier;
    }

    /**
     * Looks up a TravelClass by its short code (e.g., '3A' -> THIRD_AC).
     */
    public static TravelClass fromCode(String code) {
        if (code == null) return null;
        for (TravelClass tc : values()) {
            if (tc.code.equalsIgnoreCase(code.trim())) {
                return tc;
            }
        }
        throw new IllegalArgumentException("Unknown travel class code: " + code);
    }

    @Override
    public String toString() {
        return displayName + " (" + code + ")";
    }
}
