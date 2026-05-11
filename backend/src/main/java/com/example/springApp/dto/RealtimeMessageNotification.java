package com.example.springApp.dto;

import java.time.LocalDateTime;

public record RealtimeMessageNotification(
        Long groupId,
        Long messageId,
        String senderDisplayName,
        String preview,
        LocalDateTime sentAt,
        Long unreadCount
) {
}
