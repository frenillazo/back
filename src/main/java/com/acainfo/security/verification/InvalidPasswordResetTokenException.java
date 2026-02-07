package com.acainfo.security.verification;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when password reset token is invalid, expired, or already used.
 */
public class InvalidPasswordResetTokenException extends BusinessRuleException {

    public InvalidPasswordResetTokenException(String message) {
        super(message);
    }

    public InvalidPasswordResetTokenException() {
        super("Token de recuperacion invalido o expirado");
    }

    @Override
    public String getErrorCode() {
        return "INVALID_PASSWORD_RESET_TOKEN";
    }
}
