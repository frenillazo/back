package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import lombok.Builder;

/**
 * REST DTO for authentication responses (login and refresh).
 * Contains JWT tokens and user information.
 */
@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {
    /**
     * Creates an AuthResponse with default token type "Bearer".
     */
    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn, UserResponse user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }
}
