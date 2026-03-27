package com.example.BookingApplication.Service;

import com.example.BookingApplication.Entity.User;
import com.example.BookingApplication.JwtUtil.Jwtutil;
import com.example.BookingApplication.Repositories.UserRepository;
import com.example.BookingApplication.Validation.EmptyDetailsException;
import com.example.BookingApplication.Validation.SlotBookedException;
import com.example.BookingApplication.Validation.UserAlreadyExistsException;
import com.example.BookingApplication.Validation.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password4j.BcryptPassword4jPasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.BookingApplication.Enum.Role.CREATOR;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsImpl userDetails;

    @Autowired
    private Jwtutil jwtutil;

    @Autowired
    private AuthenticationManager authenticationManager;

    public void CreateUser(User newuser) {
        if (newuser.getEmail().isBlank() && newuser.getName().isBlank() && newuser.getPassword().isBlank()) {
            throw new EmptyDetailsException("Please Provide Details");
        }
        User existinguser = userRepository.findByName(newuser.getName());
        User userByemail = userRepository.findByName(newuser.getEmail());
        if (existinguser!= null || userByemail!=null)
            throw new UserAlreadyExistsException("User Already exists");
        try {
            newuser.setPassword(passwordEncoder.encode(newuser.getPassword()));
            newuser.setRole(CREATOR);
            userRepository.save(newuser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String LoginUser(User user) {
        if (user.getName().isBlank() && user.getEmail().isBlank() && user.getPassword().isBlank())
            throw new EmptyDetailsException("User Blank Details");
        String token = null;
        String useremail = user.getEmail();
           String username = user.getName();
        User byName = userRepository.findByName(username);
        if (!byName.getEmail().equals( useremail) || !byName.getName().equals(username))
            throw new UserNotFoundException("No user with such Credentials, Please Check again!");
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword()));
            UserDetails userDetailsf = userDetails.loadUserByUsername(username);

            token = jwtutil.GenerateToken(userDetailsf);


        return token;
    }



}
