package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CacheHitRatioTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate rest = new TestRestTemplate();

    @Test
    void hitRatioExceeds90PercentUnderZipfianAccess() {
        String base = "http://localhost:" + port;

        // Real link-sharing traffic is a small universe of URLs getting most of
        // the clicks -- not uniform random access. Seed 20 links to draw from.
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ResponseEntity<Map> resp = rest.postForEntity(base + "/api/shorten",
                    Map.of("url", "https://example.com/page" + i), Map.class);
            codes.add((String) resp.getBody().get("shortCode"));
        }

        // Baseline BEFORE this test's traffic -- stats are cumulative since app
        // start, so we measure the delta this run causes, not the running total.
        Map before = rest.getForObject(base + "/api/cache-stats", Map.class);
        long hitsBefore = ((Number) before.get("hits")).longValue();
        long missesBefore = ((Number) before.get("misses")).longValue();

        // 2,000 requests, Zipfian-distributed: codes.get(0) gets hit far more
        // often than codes.get(19).
        Random random = new Random(42); // fixed seed -- reproducible, not flaky
        for (int i = 0; i < 2000; i++) {
            rest.getForEntity(base + "/" + codes.get(zipfianRank(codes.size(), random)), Void.class);
        }

        Map after = rest.getForObject(base + "/api/cache-stats", Map.class);
        long hits = ((Number) after.get("hits")).longValue() - hitsBefore;
        long misses = ((Number) after.get("misses")).longValue() - missesBefore;
        double hitRatio = (double) hits / (hits + misses);

        System.out.println("Hit ratio for this run: " + hitRatio + " (" + hits + " hits, " + misses + " misses)");
        assertTrue(hitRatio > 0.90, "Expected hit ratio > 90%, got " + hitRatio);
    }

    // Crude but honest Zipfian sampler: weight of rank i is proportional to 1/(i+1)
    private int zipfianRank(int size, Random random) {
        double[] weights = new double[size];
        double total = 0;
        for (int i = 0; i < size; i++) { weights[i] = 1.0 / (i + 1); total += weights[i]; }
        double target = random.nextDouble() * total;
        double cumulative = 0;
        for (int i = 0; i < size; i++) {
            cumulative += weights[i];
            if (target <= cumulative) return i;
        }
        return size - 1;
    }
}