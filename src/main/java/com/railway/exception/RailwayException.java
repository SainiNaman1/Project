package com.railway.exception;

/**
 * Base checked exception for all business domain errors in the Railway Management System.
 * Ensures that service-layer failures are clearly caught and presented to the GUI.
 */
public class RailwayException extends Exception {
    public RailwayException(String message) {
        super(message);
    }

    public RailwayException(String message, Throwable cause) {
        super(message, cause);
    }
}
