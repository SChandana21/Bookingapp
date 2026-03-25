package com.example.BookingApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDTO {

    private String bookingId;

    private String userId;

    private LocalDateTime Starttime;

    private LocalDateTime endTime;

    private String studioRecipient;

    private String subject;

    private String text;

    private String to;

}
