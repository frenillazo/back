package com.acainfo.user.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when user credentials are invalid during authentication.
 */
public class InvalidCredentialsException extends BusinessRuleException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    @Override
    public String getErrorCode() {
        return "USER_INVALID_CREDENTIALS";
    }
}
