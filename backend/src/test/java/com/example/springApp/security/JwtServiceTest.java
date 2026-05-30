package com.example.springApp.security;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService("test-secret-with-enough-entropy", 60);

    @Test
    void generatedTokenCanBeValidated() {
        String token = jwtService.generateToken(42L, "user@example.com", "User");

        assertThat(jwtService.validateAndGetUserId(token)).isEqualTo(42L);
    }

    @Test
    void validationRejectsTamperedSignature() {
        String token = jwtService.generateToken(42L, "user@example.com", "User");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.validateAndGetUserId(tampered)).isNull();
    }

    @Test
    void validationRejectsExpiredToken() throws Exception {
        String expiredToken = tokenWithExpiration(Instant.now().minusSeconds(1).getEpochSecond());

        assertThat(jwtService.validateAndGetUserId(expiredToken)).isNull();
    }

    @Test
    void validationRejectsMalformedAndOversizedTokens() {
        String oversized = "a".repeat(4097);

        assertThat(jwtService.validateAndGetUserId("not-a-jwt")).isNull();
        assertThat(jwtService.validateAndGetUserId(oversized)).isNull();
    }

    @Test
    void validationRejectsUnsupportedHeader() {
        String token = jwtService.generateToken(42L, "user@example.com", "User");
        String[] parts = token.split("\\.");
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes());

        assertThat(jwtService.validateAndGetUserId(header + "." + parts[1] + "." + parts[2])).isNull();
    }

    private String tokenWithExpiration(long expiresAt) throws Exception {
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", "42");
        payload.put("email", "user@example.com");
        payload.put("name", "User");
        payload.put("iat", Instant.now().minusSeconds(120).getEpochSecond());
        payload.put("exp", expiresAt);

        String unsigned = invokeEncodeJson(header) + "." + invokeEncodeJson(payload);
        return unsigned + "." + invokeSign(unsigned);
    }

    private String invokeEncodeJson(Map<String, Object> value) throws Exception {
        Method method = JwtService.class.getDeclaredMethod("encodeJson", Map.class);
        method.setAccessible(true);
        return (String) method.invoke(jwtService, value);
    }

    private String invokeSign(String value) throws Exception {
        Method method = JwtService.class.getDeclaredMethod("sign", String.class);
        method.setAccessible(true);
        return (String) method.invoke(jwtService, value);
    }
}
