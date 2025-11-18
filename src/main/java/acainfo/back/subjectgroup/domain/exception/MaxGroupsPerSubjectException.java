package acainfo.back.subjectgroup.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when attempting to create more than the maximum allowed groups per subject.
 * Maximum is 3 groups per subject.
 */
public class MaxGroupsPerSubjectException extends DomainException {

    private static final int MAX_GROUPS = 3;

    public MaxGroupsPerSubjectException(Long subjectId) {
        super("Cannot create more than " + MAX_GROUPS + " groups for subject with id: " + subjectId);
    }

    public MaxGroupsPerSubjectException(String subjectCode) {
        super("Cannot create more than " + MAX_GROUPS + " groups for subject: " + subjectCode);
    }

    public MaxGroupsPerSubjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
