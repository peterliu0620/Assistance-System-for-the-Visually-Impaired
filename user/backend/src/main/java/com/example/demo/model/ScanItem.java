package com.example.demo.model;

import lombok.Data;

@Data
public class ScanItem {

    private Long id;
    private Long recordId;
    private String name;
    private String position;
    private Integer confidence;
    private String attributesJson;
    private Boolean isPrimary;
}
