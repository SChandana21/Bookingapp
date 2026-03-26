package com.example.BookingApplication.Controllers;

import com.example.BookingApplication.Entity.Bookings;
import com.example.BookingApplication.Entity.Studio;
import com.example.BookingApplication.Enum.BookingStatus;
import com.example.BookingApplication.Repositories.BookingsRepository;
import com.example.BookingApplication.Service.MemberService;
import com.example.BookingApplication.Service.PaymentService;
import com.example.BookingApplication.Service.UserService;
import com.example.BookingApplication.Validation.SlotBookedException;
import com.example.BookingApplication.dto.BookingDTO;
import com.example.BookingApplication.dto.PaymentDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Booking")
public class MemberController {
@Autowired
private MemberService memberService;

@Autowired
private BookingsRepository bookingsRepository;

@Autowired
private PaymentService paymentService;
@PostMapping("/new")
public ResponseEntity<Map<String, String>> CreateBooking (@RequestBody  BookingDTO bookingDTO) {
    Map<String, String> createdBooking = memberService.CreateBooking(bookingDTO);
    return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
        }

 @DeleteMapping("/cancelBooking")
 public ResponseEntity<String> DeleteBooking(@RequestBody BookingDTO bookingDTO) {
     boolean deleted = memberService.cancelBooking(bookingDTO);
     if (deleted)
         return new ResponseEntity<>(HttpStatus.ACCEPTED);
     return new ResponseEntity<>(HttpStatus.NOT_FOUND);
 }

    @GetMapping("/payment/session/{sessionId}")
    public Map<String, String> getSession(@PathVariable String sessionId) {
    try {
        Bookings booking = bookingsRepository.findBySessionId(sessionId);
        if (booking == null || booking.getExpiresAt() == null)
            return Map.of("expiry", "0");

        return Map.of("expiry", booking.getExpiresAt().toString());
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    }

    @GetMapping("/payment/status/{sessionId}")
    public Map<String, String> getStatus(@PathVariable String sessionId) {

        Bookings booking = bookingsRepository.findBySessionId(sessionId);

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return Map.of("status", "paid");
        }

        if (booking.getStatus() == BookingStatus.EXPIRED) {
            return Map.of("status", "expired");
        }

        return Map.of("status", "pending");
    }


    @GetMapping("/List")
    public ResponseEntity<?> GetallStudios() {
        try {
            List<Studio> studios = memberService.GetallStudios();
            return new ResponseEntity<>(studios, HttpStatus.FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    }


