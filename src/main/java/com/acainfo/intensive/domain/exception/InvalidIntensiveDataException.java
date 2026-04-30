package com.acainfo.intensive.domain.exception;

import com.acainfo.shared.domain.exception.ValidationException;

/**
 * Exception thrown when intensive course data is invalid.
 */
public class InvalidIntensiveDataException extends ValidationException {

    public InvalidIntensiveDataException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "INTENSIVE_INVALID_DATA";
    }
}
