package com.example.springApp.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ProductionSafetyValidator {

    private static final int MIN_JWT_SECRET_LENGTH = 32;

    /**
     * Executa validacoes de configuracao no boot para impedir subida insegura em producao.
     */
    @Bean
    ApplicationRunner validateProductionSafety(Environment environment) {
        return args -> {
            validateCookieSameSite(environment);
            if (!isProd(environment)) {
                return;
            }

            validateProdRequiredValue(environment, "app.frontend.origin");
            validateProdRequiredValue(environment, "app.jwt.secret");
            validateProdRequiredValue(environment, "spring.security.oauth2.client.registration.google.client-id");
            validateProdRequiredValue(environment, "spring.security.oauth2.client.registration.google.client-secret");
            validateProdRequiredValue(environment, "spring.datasource.url");
            validateProdRequiredValue(environment, "spring.datasource.username");
            validateProdRequiredValue(environment, "spring.datasource.password");

            // Em prod, a aplicacao deve falhar rapido quando alguma superficie insegura estiver ativa.
            requireHttpsProperty(environment, "app.frontend.origin");
            requireFalse(environment, "app.dev-auth.enabled");
            requireFalse(environment, "springdoc.swagger-ui.enabled");
            requireFalse(environment, "springdoc.api-docs.enabled");
            requireTrue(environment, "app.auth.cookie-secure");
            requireHttpsOrigins(environment);
            validateJwtSecretStrength(environment);
            validateOptionalIntegrations(environment);
        };
    }

    /**
     * Verifica se o perfil ativo exige regras rigidas de producao.
     */
    private boolean isProd(Environment environment) {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    /**
     * Rejeita propriedades obrigatorias ausentes, placeholders ou segredos de exemplo.
     */
    private void validateProdRequiredValue(Environment environment, String property) {
        String value = environment.getProperty(property);
        if (value == null || value.isBlank() || value.contains("placeholder") || value.contains("change-before-production")) {
            throw new IllegalStateException("Configuracao de producao ausente ou insegura: " + property);
        }
    }

    /**
     * Exige segredo JWT minimo para reduzir risco de assinatura fraca em producao.
     */
    private void validateJwtSecretStrength(Environment environment) {
        String secret = environment.getRequiredProperty("app.jwt.secret");
        if (secret.length() < MIN_JWT_SECRET_LENGTH) {
            throw new IllegalStateException("Configuracao de producao insegura: app.jwt.secret deve ter pelo menos 32 caracteres");
        }
    }

    /**
     * Valida a combinacao SameSite/Secure usada no cookie de autenticacao.
     */
    private void validateCookieSameSite(Environment environment) {
        String sameSite = environment.getProperty("app.auth.cookie-same-site", "Lax");
        List<String> allowedValues = List.of("Lax", "Strict", "None");
        if (!allowedValues.contains(sameSite)) {
            throw new IllegalStateException("app.auth.cookie-same-site deve ser Lax, Strict ou None");
        }

        boolean secureCookie = environment.getProperty("app.auth.cookie-secure", Boolean.class, false);
        if ("None".equals(sameSite) && !secureCookie) {
            throw new IllegalStateException("app.auth.cookie-secure deve ser true quando app.auth.cookie-same-site=None");
        }
    }

    /**
     * Garante que flags perigosas estejam desligadas no perfil de producao.
     */
    private void requireFalse(Environment environment, String property) {
        if (environment.getProperty(property, Boolean.class, false)) {
            throw new IllegalStateException("Configuracao de producao insegura: " + property + " deve ser false");
        }
    }

    /**
     * Garante que flags obrigatorias de seguranca estejam ligadas no perfil de producao.
     */
    private void requireTrue(Environment environment, String property) {
        if (!environment.getProperty(property, Boolean.class, false)) {
            throw new IllegalStateException("Configuracao de producao insegura: " + property + " deve ser true");
        }
    }

    /**
     * Impede wildcard e origens sem HTTPS para cookies e CORS em producao.
     */
    private void requireHttpsOrigins(Environment environment) {
        String origins = environment.getRequiredProperty("app.frontend.origins");
        for (String origin : origins.split(",")) {
            String normalizedOrigin = origin.trim();
            if (normalizedOrigin.equals("*")) {
                throw new IllegalStateException("Configuracao de producao insegura: app.frontend.origins nao pode usar *");
            }
            if (!normalizedOrigin.startsWith("https://")) {
                throw new IllegalStateException("Configuracao de producao insegura: app.frontend.origins deve usar HTTPS");
            }
        }
    }

    /**
     * Exige HTTPS em propriedades que representam URLs publicas.
     */
    private void requireHttpsProperty(Environment environment, String property) {
        String value = environment.getRequiredProperty(property).trim();
        if (!value.startsWith("https://")) {
            throw new IllegalStateException("Configuracao de producao insegura: " + property + " deve usar HTTPS");
        }
    }

    /**
     * Valida credenciais somente quando recursos opcionais forem habilitados.
     */
    private void validateOptionalIntegrations(Environment environment) {
        if (environment.getProperty("app.mail.enabled", Boolean.class, false)) {
            validateProdRequiredValue(environment, "spring.mail.username");
            validateProdRequiredValue(environment, "spring.mail.password");
        }

        if (environment.getProperty("app.ai.enabled", Boolean.class, false)) {
            validateProdRequiredValue(environment, "spring.ai.openai.api-key");
        }
    }
}
