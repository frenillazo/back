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
    List<ScheduleJpaEntity> findByCourseId(Long courseId);

    /**
     * Find schedules by teacher ID and day of week.
     * Joins through the courses table to get schedules where the group's teacher matches.
     */
    @Query("SELECT s FROM ScheduleJpaEntity s " +
           "JOIN CourseJpaEntity g ON s.courseId = g.id " +
           "WHERE g.teacherId = :teacherId AND s.dayOfWeek = :dayOfWeek")
    List<ScheduleJpaEntity> findByTeacherIdAndDayOfWeek(
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek);
}
