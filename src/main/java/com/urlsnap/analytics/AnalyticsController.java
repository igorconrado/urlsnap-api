package com.urlsnap.analytics;

import com.urlsnap.analytics.dto.UrlStatsResponse;
import com.urlsnap.config.AuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthHelper authHelper;

    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlStatsResponse> getUrlStats(@PathVariable String shortCode) {
        var userId = authHelper.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getUrlStats(shortCode, userId));
    }
}
