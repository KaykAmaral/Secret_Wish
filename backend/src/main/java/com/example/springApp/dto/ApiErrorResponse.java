package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Contrato padrao de erro da API.")
public record ApiErrorResponse(
        @Schema(example = "2026-05-13T14:30:00")
        LocalDateTime timestamp,

        @Schema(example = "400")
        int status,

        @Schema(example = "Erro de validacao")
        String error,

        @Schema(example = "Existem campos invalidos na requisicao")
        String message,

        @Schema(description = "Erros por campo. Vazio quando o erro nao for de validacao.")
        Map<String, String> fields
) {
}
