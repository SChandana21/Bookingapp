package com.example.BookingApplication.Filter;

import com.example.BookingApplication.Entity.User;
import com.example.BookingApplication.JwtUtil.Jwtutil;
import com.example.BookingApplication.Repositories.UserRepository;
import com.example.BookingApplication.Service.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class Filterjwt extends OncePerRequestFilter {

    @Autowired
    private Jwtutil jwtutil;

    @Autowired
    private UserDetailsImpl userDetailsimpl;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException {
        String authorization = request.getHeader("Authorization");
        String token = null;
        String username = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
             token = authorization.substring(7);
                 username = jwtutil.Extractusername(token);
                UserDetails userDetails = userDetailsimpl.loadUserByUsername(username);
            boolean isvalidtoken = jwtutil.isvalidtoken(token);
                if (isvalidtoken) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

        }
        try {
filterChain.doFilter(request, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
