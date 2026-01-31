package com.acainfo.user.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a user attempts to login without having verified their email.
 */
public class EmailNotVerifiedException extends BusinessRuleException {

    public EmailNotVerifiedException(String email) {
        super("Por favor, verifica tu email antes de iniciar sesion: " + email);
    }

    @Override
    public String getErrorCode() {
        return "EMAIL_NOT_VERIFIED";
    }
}
