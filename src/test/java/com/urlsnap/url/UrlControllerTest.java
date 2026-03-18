package com.urlsnap.url;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlsnap.auth.AuthService;
import com.urlsnap.auth.JwtService;
import com.urlsnap.auth.User;
import com.urlsnap.auth.UserRepository;
import com.urlsnap.auth.dto.RegisterRequest;
import com.urlsnap.config.RateLimitService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlControllerTest {

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

        if (userRepository.findByEmail("urltest@urlsnap.com").isEmpty()) {
            var response = authService.register(new RegisterRequest("Tester", "urltest@urlsnap.com", "123456"));
            token = response.getToken();
        } else {
            User user = userRepository.findByEmail("urltest@urlsnap.com").get();
            token = jwtService.generateToken(user);
        }
    }

    @Test
    void createUrl_shouldReturn201_whenAuthenticated() throws Exception {
        var request = new CreateUrlRequest("https://github.com", null, null);

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").isNotEmpty())
                .andExpect(jsonPath("$.shortUrl").isNotEmpty())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.originalUrl").value("https://github.com"));
    }

    @Test
    void createUrl_shouldReturn201_whenAnonymous() throws Exception {
        var request = new CreateUrlRequest("https://example.com", null, null);

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").isNotEmpty())
                .andExpect(jsonPath("$.userId").isEmpty());
    }

    @Test
    void redirect_shouldReturn302_toOriginalUrl() throws Exception {
        var request = new CreateUrlRequest("https://google.com", "redir1", null);
        mockMvc.perform(post("/api/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/redir1"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://google.com"));
    }

    @Test
    void getUserUrls_shouldReturn200_withList() throws Exception {
        var request = new CreateUrlRequest("https://spring.io", null, null);
        mockMvc.perform(post("/api/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/urls")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].shortCode").isNotEmpty());
    }
}
