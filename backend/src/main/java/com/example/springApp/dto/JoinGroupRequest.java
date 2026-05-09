package com.example.springApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record JoinGroupRequest(
        @NotBlank @Pattern(regexp = "^[A-Z0-9]{4}-[A-Z0-9]{4}$") String codigoUnico,
        @NotNull Long userId
) {
}
