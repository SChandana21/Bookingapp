package com.example.BookingApplication.Entity;

import com.example.BookingApplication.Enum.PaymentStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String bookingId;

    private String razorpayOrderId;
    private String razorpayPaymentId;

    private double amount;

    private PaymentStatus status;
}
