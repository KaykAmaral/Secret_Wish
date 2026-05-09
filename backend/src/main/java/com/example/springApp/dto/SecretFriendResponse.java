package com.example.springApp.dto;

public record SecretFriendResponse(
        Long grupoId,
        UserResponse amigoSecreto,
        WishlistResponse wishlist
) {
}
