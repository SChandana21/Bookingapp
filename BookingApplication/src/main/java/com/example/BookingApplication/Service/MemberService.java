package com.example.BookingApplication.Service;

import com.example.BookingApplication.Entity.Bookings;
import com.example.BookingApplication.Entity.User;
import com.example.BookingApplication.Repositories.BookingsRepository;
import com.example.BookingApplication.Repositories.StudioQuery;
import com.example.BookingApplication.Repositories.StudioRepository;
import com.example.BookingApplication.Repositories.UserRepository;
import com.example.BookingApplication.Validation.InvalidtimeException;
import com.example.BookingApplication.Validation.SlotBookedException;
import com.example.BookingApplication.dto.BookingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberService {
    //payment

    @Autowired
    private BookingsRepository bookingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudioRepository studioRepository;

    @Autowired
    private StudioQuery studioQuery;



    public boolean ConflictDetection (BookingDTO bookingDto) {
        boolean Conflict = false;
        try {
            boolean conflictBookings = studioQuery.FindConflictBookings(bookingDto);
            if (conflictBookings) {
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return Conflict;
    }

    public boolean ValidRequest(BookingDTO bookingDto) {
        if (bookingDto.getStartTime().isBefore(bookingDto.getEndTime()) && bookingDto.getEndTime().getMinute() - bookingDto.getStartTime().getMinute() >= 30)
            return true;
        return false;
    }

    public boolean CreateBooking(BookingDTO bookingDto) throws SlotBookedException {
        boolean conflictDetection = ConflictDetection(bookingDto);
        if (conflictDetection)
            throw new SlotBookedException("Slot Booked");
        boolean validateRequest = ValidRequest(bookingDto);
        if (!validateRequest) {
            throw new InvalidtimeException("Please Check your time Slots, Minimum Duration must be 30minutes");
        }
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User member = userRepository.findByName(name);
        String memberId = member.getId();
        boolean Status = false;
        try {
            Bookings booking = new Bookings();
            booking.setCreatedAt(LocalDateTime.now());
            booking.setAmount(bookingDto.getAmount());
            booking.setStartTime(bookingDto.getStartTime());
            booking.setEndTime(bookingDto.getEndTime());
            booking.setStatus(bookingDto.getStatus());
            booking.setStudioId(bookingDto.getStudioId());
            booking.setUserId(memberId);
            bookingsRepository.save(booking);
            Status = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Status;
    }

    public boolean cancelBooking(BookingDTO bookingDTO) {
        boolean deletedStatus = false;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByName(name);
            String studioId = bookingDTO.getStudioId();
            LocalDateTime startTime = bookingDTO.getStartTime();
            LocalDateTime endTime = bookingDTO.getEndTime();
            List<Bookings> bookings = studioQuery.FindStudioBooking(bookingDTO);
            Bookings Booking = bookings.getFirst();
            if (Booking.getUserId().equals(user.getId())) {
                bookingsRepository.delete(Booking);
                deletedStatus = true;
            }


        }
        return deletedStatus;
    }
}
