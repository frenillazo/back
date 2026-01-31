package com.acainfo.security.verification;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when email verification token is invalid, expired, or already used.
 */
public class InvalidVerificationTokenException extends BusinessRuleException {

    public InvalidVerificationTokenException(String message) {
        super(message);
    }

    public InvalidVerificationTokenException() {
        super("Token de verificacion invalido o expirado");
    }

    @Override
    public String getErrorCode() {
        return "INVALID_VERIFICATION_TOKEN";
    }
}
