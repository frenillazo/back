package com.acainfo.schedule.infrastructure.adapter.out.persistence.repository;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.infrastructure.adapter.out.persistence.entity.ScheduleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
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

    /**
     * Find schedules by classroom and day of week.
     */
    List<ScheduleJpaEntity> findByClassroomAndDayOfWeek(Classroom classroom, DayOfWeek dayOfWeek);

    /**
     * Find conflicting schedules (same classroom, same day, overlapping time).
     * Time overlap: start1 < end2 AND end1 > start2
     */
    @Query("""
        SELECT s FROM ScheduleJpaEntity s
        WHERE s.classroom = :classroom
          AND s.dayOfWeek = :dayOfWeek
          AND s.startTime < :endTime
          AND s.endTime > :startTime
          AND (:excludeId IS NULL OR s.id != :excludeId)
        """)
    List<ScheduleJpaEntity> findConflictingSchedules(
            @Param("classroom") Classroom classroom,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}
