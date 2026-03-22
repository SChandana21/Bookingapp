package com.example.BookingApplication.Controllers;

import com.example.BookingApplication.Entity.Studio;
import com.example.BookingApplication.Service.MemberService;
import com.example.BookingApplication.Service.PaymentService;
import com.example.BookingApplication.Service.UserService;
import com.example.BookingApplication.Validation.SlotBookedException;
import com.example.BookingApplication.dto.BookingDTO;
import com.example.BookingApplication.dto.PaymentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Booking")
public class MemberController {
@Autowired
private MemberService memberService;

@Autowired
private PaymentService paymentService;
@PostMapping("/new")
public ResponseEntity<String> CreateBooking (@RequestBody  BookingDTO bookingDTO) {
    String checkoutUrl = memberService.CreateBooking(bookingDTO);
    return new ResponseEntity<>(checkoutUrl,    HttpStatus.CREATED);
        }

 @DeleteMapping("/cancelBooking")
 public ResponseEntity<String> DeleteBooking(@RequestBody BookingDTO bookingDTO) {
     boolean deleted = memberService.cancelBooking(bookingDTO);
     if (deleted)
         return new ResponseEntity<>(HttpStatus.ACCEPTED);
     return new ResponseEntity<>(HttpStatus.NOT_FOUND);
 }

 @PostMapping("/checkout")
 public String Checkout(PaymentDTO paymentDTO) {
    return paymentService.checkOut(paymentDTO);
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


