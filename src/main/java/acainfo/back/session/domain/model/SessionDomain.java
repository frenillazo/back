package acainfo.back.session.domain.model;

import acainfo.back.schedule.domain.model.Classroom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Pure Domain Model for Session
 *
 * Represents a session (class instance) in the training center.
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
 *
 * Business Rules:
 * - Scheduled end must be after scheduled start
 * - PRESENCIAL and DUAL sessions must have a classroom
 * - ONLINE and DUAL sessions must have a Zoom meeting ID
 * - Sessions can only transition through specific states
 * - Mode changes require 2+ hours notice before start
 */
@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class SessionDomain {

    private final Long id;

    /**
     * ID of the subject group this session belongs to
     */
    private final Long subjectGroupId;

    /**
     * ID of the Schedule (weekly recurrent pattern) that generated this session
     *
     * NULL for RECUPERACION and EXTRA sessions (manually created)
     * NOT NULL for REGULAR sessions (automatically generated from weekly schedule)
     */
    private final Long generatedFromScheduleId;

    /**
     * Type of session (REGULAR, RECUPERACION, EXTRA)
     */
    private final SessionType type;

    /**
     * Scheduled start date and time
     */
    private final LocalDateTime scheduledStart;

    /**
     * Scheduled end date and time
     */
    private final LocalDateTime scheduledEnd;

    /**
     * Actual start time (when teacher started the session)
     * Null until session is started
     */
    private final LocalDateTime actualStart;

    /**
     * Actual end time (when teacher completed the session)
     * Null until session is completed
     */
    private final LocalDateTime actualEnd;

    /**
     * Mode of delivery (PRESENCIAL, DUAL, ONLINE)
     */
    private final SessionMode mode;

    /**
     * Current status (PROGRAMADA, EN_CURSO, COMPLETADA, POSPUESTA, CANCELADA)
     */
    private final SessionStatus status;

    /**
     * Physical or virtual classroom assignment
     * Required for PRESENCIAL and DUAL modes
     */
    private final Classroom classroom;

    /**
     * Zoom meeting ID for online/dual sessions
     * Required for ONLINE and DUAL modes
     */
    private final String zoomMeetingId;

    /**
     * Reason for cancellation (if status = CANCELADA)
     */
    private final String cancellationReason;

    /**
     * Reason for postponement (if status = POSPUESTA)
     */
    private final String postponementReason;

    /**
     * Reference to the original session if this is a postponed session
     */
    private final Long originalSessionId;

    /**
     * Reference to the session this one is recovering for
     */
    private final Long recoveryForSessionId;

    /**
     * Teacher's notes about the session
     */
    private final String notes;

    /**
     * Topics or content covered in this session
     */
    private final String topicsCovered;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates the session data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (subjectGroupId == null) {
            throw new IllegalArgumentException("Subject group ID is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Session type is required");
        }
        if (scheduledStart == null) {
            throw new IllegalArgumentException("Scheduled start is required");
        }
        if (scheduledEnd == null) {
            throw new IllegalArgumentException("Scheduled end is required");
        }
        if (mode == null) {
            throw new IllegalArgumentException("Session mode is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("Session status is required");
        }

        validateScheduledTime();
        validateClassroom();
        validateZoomMeetingId();
    }

    /**
     * Validates that scheduled end is after scheduled start
     */
    private void validateScheduledTime() {
        if (scheduledStart != null && scheduledEnd != null) {
            if (!scheduledEnd.isAfter(scheduledStart)) {
                throw new IllegalArgumentException("Scheduled end must be after scheduled start");
            }
        }
    }

    /**
     * Validates that PRESENCIAL or DUAL sessions have a classroom assigned
     */
    private void validateClassroom() {
        if (mode != null && (mode == SessionMode.PRESENCIAL || mode == SessionMode.DUAL)) {
            if (classroom == null) {
                throw new IllegalArgumentException("PRESENCIAL and DUAL sessions must have a classroom assigned");
            }
        }
    }

    /**
     * Validates that ONLINE or DUAL sessions have a Zoom meeting ID
     */
    private void validateZoomMeetingId() {
        if (mode != null && (mode == SessionMode.ONLINE || mode == SessionMode.DUAL)) {
            if (zoomMeetingId == null || zoomMeetingId.trim().isEmpty()) {
                throw new IllegalArgumentException("ONLINE and DUAL sessions must have a Zoom meeting ID");
            }
        }
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
    public boolean canChangeMode() {
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
        return generatedFromScheduleId != null;
    }

    /**
     * Checks if this is a manually created session (not generated from schedule)
     */
    public boolean isManuallyCreated() {
        return generatedFromScheduleId == null;
    }

    /**
     * Checks if this session should be linked to a schedule
     * (typically REGULAR sessions should be linked)
     */
    public boolean shouldBeLinkedToSchedule() {
        return type == SessionType.REGULAR;
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
     * Returns a new SessionDomain with updated status and actualStart
     */
    public SessionDomain start() {
        if (!canBeStarted()) {
            throw new IllegalStateException("Session cannot be started in current state: " + status);
        }

        return this.toBuilder()
                .status(SessionStatus.EN_CURSO)
                .actualStart(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Completes the session
     * Returns a new SessionDomain with updated status, actualEnd, and topicsCovered
     */
    public SessionDomain complete(String topicsCovered) {
        if (!canBeCompleted()) {
            throw new IllegalStateException("Session cannot be completed in current state: " + status);
        }

        return this.toBuilder()
                .status(SessionStatus.COMPLETADA)
                .actualEnd(LocalDateTime.now())
                .topicsCovered(topicsCovered)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Postpones the session
     * Returns a new SessionDomain with updated status and postponementReason
     */
    public SessionDomain postpone(String reason) {
        if (!canBePostponed()) {
            throw new IllegalStateException("Session cannot be postponed in current state: " + status);
        }

        return this.toBuilder()
                .status(SessionStatus.POSPUESTA)
                .postponementReason(reason)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Cancels the session
     * Returns a new SessionDomain with updated status and cancellationReason
     */
    public SessionDomain cancel(String reason) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Session cannot be cancelled in current state: " + status);
        }

        return this.toBuilder()
                .status(SessionStatus.CANCELADA)
                .cancellationReason(reason)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Changes the mode of the session
     * Returns a new SessionDomain with updated mode, classroom, and zoomMeetingId
     */
    public SessionDomain changeMode(SessionMode newMode, String zoomMeetingId, Classroom classroom) {
        if (!canChangeMode()) {
            throw new IllegalStateException("Session mode cannot be changed in current state or timing");
        }

        Classroom finalClassroom = newMode.requiresPhysicalClassroom() ? classroom : Classroom.VIRTUAL;
        String finalZoomId = newMode.requiresZoom() ? zoomMeetingId : this.zoomMeetingId;

        return this.toBuilder()
                .mode(newMode)
                .classroom(finalClassroom)
                .zoomMeetingId(finalZoomId)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updates session notes
     * Returns a new SessionDomain with updated notes
     */
    public SessionDomain updateNotes(String notes) {
        if (notes != null && notes.length() > 1000) {
            throw new IllegalArgumentException("Notes must not exceed 1000 characters");
        }

        return this.toBuilder()
                .notes(notes)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Reschedules the session to a new time
     * Returns a new SessionDomain with updated scheduled times
     */
    public SessionDomain reschedule(LocalDateTime newStart, LocalDateTime newEnd) {
        if (isTerminal()) {
            throw new IllegalStateException("Cannot reschedule a session in terminal state: " + status);
        }
        if (newEnd == null || newStart == null) {
            throw new IllegalArgumentException("New scheduled start and end are required");
        }
        if (!newEnd.isAfter(newStart)) {
            throw new IllegalArgumentException("Scheduled end must be after scheduled start");
        }

        return this.toBuilder()
                .scheduledStart(newStart)
                .scheduledEnd(newEnd)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public String toString() {
        return "SessionDomain{" +
                "id=" + id +
                ", subjectGroupId=" + subjectGroupId +
                ", type=" + type +
                ", scheduledStart=" + scheduledStart +
                ", scheduledEnd=" + scheduledEnd +
                ", mode=" + mode +
                ", status=" + status +
                ", classroom=" + classroom +
                '}';
    }
}
