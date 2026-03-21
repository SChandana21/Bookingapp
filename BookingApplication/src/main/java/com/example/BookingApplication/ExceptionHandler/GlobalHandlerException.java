package com.example.BookingApplication.ExceptionHandler;

import com.example.BookingApplication.Validation.InvalidtimeException;
import com.example.BookingApplication.Validation.SlotBookedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(SlotBookedException.class)
    public ResponseEntity<String> handleSlotBooked(SlotBookedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidtimeException.class)
    public ResponseEntity<String> HandleinvalidRequest(InvalidtimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

}
