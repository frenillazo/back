package com.acainfo.user.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when attempting to register with an email domain that is not allowed.
 * Only @red.ujaen.es and @gmail.com domains are permitted.
 */
public class InvalidEmailDomainException extends BusinessRuleException {

    public InvalidEmailDomainException() {
        super("Solo se permiten emails de @red.ujaen.es o @gmail.com");
    }

    @Override
    public String getErrorCode() {
        return "INVALID_EMAIL_DOMAIN";
    }
}
