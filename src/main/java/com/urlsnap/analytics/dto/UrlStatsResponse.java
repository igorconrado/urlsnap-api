package com.urlsnap.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlStatsResponse {

    private String shortCode;
    private String originalUrl;
    private String shortUrl;
    private long totalClicks;
    private long clicksLast24h;
    private long clicksLast7d;
    private long clicksLast30d;
    private List<RefererStat> topReferers;
    private List<DailyClickStat> clicksByDay;
    private boolean isActive;
    private LocalDateTime createdAt;
}
