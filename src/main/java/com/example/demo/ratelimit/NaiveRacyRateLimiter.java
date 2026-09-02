package com.example.demo.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

// STEP 1 of this checkpoint -- deliberately broken, so you can feel the race
// condition firsthand rather than take it on faith. @Component stays here
// for now; you'll move it onto TokenBucketRateLimiter after you've watched
// this one fail in Sub-step 4.

public class NaiveRacyRateLimiter implements RateLimiter {

    private static final int LIMIT = 10;
    private final StringRedisTemplate redisTemplate;

    public NaiveRacyRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryConsume(String key) {
        String redisKey = "naive-ratelimit:" + key;

        String currentValue = redisTemplate.opsForValue().get(redisKey);   // GET
        int current = currentValue == null ? 0 : Integer.parseInt(currentValue);

        if (current >= LIMIT) {
            return false;
        }

        // Two threads can both read "current = 9" right here, both pass the
        // check above, and both proceed to increment. That gap is the bug.
        Long newValue = redisTemplate.opsForValue().increment(redisKey);   // INCR
        if (newValue != null && newValue == 1L) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(10));
        }
        return true;
    }
}