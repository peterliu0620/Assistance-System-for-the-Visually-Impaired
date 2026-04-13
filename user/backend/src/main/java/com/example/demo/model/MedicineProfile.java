package com.example.demo.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MedicineProfile {

    private Long id;
    private Long userId;
    private Long familyMemberId;
    private String medicineName;
    private String genericName;
    private String description;
    private String dosageUsage;
    private String suitablePeople;
    private String contraindications;
    private LocalDate expiryDate;
    private String barcodeOrAlias;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
