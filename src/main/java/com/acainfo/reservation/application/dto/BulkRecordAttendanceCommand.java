package com.acainfo.reservation.application.dto;

import com.acainfo.reservation.domain.model.AttendanceStatus;

import java.util.Map;

/**
 * Command to record attendance for multiple reservations at once.
 * Used to record attendance for all students in a session.
 *
 * @param sessionId ID of the session
 * @param attendanceMap Map of reservationId to AttendanceStatus
 * @param recordedById ID of the user recording attendance (teacher/admin)
 */
public record BulkRecordAttendanceCommand(
        Long sessionId,
        Map<Long, AttendanceStatus> attendanceMap,
        Long recordedById
) {
}
