package com.acainfo.subject.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA Entity for SubjectInterest persistence.
 * Maps to {@code subject_interest} table (created by Flyway V2__curso_unificado.sql).
 */
@Entity
@Table(
    name = "subject_interest",
    indexes = {
        @Index(name = "idx_subject_interest_subject", columnList = "subject_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_subject_interest", columnNames = {"subject_id", "student_id"})
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SubjectInterestJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
