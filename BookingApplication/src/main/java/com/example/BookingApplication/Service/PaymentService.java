package com.example.BookingApplication.Service;

import com.example.BookingApplication.dto.PaymentDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentService {

    @Value("${Stripe_api_key}")
    private String secretkey;



    public Map<String, String> checkOut(PaymentDTO paymentDTO, String userId, String expiryTime) {

        Stripe.apiKey = secretkey;


        String name = paymentDTO.getName() != null ? paymentDTO.getName() : "Booking";
        Long amount = paymentDTO.getAmount() != null && paymentDTO.getAmount() > 0
                ? paymentDTO.getAmount()
                : 10000L; // fallback ₹1
        Long quantity = paymentDTO.getQuantity() != null && paymentDTO.getQuantity() > 0
                ? paymentDTO.getQuantity()
                : 1L;

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(name)
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("INR")
                        .setUnitAmount(amount)
                        .setProductData(productData)
                        .build();

        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setQuantity(quantity)
                        .setPriceData(priceData)
                        .build();

        SessionCreateParams params =
                SessionCreateParams.builder().putMetadata("bookingId",paymentDTO.getBookingId() != null ? paymentDTO.getBookingId() : "unknown").putMetadata("userId", userId).putMetadata("expiry", expiryTime)
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:3000/success")
                        .setCancelUrl("http://localhost:8080/cancel")
                        .addLineItem(lineItem)
                        .build();

        try {
            Session session = Session.create(params);
            return Map.of(
                    "url", session.getUrl(),
                    "sessionId", session.getId()
            );
        } catch (StripeException e) {
            throw new RuntimeException("Stripe Error: " + e.getMessage());
        }
    }
}