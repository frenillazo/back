package com.acainfo.reservation.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a student already has a confirmed reservation
 * for another session of the same subject.
 * Students should use "Switch Session" instead of creating a new reservation.
 */
public class SubjectReservationAlreadyExistsException extends BusinessRuleException {

    public SubjectReservationAlreadyExistsException(Long studentId, Long subjectId) {
        super("El estudiante " + studentId + " ya tiene una reserva confirmada para la asignatura " + subjectId
                + ". Use 'Cambiar Sesion' para cambiar a otra sesión.");
    }

    @Override
    public String getErrorCode() {
        return "SUBJECT_RESERVATION_ALREADY_EXISTS";
    }
}
