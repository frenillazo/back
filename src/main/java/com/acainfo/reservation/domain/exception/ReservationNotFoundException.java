package com.acainfo.reservation.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when a session reservation is not found.
 */
public class ReservationNotFoundException extends NotFoundException {

    public ReservationNotFoundException(Long id) {
        super("Reserva de sesión no encontrada con ID: " + id);
    }

    public ReservationNotFoundException(Long studentId, Long sessionId) {
        super("Reserva de sesión no encontrada para el estudiante " + studentId + " y la sesión " + sessionId);
    }

    @Override
    public String getErrorCode() {
        return "RESERVATION_NOT_FOUND";
    }
}
