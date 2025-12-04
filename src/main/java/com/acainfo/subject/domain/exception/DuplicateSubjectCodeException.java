package com.acainfo.subject.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when attempting to create a subject with a code that already exists.
 */
public class DuplicateSubjectCodeException extends BusinessRuleException {

    public DuplicateSubjectCodeException(String code) {
        super("Subject code already exists: " + code);
    }

    @Override
    public String getErrorCode() {
        return "SUBJECT_DUPLICATE_CODE";
    }
}
