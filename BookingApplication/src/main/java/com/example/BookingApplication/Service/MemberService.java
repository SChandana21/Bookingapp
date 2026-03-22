package com.example.BookingApplication.Service;

import com.example.BookingApplication.Entity.Bookings;
import com.example.BookingApplication.Entity.Studio;
import com.example.BookingApplication.Entity.User;
import com.example.BookingApplication.Enum.BookingStatus;
import com.example.BookingApplication.Repositories.BookingsRepository;
import com.example.BookingApplication.Repositories.StudioQuery;
import com.example.BookingApplication.Repositories.StudioRepository;
import com.example.BookingApplication.Repositories.UserRepository;
import com.example.BookingApplication.Validation.InvalidtimeException;
import com.example.BookingApplication.Validation.SlotBookedException;
import com.example.BookingApplication.dto.BookingDTO;

import com.example.BookingApplication.dto.PaymentDTO;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.BookingApplication.Enum.BookingStatus.EXPIRED;
import static com.example.BookingApplication.Enum.BookingStatus.PENDING;

@Slf4j
@Service
public class MemberService {
    //payment

    private static final Marker SUCCESS_MARKER = MarkerFactory.getMarker("BOOKING_SUCCESS");
    @Autowired
    private BookingsRepository bookingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudioRepository studioRepository;

    @Autowired
    private StudioQuery studioQuery;



  @Autowired
  private PaymentService paymentService;


    public boolean ConflictDetection(BookingDTO bookingDto) {
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

    public String CreateBooking(BookingDTO bookingDto) throws SlotBookedException {
        boolean conflictDetection = ConflictDetection(bookingDto);
        if (conflictDetection)
            throw new SlotBookedException("Slot Booked");

        boolean validateRequest = ValidRequest(bookingDto);
        if (!validateRequest)
            throw new InvalidtimeException("Please Check your time Slots, Minimum Duration must be 30minutes");
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User member = userRepository.findByName(name);
        String memberId = member.getId();
        Bookings booking = new Bookings();
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        booking.setCreatedAt(LocalDateTime.now());
        booking.setAmount(bookingDto.getAmount());
        booking.setStartTime(bookingDto.getStartTime());
        booking.setEndTime(bookingDto.getEndTime());
        booking.setStatus(PENDING);
        booking.setStudioId(bookingDto.getStudioId());
        booking.setUserId(memberId);
        Bookings saved = bookingsRepository.save(booking);
        String bookingId = saved.getId();
            studioQuery.ScheduleCancelled();
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setBookingId(bookingId);
            paymentDTO.setName(bookingDto.getStudioId());
            paymentDTO.setQuantity(1L);
            paymentDTO.setAmount(10000L);
            String paymentUrl = paymentService.checkOut(paymentDTO);
        return paymentUrl;

    }


    public boolean ConfirmBooking(String BookingId) {
        boolean Bookingstatus = false;
        ObjectId id = new ObjectId(BookingId);
        Bookings Bookingtoconfirm = bookingsRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (Bookingtoconfirm.getStatus() == PENDING) {
            if (Bookingtoconfirm.getExpiresAt().isAfter(LocalDateTime.now())) {
                Bookingtoconfirm.setStatus(BookingStatus.CONFIRMED);
                bookingsRepository.save(Bookingtoconfirm);
                Bookingstatus = true;
            } else {
                //log
                System.out.println("expired, ignored payment!");
            }
        }
        return Bookingstatus;
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


    public List<Studio> GetallStudios() {
        List<Studio> studioList = studioRepository.findAll();
        return studioList;
    }


}
