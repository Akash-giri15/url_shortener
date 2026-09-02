package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.concurrent.Executors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VirtualThreadPinningTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate rest = new TestRestTemplate();

    @Test
    void comparePinnedVsUnpinnedThroughput() throws InterruptedException {
        String base = "http://localhost:" + port;

        long unpinnedMillis = timeConcurrentRequests(base + "/api/demo/unpinned", 500);
        long pinnedMillis = timeConcurrentRequests(base + "/api/demo/pinned", 500);

        System.out.println("Unpinned: " + unpinnedMillis + "ms for 500 requests");
        System.out.println("Pinned:   " + pinnedMillis + "ms for 500 requests");
        // No assertion on purpose -- this is a number to read, not a pass/fail
        // check. Compare the two yourself in Phase 4.
    }

    private long timeConcurrentRequests(String url, int count) throws InterruptedException {
        long start = System.currentTimeMillis();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < count; i++) {
                executor.submit(() -> rest.getForEntity(url, String.class));
            }
        } // try-with-resources on ExecutorService (JDK 19+) blocks here until all tasks finish
        return System.currentTimeMillis() - start;
    }
}
