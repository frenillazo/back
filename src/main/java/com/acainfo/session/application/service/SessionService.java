package com.acainfo.session.application.service;

import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.model.SubjectGroup;
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
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.exception.SubjectNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final GroupRepositoryPort groupRepositoryPort;
    private final SubjectRepositoryPort subjectRepositoryPort;
    private final ScheduleRepositoryPort scheduleRepositoryPort;

    // ==================== CreateSessionUseCase ====================

    @Override
    @Transactional
    public Session create(CreateSessionCommand command) {
        log.info("Creating session: type={}, groupId={}, date={}",
                command.type(), command.groupId(), command.date());

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
                if (command.subjectId() == null) {
                    throw new InvalidSessionStateException(
                            "SCHEDULING sessions require a subjectId"
                    );
                }
                subjectRepositoryPort.findById(command.subjectId())
                        .orElseThrow(() -> new SubjectNotFoundException(command.subjectId()));
                yield command.subjectId();
            }
            case EXTRA -> {
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
}
