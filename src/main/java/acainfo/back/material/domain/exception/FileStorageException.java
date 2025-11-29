package acainfo.back.material.domain.exception;

import acainfo.back.config.exception.DomainException;

/**
 * Exception thrown when there is an error storing or retrieving files.
 */
public class FileStorageException extends DomainException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
