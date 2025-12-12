package com.acainfo.reservation.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when attempting to record attendance that was already recorded.
 */
public class AttendanceAlreadyRecordedException extends BusinessRuleException {

    public AttendanceAlreadyRecordedException(Long reservationId) {
        super("Attendance has already been recorded for reservation " + reservationId);
    }

    @Override
    public String getErrorCode() {
        return "ATTENDANCE_ALREADY_RECORDED";
    }
}
