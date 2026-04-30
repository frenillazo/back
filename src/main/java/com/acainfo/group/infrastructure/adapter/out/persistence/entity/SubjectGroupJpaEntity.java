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

    // Deploy 1: nullable=true so Hibernate ddl-auto=update can ALTER ADD COLUMN
    // on a non-empty subject_groups table without failing.
    // The application layer (GroupService.create / .update) still requires both
    // dates, so any NEW group has them populated; legacy rows are filled by
    // GroupDateBackfillRunner at startup.
    // Deploy 2: re-tighten to nullable=false AND run
    //   ALTER TABLE subject_groups ALTER COLUMN start_date SET NOT NULL;
    //   ALTER TABLE subject_groups ALTER COLUMN end_date SET NOT NULL;
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
