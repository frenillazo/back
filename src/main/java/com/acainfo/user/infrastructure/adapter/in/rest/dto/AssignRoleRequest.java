package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import com.acainfo.user.domain.model.RoleType;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for assigning a role to a user.
 */
public record AssignRoleRequest(
        @NotNull(message = "El tipo de rol es obligatorio")
        RoleType roleType
) {
}
