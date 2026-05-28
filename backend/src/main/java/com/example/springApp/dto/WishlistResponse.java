package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Wishlist de um usuario.")
public record WishlistResponse(
        @Schema(example = "10")
        Long id,

        UserResponse usuario,

        @Schema(description = "Ultima sugestao gerada por IA, quando houver", example = "Uma boa opcao seria escolher o fone bluetooth.")
        String sugestaoIa,

        @Schema(description = "Itens desejados pelo usuario")
        List<WishlistItemResponse> itens
) {
}
