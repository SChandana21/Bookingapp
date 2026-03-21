package com.example.BookingApplication.Repositories;

import com.example.BookingApplication.Entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository  extends MongoRepository<User, ObjectId> {
    User findByName(String name);
}
