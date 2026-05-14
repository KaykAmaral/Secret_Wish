package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status da sessao atual.")
public record AuthStatusResponse(
        @Schema(description = "Indica se a requisicao possui autenticacao valida.", example = "true")
        boolean authenticated,

        @Schema(description = "Usuario autenticado. Nulo quando authenticated=false.")
        UserResponse user
) {
}
