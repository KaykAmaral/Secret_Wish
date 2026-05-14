package com.example.springApp.dto;

public record ChatSummaryResponse(
        Long grupoId,
        Long outroUsuarioId,
        String nomeExibicao,
        boolean anonimoParaUsuario,
        Long unreadCount
) {
}
