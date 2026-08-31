package com.example.demo.dto;

public class CacheStatsDto {
    private final long hits;
    private final long misses;
    private final double hitRatio;

    public CacheStatsDto(long hits, long misses) {
        this.hits = hits;
        this.misses = misses;
        long total = hits + misses;
        this.hitRatio = total == 0 ? 0.0 : (double) hits / total;
    }

    public long getHits() { return hits; }
    public long getMisses() { return misses; }
    public double getHitRatio() { return hitRatio; }
}