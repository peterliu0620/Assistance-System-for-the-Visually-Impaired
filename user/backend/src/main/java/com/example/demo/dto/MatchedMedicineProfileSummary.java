package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchedMedicineProfileSummary {

    private Long id;
    private String medicineName;
    private String dosageUsage;
    private String suitablePeople;
    private String contraindications;
    private LocalDate expiryDate;
}
