package com.urlsnap.url;

import com.github.benmanes.caffeine.cache.Ticker;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class UrlCacheServiceTest {
    @Test
    void supportsMissHitAndInvalidation() {
        var cache = new UrlCacheService(10, Duration.ofMinutes(1), Ticker.systemTicker());
        assertThat(cache.getUrl("code")).isNull();
        cache.saveUrl("code", "https://example.com");
        assertThat(cache.getUrl("code")).isEqualTo("https://example.com");
        cache.invalidateUrl("code");
        assertThat(cache.getUrl("code")).isNull();
    }

    @Test
    void entriesExpire() {
        var ticker = new MutableTicker();
        var cache = new UrlCacheService(10, Duration.ofSeconds(5), ticker);
        cache.saveUrl("code", "https://example.com");
        ticker.advance(Duration.ofSeconds(6));
        assertThat(cache.getUrl("code")).isNull();
    }

    @Test
    void maximumSizePreventsUnboundedGrowth() {
        var cache = new UrlCacheService(2, Duration.ofMinutes(1), Ticker.systemTicker());
        cache.saveUrl("one", "https://one.example");
        cache.saveUrl("two", "https://two.example");
        cache.saveUrl("three", "https://three.example");
        assertThat(cache.estimatedSize()).isLessThanOrEqualTo(2);
    }

    private static final class MutableTicker implements Ticker {
        private final AtomicLong nanos = new AtomicLong();
        public long read() { return nanos.get(); }
        void advance(Duration duration) { nanos.addAndGet(duration.toNanos()); }
    }
}
