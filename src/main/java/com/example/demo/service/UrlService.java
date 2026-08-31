

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
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UrlService {

    private static final String CACHE_KEY_PREFIX = "url:";

    private final UrlRepository urlRepository;
    private final StringRedisTemplate redisTemplate;
    private final Duration cacheTtl;

    // AtomicLong, not a plain long -- multiple request threads hit these concurrently
    private final AtomicLong cacheHits = new AtomicLong();
    private final AtomicLong cacheMisses = new AtomicLong();

    public UrlService(UrlRepository urlRepository,
                      StringRedisTemplate redisTemplate,
                      @Value("${app.cache.ttl-seconds:3600}") long ttlSeconds) {
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

    // Cache-aside, written by hand: check Redis first; only a miss touches
    // Postgres, and every miss warms the cache before returning.
    public String resolveOriginalUrl(String shortCode) {
        String cacheKey = CACHE_KEY_PREFIX + shortCode;
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            cacheHits.incrementAndGet();
            return cached;                          // Postgres never touched on a hit
        }

        cacheMisses.incrementAndGet();
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        redisTemplate.opsForValue().set(cacheKey, url.getOriginalUrl(), cacheTtl);
        return url.getOriginalUrl();
    }

    public long getCacheHits() { return cacheHits.get(); }
    public long getCacheMisses() { return cacheMisses.get(); }
}