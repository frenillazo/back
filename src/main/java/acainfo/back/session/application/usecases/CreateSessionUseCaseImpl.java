package acainfo.back.session.application.usecases;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.session.application.ports.in.CreateSessionUseCase;
import acainfo.back.session.application.ports.out.SessionRepositoryPort;
import acainfo.back.session.domain.model.SessionDomain;
import acainfo.back.session.domain.model.SessionMode;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.session.domain.model.SessionType;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of CreateSessionUseCase
 * Handles session creation with business validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreateSessionUseCaseImpl implements CreateSessionUseCase {

    private final SessionRepositoryPort sessionRepository;
    private final GroupRepositoryPort groupRepository;

    @Override
    public SessionDomain createSession(CreateSessionCommand command) {
        log.info("Creating new session for group {}", command.subjectGroupId());

        // 1. Validate subject group exists
        SubjectGroupDomain group = groupRepository.findById(command.subjectGroupId())
                .orElseThrow(() -> new GroupNotFoundException(command.subjectGroupId()));

        // 2. Parse enums
        SessionType type = SessionType.valueOf(command.sessionType());
        SessionMode mode = SessionMode.valueOf(command.mode());
        Classroom classroom = command.classroom() != null ?
                Classroom.valueOf(command.classroom()) : null;

        // 3. Parse dates
        LocalDateTime scheduledStart = LocalDateTime.parse(command.scheduledStart());
        LocalDateTime scheduledEnd = LocalDateTime.parse(command.scheduledEnd());

        // 4. Build domain
        SessionDomain session = SessionDomain.builder()
                .subjectGroupId(command.subjectGroupId())
                .generatedFromScheduleId(command.generatedFromScheduleId())
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
                .createdAt(LocalDateTime.now())
                .build();

        // 5. Validate domain
        session.validate();

        // 6. Additional validations (conflicts)
        if (classroom != null && classroom != Classroom.VIRTUAL) {
            boolean hasConflict = sessionRepository.hasClassroomConflict(
                    classroom, scheduledStart, scheduledEnd, null);
            if (hasConflict) {
                throw new IllegalStateException(
                        "Classroom " + classroom + " is not available for the requested time slot");
            }
        }

        // Check teacher conflicts (if group has assigned teacher)
        if (group.getTeacherId() != null) {
            boolean hasTeacherConflict = sessionRepository.hasTeacherConflict(
                    group.getTeacherId(), scheduledStart, scheduledEnd, null);
            if (hasTeacherConflict) {
                throw new IllegalStateException(
                        "Teacher has a conflicting session in the requested time slot");
            }
        }

        // 7. Save
        SessionDomain saved = sessionRepository.save(session);

        log.info("Session created successfully with ID: {}", saved.getId());
        return saved;
    }
}
