package com.example.BookingApplication.dto;

import com.example.BookingApplication.Enum.BookingStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingDTO {
    private String UserID;
    private String studioId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private String studioName;


    private double amount;

    private LocalDateTime createdAt;
}
