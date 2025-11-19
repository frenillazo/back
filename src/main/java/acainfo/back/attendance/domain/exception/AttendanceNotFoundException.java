package acainfo.back.attendance.domain.exception;

/**
 * Exception thrown when an attendance record is not found.
 */
public class AttendanceNotFoundException extends RuntimeException {

    public AttendanceNotFoundException(Long attendanceId) {
        super("Attendance record not found with ID: " + attendanceId);
    }

    public AttendanceNotFoundException(Long sessionId, Long studentId) {
        super(String.format(
            "Attendance record not found for session ID: %d and student ID: %d",
            sessionId, studentId
        ));
    }

    public AttendanceNotFoundException(String message) {
        super(message);
    }

    public AttendanceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
