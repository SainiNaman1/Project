package com.railway.exception;

public class RailwayException extends Exception {
    public RailwayException(String message) {
        super(message);
    }

    public RailwayException(String message, Throwable cause) {
        super(message, cause);
    }
}
