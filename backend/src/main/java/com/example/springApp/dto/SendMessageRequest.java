package com.example.springApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendMessageRequest(
        @NotNull Long remetenteId,
        @NotNull Long destinatarioId,
        @NotBlank String conteudo,
        boolean anonima
) {
}
