package com.acainfo.enrollment.application.dto;

/**
 * Command DTO for enrolling a student in a group.
 */
public record EnrollStudentCommand(
        Long studentId,
        Long groupId
) {
}
