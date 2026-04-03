package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRecordResponse {

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
    private List<KnowledgeRecordItemResponse> items;
}
