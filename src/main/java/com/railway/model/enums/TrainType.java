package com.railway.model.enums;

/**
 * Categorizes trains by operational speed and service tier.
 */
public enum TrainType {
    RAJDHANI("Rajdhani Express"),
    SHATABDI("Shatabdi Express"),
    MAIL_EXPRESS("Mail / Express"),
    SUPERFAST("Superfast Express");

    private final String description;

    TrainType(String description) {
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
