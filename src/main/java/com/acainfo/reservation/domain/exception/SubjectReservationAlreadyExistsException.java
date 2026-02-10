package com.acainfo.reservation.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a student already has a confirmed reservation
 * for another session of the same subject.
 * Students should use "Switch Session" instead of creating a new reservation.
 */
public class SubjectReservationAlreadyExistsException extends BusinessRuleException {

    public SubjectReservationAlreadyExistsException(Long studentId, Long subjectId) {
        super("Student " + studentId + " already has a confirmed reservation for subject " + subjectId
                + ". Use 'Cambiar Sesion' to switch to a different session.");
    }

    @Override
    public String getErrorCode() {
        return "SUBJECT_RESERVATION_ALREADY_EXISTS";
    }
}
