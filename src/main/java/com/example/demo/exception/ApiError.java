package com.example.demo.exception;

public record ApiError(
        int status,
        String message,
        String correlationId
) {
}
