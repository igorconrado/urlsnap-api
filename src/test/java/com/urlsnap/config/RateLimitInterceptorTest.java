package com.urlsnap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitInterceptorTest {
    @Test
    void rejectedRequestReturns429AndRateLimitHeaders() throws Exception {
        RateLimitService service = mock(RateLimitService.class);
        when(service.consume("127.0.0.1"))
                .thenReturn(new RateLimitService.RateLimitResult(false, 0, 42));
        var interceptor = new RateLimitInterceptor(service, new ObjectMapper());
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        var response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(response.getHeader("X-RateLimit-Reset")).isEqualTo("42");
        assertThat(response.getContentAsString()).contains("Rate limit exceeded");
    }
}
