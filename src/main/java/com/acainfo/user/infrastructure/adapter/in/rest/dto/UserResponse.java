package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import com.acainfo.user.domain.model.UserStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * REST DTO for user responses.
 * Contains user information without sensitive data (no password).
 */
@Builder
public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        UserStatus status,
        Set<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
