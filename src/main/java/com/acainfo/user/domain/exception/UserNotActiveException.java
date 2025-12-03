package com.acainfo.user.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when attempting to authenticate with an inactive user.
 */
public class UserNotActiveException extends BusinessRuleException {

    public UserNotActiveException(String email) {
        super("User account is not active: " + email);
    }

    @Override
    public String getErrorCode() {
        return "USER_NOT_ACTIVE";
    }
}
