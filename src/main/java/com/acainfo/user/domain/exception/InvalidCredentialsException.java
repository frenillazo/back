package com.acainfo.user.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when user credentials are invalid during authentication.
 */
public class InvalidCredentialsException extends BusinessRuleException {

    public InvalidCredentialsException() {
        super("Email o contraseña incorrectos");
    }

    @Override
    public String getErrorCode() {
        return "USER_INVALID_CREDENTIALS";
    }
}
