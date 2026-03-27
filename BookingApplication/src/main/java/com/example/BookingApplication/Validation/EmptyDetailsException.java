package com.example.BookingApplication.Validation;

public class EmptyDetailsException extends RuntimeException {
    public EmptyDetailsException(String message) {
        super(message);
    }
}
