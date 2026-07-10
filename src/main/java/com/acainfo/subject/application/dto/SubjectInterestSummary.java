package com.acainfo.subject.application.dto;

/**
 * Aggregated interest ("me interesa") for one subject.
 */
public record SubjectInterestSummary(
        Long subjectId,
        String subjectName,
        String subjectCode,
        long interestedStudents
) {
}
