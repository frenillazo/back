package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import com.acainfo.user.domain.model.UserStatus;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * REST DTO for teacher responses.
 * Simplified version of UserResponse focused on teacher information.
 */
@Builder
public record TeacherResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
