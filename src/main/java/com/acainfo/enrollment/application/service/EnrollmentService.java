package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.dto.ChangeGroupCommand;
import com.acainfo.enrollment.application.dto.EnrollStudentCommand;
import com.acainfo.enrollment.application.dto.EnrollmentFilters;
import com.acainfo.enrollment.application.port.in.ChangeGroupUseCase;
import com.acainfo.enrollment.application.port.in.EnrollStudentUseCase;
import com.acainfo.enrollment.application.port.in.GetEnrollmentUseCase;
import com.acainfo.enrollment.application.port.in.WithdrawEnrollmentUseCase;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.exception.AlreadyEnrolledException;
import com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException;
import com.acainfo.enrollment.domain.exception.GroupFullException;
import com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.model.SubjectGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementing enrollment CRUD and query use cases.
 * Handles basic enrollment operations: enroll, withdraw, change group, queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService implements
        EnrollStudentUseCase,
        WithdrawEnrollmentUseCase,
        ChangeGroupUseCase,
        GetEnrollmentUseCase {

    private final EnrollmentRepositoryPort enrollmentRepositoryPort;
    private final GroupRepositoryPort groupRepositoryPort;
    private final WaitingListService waitingListService;

    // ==================== EnrollStudentUseCase ====================

    @Override
    @Transactional
    public Enrollment enroll(EnrollStudentCommand command) {
        log.info("Enrolling student {} in group {} (pending approval)", command.studentId(), command.groupId());

        // Validate group exists
        SubjectGroup group = groupRepositoryPort.findById(command.groupId())
                .orElseThrow(() -> new GroupNotFoundException(command.groupId()));

        // Check if already enrolled (active, waiting, or pending)
        if (enrollmentRepositoryPort.existsActiveOrWaitingOrPendingEnrollment(command.studentId(), command.groupId())) {
            throw new AlreadyEnrolledException(command.studentId(), command.groupId());
        }

        // Get the price per hour from the group (uses default if not set)
        BigDecimal pricePerHour = group.getEffectivePricePerHour();

        // Create enrollment as PENDING_APPROVAL - teacher must approve
        Enrollment enrollment = Enrollment.builder()
                .studentId(command.studentId())
                .groupId(command.groupId())
                .pricePerHour(pricePerHour)
                .status(EnrollmentStatus.PENDING_APPROVAL)
                .enrolledAt(LocalDateTime.now())
                .build();

        log.info("Student {} enrollment request created for group {} at {}€/hour (pending teacher approval)",
                command.studentId(), command.groupId(), pricePerHour);

        return enrollmentRepositoryPort.save(enrollment);
    }

    // ==================== WithdrawEnrollmentUseCase ====================

    @Override
    @Transactional
    public Enrollment withdraw(Long enrollmentId) {
        log.info("Withdrawing enrollment {}", enrollmentId);

        Enrollment enrollment = getById(enrollmentId);

        if (!enrollment.canBeWithdrawn()) {
            throw new InvalidEnrollmentStateException(
                    "Cannot withdraw enrollment with status: " + enrollment.getStatus()
            );
        }

        boolean wasActive = enrollment.isActive();
        Integer oldPosition = enrollment.getWaitingListPosition();
        Long groupId = enrollment.getGroupId();

        // Update enrollment status
        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        enrollment.setWithdrawnAt(LocalDateTime.now());
        enrollment.setWaitingListPosition(null);

        Enrollment savedEnrollment = enrollmentRepositoryPort.save(enrollment);

        // If was on waiting list, adjust positions
        if (oldPosition != null) {
            enrollmentRepositoryPort.decrementWaitingListPositionsAfter(groupId, oldPosition);
        }

        // If was active, promote next from waiting list
        if (wasActive) {
            waitingListService.promoteNextFromWaitingList(groupId);
        }

        log.info("Enrollment {} withdrawn successfully", enrollmentId);
        return savedEnrollment;
    }

    // ==================== ChangeGroupUseCase ====================

    @Override
    @Transactional
    public Enrollment changeGroup(ChangeGroupCommand command) {
        log.info("Changing enrollment {} to group {}", command.enrollmentId(), command.newGroupId());

        Enrollment enrollment = getById(command.enrollmentId());

        if (!enrollment.isActive()) {
            throw new InvalidEnrollmentStateException(
                    "Only ACTIVE enrollments can change group. Current status: " + enrollment.getStatus()
            );
        }

        // Validate new group exists
        SubjectGroup newGroup = groupRepositoryPort.findById(command.newGroupId())
                .orElseThrow(() -> new GroupNotFoundException(command.newGroupId()));

        // Check capacity in new group
        long activeCount = enrollmentRepositoryPort.countActiveByGroupId(command.newGroupId());
        if (activeCount >= newGroup.getMaxCapacity()) {
            throw new GroupFullException(command.newGroupId());
        }

        Long oldGroupId = enrollment.getGroupId();

        // Update enrollment
        enrollment.setGroupId(command.newGroupId());
        Enrollment savedEnrollment = enrollmentRepositoryPort.save(enrollment);

        // Promote next from old group's waiting list
        waitingListService.promoteNextFromWaitingList(oldGroupId);

        log.info("Enrollment {} changed from group {} to group {}",
                command.enrollmentId(), oldGroupId, command.newGroupId());

        return savedEnrollment;
    }

    // ==================== GetEnrollmentUseCase ====================

    @Override
    @Transactional(readOnly = true)
    public Enrollment getById(Long id) {
        log.debug("Getting enrollment by ID: {}", id);
        return enrollmentRepositoryPort.findById(id)
                .orElseThrow(() -> new EnrollmentNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Enrollment> findWithFilters(EnrollmentFilters filters) {
        log.debug("Finding enrollments with filters: studentId={}, groupId={}, status={}",
                filters.studentId(), filters.groupId(), filters.status());
        return enrollmentRepositoryPort.findWithFilters(filters);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> findActiveByStudentId(Long studentId) {
        log.debug("Finding active enrollments for student: {}", studentId);
        return enrollmentRepositoryPort.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> findActiveByGroupId(Long groupId) {
        log.debug("Finding active enrollments for group: {}", groupId);
        return enrollmentRepositoryPort.findByGroupIdAndStatus(groupId, EnrollmentStatus.ACTIVE);
    }
}
