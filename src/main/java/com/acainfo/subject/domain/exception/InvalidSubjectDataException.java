package com.acainfo.subject.domain.exception;

import com.acainfo.shared.domain.exception.ValidationException;

/**
 * Exception thrown when subject data is invalid (e.g., invalid credits, invalid code format).
 */
public class InvalidSubjectDataException extends ValidationException {

    public InvalidSubjectDataException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "SUBJECT_INVALID_DATA";
    }
}
