package com.acainfo.schedule.infrastructure.adapter.out.persistence.repository;

import com.acainfo.schedule.infrastructure.adapter.out.persistence.entity.ScheduleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for ScheduleJpaEntity.
 * Extends JpaSpecificationExecutor for Criteria Builder support.
 */
@Repository
public interface JpaScheduleRepository extends
        JpaRepository<ScheduleJpaEntity, Long>,
        JpaSpecificationExecutor<ScheduleJpaEntity> {

    /**
     * Find all schedules for a specific group.
     */
    List<ScheduleJpaEntity> findByGroupId(Long groupId);
}
