package com.example.BookingApplication.Service;

import com.example.BookingApplication.Entity.Studio;
import com.example.BookingApplication.dto.EmailDTO;
import org.springframework.mail.javamail.JavaMailSender;

public interface EmailService  {
    public String sendSimpleMail(EmailDTO studiosendMail);
}
