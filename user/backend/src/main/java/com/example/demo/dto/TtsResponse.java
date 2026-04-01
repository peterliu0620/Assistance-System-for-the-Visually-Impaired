package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TtsResponse {

    private String audioId;
    private String audioUrl;
    private long expiresInSeconds;
}
