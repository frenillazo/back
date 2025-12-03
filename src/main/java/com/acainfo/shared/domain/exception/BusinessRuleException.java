package com.acainfo.shared.domain.exception;

/**
 * Base exception for business rule violations.
 */
public abstract class BusinessRuleException extends DomainException {

    protected BusinessRuleException(String message) {
        super(message);
    }

    protected BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return "BUSINESS_RULE_VIOLATION";
    }
}
