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

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;
    private final AuthHelper authHelper;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        String originalUrl = urlService.redirect(shortCode, request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, originalUrl)
                .build();
    }

    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponse> createUrl(@Valid @RequestBody CreateUrlRequest request) {
        UUID userId = authHelper.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(urlService.createUrl(request, userId));
    }

    @GetMapping("/api/urls")
    public ResponseEntity<List<UrlResponse>> getUserUrls() {
        UUID userId = authHelper.getCurrentUserId();
        return ResponseEntity.ok(urlService.getUserUrls(userId));
    }

    @DeleteMapping("/api/urls/{id}")
    public ResponseEntity<Void> deactivateUrl(@PathVariable UUID id) {
        UUID userId = authHelper.getCurrentUserId();
        urlService.deactivateUrl(id, userId);
        return ResponseEntity.noContent().build();
    }
}
