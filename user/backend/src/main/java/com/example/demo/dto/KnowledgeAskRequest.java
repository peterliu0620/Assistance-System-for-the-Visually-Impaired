package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KnowledgeAskRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "问题不能为空")
    private String question;

    private String sessionId;
}
