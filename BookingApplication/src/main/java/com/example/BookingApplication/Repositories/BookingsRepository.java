package com.example.BookingApplication.Repositories;

import com.example.BookingApplication.Entity.Bookings;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookingsRepository extends MongoRepository<Bookings, ObjectId> {
    Bookings findBySessionId(String sessionId);
}
