package acainfo.back.session.domain.validation;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.session.domain.exception.SessionConflictException;
import acainfo.back.session.domain.model.Session;
import acainfo.back.session.domain.model.SessionMode;
import acainfo.back.session.infrastructure.adapters.out.SessionRepository;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for validating session business rules.
 * Ensures data integrity and business logic compliance.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SessionValidationService {

    private final SessionRepository sessionRepository;

    // ==================== MODE CHANGE VALIDATIONS ====================

    /**
     * Validates that a session mode can be changed.
     *
     * Business Rules:
     * 1. Mode cannot be changed less than 2 hours before session start
     * 2. PRESENCIAL/DUAL require a physical classroom
     * 3. ONLINE/DUAL require a Zoom meeting ID
     * 4. Session must not be in terminal state (COMPLETADA, POSPUESTA, CANCELADA)
     *
     * @param session the session to validate
     * @param newMode the new mode to change to
     * @param classroom the classroom (required for PRESENCIAL/DUAL)
     * @param zoomMeetingId the zoom meeting ID (required for ONLINE/DUAL)
     * @throws IllegalStateException if mode cannot be changed
     * @throws IllegalArgumentException if validation fails
     */
    public void validateModeChange(Session session, SessionMode newMode,
                                   Classroom classroom, String zoomMeetingId) {
        log.debug("Validating mode change for session {} from {} to {}",
            session.getId(), session.getMode(), newMode);

        // Rule 1: Check timing
        if (!canChangeModeInTime(session)) {
            throw new IllegalStateException(
                "Mode cannot be changed less than 2 hours before session start. " +
                "Scheduled start: " + session.getScheduledStart()
            );
        }

        // Rule 2: Check session state
        if (session.isTerminal()) {
            throw new IllegalStateException(
                "Cannot change mode of a session in terminal state: " + session.getStatus()
            );
        }

        // Rule 3: Validate classroom requirement
        if (newMode.requiresPhysicalClassroom() && classroom == null) {
            throw new IllegalArgumentException(
                "Classroom is required for mode: " + newMode
            );
        }

        // Rule 4: Validate Zoom requirement
        if (newMode.requiresZoom() && (zoomMeetingId == null || zoomMeetingId.isBlank())) {
            throw new IllegalArgumentException(
                "Zoom meeting ID is required for mode: " + newMode
            );
        }

        // Rule 5: If changing to physical classroom, validate availability
        if (newMode.requiresPhysicalClassroom() && classroom.isPhysical()) {
            validateClassroomAvailability(
                classroom,
                session.getScheduledStart(),
                session.getScheduledEnd(),
                session.getId()
            );
        }

        log.debug("Mode change validation passed for session {}", session.getId());
    }

    /**
     * Checks if mode can be changed based on timing.
     * Must be at least 2 hours before session start.
     */
    private boolean canChangeModeInTime(Session session) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursBeforeStart = session.getScheduledStart().minusHours(2);
        return now.isBefore(twoHoursBeforeStart);
    }

    // ==================== TIME SLOT VALIDATIONS ====================

    /**
     * Validates that a session time slot is valid.
     *
     * Business Rules:
     * 1. Start time must be before end time
     * 2. Duration must be at least 30 minutes
     * 3. Duration should not exceed 4 hours (warning)
     *
     * @param scheduledStart the start time
     * @param scheduledEnd the end time
     * @throws IllegalArgumentException if validation fails
     */
    public void validateSessionTimeSlot(LocalDateTime scheduledStart, LocalDateTime scheduledEnd) {
        if (scheduledStart == null || scheduledEnd == null) {
            throw new IllegalArgumentException("Scheduled start and end times are required");
        }

        if (!scheduledStart.isBefore(scheduledEnd)) {
            throw new IllegalArgumentException(
                "Scheduled end must be after scheduled start. Start: " + scheduledStart +
                ", End: " + scheduledEnd
            );
        }

        long durationMinutes = java.time.Duration.between(scheduledStart, scheduledEnd).toMinutes();

        if (durationMinutes < 30) {
            throw new IllegalArgumentException(
                "Session duration must be at least 30 minutes. Current: " + durationMinutes + " minutes"
            );
        }

        if (durationMinutes > 240) {
            log.warn("Session duration exceeds 4 hours ({} minutes). This may be unusual.", durationMinutes);
        }
    }

    // ==================== CLASSROOM CONFLICT VALIDATIONS ====================

    /**
     * Validates that a classroom is available for the given time slot.
     * Only physical classrooms need validation (VIRTUAL has unlimited capacity).
     *
     * @param classroom the classroom to validate
     * @param startTime the start time
     * @param endTime the end time
     * @param excludeSessionId session ID to exclude from conflict check (for updates)
     * @throws SessionConflictException if classroom is occupied
     */
    public void validateClassroomAvailability(Classroom classroom,
                                             LocalDateTime startTime,
                                             LocalDateTime endTime,
                                             Long excludeSessionId) {
        // Virtual classrooms have unlimited capacity
        if (classroom.isVirtual()) {
            log.debug("Skipping classroom validation for VIRTUAL classroom");
            return;
        }

        log.debug("Validating classroom {} availability from {} to {}",
            classroom, startTime, endTime);

        List<Session> conflictingSessions = sessionRepository.findConflictingClassroomSessions(
            classroom, startTime, endTime, excludeSessionId != null ? excludeSessionId : -1L
        );

        if (!conflictingSessions.isEmpty()) {
            Session conflict = conflictingSessions.get(0);
            throw SessionConflictException.classroomOccupied(
                classroom.getDisplayName(),
                String.format("%s to %s (conflict with session #%d)",
                    startTime, endTime, conflict.getId())
            );
        }

        log.debug("Classroom {} is available", classroom);
    }

    // ==================== TEACHER CONFLICT VALIDATIONS ====================

    /**
     * Validates that a teacher is available for the given time slot.
     *
     * @param subjectGroup the subject group (contains teacher)
     * @param startTime the start time
     * @param endTime the end time
     * @param excludeSessionId session ID to exclude from conflict check (for updates)
     * @throws SessionConflictException if teacher has another session
     */
    public void validateTeacherAvailability(SubjectGroup subjectGroup,
                                           LocalDateTime startTime,
                                           LocalDateTime endTime,
                                           Long excludeSessionId) {
        if (subjectGroup.getTeacher() == null) {
            log.warn("Subject group {} has no teacher assigned", subjectGroup.getId());
            return;
        }

        Long teacherId = subjectGroup.getTeacher().getId();

        log.debug("Validating teacher {} availability from {} to {}",
            teacherId, startTime, endTime);

        boolean hasConflict = sessionRepository.existsByTeacherAndDateRangeExcludingId(
            teacherId, startTime, endTime, excludeSessionId != null ? excludeSessionId : -1L
        );

        if (hasConflict) {
            String teacherName = subjectGroup.getTeacher().getFirstName() + " " +
                                subjectGroup.getTeacher().getLastName();
            throw SessionConflictException.teacherConflict(
                teacherName,
                String.format("%s to %s", startTime, endTime)
            );
        }

        log.debug("Teacher {} is available", teacherId);
    }

    // ==================== POSTPONEMENT VALIDATIONS ====================

    /**
     * Validates that a session can be postponed.
     *
     * Business Rules:
     * 1. Only PROGRAMADA sessions can be postponed
     * 2. Must be at least 2 hours before session start
     * 3. Reason is required
     *
     * @param session the session to postpone
     * @param reason the reason for postponement
     * @throws IllegalStateException if session cannot be postponed
     * @throws IllegalArgumentException if reason is invalid
     */
    public void validatePostponement(Session session, String reason) {
        if (!session.isScheduled()) {
            throw new IllegalStateException(
                "Only PROGRAMADA sessions can be postponed. Current status: " + session.getStatus()
            );
        }

        if (!canChangeModeInTime(session)) {
            throw new IllegalStateException(
                "Session cannot be postponed less than 2 hours before start. " +
                "Scheduled start: " + session.getScheduledStart()
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Postponement reason is required");
        }

        if (reason.length() < 10) {
            throw new IllegalArgumentException(
                "Postponement reason must be at least 10 characters. Current: " + reason.length()
            );
        }
    }

    // ==================== CANCELLATION VALIDATIONS ====================

    /**
     * Validates that a session can be cancelled.
     *
     * Business Rules:
     * 1. Only PROGRAMADA sessions can be cancelled
     * 2. Reason is required
     *
     * @param session the session to cancel
     * @param reason the reason for cancellation
     * @throws IllegalStateException if session cannot be cancelled
     * @throws IllegalArgumentException if reason is invalid
     */
    public void validateCancellation(Session session, String reason) {
        if (!session.isScheduled()) {
            throw new IllegalStateException(
                "Only PROGRAMADA sessions can be cancelled. Current status: " + session.getStatus()
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }

        if (reason.length() < 10) {
            throw new IllegalArgumentException(
                "Cancellation reason must be at least 10 characters. Current: " + reason.length()
            );
        }
    }

    // ==================== START SESSION VALIDATIONS ====================

    /**
     * Validates that a session can be started.
     *
     * Business Rules:
     * 1. Only PROGRAMADA sessions can be started
     * 2. Can only start within 30 minutes before scheduled start
     * 3. Cannot start more than 15 minutes after scheduled start (late start warning)
     *
     * @param session the session to start
     * @throws IllegalStateException if session cannot be started
     */
    public void validateSessionStart(Session session) {
        if (!session.isScheduled()) {
            throw new IllegalStateException(
                "Only PROGRAMADA sessions can be started. Current status: " + session.getStatus()
            );
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earliestStart = session.getScheduledStart().minusMinutes(30);
        LocalDateTime latestStart = session.getScheduledStart().plusMinutes(15);

        if (now.isBefore(earliestStart)) {
            throw new IllegalStateException(
                "Session can only be started 30 minutes before scheduled time. " +
                "Earliest start: " + earliestStart
            );
        }

        if (now.isAfter(latestStart)) {
            log.warn("Session {} is being started late (more than 15 minutes after scheduled start)",
                session.getId());
        }
    }

    // ==================== COMPLETION VALIDATIONS ====================

    /**
     * Validates that a session can be completed.
     *
     * Business Rules:
     * 1. Only EN_CURSO sessions can be completed
     * 2. Topics covered is required
     *
     * @param session the session to complete
     * @param topicsCovered the topics covered in the session
     * @throws IllegalStateException if session cannot be completed
     * @throws IllegalArgumentException if topics covered is invalid
     */
    public void validateSessionCompletion(Session session, String topicsCovered) {
        if (!session.isInProgress()) {
            throw new IllegalStateException(
                "Only EN_CURSO sessions can be completed. Current status: " + session.getStatus()
            );
        }

        if (topicsCovered == null || topicsCovered.isBlank()) {
            throw new IllegalArgumentException("Topics covered is required when completing a session");
        }

        if (topicsCovered.length() < 10) {
            throw new IllegalArgumentException(
                "Topics covered must be at least 10 characters. Current: " + topicsCovered.length()
            );
        }
    }

    // ==================== CREATION VALIDATIONS ====================

    /**
     * Validates all requirements for creating a new session.
     *
     * @param subjectGroup the subject group
     * @param scheduledStart the start time
     * @param scheduledEnd the end time
     * @param mode the session mode
     * @param classroom the classroom (nullable for ONLINE)
     * @param zoomMeetingId the zoom meeting ID (nullable for PRESENCIAL)
     * @throws IllegalArgumentException if validation fails
     * @throws SessionConflictException if there are scheduling conflicts
     */
    public void validateSessionCreation(SubjectGroup subjectGroup,
                                        LocalDateTime scheduledStart,
                                        LocalDateTime scheduledEnd,
                                        SessionMode mode,
                                        Classroom classroom,
                                        String zoomMeetingId) {
        log.debug("Validating session creation for group {}", subjectGroup.getId());

        // Validate time slot
        validateSessionTimeSlot(scheduledStart, scheduledEnd);

        // Validate mode requirements
        if (mode.requiresPhysicalClassroom() && classroom == null) {
            throw new IllegalArgumentException("Classroom is required for mode: " + mode);
        }

        if (mode.requiresZoom() && (zoomMeetingId == null || zoomMeetingId.isBlank())) {
            throw new IllegalArgumentException("Zoom meeting ID is required for mode: " + mode);
        }

        // Validate classroom availability (if physical)
        if (classroom != null && classroom.isPhysical()) {
            validateClassroomAvailability(classroom, scheduledStart, scheduledEnd, null);
        }

        // Validate teacher availability
        validateTeacherAvailability(subjectGroup, scheduledStart, scheduledEnd, null);

        log.debug("Session creation validation passed");
    }
}
