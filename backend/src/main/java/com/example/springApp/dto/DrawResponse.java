package com.example.springApp.dto;

public record DrawResponse(
        Long id,
        Long grupoId,
        UserResponse remetente,
        UserResponse destinatario
) {
}
