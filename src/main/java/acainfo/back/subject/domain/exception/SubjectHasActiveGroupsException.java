package acainfo.back.subject.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when attempting to delete a subject that has active groups.
 */
public class SubjectHasActiveGroupsException extends DomainException {

    public SubjectHasActiveGroupsException(Long subjectId, int groupCount) {
        super("Cannot delete subject with id " + subjectId +
              " because it has " + groupCount + " active subjectGroup(s)");
    }

    public SubjectHasActiveGroupsException(String code, int groupCount) {
        super("Cannot delete subject with code '" + code +
              "' because it has " + groupCount + " active subjectGroup(s)");
    }

    public SubjectHasActiveGroupsException(String message) {
        super(message);
    }
}
