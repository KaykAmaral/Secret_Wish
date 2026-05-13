package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuario retornado pela API.")
public record UserResponse(
        @Schema(example = "1")
        Long id,

        @Schema(example = "Kayky")
        String nome,

        @Schema(example = "kayky@example.com")
        String email,

        @Schema(description = "Identificador OAuth do provedor. Pode ser omitido no frontend.", example = "google-sub-123")
        String oauthId
) {
}
