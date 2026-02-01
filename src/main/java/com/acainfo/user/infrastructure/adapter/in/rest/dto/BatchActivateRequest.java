package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request for batch activation of users.
 */
public record BatchActivateRequest(
        @NotEmpty(message = "User IDs list cannot be empty")
        List<Long> userIds
) {
}
