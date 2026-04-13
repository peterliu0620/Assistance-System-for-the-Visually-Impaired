package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FamilyBindingCreateRequest {
    private Long familyMemberId;
    @NotNull
    private Long userId;
    @NotBlank
    private String familyName;
    private String familyPhone;
    private String familyEmail;
    @NotBlank
    private String relationship;
    private String status;
}
