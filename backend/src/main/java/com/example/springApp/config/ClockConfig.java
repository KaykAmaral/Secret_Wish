package com.example.springApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    /**
     * Injeta Clock para permitir testes deterministas de regras baseadas em data.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
