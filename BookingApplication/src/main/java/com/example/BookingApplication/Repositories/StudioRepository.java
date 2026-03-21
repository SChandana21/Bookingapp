package com.example.BookingApplication.Repositories;

import com.example.BookingApplication.Entity.Studio;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudioRepository extends MongoRepository<Studio, ObjectId> {
}
