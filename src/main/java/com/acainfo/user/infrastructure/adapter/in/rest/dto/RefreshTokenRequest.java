package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * REST DTO for refresh token requests.
 */
public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
