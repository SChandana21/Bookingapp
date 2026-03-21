package com.example.BookingApplication.Controllers;

import com.example.BookingApplication.Service.MemberService;
import com.example.BookingApplication.Service.UserService;
import com.example.BookingApplication.Validation.SlotBookedException;
import com.example.BookingApplication.dto.BookingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Booking")
public class MemberController {
@Autowired
private MemberService memberService;
@PostMapping("/new")
public ResponseEntity<?> CreateBooking (@RequestBody  BookingDTO bookingDTO) {
            memberService.CreateBooking(bookingDTO);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

 @DeleteMapping("/cancelBooking")
 public ResponseEntity<String> DeleteBooking(@RequestBody BookingDTO bookingDTO) {
     boolean deleted = memberService.cancelBooking(bookingDTO);
     if (deleted)
         return new ResponseEntity<>(HttpStatus.ACCEPTED);
     return new ResponseEntity<>(HttpStatus.NOT_FOUND);
 }
    }


