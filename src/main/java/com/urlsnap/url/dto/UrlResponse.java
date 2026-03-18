package com.urlsnap.url.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlResponse {

    private UUID id;
    private String originalUrl;
    private String shortCode;
    private String shortUrl;
    private UUID userId;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private long clickCount;
    private LocalDateTime createdAt;
}
