package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resumo da execucao do sorteio.")
public record PerformDrawResponse(
        @Schema(example = "1")
        Long groupId,

        @Schema(description = "Quantidade de participantes sorteados", example = "3")
        int participantCount,

        @Schema(description = "Data em que o sorteio foi realizado", example = "2026-05-12T22:00:00")
        LocalDateTime performedAt
) {
}
