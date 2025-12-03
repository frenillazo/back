package com.acainfo.user.application.dto;

import com.acainfo.user.domain.model.User;

/**
 * Result DTO for authentication containing tokens and user data.
 */
public record AuthenticationResult(
        String accessToken,
        String refreshToken,
        User user
) {
}
