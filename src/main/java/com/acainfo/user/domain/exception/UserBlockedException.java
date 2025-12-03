package com.acainfo.user.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when attempting to perform actions with a blocked user.
 */
public class UserBlockedException extends BusinessRuleException {

    public UserBlockedException(String email) {
        super("User is blocked: " + email);
    }

    @Override
    public String getErrorCode() {
        return "USER_BLOCKED";
    }
}
