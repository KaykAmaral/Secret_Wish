package com.example.springApp.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void rateLimitUsesTooManyRequestsContract() {
        var response = handler.handleRateLimitException(new RateLimitException("Limite de 3 sugestoes com IA por hora atingido"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(429);
        assertThat(response.getBody().error()).isEqualTo("Limite de uso atingido");
        assertThat(response.getBody().message()).contains("Limite de 3 sugestoes");
    }
}
