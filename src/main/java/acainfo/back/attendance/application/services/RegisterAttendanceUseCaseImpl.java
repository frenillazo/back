package acainfo.back.attendance.application.services;

import acainfo.back.attendance.application.ports.in.RegisterAttendanceUseCase;
import acainfo.back.attendance.application.ports.out.AttendanceRepositoryPort;
import acainfo.back.attendance.domain.exception.AttendanceAlreadyRegisteredException;
import acainfo.back.attendance.domain.exception.InvalidAttendanceOperationException;
import acainfo.back.attendance.domain.model.AttendanceDomain;
import acainfo.back.attendance.domain.model.AttendanceStatus;
import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.exception.EnrollmentNotFoundException;
import acainfo.back.enrollment.domain.model.EnrollmentDomain;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.session.application.ports.out.SessionRepositoryPort;
import acainfo.back.session.domain.exception.SessionNotFoundException;
import acainfo.back.session.domain.model.SessionDomain;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.shared.domain.exception.UserNotFoundException;
import acainfo.back.shared.infrastructure.adapters.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use Case Implementation: Register Attendance
 * Application layer - implements attendance registration operations
 *
 * Business Rules:
 * - Attendance can only be registered for COMPLETADA sessions
 * - One attendance record per enrollment per session (no duplicates)
 * - Enrollment must be ACTIVO to register attendance
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RegisterAttendanceUseCaseImpl implements RegisterAttendanceUseCase {

    private final AttendanceRepositoryPort attendanceRepository;
    private final SessionRepositoryPort sessionRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final UserRepository userRepository;

    @Override
    public AttendanceDomain registerAttendance(RegisterAttendanceCommand command) {
        log.info("Registering attendance for student {} in session {}",
            command.studentId(), command.sessionId());

        // 1. Validate session exists and is completed
        SessionDomain session = validateSessionForAttendance(command.sessionId());

        // 2. Get enrollment for student in this subject group
        EnrollmentDomain enrollment = getActiveEnrollmentForSession(command.studentId(), session);

        // 3. Check for duplicate attendance
        if (attendanceRepository.existsBySessionIdAndEnrollmentId(
                command.sessionId(), enrollment.getId())) {
            throw new AttendanceAlreadyRegisteredException(
                command.sessionId(), command.studentId()
            );
        }

        // 4. Validate user exists
        if (!userRepository.existsById(command.recordedById())) {
            throw new UserNotFoundException(command.recordedById());
        }

        // 5. Parse and validate status
        AttendanceStatus status = parseAttendanceStatus(command.status());

        // 6. Build attendance domain object
        AttendanceDomain.AttendanceDomainBuilder builder = AttendanceDomain.builder()
            .sessionId(session.getId())
            .enrollmentId(enrollment.getId())
            .status(status)
            .recordedAt(LocalDateTime.now())
            .recordedById(command.recordedById())
            .notes(command.notes());

        // 7. Set minutes late if TARDANZA
        if (status == AttendanceStatus.TARDANZA && command.minutesLate() != null) {
            builder.minutesLate(command.minutesLate());
        }

        AttendanceDomain attendance = builder.build();

        // 8. Validate business rules
        attendance.validate();

        // 9. Save and return
        AttendanceDomain saved = attendanceRepository.save(attendance);
        log.info("Attendance registered successfully with ID: {}", saved.getId());

        return saved;
    }

    @Override
    public List<AttendanceDomain> registerBulkAttendance(RegisterBulkAttendanceCommand command) {
        log.info("Registering bulk attendance for session {} ({} students)",
            command.sessionId(), command.attendances().size());

        // 1. Validate session exists and is completed
        SessionDomain session = validateSessionForAttendance(command.sessionId());

        // 2. Validate user exists
        if (!userRepository.existsById(command.recordedById())) {
            throw new UserNotFoundException(command.recordedById());
        }

        // 3. Check for existing attendance records
        List<AttendanceDomain> existingAttendances = attendanceRepository
            .findBySessionId(command.sessionId());

        Set<Long> alreadyRecordedEnrollmentIds = existingAttendances.stream()
            .map(AttendanceDomain::getEnrollmentId)
            .collect(Collectors.toSet());

        // 4. Create attendance records
        List<AttendanceDomain> attendances = new ArrayList<>();
        LocalDateTime recordedAt = LocalDateTime.now();

        for (StudentAttendanceData data : command.attendances()) {
            try {
                // Get enrollment for this student
                EnrollmentDomain enrollment = getActiveEnrollmentForSession(data.studentId(), session);

                // Skip if already recorded
                if (alreadyRecordedEnrollmentIds.contains(enrollment.getId())) {
                    log.warn("Skipping duplicate attendance for student {} in session {}",
                        data.studentId(), command.sessionId());
                    continue;
                }

                AttendanceStatus status = parseAttendanceStatus(data.status());

                AttendanceDomain.AttendanceDomainBuilder builder = AttendanceDomain.builder()
                    .sessionId(session.getId())
                    .enrollmentId(enrollment.getId())
                    .status(status)
                    .recordedAt(recordedAt)
                    .recordedById(command.recordedById())
                    .notes(data.notes());

                // Set minutes late if TARDANZA
                if (status == AttendanceStatus.TARDANZA && data.minutesLate() != null) {
                    builder.minutesLate(data.minutesLate());
                }

                AttendanceDomain attendance = builder.build();
                attendance.validate();
                attendances.add(attendance);

            } catch (EnrollmentNotFoundException e) {
                log.warn("Skipping student {}: no active enrollment found for session {}",
                    data.studentId(), command.sessionId());
            }
        }

        // 5. Batch save
        List<AttendanceDomain> saved = new ArrayList<>();
        for (AttendanceDomain attendance : attendances) {
            saved.add(attendanceRepository.save(attendance));
        }

        log.info("Bulk attendance registered: {} records created", saved.size());
        return saved;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Gets the active enrollment for a student in the subject group of the given session.
     * Validates that the enrollment exists and is ACTIVO.
     */
    private EnrollmentDomain getActiveEnrollmentForSession(Long studentId, SessionDomain session) {
        Long groupId = session.getSubjectGroupId();

        // Find enrollment for this student in this subject group
        return enrollmentRepository.findByStudentIdAndSubjectGroupId(studentId, groupId)
            .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVO)
            .orElseThrow(() -> {
                log.warn("No active enrollment found for student {} in group {}", studentId, groupId);
                return new EnrollmentNotFoundException(
                    String.format("Student %d has no active enrollment in group %d", studentId, groupId)
                );
            });
    }

    /**
     * Validates that a session exists and is in COMPLETADA status.
     * Attendance can only be registered for completed sessions.
     */
    private SessionDomain validateSessionForAttendance(Long sessionId) {
        SessionDomain session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (session.getStatus() != SessionStatus.COMPLETADA) {
            throw InvalidAttendanceOperationException.sessionNotCompleted(sessionId);
        }

        return session;
    }

    /**
     * Parses attendance status string to enum.
     */
    private AttendanceStatus parseAttendanceStatus(String statusStr) {
        try {
            return AttendanceStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid attendance status: " + statusStr +
                ". Must be one of: PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO"
            );
        }
    }
}
