package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
    @NotBlank(message = "Nome e obrigatorio")
    @Schema(example = "David Vieira")
    String nome,

    @Schema(example = "https://example.com/avatar.jpg")
    String imagemUrl
) {}
