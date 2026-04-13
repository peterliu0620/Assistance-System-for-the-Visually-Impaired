package com.example.admin.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FamilyBinding {
    private Long id;
    private Long familyMemberId;
    private Long userId;
    private String relationship;
    private String status;
    private LocalDateTime createdAt;
}
