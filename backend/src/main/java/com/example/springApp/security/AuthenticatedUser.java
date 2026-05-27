package com.example.springApp.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUser {

    /**
     * Extrai o id normalizado do principal criado pelo filtro JWT.
     */
    public Long id(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        if (principal instanceof String value) {
            return Long.valueOf(value);
        }
        throw new IllegalStateException("Usuario autenticado invalido");
    }
}
