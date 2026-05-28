package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Grupo de amigo secreto retornado pela API.")
public record GroupResponse(
        @Schema(example = "1")
        Long id,

        @Schema(example = "Amigo secreto da familia")
        String nome,

        @Schema(example = "Troca de presentes de natal.")
        String descricao,

        @Schema(description = "Codigo publico para entrar no grupo", example = "AB12-CD34")
        String codigoUnico,

        UserResponse dono,

        @Schema(description = "Participantes do grupo")
        Set<UserResponse> membros,

        @Schema(example = "2026-05-28T01:00:00")
        LocalDateTime dataCriacao,

        @Schema(description = "Preenchida depois que o sorteio e realizado", example = "2026-05-29T20:00:00")
        LocalDateTime dataSorteio,

        @Schema(description = "Data planejada para a troca de presentes", example = "2026-12-24T20:00:00")
        LocalDateTime dataEvento
) {
}
