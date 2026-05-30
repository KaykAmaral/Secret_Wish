package com.example.springApp.dto;

import com.example.springApp.model.Group;

/**
 * Projecao usada em listagens para retornar dados do grupo e contagem sem carregar membros.
 */
public record GroupSummary(Group group, long memberCount) {
}
