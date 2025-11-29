package acainfo.back.enrollment.application.services;

import acainfo.back.enrollment.application.ports.in.EnrollStudentUseCase;
import acainfo.back.enrollment.application.ports.in.WithdrawEnrollmentUseCase;
import acainfo.back.enrollment.application.ports.in.ChangeGroupUseCase;
import acainfo.back.enrollment.application.ports.in.GetEnrollmentUseCase;
import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.exception.*;
import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.enrollment.domain.model.AttendanceMode;
import acainfo.back.payment.application.services.PaymentService;
import acainfo.back.user.domain.exception.UserNotFoundException;
import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.domain.model.User;
import acainfo.back.user.infrastructure.adapters.out.UserRepository;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for enrollment management.
 * Implements all enrollment use cases with business logic and validations.
 *
 * Business Rules:
 * 1. Student can only have one ACTIVE enrollment per subject group
 * 2. If group is full, enrollment goes to EN_ESPERA (waiting queue)
 * 3. When student withdraws from PRESENCIAL enrollment, a place is freed
 * 4. Only students with STUDENT role can enroll
 * 5. Group must be ACTIVE to accept enrollments
 * 6. Student must not have overdue payments (>5 days) to enroll
 * 7. Online mode for 2+ enrollments (TODO: implement later)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentService implements
        EnrollStudentUseCase,
        WithdrawEnrollmentUseCase,
        ChangeGroupUseCase,
        GetEnrollmentUseCase {

    private final EnrollmentRepositoryPort enrollmentRepository;
    private final GroupRepositoryPort groupRepository;
    private final UserRepository userRepository;
    private final WaitingQueueService waitingQueueService;
    private final PaymentService paymentService;

    // ==================== ENROLL ====================

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Enrollment enrollStudent(Long studentId, Long groupId) {
        log.info("Enrolling student {} in group {}", studentId, groupId);

        // 1. Validate student exists and has STUDENT role
        User student = validateStudent(studentId);

        // 2. Validate group exists and is active
        SubjectGroup group = validateGroupActive(groupId);

        // 3. Validate not already enrolled
        validateNotAlreadyEnrolled(studentId, groupId);

        // 4. Validate payment status - student must not have overdue payments
        paymentService.validateNoOverduePayments(studentId);

        // 5. Determine attendance mode and enrollment status
        AttendanceMode attendanceMode = determineAttendanceMode(student, group);
        EnrollmentStatus status = determineEnrollmentStatus(group, attendanceMode);

        // 6. Create enrollment
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .subjectGroup(group)
                .status(status)
                .attendanceMode(attendanceMode)
                .build();

        // 7. Update group occupancy if presential and active
        if (status == EnrollmentStatus.ACTIVO && attendanceMode == AttendanceMode.PRESENCIAL) {
            group.incrementOccupancy();
            groupRepository.save(group);
            log.info("Group {} occupancy incremented to {}/{}",
                groupId, group.getCurrentOccupancy(), group.getMaxCapacity());
        }

        // 8. Save enrollment
        enrollment = enrollmentRepository.save(enrollment);

        log.info("Student {} enrolled successfully in group {} with status {} and mode {}",
            studentId, groupId, status, attendanceMode);

        // 9. TODO: Send notification to student
        // notificationService.notifyEnrollment(enrollment);

        return enrollment;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canEnroll(Long studentId, Long groupId) {
        try {
            validateStudent(studentId);
            validateGroupActive(groupId);
            validateNotAlreadyEnrolled(studentId, groupId);
            // TODO: validate payment status
            return true;
        } catch (Exception e) {
            log.debug("Student {} cannot enroll in group {}: {}", studentId, groupId, e.getMessage());
            return false;
        }
    }

    // ==================== WITHDRAW ====================

    @Override
    @Transactional
    public void withdrawEnrollment(Long enrollmentId, String reason) {
        log.info("Withdrawing enrollment {}", enrollmentId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        if (enrollment.isWithdrawn()) {
            throw new IllegalStateException("Enrollment is already withdrawn");
        }

        // Mark enrollment as withdrawn
        enrollment.withdraw(reason);

        // Free place if was presential and active
        if (enrollment.occupiesPhysicalSpace()) {
            SubjectGroup group = enrollment.getSubjectGroup();
            group.decrementOccupancy();
            groupRepository.save(group);

            log.info("Group {} occupancy decremented to {}/{}",
                group.getId(), group.getCurrentOccupancy(), group.getMaxCapacity());

            // Process waiting queue automatically
            waitingQueueService.processWaitingQueue(group.getId());
        }

        enrollmentRepository.save(enrollment);
        log.info("Enrollment {} withdrawn successfully", enrollmentId);

        // TODO: Send notification
        // notificationService.notifyWithdrawal(enrollment);
    }

    // ==================== CHANGE GROUP ====================

    @Override
    @Transactional
    public Enrollment changeGroup(Long enrollmentId, Long newGroupId) {
        log.info("Changing enrollment {} to new group {}", enrollmentId, newGroupId);

        Enrollment currentEnrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        SubjectGroup newGroup = groupRepository.findById(newGroupId)
                .orElseThrow(() -> new GroupNotFoundException(newGroupId));

        // Validate same subject
        if (!currentEnrollment.getSubjectGroup().getSubject().getId()
                .equals(newGroup.getSubject().getId())) {
            throw new IllegalArgumentException(
                "Cannot change to a group of a different subject. " +
                "Current subject: " + currentEnrollment.getSubjectGroup().getSubject().getCode() +
                ", New group subject: " + newGroup.getSubject().getCode()
            );
        }

        // Withdraw from current group
        withdrawEnrollment(enrollmentId, "Cambio de grupo");

        // Enroll in new group
        Enrollment newEnrollment = enrollStudent(
            currentEnrollment.getStudent().getId(),
            newGroupId
        );

        log.info("Student {} changed from group {} to group {} successfully",
            currentEnrollment.getStudent().getId(),
            currentEnrollment.getSubjectGroup().getId(),
            newGroupId);

        return newEnrollment;
    }

    // ==================== GET ====================

    @Override
    @Transactional(readOnly = true)
    public Enrollment getEnrollmentById(Long id) {
        log.debug("Fetching enrollment by ID: {}", id);
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new EnrollmentNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getActiveEnrollmentsByStudent(Long studentId) {
        log.debug("Fetching active enrollments for student: {}", studentId);
        return enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getAllEnrollmentsByStudent(Long studentId) {
        log.debug("Fetching all enrollments for student: {}", studentId);
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getEnrollmentsByGroup(Long groupId) {
        log.debug("Fetching all enrollments for group: {}", groupId);
        return enrollmentRepository.findBySubjectGroupId(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getActiveEnrollmentsByGroup(Long groupId) {
        log.debug("Fetching active enrollments for group: {}", groupId);
        return enrollmentRepository.findBySubjectGroupIdAndStatus(groupId, EnrollmentStatus.ACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentEnrolled(Long studentId, Long groupId) {
        return enrollmentRepository.existsByStudentIdAndSubjectGroupIdAndStatus(
            studentId, groupId, EnrollmentStatus.ACTIVO
        );
    }

    // ==================== PRIVATE VALIDATION METHODS ====================

    /**
     * Validates that the user exists and has STUDENT role.
     */
    private User validateStudent(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID is required");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException(studentId));

        if (!student.hasRole(RoleType.STUDENT)) {
            throw new IllegalArgumentException(
                "User " + studentId + " is not a student. Only users with STUDENT role can enroll."
            );
        }

        if (!student.isActive()) {
            throw new IllegalStateException("Student account is not active");
        }

        return student;
    }

    /**
     * Validates that the group exists and is active.
     */
    private SubjectGroup validateGroupActive(Long groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("Group ID is required");
        }

        SubjectGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        if (!group.isActive()) {
            throw new GroupNotActiveException(groupId);
        }

        return group;
    }

    /**
     * Validates that the student is not already enrolled in the group.
     */
    private void validateNotAlreadyEnrolled(Long studentId, Long groupId) {
        if (enrollmentRepository.existsByStudentIdAndSubjectGroupIdAndStatus(
                studentId, groupId, EnrollmentStatus.ACTIVO)) {
            throw new AlreadyEnrolledException(studentId, groupId);
        }
    }

    /**
     * Determines the attendance mode for the enrollment.
     *
     * Current logic:
     * - If group has available places: PRESENCIAL
     * - If group is full: EN_ESPERA (waiting queue)
     *
     * TODO: Implement online mode for students with 2+ active enrollments when group is full
     * This requires counting active enrollments and allowing ONLINE mode instead of EN_ESPERA
     */
    private AttendanceMode determineAttendanceMode(User student, SubjectGroup group) {
        // If group has available places, enroll presentially
        if (group.hasAvailablePlaces()) {
            return AttendanceMode.PRESENCIAL;
        }

        // TODO: Implement online mode logic for students with 2+ enrollments
        // long activeEnrollments = enrollmentRepository.countByStudentIdAndStatus(
        //     student.getId(), EnrollmentStatus.ACTIVO
        // );
        // if (activeEnrollments >= 2) {
        //     log.info("Student {} has {}+ enrollments, allowing ONLINE mode",
        //         student.getId(), activeEnrollments);
        //     return AttendanceMode.ONLINE;
        // }

        // If no places and doesn't qualify for online, will go to waiting queue
        // Still set PRESENCIAL as default mode
        return AttendanceMode.PRESENCIAL;
    }

    /**
     * Determines the enrollment status based on group availability and attendance mode.
     */
    private EnrollmentStatus determineEnrollmentStatus(SubjectGroup group, AttendanceMode mode) {
        // Online mode is always active (doesn't occupy physical space)
        if (mode == AttendanceMode.ONLINE) {
            return EnrollmentStatus.ACTIVO;
        }

        // Presential mode: active if group has places, waiting otherwise
        if (group.hasAvailablePlaces()) {
            return EnrollmentStatus.ACTIVO;
        } else {
            log.info("Group {} is full, enrollment will go to waiting queue", group.getId());
            return EnrollmentStatus.EN_ESPERA;
        }
    }
}
