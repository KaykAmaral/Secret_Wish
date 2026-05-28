package com.example.springApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro local por email e senha.")
public record RegisterRequest(
    @Schema(description = "Nome exibido no sistema", example = "Kayky Silva")
    @NotBlank(message = "Nome e obrigatorio")
    String nome,

    @Schema(description = "Email unico usado para login", example = "kayky@example.com")
    @NotBlank(message = "Email e obrigatorio")
    @Email(message = "Email invalido")
    String email,

    @Schema(description = "Senha com no minimo 6 caracteres", example = "secret123", minLength = 6)
    @NotBlank(message = "Senha e obrigatoria")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    String password
) {}
