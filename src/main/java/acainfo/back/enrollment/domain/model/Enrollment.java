package acainfo.back.enrollment.domain.model;

import acainfo.back.shared.domain.model.User;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a student enrollment in a subject group.
 * Manages the relationship between students and their enrolled groups.
 */
@Entity
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
            name = "uk_student_group",
            columnNames = {"student_id", "subject_group_id"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The student who is enrolled
     */
    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /**
     * The subject group the student is enrolled in
     */
    @NotNull(message = "Subject group is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_group_id", nullable = false)
    private SubjectGroup subjectGroup;

    /**
     * Current status of the enrollment
     */
    @NotNull(message = "Enrollment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    /**
     * Date and time when the enrollment was created
     */
    @NotNull(message = "Enrollment date is required")
    @Column(name = "enrollment_date", nullable = false)
    @Builder.Default
    private LocalDateTime enrollmentDate = LocalDateTime.now();

    /**
     * Date and time when the enrollment was cancelled (if applicable)
     */
    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;

    /**
     * Reason for cancellation or suspension (if applicable)
     */
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    /**
     * Whether the student can attend online if no physical space available
     * (applicable when student has 2+ subjects enrolled)
     */
    @Column(name = "online_attendance_allowed")
    @Builder.Default
    private Boolean onlineAttendanceAllowed = false;

    /**
     * Notes or comments about the enrollment
     */
    @Column(name = "notes", length = 1000)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== HELPER METHODS ====================

    /**
     * Checks if the enrollment is active
     */
    public boolean isActive() {
        return status == EnrollmentStatus.ACTIVE;
    }

    /**
     * Checks if the enrollment is cancelled
     */
    public boolean isCancelled() {
        return status == EnrollmentStatus.CANCELLED;
    }

    /**
     * Checks if the enrollment is suspended
     */
    public boolean isSuspended() {
        return status == EnrollmentStatus.SUSPENDED;
    }

    /**
     * Checks if the enrollment is completed
     */
    public boolean isCompleted() {
        return status == EnrollmentStatus.COMPLETED;
    }

    /**
     * Checks if the enrollment is pending
     */
    public boolean isPending() {
        return status == EnrollmentStatus.PENDING;
    }

    /**
     * Activates the enrollment
     */
    public void activate() {
        this.status = EnrollmentStatus.ACTIVE;
        this.cancellationDate = null;
        this.cancellationReason = null;
    }

    /**
     * Cancels the enrollment with a reason
     */
    public void cancel(String reason) {
        this.status = EnrollmentStatus.CANCELLED;
        this.cancellationDate = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    /**
     * Suspends the enrollment with a reason
     */
    public void suspend(String reason) {
        this.status = EnrollmentStatus.SUSPENDED;
        this.cancellationDate = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    /**
     * Completes the enrollment
     */
    public void complete() {
        this.status = EnrollmentStatus.COMPLETED;
    }

    /**
     * Marks the enrollment as pending
     */
    public void markAsPending() {
        this.status = EnrollmentStatus.PENDING;
    }

    /**
     * Allows online attendance for this enrollment
     */
    public void allowOnlineAttendance() {
        this.onlineAttendanceAllowed = true;
    }

    /**
     * Disallows online attendance for this enrollment
     */
    public void disallowOnlineAttendance() {
        this.onlineAttendanceAllowed = false;
    }

    /**
     * Checks if online attendance is allowed
     */
    public boolean canAttendOnline() {
        return Boolean.TRUE.equals(onlineAttendanceAllowed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enrollment)) return false;
        Enrollment enrollment = (Enrollment) o;
        return id != null && id.equals(enrollment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                ", student=" + (student != null ? student.getId() : "null") +
                ", subjectGroup=" + (subjectGroup != null ? subjectGroup.getId() : "null") +
                ", status=" + status +
                ", enrollmentDate=" + enrollmentDate +
                '}';
    }
}
