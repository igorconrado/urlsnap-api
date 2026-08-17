package com.urlsnap.url;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UrlCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    public String getUrl(String shortCode) {
        try {
            return stringRedisTemplate.opsForValue().get("url:" + shortCode);
        } catch (DataAccessException exception) {
            return null;
        }
    }

    public void saveUrl(String shortCode, String originalUrl) {
        try {
            stringRedisTemplate.opsForValue().set("url:" + shortCode, originalUrl, 1, TimeUnit.HOURS);
        } catch (DataAccessException ignored) {
            // PostgreSQL remains the source of truth when Redis is unavailable.
        }
    }

    public void invalidateUrl(String shortCode) {
        try {
            stringRedisTemplate.delete("url:" + shortCode);
        } catch (DataAccessException ignored) {
            // Deactivation is persisted in PostgreSQL and checked before every redirect.
        }
    }

    public void incrementClickCount(String shortCode) {
        try {
            stringRedisTemplate.opsForValue().increment("clicks:" + shortCode);
        } catch (DataAccessException ignored) {
            // Redis counters are supplemental; persisted click events remain authoritative.
        }
    }
}
