package com.urlsnap.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlsnap.auth.AuthService;
import com.urlsnap.auth.JwtService;
import com.urlsnap.auth.User;
import com.urlsnap.auth.UserRepository;
import com.urlsnap.auth.dto.RegisterRequest;
import com.urlsnap.config.RateLimitService;
import com.urlsnap.url.UrlCacheService;
import com.urlsnap.url.dto.CreateUrlRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private UrlCacheService urlCacheService;

    @MockitoBean
    private RateLimitService rateLimitService;

    private String token;

    @BeforeEach
    void setUp() {
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);

        if (userRepository.findByEmail("analytics@urlsnap.com").isEmpty()) {
            var response = authService.register(new RegisterRequest("Analyst", "analytics@urlsnap.com", "123456"));
            token = response.getToken();
        } else {
            User user = userRepository.findByEmail("analytics@urlsnap.com").get();
            token = jwtService.generateToken(user);
        }
    }

    @Test
    void getStats_shouldReturn200_withClickData() throws Exception {
        var request = new CreateUrlRequest("https://stats-test.com", "stats1", null);
        mockMvc.perform(post("/api/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/analytics/stats1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("stats1"))
                .andExpect(jsonPath("$.originalUrl").value("https://stats-test.com"))
                .andExpect(jsonPath("$.totalClicks").value(0))
                .andExpect(jsonPath("$.topReferers").isArray())
                .andExpect(jsonPath("$.clicksByDay").isArray());
    }

    @Test
    void getStats_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/analytics/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("URL not found"));
    }
}
