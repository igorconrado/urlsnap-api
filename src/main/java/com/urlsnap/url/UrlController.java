package com.urlsnap.url;

import com.urlsnap.config.AuthHelper;
import com.urlsnap.url.dto.CreateUrlRequest;
import com.urlsnap.url.dto.UrlResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;
    private final AuthHelper authHelper;

    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to the active destination URL")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        String originalUrl = urlService.redirect(shortCode, request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, originalUrl)
                .build();
    }

    @PostMapping("/api/urls")
    @Operation(summary = "Create a short URL", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UrlResponse> createUrl(@Valid @RequestBody CreateUrlRequest request) {
        UUID userId = authHelper.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(urlService.createUrl(request, userId));
    }

    @GetMapping("/api/urls")
    @Operation(summary = "List URLs owned by the current user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<UrlResponse>> getUserUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }
        UUID userId = authHelper.getCurrentUserId();
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(urlService.getUserUrls(userId, pageable));
    }

    @DeleteMapping("/api/urls/{id}")
    @Operation(summary = "Deactivate an owned URL", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deactivateUrl(@PathVariable UUID id) {
        UUID userId = authHelper.getCurrentUserId();
        urlService.deactivateUrl(id, userId);
        return ResponseEntity.noContent().build();
    }
}
