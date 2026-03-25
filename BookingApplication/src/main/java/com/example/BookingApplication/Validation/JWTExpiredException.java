package com.example.BookingApplication.Validation;

public class JWTExpiredException extends RuntimeException {
    public JWTExpiredException(String message) {
        super(message);
    }
}
