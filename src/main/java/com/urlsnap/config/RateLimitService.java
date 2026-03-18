package com.urlsnap.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${RATE_LIMIT_MAX:10}")
    private long maxRequests;

    @Value("${RATE_LIMIT_WINDOW_SECONDS:60}")
    private long windowSeconds;

    public boolean isAllowed(String ipAddress) {
        String key = "rate_limit:" + ipAddress;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        return count != null && count <= maxRequests;
    }

    public long getRemainingRequests(String ipAddress) {
        String key = "rate_limit:" + ipAddress;
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return maxRequests;
        }
        long used = Long.parseLong(value);
        return Math.max(0, maxRequests - used);
    }
}
