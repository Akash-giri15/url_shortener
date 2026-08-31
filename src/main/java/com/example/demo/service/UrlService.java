package com.example.demo.service;

import com.example.demo.exception.UrlNotFoundException;
import com.example.demo.model.Url;
import com.example.demo.repository.UrlRepository;
import com.example.demo.util.Base62Encoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UrlService {

    private final UrlRepository urlRepository;

    // Constructor injection -- the same DI pattern from Checkpoint 1, now injecting
    // the repository Spring Data JPA generated for you back in Checkpoint 2.
    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Transactional // both saves below succeed together or roll back together -- no half-written row
    public Url createShortUrl(String originalUrl) {
        // The chicken-and-egg problem from Phase 2: we want the id as the short code,
        // but the id doesn't exist until after the row is inserted. So: insert with a
        // placeholder, then update once Postgres hands us the real id.
        // (Checkpoint 4 keeps this exact two-step shape -- it just replaces
        // String.valueOf(id) with a Base62-encoded version of the same id.)
        Url url = new Url("PENDING", originalUrl);
        Url saved = urlRepository.save(url);              // INSERT -- id generated here

        saved.setShortCode(Base62Encoder.encode(saved.getId()));  // <-- was String.valueOf(saved.getId())
        return urlRepository.save(saved);                  // UPDATE -- short_code now set
    }

    public Url getByShortCode(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
    }
}