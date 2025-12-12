package com.acainfo.enrollment.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when an operation is invalid for the current group request state.
 */
public class InvalidGroupRequestStateException extends BusinessRuleException {

    public InvalidGroupRequestStateException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "INVALID_GROUP_REQUEST_STATE";
    }
}
