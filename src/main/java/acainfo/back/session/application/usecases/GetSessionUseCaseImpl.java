package acainfo.back.session.application.usecases;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.session.application.ports.in.GetSessionUseCase;
import acainfo.back.session.application.ports.out.SessionRepositoryPort;
import acainfo.back.session.domain.model.SessionDomain;
import acainfo.back.session.domain.model.SessionMode;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.session.domain.model.SessionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of GetSessionUseCase
 * Handles all read-only session queries
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetSessionUseCaseImpl implements GetSessionUseCase {

    private final SessionRepositoryPort repository;

    @Override
    public Optional<SessionDomain> findById(Long id) {
        log.debug("Finding session by ID: {}", id);
        return repository.findById(id);
    }

    @Override
    public List<SessionDomain> findAll() {
        log.debug("Finding all sessions");
        return repository.findAll();
    }

    @Override
    public List<SessionDomain> findBySubjectGroupId(Long groupId) {
        log.debug("Finding sessions by subject group ID: {}", groupId);
        return repository.findBySubjectGroupId(groupId);
    }

    @Override
    public List<SessionDomain> findBySubjectGroupIdAndStatus(Long groupId, SessionStatus status) {
        log.debug("Finding sessions by group ID {} and status {}", groupId, status);
        return repository.findBySubjectGroupIdAndStatus(groupId, status);
    }

    @Override
    public List<SessionDomain> findBySubjectGroupIdAndDateRange(Long groupId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding sessions by group ID {} between {} and {}", groupId, startDate, endDate);
        return repository.findBySubjectGroupIdAndDateRange(groupId, startDate, endDate);
    }

    @Override
    public List<SessionDomain> findByTeacherId(Long teacherId) {
        log.debug("Finding sessions by teacher ID: {}", teacherId);
        return repository.findByTeacherId(teacherId);
    }

    @Override
    public List<SessionDomain> findByTeacherIdAndDateRange(Long teacherId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding sessions by teacher ID {} between {} and {}", teacherId, startDate, endDate);
        return repository.findByTeacherIdAndDateRange(teacherId, startDate, endDate);
    }

    @Override
    public List<SessionDomain> findActiveSessionsByTeacherId(Long teacherId) {
        log.debug("Finding active sessions by teacher ID: {}", teacherId);
        return repository.findActiveSessionsByTeacherId(teacherId);
    }

    @Override
    public List<SessionDomain> findByClassroom(Classroom classroom) {
        log.debug("Finding sessions by classroom: {}", classroom);
        return repository.findByClassroom(classroom);
    }

    @Override
    public List<SessionDomain> findByStatus(SessionStatus status) {
        log.debug("Finding sessions by status: {}", status);
        return repository.findByStatus(status);
    }

    @Override
    public List<SessionDomain> findByType(SessionType type) {
        log.debug("Finding sessions by type: {}", type);
        return repository.findByType(type);
    }

    @Override
    public List<SessionDomain> findByMode(SessionMode mode) {
        log.debug("Finding sessions by mode: {}", mode);
        return repository.findByMode(mode);
    }

    @Override
    public List<SessionDomain> findUpcomingSessions(LocalDateTime fromDate) {
        log.debug("Finding upcoming sessions from: {}", fromDate);
        return repository.findUpcomingSessions(fromDate);
    }

    @Override
    public List<SessionDomain> findSessionsStartingInNext24Hours() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusHours(24);
        log.debug("Finding sessions starting in next 24 hours");
        return repository.findSessionsStartingInNext24Hours(now, tomorrow);
    }

    @Override
    public List<SessionDomain> findInProgressSessions() {
        log.debug("Finding in-progress sessions");
        return repository.findInProgressSessions();
    }

    @Override
    public List<SessionDomain> findLateSessions() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("Finding late sessions");
        return repository.findLateSessions(now);
    }

    @Override
    public List<SessionDomain> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding sessions between {} and {}", startDate, endDate);
        return repository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<SessionDomain> findByGeneratedFromScheduleId(Long scheduleId) {
        log.debug("Finding sessions generated from schedule ID: {}", scheduleId);
        return repository.findByGeneratedFromScheduleId(scheduleId);
    }

    @Override
    public List<SessionDomain> findManuallyCreatedSessions() {
        log.debug("Finding manually created sessions");
        return repository.findManuallyCreatedSessions();
    }

    @Override
    public List<SessionDomain> findRecoverySessionsFor(Long sessionId) {
        log.debug("Finding recovery sessions for session ID: {}", sessionId);
        return repository.findRecoverySessionsFor(sessionId);
    }

    @Override
    public List<SessionDomain> findSessionsRequiringAction() {
        log.debug("Finding sessions requiring action");
        return repository.findSessionsRequiringAction();
    }

    @Override
    public Double getCompletionRateForGroup(Long groupId) {
        log.debug("Getting completion rate for group ID: {}", groupId);
        return repository.getCompletionRateForGroup(groupId);
    }

    @Override
    public Double getCompletionRateForSchedule(Long scheduleId) {
        log.debug("Getting completion rate for schedule ID: {}", scheduleId);
        return repository.getCompletionRateForSchedule(scheduleId);
    }

    @Override
    public long countBySubjectGroupId(Long groupId) {
        log.debug("Counting sessions for group ID: {}", groupId);
        return repository.countBySubjectGroupId(groupId);
    }

    @Override
    public long countBySubjectGroupIdAndStatus(Long groupId, SessionStatus status) {
        log.debug("Counting sessions for group ID {} with status {}", groupId, status);
        return repository.countBySubjectGroupIdAndStatus(groupId, status);
    }
}
