package com.example.BookingApplication.Validation;

import org.springframework.stereotype.Component;


public class SlotBookedException extends RuntimeException {
    public SlotBookedException(String message) {
        super(message);
    }
}
