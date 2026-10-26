package com.railway.exception;

/**
 * Thrown when a reservation cancellation request fails (e.g., journey date has already passed,
 * ticket is already cancelled).
 */
public class CancellationException extends RailwayException {
    public CancellationException(String message) {
        super(message);
    }
}
