package com.example.demo.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysUser {

    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String phone;
    private String email;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
