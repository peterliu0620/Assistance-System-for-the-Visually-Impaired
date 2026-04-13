package com.example.admin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MedicineProfileResponse(
        Long id,
        Long userId,
        String username,
        String nickname,
        Long familyMemberId,
        String familyName,
        String medicineName,
        String genericName,
        String description,
        String dosageUsage,
        String suitablePeople,
        String contraindications,
        LocalDate expiryDate,
        String barcodeOrAlias,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
