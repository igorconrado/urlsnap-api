package com.urlsnap.config;

import com.github.benmanes.caffeine.cache.Ticker;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitServiceTest {
    @Test
    void limitsRequestsWithinWindowAndReportsHeaders() {
        var service = new RateLimitService(2, Duration.ofSeconds(60), 100, Ticker.systemTicker());
        var first = service.consume("client");
        assertThat(first.allowed()).isTrue();
        assertThat(first.remaining()).isEqualTo(1);
        assertThat(first.resetSeconds()).isBetween(59L, 60L);
        var second = service.consume("client");
        assertThat(second.allowed()).isTrue();
        assertThat(second.remaining()).isZero();
        assertThat(service.consume("client").allowed()).isFalse();
    }

    @Test
    void renewsWindowAfterExpiration() {
        var ticker = new MutableTicker();
        var service = new RateLimitService(1, Duration.ofSeconds(10), 100, ticker);
        assertThat(service.consume("client").allowed()).isTrue();
        assertThat(service.consume("client").allowed()).isFalse();
        ticker.advance(Duration.ofSeconds(11));
        assertThat(service.consume("client")).isEqualTo(new RateLimitService.RateLimitResult(true, 0, 10));
    }

    @Test
    void isolatesClientsAndBoundsMemory() {
        var service = new RateLimitService(1, Duration.ofMinutes(1), 2, Ticker.systemTicker());
        assertThat(service.consume("one").allowed()).isTrue();
        assertThat(service.consume("two").allowed()).isTrue();
        assertThat(service.consume("one").allowed()).isFalse();
        service.consume("three");
        assertThat(service.estimatedClientCount()).isLessThanOrEqualTo(2);
    }

    @Test
    void concurrentRequestsCannotExceedLimit() throws Exception {
        var service = new RateLimitService(10, Duration.ofMinutes(1), 100, Ticker.systemTicker());
        var start = new CountDownLatch(1);
        var allowed = new AtomicLong();
        try (var executor = Executors.newFixedThreadPool(12)) {
            for (int i = 0; i < 100; i++) {
                executor.submit(() -> {
                    start.await();
                    if (service.consume("shared").allowed()) allowed.incrementAndGet();
                    return null;
                });
            }
            start.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(allowed).hasValue(10);
    }

    private static final class MutableTicker implements Ticker {
        private final AtomicLong nanos = new AtomicLong();
        public long read() { return nanos.get(); }
        void advance(Duration duration) { nanos.addAndGet(duration.toNanos()); }
    }
}
