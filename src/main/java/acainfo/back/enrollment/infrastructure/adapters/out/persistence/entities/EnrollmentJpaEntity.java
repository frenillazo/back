package acainfo.back.enrollment.infrastructure.adapters.out.persistence.entities;

import acainfo.back.enrollment.domain.model.AttendanceMode;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities.SubjectGroupJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA Entity for Enrollment persistence
 * Infrastructure layer - handles database mapping only
 */
@Entity(name = "Enrollment")
@Table(
    name = "enrollments",
    indexes = {
        @Index(name = "idx_enrollment_student", columnList = "student_id"),
        @Index(name = "idx_enrollment_group", columnList = "subject_group_id"),
        @Index(name = "idx_enrollment_status", columnList = "status"),
        @Index(name = "idx_enrollment_student_status", columnList = "student_id, status")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_enrollment_student_group_active",
            columnNames = {"student_id", "subject_group_id", "status"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private UserJpaEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_group_id", nullable = false)
    private SubjectGroupJpaEntity subjectGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_mode", nullable = false, length = 20)
    private AttendanceMode attendanceMode;

    @CreatedDate
    @Column(name = "enrollment_date", nullable = false, updatable = false)
    private LocalDateTime enrollmentDate;

    @Column(name = "withdrawal_date")
    private LocalDateTime withdrawalDate;

    @Column(name = "withdrawal_reason", length = 500)
    private String withdrawalReason;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnrollmentJpaEntity)) return false;
        EnrollmentJpaEntity that = (EnrollmentJpaEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
