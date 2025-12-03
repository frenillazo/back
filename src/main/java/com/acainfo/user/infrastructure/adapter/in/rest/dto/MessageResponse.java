package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * REST DTO for generic message responses.
 * Used for success/error messages without entity data.
 */
@Builder
public record MessageResponse(
        String message,
        LocalDateTime timestamp
) {
    /**
     * Creates a MessageResponse with current timestamp.
     */
    public static MessageResponse of(String message) {
        return MessageResponse.builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
