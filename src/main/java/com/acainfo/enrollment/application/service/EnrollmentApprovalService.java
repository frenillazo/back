package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.port.in.ApproveEnrollmentUseCase;
import com.acainfo.enrollment.application.port.in.RejectEnrollmentUseCase;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException;
import com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException;
import com.acainfo.enrollment.domain.exception.UnauthorizedApprovalException;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service implementing enrollment approval and rejection use cases.
 * Handles teacher approval workflow for enrollment requests.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentApprovalService implements ApproveEnrollmentUseCase, RejectEnrollmentUseCase {

    private final EnrollmentRepositoryPort enrollmentRepositoryPort;
    private final GroupRepositoryPort groupRepositoryPort;
    private final GetUserProfileUseCase getUserProfileUseCase;

    @Override
    @Transactional
    public Enrollment approve(Long enrollmentId, Long approverUserId) {
        log.info("Approving enrollment {} by user {}", enrollmentId, approverUserId);

        Enrollment enrollment = getEnrollmentById(enrollmentId);

        if (!enrollment.canBeApproved()) {
            throw new InvalidEnrollmentStateException(
                    "Cannot approve enrollment with status: " + enrollment.getStatus()
            );
        }

        SubjectGroup group = getGroupById(enrollment.getGroupId());
        validateApproverAuthorization(approverUserId, group);

        // Check capacity to determine final status
        long activeCount = enrollmentRepositoryPort.countActiveByGroupId(group.getId());
        boolean hasAvailableSeats = activeCount < group.getMaxCapacity();

        if (hasAvailableSeats) {
            // Direct enrollment as ACTIVE
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            // Update group enrollment count
            group.setCurrentEnrollmentCount(group.getCurrentEnrollmentCount() + 1);
            groupRepositoryPort.save(group);
            log.info("Enrollment {} approved as ACTIVE (seats available)", enrollmentId);
        } else {
            // Add to waiting list
            int position = enrollmentRepositoryPort.getNextWaitingListPosition(group.getId());
            enrollment.setStatus(EnrollmentStatus.WAITING_LIST);
            enrollment.setWaitingListPosition(position);
            log.info("Enrollment {} approved but added to waiting list at position {} (no seats)",
                    enrollmentId, position);
        }

        enrollment.setApprovedAt(LocalDateTime.now());
        enrollment.setApprovedByUserId(approverUserId);

        return enrollmentRepositoryPort.save(enrollment);
    }

    @Override
    @Transactional
    public Enrollment reject(Long enrollmentId, Long rejecterUserId, String reason) {
        log.info("Rejecting enrollment {} by user {} with reason: {}", enrollmentId, rejecterUserId, reason);

        Enrollment enrollment = getEnrollmentById(enrollmentId);

        if (!enrollment.canBeRejected()) {
            throw new InvalidEnrollmentStateException(
                    "Cannot reject enrollment with status: " + enrollment.getStatus()
            );
        }

        SubjectGroup group = getGroupById(enrollment.getGroupId());
        validateApproverAuthorization(rejecterUserId, group);

        enrollment.setStatus(EnrollmentStatus.REJECTED);
        enrollment.setRejectedAt(LocalDateTime.now());
        enrollment.setApprovedByUserId(rejecterUserId);
        enrollment.setRejectionReason(reason);

        log.info("Enrollment {} rejected", enrollmentId);
        return enrollmentRepositoryPort.save(enrollment);
    }

    private Enrollment getEnrollmentById(Long enrollmentId) {
        return enrollmentRepositoryPort.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));
    }

    private SubjectGroup getGroupById(Long groupId) {
        return groupRepositoryPort.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    /**
     * Validate that the approver is authorized to approve/reject enrollments for this group.
     * Authorization is granted if:
     * - User is an admin, OR
     * - User is the teacher assigned to the group
     */
    private void validateApproverAuthorization(Long userId, SubjectGroup group) {
        User user = getUserProfileUseCase.getUserById(userId);

        boolean isAdmin = user.isAdmin();
        boolean isGroupTeacher = group.getTeacherId().equals(userId);

        if (!isAdmin && !isGroupTeacher) {
            throw new UnauthorizedApprovalException(userId, group.getId());
        }
    }
}
