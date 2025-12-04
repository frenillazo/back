package com.acainfo.subject.application.port.in;

import com.acainfo.subject.domain.model.Subject;

/**
 * Use case for deleting subjects.
 * Input port defining the contract for subject deletion and archiving.
 */
public interface DeleteSubjectUseCase {

    /**
     * Delete a subject.
     * Note: Should verify that no active groups are associated.
     *
     * @param id Subject ID
     */
    void delete(Long id);

    /**
     * Archive a subject (soft delete).
     * Sets status to ARCHIVED.
     *
     * @param id Subject ID
     * @return The archived subject
     */
    Subject archive(Long id);
}
