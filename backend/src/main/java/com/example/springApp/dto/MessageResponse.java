package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Mensagem privada dentro de um grupo.")
public record MessageResponse(
        @Schema(example = "50")
        Long id,

        @Schema(example = "1")
        Long grupoId,

        @Schema(description = "Remetente real. Pode ser nulo para preservar anonimato.")
        UserResponse remetente,

        UserResponse destinatario,

        @Schema(description = "Nome exibido para o leitor", example = "amigo secreto")
        String nomeRemetenteExibicao,

        @Schema(example = "Voce prefere camiseta preta ou azul?")
        String conteudo,

        @Schema(example = "2026-05-28T01:00:00")
        LocalDateTime dataEnvio,

        @Schema(example = "false")
        boolean lida,

        @Schema(description = "Indica se a mensagem deve ocultar o remetente para o destinatario", example = "true")
        boolean anonima
) {
}
