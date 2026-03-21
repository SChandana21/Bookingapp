package com.example.BookingApplication.Entity;

import com.example.BookingApplication.Enum.BookingStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "bookings")
@Getter
@Setter
public class Bookings {

    @Id
    private String id;

    private String userId;
    private String studioId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private BookingStatus status;

    private double amount;

    private LocalDateTime createdAt;
}