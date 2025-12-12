package com.acainfo.reservation.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a session has no available seats for in-person attendance.
 */
public class SessionFullException extends BusinessRuleException {

    public SessionFullException(Long sessionId) {
        super("Session " + sessionId + " has no available in-person seats");
    }

    public SessionFullException(Long sessionId, int capacity) {
        super("Session " + sessionId + " has reached its in-person capacity of " + capacity);
    }

    @Override
    public String getErrorCode() {
        return "SESSION_FULL";
    }
}
