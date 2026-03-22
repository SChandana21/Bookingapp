package com.example.BookingApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeResponse {
     private String status;
     private String message;
     private String sessionId;
     private String sessionUrl;
}
