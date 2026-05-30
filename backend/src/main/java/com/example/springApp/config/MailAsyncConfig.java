package com.example.springApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class MailAsyncConfig {

    /**
     * Mantem chamadas SMTP fora da thread da requisicao que realizou o sorteio.
     */
    @Bean(name = "mailTaskExecutor")
    public Executor mailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Pool pequeno porque envio de email e I/O externo; fila evita criar threads demais em sorteios maiores.
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        // Prefixo facilita identificar chamadas SMTP nos logs da aplicacao.
        executor.setThreadNamePrefix("mail-");
        executor.initialize();
        return executor;
    }
}
