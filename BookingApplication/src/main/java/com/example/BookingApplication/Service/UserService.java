package com.example.BookingApplication.Service;

import com.example.BookingApplication.Entity.User;
import com.example.BookingApplication.JwtUtil.Jwtutil;
import com.example.BookingApplication.Repositories.UserRepository;
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
        try {
            newuser.setPassword(passwordEncoder.encode(newuser.getPassword()));
            newuser.setRole(CREATOR);
            userRepository.save(newuser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String LoginUser(User user) {
        String username = user.getName();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword()));
        UserDetails userDetailsf = userDetails.loadUserByUsername(username);
        String token = jwtutil.GenerateToken(userDetailsf);
        return token;
    }



}
