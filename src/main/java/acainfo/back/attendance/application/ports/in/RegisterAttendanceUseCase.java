package acainfo.back.attendance.application.ports.in;

import acainfo.back.attendance.domain.model.Attendance;
import acainfo.back.attendance.domain.model.AttendanceStatus;

import java.util.List;

/**
 * Use case port for registering student attendance.
 * Defines the contract for attendance registration operations.
 */
public interface RegisterAttendanceUseCase {

    /**
     * Registers attendance for a single student in a session.
     *
     * @param command the command containing attendance data
     * @return the created attendance record
     * @throws acainfo.back.attendance.domain.exception.AttendanceAlreadyRegisteredException if attendance already exists
     * @throws acainfo.back.attendance.domain.exception.InvalidAttendanceOperationException if session is not completed
     * @throws acainfo.back.session.domain.exception.SessionNotFoundException if session doesn't exist
     */
    Attendance registerAttendance(RegisterAttendanceCommand command);

    /**
     * Registers attendance for multiple students in a session (bulk operation).
     * Typically used by teachers to register attendance for an entire class.
     *
     * @param command the command containing bulk attendance data
     * @return list of created attendance records
     * @throws acainfo.back.attendance.domain.exception.InvalidAttendanceOperationException if session is not completed
     * @throws acainfo.back.session.domain.exception.SessionNotFoundException if session doesn't exist
     */
    List<Attendance> registerBulkAttendance(RegisterBulkAttendanceCommand command);

    /**
     * Command object for registering a single attendance
     */
    record RegisterAttendanceCommand(
        Long sessionId,
        Long studentId,
        String status,        // PRESENTE, AUSENTE, TARDANZA
        Integer minutesLate,  // Only for TARDANZA
        String notes,         // Optional
        Long recordedById     // Teacher/admin who is recording
    ) {}

    /**
     * Command object for bulk attendance registration
     */
    record RegisterBulkAttendanceCommand(
        Long sessionId,
        List<StudentAttendanceData> attendances,
        Long recordedById
    ) {}

    /**
     * Data for individual student attendance in bulk operation
     */
    record StudentAttendanceData(
        Long studentId,
        String status,
        Integer minutesLate,
        String notes
    ) {}
}
