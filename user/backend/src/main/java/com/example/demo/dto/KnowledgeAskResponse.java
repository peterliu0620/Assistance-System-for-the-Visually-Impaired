package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class KnowledgeAskResponse {

    private String answer;
    private List<KnowledgeRecordResponse> matchedRecords;
    private String usedSessionId;
    private String timeRange;
}
