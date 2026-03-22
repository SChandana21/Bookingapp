package com.example.BookingApplication.Repositories;
import com.example.BookingApplication.Entity.Bookings;
import com.example.BookingApplication.dto.BookingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.BookingApplication.Enum.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.BookingApplication.Enum.BookingStatus.EXPIRED;

@Component
public class StudioQuery {
    @Autowired
    private MongoTemplate mongoTemplate;


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
        Criteria expiredBooking = Criteria.where("status").is("PENDING");
        Query query = new Query(expiredBooking);
        List<Bookings> bookings = mongoTemplate.find(query, Bookings.class);

        for (Bookings booking : bookings) {

            if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
                booking.setStatus(BookingStatus.EXPIRED);
                mongoTemplate.save(booking);
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
}
