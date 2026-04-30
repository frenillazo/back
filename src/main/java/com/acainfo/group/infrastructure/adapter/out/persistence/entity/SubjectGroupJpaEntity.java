package com.acainfo.group.infrastructure.adapter.out.persistence.entity;

import com.acainfo.group.domain.model.GroupStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for SubjectGroup persistence.
 * Maps to {@code subject_groups} table.
 */
@Entity
@Table(
    name = "subject_groups",
    indexes = {
        @Index(name = "idx_subject_group_subject_id", columnList = "subject_id"),
        @Index(name = "idx_subject_group_teacher_id", columnList = "teacher_id"),
        @Index(name = "idx_subject_group_status", columnList = "status"),
        @Index(name = "idx_subject_group_dates", columnList = "start_date, end_date")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SubjectGroupJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GroupStatus status = GroupStatus.OPEN;

    @Column(name = "current_enrollment_count", nullable = false)
    @Builder.Default
    private Integer currentEnrollmentCount = 0;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "price_per_hour", precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    // Mapping aligned with the DB constraint (NOT NULL applied during Deploy 1 +
    // manual ALTER on prod). The application layer (GroupService.create / .update)
    // also enforces both dates at validation time.
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
