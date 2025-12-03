package com.acainfo.shared.infrastructure.rest.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response DTO for REST API.
 * Used by GlobalExceptionHandler to return consistent error format.
 */
@Builder
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors
) {
    /**
     * Creates an ErrorResponse without field errors.
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .fieldErrors(null)
                .build();
    }

    /**
     * Creates an ErrorResponse with field validation errors.
     */
    public static ErrorResponse withFieldErrors(int status, String error, String message, String path, List<FieldError> fieldErrors) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .fieldErrors(fieldErrors)
                .build();
    }

    /**
     * Field validation error detail.
     */
    public record FieldError(
            String field,
            Object rejectedValue,
            String message
    ) {
    }
}
