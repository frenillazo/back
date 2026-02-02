package com.acainfo.user.application.dto;

import com.acainfo.subject.domain.model.Degree;

/**
 * Command DTO for user registration.
 */
public record RegisterUserCommand(
        String email,
        String password,
        String firstName,
        String lastName,
        String phoneNumber,
        Degree degree
) {
}
