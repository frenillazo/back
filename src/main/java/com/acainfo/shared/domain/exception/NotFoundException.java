package com.acainfo.shared.domain.exception;

/**
 * Base exception for entity not found errors.
 */
public abstract class NotFoundException extends DomainException {

    protected NotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "NOT_FOUND";
    }
}
