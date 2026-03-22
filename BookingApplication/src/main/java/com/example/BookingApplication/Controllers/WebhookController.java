package com.example.BookingApplication.Controllers;

import com.example.BookingApplication.Service.MemberService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/stripe")
public class WebhookController {

    @Autowired
    private MemberService memberService;
    private final String endpointSecret = "whsec_39477388743e3c28c9f0fda189f0d745191ff8ab322141f4f52968049fa487f6"; // replace with your CLI secret

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;


        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            System.out.println("❌ Invalid signature");
            return ResponseEntity.badRequest().body("Invalid signature");
        }


        System.out.println("📩 Webhook received: " + event.getType());


        if ("checkout.session.completed".equals(event.getType())) {

            String rawJson = event.getDataObjectDeserializer().getRawJson();

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(rawJson, JsonObject.class);

            String sessionId = jsonObject.get("id").getAsString();
            String bookingId = null;
            if (jsonObject.has("metadata") &&
                    jsonObject.get("metadata").getAsJsonObject().has("bookingId")) {

                bookingId = jsonObject.get("metadata")
                        .getAsJsonObject()
                        .get("bookingId")
                        .getAsString();
            }
            String paymentStatus = jsonObject.get("payment_status").getAsString();
            System.out.println("✅ Payment success!");
            System.out.println("Session ID: " + sessionId);
            System.out.println("Payment Status: " + paymentStatus);
            System.out.println("Booking Status" + bookingId);

            if ("paid".equals(paymentStatus) && bookingId != null) {
                System.out.println("🎯 Confirm booking for ID: " + bookingId);
                memberService.ConfirmBooking(bookingId);

            }


        }
        return ResponseEntity.ok("Success");
    }
}
