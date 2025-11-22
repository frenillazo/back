package acainfo.back.session.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.infrastructure.adapters.out.persistence.entities.ScheduleJpaEntity;
import acainfo.back.schedule.infrastructure.adapters.out.persistence.repositories.ScheduleJpaRepository;
import acainfo.back.session.application.ports.out.SessionRepositoryPort;
import acainfo.back.session.domain.model.SessionDomain;
import acainfo.back.session.domain.model.SessionMode;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.session.domain.model.SessionType;
import acainfo.back.session.infrastructure.adapters.out.persistence.entities.SessionJpaEntity;
import acainfo.back.session.infrastructure.adapters.out.persistence.mappers.SessionJpaMapper;
import acainfo.back.session.infrastructure.adapters.out.persistence.repositories.SessionJpaRepository;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities.SubjectGroupJpaEntity;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.repositories.SubjectGroupJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository Adapter Implementation
 * Implements SessionRepositoryPort using JPA infrastructure
 */
@Component
@RequiredArgsConstructor
public class SessionRepositoryAdapterImpl implements SessionRepositoryPort {

    private final SessionJpaRepository jpaRepository;
    private final SessionJpaMapper mapper;
    private final SubjectGroupJpaRepository subjectGroupRepository;
    private final ScheduleJpaRepository scheduleRepository;

    @Override
    @Transactional
    public SessionDomain save(SessionDomain session) {
        // Fetch related entities
        SubjectGroupJpaEntity subjectGroup = session.getSubjectGroupId() != null
                ? subjectGroupRepository.findById(session.getSubjectGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("SubjectGroup not found: " + session.getSubjectGroupId()))
                : null;

        ScheduleJpaEntity schedule = session.getGeneratedFromScheduleId() != null
                ? scheduleRepository.findById(session.getGeneratedFromScheduleId()).orElse(null)
                : null;

        SessionJpaEntity jpaEntity;

        if (session.getId() != null) {
            // Update existing
            jpaEntity = jpaRepository.findById(session.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Session not found: " + session.getId()));
            mapper.updateJpaEntity(jpaEntity, session);
        } else {
            // Create new
            jpaEntity = mapper.toJpaEntity(session, subjectGroup, schedule);
        }

        SessionJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SessionDomain> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findBySubjectGroupId(Long groupId) {
        return jpaRepository.findBySubjectGroupId(groupId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findBySubjectGroupIdAndStatus(Long groupId, SessionStatus status) {
        return jpaRepository.findBySubjectGroupIdAndStatus(groupId, status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findBySubjectGroupIdAndDateRange(Long groupId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findBySubjectGroupIdAndDateRange(groupId, startDate, endDate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findByStatus(SessionStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findByType(SessionType type) {
        return jpaRepository.findByType(type).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findByMode(SessionMode mode) {
        return jpaRepository.findByMode(mode).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findByTeacherId(Long teacherId) {
        return jpaRepository.findByTeacherId(teacherId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findByTeacherIdAndDateRange(Long teacherId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByTeacherIdAndDateRange(teacherId, startDate, endDate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findActiveSessionsByTeacherId(Long teacherId) {
        List<SessionStatus> activeStatuses = Arrays.asList(SessionStatus.PROGRAMADA, SessionStatus.EN_CURSO);
        return jpaRepository.findByTeacherId(teacherId).stream()
                .filter(s -> activeStatuses.contains(s.getStatus()))
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findByClassroom(Classroom classroom) {
        return jpaRepository.findByClassroom(classroom).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findByClassroomAndDateRange(Classroom classroom, LocalDateTime startDate, LocalDateTime endDate) {
        List<SessionStatus> excludedStatuses = Arrays.asList(SessionStatus.CANCELADA, SessionStatus.POSPUESTA);
        return jpaRepository.findByClassroom(classroom).stream()
                .filter(s -> !excludedStatuses.contains(s.getStatus()))
                .filter(s -> !s.getScheduledStart().isBefore(startDate) && !s.getScheduledEnd().isAfter(endDate))
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findConflictingClassroomSessions(Classroom classroom, LocalDateTime startTime, LocalDateTime endTime, Long excludeSessionId) {
        List<SessionStatus> excludedStatuses = Arrays.asList(SessionStatus.CANCELADA, SessionStatus.POSPUESTA);
        return jpaRepository.findClassroomConflicts(classroom, startTime, endTime, excludeSessionId, excludedStatuses).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasClassroomConflict(Classroom classroom, LocalDateTime startTime, LocalDateTime endTime, Long excludeSessionId) {
        return !findConflictingClassroomSessions(classroom, startTime, endTime, excludeSessionId).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findUpcomingSessions(LocalDateTime fromDate) {
        return jpaRepository.findUpcomingSessions(fromDate, SessionStatus.PROGRAMADA).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findSessionsStartingInNext24Hours(LocalDateTime now, LocalDateTime tomorrow) {
        return jpaRepository.findUpcomingSessions(now, SessionStatus.PROGRAMADA).stream()
                .filter(s -> !s.getScheduledStart().isAfter(tomorrow))
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findInProgressSessions() {
        return findByStatus(SessionStatus.EN_CURSO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findLateSessions(LocalDateTime now) {
        return jpaRepository.findByStatus(SessionStatus.PROGRAMADA).stream()
                .filter(s -> s.getScheduledStart().isBefore(now))
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByDateRange(startDate, endDate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findSessionsRequiringAction() {
        // Postponed sessions without recovery session
        return jpaRepository.findByStatus(SessionStatus.POSPUESTA).stream()
                .filter(s -> jpaRepository.findRecoverySessionsFor(s.getId()).isEmpty())
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SessionDomain> findRecoverySession(Long originalSessionId) {
        List<SessionJpaEntity> recoveries = jpaRepository.findRecoverySessionsFor(originalSessionId);
        return recoveries.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(recoveries.get(0)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findRecoverySessionsFor(Long sessionId) {
        return jpaRepository.findRecoverySessionsFor(sessionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findRecoverySessionsByGroupId(Long groupId) {
        return jpaRepository.findBySubjectGroupId(groupId).stream()
                .filter(s -> s.getType() == SessionType.RECUPERACION)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findSessionsWithRecentChanges(LocalDateTime since) {
        return jpaRepository.findByStatus(SessionStatus.PROGRAMADA).stream()
                .filter(s -> s.getUpdatedAt() != null && !s.getUpdatedAt().isBefore(since))
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasTeacherConflict(Long teacherId, LocalDateTime startTime, LocalDateTime endTime, Long excludeSessionId) {
        List<SessionStatus> excludedStatuses = Arrays.asList(SessionStatus.CANCELADA, SessionStatus.POSPUESTA);
        return jpaRepository.findByTeacherId(teacherId).stream()
                .anyMatch(s -> !s.getId().equals(excludeSessionId)
                        && !excludedStatuses.contains(s.getStatus())
                        && s.getScheduledStart().isBefore(endTime)
                        && s.getScheduledEnd().isAfter(startTime));
    }

    @Override
    @Transactional(readOnly = true)
    public long countBySubjectGroupIdAndStatus(Long groupId, SessionStatus status) {
        return jpaRepository.countBySubjectGroupIdAndStatus(groupId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countBySubjectGroupId(Long groupId) {
        return jpaRepository.findBySubjectGroupId(groupId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findCompletedSessionsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByStatus(SessionStatus.COMPLETADA).stream()
                .filter(s -> s.getActualEnd() != null
                        && !s.getActualEnd().isBefore(startDate)
                        && !s.getActualEnd().isAfter(endDate))
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findCancelledSessionsByGroupId(Long groupId) {
        return findBySubjectGroupIdAndStatus(groupId, SessionStatus.CANCELADA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> countSessionsByStatusForTeacher(Long teacherId) {
        // Group by status and count
        return jpaRepository.findByTeacherId(teacherId).stream()
                .collect(Collectors.groupingBy(SessionJpaEntity::getStatus, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> countSessionsByStatusForGroup(Long groupId) {
        return jpaRepository.findBySubjectGroupId(groupId).stream()
                .collect(Collectors.groupingBy(SessionJpaEntity::getStatus, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getCompletionRateForGroup(Long groupId) {
        List<SessionJpaEntity> sessions = jpaRepository.findBySubjectGroupId(groupId);
        if (sessions.isEmpty()) {
            return 0.0;
        }
        long completed = sessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETADA)
                .count();
        return (completed * 100.0) / sessions.size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findByGeneratedFromScheduleId(Long scheduleId) {
        return jpaRepository.findByGeneratedFromScheduleId(scheduleId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findByGeneratedFromScheduleIdAndDateRange(Long scheduleId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByGeneratedFromScheduleId(scheduleId).stream()
                .filter(s -> !s.getScheduledStart().isBefore(startDate) && !s.getScheduledStart().isAfter(endDate))
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findManuallyCreatedSessions() {
        return jpaRepository.findAll().stream()
                .filter(s -> s.getGeneratedFromSchedule() == null)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDomain> findManuallyCreatedSessionsByGroupId(Long groupId) {
        return jpaRepository.findBySubjectGroupId(groupId).stream()
                .filter(s -> s.getGeneratedFromSchedule() == null)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByGeneratedFromScheduleId(Long scheduleId) {
        return jpaRepository.findByGeneratedFromScheduleId(scheduleId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByGeneratedFromScheduleIdAndStatus(Long scheduleId, SessionStatus status) {
        return jpaRepository.findByGeneratedFromScheduleId(scheduleId).stream()
                .filter(s -> s.getStatus() == status)
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByGeneratedFromScheduleId(Long scheduleId) {
        return jpaRepository.existsByGeneratedFromScheduleId(scheduleId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getCompletionRateForSchedule(Long scheduleId) {
        List<SessionJpaEntity> sessions = jpaRepository.findByGeneratedFromScheduleId(scheduleId);
        if (sessions.isEmpty()) {
            return 0.0;
        }
        long completed = sessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETADA)
                .count();
        return (completed * 100.0) / sessions.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SessionDomain> findMostRecentSessionByScheduleId(Long scheduleId) {
        return jpaRepository.findByGeneratedFromScheduleId(scheduleId).stream()
                .max((s1, s2) -> s1.getScheduledStart().compareTo(s2.getScheduledStart()))
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SessionDomain> findNextUpcomingSessionByScheduleId(Long scheduleId, LocalDateTime now) {
        return jpaRepository.findByGeneratedFromScheduleId(scheduleId).stream()
                .filter(s -> s.getScheduledStart().isAfter(now) && s.getStatus() == SessionStatus.PROGRAMADA)
                .min((s1, s2) -> s1.getScheduledStart().compareTo(s2.getScheduledStart()))
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteByGeneratedFromScheduleIdAndStatus(Long scheduleId, SessionStatus status) {
        jpaRepository.deleteByGeneratedFromScheduleIdAndStatus(scheduleId, status);
    }
}
