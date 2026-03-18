package com.urlsnap.analytics;

import com.urlsnap.url.Url;
import com.urlsnap.url.UrlCacheService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClickService {

    private final ClickRepository clickRepository;
    private final UrlCacheService urlCacheService;

    @Async
    public void recordClick(UUID urlId, String shortCode, HttpServletRequest request) {
        var click = Click.builder()
                .url(Url.builder().id(urlId).build())
                .ipAddress(request.getRemoteAddr())
                .userAgent(request.getHeader("User-Agent"))
                .referer(request.getHeader("Referer"))
                .build();

        clickRepository.save(click);
        urlCacheService.incrementClickCount(shortCode);
    }
}
