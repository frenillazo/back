package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import com.acainfo.user.domain.model.RoleType;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for revoking a role from a user.
 */
public record RevokeRoleRequest(
        @NotNull(message = "El tipo de rol es obligatorio")
        RoleType roleType
) {
}
