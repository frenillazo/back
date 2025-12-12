package com.acainfo.reservation.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when attempting to create a duplicate reservation.
 */
public class ReservationAlreadyExistsException extends BusinessRuleException {

    public ReservationAlreadyExistsException(Long studentId, Long sessionId) {
        super("Student " + studentId + " already has a reservation for session " + sessionId);
    }

    @Override
    public String getErrorCode() {
        return "RESERVATION_ALREADY_EXISTS";
    }
}
