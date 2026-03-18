package com.urlsnap.url;

import com.urlsnap.analytics.ClickRepository;
import com.urlsnap.analytics.ClickService;
import com.urlsnap.auth.User;
import com.urlsnap.url.dto.CreateUrlRequest;
import com.urlsnap.url.dto.UrlResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final UrlCacheService urlCacheService;
    private final ShortCodeGenerator shortCodeGenerator;
    private final ClickService clickService;
    private final ClickRepository clickRepository;

    @Value("${BASE_URL:http://localhost:8080}")
    private String baseUrl;

    public UrlResponse createUrl(CreateUrlRequest request, UUID userId) {
        String shortCode;

        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            if (urlRepository.existsByShortCode(request.getCustomCode())) {
                throw new IllegalArgumentException("Custom code already taken");
            }
            shortCode = request.getCustomCode();
        } else {
            shortCode = shortCodeGenerator.generate(urlRepository);
        }

        var url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .user(userId != null ? User.builder().id(userId).build() : null)
                .expiresAt(request.getExpiresAt())
                .isActive(true)
                .build();

        urlRepository.save(url);
        urlCacheService.saveUrl(shortCode, request.getOriginalUrl());

        return toResponse(url, 0);
    }

    public String redirect(String shortCode, HttpServletRequest request) {
        String cached = urlCacheService.getUrl(shortCode);

        if (cached != null) {
            var url = urlRepository.findByShortCodeAndIsActiveTrue(shortCode)
                    .orElseThrow(() -> new EntityNotFoundException("URL not found"));
            if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new EntityNotFoundException("URL has expired");
            }
            clickService.recordClick(url.getId(), shortCode, request);
            return cached;
        }

        var url = urlRepository.findByShortCodeAndIsActiveTrue(shortCode)
                .orElseThrow(() -> new EntityNotFoundException("URL not found"));

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new EntityNotFoundException("URL has expired");
        }

        urlCacheService.saveUrl(shortCode, url.getOriginalUrl());
        clickService.recordClick(url.getId(), shortCode, request);

        return url.getOriginalUrl();
    }

    public List<UrlResponse> getUserUrls(UUID userId) {
        return urlRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(url -> toResponse(url, clickRepository.countByUrlId(url.getId())))
                .toList();
    }

    public UrlResponse deactivateUrl(UUID id, UUID userId) {
        var url = urlRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("URL not found"));

        if (url.getUser() == null || !url.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You don't own this URL");
        }

        url.setIsActive(false);
        urlRepository.save(url);
        urlCacheService.invalidateUrl(url.getShortCode());

        return toResponse(url, clickRepository.countByUrlId(url.getId()));
    }

    private UrlResponse toResponse(Url url, long clickCount) {
        return UrlResponse.builder()
                .id(url.getId())
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .userId(url.getUser() != null ? url.getUser().getId() : null)
                .expiresAt(url.getExpiresAt())
                .isActive(url.getIsActive())
                .clickCount(clickCount)
                .createdAt(url.getCreatedAt())
                .build();
    }
}
