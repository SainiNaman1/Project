package com.railway.exception;

/**
 * Thrown when a ticket booking transaction cannot be completed (e.g., zero available seats,
 * concurrency conflict, invalid passenger data).
 */
public class BookingException extends RailwayException {
    public BookingException(String message) {
        super(message);
    }

    public BookingException(String message, Throwable cause) {
        super(message, cause);
    }
}
