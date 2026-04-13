package com.example.admin.dto;

import java.time.LocalDateTime;

public record SharedLogResponse(
        Long recordId,
        Long userId,
        String username,
        String nickname,
        String mode,
        String triggerCommand,
        String scene,
        String coreItem,
        String locationText,
        LocalDateTime capturedAt,
        boolean alertTriggered,
        String alertType,
        String alertMessage
) {
}
