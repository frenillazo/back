package com.acainfo.subject.application.port.in;

import com.acainfo.subject.application.dto.SubjectInterestSummary;

import java.util.List;

/**
 * Use case for the minimal "me interesa" flow on subjects.
 */
public interface SubjectInterestUseCase {

    /**
     * Mark interest of a student in a subject. Idempotent (already marked = no-op).
     */
    void markInterest(Long subjectId, Long studentId);

    /**
     * Remove a student's interest in a subject. Idempotent.
     */
    void removeInterest(Long subjectId, Long studentId);

    /**
     * Whether the student has marked interest in the subject.
     */
    boolean hasInterest(Long subjectId, Long studentId);

    /**
     * Subject ids the student has marked interest in.
     */
    List<Long> getInterestSubjectIds(Long studentId);

    /**
     * Aggregated demand per subject (for the admin view).
     */
    List<SubjectInterestSummary> getInterestSummary();
}
