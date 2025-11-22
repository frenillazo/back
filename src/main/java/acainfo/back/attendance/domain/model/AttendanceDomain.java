package acainfo.back.attendance.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;

/**
 * Pure domain model for Attendance (POJO).
 * No infrastructure dependencies - Pure business logic.
 *
 * Represents student attendance for a session.
 * Tracks whether a student attended, was absent, arrived late,
 * or had a justified absence for a specific session.
 *
 * Business Rules:
 * - One attendance record per enrollment per session
 * - Enrollment must be ACTIVO to register attendance
 * - Can only register attendance for COMPLETADA sessions
 * - Status can be updated (e.g., AUSENTE -> JUSTIFICADO)
 * - Teachers can only register attendance for their own groups
 * - Attendance affects access to materials (students with many absences may be restricted)
 *
 * Immutable design: State transitions return new instances via toBuilder()
 */
@Value
@Builder(toBuilder = true)
public class AttendanceDomain {

    Long id;

    /**
     * The session ID for which attendance is being recorded
     */
    Long sessionId;

    /**
     * The enrollment ID for which attendance is being recorded.
     * Links the student to the specific subject group enrollment.
     */
    Long enrollmentId;

    /**
     * Attendance status (PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO)
     */
    @With
    AttendanceStatus status;

    /**
     * Timestamp when attendance was recorded
     */
    LocalDateTime recordedAt;

    /**
     * The user ID (teacher/admin) who recorded the attendance
     */
    Long recordedById;

    /**
     * Optional notes about the attendance
     * Can include:
     * - Reason for tardiness
     * - Justification documentation reference
     * - Teacher observations
     * - Medical certificate number (for justified absences)
     */
    @With
    String notes;

    /**
     * Minutes late (only applicable when status = TARDANZA)
     * Used for reporting and statistics
     */
    @With
    Integer minutesLate;

    /**
     * Timestamp when justification was provided (if status = JUSTIFICADO)
     */
    LocalDateTime justifiedAt;

    /**
     * The user ID who approved the justification (teacher/admin)
     */
    Long justifiedById;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates the attendance domain object
     */
    public void validate() {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID is required");
        }
        if (enrollmentId == null) {
            throw new IllegalArgumentException("Enrollment ID is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("Attendance status is required");
        }
        if (recordedAt == null) {
            throw new IllegalArgumentException("Recorded at timestamp is required");
        }
        if (recordedById == null) {
            throw new IllegalArgumentException("Recorded by user ID is required");
        }
        if (notes != null && notes.length() > 500) {
            throw new IllegalArgumentException("Notes must not exceed 500 characters");
        }
        if (minutesLate != null && minutesLate < 0) {
            throw new IllegalArgumentException("Minutes late cannot be negative");
        }

        // Validate that minutesLate is only set when status is TARDANZA
        if (minutesLate != null && minutesLate > 0 && status != AttendanceStatus.TARDANZA) {
            throw new IllegalStateException("Minutes late should only be set when status is TARDANZA");
        }

        // Validate that justification fields are only set when status is JUSTIFICADO
        if ((justifiedAt != null || justifiedById != null) && status != AttendanceStatus.JUSTIFICADO) {
            throw new IllegalStateException("Justification fields should only be set when status is JUSTIFICADO");
        }
    }

    // ==================== STATUS CHECK METHODS ====================

    /**
     * Checks if the student was present
     */
    public boolean isPresent() {
        return status == AttendanceStatus.PRESENTE;
    }

    /**
     * Checks if the student was absent
     */
    public boolean isAbsent() {
        return status == AttendanceStatus.AUSENTE;
    }

    /**
     * Checks if the student arrived late
     */
    public boolean wasLate() {
        return status == AttendanceStatus.TARDANZA;
    }

    /**
     * Checks if the absence was justified
     */
    public boolean isJustified() {
        return status == AttendanceStatus.JUSTIFICADO;
    }

    /**
     * Checks if this attendance counts as effective attendance
     * (for statistics calculation)
     */
    public boolean countsAsEffectiveAttendance() {
        return status.isEffectiveAttendance();
    }

    /**
     * Checks if this is an absence (justified or not)
     */
    public boolean isAnyAbsence() {
        return status.isAbsence();
    }

    /**
     * Checks if attendance can be modified
     * Typically, attendance can be modified within a reasonable timeframe
     */
    public boolean canBeModified() {
        // Can modify if recorded less than 7 days ago
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        return recordedAt.isAfter(cutoffDate);
    }

    // ==================== STATE TRANSITION METHODS ====================
    // These methods return new instances (immutable pattern)

    /**
     * Marks the attendance as present
     * Returns a new instance with updated state
     */
    public AttendanceDomain markAsPresent() {
        return this.toBuilder()
                .status(AttendanceStatus.PRESENTE)
                .minutesLate(null)
                .justifiedAt(null)
                .justifiedById(null)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Marks the attendance as absent
     * Returns a new instance with updated state
     */
    public AttendanceDomain markAsAbsent(String reason) {
        return this.toBuilder()
                .status(AttendanceStatus.AUSENTE)
                .notes(reason)
                .minutesLate(null)
                .justifiedAt(null)
                .justifiedById(null)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Marks the attendance as late
     * Returns a new instance with updated state
     */
    public AttendanceDomain markAsLate(int minutesLate, String notes) {
        if (minutesLate <= 0) {
            throw new IllegalArgumentException("Minutes late must be greater than 0");
        }
        return this.toBuilder()
                .status(AttendanceStatus.TARDANZA)
                .minutesLate(minutesLate)
                .notes(notes)
                .justifiedAt(null)
                .justifiedById(null)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Justifies an absence
     * Can only justify if current status is AUSENTE
     * Returns a new instance with updated state
     *
     * @param justifiedByUserId the user ID (teacher/admin) who approves the justification
     * @param justificationReason the reason for the justification
     */
    public AttendanceDomain justify(Long justifiedByUserId, String justificationReason) {
        if (!status.canBeJustified()) {
            throw new IllegalStateException(
                "Cannot justify attendance with status: " + status +
                ". Only AUSENTE can be justified."
            );
        }
        if (justifiedByUserId == null) {
            throw new IllegalArgumentException("Justified by user ID cannot be null");
        }
        return this.toBuilder()
                .status(AttendanceStatus.JUSTIFICADO)
                .justifiedAt(LocalDateTime.now())
                .justifiedById(justifiedByUserId)
                .notes(justificationReason)
                .minutesLate(null)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updates the status with validation
     * Returns a new instance with updated state
     */
    public AttendanceDomain updateStatus(AttendanceStatus newStatus, String newNotes) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        AttendanceDomainBuilder builder = this.toBuilder()
                .status(newStatus)
                .updatedAt(LocalDateTime.now());

        // Clear fields that don't apply to new status
        if (newStatus != AttendanceStatus.TARDANZA) {
            builder.minutesLate(null);
        }
        if (newStatus != AttendanceStatus.JUSTIFICADO) {
            builder.justifiedAt(null).justifiedById(null);
        }

        if (newNotes != null && !newNotes.trim().isEmpty()) {
            builder.notes(newNotes);
        }

        return builder.build();
    }

    /**
     * Updates the notes
     * Returns a new instance with updated notes
     */
    public AttendanceDomain updateNotes(String newNotes) {
        if (newNotes != null && newNotes.length() > 500) {
            throw new IllegalArgumentException("Notes must not exceed 500 characters");
        }
        return this.toBuilder()
                .notes(newNotes)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
