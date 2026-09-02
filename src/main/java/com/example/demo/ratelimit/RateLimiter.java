package com.example.demo.ratelimit;

public interface RateLimiter {
    boolean tryConsume(String key);
}