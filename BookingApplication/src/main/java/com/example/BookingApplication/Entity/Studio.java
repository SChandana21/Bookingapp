package com.example.BookingApplication.Entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Getter
@Setter
@Document(collection = "studios")
public class Studio {

    @Id
    private String id;

    private String name;
    private String location;
    private double pricePerHour;

    private List<String> amenities; // mic, camera, lighting
}