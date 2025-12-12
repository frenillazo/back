package com.acainfo.shared.factory;

import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.enrollment.domain.model.GroupRequest;
import com.acainfo.enrollment.domain.model.GroupRequestStatus;
import com.acainfo.group.domain.model.GroupType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory for creating Enrollment and GroupRequest test data.
 * Provides static methods for building domain entities with sensible defaults.
 */
public class EnrollmentFactory {

    private EnrollmentFactory() {
        // Utility class
    }

    // ==================== Enrollment Factory Methods ====================

    /**
     * Create a default active enrollment.
     */
    public static Enrollment defaultEnrollment() {
        return Enrollment.builder()
                .id(1L)
                .studentId(100L)
                .groupId(1L)
                .status(EnrollmentStatus.ACTIVE)
                .enrolledAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create an active enrollment for specific student and group.
     */
    public static Enrollment activeEnrollment(Long studentId, Long groupId) {
        return Enrollment.builder()
                .id(null)
                .studentId(studentId)
                .groupId(groupId)
                .status(EnrollmentStatus.ACTIVE)
                .enrolledAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a waiting list enrollment.
     */
    public static Enrollment waitingListEnrollment(Long studentId, Long groupId, int position) {
        return Enrollment.builder()
                .id(null)
                .studentId(studentId)
                .groupId(groupId)
                .status(EnrollmentStatus.WAITING_LIST)
                .waitingListPosition(position)
                .enrolledAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a withdrawn enrollment.
     */
    public static Enrollment withdrawnEnrollment(Long studentId, Long groupId) {
        return Enrollment.builder()
                .id(null)
                .studentId(studentId)
                .groupId(groupId)
                .status(EnrollmentStatus.WITHDRAWN)
                .enrolledAt(LocalDateTime.now().minusDays(30))
                .withdrawnAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a completed enrollment.
     */
    public static Enrollment completedEnrollment(Long studentId, Long groupId) {
        return Enrollment.builder()
                .id(null)
                .studentId(studentId)
                .groupId(groupId)
                .status(EnrollmentStatus.COMPLETED)
                .enrolledAt(LocalDateTime.now().minusMonths(3))
                .createdAt(LocalDateTime.now().minusMonths(3))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create an enrollment that was promoted from waiting list.
     */
    public static Enrollment promotedEnrollment(Long studentId, Long groupId) {
        return Enrollment.builder()
                .id(null)
                .studentId(studentId)
                .groupId(groupId)
                .status(EnrollmentStatus.ACTIVE)
                .enrolledAt(LocalDateTime.now().minusDays(7))
                .promotedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now().minusDays(7))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== GroupRequest Factory Methods ====================

    /**
     * Create a default pending group request.
     */
    public static GroupRequest defaultGroupRequest() {
        Set<Long> supporters = new HashSet<>();
        supporters.add(100L);
        return GroupRequest.builder()
                .id(1L)
                .subjectId(1L)
                .requesterId(100L)
                .requestedGroupType(GroupType.REGULAR_Q1)
                .status(GroupRequestStatus.PENDING)
                .supporterIds(supporters)
                .justification("We need an additional group for this subject")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a pending group request with specific subject and requester.
     */
    public static GroupRequest pendingGroupRequest(Long subjectId, Long requesterId, GroupType type) {
        Set<Long> supporters = new HashSet<>();
        supporters.add(requesterId);
        return GroupRequest.builder()
                .id(null)
                .subjectId(subjectId)
                .requesterId(requesterId)
                .requestedGroupType(type)
                .status(GroupRequestStatus.PENDING)
                .supporterIds(supporters)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a group request with minimum supporters (8).
     * Requester is included as first supporter, plus 7 others.
     */
    public static GroupRequest groupRequestWithMinSupporters(Long subjectId, Long requesterId) {
        Set<Long> supporters = new HashSet<>();
        supporters.add(requesterId); // Requester is always first supporter
        for (long i = 1; i <= 7; i++) {
            supporters.add(requesterId + i); // Add 7 more unique supporters
        }
        return GroupRequest.builder()
                .id(null)
                .subjectId(subjectId)
                .requesterId(requesterId)
                .requestedGroupType(GroupType.REGULAR_Q1)
                .status(GroupRequestStatus.PENDING)
                .supporterIds(supporters)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create an approved group request with default values.
     */
    public static GroupRequest approvedGroupRequest() {
        return approvedGroupRequest(1L, 100L, 10L);
    }

    /**
     * Create an approved group request.
     */
    public static GroupRequest approvedGroupRequest(Long subjectId, Long requesterId, Long createdGroupId) {
        Set<Long> supporters = new HashSet<>();
        for (long i = 1; i <= 8; i++) {
            supporters.add(i);
        }
        return GroupRequest.builder()
                .id(null)
                .subjectId(subjectId)
                .requesterId(requesterId)
                .requestedGroupType(GroupType.REGULAR_Q1)
                .status(GroupRequestStatus.APPROVED)
                .supporterIds(supporters)
                .createdGroupId(createdGroupId)
                .adminResponse("Request approved, group created")
                .processedByAdminId(1L)
                .processedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now().minusDays(7))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a rejected group request with default values.
     */
    public static GroupRequest rejectedGroupRequest() {
        return rejectedGroupRequest(1L, 100L);
    }

    /**
     * Create a rejected group request.
     */
    public static GroupRequest rejectedGroupRequest(Long subjectId, Long requesterId) {
        Set<Long> supporters = new HashSet<>();
        supporters.add(requesterId);
        return GroupRequest.builder()
                .id(null)
                .subjectId(subjectId)
                .requesterId(requesterId)
                .requestedGroupType(GroupType.REGULAR_Q1)
                .status(GroupRequestStatus.REJECTED)
                .supporterIds(supporters)
                .adminResponse("Not enough demand for this group")
                .processedByAdminId(1L)
                .processedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now().minusDays(7))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create an expired group request.
     */
    public static GroupRequest expiredGroupRequest(Long subjectId, Long requesterId) {
        Set<Long> supporters = new HashSet<>();
        supporters.add(requesterId);
        return GroupRequest.builder()
                .id(null)
                .subjectId(subjectId)
                .requesterId(requesterId)
                .requestedGroupType(GroupType.REGULAR_Q1)
                .status(GroupRequestStatus.EXPIRED)
                .supporterIds(supporters)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusDays(31))
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
