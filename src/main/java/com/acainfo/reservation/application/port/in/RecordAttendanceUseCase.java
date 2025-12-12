package com.acainfo.reservation.application.port.in;

import com.acainfo.reservation.application.dto.BulkRecordAttendanceCommand;
import com.acainfo.reservation.application.dto.RecordAttendanceCommand;
import com.acainfo.reservation.domain.model.SessionReservation;

import java.util.List;

/**
 * Use case for recording attendance.
 * Input port defining the contract for attendance recording.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Attendance can only be recorded once per reservation</li>
 *   <li>Only teachers or admins can record attendance</li>
 *   <li>Session should be completed or in progress</li>
 * </ul>
 */
public interface RecordAttendanceUseCase {

    /**
     * Record attendance for a single reservation.
     *
     * @param command Attendance data (reservationId, status, recordedById)
     * @return The updated reservation with attendance recorded
     * @throws com.acainfo.reservation.domain.exception.ReservationNotFoundException if not found
     * @throws com.acainfo.reservation.domain.exception.AttendanceAlreadyRecordedException if already recorded
     */
    SessionReservation recordSingle(RecordAttendanceCommand command);

    /**
     * Record attendance for multiple reservations at once.
     *
     * @param command Bulk attendance data (sessionId, attendanceMap, recordedById)
     * @return List of updated reservations
     * @throws com.acainfo.reservation.domain.exception.AttendanceAlreadyRecordedException if any already recorded
     */
    List<SessionReservation> recordBulk(BulkRecordAttendanceCommand command);
}
