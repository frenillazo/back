package com.acainfo.session.application.service;

import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.schedule.application.port.out.ScheduleRepositoryPort;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.session.application.dto.*;
import com.acainfo.session.application.port.in.*;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.exception.InvalidSessionStateException;
import com.acainfo.session.domain.exception.SessionNotFoundException;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.exception.SubjectNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementing session use cases.
 * Contains business logic and validations for session operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService implements
        CreateSessionUseCase,
        GetSessionUseCase,
        UpdateSessionUseCase,
        DeleteSessionUseCase,
        SessionLifecycleUseCase,
        GenerateSessionsUseCase {

    private final SessionRepositoryPort sessionRepositoryPort;
    private final GroupRepositoryPort groupRepositoryPort;
    private final SubjectRepositoryPort subjectRepositoryPort;
    private final ScheduleRepositoryPort scheduleRepositoryPort;

    // ==================== CreateSessionUseCase ====================

    @Override
    @Transactional
    public Session create(CreateSessionCommand command) {
        log.info("Creating session: type={}, groupId={}, date={}",
                command.type(), command.groupId(), command.date());

        // Validate and resolve references based on session type
        Long subjectId = resolveSubjectId(command);

        Session session = Session.builder()
                .subjectId(subjectId)
                .groupId(command.groupId())
                .scheduleId(command.scheduleId())
                .classroom(command.classroom())
                .date(command.date())
                .startTime(command.startTime())
                .endTime(command.endTime())
                .status(SessionStatus.SCHEDULED)
                .type(command.type())
                .mode(command.mode())
                .build();

        Session savedSession = sessionRepositoryPort.save(session);

        log.info("Session created successfully: ID {}, type={}, date={}",
                savedSession.getId(), command.type(), command.date());

        return savedSession;
    }

    private Long resolveSubjectId(CreateSessionCommand command) {
        return switch (command.type()) {
            case SCHEDULING -> {
                // SCHEDULING: subjectId is required, no group
                if (command.subjectId() == null) {
                    throw new InvalidSessionStateException(
                            "SCHEDULING sessions require a subjectId"
                    );
                }
                // Validate subject exists
                subjectRepositoryPort.findById(command.subjectId())
                        .orElseThrow(() -> new SubjectNotFoundException(command.subjectId()));
                yield command.subjectId();
            }
            case EXTRA -> {
                // EXTRA: groupId is required, derive subjectId from group
                if (command.groupId() == null) {
                    throw new InvalidSessionStateException(
                            "EXTRA sessions require a groupId"
                    );
                }
                SubjectGroup group = groupRepositoryPort.findById(command.groupId())
                        .orElseThrow(() -> new GroupNotFoundException(command.groupId()));
                yield group.getSubjectId();
            }
            case REGULAR -> {
                // REGULAR: scheduleId is required, derive groupId and subjectId from schedule
                if (command.scheduleId() == null) {
                    throw new InvalidSessionStateException(
                            "REGULAR sessions require a scheduleId"
                    );
                }
                Schedule schedule = scheduleRepositoryPort.findById(command.scheduleId())
                        .orElseThrow(() -> new InvalidSessionStateException(
                                "Schedule not found: " + command.scheduleId()
                        ));
                SubjectGroup group = groupRepositoryPort.findById(schedule.getGroupId())
                        .orElseThrow(() -> new GroupNotFoundException(schedule.getGroupId()));
                yield group.getSubjectId();
            }
        };
    }

    // ==================== GetSessionUseCase ====================

    @Override
    @Transactional(readOnly = true)
    public Session getById(Long id) {
        log.debug("Getting session by ID: {}", id);
        return sessionRepositoryPort.findById(id)
                .orElseThrow(() -> new SessionNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Session> findWithFilters(SessionFilters filters) {
        log.debug("Finding sessions with filters: groupId={}, subjectId={}, status={}, dateFrom={}, dateTo={}",
                filters.groupId(), filters.subjectId(), filters.status(),
                filters.dateFrom(), filters.dateTo());
        return sessionRepositoryPort.findWithFilters(filters);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> findByGroupId(Long groupId) {
        log.debug("Finding sessions by groupId: {}", groupId);
        return sessionRepositoryPort.findByGroupId(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> findBySubjectId(Long subjectId) {
        log.debug("Finding sessions by subjectId: {}", subjectId);
        return sessionRepositoryPort.findBySubjectId(subjectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> findByScheduleId(Long scheduleId) {
        log.debug("Finding sessions by scheduleId: {}", scheduleId);
        return sessionRepositoryPort.findByScheduleId(scheduleId);
    }

    // ==================== UpdateSessionUseCase ====================

    @Override
    @Transactional
    public Session update(Long id, UpdateSessionCommand command) {
        log.info("Updating session with ID: {}", id);

        Session session = getById(id);

        // Only SCHEDULED sessions can be updated
        if (!session.isScheduled()) {
            throw new InvalidSessionStateException(
                    "Only SCHEDULED sessions can be updated. Current status: " + session.getStatus()
            );
        }

        // Update fields if provided
        if (command.classroom() != null) {
            session.setClassroom(command.classroom());
        }
        if (command.date() != null) {
            session.setDate(command.date());
        }
        if (command.startTime() != null) {
            session.setStartTime(command.startTime());
        }
        if (command.endTime() != null) {
            session.setEndTime(command.endTime());
        }
        if (command.mode() != null) {
            session.setMode(command.mode());
        }

        Session updatedSession = sessionRepositoryPort.save(session);
        log.info("Session updated successfully: ID {}", id);

        return updatedSession;
    }

    // ==================== DeleteSessionUseCase ====================

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting session with ID: {}", id);

        Session session = getById(id);

        // Only SCHEDULED sessions can be deleted
        if (!session.isScheduled()) {
            throw new InvalidSessionStateException(
                    "Only SCHEDULED sessions can be deleted. Current status: " + session.getStatus()
            );
        }

        sessionRepositoryPort.delete(id);
        log.info("Session deleted successfully: ID {}", id);
    }

    // ==================== SessionLifecycleUseCase ====================

    @Override
    @Transactional
    public Session start(Long id) {
        log.info("Starting session with ID: {}", id);

        Session session = getById(id);

        if (!session.isScheduled()) {
            throw new InvalidSessionStateException(
                    "Only SCHEDULED sessions can be started. Current status: " + session.getStatus()
            );
        }

        session.setStatus(SessionStatus.IN_PROGRESS);
        Session updatedSession = sessionRepositoryPort.save(session);

        log.info("Session started successfully: ID {}", id);
        return updatedSession;
    }

    @Override
    @Transactional
    public Session complete(Long id) {
        log.info("Completing session with ID: {}", id);

        Session session = getById(id);

        if (!session.isInProgress()) {
            throw new InvalidSessionStateException(
                    "Only IN_PROGRESS sessions can be completed. Current status: " + session.getStatus()
            );
        }

        session.setStatus(SessionStatus.COMPLETED);
        Session updatedSession = sessionRepositoryPort.save(session);

        log.info("Session completed successfully: ID {}", id);
        return updatedSession;
    }

    @Override
    @Transactional
    public Session cancel(Long id) {
        log.info("Cancelling session with ID: {}", id);

        Session session = getById(id);

        if (!session.isScheduled()) {
            throw new InvalidSessionStateException(
                    "Only SCHEDULED sessions can be cancelled. Current status: " + session.getStatus()
            );
        }

        session.setStatus(SessionStatus.CANCELLED);
        Session updatedSession = sessionRepositoryPort.save(session);

        log.info("Session cancelled successfully: ID {}", id);
        return updatedSession;
    }

    @Override
    @Transactional
    public Session postpone(Long id, PostponeSessionCommand command) {
        log.info("Postponing session with ID: {} to date: {}", id, command.newDate());

        Session originalSession = getById(id);

        if (!originalSession.isScheduled()) {
            throw new InvalidSessionStateException(
                    "Only SCHEDULED sessions can be postponed. Current status: " + originalSession.getStatus()
            );
        }

        // Mark original session as postponed
        originalSession.setStatus(SessionStatus.POSTPONED);
        originalSession.setPostponedToDate(command.newDate());
        sessionRepositoryPort.save(originalSession);

        // Create new session with the new date/time
        Session newSession = Session.builder()
                .subjectId(originalSession.getSubjectId())
                .groupId(originalSession.getGroupId())
                .scheduleId(originalSession.getScheduleId())
                .classroom(command.newClassroom() != null ? command.newClassroom() : originalSession.getClassroom())
                .date(command.newDate())
                .startTime(command.newStartTime() != null ? command.newStartTime() : originalSession.getStartTime())
                .endTime(command.newEndTime() != null ? command.newEndTime() : originalSession.getEndTime())
                .status(SessionStatus.SCHEDULED)
                .type(originalSession.getType())
                .mode(command.newMode() != null ? command.newMode() : originalSession.getMode())
                .build();

        Session savedNewSession = sessionRepositoryPort.save(newSession);

        log.info("Session postponed successfully: original ID {}, new ID {}, new date {}",
                id, savedNewSession.getId(), command.newDate());

        return savedNewSession;
    }

    // ==================== GenerateSessionsUseCase ====================

    @Override
    @Transactional
    public List<Session> generate(GenerateSessionsCommand command) {
        log.info("Generating sessions: groupId={}, from={}, to={}",
                command.groupId(), command.startDate(), command.endDate());

        List<Session> sessionsToCreate = preview(command);

        if (sessionsToCreate.isEmpty()) {
            log.info("No sessions to generate");
            return List.of();
        }

        List<Session> savedSessions = sessionRepositoryPort.saveAll(sessionsToCreate);

        log.info("Generated {} sessions", savedSessions.size());
        return savedSessions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> preview(GenerateSessionsCommand command) {
        log.debug("Previewing session generation: groupId={}, from={}, to={}",
                command.groupId(), command.startDate(), command.endDate());

        List<Schedule> schedules;

        if (command.groupId() != null) {
            // Generate for specific group
            schedules = scheduleRepositoryPort.findByGroupId(command.groupId());
        } else {
            // Generate for all groups - would need to fetch all schedules
            // For now, require a groupId
            throw new InvalidSessionStateException(
                    "Generation for all groups not yet implemented. Please specify a groupId."
            );
        }

        if (schedules.isEmpty()) {
            log.debug("No schedules found for groupId: {}", command.groupId());
            return List.of();
        }

        // Get group to derive subjectId
        SubjectGroup group = groupRepositoryPort.findById(command.groupId())
                .orElseThrow(() -> new GroupNotFoundException(command.groupId()));

        List<Session> sessionsToCreate = new ArrayList<>();

        // Iterate through each day in the date range
        LocalDate currentDate = command.startDate();
        while (!currentDate.isAfter(command.endDate())) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            // Find schedules that match this day of week
            for (Schedule schedule : schedules) {
                if (schedule.getDayOfWeek() == dayOfWeek) {
                    // Check if session already exists for this schedule and date
                    if (!sessionRepositoryPort.existsByScheduleIdAndDate(schedule.getId(), currentDate)) {
                        Session session = Session.builder()
                                .subjectId(group.getSubjectId())
                                .groupId(command.groupId())
                                .scheduleId(schedule.getId())
                                .classroom(schedule.getClassroom())
                                .date(currentDate)
                                .startTime(schedule.getStartTime())
                                .endTime(schedule.getEndTime())
                                .status(SessionStatus.SCHEDULED)
                                .type(SessionType.REGULAR)
                                .mode(determineSessionMode(schedule))
                                .build();

                        sessionsToCreate.add(session);
                    }
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        log.debug("Preview: {} sessions would be generated", sessionsToCreate.size());
        return sessionsToCreate;
    }

    /**
     * Determine session mode based on schedule's classroom.
     */
    private SessionMode determineSessionMode(Schedule schedule) {
        if (schedule.isOnline()) {
            return SessionMode.ONLINE;
        } else if (schedule.isPhysical()) {
            return SessionMode.IN_PERSON;
        }
        return SessionMode.DUAL;
    }
}
