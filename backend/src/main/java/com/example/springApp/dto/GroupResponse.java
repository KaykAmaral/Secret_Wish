package com.example.springApp.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record GroupResponse(
        Long id,
        String nome,
        String codigoUnico,
        UserResponse dono,
        Set<UserResponse> membros,
        LocalDateTime dataCriacao,
        LocalDateTime dataSorteio,
        LocalDateTime dataEvento
) {
}
