package acainfo.back.schedule.infrastructure.adapters.out.persistence.entities;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.session.domain.model.Session;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for Schedule persistence
 * Infrastructure concern - contains ONLY persistence-related annotations
 * NO business logic here - business logic belongs in ScheduleDomain
 */
@Entity(name = "Schedule")
@Table(name = "schedules", indexes = {
        @Index(name = "idx_schedule_group", columnList = "group_id"),
        @Index(name = "idx_schedule_day", columnList = "day_of_week"),
        @Index(name = "idx_schedule_time", columnList = "start_time, end_time"),
        @Index(name = "idx_schedule_classroom", columnList = "classroom")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @NotNull(message = "SubjectGroup is required")
    private SubjectGroup subjectGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "classroom", nullable = false, length = 20)
    @NotNull(message = "Classroom is required")
    private Classroom classroom;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "last_modified_at")
    private LocalDateTime updatedAt;

    /**
     * Sessions that were generated from this schedule.
     * This is the INVERSE side of the relationship (mappedBy).
     */
    @OneToMany(mappedBy = "generatedFromSchedule", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Session> generatedSessions = new ArrayList<>();

    /**
     * JPA validation: start time must be before end time
     */
    @PrePersist
    @PreUpdate
    private void validateTimes() {
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                    "Start time must be before end time. Start: " + startTime + ", End: " + endTime
            );
        }
    }

    @Override
    public String toString() {
        return "ScheduleJpaEntity{" +
                "id=" + id +
                ", dayOfWeek=" + dayOfWeek +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", classroom=" + classroom +
                '}';
    }
}
