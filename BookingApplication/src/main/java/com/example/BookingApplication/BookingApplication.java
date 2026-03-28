package com.example.BookingApplication;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookingApplication {

    @Autowired
    private RedisTemplate redisTemplate;



    @PostConstruct
    public void testRedis() {
        try {
            redisTemplate.opsForValue().set("test-key", "hello");
            String value = (String) redisTemplate.opsForValue().get("test-key");
            System.out.println("Redis working: " + value);
            System.out.println(redisTemplate.getConnectionFactory().getConnection().getClientName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	public static void main(String[] args) {

        SpringApplication.run(BookingApplication.class, args);
	}

}
