package com.acainfo.session.domain.model;

import com.acainfo.schedule.domain.model.Classroom;
import lombok.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Session domain entity (anemic model with Lombok).
 * Represents a scheduled class session for a subject group.
 *
 * <p>Session types and their field requirements:</p>
 * <ul>
 *   <li>REGULAR: Has scheduleId (derived from Schedule), groupId derived from schedule</li>
 *   <li>EXTRA: Has groupId (required), no scheduleId - additional sessions for groups</li>
 *   <li>SCHEDULING: Has subjectId only, no groupId - meetings to agree on schedules before group creation</li>
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
     * Reference to the subject. Always present for all session types.
     * For REGULAR/EXTRA: derived from the group's subject.
     * For SCHEDULING: the subject for which schedules are being discussed.
     */
    private Long subjectId;

    /**
     * Reference to the group. Nullable.
     * - REGULAR: derived from the schedule's groupId
     * - EXTRA: required (the group receiving the extra session)
     * - SCHEDULING: null (no group exists yet)
     */
    private Long groupId;

    /**
     * Reference to the schedule. Nullable.
     * Only present for REGULAR sessions (generated from a Schedule).
     * EXTRA and SCHEDULING sessions have no associated schedule.
     */
    private Long scheduleId;

    /**
     * Classroom where the session takes place.
     * - REGULAR: inherited from the referenced schedule
     * - EXTRA/SCHEDULING: manually set by administrator
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

    public boolean isSchedulingType() {
        return type == SessionType.SCHEDULING;
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
     * Check if this session belongs to a group.
     * SCHEDULING sessions don't belong to any group.
     */
    public boolean hasGroup() {
        return groupId != null;
    }

    /**
     * Check if this session was generated from a schedule.
     */
    public boolean hasSchedule() {
        return scheduleId != null;
    }
}
