package acainfo.back.application.ports.in;

import acainfo.back.domain.model.Subject;

/**
 * Use case for updating an existing subject.
 */
public interface UpdateSubjectUseCase {

    /**
     * Updates an existing subject in the system.
     *
     * @param id the subject ID to update
     * @param subject the updated subject data
     * @return the updated subject
     * @throws acainfo.back.domain.exception.SubjectNotFoundException if subject not found
     * @throws acainfo.back.domain.exception.DuplicateSubjectCodeException if new code already exists
     */
    Subject updateSubject(Long id, Subject subject);
}
