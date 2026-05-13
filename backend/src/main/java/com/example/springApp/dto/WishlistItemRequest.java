package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Item da wishlist.")
public record WishlistItemRequest(
        @Schema(description = "Nome do produto desejado", example = "Fone de ouvido bluetooth")
        @NotBlank String nomeProduto,

        @Schema(description = "Link do produto", example = "https://example.com/produto")
        @NotBlank String link
) {
}
