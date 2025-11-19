package acainfo.back.attendance.domain.exception;

/**
 * Exception thrown when an invalid operation is attempted on attendance.
 * Examples:
 * - Trying to register attendance for a session that is not COMPLETADA
 * - Trying to justify an attendance that is not AUSENTE
 * - Trying to modify attendance after the allowed timeframe
 */
public class InvalidAttendanceOperationException extends RuntimeException {

    public InvalidAttendanceOperationException(String message) {
        super(message);
    }

    public InvalidAttendanceOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidAttendanceOperationException sessionNotCompleted(Long sessionId) {
        return new InvalidAttendanceOperationException(
            "Cannot register attendance for session ID " + sessionId +
            " because it is not in COMPLETADA status"
        );
    }

    public static InvalidAttendanceOperationException cannotJustifyStatus(String currentStatus) {
        return new InvalidAttendanceOperationException(
            "Cannot justify attendance with status: " + currentStatus +
            ". Only AUSENTE can be justified."
        );
    }

    public static InvalidAttendanceOperationException modificationNotAllowed(Long attendanceId) {
        return new InvalidAttendanceOperationException(
            "Attendance record ID " + attendanceId +
            " cannot be modified (timeframe exceeded or other restriction)"
        );
    }
}
