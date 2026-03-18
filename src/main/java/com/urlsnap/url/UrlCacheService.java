package com.urlsnap.url;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UrlCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    public String getUrl(String shortCode) {
        return stringRedisTemplate.opsForValue().get("url:" + shortCode);
    }

    public void saveUrl(String shortCode, String originalUrl) {
        stringRedisTemplate.opsForValue().set("url:" + shortCode, originalUrl, 1, TimeUnit.HOURS);
    }

    public void invalidateUrl(String shortCode) {
        stringRedisTemplate.delete("url:" + shortCode);
    }

    public void incrementClickCount(String shortCode) {
        stringRedisTemplate.opsForValue().increment("clicks:" + shortCode);
    }
}
