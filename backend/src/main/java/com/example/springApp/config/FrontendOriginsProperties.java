package com.example.springApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class FrontendOriginsProperties {

    private final List<String> allowedOrigins;

    public FrontendOriginsProperties(
            @Value("${app.frontend.origins:${app.frontend.origin}}") String configuredOrigins
    ) {
        this.allowedOrigins = Arrays.stream(configuredOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .distinct()
                .toList();

        if (allowedOrigins.isEmpty()) {
            throw new IllegalStateException("Pelo menos uma origem de frontend deve ser configurada");
        }
    }

    public List<String> allowedOrigins() {
        return allowedOrigins;
    }

    public String primaryOrigin() {
        return allowedOrigins.getFirst();
    }

    public String[] allowedOriginsArray() {
        return allowedOrigins.toArray(String[]::new);
    }
}
