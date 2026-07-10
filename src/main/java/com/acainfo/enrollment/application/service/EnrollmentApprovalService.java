package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.port.in.ApproveEnrollmentUseCase;
import com.acainfo.enrollment.application.port.in.RejectEnrollmentUseCase;
import com.acainfo.enrollment.application.port.out.AutoReservationPort;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException;
import com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException;
import com.acainfo.enrollment.domain.exception.UnauthorizedApprovalException;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.course.domain.exception.CourseNotFoundException;
import com.acainfo.course.domain.model.Course;
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
    private final CourseRepositoryPort courseRepositoryPort;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final AutoReservationPort autoReservationPort;

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

        Course course = courseRepositoryPort.findByIdForUpdate(enrollment.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException(enrollment.getCourseId()));
        validateApproverAuthorization(approverUserId, course);

        // Capacity semantics: null capacity = unlimited (virtual/dual) → always ACTIVE,
        // never waiting list. With a capacity, check occupancy under the row lock.
        boolean hasAvailableSeats = !course.hasCapacityLimit()
                || enrollmentRepositoryPort.countActiveByCourseId(course.getId()) < course.getCapacity();

        if (hasAvailableSeats) {
            // Direct enrollment as ACTIVE
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            log.info("Enrollment {} approved as ACTIVE (seats available)", enrollmentId);
        } else {
            // Add to waiting list
            int position = enrollmentRepositoryPort.getNextWaitingListPosition(course.getId());
            enrollment.setStatus(EnrollmentStatus.WAITING_LIST);
            enrollment.setWaitingListPosition(position);
            log.info("Enrollment {} approved but added to waiting list at position {} (no seats)",
                    enrollmentId, position);
        }

        enrollment.setApprovedAt(LocalDateTime.now());
        enrollment.setApprovedByUserId(approverUserId);

        Enrollment savedEnrollment = enrollmentRepositoryPort.save(enrollment);

        // Auto-generate reservations for the newly active student
        if (savedEnrollment.isActive()) {
            autoReservationPort.generateForNewEnrollment(
                    savedEnrollment.getStudentId(),
                    savedEnrollment.getCourseId(),
                    savedEnrollment.getId()
            );
        }

        return savedEnrollment;
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

        Course group = getGroupById(enrollment.getCourseId());
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

    private Course getGroupById(Long courseId) {
        return courseRepositoryPort.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    /**
     * Validate that the approver is authorized to approve/reject enrollments for this course.
     * Authorization is granted if:
     * - User is an admin, OR
     * - User is the teacher assigned to the course (teacher may be unassigned = null)
     */
    private void validateApproverAuthorization(Long userId, Course course) {
        User user = getUserProfileUseCase.getUserById(userId);

        boolean isAdmin = user.isAdmin();
        boolean isCourseTeacher = course.getTeacherId() != null && course.getTeacherId().equals(userId);

        if (!isAdmin && !isCourseTeacher) {
            throw new UnauthorizedApprovalException(userId, course.getId());
        }
    }
}
