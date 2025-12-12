package com.acainfo.reservation.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when an operation is not valid for the current reservation state.
 */
public class InvalidReservationStateException extends BusinessRuleException {

    public InvalidReservationStateException(String message) {
        super(message);
    }

    public InvalidReservationStateException(Long reservationId, String currentState, String operation) {
        super("Cannot " + operation + " reservation " + reservationId + " in state " + currentState);
    }

    @Override
    public String getErrorCode() {
        return "INVALID_RESERVATION_STATE";
    }
}
