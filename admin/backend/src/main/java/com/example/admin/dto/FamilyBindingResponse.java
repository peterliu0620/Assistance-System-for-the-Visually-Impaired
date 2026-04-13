package com.example.admin.dto;

import java.time.LocalDateTime;

public record FamilyBindingResponse(
        Long id,
        Long familyMemberId,
        String familyName,
        String familyPhone,
        String familyEmail,
        Long userId,
        String username,
        String nickname,
        String relationship,
        String status,
        LocalDateTime createdAt
) {
}
