package com.example.springApp.websocket;

import java.security.Principal;

public class StompUserPrincipal implements Principal {

    private final String name;

    public StompUserPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
