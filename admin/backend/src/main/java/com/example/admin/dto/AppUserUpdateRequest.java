package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AppUserUpdateRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @Pattern(regexp = "^$|^.{6,20}$", message = "密码长度需在6-20之间")
    private String password;

    @Pattern(regexp = AppUserValidationPatterns.PHONE, message = "手机号格式不正确")
    private String phone;

    @Pattern(regexp = AppUserValidationPatterns.EMAIL, message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
