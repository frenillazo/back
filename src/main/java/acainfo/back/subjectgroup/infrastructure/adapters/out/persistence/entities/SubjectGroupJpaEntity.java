package acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities;

import acainfo.back.schedule.infrastructure.adapters.out.persistence.entities.ScheduleJpaEntity;
import acainfo.back.session.infrastructure.adapters.out.persistence.entities.SessionJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.subject.infrastructure.adapters.out.persistence.entities.SubjectJpaEntity;
import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for SubjectGroup persistence
 * Infrastructure concern - contains ONLY persistence-related annotations
 * NO business logic here - business logic belongs in SubjectGroupDomain
 */
@Entity(name = "SubjectGroup")
@Table(
    name = "groups",
    indexes = {
        @Index(name = "idx_group_subject", columnList = "subject_id"),
        @Index(name = "idx_group_teacher", columnList = "teacher_id"),
        @Index(name = "idx_group_status", columnList = "status"),
        @Index(name = "idx_group_type", columnList = "type"),
        @Index(name = "idx_group_period", columnList = "period")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectGroupJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Subject is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectJpaEntity subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private UserJpaEntity teacher;

    @NotNull(message = "SubjectGroup type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupType type;

    @NotNull(message = "Academic period is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AcademicPeriod period;

    @NotNull(message = "SubjectGroup status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVO;

    @NotNull(message = "Max capacity is required")
    @Min(value = 1, message = "Max capacity must be at least 1")
    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @NotNull(message = "Current occupancy is required")
    @Min(value = 0, message = "Current occupancy cannot be negative")
    @Column(name = "current_occupancy", nullable = false)
    @Builder.Default
    private Integer currentOccupancy = 0;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "subjectGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ScheduleJpaEntity> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "subjectGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SessionJpaEntity> sessions = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubjectGroupJpaEntity)) return false;
        SubjectGroupJpaEntity that = (SubjectGroupJpaEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SubjectGroupJpaEntity{" +
                "id=" + id +
                ", subject=" + (subject != null ? subject.getCode() : "null") +
                ", type=" + type +
                ", period=" + period +
                ", status=" + status +
                ", occupancy=" + currentOccupancy + "/" + maxCapacity +
                '}';
    }
}
