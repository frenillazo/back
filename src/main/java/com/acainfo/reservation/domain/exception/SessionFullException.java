package com.acainfo.reservation.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a session has no available seats for in-person attendance.
 */
public class SessionFullException extends BusinessRuleException {

    public SessionFullException(Long sessionId) {
        super("La sesión " + sessionId + " no tiene plazas presenciales disponibles");
    }

    public SessionFullException(Long sessionId, int capacity) {
        super("La sesión " + sessionId + " ha alcanzado su capacidad presencial de " + capacity);
    }

    @Override
    public String getErrorCode() {
        return "SESSION_FULL";
    }
}
