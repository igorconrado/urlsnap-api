package com.urlsnap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    Map<String, String> index(@Value("${app.base-url}") String baseUrl) {
        return Map.of(
                "name", "URLSnap API",
                "status", "available",
                "documentation", baseUrl + "/swagger-ui.html",
                "health", baseUrl + "/actuator/health"
        );
    }
}
