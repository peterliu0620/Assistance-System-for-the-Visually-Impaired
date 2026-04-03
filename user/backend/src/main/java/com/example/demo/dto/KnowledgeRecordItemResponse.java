package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRecordItemResponse {

    private Long id;
    private String name;
    private String position;
    private Integer confidence;
    private String attributesJson;
    private Boolean primary;
}
