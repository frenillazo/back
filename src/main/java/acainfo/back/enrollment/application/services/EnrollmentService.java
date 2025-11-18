package acainfo.back.enrollment.application.services;

import acainfo.back.enrollment.application.ports.in.CancelEnrollmentUseCase;
import acainfo.back.enrollment.application.ports.in.CreateEnrollmentUseCase;
import acainfo.back.enrollment.application.ports.in.GetEnrollmentUseCase;
import acainfo.back.enrollment.application.ports.in.UpdateEnrollmentStatusUseCase;
import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.exception.EnrollmentAlreadyExistsException;
import acainfo.back.enrollment.domain.exception.EnrollmentCannotBeCancelledException;
import acainfo.back.enrollment.domain.exception.EnrollmentNotFoundException;
import acainfo.back.enrollment.domain.exception.InvalidEnrollmentStatusException;
import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.shared.domain.exception.UserNotFoundException;
import acainfo.back.shared.domain.model.RoleType;
import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.out.UserRepository;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.exception.GroupFullException;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation for enrollment management.
 * Implements all enrollment use cases with business logic and validations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentService implements
        CreateEnrollmentUseCase,
        CancelEnrollmentUseCase,
        GetEnrollmentUseCase,
        UpdateEnrollmentStatusUseCase {

    private final EnrollmentRepositoryPort enrollmentRepository;
    private final GroupRepositoryPort groupRepository;
    private final UserRepository userRepository;

    // ==================== CREATE ====================

    @Override
    public Enrollment createEnrollment(Enrollment enrollment) {
        log.info("Creating enrollment for student ID: {} in group ID: {}",
                enrollment.getStudent().getId(),
                enrollment.getSubjectGroup().getId());

        // Validate student exists and has STUDENT role
        User student = validateStudent(enrollment.getStudent().getId());
        enrollment.setStudent(student);

        // Validate subject group exists and is active
        SubjectGroup subjectGroup = validateSubjectGroup(enrollment.getSubjectGroup().getId());
        enrollment.setSubjectGroup(subjectGroup);

        // Check if student is already enrolled in this group
        if (enrollmentRepository.existsByStudentIdAndSubjectGroupId(
                student.getId(), subjectGroup.getId())) {
            throw new EnrollmentAlreadyExistsException(student.getId(), subjectGroup.getId());
        }

        // Check group availability or allow online attendance
        long activeEnrollmentsCount = enrollmentRepository.countActiveByStudentId(student.getId());

        if (!subjectGroup.hasAvailablePlaces()) {
            // If student has 2+ enrollments, can attend online
            if (activeEnrollmentsCount >= 2) {
                log.info("Student {} has {} active enrollments, allowing online attendance",
                        student.getId(), activeEnrollmentsCount);
                enrollment.allowOnlineAttendance();
            } else {
                throw new GroupFullException(subjectGroup.getId());
            }
        }

        // Set initial values
        if (enrollment.getStatus() == null) {
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
        }
        if (enrollment.getEnrollmentDate() == null) {
            enrollment.setEnrollmentDate(LocalDateTime.now());
        }

        // Save enrollment
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // Increment group occupancy if not online-only
        if (!enrollment.canAttendOnline()) {
            subjectGroup.incrementOccupancy();
            groupRepository.save(subjectGroup);
            log.info("Group {} occupancy incremented to {}/{}",
                    subjectGroup.getId(),
                    subjectGroup.getCurrentOccupancy(),
                    subjectGroup.getMaxCapacity());
        }

        log.info("Enrollment created successfully with ID: {}", savedEnrollment.getId());
        return savedEnrollment;
    }

    // ==================== CANCEL ====================

    @Override
    public void cancelEnrollment(Long enrollmentId, String reason) {
        log.info("Cancelling enrollment ID: {} with reason: {}", enrollmentId, reason);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // Validate enrollment can be cancelled
        if (enrollment.isCancelled()) {
            throw new EnrollmentCannotBeCancelledException(
                    enrollmentId, "Enrollment is already cancelled");
        }

        if (enrollment.isCompleted()) {
            throw new EnrollmentCannotBeCancelledException(
                    enrollmentId, "Completed enrollments cannot be cancelled");
        }

        // Cancel enrollment
        enrollment.cancel(reason);
        enrollmentRepository.save(enrollment);

        // Decrement group occupancy if not online-only
        if (!enrollment.canAttendOnline()) {
            SubjectGroup subjectGroup = enrollment.getSubjectGroup();
            subjectGroup.decrementOccupancy();
            groupRepository.save(subjectGroup);
            log.info("Group {} occupancy decremented to {}/{}",
                    subjectGroup.getId(),
                    subjectGroup.getCurrentOccupancy(),
                    subjectGroup.getMaxCapacity());
        }

        log.info("Enrollment {} cancelled successfully", enrollmentId);
    }

    // ==================== GET ====================

    @Override
    @Transactional(readOnly = true)
    public Enrollment getEnrollmentById(Long id) {
        log.debug("Getting enrollment by ID: {}", id);
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new EnrollmentNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        log.debug("Getting enrollments for student ID: {}", studentId);
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getEnrollmentsBySubjectGroup(Long subjectGroupId) {
        log.debug("Getting enrollments for subject group ID: {}", subjectGroupId);
        return enrollmentRepository.findBySubjectGroupId(subjectGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getActiveEnrollmentsByStudent(Long studentId) {
        log.debug("Getting active enrollments for student ID: {}", studentId);
        return enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getActiveEnrollmentsBySubjectGroup(Long subjectGroupId) {
        log.debug("Getting active enrollments for subject group ID: {}", subjectGroupId);
        return enrollmentRepository.findBySubjectGroupIdAndStatus(subjectGroupId, EnrollmentStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getEnrollmentsByStatus(EnrollmentStatus status) {
        log.debug("Getting enrollments by status: {}", status);
        return enrollmentRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getAllEnrollments() {
        log.debug("Getting all enrollments");
        return enrollmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveEnrollmentsByStudent(Long studentId) {
        log.debug("Counting active enrollments for student ID: {}", studentId);
        return enrollmentRepository.countActiveByStudentId(studentId);
    }

    // ==================== UPDATE STATUS ====================

    @Override
    public void updateEnrollmentStatus(Long enrollmentId, EnrollmentStatus newStatus, String reason) {
        log.info("Updating enrollment {} status to: {}", enrollmentId, newStatus);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        EnrollmentStatus oldStatus = enrollment.getStatus();

        // Validate status transition
        validateStatusTransition(oldStatus, newStatus);

        // Update status based on new value
        switch (newStatus) {
            case ACTIVE -> enrollment.activate();
            case CANCELLED -> enrollment.cancel(reason);
            case SUSPENDED -> enrollment.suspend(reason);
            case COMPLETED -> enrollment.complete();
            case PENDING -> enrollment.markAsPending();
            default -> throw new InvalidEnrollmentStatusException(
                    oldStatus.name(), newStatus.name());
        }

        enrollmentRepository.save(enrollment);
        log.info("Enrollment {} status updated from {} to {}",
                enrollmentId, oldStatus, newStatus);
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates that the student exists and has STUDENT role.
     */
    private User validateStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException(studentId));

        boolean hasStudentRole = student.getRoles().stream()
                .anyMatch(role -> role.getType() == RoleType.STUDENT);

        if (!hasStudentRole) {
            throw new IllegalArgumentException(
                    "User " + studentId + " does not have STUDENT role");
        }

        return student;
    }

    /**
     * Validates that the subject group exists and is active.
     */
    private SubjectGroup validateSubjectGroup(Long subjectGroupId) {
        SubjectGroup subjectGroup = groupRepository.findById(subjectGroupId)
                .orElseThrow(() -> new GroupNotFoundException(subjectGroupId));

        if (!subjectGroup.isActive()) {
            throw new IllegalArgumentException(
                    "Subject group " + subjectGroupId + " is not active");
        }

        return subjectGroup;
    }

    /**
     * Validates that a status transition is valid.
     */
    private void validateStatusTransition(EnrollmentStatus from, EnrollmentStatus to) {
        // Define valid transitions
        boolean isValid = switch (from) {
            case PENDING -> to == EnrollmentStatus.ACTIVE ||
                           to == EnrollmentStatus.CANCELLED;
            case ACTIVE -> to == EnrollmentStatus.SUSPENDED ||
                          to == EnrollmentStatus.CANCELLED ||
                          to == EnrollmentStatus.COMPLETED;
            case SUSPENDED -> to == EnrollmentStatus.ACTIVE ||
                             to == EnrollmentStatus.CANCELLED;
            case CANCELLED, COMPLETED -> false; // Terminal states
        };

        if (!isValid) {
            throw new InvalidEnrollmentStatusException(from.name(), to.name());
        }
    }
}
