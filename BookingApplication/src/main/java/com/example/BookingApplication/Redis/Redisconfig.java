package com.example.BookingApplication.Redis;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;

public class Redisconfig {

    @Autowired
    private RedisTemplate redisTemplate;

    public boolean AcquireLock(String studioId, LocalDateTime start, LocalDateTime end) {
        String key = "lock" + studioId + ":" + start + ":" + end;

        Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, "LOCKED", Duration.ofMinutes(5));

    return Boolean.TRUE.equals(locked);
    }
}
