package com.example.springApp.dto;

import jakarta.validation.constraints.NotBlank;

public record WishlistItemRequest(
        @NotBlank String nomeProduto,
        @NotBlank String link
) {
}
