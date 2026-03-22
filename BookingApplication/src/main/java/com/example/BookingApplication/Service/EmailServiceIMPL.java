package com.example.BookingApplication.Service;

import com.example.BookingApplication.Entity.Studio;
import com.example.BookingApplication.dto.EmailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceIMPL implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

@Override
public String sendSimpleMail(EmailDTO emaildetails) {
try {
    SimpleMailMessage simpleMailMessage  = new SimpleMailMessage();
    simpleMailMessage.setFrom(sender);
    simpleMailMessage.setSubject("Your studio has been booked!");
    simpleMailMessage.setTo(emaildetails.getStudioRecipient());
    javaMailSender.send(simpleMailMessage);
    return "Mail sent to the studio!";
} catch (Exception e) {
    return "Could not send the mail";
}
}
}
