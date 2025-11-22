package acainfo.back.enrollment.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Pure domain model for group requests.
 * Represents a student request to create a new group for a subject.
 * Immutable and contains only business logic.
 *
 * Business Rules:
 * - A student creates a request for a specific subject
 * - Other students can support the request
 * - Minimum 8 supporters required for admin consideration
 * - Admin can approve or reject the request
 * - Once approved, a new group is created for that subject
 * - Once resolved, the request cannot be modified
 */
@Value
@Builder(toBuilder = true)
public class GroupRequestDomain {

    public static final int MINIMUM_SUPPORTERS = 8;

    Long id;

    /**
     * ID of the subject for which a new group is requested
     */
    Long subjectId;

    /**
     * ID of the student who created the request
     */
    Long requestedById;

    /**
     * IDs of students who support this request (including the requester)
     */
    Set<Long> supporterIds;

    /**
     * Current status of the request
     */
    @With
    GroupRequestStatus status;

    /**
     * When the request was created
     */
    LocalDateTime requestedAt;

    /**
     * When the request was resolved (approved or rejected)
     */
    @With
    LocalDateTime resolvedAt;

    /**
     * Reason for rejection (if rejected)
     */
    @With
    String rejectionReason;

    /**
     * Additional comments or notes about the request
     */
    @With
    String comments;

    // ==================== BUSINESS METHODS ====================

    /**
     * Add a student as a supporter
     */
    public GroupRequestDomain addSupporter(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }

        Set<Long> updatedSupporters = new java.util.HashSet<>(
            this.supporterIds != null ? this.supporterIds : Set.of()
        );
        updatedSupporters.add(studentId);

        return this.toBuilder()
                .supporterIds(updatedSupporters)
                .build();
    }

    /**
     * Remove a student from supporters
     */
    public GroupRequestDomain removeSupporter(Long studentId) {
        if (this.supporterIds == null || studentId == null) {
            return this;
        }

        Set<Long> updatedSupporters = new java.util.HashSet<>(this.supporterIds);
        updatedSupporters.remove(studentId);

        return this.toBuilder()
                .supporterIds(updatedSupporters)
                .build();
    }

    /**
     * Check if has minimum supporters required
     */
    public boolean hasMinimumSupporters() {
        return getSupportersCount() >= MINIMUM_SUPPORTERS;
    }

    /**
     * Get the count of supporters
     */
    public int getSupportersCount() {
        return (supporterIds != null) ? supporterIds.size() : 0;
    }

    /**
     * Approve the request
     */
    public GroupRequestDomain approve() {
        if (this.status != GroupRequestStatus.PENDIENTE) {
            throw new IllegalStateException("Only pending requests can be approved");
        }

        return this.toBuilder()
                .status(GroupRequestStatus.APROBADA)
                .resolvedAt(LocalDateTime.now())
                .rejectionReason(null) // Clear rejection reason if any
                .build();
    }

    /**
     * Reject the request with a reason
     */
    public GroupRequestDomain reject(String reason) {
        if (this.status != GroupRequestStatus.PENDIENTE) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }

        return this.toBuilder()
                .status(GroupRequestStatus.RECHAZADA)
                .resolvedAt(LocalDateTime.now())
                .rejectionReason(reason)
                .build();
    }

    /**
     * Check if the student is a supporter
     */
    public boolean isSupporter(Long studentId) {
        return supporterIds != null && supporterIds.contains(studentId);
    }

    /**
     * Check if the student is the requester
     */
    public boolean isRequester(Long studentId) {
        return requestedById != null && requestedById.equals(studentId);
    }

    // ==================== QUERY METHODS ====================

    /**
     * Check if request is pending
     */
    public boolean isPending() {
        return this.status == GroupRequestStatus.PENDIENTE;
    }

    /**
     * Check if request is approved
     */
    public boolean isApproved() {
        return this.status == GroupRequestStatus.APROBADA;
    }

    /**
     * Check if request is rejected
     */
    public boolean isRejected() {
        return this.status == GroupRequestStatus.RECHAZADA;
    }

    /**
     * Check if request is resolved (approved or rejected)
     */
    public boolean isResolved() {
        return this.status != null && this.status.isResolved();
    }

    /**
     * Get percentage of supporters progress (towards minimum)
     */
    public double getSupportersProgress() {
        return (getSupportersCount() * 100.0) / MINIMUM_SUPPORTERS;
    }

    /**
     * Get how many more supporters are needed
     */
    public int getSupportersNeeded() {
        int needed = MINIMUM_SUPPORTERS - getSupportersCount();
        return Math.max(0, needed);
    }

    /**
     * Validate business rules
     */
    public void validate() {
        if (subjectId == null) {
            throw new IllegalArgumentException("Subject ID is required");
        }
        if (requestedById == null) {
            throw new IllegalArgumentException("Requester ID is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        if (requestedAt == null) {
            throw new IllegalArgumentException("Requested date is required");
        }
    }
}
