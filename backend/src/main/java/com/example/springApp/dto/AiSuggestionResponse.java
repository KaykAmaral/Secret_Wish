package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sugestao gerada por IA para uma wishlist visivel.")
public record AiSuggestionResponse(
        @Schema(example = "10")
        Long wishlistId,

        @Schema(example = "Uma boa opcao seria escolher o fone da wishlist, pois combina com o perfil informado.")
        String sugestaoIa
) {
}
