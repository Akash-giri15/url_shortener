package com.example.demo.controller;

import com.example.demo.dto.ShortenRequest;
import com.example.demo.dto.UrlResponseDto;
import com.example.demo.model.Url;
import com.example.demo.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class UrlController {

    private final UrlService urlService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<UrlResponseDto> shorten(@Valid @RequestBody ShortenRequest request) {
        // If @Valid fails, GlobalExceptionHandler responds with 400 and this line never runs.
        Url saved = urlService.createShortUrl(request.getUrl());

        UrlResponseDto body = new UrlResponseDto(
                saved.getShortCode(),
                baseUrl + "/" + saved.getShortCode(),
                saved.getOriginalUrl()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body); // 201
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.resolveOriginalUrl(shortCode); // cache-aside happens here
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}