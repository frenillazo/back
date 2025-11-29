package acainfo.back.enrollment.domain.model;

import acainfo.back.user.domain.model.User;
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
 * This links a student (User with STUDENT role) to a specific SubjectGroup.
 *
 * Business rules:
 * - Student can only have one ACTIVE enrollment per subject group
 * - If group is full and student has 2+ enrollments, can enroll in ONLINE mode
 * - If group is full and student has <2 enrollments, goes to EN_ESPERA (waiting queue)
 * - When enrollment is withdrawn, if mode was PRESENCIAL, a place is freed
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
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The student enrolled in the group
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
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVO;

    /**
     * How the student will attend classes
     * - PRESENCIAL: occupies physical space
     * - ONLINE: attends remotely (when group is full and student has 2+ enrollments)
     */
    @NotNull(message = "Attendance mode is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_mode", nullable = false, length = 20)
    @Builder.Default
    private AttendanceMode attendanceMode = AttendanceMode.PRESENCIAL;

    /**
     * When the student enrolled
     */
    @CreatedDate
    @Column(name = "enrollment_date", nullable = false, updatable = false)
    private LocalDateTime enrollmentDate;

    /**
     * When the student withdrew (if applicable)
     */
    @Column(name = "withdrawal_date")
    private LocalDateTime withdrawalDate;

    /**
     * Reason for withdrawal (if applicable)
     */
    @Column(name = "withdrawal_reason", length = 500)
    private String withdrawalReason;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== BUSINESS METHODS ====================

    /**
     * Withdraw the student from the group
     */
    public void withdraw(String reason) {
        if (this.status == EnrollmentStatus.RETIRADO) {
            throw new IllegalStateException("Enrollment is already withdrawn");
        }
        this.status = EnrollmentStatus.RETIRADO;
        this.withdrawalDate = LocalDateTime.now();
        this.withdrawalReason = reason;
    }

    /**
     * Activate the enrollment (from EN_ESPERA to ACTIVO)
     */
    public void activate() {
        if (this.status == EnrollmentStatus.ACTIVO) {
            throw new IllegalStateException("Enrollment is already active");
        }
        this.status = EnrollmentStatus.ACTIVO;
        this.withdrawalDate = null;
        this.withdrawalReason = null;
    }

    /**
     * Put enrollment in waiting queue
     */
    public void putInWaitingQueue() {
        if (this.status == EnrollmentStatus.EN_ESPERA) {
            throw new IllegalStateException("Enrollment is already in waiting queue");
        }
        this.status = EnrollmentStatus.EN_ESPERA;
    }

    /**
     * Change attendance mode
     */
    public void changeAttendanceMode(AttendanceMode newMode) {
        if (this.attendanceMode == newMode) {
            throw new IllegalStateException("Attendance mode is already " + newMode);
        }
        this.attendanceMode = newMode;
    }

    // ==================== QUERY METHODS ====================

    /**
     * Check if enrollment is active
     */
    public boolean isActive() {
        return this.status == EnrollmentStatus.ACTIVO;
    }

    /**
     * Check if enrollment is withdrawn
     */
    public boolean isWithdrawn() {
        return this.status == EnrollmentStatus.RETIRADO;
    }

    /**
     * Check if enrollment is in waiting queue
     */
    public boolean isWaiting() {
        return this.status == EnrollmentStatus.EN_ESPERA;
    }

    /**
     * Check if student attends online
     */
    public boolean isOnlineMode() {
        return this.attendanceMode == AttendanceMode.ONLINE;
    }

    /**
     * Check if student attends presentially
     */
    public boolean isPresentialMode() {
        return this.attendanceMode == AttendanceMode.PRESENCIAL;
    }

    /**
     * Check if enrollment occupies physical space
     * Only ACTIVE and PRESENCIAL enrollments occupy space
     */
    public boolean occupiesPhysicalSpace() {
        return this.status == EnrollmentStatus.ACTIVO &&
               this.attendanceMode == AttendanceMode.PRESENCIAL;
    }

    /**
     * Get student's full name
     */
    public String getStudentFullName() {
        if (student == null) return "Unknown";
        return student.getFirstName() + " " + student.getLastName();
    }

    /**
     * Get subject group display name
     */
    public String getSubjectGroupDisplayName() {
        if (subjectGroup == null) return "Unknown";
        return subjectGroup.getDisplayName();
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
                ", student=" + (student != null ? student.getEmail() : "null") +
                ", subjectGroup=" + (subjectGroup != null ? subjectGroup.getId() : "null") +
                ", status=" + status +
                ", attendanceMode=" + attendanceMode +
                ", enrollmentDate=" + enrollmentDate +
                '}';
    }
}
