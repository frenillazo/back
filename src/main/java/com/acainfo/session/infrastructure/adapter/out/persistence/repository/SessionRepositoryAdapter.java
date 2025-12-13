package com.acainfo.session.infrastructure.adapter.out.persistence.repository;

import com.acainfo.session.application.dto.SessionFilters;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.infrastructure.adapter.out.persistence.entity.SessionJpaEntity;
import com.acainfo.session.infrastructure.adapter.out.persistence.specification.SessionSpecifications;
import com.acainfo.session.infrastructure.mapper.SessionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing SessionRepositoryPort.
 * Translates domain operations to JPA operations.
 * Uses SessionPersistenceMapper to convert between domain and JPA entities.
 */
@Component
@RequiredArgsConstructor
public class SessionRepositoryAdapter implements SessionRepositoryPort {

    private final JpaSessionRepository jpaSessionRepository;
    private final SessionPersistenceMapper sessionPersistenceMapper;

    @Override
    public Session save(Session session) {
        SessionJpaEntity jpaEntity = sessionPersistenceMapper.toJpaEntity(session);
        SessionJpaEntity savedEntity = jpaSessionRepository.save(jpaEntity);
        return sessionPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public List<Session> saveAll(List<Session> sessions) {
        List<SessionJpaEntity> jpaEntities = sessionPersistenceMapper.toJpaEntityList(sessions);
        List<SessionJpaEntity> savedEntities = jpaSessionRepository.saveAll(jpaEntities);
        return sessionPersistenceMapper.toDomainList(savedEntities);
    }

    @Override
    public Optional<Session> findById(Long id) {
        return jpaSessionRepository.findById(id)
                .map(sessionPersistenceMapper::toDomain);
    }

    @Override
    public Page<Session> findWithFilters(SessionFilters filters) {
        // Build specification from filters
        Specification<SessionJpaEntity> spec = SessionSpecifications.withFilters(filters);

        // Build pagination and sorting
        Sort sort = filters.sortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filters.sortBy()).ascending()
                : Sort.by(filters.sortBy()).descending();

        PageRequest pageRequest = PageRequest.of(filters.page(), filters.size(), sort);

        // Execute query and map to domain
        return jpaSessionRepository.findAll(spec, pageRequest)
                .map(sessionPersistenceMapper::toDomain);
    }

    @Override
    public List<Session> findByGroupId(Long groupId) {
        return sessionPersistenceMapper.toDomainList(
                jpaSessionRepository.findByGroupId(groupId)
        );
    }

    @Override
    public List<Session> findBySubjectId(Long subjectId) {
        return sessionPersistenceMapper.toDomainList(
                jpaSessionRepository.findBySubjectId(subjectId)
        );
    }

    @Override
    public List<Session> findByScheduleId(Long scheduleId) {
        return sessionPersistenceMapper.toDomainList(
                jpaSessionRepository.findByScheduleId(scheduleId)
        );
    }

    @Override
    public boolean existsByScheduleIdAndDate(Long scheduleId, LocalDate date) {
        return jpaSessionRepository.existsByScheduleIdAndDate(scheduleId, date);
    }

    @Override
    public void delete(Long id) {
        jpaSessionRepository.deleteById(id);
    }

    @Override
    public boolean existsConflictingSession(Long groupId, LocalDate date, Long excludeSessionId) {
        List<SessionJpaEntity> sessionsOnDate = jpaSessionRepository.findByGroupIdAndDate(groupId, date);

        // If excludeSessionId is provided, filter it out
        if (excludeSessionId != null) {
            sessionsOnDate = sessionsOnDate.stream()
                    .filter(s -> !s.getId().equals(excludeSessionId))
                    .toList();
        }

        return !sessionsOnDate.isEmpty();
    }

    @Override
    public List<Session> findUpcomingByGroupIds(List<Long> groupIds, LocalDate fromDate, int limit) {
        if (groupIds == null || groupIds.isEmpty()) {
            return List.of();
        }

        SessionFilters filters = SessionFilters.upcomingForGroups(groupIds, fromDate, limit);
        Specification<SessionJpaEntity> spec = SessionSpecifications.withFilters(filters);

        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by("date", "startTime").ascending());

        return jpaSessionRepository.findAll(spec, pageRequest)
                .map(sessionPersistenceMapper::toDomain)
                .getContent();
    }
}
