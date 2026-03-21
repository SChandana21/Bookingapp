package com.example.BookingApplication.Repositories;
import com.example.BookingApplication.Entity.Bookings;
import com.example.BookingApplication.dto.BookingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class StudioQuery {
    @Autowired
    private MongoTemplate mongoTemplate;


    public boolean FindConflictBookings(BookingDTO currentBooking) {
        boolean conflict = false;
        Query query = new Query();
        Criteria starttime = Criteria.where("startTime").lt(currentBooking.getEndTime());
        Criteria endtime = Criteria.where("endTime").gt(currentBooking.getStartTime());
       query.addCriteria(new Criteria().andOperator(starttime, endtime));
        List<Bookings> bookings = mongoTemplate.find(query, Bookings.class);
        if (!bookings.isEmpty()) {
            conflict = true;
        }
        return conflict;
    }

    public List<Bookings> FindStudioBooking(BookingDTO bookingDTO) {
    Query query = new Query();
    Criteria FindStudio = Criteria.where("studioId").eq(bookingDTO.getStudioId());
    Criteria findStarttime = Criteria.where("startTime").eq(bookingDTO.getStartTime());
    Criteria findendTime = Criteria.where("endTime").eq(bookingDTO.getEndTime());
    query.addCriteria(new Criteria().andOperator(FindStudio, findStarttime, findendTime));
        List<Bookings> bookings = mongoTemplate.find(query, Bookings.class);
        return bookings;
    }
}
