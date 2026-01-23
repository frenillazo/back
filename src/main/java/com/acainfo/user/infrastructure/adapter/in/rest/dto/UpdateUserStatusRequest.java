package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import com.acainfo.user.domain.model.UserStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating user status.
 */
public record UpdateUserStatusRequest(
        @NotNull(message = "Status is required")
        UserStatus status
) {
}
