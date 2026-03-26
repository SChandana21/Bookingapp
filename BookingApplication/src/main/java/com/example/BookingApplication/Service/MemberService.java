package com.example.BookingApplication.Service;

import com.example.BookingApplication.Entity.Bookings;
import com.example.BookingApplication.Entity.Studio;
import com.example.BookingApplication.Entity.User;
import com.example.BookingApplication.Enum.BookingStatus;
import com.example.BookingApplication.Redis.Redisconfig;
import com.example.BookingApplication.Repositories.BookingsRepository;
import com.example.BookingApplication.Repositories.StudioQuery;
import com.example.BookingApplication.Repositories.StudioRepository;
import com.example.BookingApplication.Repositories.UserRepository;
import com.example.BookingApplication.Validation.InvalidtimeException;
import com.example.BookingApplication.Validation.SlotBookedException;
import com.example.BookingApplication.dto.BookingDTO;

import com.example.BookingApplication.dto.EmailDTO;
import com.example.BookingApplication.dto.PaymentDTO;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private EmailServiceIMPL emailServiceIMPL;


    @Autowired
    private Redisconfig redisconfig;
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
        if (bookingDto.getStartTime().isBefore(bookingDto.getEndTime()) && Duration.between(bookingDto.getStartTime(), bookingDto.getEndTime()).toMinutes() >= 30)
            return true;
        return false;
    }

    public Map<String, String> CreateBooking(BookingDTO bookingDto) throws SlotBookedException {
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
        String str = booking.getExpiresAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        booking.setCreatedAt(LocalDateTime.now());
        booking.setAmount(bookingDto.getAmount());
        booking.setStartTime(bookingDto.getStartTime());
        booking.setEndTime(bookingDto.getEndTime());
        booking.setStatus(PENDING);
        booking.setStudioId(bookingDto.getStudioId());
        booking.setUserId(memberId);
        Bookings saved = null;

            boolean locked = redisconfig.AcquireLock(
                    bookingDto.getStudioId(),
                    bookingDto.getStartTime(),
                    bookingDto.getEndTime()
            );

            if (!locked) {
                throw new SlotBookedException("Slot already locked");
            }
        try {
            saved = bookingsRepository.save(booking);

        } finally {
            redisconfig.releaseLock(
                    bookingDto.getStudioId(),
                    bookingDto.getStartTime(),
                    bookingDto.getEndTime()
            );
        }
        String bookingId = saved.getId();
            studioQuery.ScheduleCancelled();
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setBookingId(bookingId);
            paymentDTO.setName(bookingDto.getStudioId());
            paymentDTO.setQuantity(1L);
            paymentDTO.setAmount(10000L);
            Map<String, String> paymentUrl = paymentService.checkOut(paymentDTO, memberId, str, booking);
        return paymentUrl;

    }


    public boolean ConfirmBooking(String BookingId, String userId)   {
        boolean Bookingstatus = false;
        ObjectId id = new ObjectId(BookingId);
        Bookings Bookingtoconfirm = bookingsRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (Bookingtoconfirm.getStatus() == PENDING) {
            if (Bookingtoconfirm.getExpiresAt() != null && Bookingtoconfirm.getExpiresAt().plusSeconds(30).isBefore(LocalDateTime.now())) {
                Bookingtoconfirm.setStatus(EXPIRED);
                bookingsRepository.save(Bookingtoconfirm);
                return false;
            } else {
                Bookingtoconfirm.setStatus(BookingStatus.CONFIRMED);
                Bookingtoconfirm.setExpiresAt(null);
                bookingsRepository.save(Bookingtoconfirm);
                ObjectId userIdo = new ObjectId(userId);
                User byName = userRepository.findById(userIdo).orElse(null);

                String studioId = Bookingtoconfirm.getStudioId();
                ObjectId studioid = new ObjectId(studioId);
                Studio byId = studioRepository.findById(studioid).orElse(null);
                String recipient = (byId.getStudiorecipient());
                Bookingstatus = true;
                EmailDTO emailDTO = new EmailDTO();
                emailDTO.setBookingId(Bookingtoconfirm.getId());
                emailDTO.setStarttime(Bookingtoconfirm.getStartTime());
                emailDTO.setEndTime(Bookingtoconfirm.getEndTime());
                emailDTO.setTo(recipient);
                emailDTO.setText("Your studio has been booked!"+ "at" +  Bookingtoconfirm.getStartTime() + "till" + Bookingtoconfirm.getEndTime() + "by" + Bookingtoconfirm.getUserId());
                String userEmail = byName.getEmail();
                emailDTO.setUserId(userId);
                emailDTO.setStudioRecipient(recipient);
                emailServiceIMPL.sendSimpleMail(emailDTO);
                EmailDTO userEmailsend = new EmailDTO();
                userEmailsend.setTo(userEmail);
                userEmailsend.setStudioRecipient(recipient);
                userEmailsend.setBookingId(Bookingtoconfirm.getId());
                userEmailsend.setStarttime(Bookingtoconfirm.getStartTime());
                userEmailsend.setEndTime(Bookingtoconfirm.getEndTime());
                userEmailsend.setSubject("Booking Confirmation");
                userEmailsend.setText("Congratulations you booked " + byId.getName() + "at" + Bookingtoconfirm.getStartTime() + Bookingtoconfirm.getEndTime());
                emailServiceIMPL.sendSimpleMail(userEmailsend);

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
