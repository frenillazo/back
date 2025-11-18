package acainfo.back.session.application.services;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.domain.model.Schedule;
import acainfo.back.schedule.infrastructure.adapters.out.ScheduleRepository;
import acainfo.back.session.application.ports.in.*;
import acainfo.back.session.domain.exception.SessionNotFoundException;
import acainfo.back.session.domain.model.Session;
import acainfo.back.session.domain.model.SessionMode;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.session.domain.model.SessionType;
import acainfo.back.session.domain.validation.SessionValidationService;
import acainfo.back.session.infrastructure.adapters.out.SessionRepository;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import acainfo.back.subjectgroup.infrastructure.adapters.out.SubjectGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementing all session use cases.
 * Handles session lifecycle: creation, start, completion, mode changes, postponement, cancellation.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SessionService implements
    CreateSessionUseCase,
    StartSessionUseCase,
    CompleteSessionUseCase,
    UpdateSessionModeUseCase,
    PostponeSessionUseCase,
    CancelSessionUseCase {

    private final SessionRepository sessionRepository;
    private final SubjectGroupRepository subjectGroupRepository;
    private final ScheduleRepository scheduleRepository;
    private final SessionValidationService validationService;

    // ==================== CREATE SESSION ====================

    @Override
    public Session createSession(CreateSessionCommand command) {
        log.info("Creating new session for group {}", command.subjectGroupId());

        // 1. Fetch and validate subject group
        SubjectGroup subjectGroup = subjectGroupRepository.findById(command.subjectGroupId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Subject group not found with ID: " + command.subjectGroupId()
            ));

        // 2. Parse enums
        SessionType type = SessionType.valueOf(command.sessionType());
        SessionMode mode = SessionMode.valueOf(command.mode());
        Classroom classroom = command.classroom() != null ?
            Classroom.valueOf(command.classroom()) : null;

        // 3. Parse dates
        LocalDateTime scheduledStart = LocalDateTime.parse(command.scheduledStart());
        LocalDateTime scheduledEnd = LocalDateTime.parse(command.scheduledEnd());

        // 4. Validate session creation
        validationService.validateSessionCreation(
            subjectGroup,
            scheduledStart,
            scheduledEnd,
            mode,
            classroom,
            command.zoomMeetingId()
        );

        // 5. Fetch schedule if provided
        Schedule generatedFromSchedule = null;
        if (command.generatedFromScheduleId() != null) {
            generatedFromSchedule = scheduleRepository.findById(command.generatedFromScheduleId())
                .orElse(null);
        }

        // 6. Build session
        Session session = Session.builder()
            .subjectGroup(subjectGroup)
            .generatedFromSchedule(generatedFromSchedule)
            .type(type)
            .scheduledStart(scheduledStart)
            .scheduledEnd(scheduledEnd)
            .mode(mode)
            .status(SessionStatus.PROGRAMADA)
            .classroom(classroom)
            .zoomMeetingId(command.zoomMeetingId())
            .notes(command.notes())
            .originalSessionId(command.originalSessionId())
            .recoveryForSessionId(command.recoveryForSessionId())
            .build();

        // 7. Save
        Session savedSession = sessionRepository.save(session);

        log.info("Session created successfully with ID: {}", savedSession.getId());
        return savedSession;
    }

    // ==================== START SESSION ====================

    @Override
    public Session startSession(StartSessionCommand command) {
        log.info("Starting session {}", command.sessionId());

        // 1. Fetch session
        Session session = findSessionById(command.sessionId());

        // 2. Validate
        validationService.validateSessionStart(session);

        // 3. Start session
        session.start();

        // 4. Add notes if provided
        if (command.notes() != null && !command.notes().isBlank()) {
            session.setNotes(command.notes());
        }

        // 5. Save
        Session savedSession = sessionRepository.save(session);

        log.info("Session {} started successfully at {}", savedSession.getId(), savedSession.getActualStart());
        return savedSession;
    }

    // ==================== COMPLETE SESSION ====================

    @Override
    public Session completeSession(CompleteSessionCommand command) {
        log.info("Completing session {}", command.sessionId());

        // 1. Fetch session
        Session session = findSessionById(command.sessionId());

        // 2. Validate
        validationService.validateSessionCompletion(session, command.topicsCovered());

        // 3. Complete session
        session.complete(command.topicsCovered());

        // 4. Add notes if provided
        if (command.notes() != null && !command.notes().isBlank()) {
            String existingNotes = session.getNotes() != null ? session.getNotes() + "\n" : "";
            session.setNotes(existingNotes + command.notes());
        }

        // 5. Save
        Session savedSession = sessionRepository.save(session);

        log.info("Session {} completed successfully at {}", savedSession.getId(), savedSession.getActualEnd());
        return savedSession;
    }

    // ==================== UPDATE SESSION MODE ====================

    @Override
    public Session updateSessionMode(UpdateSessionModeCommand command) {
        log.info("Updating mode for session {} to {}", command.sessionId(), command.newMode());

        // 1. Fetch session
        Session session = findSessionById(command.sessionId());

        // 2. Parse new mode and classroom
        SessionMode newMode = SessionMode.valueOf(command.newMode());
        Classroom classroom = command.classroom() != null ?
            Classroom.valueOf(command.classroom()) : null;

        // 3. Validate mode change
        validationService.validateModeChange(session, newMode, classroom, command.zoomMeetingId());

        // 4. Store old mode for logging
        SessionMode oldMode = session.getMode();

        // 5. Change mode
        session.changeMode(newMode, command.zoomMeetingId(), classroom);

        // 6. Add reason to notes if provided
        if (command.reason() != null && !command.reason().isBlank()) {
            String changeNote = String.format(
                "[Mode Change] %s → %s: %s",
                oldMode, newMode, command.reason()
            );
            String existingNotes = session.getNotes() != null ? session.getNotes() + "\n" : "";
            session.setNotes(existingNotes + changeNote);
        }

        // 7. Save
        Session savedSession = sessionRepository.save(session);

        log.info("Session {} mode changed from {} to {}", savedSession.getId(), oldMode, newMode);
        return savedSession;
    }

    // ==================== POSTPONE SESSION ====================

    @Override
    public Session postponeSession(PostponeSessionCommand command) {
        log.info("Postponing session {}", command.sessionId());

        // 1. Fetch session
        Session session = findSessionById(command.sessionId());

        // 2. Validate
        validationService.validatePostponement(session, command.reason());

        // 3. Postpone session
        session.postpone(command.reason());

        // 4. Save
        Session savedSession = sessionRepository.save(session);

        log.info("Session {} postponed. Reason: {}", savedSession.getId(), command.reason());
        return savedSession;
    }

    @Override
    public Session postponeAndReschedule(PostponeAndRescheduleCommand command) {
        log.info("Postponing session {} and creating recovery session", command.sessionId());

        // 1. Postpone original session
        Session postponedSession = postponeSession(
            new PostponeSessionCommand(command.sessionId(), command.reason())
        );

        // 2. Parse new dates
        LocalDateTime newStart = LocalDateTime.parse(command.newScheduledStart());
        LocalDateTime newEnd = LocalDateTime.parse(command.newScheduledEnd());

        // 3. Determine classroom and zoom (use original if not provided)
        String classroom = command.classroom() != null ?
            command.classroom() :
            (postponedSession.getClassroom() != null ? postponedSession.getClassroom().name() : null);

        String zoomMeetingId = command.newZoomMeetingId() != null ?
            command.newZoomMeetingId() :
            postponedSession.getZoomMeetingId();

        // 4. Create recovery session
        Session recoverySession = createSession(new CreateSessionCommand(
            postponedSession.getSubjectGroup().getId(),
            SessionType.RECUPERACION.name(),
            command.newScheduledStart(),
            command.newScheduledEnd(),
            postponedSession.getMode().name(),
            classroom,
            zoomMeetingId,
            "Recovery session for postponed session #" + postponedSession.getId(),
            null, // No schedule origin for recovery sessions
            postponedSession.getId(), // This recovers the postponed session
            null
        ));

        log.info("Recovery session {} created for postponed session {}",
            recoverySession.getId(), postponedSession.getId());

        return recoverySession;
    }

    // ==================== CANCEL SESSION ====================

    @Override
    public Session cancelSession(CancelSessionCommand command) {
        log.info("Cancelling session {}", command.sessionId());

        // 1. Fetch session
        Session session = findSessionById(command.sessionId());

        // 2. Validate
        validationService.validateCancellation(session, command.reason());

        // 3. Cancel session
        session.cancel(command.reason());

        // 4. Save
        Session savedSession = sessionRepository.save(session);

        log.info("Session {} cancelled. Reason: {}", savedSession.getId(), command.reason());
        return savedSession;
    }

    // ==================== QUERY METHODS ====================

    /**
     * Find session by ID or throw exception.
     */
    @Transactional(readOnly = true)
    public Session findSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    /**
     * Get all sessions for a subject group.
     */
    @Transactional(readOnly = true)
    public List<Session> getSessionsByGroup(Long groupId) {
        return sessionRepository.findBySubjectGroupId(groupId);
    }

    /**
     * Get all sessions for a teacher.
     */
    @Transactional(readOnly = true)
    public List<Session> getSessionsByTeacher(Long teacherId) {
        return sessionRepository.findByTeacherId(teacherId);
    }

    /**
     * Get upcoming sessions (next 7 days).
     */
    @Transactional(readOnly = true)
    public List<Session> getUpcomingSessions() {
        return sessionRepository.findUpcomingSessions(LocalDateTime.now());
    }

    /**
     * Get sessions in progress.
     */
    @Transactional(readOnly = true)
    public List<Session> getInProgressSessions() {
        return sessionRepository.findInProgressSessions();
    }

    /**
     * Get sessions requiring action (postponed without recovery).
     */
    @Transactional(readOnly = true)
    public List<Session> getSessionsRequiringAction() {
        return sessionRepository.findSessionsRequiringAction();
    }

    /**
     * Get sessions by status.
     */
    @Transactional(readOnly = true)
    public List<Session> getSessionsByStatus(SessionStatus status) {
        return sessionRepository.findByStatus(status);
    }

    /**
     * Get sessions for a teacher in a date range.
     */
    @Transactional(readOnly = true)
    public List<Session> getSessionsByTeacherAndDateRange(
        Long teacherId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        return sessionRepository.findByTeacherIdAndDateRange(teacherId, startDate, endDate);
    }

    /**
     * Get sessions for a group in a date range.
     */
    @Transactional(readOnly = true)
    public List<Session> getSessionsByGroupAndDateRange(
        Long groupId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        SubjectGroup group = subjectGroupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        return sessionRepository.findBySubjectGroupAndDateRange(group, startDate, endDate);
    }
}
