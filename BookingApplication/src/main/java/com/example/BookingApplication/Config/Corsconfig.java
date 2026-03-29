package com.example.BookingApplication.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class Corsconfig implements WebMvcConfigurer {

@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/getstarted/**").allowedOrigins("http://localhost:3000").allowedOrigins("https://sweet-klepon-b0c33a.netlify.app/").allowedMethods("GET", "POST", "PUT", "DELETE").allowedHeaders("*").allowCredentials(true);
        registry.addMapping("/Booking/**").allowedOrigins("http://localhost:3000").allowedOrigins("https://sweet-klepon-b0c33a.netlify.app/").allowedMethods("GET", "POST", "PUT", "DELETE").allowedHeaders("*").allowCredentials(true);

}
}
