package com.railway.exception;

/**
 * Thrown when user inputs fail syntactic or business validation rules.
 */
public class ValidationException extends RailwayException {
    public ValidationException(String message) {
        super(message);
    }
}
