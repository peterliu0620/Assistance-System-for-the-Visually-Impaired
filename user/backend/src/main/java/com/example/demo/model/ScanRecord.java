package com.example.demo.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScanRecord {

    private Long id;
    private Long userId;
    private String sessionId;
    private String mode;
    private String triggerCommand;
    private String scene;
    private String narration;
    private String positionSummary;
    private LocalDateTime capturedAt;
    private Double latitude;
    private Double longitude;
    private String locationText;
    private LocalDateTime createdAt;
}
