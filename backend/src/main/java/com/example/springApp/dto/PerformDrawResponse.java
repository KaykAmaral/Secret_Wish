package com.example.springApp.dto;

import java.time.LocalDateTime;

public record PerformDrawResponse(
        Long groupId,
        int participantCount,
        LocalDateTime performedAt
) {
}
