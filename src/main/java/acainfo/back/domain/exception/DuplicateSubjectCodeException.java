package acainfo.back.domain.exception;

/**
 * Exception thrown when attempting to create or update a subject with a code that already exists.
 */
public class DuplicateSubjectCodeException extends DomainException {

    public DuplicateSubjectCodeException(String code) {
        super("Subject with code '" + code + "' already exists");
    }

    public DuplicateSubjectCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
