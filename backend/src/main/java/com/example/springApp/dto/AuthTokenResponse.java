package com.example.springApp.dto;

public record AuthTokenResponse(
        String token,
        UserResponse user
) {
}
