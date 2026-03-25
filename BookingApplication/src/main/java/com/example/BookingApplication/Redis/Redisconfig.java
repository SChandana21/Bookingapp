package com.example.BookingApplication.Redis;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
@Component
public class Redisconfig {

    @Autowired
    private RedisTemplate redisTemplate;

    public boolean AcquireLock(String studioId, LocalDateTime start, LocalDateTime end) {
        String key = "lock" + studioId + ":" + start + ":" + end;

        Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, "LOCKED", Duration.ofMinutes(1));

    return Boolean.TRUE.equals(locked);
    }


    public void releaseLock(String studioId, LocalDateTime start, LocalDateTime end) {
        String key = "lock:" + studioId + ":" + start + ":" + end;
        redisTemplate.delete(key);
    }
}
