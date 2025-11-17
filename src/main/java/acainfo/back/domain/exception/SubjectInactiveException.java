package acainfo.back.domain.exception;

/**
 * Exception thrown when attempting to create a group for an inactive subject.
 */
public class SubjectInactiveException extends DomainException {

    public SubjectInactiveException(Long subjectId) {
        super("Cannot create group for inactive subject with id: " + subjectId);
    }

    public SubjectInactiveException(String subjectCode) {
        super("Cannot create group for inactive subject: " + subjectCode);
    }
}
