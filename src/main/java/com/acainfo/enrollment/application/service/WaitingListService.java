package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.port.in.WaitingListUseCase;
import com.acainfo.enrollment.application.port.out.AutoReservationPort;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException;
import com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.course.domain.exception.CourseNotFoundException;
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
    private final AutoReservationPort autoReservationPort;
    private final CourseRepositoryPort courseRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getWaitingListByCourseId(Long courseId) {
        log.debug("Getting waiting list for group: {}", courseId);
        return enrollmentRepositoryPort.findWaitingListByCourseId(courseId);
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
        Long courseId = enrollment.getCourseId();

        // Update enrollment status
        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        enrollment.setWithdrawnAt(LocalDateTime.now());
        enrollment.setWaitingListPosition(null);

        Enrollment savedEnrollment = enrollmentRepositoryPort.save(enrollment);

        // Adjust positions for remaining students in queue
        if (oldPosition != null) {
            enrollmentRepositoryPort.decrementWaitingListPositionsAfter(courseId, oldPosition);
        }

        log.info("Student left waiting list successfully, enrollment: {}", enrollmentId);
        return savedEnrollment;
    }

    @Override
    @Transactional
    public Enrollment promoteNextFromWaitingList(Long courseId) {
        log.debug("Attempting to promote next student from waiting list for group: {}", courseId);

        // Lock the group row to prevent concurrent promotions from exceeding capacity
        courseRepositoryPort.findByIdForUpdate(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        List<Enrollment> waitingList = enrollmentRepositoryPort.findWaitingListByCourseId(courseId);

        if (waitingList.isEmpty()) {
            log.debug("No students in waiting list for group: {}", courseId);
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
            enrollmentRepositoryPort.decrementWaitingListPositionsAfter(courseId, oldPosition);
        }

        // Auto-generate reservations for promoted student
        autoReservationPort.generateForNewEnrollment(
                promotedEnrollment.getStudentId(),
                courseId,
                promotedEnrollment.getId()
        );

        log.info("Student {} promoted from waiting list to ACTIVE for group {}",
                promotedEnrollment.getStudentId(), courseId);

        return promotedEnrollment;
    }
}
