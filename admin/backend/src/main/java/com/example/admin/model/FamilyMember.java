package com.example.admin.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FamilyMember {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
