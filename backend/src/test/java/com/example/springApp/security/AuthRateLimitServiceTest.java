package com.example.springApp.security;

import com.example.springApp.exception.RateLimitException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthRateLimitServiceTest {

    @Test
    void loginAttemptsAreLimitedByEmailAndClientAddress() {
        MutableClock clock = new MutableClock(Instant.parse("2026-05-28T12:00:00Z"));
        AuthRateLimitService service = new AuthRateLimitService(2, 3, 15, clock);

        service.checkLoginAllowed("user@example.com", "127.0.0.1");
        service.checkLoginAllowed("user@example.com", "127.0.0.1");

        assertThatThrownBy(() -> service.checkLoginAllowed("user@example.com", "127.0.0.1"))
                .isInstanceOf(RateLimitException.class)
                .hasMessageContaining("Muitas tentativas");
        assertThatCode(() -> service.checkLoginAllowed("other@example.com", "127.0.0.1"))
                .doesNotThrowAnyException();
    }

    @Test
    void attemptsExpireAfterConfiguredWindow() {
        MutableClock clock = new MutableClock(Instant.parse("2026-05-28T12:00:00Z"));
        AuthRateLimitService service = new AuthRateLimitService(1, 1, 15, clock);

        service.checkRegisterAllowed("user@example.com", "127.0.0.1");
        clock.advance(Duration.ofMinutes(16));

        assertThatCode(() -> service.checkRegisterAllowed("user@example.com", "127.0.0.1"))
                .doesNotThrowAnyException();
    }

    private static class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
