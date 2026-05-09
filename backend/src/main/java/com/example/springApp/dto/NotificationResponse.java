package com.example.springApp.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        UserResponse usuario,
        String titulo,
        String mensagem,
        LocalDateTime dataCriacao,
        boolean lida
) {
}
