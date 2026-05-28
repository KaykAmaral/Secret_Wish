package com.example.springApp.security;

import com.example.springApp.exception.RateLimitException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthRateLimitService {

    private final int loginMaxAttempts;
    private final int registerMaxAttempts;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Instant>> attemptsByKey = new ConcurrentHashMap<>();

    public AuthRateLimitService(
            @Value("${app.security.auth-rate-limit.login-max-attempts:5}") int loginMaxAttempts,
            @Value("${app.security.auth-rate-limit.register-max-attempts:3}") int registerMaxAttempts,
            @Value("${app.security.auth-rate-limit.window-minutes:15}") long windowMinutes,
            Clock clock
    ) {
        this.loginMaxAttempts = loginMaxAttempts;
        this.registerMaxAttempts = registerMaxAttempts;
        this.window = Duration.ofMinutes(windowMinutes);
        this.clock = clock;
    }

    /**
     * Limita tentativas de login por IP e email para reduzir abuso de credenciais.
     */
    public void checkLoginAllowed(String email, String clientAddress) {
        checkAllowed("login", email, clientAddress, loginMaxAttempts);
    }

    /**
     * Limita cadastro por IP e email para reduzir automacao e spam de contas.
     */
    public void checkRegisterAllowed(String email, String clientAddress) {
        checkAllowed("register", email, clientAddress, registerMaxAttempts);
    }

    private void checkAllowed(String action, String email, String clientAddress, int maxAttempts) {
        String key = action + ":" + normalize(clientAddress) + ":" + normalize(email);
        Instant now = Instant.now(clock);
        Instant oldestAllowed = now.minus(window);
        Deque<Instant> attempts = attemptsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (attempts) {
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(oldestAllowed)) {
                attempts.removeFirst();
            }

            if (attempts.size() >= maxAttempts) {
                throw new RateLimitException("Muitas tentativas. Aguarde alguns minutos antes de tentar novamente");
            }

            attempts.addLast(now);
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
