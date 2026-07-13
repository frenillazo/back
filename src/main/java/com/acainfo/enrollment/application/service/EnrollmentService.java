package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.dto.ChangeCourseCommand;
import com.acainfo.enrollment.application.dto.EnrollStudentCommand;
import com.acainfo.enrollment.application.dto.EnrollmentFilters;
import com.acainfo.enrollment.application.port.in.ChangeCourseUseCase;
import com.acainfo.enrollment.application.port.in.EnrollStudentUseCase;
import com.acainfo.enrollment.application.port.in.GetEnrollmentUseCase;
import com.acainfo.enrollment.application.port.in.WithdrawEnrollmentUseCase;
import com.acainfo.enrollment.application.port.out.AutoReservationPort;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.exception.AlreadyEnrolledException;
import com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException;
import com.acainfo.enrollment.domain.exception.CourseFullException;
import com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.course.domain.exception.CourseNotFoundException;
import com.acainfo.course.domain.model.Course;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        com.acainfo.enrollment.application.port.in.CloseCourseEnrollmentsUseCase,
        EnrollStudentUseCase,
        WithdrawEnrollmentUseCase,
        ChangeCourseUseCase,
        GetEnrollmentUseCase {

    private final EnrollmentRepositoryPort enrollmentRepositoryPort;
    private final CourseRepositoryPort courseRepositoryPort;
    private final WaitingListService waitingListService;
    private final AutoReservationPort autoReservationPort;

    // ==================== EnrollStudentUseCase ====================

    @Override
    @Transactional
    public Enrollment enroll(EnrollStudentCommand command) {
        log.info("Enrolling student {} in group {} (pending approval)", command.studentId(), command.courseId());

        // Validate group exists
        Course group = courseRepositoryPort.findById(command.courseId())
                .orElseThrow(() -> new CourseNotFoundException(command.courseId()));

        // Check if already enrolled (active, waiting, or pending)
        if (enrollmentRepositoryPort.existsActiveOrWaitingOrPendingEnrollment(command.studentId(), command.courseId())) {
            throw new AlreadyEnrolledException(command.studentId(), command.courseId());
        }

        // Create enrollment as PENDING_APPROVAL - teacher must approve
        Enrollment enrollment = Enrollment.builder()
                .studentId(command.studentId())
                .courseId(command.courseId())
                .status(EnrollmentStatus.PENDING_APPROVAL)
                .enrolledAt(LocalDateTime.now())
                .build();

        log.info("Student {} enrollment request created for course {} (pending approval)",
                command.studentId(), command.courseId());

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
                    "No se puede dar de baja una inscripción con estado: " + enrollment.getStatus()
            );
        }

        boolean wasActive = enrollment.isActive();
        Integer oldPosition = enrollment.getWaitingListPosition();
        Long courseId = enrollment.getCourseId();

        // Update enrollment status
        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        enrollment.setWithdrawnAt(LocalDateTime.now());
        enrollment.setWaitingListPosition(null);

        Enrollment savedEnrollment = enrollmentRepositoryPort.save(enrollment);

        // Cancel future reservations for the withdrawn student
        if (wasActive) {
            autoReservationPort.cancelFutureReservations(enrollment.getStudentId(), courseId);
        }

        // If was on waiting list, adjust positions
        if (oldPosition != null) {
            enrollmentRepositoryPort.decrementWaitingListPositionsAfter(courseId, oldPosition);
        }

        // If was active, promote next from waiting list (which will auto-generate their reservations)
        if (wasActive) {
            waitingListService.promoteNextFromWaitingList(courseId);
        }

        log.info("Enrollment {} withdrawn successfully", enrollmentId);
        return savedEnrollment;
    }

    // ==================== ChangeCourseUseCase ====================

    @Override
    @Transactional
    public Enrollment changeCourse(ChangeCourseCommand command) {
        log.info("Changing enrollment {} to group {}", command.enrollmentId(), command.newCourseId());

        Enrollment enrollment = getById(command.enrollmentId());

        if (!enrollment.isActive()) {
            throw new InvalidEnrollmentStateException(
                    "Solo las inscripciones ACTIVE pueden cambiar de grupo. Estado actual: " + enrollment.getStatus()
            );
        }

        // Validate new group exists
        Course newGroup = courseRepositoryPort.findById(command.newCourseId())
                .orElseThrow(() -> new CourseNotFoundException(command.newCourseId()));

        // Check capacity in new course (null capacity = unlimited)
        if (newGroup.hasCapacityLimit()) {
            long activeCount = enrollmentRepositoryPort.countActiveByCourseId(command.newCourseId());
            if (activeCount >= newGroup.getCapacity()) {
                throw new CourseFullException(command.newCourseId());
            }
        }

        Long oldCourseId = enrollment.getCourseId();

        // Update enrollment
        enrollment.setCourseId(command.newCourseId());
        Enrollment savedEnrollment = enrollmentRepositoryPort.save(enrollment);

        // Promote next from old group's waiting list
        waitingListService.promoteNextFromWaitingList(oldCourseId);

        log.info("Enrollment {} changed from group {} to group {}",
                command.enrollmentId(), oldCourseId, command.newCourseId());

        return savedEnrollment;
    }

    // ==================== CloseCourseEnrollmentsUseCase ====================

    /**
     * Close all live enrollments of a course (invoked when the course stops being OPEN).
     * ACTIVE -> COMPLETED (+ cancel future reservations); PENDING_APPROVAL / WAITING_LIST -> EXPIRED.
     */
    @Override
    @Transactional
    public int closeAllForCourse(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepositoryPort.findByCourseId(courseId);
        int transitioned = 0;

        for (Enrollment enrollment : enrollments) {
            switch (enrollment.getStatus()) {
                case ACTIVE -> {
                    enrollment.setStatus(EnrollmentStatus.COMPLETED);
                    enrollmentRepositoryPort.save(enrollment);
                    autoReservationPort.cancelFutureReservations(
                            enrollment.getStudentId(), courseId);
                    transitioned++;
                }
                case PENDING_APPROVAL, WAITING_LIST -> {
                    enrollment.setStatus(EnrollmentStatus.EXPIRED);
                    enrollment.setWaitingListPosition(null);
                    enrollmentRepositoryPort.save(enrollment);
                    transitioned++;
                }
                default -> { /* WITHDRAWN, COMPLETED, REJECTED, EXPIRED: nada que hacer */ }
            }
        }

        if (transitioned > 0) {
            log.info("Closed {} live enrollments for course {}", transitioned, courseId);
        }
        return transitioned;
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
        log.debug("Finding enrollments with filters: studentId={}, courseId={}, status={}",
                filters.studentId(), filters.courseId(), filters.status());
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
    public List<Enrollment> findActiveAndPendingByStudentId(Long studentId) {
        log.debug("Finding active and pending enrollments for student: {}", studentId);
        return enrollmentRepositoryPort.findByStudentIdAndStatusIn(
                studentId,
                List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.WAITING_LIST, EnrollmentStatus.PENDING_APPROVAL)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> findActiveByCourseId(Long courseId) {
        log.debug("Finding active enrollments for group: {}", courseId);
        return enrollmentRepositoryPort.findByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveByCourseId(Long courseId) {
        log.debug("Counting active enrollments for group: {}", courseId);
        return enrollmentRepositoryPort.countActiveByCourseId(courseId);
    }
}
