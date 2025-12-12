package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.port.in.WaitingListUseCase;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException;
import com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementing waiting list management.
 * Handles FIFO queue operations: get list, leave queue, promote.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WaitingListService implements WaitingListUseCase {

    private final EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getWaitingListByGroupId(Long groupId) {
        log.debug("Getting waiting list for group: {}", groupId);
        return enrollmentRepositoryPort.findWaitingListByGroupId(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getWaitingListByStudentId(Long studentId) {
        log.debug("Getting waiting list positions for student: {}", studentId);
        return enrollmentRepositoryPort.findByStudentIdAndStatus(studentId, EnrollmentStatus.WAITING_LIST);
    }

    @Override
    @Transactional
    public Enrollment leaveWaitingList(Long enrollmentId) {
        log.info("Student leaving waiting list, enrollment: {}", enrollmentId);

        Enrollment enrollment = enrollmentRepositoryPort.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        if (!enrollment.isOnWaitingList()) {
            throw new InvalidEnrollmentStateException(
                    "Enrollment is not on waiting list. Current status: " + enrollment.getStatus()
            );
        }

        Integer oldPosition = enrollment.getWaitingListPosition();
        Long groupId = enrollment.getGroupId();

        // Update enrollment status
        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        enrollment.setWithdrawnAt(LocalDateTime.now());
        enrollment.setWaitingListPosition(null);

        Enrollment savedEnrollment = enrollmentRepositoryPort.save(enrollment);

        // Adjust positions for remaining students in queue
        if (oldPosition != null) {
            enrollmentRepositoryPort.decrementWaitingListPositionsAfter(groupId, oldPosition);
        }

        log.info("Student left waiting list successfully, enrollment: {}", enrollmentId);
        return savedEnrollment;
    }

    @Override
    @Transactional
    public Enrollment promoteNextFromWaitingList(Long groupId) {
        log.debug("Attempting to promote next student from waiting list for group: {}", groupId);

        List<Enrollment> waitingList = enrollmentRepositoryPort.findWaitingListByGroupId(groupId);

        if (waitingList.isEmpty()) {
            log.debug("No students in waiting list for group: {}", groupId);
            return null;
        }

        // Get first in queue (FIFO)
        Enrollment nextInLine = waitingList.get(0);
        Integer oldPosition = nextInLine.getWaitingListPosition();

        // Promote to ACTIVE
        nextInLine.setStatus(EnrollmentStatus.ACTIVE);
        nextInLine.setWaitingListPosition(null);
        nextInLine.setPromotedAt(LocalDateTime.now());

        Enrollment promotedEnrollment = enrollmentRepositoryPort.save(nextInLine);

        // Adjust positions for remaining students
        if (oldPosition != null) {
            enrollmentRepositoryPort.decrementWaitingListPositionsAfter(groupId, oldPosition);
        }

        log.info("Student {} promoted from waiting list to ACTIVE for group {}",
                promotedEnrollment.getStudentId(), groupId);

        return promotedEnrollment;
    }
}
