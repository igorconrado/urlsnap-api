package com.urlsnap.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlsnap.auth.dto.LoginRequest;
import com.urlsnap.auth.dto.RegisterRequest;
import com.urlsnap.config.RateLimitService;
import com.urlsnap.url.UrlCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UrlCacheService urlCacheService;

    @MockitoBean
    private RateLimitService rateLimitService;

    @Test
    void register_shouldReturn201_withToken() throws Exception {
        var request = new RegisterRequest("Igor", "test@urlsnap.com", "123456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Igor"))
                .andExpect(jsonPath("$.email").value("test@urlsnap.com"));
    }

    @Test
    void register_shouldReturn400_whenDuplicateEmail() throws Exception {
        var request = new RegisterRequest("Igor", "duplicate@urlsnap.com", "123456");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void login_shouldReturn200_withToken() throws Exception {
        var registerRequest = new RegisterRequest("Igor", "login@urlsnap.com", "123456");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        var loginRequest = new LoginRequest("login@urlsnap.com", "123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("login@urlsnap.com"));
    }

    @Test
    void login_shouldReturn401_whenWrongPassword() throws Exception {
        var registerRequest = new RegisterRequest("Igor", "wrong@urlsnap.com", "123456");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        var loginRequest = new LoginRequest("wrong@urlsnap.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
