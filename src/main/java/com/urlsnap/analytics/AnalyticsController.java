package com.urlsnap.analytics;

import com.urlsnap.analytics.dto.UrlStatsResponse;
import com.urlsnap.config.AuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthHelper authHelper;

    @GetMapping("/{shortCode}")
    @Operation(summary = "Get analytics for an owned URL", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UrlStatsResponse> getUrlStats(@PathVariable String shortCode) {
        var userId = authHelper.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getUrlStats(shortCode, userId));
    }
}
