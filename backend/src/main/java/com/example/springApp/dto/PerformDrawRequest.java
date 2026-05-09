package com.example.springApp.dto;

import jakarta.validation.constraints.NotNull;

public record PerformDrawRequest(
        @NotNull Long donoId
) {
}
