package com.example.BookingApplication.dto;

import com.example.BookingApplication.Enum.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingDTO {
    private String UserID;
    private String studioId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    private BookingStatus status;
    private String studioName;


    private double amount;

    private LocalDateTime createdAt;
}
