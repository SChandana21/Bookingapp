package com.example.BookingApplication.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class Jwtutil {



    private String SECRET_KEY = "msjFThnpWWpeOWAESLzuNmeBhl9TTMMCc2PSPylbstn";

    public SecretKey Getsigningkey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String Createtoken(Map<String, Object> claims, String subject) {
        return Jwts.builder().claims(claims).subject(subject).header().empty().add("typ", "JWT").and().issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)).signWith(Getsigningkey()).compact();
    }

    public String GenerateToken(UserDetails userDetails) {
        Map <String, Object> claims = new HashMap<>();
        return Createtoken(claims, userDetails.getUsername());
    }

    public boolean isvalidtoken(String token) {
        return !isExpiredtoken(token);
    }

    public boolean isExpiredtoken(String token) {
        return Extractexpiredtoken(token).before(new Date());
    }

    public Date Extractexpiredtoken(String token) {
    return Extractallclaims(token).getExpiration();
    }

    public Claims Extractallclaims(String token) {
    return Jwts.parser().verifyWith(Getsigningkey()).build().parseSignedClaims(token).getBody();
    }

    public String Extractusername(String token) {
        return Extractallclaims(token).getSubject();
    }


}
