package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Schema(description = "Dados para criacao de grupo.")
public record CreateGroupRequest(
        @Schema(description = "Nome do grupo", example = "Amigo secreto da familia")
        @NotBlank String nome,

        @Schema(description = "Descricao opcional do grupo", example = "Amigo secreto da familia para o natal.")
        String descricao,

        @Schema(description = "Data opcional do evento", example = "2026-12-24T20:00:00")
        LocalDateTime dataEvento
) {
}
