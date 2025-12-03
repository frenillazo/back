package com.acainfo.user.application.dto;

/**
 * Command DTO for user authentication (login).
 */
public record AuthenticationCommand(
        String email,
        String password
) {
}
