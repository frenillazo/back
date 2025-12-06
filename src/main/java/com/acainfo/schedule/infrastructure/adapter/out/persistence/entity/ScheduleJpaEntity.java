package com.acainfo.schedule.infrastructure.adapter.out.persistence.entity;

import com.acainfo.schedule.domain.model.Classroom;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * JPA Entity for Schedule persistence.
 * Maps to 'schedules' table in database.
 */
@Entity
@Table(
    name = "schedules",
    indexes = {
        @Index(name = "idx_schedule_group_id", columnList = "group_id"),
        @Index(name = "idx_schedule_classroom", columnList = "classroom"),
        @Index(name = "idx_schedule_day_of_week", columnList = "day_of_week"),
        @Index(name = "idx_schedule_conflict_check", columnList = "classroom, day_of_week, start_time, end_time")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ScheduleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "classroom", nullable = false, length = 20)
    private Classroom classroom;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
