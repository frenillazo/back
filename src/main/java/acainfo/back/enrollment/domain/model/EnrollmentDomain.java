package acainfo.back.enrollment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Pure Domain Model for Enrollment
 *
 * Represents a student enrollment in a subject group.
 * Links a student to a specific SubjectGroup.
 *
 * Business Rules:
 * - Student can only have one ACTIVE enrollment per subject group
 * - If group is full and student has 2+ enrollments, can enroll in ONLINE mode
 * - If group is full and student has <2 enrollments, goes to EN_ESPERA (waiting queue)
 * - When enrollment is withdrawn, if mode was PRESENCIAL, a place is freed
 */
@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class EnrollmentDomain {

    private final Long id;

    /**
     * ID of the student enrolled in the group
     */
    private final Long studentId;

    /**
     * ID of the subject group the student is enrolled in
     */
    private final Long subjectGroupId;

    /**
     * Current status of the enrollment
     */
    private final EnrollmentStatus status;

    /**
     * How the student will attend classes
     * - PRESENCIAL: occupies physical space
     * - ONLINE: attends remotely (when group is full and student has 2+ enrollments)
     */
    private final AttendanceMode attendanceMode;

    /**
     * When the student enrolled
     */
    private final LocalDateTime enrollmentDate;

    /**
     * When the student withdrew (if applicable)
     */
    private final LocalDateTime withdrawalDate;

    /**
     * Reason for withdrawal (if applicable)
     */
    private final String withdrawalReason;

    private final LocalDateTime updatedAt;

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates the enrollment data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (subjectGroupId == null) {
            throw new IllegalArgumentException("Subject group ID is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        if (attendanceMode == null) {
            throw new IllegalArgumentException("Attendance mode is required");
        }

        // Validate withdrawal data consistency
        if (status == EnrollmentStatus.RETIRADO) {
            if (withdrawalDate == null) {
                throw new IllegalArgumentException("Withdrawal date is required for withdrawn enrollments");
            }
        }

        // Validate withdrawal reason length
        if (withdrawalReason != null && withdrawalReason.length() > 500) {
            throw new IllegalArgumentException("Withdrawal reason must not exceed 500 characters");
        }
    }

    // ==================== STATE TRANSITION METHODS ====================

    /**
     * Withdraw the student from the group
     * Returns a new EnrollmentDomain with RETIRADO status
     */
    public EnrollmentDomain withdraw(String reason) {
        if (this.status == EnrollmentStatus.RETIRADO) {
            throw new IllegalStateException("Enrollment is already withdrawn");
        }

        return this.toBuilder()
                .status(EnrollmentStatus.RETIRADO)
                .withdrawalDate(LocalDateTime.now())
                .withdrawalReason(reason)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Activate the enrollment (from EN_ESPERA to ACTIVO)
     * Returns a new EnrollmentDomain with ACTIVO status
     */
    public EnrollmentDomain activate() {
        if (this.status == EnrollmentStatus.ACTIVO) {
            throw new IllegalStateException("Enrollment is already active");
        }

        return this.toBuilder()
                .status(EnrollmentStatus.ACTIVO)
                .withdrawalDate(null)
                .withdrawalReason(null)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Put enrollment in waiting queue
     * Returns a new EnrollmentDomain with EN_ESPERA status
     */
    public EnrollmentDomain putInWaitingQueue() {
        if (this.status == EnrollmentStatus.EN_ESPERA) {
            throw new IllegalStateException("Enrollment is already in waiting queue");
        }

        return this.toBuilder()
                .status(EnrollmentStatus.EN_ESPERA)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Change attendance mode
     * Returns a new EnrollmentDomain with updated attendance mode
     */
    public EnrollmentDomain changeAttendanceMode(AttendanceMode newMode) {
        if (this.attendanceMode == newMode) {
            throw new IllegalStateException("Attendance mode is already " + newMode);
        }

        return this.toBuilder()
                .attendanceMode(newMode)
                .updatedAt(LocalDateTime.now())
                .build();
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

    @Override
    public String toString() {
        return "EnrollmentDomain{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", subjectGroupId=" + subjectGroupId +
                ", status=" + status +
                ", attendanceMode=" + attendanceMode +
                ", enrollmentDate=" + enrollmentDate +
                '}';
    }
}
