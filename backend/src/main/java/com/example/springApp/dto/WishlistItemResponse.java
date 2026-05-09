package com.example.springApp.dto;

public record WishlistItemResponse(
        Long id,
        String nomeProduto,
        String link
) {
}
