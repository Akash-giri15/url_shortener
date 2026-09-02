package com.example.demo.ratelimit;

import com.example.demo.exception.ErrorResponse;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public RateLimitInterceptor(RateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // No real client identity in this project yet, so IP is the rate-limit
        // key -- a real system might key on an API token instead.
        String clientKey = request.getRemoteAddr();

        if (rateLimiter.tryConsume(clientKey)) {
            return true; // continue on to the controller
        }

        // The same ErrorResponse shape GlobalExceptionHandler already uses --
        // one consistent error contract across the whole API, not two.
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = new ErrorResponse(HttpStatus.TOO_MANY_REQUESTS.value(), "Rate limit exceeded");
        response.getWriter().write(objectMapper.writeValueAsString(body));
        return false; // stop here -- the controller never runs
    }
}