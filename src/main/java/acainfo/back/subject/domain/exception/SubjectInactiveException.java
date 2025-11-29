package acainfo.back.subject.domain.exception;

import acainfo.back.config.exception.DomainException;

/**
 * Exception thrown when attempting to create a subjectGroup for an inactive subject.
 */
public class SubjectInactiveException extends DomainException {

    public SubjectInactiveException(Long subjectId) {
        super("Cannot create subjectGroup for inactive subject with id: " + subjectId);
    }

    public SubjectInactiveException(String subjectCode) {
        super("Cannot create subjectGroup for inactive subject: " + subjectCode);
    }

}
