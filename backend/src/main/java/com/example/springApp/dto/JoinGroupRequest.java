package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Codigo para entrar em um grupo.")
public record JoinGroupRequest(
        @Schema(description = "Codigo unico gerado pelo backend", example = "AB12-CD34")
        @NotBlank @Pattern(regexp = "^[A-Z0-9]{4}-[A-Z0-9]{4}$") String codigoUnico
) {
}
