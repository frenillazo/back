package com.acainfo.reservation.application.dto;

import com.acainfo.reservation.domain.model.AttendanceStatus;

/**
 * Command to record attendance for a single reservation.
 *
 * @param reservationId ID of the reservation
 * @param status Attendance status: PRESENT or ABSENT
 * @param recordedById ID of the user recording attendance (teacher/admin)
 */
public record RecordAttendanceCommand(
        Long reservationId,
        AttendanceStatus status,
        Long recordedById
) {
}
