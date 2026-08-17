package com.urlsnap.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitServiceTest {

    private StringRedisTemplate redis;
    private RateLimitService service;

    @BeforeEach
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        service = new RateLimitService(redis);
        ReflectionTestUtils.setField(service, "maxRequests", 2L);
        ReflectionTestUtils.setField(service, "windowSeconds", 60L);
        ReflectionTestUtils.setField(service, "failOpen", true);
    }

    @Test
    void permitsCountsWithinLimitAndRejectsAboveIt() {
        when(redis.execute(any(), any(java.util.List.class), any())).thenReturn(1L, 2L, 3L);
        assertThat(service.isAllowed("127.0.0.1")).isTrue();
        assertThat(service.isAllowed("127.0.0.1")).isTrue();
        assertThat(service.isAllowed("127.0.0.1")).isFalse();
    }

    @Test
    void followsConfiguredFailOpenPolicyWhenRedisFails() {
        when(redis.execute(any(), any(java.util.List.class), any()))
                .thenThrow(new DataAccessResourceFailureException("unavailable"));
        assertThat(service.isAllowed("127.0.0.1")).isTrue();
        ReflectionTestUtils.setField(service, "failOpen", false);
        assertThat(service.isAllowed("127.0.0.1")).isFalse();
    }
}
