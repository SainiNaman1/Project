package com.railway.model.enums;

/**
 * Physical berth and seat position layout classifications.
 */
public enum BerthType {
    LOWER("Lower Berth"),
    MIDDLE("Middle Berth"),
    UPPER("Upper Berth"),
    SIDE_LOWER("Side Lower"),
    SIDE_UPPER("Side Upper"),
    WINDOW("Window Seat"),
    AISLE("Aisle Seat"),
    NO_PREFERENCE("No Preference");

    private final String label;

    BerthType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
