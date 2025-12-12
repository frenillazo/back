package com.acainfo.enrollment.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when an operation is invalid for the current enrollment state.
 */
public class InvalidEnrollmentStateException extends BusinessRuleException {

    public InvalidEnrollmentStateException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "INVALID_ENROLLMENT_STATE";
    }
}
