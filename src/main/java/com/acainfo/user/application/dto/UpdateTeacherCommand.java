package com.acainfo.user.application.dto;

/**
 * Command DTO for updating a teacher.
 */
public record UpdateTeacherCommand(
        String firstName,
        String lastName
) {
}
