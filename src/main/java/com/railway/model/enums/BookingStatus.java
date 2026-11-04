package com.railway.model.enums;

public enum BookingStatus {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled");

    private final String description;

    BookingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
