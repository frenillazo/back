package acainfo.back.subject.application.ports.in;

import acainfo.back.subject.domain.exception.DuplicateSubjectCodeException;
import acainfo.back.subject.domain.exception.SubjectNotFoundException;
import acainfo.back.subject.domain.model.Subject;

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
     * @throws SubjectNotFoundException if subject not found
     * @throws DuplicateSubjectCodeException if new code already exists
     */
    Subject updateSubject(Long id, Subject subject);
}
