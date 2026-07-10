package com.acainfo.enrollment.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a student is already enrolled in a group.
 */
public class AlreadyEnrolledException extends BusinessRuleException {

    public AlreadyEnrolledException(Long studentId, Long courseId) {
        super("Student " + studentId + " is already enrolled or on waiting list for course " + courseId);
    }

    @Override
    public String getErrorCode() {
        return "ALREADY_ENROLLED";
    }
}
