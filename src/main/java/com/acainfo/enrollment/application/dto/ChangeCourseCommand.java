package com.acainfo.enrollment.application.dto;

/**
 * Command DTO for changing a student's enrollment to a different group.
 * Used for moving between parallel groups of the same subject.
 */
public record ChangeCourseCommand(
        Long enrollmentId,
        Long newCourseId
) {
}
