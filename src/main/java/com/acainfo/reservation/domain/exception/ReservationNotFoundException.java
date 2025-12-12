package com.acainfo.reservation.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when a session reservation is not found.
 */
public class ReservationNotFoundException extends NotFoundException {

    public ReservationNotFoundException(Long id) {
        super("Session reservation not found with ID: " + id);
    }

    public ReservationNotFoundException(Long studentId, Long sessionId) {
        super("Session reservation not found for student " + studentId + " and session " + sessionId);
    }

    @Override
    public String getErrorCode() {
        return "RESERVATION_NOT_FOUND";
    }
}
