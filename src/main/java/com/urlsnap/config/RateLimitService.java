package com.urlsnap.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.dao.DataAccessException;

import java.util.concurrent.TimeUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> INCREMENT_WITH_TTL = new DefaultRedisScript<>(
            "local count = redis.call('INCR', KEYS[1]); " +
            "if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; return count;", Long.class);

    @Value("${app.rate-limit.max-requests}")
    private long maxRequests;

    @Value("${app.rate-limit.window-seconds}")
    private long windowSeconds;

    @Value("${app.rate-limit.fail-open}")
    private boolean failOpen;

    public boolean isAllowed(String ipAddress) {
        String key = "rate_limit:" + ipAddress;
        try {
            Long count = stringRedisTemplate.execute(INCREMENT_WITH_TTL, List.of(key), String.valueOf(windowSeconds));
            return count != null && count <= maxRequests;
        } catch (DataAccessException exception) {
            return failOpen;
        }
    }

    public long getRemainingRequests(String ipAddress) {
        try {
            String value = stringRedisTemplate.opsForValue().get("rate_limit:" + ipAddress);
            if (value == null) {
                return maxRequests;
            }
            long used = Long.parseLong(value);
            return Math.max(0, maxRequests - used);
        } catch (DataAccessException | NumberFormatException exception) {
            return maxRequests;
        }
    }

    public long getResetSeconds(String ipAddress) {
        try {
            Long ttl = stringRedisTemplate.getExpire("rate_limit:" + ipAddress, TimeUnit.SECONDS);
            return ttl == null || ttl < 0 ? windowSeconds : ttl;
        } catch (DataAccessException exception) {
            return windowSeconds;
        }
    }
}
