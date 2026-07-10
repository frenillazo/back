package com.acainfo.session.application.service;

import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.course.domain.exception.CourseNotFoundException;
import com.acainfo.course.domain.model.Course;
import com.acainfo.schedule.application.port.out.ScheduleRepositoryPort;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.session.application.dto.CreateSessionCommand;
import com.acainfo.session.application.dto.SessionFilters;
import com.acainfo.session.application.dto.UpdateSessionCommand;
import com.acainfo.session.application.port.in.CreateSessionUseCase;
import com.acainfo.session.application.port.in.DeleteSessionUseCase;
import com.acainfo.session.application.port.in.GetSessionUseCase;
import com.acainfo.session.application.port.in.UpdateSessionUseCase;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.exception.InvalidSessionStateException;
import com.acainfo.session.domain.exception.SessionNotFoundException;
import com.acainfo.session.domain.exception.TeacherSessionConflictException;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service implementing session CRUD use cases.
 * Contains business logic for basic session operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService implements
        CreateSessionUseCase,
        GetSessionUseCase,
        UpdateSessionUseCase,
        DeleteSessionUseCase {

    private final SessionRepositoryPort sessionRepositoryPort;
    private final CourseRepositoryPort courseRepositoryPort;
    private final ScheduleRepositoryPort scheduleRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    // ==================== CreateSessionUseCase ====================

    @Override
    @Transactional
    public Session create(CreateSessionCommand command) {
        log.info("Creating session: type={}, courseId={}, date={}",
                command.type(), command.courseId(), command.date());

        // Resolve subject ID and get group info for teacher validation
        Long subjectId = resolveSubjectId(command);
        Long teacherId = resolveTeacherId(command);

        // Check for teacher conflicts (only for EXTRA sessions)
        // REGULAR sessions are already validated at schedule level
        if (command.type() != com.acainfo.session.domain.model.SessionType.REGULAR && teacherId != null) {
            checkForTeacherConflicts(
                    teacherId,
                    subjectId,
                    command.date(),
                    command.startTime(),
                    command.endTime(),
                    command.mode(),
                    null  // No session to exclude (new session)
            );
        }

        Session session = Session.builder()
                .subjectId(subjectId)
                .courseId(command.courseId())
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
            case EXTRA -> {
                if (command.courseId() == null) {
                    throw new InvalidSessionStateException(
                            "EXTRA sessions require a courseId"
                    );
                }
                Course group = courseRepositoryPort.findById(command.courseId())
                        .orElseThrow(() -> new CourseNotFoundException(command.courseId()));
                yield group.getSubjectId();
            }
            case REGULAR -> {
                if (command.scheduleId() == null) {
                    throw new InvalidSessionStateException(
                            "REGULAR sessions require a scheduleId"
                    );
                }
                Schedule schedule = scheduleRepositoryPort.findById(command.scheduleId())
                        .orElseThrow(() -> new InvalidSessionStateException(
                                "Schedule not found: " + command.scheduleId()
                        ));
                Course group = courseRepositoryPort.findById(schedule.getCourseId())
                        .orElseThrow(() -> new CourseNotFoundException(schedule.getCourseId()));
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
        log.debug("Finding sessions with filters: courseId={}, subjectId={}, status={}, dateFrom={}, dateTo={}",
                filters.courseId(), filters.subjectId(), filters.status(),
                filters.dateFrom(), filters.dateTo());
        return sessionRepositoryPort.findWithFilters(filters);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> findByCourseId(Long courseId) {
        log.debug("Finding sessions by courseId: {}", courseId);
        return sessionRepositoryPort.findByCourseId(courseId);
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

        if (!session.isScheduled()) {
            throw new InvalidSessionStateException(
                    "Only SCHEDULED sessions can be updated. Current status: " + session.getStatus()
            );
        }

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

        if (!session.isScheduled()) {
            throw new InvalidSessionStateException(
                    "Only SCHEDULED sessions can be deleted. Current status: " + session.getStatus()
            );
        }

        sessionRepositoryPort.delete(id);
        log.info("Session deleted successfully: ID {}", id);
    }

    // ==================== Helper Methods ====================

    /**
     * Resolve teacher ID from the command based on session type.
     */
    private Long resolveTeacherId(CreateSessionCommand command) {
        return switch (command.type()) {
            case EXTRA -> {
                if (command.courseId() == null) {
                    yield null;
                }
                Course group = courseRepositoryPort.findById(command.courseId())
                        .orElse(null);
                yield group != null ? group.getTeacherId() : null;
            }
            case REGULAR -> {
                if (command.scheduleId() == null) {
                    yield null;
                }
                Schedule schedule = scheduleRepositoryPort.findById(command.scheduleId())
                        .orElse(null);
                if (schedule == null) {
                    yield null;
                }
                Course group = courseRepositoryPort.findById(schedule.getCourseId())
                        .orElse(null);
                yield group != null ? group.getTeacherId() : null;
            }
        };
    }

    /**
     * Check for teacher session conflicts.
     * A teacher can have overlapping sessions ONLY if:
     * 1. Both sessions are online (SessionMode.ONLINE)
     * 2. Both sessions are for the same subject
     *
     * @param teacherId The teacher's ID
     * @param subjectId The subject ID for the new session
     * @param date Session date
     * @param startTime Start time
     * @param endTime End time
     * @param mode Session mode (to check if online)
     * @param excludeSessionId Session ID to exclude (for updates)
     */
    private void checkForTeacherConflicts(
            Long teacherId,
            Long subjectId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            SessionMode mode,
            Long excludeSessionId
    ) {
        // Get all sessions for this teacher on this date
        List<Session> teacherSessions = sessionRepositoryPort.findByTeacherIdAndDate(teacherId, date);

        boolean newSessionIsOnline = mode == SessionMode.ONLINE;

        for (Session existing : teacherSessions) {
            // Skip the session being updated
            if (excludeSessionId != null && excludeSessionId.equals(existing.getId())) {
                continue;
            }

            // Check time overlap: start1 < end2 AND end1 > start2
            if (!timeOverlaps(startTime, endTime, existing.getStartTime(), existing.getEndTime())) {
                continue; // No overlap, no conflict
            }

            // There's a time overlap - check if it's allowed
            boolean existingSessionIsOnline = existing.getMode() == SessionMode.ONLINE;
            boolean sameSubject = subjectId.equals(existing.getSubjectId());
            boolean bothOnline = newSessionIsOnline && existingSessionIsOnline;

            // Allow overlap ONLY if both are online AND same subject
            if (bothOnline && sameSubject) {
                log.debug("Allowing teacher session overlap: both online and same subject (subjectId={})", subjectId);
                continue;
            }

            // Conflict! Get teacher name for the error message
            String teacherName = userRepositoryPort.findById(teacherId)
                    .map(user -> user.getFullName())
                    .orElse("ID " + teacherId);

            log.warn("Teacher session conflict detected: teacher={}, date={}, time={}-{} overlaps with existing session {}",
                    teacherName, date, startTime, endTime, existing.getId());

            throw new TeacherSessionConflictException(
                    teacherName,
                    date,
                    existing.getStartTime(),
                    existing.getEndTime()
            );
        }
    }

    /**
     * Check if two time ranges overlap.
     */
    private boolean timeOverlaps(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}
