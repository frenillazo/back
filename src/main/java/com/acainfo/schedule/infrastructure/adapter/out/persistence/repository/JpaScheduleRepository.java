package com.acainfo.schedule.infrastructure.adapter.out.persistence.repository;

import com.acainfo.schedule.infrastructure.adapter.out.persistence.entity.ScheduleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
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
     * Find schedules by teacher ID and day of week.
     * Joins through the subject_groups table to get schedules where the group's teacher matches.
     */
    @Query("SELECT s FROM ScheduleJpaEntity s " +
           "JOIN SubjectGroupJpaEntity g ON s.groupId = g.id " +
           "WHERE g.teacherId = :teacherId AND s.dayOfWeek = :dayOfWeek")
    List<ScheduleJpaEntity> findByTeacherIdAndDayOfWeek(
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek);
}
