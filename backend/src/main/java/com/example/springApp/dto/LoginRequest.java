package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para login local.")
public record LoginRequest(
    @Schema(description = "Email cadastrado", example = "kayky@example.com")
    @NotBlank(message = "Email e obrigatorio")
    @Email(message = "Email invalido")
    String email,

    @Schema(description = "Senha cadastrada", example = "secret123")
    @NotBlank(message = "Senha e obrigatoria")
    String password
) {}
