package acainfo.back.session.domain.exception;

/**
 * Exception thrown when a session scheduling conflict is detected.
 * This can occur when:
 * - A classroom is already booked for the requested time slot
 * - A teacher has another session scheduled at the same time
 * - Students have overlapping sessions
 */
public class SessionConflictException extends RuntimeException {

    private final ConflictType conflictType;

    public SessionConflictException(ConflictType conflictType, String message) {
        super(message);
        this.conflictType = conflictType;
    }

    public SessionConflictException(ConflictType conflictType, String message, Throwable cause) {
        super(message, cause);
        this.conflictType = conflictType;
    }

    public ConflictType getConflictType() {
        return conflictType;
    }

    /**
     * Types of scheduling conflicts
     */
    public enum ConflictType {
        CLASSROOM_OCCUPIED("The classroom is already occupied during the requested time slot"),
        TEACHER_CONFLICT("The teacher has another session scheduled at the same time"),
        STUDENT_CONFLICT("Students have overlapping sessions"),
        TIME_OVERLAP("The requested time slot overlaps with an existing session");

        private final String description;

        ConflictType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Factory method for classroom conflicts
     */
    public static SessionConflictException classroomOccupied(String classroom, String timeSlot) {
        return new SessionConflictException(
            ConflictType.CLASSROOM_OCCUPIED,
            String.format("Classroom %s is already occupied during %s", classroom, timeSlot)
        );
    }

    /**
     * Factory method for teacher conflicts
     */
    public static SessionConflictException teacherConflict(String teacherName, String timeSlot) {
        return new SessionConflictException(
            ConflictType.TEACHER_CONFLICT,
            String.format("Teacher %s has another session scheduled during %s", teacherName, timeSlot)
        );
    }

    /**
     * Factory method for time overlap
     */
    public static SessionConflictException timeOverlap(String details) {
        return new SessionConflictException(
            ConflictType.TIME_OVERLAP,
            "Time slot overlaps with existing session: " + details
        );
    }
}
