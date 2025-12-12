package com.acainfo.enrollment.domain.model;

import com.acainfo.group.domain.model.GroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * GroupRequest domain entity - Anemic model with Lombok.
 * Represents a student request to create a new subject group.
 *
 * <p>Business rules (enforced in application services):</p>
 * <ul>
 *   <li>Minimum 8 supporters required for approval</li>
 *   <li>Requester is automatically the first supporter</li>
 *   <li>Admin can approve/reject at any time</li>
 *   <li>Requests expire if deadline passes without enough support</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class GroupRequest {

    public static final int MIN_SUPPORTERS_FOR_APPROVAL = 8;

    private Long id;

    /**
     * Reference to the subject for which a new group is requested.
     */
    private Long subjectId;

    /**
     * Reference to the student who created the request.
     */
    private Long requesterId;

    /**
     * Requested type of group (e.g., REGULAR_Q1, INTENSIVE_Q2).
     */
    private GroupType requestedGroupType;

    /**
     * Current status of this request.
     */
    private GroupRequestStatus status;

    /**
     * Set of student IDs who support this request.
     * Requester is automatically included.
     */
    @Builder.Default
    private Set<Long> supporterIds = new HashSet<>();

    /**
     * Optional message explaining why the group is needed.
     */
    private String justification;

    /**
     * Deadline for gathering supporters.
     */
    private LocalDateTime expiresAt;

    /**
     * Reference to the group created if request was approved.
     * Null if not approved or pending.
     */
    private Long createdGroupId;

    /**
     * Admin response when approving/rejecting.
     */
    private String adminResponse;

    /**
     * Reference to the admin who processed the request.
     */
    private Long processedByAdminId;

    /**
     * Date when admin processed (approved/rejected) the request.
     */
    private LocalDateTime processedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Status Query Methods ====================

    /**
     * Check if request is pending approval.
     */
    public boolean isPending() {
        return status == GroupRequestStatus.PENDING;
    }

    /**
     * Check if request has been approved.
     */
    public boolean isApproved() {
        return status == GroupRequestStatus.APPROVED;
    }

    /**
     * Check if request has been rejected.
     */
    public boolean isRejected() {
        return status == GroupRequestStatus.REJECTED;
    }

    /**
     * Check if request has expired.
     */
    public boolean isExpired() {
        return status == GroupRequestStatus.EXPIRED;
    }

    // ==================== Computed Properties ====================

    /**
     * Get the current number of supporters.
     */
    public int getSupporterCount() {
        return supporterIds != null ? supporterIds.size() : 0;
    }

    /**
     * Check if the request has reached minimum supporters for approval.
     */
    public boolean hasMinimumSupporters() {
        return getSupporterCount() >= MIN_SUPPORTERS_FOR_APPROVAL;
    }

    /**
     * Check if a specific student is a supporter.
     */
    public boolean isSupporter(Long studentId) {
        return supporterIds != null && supporterIds.contains(studentId);
    }

    /**
     * Check if the request can still accept new supporters.
     */
    public boolean canAcceptSupporters() {
        return isPending();
    }

    /**
     * Check if the request has been processed by an admin.
     */
    public boolean isProcessed() {
        return isApproved() || isRejected();
    }

    /**
     * Get remaining supporters needed to reach minimum.
     */
    public int getSupportersNeeded() {
        int needed = MIN_SUPPORTERS_FOR_APPROVAL - getSupporterCount();
        return Math.max(0, needed);
    }
}
