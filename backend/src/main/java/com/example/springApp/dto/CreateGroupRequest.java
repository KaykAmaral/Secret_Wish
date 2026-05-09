package com.example.springApp.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record CreateGroupRequest(
        @NotBlank String nome,
        LocalDateTime dataEvento
) {
}
