package com.railway.model.enums;

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
