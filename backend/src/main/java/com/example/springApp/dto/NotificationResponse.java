package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Notificacao exibida no dashboard.")
public record NotificationResponse(
        @Schema(example = "1")
        Long id,

        UserResponse usuario,

        @Schema(example = "Sorteio realizado")
        String titulo,

        @Schema(example = "Voce tirou Maria no amigo secreto.")
        String mensagem,

        @Schema(example = "2026-05-28T01:00:00")
        LocalDateTime dataCriacao,

        @Schema(example = "false")
        boolean lida
) {
}
