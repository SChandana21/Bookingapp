package com.example.BookingApplication.Repositories;
import com.example.BookingApplication.Entity.Bookings;
import com.example.BookingApplication.Entity.User;
import com.example.BookingApplication.Redis.Redisconfig;
import com.example.BookingApplication.dto.BookingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.example.BookingApplication.Enum.BookingStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class StudioQuery {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Redisconfig redisconfig;

    public boolean FindConflictBookings(BookingDTO currentBooking) {
        boolean conflict = false;
        Query query = new Query();
        Criteria starttime = Criteria.where("startTime").lt(currentBooking.getEndTime());
        Criteria endtime = Criteria.where("endTime").gt(currentBooking.getStartTime());
        Criteria currentlyProcessing = Criteria.where("status").is("PENDING");
        Criteria Bookingdone = Criteria.where("status").is("CONFIRMED");
       query.addCriteria(new Criteria().andOperator(starttime, endtime, currentlyProcessing, Bookingdone));
        List<Bookings> bookings = mongoTemplate.find(query, Bookings.class);
        if (!bookings.isEmpty()) {
            conflict = true;
        }
        return conflict;
    }

    @Scheduled(fixedRate = 60000)
    public void ScheduleCancelled() {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("status").is("PENDING"),
                Criteria.where("expiresAt").lt(LocalDateTime.now())
        );

        Query query = new Query(criteria);

        List<Bookings> bookings = mongoTemplate.find(query, Bookings.class);

        for (Bookings booking : bookings) {

            booking.setStatus(BookingStatus.EXPIRED);


            redisconfig.releaseLock(
                    booking.getStudioId(),
                    booking.getStartTime(),
                    booking.getEndTime()
            );

            mongoTemplate.save(booking);
        }
    }

    @Scheduled(fixedRate = 300000)
    public void releaseExpiredLocks() {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

        Query query = new Query(
                Criteria.where("status").is("PENDING")
                        .and("expiresAt").lt(now)
        );

        List<Bookings> expiredBookings =
                mongoTemplate.find(query, Bookings.class);

        for (Bookings booking : expiredBookings) {

            try {

                redisconfig.releaseLock(
                        booking.getStudioId(),
                        booking.getStartTime(),
                        booking.getEndTime()
                );

                booking.setStatus(BookingStatus.EXPIRED);
                mongoTemplate.save(booking);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<Bookings> FindStudioBooking(BookingDTO bookingDTO) {
    Query query = new Query();
    Criteria FindStudio = Criteria.where("studioId").is(bookingDTO.getStudioId());
    Criteria findStarttime = Criteria.where("startTime").eq(bookingDTO.getStartTime());
    Criteria findendTime = Criteria.where("endTime").eq(bookingDTO.getEndTime());
    query.addCriteria(new Criteria().andOperator(FindStudio, findStarttime, findendTime));
        List<Bookings> bookings = mongoTemplate.find(query, Bookings.class);
        return bookings;
    }

    public List<Bookings> FindUserBookings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentuser = null;
        if (authentication != null && authentication.isAuthenticated()) {
            String name = authentication.getName();
            currentuser = userRepository.findByName(name);
            Criteria userCriteria = Criteria.where("userId").is(currentuser.getId());
            Query userBookings = new Query(userCriteria);
            List<Bookings> bookings = mongoTemplate.find(userBookings, Bookings.class);
            return bookings;
        }
        throw new RuntimeException("Not an Authenticated User"); //validation
    }
}
