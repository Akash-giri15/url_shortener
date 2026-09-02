package com.example.demo.ratelimit;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

// STEP 2 of this checkpoint -- the real, atomic implementation.
@Component
public class TokenBucketRateLimiter implements RateLimiter {

    private static final int CAPACITY = 10;         // max burst size
    private static final double REFILL_RATE = 1.0;  // tokens/sec -- 10 tokens per 10s window

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> script;

    public TokenBucketRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        DefaultRedisScript<Long> tokenBucketScript = new DefaultRedisScript<>();
        tokenBucketScript.setLocation(new ClassPathResource("scripts/token_bucket.lua"));
        tokenBucketScript.setResultType(Long.class);
        this.script = tokenBucketScript;
    }

    @Override
    public boolean tryConsume(String key) {
        String redisKey = "ratelimit:" + key;
        double now = System.currentTimeMillis() / 1000.0;

        Long result = redisTemplate.execute(script,
                List.of(redisKey),
                String.valueOf(now), String.valueOf(CAPACITY), String.valueOf(REFILL_RATE));

        return result != null && result == 1L;
    }
}