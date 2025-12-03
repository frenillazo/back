package com.acainfo.shared.domain.exception;

/**
 * Base exception for all domain exceptions.
 * Domain exceptions represent business rule violations or domain-specific errors.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns the error code for this exception.
     * Can be used for internationalization or API error responses.
     */
    public abstract String getErrorCode();
}
