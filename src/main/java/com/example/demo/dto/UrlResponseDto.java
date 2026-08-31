package com.example.demo.dto;

// What we send BACK. No id, no createdAt -- internal database details the
// client has no reason to see, and that we're free to change later without
// it counting as a breaking API change.
public class UrlResponseDto {

    private final String shortCode;
    private final String shortUrl;
    private final String originalUrl;

    public UrlResponseDto(String shortCode, String shortUrl, String originalUrl) {
        this.shortCode = shortCode;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
    }

    public String getShortCode() { return shortCode; }
    public String getShortUrl() { return shortUrl; }
    public String getOriginalUrl() { return originalUrl; }
}
