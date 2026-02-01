package com.acainfo.user.application.dto;

/**
 * Command DTO for user registration.
 */
public record RegisterUserCommand(
        String email,
        String password,
        String firstName,
        String lastName,
        String phoneNumber
) {
}
