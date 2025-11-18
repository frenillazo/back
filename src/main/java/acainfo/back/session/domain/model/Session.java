package acainfo.back.session.domain.model;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.domain.model.Schedule;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a session (class instance) in the training center.
 * A session is a specific occurrence of a class for a subject group.
 *
 * Sessions can be:
 * - REGULAR: Part of the normal weekly schedule
 * - RECUPERACION: Make-up session for a postponed/cancelled session
 * - EXTRA: Additional session (review, group tutoring, etc.)
 *
 * Sessions can be held in different modes:
 * - PRESENCIAL: Fully in-person (requires physical classroom)
 * - DUAL: Hybrid (in-person + online simultaneously, requires classroom + Zoom)
 * - ONLINE: Fully online (requires Zoom meeting ID only)
 */
@Entity
@Table(
    name = "sessions",
    indexes = {
        @Index(name = "idx_session_group", columnList = "subject_group_id"),
        @Index(name = "idx_session_schedule", columnList = "generated_from_schedule_id"),
        @Index(name = "idx_session_status", columnList = "status"),
        @Index(name = "idx_session_mode", columnList = "mode"),
        @Index(name = "idx_session_type", columnList = "type"),
        @Index(name = "idx_session_scheduled_start", columnList = "scheduled_start"),
        @Index(name = "idx_session_classroom", columnList = "classroom"),
        @Index(name = "idx_session_recovery_for", columnList = "recovery_for_session_id"),
        @Index(name = "idx_session_original", columnList = "original_session_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The subject group this session belongs to
     */
    @NotNull(message = "Subject group is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_group_id", nullable = false)
    private SubjectGroup subjectGroup;

    /**
     * The Schedule (weekly recurrent pattern) that generated this session.
     *
     * RELATIONSHIP SEMANTICS:
     * - NULL for RECUPERACION sessions (manually created to recover cancelled/postponed sessions)
     * - NULL for EXTRA sessions (manually created for reviews, group tutoring, etc.)
     * - NOT NULL for REGULAR sessions (automatically generated from weekly schedule)
     *
     * USAGE:
     * - Traceability: "This session was generated from the Monday 10-12 schedule"
     * - Bulk operations: "Regenerate all sessions from this schedule if it changes"
     * - Reporting: "Show me all sessions generated from schedule X"
     *
     * IMPORTANT: This is a LOGICAL relationship, not a strict FK constraint.
     * A Session can exist without a Schedule (manual sessions), and a Schedule
     * can exist without Sessions (before semester starts).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_from_schedule_id")
    private Schedule generatedFromSchedule;

    /**
     * Type of session (REGULAR, RECUPERACION, EXTRA)
     */
    @NotNull(message = "Session type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SessionType type = SessionType.REGULAR;

    /**
     * Scheduled start date and time
     */
    @NotNull(message = "Scheduled start is required")
    @Column(name = "scheduled_start", nullable = false)
    private LocalDateTime scheduledStart;

    /**
     * Scheduled end date and time
     */
    @NotNull(message = "Scheduled end is required")
    @Column(name = "scheduled_end", nullable = false)
    private LocalDateTime scheduledEnd;

    /**
     * Actual start time (when teacher started the session)
     * Null until session is started
     */
    @Column(name = "actual_start")
    private LocalDateTime actualStart;

    /**
     * Actual end time (when teacher completed the session)
     * Null until session is completed
     */
    @Column(name = "actual_end")
    private LocalDateTime actualEnd;

    /**
     * Mode of delivery (PRESENCIAL, DUAL, ONLINE)
     */
    @NotNull(message = "Session mode is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SessionMode mode = SessionMode.PRESENCIAL;

    /**
     * Current status (PROGRAMADA, EN_CURSO, COMPLETADA, POSPUESTA, CANCELADA)
     */
    @NotNull(message = "Session status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.PROGRAMADA;

    /**
     * Physical or virtual classroom assignment
     * Required for PRESENCIAL and DUAL modes
     * Optional for ONLINE mode (typically VIRTUAL)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Classroom classroom;

    /**
     * Zoom meeting ID for online/dual sessions
     * Required for ONLINE and DUAL modes
     * Format: typically a 9-11 digit number or custom URL
     */
    @Size(max = 100, message = "Zoom meeting ID must not exceed 100 characters")
    @Column(name = "zoom_meeting_id", length = 100)
    private String zoomMeetingId;

    /**
     * Reason for cancellation (if status = CANCELADA)
     */
    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    /**
     * Reason for postponement (if status = POSPUESTA)
     */
    @Size(max = 500, message = "Postponement reason must not exceed 500 characters")
    @Column(name = "postponement_reason", length = 500)
    private String postponementReason;

    /**
     * Reference to the original session if this is a postponed session
     * When a session is postponed, the original session gets POSPUESTA status
     * and a new session is created with this field pointing to the original
     */
    @Column(name = "original_session_id")
    private Long originalSessionId;

    /**
     * Reference to the session this one is recovering for
     * When creating a RECUPERACION session, this field points to the
     * POSPUESTA or CANCELADA session being recovered
     */
    @Column(name = "recovery_for_session_id")
    private Long recoveryForSessionId;

    /**
     * Teacher's notes about the session
     * Can include topics covered, observations, etc.
     */
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(length = 1000)
    private String notes;

    /**
     * Topics or content covered in this session
     * Filled when completing the session
     */
    @Size(max = 1000, message = "Topics covered must not exceed 1000 characters")
    @Column(name = "topics_covered", length = 1000)
    private String topicsCovered;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== VALIDATION HELPER METHODS ====================

    /**
     * Validates that scheduled end is after scheduled start
     */
    @AssertTrue(message = "Scheduled end must be after scheduled start")
    public boolean isScheduledTimeValid() {
        if (scheduledStart == null || scheduledEnd == null) {
            return true; // Let @NotNull handle null validation
        }
        return scheduledEnd.isAfter(scheduledStart);
    }

    /**
     * Validates that PRESENCIAL or DUAL sessions have a classroom assigned
     */
    @AssertTrue(message = "PRESENCIAL and DUAL sessions must have a classroom assigned")
    public boolean isClassroomValid() {
        if (mode == null) {
            return true; // Let @NotNull handle null validation
        }
        if (mode == SessionMode.PRESENCIAL || mode == SessionMode.DUAL) {
            return classroom != null;
        }
        return true;
    }

    /**
     * Validates that ONLINE or DUAL sessions have a Zoom meeting ID
     */
    @AssertTrue(message = "ONLINE and DUAL sessions must have a Zoom meeting ID")
    public boolean isZoomMeetingIdValid() {
        if (mode == null) {
            return true; // Let @NotNull handle null validation
        }
        if (mode == SessionMode.ONLINE || mode == SessionMode.DUAL) {
            return zoomMeetingId != null && !zoomMeetingId.trim().isEmpty();
        }
        return true;
    }

    // ==================== STATUS CHECK METHODS ====================

    /**
     * Checks if the session is scheduled (not yet started)
     */
    public boolean isScheduled() {
        return status == SessionStatus.PROGRAMADA;
    }

    /**
     * Checks if the session is currently in progress
     */
    public boolean isInProgress() {
        return status == SessionStatus.EN_CURSO;
    }

    /**
     * Checks if the session is completed
     */
    public boolean isCompleted() {
        return status == SessionStatus.COMPLETADA;
    }

    /**
     * Checks if the session is postponed
     */
    public boolean isPostponed() {
        return status == SessionStatus.POSPUESTA;
    }

    /**
     * Checks if the session is cancelled
     */
    public boolean isCancelled() {
        return status == SessionStatus.CANCELADA;
    }

    /**
     * Checks if the session is in a terminal state (cannot be modified)
     */
    public boolean isTerminal() {
        return status == SessionStatus.COMPLETADA ||
               status == SessionStatus.POSPUESTA ||
               status == SessionStatus.CANCELADA;
    }

    /**
     * Checks if the session can be started
     */
    public boolean canBeStarted() {
        return status == SessionStatus.PROGRAMADA &&
               LocalDateTime.now().isAfter(scheduledStart.minusMinutes(30));
    }

    /**
     * Checks if the session can be completed
     */
    public boolean canBeCompleted() {
        return status == SessionStatus.EN_CURSO;
    }

    /**
     * Checks if the session can be postponed
     */
    public boolean canBePostponed() {
        return status == SessionStatus.PROGRAMADA &&
               LocalDateTime.now().isBefore(scheduledStart.minusHours(2));
    }

    /**
     * Checks if the session can be cancelled
     */
    public boolean canBeCancelled() {
        return status == SessionStatus.PROGRAMADA;
    }

    /**
     * Checks if mode can be changed
     */
    public boolean canChangeModeChangeBe() {
        return (status == SessionStatus.PROGRAMADA || status == SessionStatus.EN_CURSO) &&
               LocalDateTime.now().isBefore(scheduledStart.minusHours(2));
    }

    // ==================== MODE CHECK METHODS ====================

    /**
     * Checks if the session is fully in-person
     */
    public boolean isPresencial() {
        return mode == SessionMode.PRESENCIAL;
    }

    /**
     * Checks if the session is hybrid
     */
    public boolean isDual() {
        return mode == SessionMode.DUAL;
    }

    /**
     * Checks if the session is fully online
     */
    public boolean isOnline() {
        return mode == SessionMode.ONLINE;
    }

    /**
     * Checks if the session requires a physical classroom
     */
    public boolean requiresPhysicalClassroom() {
        return mode == SessionMode.PRESENCIAL || mode == SessionMode.DUAL;
    }

    /**
     * Checks if the session requires Zoom
     */
    public boolean requiresZoom() {
        return mode == SessionMode.ONLINE || mode == SessionMode.DUAL;
    }

    // ==================== TYPE CHECK METHODS ====================

    /**
     * Checks if this is a regular session
     */
    public boolean isRegular() {
        return type == SessionType.REGULAR;
    }

    /**
     * Checks if this is a recovery session
     */
    public boolean isRecovery() {
        return type == SessionType.RECUPERACION;
    }

    /**
     * Checks if this is an extra session
     */
    public boolean isExtra() {
        return type == SessionType.EXTRA;
    }

    /**
     * Checks if this session is recovering another session
     */
    public boolean isRecoveringAnotherSession() {
        return recoveryForSessionId != null;
    }

    /**
     * Checks if this session has been rescheduled
     */
    public boolean hasBeenRescheduled() {
        return originalSessionId != null;
    }

    // ==================== SCHEDULE RELATIONSHIP METHODS ====================

    /**
     * Checks if this session was generated from a Schedule
     */
    public boolean wasGeneratedFromSchedule() {
        return generatedFromSchedule != null;
    }

    /**
     * Checks if this is a manually created session (not generated from schedule)
     */
    public boolean isManuallyCreated() {
        return generatedFromSchedule == null;
    }

    /**
     * Checks if this session should be linked to a schedule
     * (typically REGULAR sessions should be linked)
     */
    public boolean shouldBeLinkedToSchedule() {
        return type == SessionType.REGULAR;
    }

    /**
     * Gets the schedule that generated this session (if any)
     */
    public Schedule getOriginSchedule() {
        return generatedFromSchedule;
    }

    // ==================== TIME UTILITY METHODS ====================

    /**
     * Gets the duration of the session in minutes (scheduled)
     */
    public long getScheduledDurationInMinutes() {
        if (scheduledStart == null || scheduledEnd == null) {
            return 0;
        }
        return java.time.Duration.between(scheduledStart, scheduledEnd).toMinutes();
    }

    /**
     * Gets the actual duration of the session in minutes
     */
    public long getActualDurationInMinutes() {
        if (actualStart == null || actualEnd == null) {
            return 0;
        }
        return java.time.Duration.between(actualStart, actualEnd).toMinutes();
    }

    /**
     * Checks if the session is happening now
     */
    public boolean isHappeningNow() {
        LocalDateTime now = LocalDateTime.now();
        return scheduledStart.isBefore(now) && scheduledEnd.isAfter(now);
    }

    /**
     * Checks if the session is in the future
     */
    public boolean isInFuture() {
        return scheduledStart.isAfter(LocalDateTime.now());
    }

    /**
     * Checks if the session is in the past
     */
    public boolean isInPast() {
        return scheduledEnd.isBefore(LocalDateTime.now());
    }

    // ==================== STATE TRANSITION METHODS ====================

    /**
     * Starts the session
     */
    public void start() {
        if (!canBeStarted()) {
            throw new IllegalStateException("Session cannot be started in current state: " + status);
        }
        this.status = SessionStatus.EN_CURSO;
        this.actualStart = LocalDateTime.now();
    }

    /**
     * Completes the session
     */
    public void complete(String topicsCovered) {
        if (!canBeCompleted()) {
            throw new IllegalStateException("Session cannot be completed in current state: " + status);
        }
        this.status = SessionStatus.COMPLETADA;
        this.actualEnd = LocalDateTime.now();
        this.topicsCovered = topicsCovered;
    }

    /**
     * Postpones the session
     */
    public void postpone(String reason) {
        if (!canBePostponed()) {
            throw new IllegalStateException("Session cannot be postponed in current state: " + status);
        }
        this.status = SessionStatus.POSPUESTA;
        this.postponementReason = reason;
    }

    /**
     * Cancels the session
     */
    public void cancel(String reason) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Session cannot be cancelled in current state: " + status);
        }
        this.status = SessionStatus.CANCELADA;
        this.cancellationReason = reason;
    }

    /**
     * Changes the mode of the session
     */
    public void changeMode(SessionMode newMode, String zoomMeetingId, Classroom classroom) {
        if (!canChangeModeChangeBe()) {
            throw new IllegalStateException("Session mode cannot be changed in current state or timing");
        }
        this.mode = newMode;

        // Update classroom and zoom based on new mode
        if (newMode.requiresPhysicalClassroom()) {
            this.classroom = classroom;
        } else {
            this.classroom = Classroom.VIRTUAL;
        }

        if (newMode.requiresZoom()) {
            this.zoomMeetingId = zoomMeetingId;
        }
    }

    // ==================== EQUALITY AND HASH CODE ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session)) return false;
        Session session = (Session) o;
        return id != null && id.equals(session.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", subjectGroup=" + (subjectGroup != null ? subjectGroup.getId() : "null") +
                ", type=" + type +
                ", scheduledStart=" + scheduledStart +
                ", scheduledEnd=" + scheduledEnd +
                ", mode=" + mode +
                ", status=" + status +
                ", classroom=" + classroom +
                '}';
    }
}
