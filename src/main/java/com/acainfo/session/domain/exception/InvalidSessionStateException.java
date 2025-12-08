package com.acainfo.session.domain.exception;

import com.acainfo.shared.domain.exception.ValidationException;

public class InvalidSessionStateException extends ValidationException {
    public InvalidSessionStateException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "SESSION_INVALID_STATE";
    }
}
