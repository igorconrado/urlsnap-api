package com.urlsnap.url;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class UrlCacheService {

    private final Cache<String, String> redirects;

    @Autowired
    public UrlCacheService(
            @Value("${app.cache.maximum-size}") long maximumSize,
            @Value("${app.cache.ttl-seconds}") long ttlSeconds) {
        this(maximumSize, Duration.ofSeconds(ttlSeconds), Ticker.systemTicker());
    }

    UrlCacheService(long maximumSize, Duration ttl, Ticker ticker) {
        if (maximumSize <= 0 || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("Cache size and TTL must be positive");
        }
        redirects = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(ttl)
                .ticker(ticker)
                .build();
    }

    public String getUrl(String shortCode) {
        return redirects.getIfPresent(shortCode);
    }

    public void saveUrl(String shortCode, String originalUrl) {
        redirects.put(shortCode, originalUrl);
    }

    public void invalidateUrl(String shortCode) {
        redirects.invalidate(shortCode);
    }

    long estimatedSize() {
        redirects.cleanUp();
        return redirects.estimatedSize();
    }
}
