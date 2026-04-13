package com.example.demo.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MedicineScanAlert {

    private Long id;
    private Long userId;
    private Long scanRecordId;
    private Long medicineProfileId;
    private String alertType;
    private String alertMessage;
    private LocalDateTime createdAt;
}
