package com.example.BookingApplication.Entity;
import com.example.BookingApplication.Enum.Role;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
    public class User {

        @Id
        private String id;
    @NonNull
    private String name;
        @NonNull
        private String email;
        @NonNull
        private String password;
        private Role role;
    }

