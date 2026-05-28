package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Item de wishlist retornado pela API.")
public record WishlistItemResponse(
        @Schema(example = "100")
        Long id,

        @Schema(example = "Fone de ouvido bluetooth")
        String nomeProduto,

        @Schema(example = "https://example.com/fone")
        String link
) {
}
