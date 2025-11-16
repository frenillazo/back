package acainfo.back.application.ports.in;

import acainfo.back.domain.model.Subject;

/**
 * Use case for creating a new subject.
 */
public interface CreateSubjectUseCase {

    /**
     * Creates a new subject in the system.
     *
     * @param subject the subject to create
     * @return the created subject with generated ID
     * @throws acainfo.back.domain.exception.DuplicateSubjectCodeException if code already exists
     */
    Subject createSubject(Subject subject);
}
