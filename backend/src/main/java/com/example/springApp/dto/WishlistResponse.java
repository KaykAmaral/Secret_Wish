package com.example.springApp.dto;

import java.util.List;

public record WishlistResponse(
        Long id,
        UserResponse usuario,
        String sugestaoIa,
        List<WishlistItemResponse> itens
) {
}
