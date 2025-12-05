package com.acainfo.group.infrastructure.adapter.out.persistence.entity;

import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.domain.model.GroupType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA Entity for SubjectGroup persistence.
 * Maps to 'subject_groups' table in database.
 */
@Entity
@Table(
    name = "subject_groups",
    indexes = {
        @Index(name = "idx_subject_group_subject_id", columnList = "subject_id"),
        @Index(name = "idx_subject_group_teacher_id", columnList = "teacher_id"),
        @Index(name = "idx_subject_group_type", columnList = "type"),
        @Index(name = "idx_subject_group_status", columnList = "status")
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

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private GroupType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GroupStatus status = GroupStatus.OPEN;

    @Column(name = "current_enrollment_count", nullable = false)
    @Builder.Default
    private Integer currentEnrollmentCount = 0;

    @Column(name = "capacity")
    private Integer capacity;  // Nullable: null = use default based on type

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
