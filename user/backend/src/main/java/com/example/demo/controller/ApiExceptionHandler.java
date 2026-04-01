package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException ex) {
        return Map.of(
                "code", "BAD_REQUEST",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, Object> handleIllegalState(IllegalStateException ex) {
        return Map.of(
                "code", "UPSTREAM_ERROR",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return Map.of(
                "code", "VALIDATION_ERROR",
                "message", message,
                "timestamp", LocalDateTime.now()
        );
    }
}
