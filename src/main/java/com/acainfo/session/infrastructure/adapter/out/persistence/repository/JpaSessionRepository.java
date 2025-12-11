package com.acainfo.session.infrastructure.adapter.out.persistence.repository;

import com.acainfo.session.infrastructure.adapter.out.persistence.entity.SessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for SessionJpaEntity.
 * Extends JpaSpecificationExecutor for Criteria Builder support.
 */
@Repository
public interface JpaSessionRepository extends
        JpaRepository<SessionJpaEntity, Long>,
        JpaSpecificationExecutor<SessionJpaEntity> {

    /**
     * Find all sessions for a specific group.
     */
    List<SessionJpaEntity> findByGroupId(Long groupId);

    /**
     * Find all sessions for a specific subject.
     */
    List<SessionJpaEntity> findBySubjectId(Long subjectId);

    /**
     * Find all sessions generated from a specific schedule.
     */
    List<SessionJpaEntity> findByScheduleId(Long scheduleId);

    /**
     * Check if a session already exists for a schedule on a specific date.
     * Used to prevent duplicate session generation.
     */
    boolean existsByScheduleIdAndDate(Long scheduleId, LocalDate date);

    /**
     * Find sessions for a group on a specific date.
     * Used for conflict detection.
     */
    List<SessionJpaEntity> findByGroupIdAndDate(Long groupId, LocalDate date);
}
