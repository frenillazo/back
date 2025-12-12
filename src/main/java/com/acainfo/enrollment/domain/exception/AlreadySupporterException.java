package com.acainfo.enrollment.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a student is already a supporter of a group request.
 */
public class AlreadySupporterException extends BusinessRuleException {

    public AlreadySupporterException(Long studentId, Long groupRequestId) {
        super("Student " + studentId + " is already a supporter of group request " + groupRequestId);
    }

    @Override
    public String getErrorCode() {
        return "ALREADY_SUPPORTER";
    }
}
