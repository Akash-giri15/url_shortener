package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity                     // tells JPA/Hibernate: this class maps to a table
@Table(name = "urls")       // explicit table name, matching the migration
public class Url {

    @Id                                                    // primary key field
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // let Postgres's BIGSERIAL generate it
    private Long id;

    @Column(name = "short_code", nullable = false)
    private String shortCode;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // JPA requires a no-arg constructor -- it builds instances via reflection, then
    // populates fields itself. Skip this and Hibernate throws an exception at startup.
    protected Url() {}

    // A convenience constructor for your own code to use before saving.
    public Url(String shortCode, String originalUrl) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}