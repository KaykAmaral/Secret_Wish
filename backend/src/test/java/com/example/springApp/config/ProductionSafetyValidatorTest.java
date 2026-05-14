package com.example.springApp.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionSafetyValidatorTest {

    private final ProductionSafetyValidator validator = new ProductionSafetyValidator();

    @Test
    void acceptsSecureProductionConfiguration() {
        MockEnvironment environment = secureProdEnvironment();

        assertThatCode(() -> runValidator(environment)).doesNotThrowAnyException();
    }

    @Test
    void rejectsHttpFrontendOriginInProduction() {
        MockEnvironment environment = secureProdEnvironment()
                .withProperty("app.frontend.origin", "http://app.example.com");

        assertThatThrownBy(() -> runValidator(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.frontend.origin deve usar HTTPS");
    }

    @Test
    void rejectsDevAuthEnabledInProduction() {
        MockEnvironment environment = secureProdEnvironment()
                .withProperty("app.dev-auth.enabled", "true");

        assertThatThrownBy(() -> runValidator(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.dev-auth.enabled deve ser false");
    }

    @Test
    void rejectsSwaggerEnabledInProduction() {
        MockEnvironment environment = secureProdEnvironment()
                .withProperty("springdoc.swagger-ui.enabled", "true");

        assertThatThrownBy(() -> runValidator(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("springdoc.swagger-ui.enabled deve ser false");
    }

    @Test
    void rejectsEnabledMailWithoutSmtpCredentialsInProduction() {
        MockEnvironment environment = secureProdEnvironment()
                .withProperty("app.mail.enabled", "true")
                .withProperty("spring.mail.username", "smtp@example.com")
                .withProperty("spring.mail.password", "");

        assertThatThrownBy(() -> runValidator(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spring.mail.password");
    }

    @Test
    void rejectsEnabledAiWithoutApiKeyInProduction() {
        MockEnvironment environment = secureProdEnvironment()
                .withProperty("app.ai.enabled", "true")
                .withProperty("spring.ai.openai.api-key", "");

        assertThatThrownBy(() -> runValidator(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spring.ai.openai.api-key");
    }

    private void runValidator(MockEnvironment environment) throws Exception {
        ApplicationRunner runner = validator.validateProductionSafety(environment);
        runner.run(null);
    }

    private MockEnvironment secureProdEnvironment() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        return environment
                .withProperty("app.frontend.origin", "https://app.example.com")
                .withProperty("app.frontend.origins", "https://app.example.com,https://admin.example.com")
                .withProperty("app.jwt.secret", "secure-production-secret-with-more-than-32-chars")
                .withProperty("spring.security.oauth2.client.registration.google.client-id", "google-client-id")
                .withProperty("spring.security.oauth2.client.registration.google.client-secret", "google-client-secret")
                .withProperty("spring.datasource.url", "jdbc:mysql://db.example.com:3306/secret_wish")
                .withProperty("spring.datasource.username", "secret_wish")
                .withProperty("spring.datasource.password", "strong-password")
                .withProperty("app.dev-auth.enabled", "false")
                .withProperty("springdoc.swagger-ui.enabled", "false")
                .withProperty("springdoc.api-docs.enabled", "false")
                .withProperty("app.auth.cookie-secure", "true")
                .withProperty("app.auth.cookie-same-site", "Lax")
                .withProperty("app.mail.enabled", "false")
                .withProperty("spring.mail.username", "")
                .withProperty("spring.mail.password", "")
                .withProperty("app.ai.enabled", "false")
                .withProperty("spring.ai.openai.api-key", "");
    }
}
