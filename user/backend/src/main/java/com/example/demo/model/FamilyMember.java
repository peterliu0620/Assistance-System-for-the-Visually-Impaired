package com.example.demo.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FamilyMember {

    private Long id;
    private String name;
    private String phone;
    private Integer status;
    private LocalDateTime createdAt;
}
