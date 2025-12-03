package com.acainfo.user.application.dto;

/**
 * Command DTO for creating a teacher.
 */
public record CreateTeacherCommand(
        String email,
        String password,
        String firstName,
        String lastName
) {
}
