package com.acainfo.user.application.dto;

/**
 * Command DTO for updating user profile.
 */
public record UpdateUserCommand(
        String firstName,
        String lastName,
        String phoneNumber
) {
}
