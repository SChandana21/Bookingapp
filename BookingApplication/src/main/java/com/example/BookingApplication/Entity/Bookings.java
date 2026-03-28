package com.example.BookingApplication.Entity;

import com.example.BookingApplication.Enum.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "bookings")
@CompoundIndex(
        name = "unique_slot",
        def = "{'studioId': 1, 'startTime': 1, 'endTime': 1}",
        unique = true
)
@Getter
@Setter
public class Bookings {

    @Id
    private String id;
    private String sessionId;
    private String userId;
    private String studioId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime expiresAt;

    private BookingStatus status;

    private double amount;

    private LocalDateTime createdAt;
    private String studioName;
}