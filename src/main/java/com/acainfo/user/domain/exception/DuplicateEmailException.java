package com.acainfo.user.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when attempting to register a user with an email that already exists.
 */
public class DuplicateEmailException extends BusinessRuleException {

    public DuplicateEmailException(String email) {
        super("Email already registered: " + email);
    }

    @Override
    public String getErrorCode() {
        return "USER_DUPLICATE_EMAIL";
    }
}
