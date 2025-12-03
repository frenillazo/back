package com.acainfo.shared.domain.exception;

/**
 * Base exception for validation errors.
 */
public class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "VALIDATION_ERROR";
    }
}
