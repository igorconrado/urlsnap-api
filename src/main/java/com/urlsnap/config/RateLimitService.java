package com.urlsnap.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RateLimitService {

    private final Cache<String, RequestWindow> windows;
    private final long maxRequests;
    private final long windowNanos;
    private final Ticker ticker;

    @Autowired
    public RateLimitService(
            @Value("${app.rate-limit.max-requests}") long maxRequests,
            @Value("${app.rate-limit.window-seconds}") long windowSeconds,
            @Value("${app.rate-limit.maximum-clients}") long maximumClients) {
        this(maxRequests, Duration.ofSeconds(windowSeconds), maximumClients, Ticker.systemTicker());
    }

    RateLimitService(long maxRequests, Duration window, long maximumClients, Ticker ticker) {
        if (maxRequests <= 0 || maximumClients <= 0 || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("Rate-limit values must be positive");
        }
        this.maxRequests = maxRequests;
        this.windowNanos = window.toNanos();
        this.ticker = ticker;
        windows = Caffeine.newBuilder()
                .maximumSize(maximumClients)
                .expireAfterWrite(window)
                .ticker(ticker)
                .build();
    }

    public RateLimitResult consume(String clientKey) {
        long now = ticker.read();
        RequestWindow window = windows.get(clientKey, ignored -> new RequestWindow(now));
        long count = window.count.incrementAndGet();
        long remaining = Math.max(0, maxRequests - count);
        long resetNanos = Math.max(0, windowNanos - (now - window.startedAtNanos));
        long resetSeconds = Math.max(1, Duration.ofNanos(resetNanos).toSeconds());
        return new RateLimitResult(count <= maxRequests, remaining, resetSeconds);
    }

    long estimatedClientCount() {
        windows.cleanUp();
        return windows.estimatedSize();
    }

    private static final class RequestWindow {
        private final long startedAtNanos;
        private final AtomicLong count = new AtomicLong();

        private RequestWindow(long startedAtNanos) {
            this.startedAtNanos = startedAtNanos;
        }
    }

    public record RateLimitResult(boolean allowed, long remaining, long resetSeconds) {
    }
}
