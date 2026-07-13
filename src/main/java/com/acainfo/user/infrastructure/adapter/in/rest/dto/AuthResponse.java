package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import lombok.Builder;

/**
 * REST DTO for authentication responses (login and refresh).
 * Contains the JWT access token and user information.
 *
 * <p>El refresh token NO viaja aquí: se entrega en una cookie httpOnly
 * (ver {@code AuthCookieService}).</p>
 */
@Builder
public record AuthResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        UserResponse user,
        boolean termsAccepted
) {
    /**
     * Creates an AuthResponse with default token type "Bearer".
     */
    public static AuthResponse of(String accessToken, Long expiresIn, UserResponse user, boolean termsAccepted) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .termsAccepted(termsAccepted)
                .build();
    }
}
