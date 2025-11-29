package acainfo.back.material.domain.exception;

import acainfo.back.config.exception.DomainException;

/**
 * Exception thrown when an invalid file type is provided.
 * Only PDF, Java, C++, and Header files are allowed.
 */
public class InvalidFileTypeException extends DomainException {

    public InvalidFileTypeException(String message) {
        super(message);
    }

    public InvalidFileTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
