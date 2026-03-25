package com.example.BookingApplication.Controllers;

import com.example.BookingApplication.Entity.Studio;
import com.example.BookingApplication.Entity.User;
import com.example.BookingApplication.Service.MemberService;
import com.example.BookingApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/getstarted")
public class PublicControllers {

    @Autowired
    private MemberService memberService;
    @Autowired
    private UserService userService;

    @PostMapping("/createuser")
    public ResponseEntity<?> Signup(@RequestBody User newuser) {
        System.out.println("HIT");
        try {
            userService.CreateUser(newuser);
        } catch (Exception e) {
            return new ResponseEntity<>("User could not be created", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>("User Created", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> Login(@RequestBody User newuser) {
        try {
            String token = userService.LoginUser(newuser);
            return new ResponseEntity<>(token, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

    }

    @GetMapping("/List")
    public ResponseEntity<?> GetallStudios() {
        try {
            List<Studio> studios = memberService.GetallStudios();
            return new ResponseEntity<>(studios, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}
