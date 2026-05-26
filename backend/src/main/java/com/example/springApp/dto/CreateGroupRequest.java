package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Dados para criacao de grupo.")
public record CreateGroupRequest(
        @Schema(description = "Nome do grupo", example = "Amigo secreto da familia")
        @NotBlank String nome,

        @Schema(description = "Descricao do grupo", example = "Amigo secreto da familia para o natal.")
        @NotBlank String descricao,

        @Schema(description = "Data do evento", example = "2026-12-24T20:00:00")
        @NotNull LocalDateTime dataEvento
) {
}
