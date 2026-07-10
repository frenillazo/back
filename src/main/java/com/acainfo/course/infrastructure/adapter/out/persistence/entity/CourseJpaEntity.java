package com.acainfo.course.infrastructure.adapter.out.persistence.entity;

import com.acainfo.course.domain.model.CourseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for Course persistence.
 * Maps to {@code courses} table (created by Flyway V2__curso_unificado.sql).
 */
@Entity
@Table(
    name = "courses",
    indexes = {
        @Index(name = "idx_course_subject_id", columnList = "subject_id"),
        @Index(name = "idx_course_teacher_id", columnList = "teacher_id"),
        @Index(name = "idx_course_status", columnList = "status"),
        @Index(name = "idx_course_dates", columnList = "start_date, end_date")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CourseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CourseStatus status = CourseStatus.OPEN;

    /** Physical seats; null = unlimited (virtual/dual course). */
    @Column(name = "capacity")
    private Integer capacity;

    /** Informative only — payments are handled outside the app. */
    @Column(name = "price_per_month", precision = 10, scale = 2)
    private BigDecimal pricePerMonth;

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
