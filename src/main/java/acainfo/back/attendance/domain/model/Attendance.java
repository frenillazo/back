package acainfo.back.attendance.domain.model;

import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.session.domain.model.Session;
import acainfo.back.user.domain.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing student attendance for a session.
 *
 * This entity tracks whether a student attended, was absent, arrived late,
 * or had a justified absence for a specific session.
 *
 * Business Rules:
 * - One attendance record per enrollment per session
 * - Enrollment must be ACTIVO to register attendance
 * - Can only register attendance for COMPLETADA sessions
 * - Status can be updated (e.g., AUSENTE -> JUSTIFICADO)
 * - Teachers can only register attendance for their own groups
 * - Attendance affects access to materials (students with many absences may be restricted)
 */
@Entity
@Table(
    name = "attendance",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_attendance_session_enrollment",
            columnNames = {"session_id", "enrollment_id"}
        )
    },
    indexes = {
        @Index(name = "idx_attendance_session", columnList = "session_id"),
        @Index(name = "idx_attendance_enrollment", columnList = "enrollment_id"),
        @Index(name = "idx_attendance_status", columnList = "status"),
        @Index(name = "idx_attendance_recorded_at", columnList = "recorded_at")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The session for which attendance is being recorded
     */
    @NotNull(message = "Session is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    /**
     * The enrollment for which attendance is being recorded.
     * Links the student to the specific subject group enrollment.
     * This provides context about which enrollment the attendance belongs to,
     * especially important when a student has multiple enrollments.
     */
    @NotNull(message = "Enrollment is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    /**
     * Attendance status (PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO)
     */
    @NotNull(message = "Attendance status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.PRESENTE;

    /**
     * Timestamp when attendance was recorded
     */
    @NotNull(message = "Recorded at timestamp is required")
    @Column(name = "recorded_at", nullable = false)
    @Builder.Default
    private LocalDateTime recordedAt = LocalDateTime.now();

    /**
     * The user (teacher/admin) who recorded the attendance
     */
    @NotNull(message = "Recorded by user is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id", nullable = false)
    private User recordedBy;

    /**
     * Optional notes about the attendance
     * Can include:
     * - Reason for tardiness
     * - Justification documentation reference
     * - Teacher observations
     * - Medical certificate number (for justified absences)
     */
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Column(length = 500)
    private String notes;

    /**
     * Minutes late (only applicable when status = TARDANZA)
     * Used for reporting and statistics
     */
    @Min(value = 0, message = "Minutes late cannot be negative")
    @Column(name = "minutes_late")
    private Integer minutesLate;

    /**
     * Timestamp when justification was provided (if status = JUSTIFICADO)
     */
    @Column(name = "justified_at")
    private LocalDateTime justifiedAt;

    /**
     * The user who approved the justification (teacher/admin)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "justified_by_id")
    private User justifiedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== VALIDATION HELPER METHODS ====================

    /**
     * Validates that minutesLate is only set when status is TARDANZA
     */
    @AssertTrue(message = "Minutes late should only be set when status is TARDANZA")
    public boolean isMinutesLateValid() {
        if (minutesLate != null && minutesLate > 0) {
            return status == AttendanceStatus.TARDANZA;
        }
        return true;
    }

    /**
     * Validates that justification fields are only set when status is JUSTIFICADO
     */
    @AssertTrue(message = "Justification fields should only be set when status is JUSTIFICADO")
    public boolean isJustificationValid() {
        if (justifiedAt != null || justifiedBy != null) {
            return status == AttendanceStatus.JUSTIFICADO;
        }
        return true;
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

    // ==================== STATE TRANSITION METHODS ====================

    /**
     * Marks the attendance as present
     */
    public void markAsPresent() {
        this.status = AttendanceStatus.PRESENTE;
        this.minutesLate = null;
        this.justifiedAt = null;
        this.justifiedBy = null;
    }

    /**
     * Marks the attendance as absent
     */
    public void markAsAbsent(String reason) {
        this.status = AttendanceStatus.AUSENTE;
        this.notes = reason;
        this.minutesLate = null;
        this.justifiedAt = null;
        this.justifiedBy = null;
    }

    /**
     * Marks the attendance as late
     */
    public void markAsLate(int minutesLate, String notes) {
        if (minutesLate <= 0) {
            throw new IllegalArgumentException("Minutes late must be greater than 0");
        }
        this.status = AttendanceStatus.TARDANZA;
        this.minutesLate = minutesLate;
        this.notes = notes;
        this.justifiedAt = null;
        this.justifiedBy = null;
    }

    /**
     * Justifies an absence
     * Can only justify if current status is AUSENTE
     *
     * @param justifiedByUser the user (teacher/admin) who approves the justification
     * @param justificationReason the reason for the justification
     */
    public void justify(User justifiedByUser, String justificationReason) {
        if (!status.canBeJustified()) {
            throw new IllegalStateException(
                "Cannot justify attendance with status: " + status +
                ". Only AUSENTE can be justified."
            );
        }
        if (justifiedByUser == null) {
            throw new IllegalArgumentException("Justified by user cannot be null");
        }
        this.status = AttendanceStatus.JUSTIFICADO;
        this.justifiedAt = LocalDateTime.now();
        this.justifiedBy = justifiedByUser;
        this.notes = justificationReason;
        this.minutesLate = null;
    }

    /**
     * Updates the status with validation
     */
    public void updateStatus(AttendanceStatus newStatus, String notes) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        this.status = newStatus;

        // Clear fields that don't apply to new status
        if (newStatus != AttendanceStatus.TARDANZA) {
            this.minutesLate = null;
        }
        if (newStatus != AttendanceStatus.JUSTIFICADO) {
            this.justifiedAt = null;
            this.justifiedBy = null;
        }

        if (notes != null && !notes.trim().isEmpty()) {
            this.notes = notes;
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Gets a human-readable description of the attendance
     */
    public String getDescription() {
        StringBuilder desc = new StringBuilder();

        if (enrollment != null && enrollment.getStudent() != null) {
            desc.append("Student: ")
                .append(enrollment.getStudent().getFirstName())
                .append(" ")
                .append(enrollment.getStudent().getLastName());
        } else {
            desc.append("Enrollment ID: ").append(enrollment != null ? enrollment.getId() : "?");
        }

        desc.append(" - Session ").append(session != null ? session.getId() : "?")
            .append(" - Status: ").append(status);

        if (minutesLate != null && minutesLate > 0) {
            desc.append(" (").append(minutesLate).append(" min late)");
        }

        return desc.toString();
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

    // ==================== EQUALITY AND HASH CODE ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attendance)) return false;
        Attendance that = (Attendance) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", sessionId=" + (session != null ? session.getId() : "null") +
                ", enrollmentId=" + (enrollment != null ? enrollment.getId() : "null") +
                ", status=" + status +
                ", recordedAt=" + recordedAt +
                ", minutesLate=" + minutesLate +
                '}';
    }
}
