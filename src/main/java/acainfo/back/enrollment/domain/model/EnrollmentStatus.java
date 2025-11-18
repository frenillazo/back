package acainfo.back.enrollment.domain.model;

import lombok.Getter;

/**
 * Status of a student enrollment in a subject group.
 */
@Getter
public enum EnrollmentStatus {
    /**
     * Enrollment is active and student can attend classes
     */
    ACTIVE("Active"),

    /**
     * Enrollment has been cancelled by student or admin
     */
    CANCELLED("Cancelled"),

    /**
     * Enrollment is suspended (e.g., due to payment issues)
     */
    SUSPENDED("Suspended"),

    /**
     * Enrollment has been completed (course finished)
     */
    COMPLETED("Completed"),

    /**
     * Enrollment is on hold (waiting for approval or payment)
     */
    PENDING("Pending");

    private final String displayName;

    EnrollmentStatus(String displayName) {
        this.displayName = displayName;
    }
}
