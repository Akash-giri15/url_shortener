package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CacheStampedeTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate rest = new TestRestTemplate();

    @Test
    void fiveHundredConcurrentRequestsOnColdKeyProduceExactlyOneMiss() throws InterruptedException {
        String base = "http://localhost:" + port;

        // A brand-new URL is guaranteed cold -- POST never touches Redis, only GET does.
        Map<?, ?> created = rest.postForObject(base + "/api/shorten",
                Map.of("url", "https://example.com/stampede-test"), Map.class);
        String shortCode = (String) created.get("shortCode");

        Map<?, ?> before = rest.getForObject(base + "/api/cache-stats", Map.class);
        long missesBefore = ((Number) before.get("misses")).longValue();

        int requestCount = 500;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < requestCount; i++) {
                executor.submit(() -> rest.getForEntity(base + "/" + shortCode, Void.class));
            }
        }

        Map<?, ?> after = rest.getForObject(base + "/api/cache-stats", Map.class);
        long missesDuring = ((Number) after.get("misses")).longValue() - missesBefore;

        System.out.println("Postgres queries for the cold key: " + missesDuring);
        assertEquals(1, missesDuring, "Expected exactly one Postgres query for the cold key");
    }
}
