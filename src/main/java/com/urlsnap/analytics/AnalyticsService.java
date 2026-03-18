package com.urlsnap.analytics;

import com.urlsnap.analytics.dto.DailyClickStat;
import com.urlsnap.analytics.dto.RefererStat;
import com.urlsnap.analytics.dto.UrlStatsResponse;
import com.urlsnap.url.Url;
import com.urlsnap.url.UrlRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UrlRepository urlRepository;
    private final ClickRepository clickRepository;

    @Value("${BASE_URL:http://localhost:8080}")
    private String baseUrl;

    public UrlStatsResponse getUrlStats(String shortCode, UUID userId) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new EntityNotFoundException("URL not found"));

        long totalClicks = clickRepository.countByUrlId(url.getId());

        LocalDateTime now = LocalDateTime.now();
        long clicksLast24h = clickRepository.countByUrlIdAndClickedAtAfter(url.getId(), now.minusHours(24));
        long clicksLast7d = clickRepository.countByUrlIdAndClickedAtAfter(url.getId(), now.minusDays(7));
        long clicksLast30d = clickRepository.countByUrlIdAndClickedAtAfter(url.getId(), now.minusDays(30));

        List<RefererStat> topReferers = clickRepository
                .findTopReferersByUrlId(url.getId(), PageRequest.of(0, 5))
                .stream()
                .map(row -> new RefererStat((String) row[0], (long) row[1]))
                .toList();

        List<DailyClickStat> clicksByDay = clickRepository
                .findDailyClicksByUrlId(url.getId(), now.minusDays(30))
                .stream()
                .map(row -> new DailyClickStat(((java.sql.Date) row[0]).toLocalDate(), (long) row[1]))
                .toList();

        return UrlStatsResponse.builder()
                .shortCode(url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .totalClicks(totalClicks)
                .clicksLast24h(clicksLast24h)
                .clicksLast7d(clicksLast7d)
                .clicksLast30d(clicksLast30d)
                .topReferers(topReferers)
                .clicksByDay(clicksByDay)
                .isActive(url.getIsActive())
                .createdAt(url.getCreatedAt())
                .build();
    }
}
