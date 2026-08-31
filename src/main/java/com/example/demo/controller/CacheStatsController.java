package com.example.demo.controller;

import com.example.demo.dto.CacheStatsDto;
import com.example.demo.service.UrlService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheStatsController {

    private final UrlService urlService;

    public CacheStatsController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/api/cache-stats")
    public CacheStatsDto stats() {
        return new CacheStatsDto(urlService.getCacheHits(), urlService.getCacheMisses());
    }
}
