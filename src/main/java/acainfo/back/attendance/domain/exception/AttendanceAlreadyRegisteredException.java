package acainfo.back.attendance.domain.exception;

/**
 * Exception thrown when attempting to register attendance that already exists.
 * According to business rules, there can only be one attendance record per student per session.
 */
public class AttendanceAlreadyRegisteredException extends RuntimeException {

    public AttendanceAlreadyRegisteredException(Long sessionId, Long studentId) {
        super(String.format(
            "Attendance already registered for session ID: %d and student ID: %d",
            sessionId, studentId
        ));
    }

    public AttendanceAlreadyRegisteredException(String message) {
        super(message);
    }

    public AttendanceAlreadyRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }
}
