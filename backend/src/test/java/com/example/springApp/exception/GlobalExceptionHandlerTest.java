package com.example.springApp.exception;

import com.example.springApp.dto.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void rateLimitUsesTooManyRequestsContract() {
        ResponseEntity<ApiErrorResponse> response = handler.handleRateLimitException(
                new RateLimitException("Limite de 3 sugestoes com IA por hora atingido")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(429);
        assertThat(response.getBody().error()).isEqualTo("Limite de uso atingido");
        assertThat(response.getBody().message()).contains("Limite de 3 sugestoes");
    }
}
