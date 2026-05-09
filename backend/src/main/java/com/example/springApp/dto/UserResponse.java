package com.example.springApp.dto;

public record UserResponse(
        Long id,
        String nome,
        String email,
        String oauthId
) {
}
