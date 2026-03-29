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
import com.example.BookingApplication.Validation.InvalidDetailsException;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.awt.print.Book;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    public void ValidRequest(BookingDTO bookingDto) {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

        LocalDateTime start = bookingDto.getStartTime();
        LocalDateTime end = bookingDto.getEndTime();

        if (start == null || end == null) {
            throw new InvalidtimeException("Cannot be Empty");
        }

        if (start.isBefore(now)) {
            throw new InvalidtimeException("Cannot book in the past");
        }

        if (!end.isAfter(start)) {
            throw new InvalidtimeException("Cannot be Earlier than start");
        }

        if (Duration.between(start, end).toMinutes() < 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot be less than 30m");
        }


    }
@Transactional
    public Map<String, String> CreateBooking(BookingDTO bookingDto) throws SlotBookedException {
        boolean conflictDetection = ConflictDetection(bookingDto);
        if (conflictDetection)
            throw new SlotBookedException("Slot Booked");

        ValidRequest(bookingDto);
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
        ObjectId studioId = new ObjectId(bookingDto.getStudioId());
        Studio selectedStudio  = studioRepository.findById(studioId).orElse(null);
        booking.setStudioName(selectedStudio.getName());
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String bookingId = saved.getId();
            studioQuery.ScheduleCancelled();
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setBookingId(bookingId);
            paymentDTO.setName(bookingDto.getStudioId());
            paymentDTO.setQuantity(1L);
        double amountd = bookingDto.getAmount();
        Long amount = (long) amountd;
    paymentDTO.setAmount(amount);
            Map<String, String> paymentUrl = paymentService.checkOut(paymentDTO, memberId, str, booking);
        return paymentUrl;

    }

    @Transactional
    public boolean ConfirmBooking(String BookingId, String userId)   {
        boolean Bookingstatus = false;
        ObjectId id = new ObjectId(BookingId);
        Bookings Bookingtoconfirm = bookingsRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));

        if (Bookingtoconfirm.getStatus() == BookingStatus.CONFIRMED) {
            return true;
        }

        if (Bookingtoconfirm.getStatus() == PENDING) {
            if (Bookingtoconfirm.getExpiresAt() != null && Bookingtoconfirm.getExpiresAt().plusSeconds(30).isBefore(LocalDateTime.now())) {
                Bookingtoconfirm.setStatus(EXPIRED);

                bookingsRepository.save(Bookingtoconfirm);
                return false;
            } else {
                Bookingtoconfirm.setStatus(BookingStatus.CONFIRMED);
                Bookingtoconfirm.setExpiresAt(null);
                bookingsRepository.save(Bookingtoconfirm);
               SendEmail(Bookingtoconfirm, userId);
                Bookingstatus = true;
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


    @Transactional
    public void SendEmail (Bookings Bookingdone, String userId) {
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setBookingId(Bookingdone.getId());
        emailDTO.setStarttime(Bookingdone.getStartTime());
        emailDTO.setEndTime(Bookingdone.getEndTime());
        ObjectId userIdo = new ObjectId(userId);
        User byName = userRepository.findById(userIdo).orElse(null);

        String studioId = Bookingdone.getStudioId();
        ObjectId studioid = new ObjectId(studioId);
        Studio byId = studioRepository.findById(studioid).orElse(null);
        String recipient = (byId.getStudiorecipient());
        emailDTO.setTo(recipient);
        emailDTO.setText("Your studio has been booked!"+ "at" +  Bookingdone.getStartTime() + "till" + Bookingdone.getEndTime() + "by" + Bookingdone.getUserId());
        String userEmail = byName.getEmail();
        emailDTO.setUserId(userId);
        emailDTO.setStudioRecipient(recipient);
        emailServiceIMPL.sendSimpleMail(emailDTO);
        EmailDTO userEmailsend = new EmailDTO();
        userEmailsend.setTo(userEmail);
        userEmailsend.setStudioRecipient(recipient);
        userEmailsend.setBookingId(Bookingdone.getId());
        userEmailsend.setStarttime(Bookingdone.getStartTime());
        userEmailsend.setEndTime(Bookingdone.getEndTime());
        userEmailsend.setSubject("Booking Confirmation");
        userEmailsend.setText("Congratulations you booked " + byId.getName() + "at" + Bookingdone.getStartTime() + Bookingdone.getEndTime());
        emailServiceIMPL.sendSimpleMail(userEmailsend);
    }


}
