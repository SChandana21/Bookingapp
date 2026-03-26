package com.example.BookingApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private Long amount;
    private Long quantity = 10000L;
    private String name;
    private String currency;
    private String bookingId;

}
