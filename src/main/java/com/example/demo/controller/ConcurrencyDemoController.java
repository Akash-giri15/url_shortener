package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// TEMPORARY -- exists purely for the Checkpoint 7 deep dive.
// Safe (and advisable) to delete once you've seen the effect for yourself.
@RestController
public class ConcurrencyDemoController {

    private final Object monitor = new Object();

    // Deliberately broken: synchronized around a blocking call. On JDK 21,
    // this pins the calling virtual thread to its carrier for the full 50ms --
    // exactly the antipattern this checkpoint asks you to find and fix.
    @GetMapping("/api/demo/pinned")
    public String pinned() throws InterruptedException {
        synchronized (monitor) {
            Thread.sleep(50);
        }
        return "done";
    }

    // Identical blocking wait, no synchronized around it -- the virtual
    // thread yields its carrier for the duration instead of pinning it.
    @GetMapping("/api/demo/unpinned")
    public String unpinned() throws InterruptedException {
        Thread.sleep(50);
        return "done";
    }

    // Genuinely CPU-bound, no blocking at all. Virtual threads help with
    // WAITING, not computing -- this should show little to no difference
    // whether virtual threads are on or off.
    @GetMapping("/api/demo/cpu-bound")
    public long cpuBound() {
        long sum = 0;
        for (int i = 0; i < 50_000_000; i++) {
            sum += i;
        }
        return sum;
    }
}