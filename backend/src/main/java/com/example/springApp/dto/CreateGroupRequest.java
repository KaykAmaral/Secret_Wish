package com.example.springApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateGroupRequest(
        @NotBlank String nome,
        @NotNull Long donoId,
        LocalDateTime dataEvento
) {
}
