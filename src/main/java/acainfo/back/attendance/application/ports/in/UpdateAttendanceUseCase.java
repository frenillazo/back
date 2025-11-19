package acainfo.back.attendance.application.ports.in;

import acainfo.back.attendance.domain.model.Attendance;

/**
 * Use case port for updating attendance records.
 * Defines the contract for attendance modification operations.
 */
public interface UpdateAttendanceUseCase {

    /**
     * Updates the status of an existing attendance record.
     *
     * @param command the command containing update data
     * @return the updated attendance record
     * @throws acainfo.back.attendance.domain.exception.AttendanceNotFoundException if attendance not found
     * @throws acainfo.back.attendance.domain.exception.InvalidAttendanceOperationException if modification not allowed
     */
    Attendance updateAttendanceStatus(UpdateAttendanceStatusCommand command);

    /**
     * Justifies an absence with documentation/reason.
     * Can only justify attendance with AUSENTE status.
     *
     * @param command the command containing justification data
     * @return the updated attendance record with JUSTIFICADO status
     * @throws acainfo.back.attendance.domain.exception.AttendanceNotFoundException if attendance not found
     * @throws IllegalStateException if current status cannot be justified
     */
    Attendance justifyAbsence(JustifyAbsenceCommand command);

    /**
     * Marks a student as late with specified minutes.
     *
     * @param command the command containing tardiness data
     * @return the updated attendance record with TARDANZA status
     * @throws acainfo.back.attendance.domain.exception.AttendanceNotFoundException if attendance not found
     */
    Attendance markAsLate(MarkAsLateCommand command);

    /**
     * Command object for updating attendance status
     */
    record UpdateAttendanceStatusCommand(
        Long attendanceId,
        String newStatus,    // PRESENTE, AUSENTE, TARDANZA
        String notes,        // Optional
        Long updatedById     // User making the change
    ) {}

    /**
     * Command object for justifying an absence
     */
    record JustifyAbsenceCommand(
        Long attendanceId,
        String justificationReason,
        Long justifiedById   // User approving the justification (teacher/admin)
    ) {}

    /**
     * Command object for marking tardiness
     */
    record MarkAsLateCommand(
        Long attendanceId,
        Integer minutesLate,
        String notes,
        Long updatedById
    ) {}
}
