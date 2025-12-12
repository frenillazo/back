package com.acainfo.reservation.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a student attempts to reserve a session from a different
 * group that is not of the same subject as their enrollment.
 */
public class CrossGroupReservationNotAllowedException extends BusinessRuleException {

    public CrossGroupReservationNotAllowedException(Long studentId, Long sessionId) {
        super("Student " + studentId + " cannot reserve session " + sessionId +
              " because it belongs to a different subject than their enrollment");
    }

    @Override
    public String getErrorCode() {
        return "CROSS_GROUP_RESERVATION_NOT_ALLOWED";
    }
}
