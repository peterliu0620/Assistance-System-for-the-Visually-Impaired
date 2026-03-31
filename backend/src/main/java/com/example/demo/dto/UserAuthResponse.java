package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAuthResponse {

    private Long id;
    private String username;
    private String nickname;
}
