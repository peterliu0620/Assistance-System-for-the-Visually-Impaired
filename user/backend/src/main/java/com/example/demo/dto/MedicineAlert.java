package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineAlert {

    private Boolean triggered;
    private String alertType;
    private String alertMessage;
    private String warningLevel;
    private MatchedMedicineProfileSummary matchedMedicineProfile;
}
