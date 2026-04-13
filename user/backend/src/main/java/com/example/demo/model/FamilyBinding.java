package com.example.demo.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FamilyBinding {

    private Long id;
    private Long familyMemberId;
    private Long userId;
    private String relationship;
    private Integer status;
    private LocalDateTime createdAt;
}
