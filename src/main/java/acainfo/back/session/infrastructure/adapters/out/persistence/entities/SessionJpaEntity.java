package acainfo.back.session.infrastructure.adapters.out.persistence.entities;

import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.infrastructure.adapters.out.persistence.entities.ScheduleJpaEntity;
import acainfo.back.session.domain.model.SessionMode;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.session.domain.model.SessionType;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities.SubjectGroupJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA Entity for Session persistence
 * Infrastructure layer - handles database mapping only
 */
@Entity(name = "Session")
@Table(
    name = "sessions",
    indexes = {
        @Index(name = "idx_session_group", columnList = "subject_group_id"),
        @Index(name = "idx_session_schedule", columnList = "generated_from_schedule_id"),
        @Index(name = "idx_session_status", columnList = "status"),
        @Index(name = "idx_session_mode", columnList = "mode"),
        @Index(name = "idx_session_type", columnList = "type"),
        @Index(name = "idx_session_scheduled_start", columnList = "scheduled_start"),
        @Index(name = "idx_session_classroom", columnList = "classroom"),
        @Index(name = "idx_session_recovery_for", columnList = "recovery_for_session_id"),
        @Index(name = "idx_session_original", columnList = "original_session_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_group_id", nullable = false)
    private SubjectGroupJpaEntity subjectGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_from_schedule_id")
    private ScheduleJpaEntity generatedFromSchedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionType type;

    @Column(name = "scheduled_start", nullable = false)
    private LocalDateTime scheduledStart;

    @Column(name = "scheduled_end", nullable = false)
    private LocalDateTime scheduledEnd;

    @Column(name = "actual_start")
    private LocalDateTime actualStart;

    @Column(name = "actual_end")
    private LocalDateTime actualEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Classroom classroom;

    @Column(name = "zoom_meeting_id", length = 100)
    private String zoomMeetingId;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "postponement_reason", length = 500)
    private String postponementReason;

    @Column(name = "original_session_id")
    private Long originalSessionId;

    @Column(name = "recovery_for_session_id")
    private Long recoveryForSessionId;

    @Column(length = 1000)
    private String notes;

    @Column(name = "topics_covered", length = 1000)
    private String topicsCovered;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionJpaEntity)) return false;
        SessionJpaEntity that = (SessionJpaEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
