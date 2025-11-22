package acainfo.back.enrollment.application.usecases;

import acainfo.back.enrollment.application.ports.in.EnrollStudentUseCase;
import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.exception.AlreadyEnrolledException;
import acainfo.back.enrollment.domain.exception.GroupNotActiveException;
import acainfo.back.enrollment.domain.model.AttendanceMode;
import acainfo.back.enrollment.domain.model.EnrollmentDomain;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.payment.application.services.PaymentService;
import acainfo.back.user.domain.exception.UserNotFoundException;
import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of EnrollStudentUseCase
 * Handles enrolling students in subject groups with business validations
 *
 * Business Rules:
 * 1. Student can only have one ACTIVE enrollment per subject group
 * 2. If group is full, enrollment goes to EN_ESPERA (waiting queue)
 * 3. Only students with STUDENT role can enroll
 * 4. Group must be ACTIVE to accept enrollments
 * 5. Student must not have overdue payments (>5 days) to enroll
 * 6. Online mode for 2+ enrollments (TODO: implement later)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollStudentUseCaseImpl implements EnrollStudentUseCase {

    private final EnrollmentRepositoryPort enrollmentRepository;
    private final GroupRepositoryPort groupRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public EnrollmentDomain enrollStudent(Long studentId, Long groupId) {
        log.info("Enrolling student {} in group {}", studentId, groupId);

        // 1. Validate student exists and has STUDENT role
        User student = validateStudent(studentId);

        // 2. Validate group exists and is active
        SubjectGroupDomain group = validateGroupActive(groupId);

        // 3. Validate not already enrolled
        validateNotAlreadyEnrolled(studentId, groupId);

        // 4. Validate payment status - student must not have overdue payments
        paymentService.validateNoOverduePayments(studentId);

        // 5. Determine attendance mode and enrollment status
        AttendanceMode attendanceMode = determineAttendanceMode(student, group);
        EnrollmentStatus status = determineEnrollmentStatus(group, attendanceMode);

        // 6. Create enrollment domain
        EnrollmentDomain enrollment = EnrollmentDomain.builder()
                .studentId(student.getId())
                .subjectGroupId(group.getId())
                .status(status)
                .attendanceMode(attendanceMode)
                .enrollmentDate(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 7. Update group occupancy if presential and active
        if (status == EnrollmentStatus.ACTIVO && attendanceMode == AttendanceMode.PRESENCIAL) {
            SubjectGroupDomain updatedGroup = group.incrementOccupancy();
            groupRepository.save(updatedGroup);
            log.info("Group {} occupancy incremented to {}/{}",
                groupId, updatedGroup.getCurrentOccupancy(), updatedGroup.getMaxCapacity());
        }

        // 8. Save enrollment
        EnrollmentDomain saved = enrollmentRepository.save(enrollment);

        log.info("Student {} enrolled successfully in group {} with status {} and mode {}",
            studentId, groupId, status, attendanceMode);

        // TODO: Send notification to student
        // notificationService.notifyEnrollment(enrollment);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canEnroll(Long studentId, Long groupId) {
        try {
            validateStudent(studentId);
            validateGroupActive(groupId);
            validateNotAlreadyEnrolled(studentId, groupId);
            paymentService.validateNoOverduePayments(studentId);
            return true;
        } catch (Exception e) {
            log.debug("Student {} cannot enroll in group {}: {}", studentId, groupId, e.getMessage());
            return false;
        }
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
    private SubjectGroupDomain validateGroupActive(Long groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("Group ID is required");
        }

        SubjectGroupDomain group = groupRepository.findById(groupId)
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
    private AttendanceMode determineAttendanceMode(User student, SubjectGroupDomain group) {
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
    private EnrollmentStatus determineEnrollmentStatus(SubjectGroupDomain group, AttendanceMode mode) {
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
