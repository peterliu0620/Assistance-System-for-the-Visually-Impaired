package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicineProfileRequest {
    @NotNull
    private Long userId;
    private Long familyMemberId;
    @NotBlank
    private String medicineName;
    private String genericName;
    private String description;
    private String dosageUsage;
    private String suitablePeople;
    private String contraindications;
    private LocalDate expiryDate;
    private String barcodeOrAlias;
}
