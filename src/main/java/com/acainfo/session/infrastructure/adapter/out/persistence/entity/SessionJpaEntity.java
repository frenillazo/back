package com.acainfo.session.infrastructure.adapter.out.persistence.entity;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * JPA Entity for Session persistence.
 * Maps to 'sessions' table in database.
 */
@Entity
@Table(
    name = "sessions",
    indexes = {
        @Index(name = "idx_session_subject_id", columnList = "subject_id"),
        @Index(name = "idx_session_group_id", columnList = "group_id"),
        @Index(name = "idx_session_schedule_id", columnList = "schedule_id"),
        @Index(name = "idx_session_date", columnList = "date"),
        @Index(name = "idx_session_status", columnList = "status"),
        @Index(name = "idx_session_type", columnList = "type"),
        @Index(name = "idx_session_group_date", columnList = "group_id, date"),
        @Index(name = "idx_session_schedule_date", columnList = "schedule_id, date")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SessionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "group_id")
    private Long groupId;  // Nullable for SCHEDULING sessions

    @Column(name = "schedule_id")
    private Long scheduleId;  // Nullable for EXTRA and SCHEDULING sessions

    @Enumerated(EnumType.STRING)
    @Column(name = "classroom", nullable = false, length = 20)
    private Classroom classroom;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private SessionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private SessionMode mode;

    @Column(name = "postponed_to_date")
    private LocalDate postponedToDate;  // Only set when status is POSTPONED

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
