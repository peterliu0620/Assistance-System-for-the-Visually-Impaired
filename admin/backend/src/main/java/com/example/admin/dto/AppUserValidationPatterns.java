package com.example.admin.dto;

public final class AppUserValidationPatterns {

    public static final String PHONE = "^\\s*$|^\\s*1\\d{10}\\s*$";
    public static final String EMAIL = "^\\s*$|^\\s*[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}\\s*$";

    private AppUserValidationPatterns() {
    }
}
