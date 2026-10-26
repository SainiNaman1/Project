package com.railway.model.enums;

/**
 * Defines the security authorization roles in the Railway Management System.
 * Regular passengers are granted standard self-service booking permissions,
 * while administrators are authorized to manage trains, routes, and platforms.
 */
public enum Role {
    PASSENGER("Passenger"),
    ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
