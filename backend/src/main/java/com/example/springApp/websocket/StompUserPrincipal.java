package com.example.springApp.websocket;

import java.security.Principal;

public class StompUserPrincipal implements Principal {

    private final String name;

    public StompUserPrincipal(String name) {
        this.name = name;
    }

    /**
     * Retorna o identificador usado pelo Spring para rotear mensagens privadas do WebSocket.
     */
    @Override
    public String getName() {
        return name;
    }
}
