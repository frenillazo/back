package com.acainfo.enrollment.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a student is already enrolled in a group.
 */
public class AlreadyEnrolledException extends BusinessRuleException {

    public AlreadyEnrolledException(Long studentId, Long courseId) {
        super("El estudiante " + studentId + " ya está inscrito o en lista de espera para el curso " + courseId);
    }

    @Override
    public String getErrorCode() {
        return "ALREADY_ENROLLED";
    }
}
