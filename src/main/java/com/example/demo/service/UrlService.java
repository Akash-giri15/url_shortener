package com.example.demo.service;

import com.example.demo.exception.UrlNotFoundException;
import com.example.demo.model.Url;
import com.example.demo.repository.UrlRepository;
import com.example.demo.util.Base62Encoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class UrlService {

    private static final String CACHE_KEY_PREFIX = "url:";

    private final UrlRepository urlRepository;
    private final StringRedisTemplate redisTemplate;
    private final Duration cacheTtl;

    private final AtomicLong cacheHits = new AtomicLong();
    private final AtomicLong cacheMisses = new AtomicLong();

    // One lock per short code, created on first use. ReentrantLock, not
    // synchronized -- synchronized would pin the calling virtual thread to
    // its OS carrier thread for as long as the lock is held (see Phase 2),
    // which defeats the entire point of virtual threads on this blocking I/O path.
    private final ConcurrentHashMap<String, ReentrantLock> keyLocks = new ConcurrentHashMap<>();

    public UrlService(UrlRepository urlRepository,
                      StringRedisTemplate redisTemplate,
                      @Value("${app.cache.ttl-seconds}") long ttlSeconds) {
        this.urlRepository = urlRepository;
        this.redisTemplate = redisTemplate;
        this.cacheTtl = Duration.ofSeconds(ttlSeconds);
    }

    @Transactional
    public Url createShortUrl(String originalUrl) {
        Url url = new Url("PENDING", originalUrl);
        Url saved = urlRepository.save(url);
        saved.setShortCode(Base62Encoder.encode(saved.getId()));
        return urlRepository.save(saved);
    }

    public String resolveOriginalUrl(String shortCode) {
        String cacheKey = CACHE_KEY_PREFIX + shortCode;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            cacheHits.incrementAndGet();
            return cached;
        }

        // Cache miss: only the FIRST concurrent request for THIS key should
        // reach Postgres. Everyone else waits here, briefly, instead of
        // independently racing to the database.
        ReentrantLock lock = keyLocks.computeIfAbsent(shortCode, k -> new ReentrantLock());
        lock.lock();
        try {
            // Double-check: while we were waiting for the lock, the request
            // that got there first may have already warmed the cache.
            cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                cacheHits.incrementAndGet();
                return cached;
            }

            cacheMisses.incrementAndGet();   // only the true winner increments this
            Url url = urlRepository.findByShortCode(shortCode)
                    .orElseThrow(() -> new UrlNotFoundException(shortCode));

            redisTemplate.opsForValue().set(cacheKey, url.getOriginalUrl(), cacheTtl);
            return url.getOriginalUrl();
        } finally {
            lock.unlock();
        }
    }

    public long getCacheHits() { return cacheHits.get(); }
    public long getCacheMisses() { return cacheMisses.get(); }
}