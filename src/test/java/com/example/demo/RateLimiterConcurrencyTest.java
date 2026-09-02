package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimiterConcurrencyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final TestRestTemplate rest = new TestRestTemplate();

    @BeforeEach
    void resetBucket() {
        // Start every run with a full bucket -- otherwise a run right after a
        // previous one inherits a partially-drained bucket, and the numbers
        // below would look wrong for the wrong reason.
        redisTemplate.delete("ratelimit:127.0.0.1");
        redisTemplate.delete("naive-ratelimit:127.0.0.1");
    }

    @Test
    void fiftyConcurrentRequestsAgainstALimitOfTenLandNearTenSuccesses() throws InterruptedException {
        String url = "http://localhost:" + port + "/api/cache-stats";
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger rejections = new AtomicInteger();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 50; i++) {
                executor.submit(() -> {
                    ResponseEntity<String> response = rest.getForEntity(url, String.class);
                    if (response.getStatusCode().value() == 429) {
                        rejections.incrementAndGet();
                    } else {
                        successes.incrementAndGet();
                    }
                });
            }
        }

        System.out.println("Successes: " + successes.get() + ", Rejections: " + rejections.get());

        // With the atomic Lua version, this lands within 1 of exactly 10.
        // With the naive version, expect this to FAIL -- that failure is the point.
        assertTrue(Math.abs(successes.get() - 10) <= 1,
                "Expected successes within 1 of 10, got " + successes.get());
    }
}
