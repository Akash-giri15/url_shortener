package com.example.demo.config;

import com.example.demo.ratelimit.RateLimitInterceptor;
import com.example.demo.ratelimit.RateLimiter;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public WebConfig(RateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimiter, objectMapper))
                .addPathPatterns("/**"); // the "429 gate in front of everything"
    }
}