package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuario retornado pela API.")
public record UserResponse(
        @Schema(example = "1")
        Long id,

        @Schema(example = "Kayky")
        String nome,

        @Schema(example = "https://example.com/avatar.jpg")
        String imagemUrl,

        @Schema(example = "kayky@example.com")
        String email
) {
}
