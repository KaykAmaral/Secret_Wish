package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Mensagem privada para outro participante do grupo.")
public record SendMessageRequest(
        @Schema(description = "ID do destinatario", example = "2")
        @NotNull Long destinatarioId,

        @Schema(description = "Conteudo em texto simples", example = "Voce prefere camiseta preta ou azul?")
        @NotBlank String conteudo
) {
}
