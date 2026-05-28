package com.example.springApp.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final int MAX_TOKEN_LENGTH = 4096;

    private final String secret;
    private final long expirationSeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.secret = secret;
        this.expirationSeconds = expirationMinutes * 60;
    }

    /**
     * Gera um JWT HMAC contendo apenas identificadores necessarios para reconstruir a sessao.
     */
    public String generateToken(Long userId, String email, String name) {
        Instant now = Instant.now();

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userId.toString());
        payload.put("email", email);
        payload.put("name", name);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plusSeconds(expirationSeconds).getEpochSecond());

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    /**
     * Valida assinatura e expiracao do JWT, retornando null para tokens invalidos sem vazar motivo.
     */
    public Long validateAndGetUserId(String token) {
        try {
            if (token.length() > MAX_TOKEN_LENGTH) {
                return null;
            }

            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            Map<String, Object> header = OBJECT_MAPPER.readValue(
                    BASE64_URL_DECODER.decode(parts[0]),
                    new TypeReference<>() {
                    }
            );
            if (!"HS256".equals(header.get("alg")) || !"JWT".equals(header.get("typ"))) {
                return null;
            }

            String unsignedToken = parts[0] + "." + parts[1];
            // Comparacao constante evita vazamento por timing na assinatura.
            if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
                return null;
            }

            Map<String, Object> payload = OBJECT_MAPPER.readValue(
                    BASE64_URL_DECODER.decode(parts[1]),
                    new TypeReference<>() {
                    }
            );

            Number expiresAt = (Number) payload.get("exp");
            if (expiresAt == null || expiresAt.longValue() <= Instant.now().getEpochSecond()) {
                return null;
            }

            return Long.valueOf((String) payload.get("sub"));
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Serializa e codifica um trecho do JWT em Base64URL sem padding.
     */
    private String encodeJson(Map<String, Object> value) {
        try {
            return BASE64_URL_ENCODER.encodeToString(OBJECT_MAPPER.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao gerar JWT", ex);
        }
    }

    /**
     * Assina header e payload usando HMAC-SHA256 com o segredo da aplicacao.
     */
    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao assinar JWT", ex);
        }
    }

    /**
     * Compara assinaturas em tempo constante para reduzir risco de timing attacks.
     */
    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }
}
