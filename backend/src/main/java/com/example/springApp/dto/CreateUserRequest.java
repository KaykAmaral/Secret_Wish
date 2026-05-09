package com.example.springApp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        String oauthId
) {
}
