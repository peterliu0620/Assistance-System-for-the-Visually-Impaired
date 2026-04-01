package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisionAnalyzeResponse {

    private String triggerCommand;
    private LocalDateTime capturedAt;
    private String scene;
    private String positionSummary;
    private List<VisionItem> items;
    private String narration;
}
