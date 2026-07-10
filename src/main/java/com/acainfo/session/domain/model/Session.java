package com.acainfo.session.domain.model;

import com.acainfo.schedule.domain.model.Classroom;
import lombok.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Session domain entity (anemic model with Lombok).
 * Represents a scheduled class session of a course.
 *
 * <p>Session types and their field requirements:</p>
 * <ul>
 *   <li>REGULAR: Has scheduleId (derived from Schedule), courseId derived from schedule</li>
 *   <li>EXTRA: Has courseId (required), no scheduleId - additional ad-hoc sessions</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class Session {

    private Long id;

    /**
     * Reference to the subject. Always present (derived from the course's subject).
     */
    private Long subjectId;

    /**
     * Reference to the course. Always present.
     */
    private Long courseId;

    /**
     * Reference to the schedule. Nullable.
     * Only present for REGULAR sessions (generated from a Schedule).
     * EXTRA sessions have no associated schedule.
     */
    private Long scheduleId;

    /**
     * Classroom where the session takes place.
     * - REGULAR: inherited from the referenced schedule
     * - EXTRA: manually set by administrator
     */
    private Classroom classroom;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private SessionStatus status;
    private SessionType type;
    private SessionMode mode;

    /**
     * New date when a session is postponed.
     * Only set when status is POSTPONED.
     */
    private LocalDate postponedToDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Status Query Methods ====================

    public boolean isScheduled() {
        return status == SessionStatus.SCHEDULED;
    }

    public boolean isInProgress() {
        return status == SessionStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return status == SessionStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return status == SessionStatus.CANCELLED;
    }

    public boolean isPostponed() {
        return status == SessionStatus.POSTPONED;
    }

    // ==================== Type Query Methods ====================

    public boolean isRegular() {
        return type == SessionType.REGULAR;
    }

    public boolean isExtra() {
        return type == SessionType.EXTRA;
    }

    // ==================== Mode Query Methods ====================

    public boolean isInPerson() {
        return mode == SessionMode.IN_PERSON;
    }

    public boolean isOnline() {
        return mode == SessionMode.ONLINE;
    }

    public boolean isDual() {
        return mode == SessionMode.DUAL;
    }

    // ==================== Computed Properties ====================

    public long getDurationMinutes() {
        return Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Check if this session was generated from a schedule.
     */
    public boolean hasSchedule() {
        return scheduleId != null;
    }
}
