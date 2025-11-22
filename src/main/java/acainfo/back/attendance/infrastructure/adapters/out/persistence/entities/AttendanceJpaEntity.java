package acainfo.back.attendance.infrastructure.adapters.out.persistence.entities;

import acainfo.back.attendance.domain.model.AttendanceStatus;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.entities.EnrollmentJpaEntity;
import acainfo.back.session.infrastructure.adapters.out.persistence.entities.SessionJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA Entity for Attendance persistence.
 * Infrastructure layer - contains JPA annotations only.
 * Domain logic is in AttendanceDomain.
 */
@Entity
@Table(
    name = "attendance",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_attendance_session_enrollment",
            columnNames = {"session_id", "enrollment_id"}
        )
    },
    indexes = {
        @Index(name = "idx_attendance_session", columnList = "session_id"),
        @Index(name = "idx_attendance_enrollment", columnList = "enrollment_id"),
        @Index(name = "idx_attendance_status", columnList = "status"),
        @Index(name = "idx_attendance_recorded_at", columnList = "recorded_at")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The session for which attendance is being recorded
     */
    @NotNull(message = "Session is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionJpaEntity session;

    /**
     * The enrollment for which attendance is being recorded
     */
    @NotNull(message = "Enrollment is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private EnrollmentJpaEntity enrollment;

    /**
     * Attendance status (PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO)
     */
    @NotNull(message = "Attendance status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.PRESENTE;

    /**
     * Timestamp when attendance was recorded
     */
    @NotNull(message = "Recorded at timestamp is required")
    @Column(name = "recorded_at", nullable = false)
    @Builder.Default
    private LocalDateTime recordedAt = LocalDateTime.now();

    /**
     * The user (teacher/admin) who recorded the attendance
     */
    @NotNull(message = "Recorded by user is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id", nullable = false)
    private UserJpaEntity recordedBy;

    /**
     * Optional notes about the attendance
     */
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Column(length = 500)
    private String notes;

    /**
     * Minutes late (only applicable when status = TARDANZA)
     */
    @Min(value = 0, message = "Minutes late cannot be negative")
    @Column(name = "minutes_late")
    private Integer minutesLate;

    /**
     * Timestamp when justification was provided (if status = JUSTIFICADO)
     */
    @Column(name = "justified_at")
    private LocalDateTime justifiedAt;

    /**
     * The user who approved the justification (teacher/admin)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "justified_by_id")
    private UserJpaEntity justifiedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttendanceJpaEntity)) return false;
        AttendanceJpaEntity that = (AttendanceJpaEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
