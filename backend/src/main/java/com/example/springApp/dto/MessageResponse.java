package com.example.springApp.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long grupoId,
        UserResponse remetente,
        UserResponse destinatario,
        String nomeRemetenteExibicao,
        String conteudo,
        LocalDateTime dataEnvio,
        boolean lida,
        boolean anonima
) {
}
