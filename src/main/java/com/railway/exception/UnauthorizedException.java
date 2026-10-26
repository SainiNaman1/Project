package com.railway.exception;

/**
 * Thrown when a user attempts an administrative operation without authorized privileges,
 * or attempts to access another passenger's private booking.
 */
public class UnauthorizedException extends RailwayException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
