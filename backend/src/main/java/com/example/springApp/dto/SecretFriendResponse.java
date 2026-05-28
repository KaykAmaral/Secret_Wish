package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado individual do sorteio para o usuario autenticado.")
public record SecretFriendResponse(
        @Schema(example = "1")
        Long grupoId,

        @Schema(description = "Pessoa que o usuario autenticado tirou")
        UserResponse amigoSecreto,

        @Schema(description = "Wishlist visivel da pessoa sorteada")
        WishlistResponse wishlist
) {
}
