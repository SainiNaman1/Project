package com.railway.exception;

/**
 * Thrown when credentials fail verification or an unrecognized user attempts login.
 */
public class AuthenticationException extends RailwayException {
    public AuthenticationException(String message) {
        super(message);
    }
}
