package acainfo.back.attendance.application.services;

import acainfo.back.attendance.application.ports.in.*;
import acainfo.back.attendance.domain.exception.AttendanceAlreadyRegisteredException;
import acainfo.back.attendance.domain.exception.AttendanceNotFoundException;
import acainfo.back.attendance.domain.exception.InvalidAttendanceOperationException;
import acainfo.back.attendance.domain.model.Attendance;
import acainfo.back.attendance.domain.model.AttendanceStatus;
import acainfo.back.attendance.infrastructure.adapters.out.AttendanceRepository;
import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.exception.EnrollmentNotFoundException;
import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.session.domain.exception.SessionNotFoundException;
import acainfo.back.session.domain.model.Session;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.session.infrastructure.adapters.out.SessionRepository;
import acainfo.back.shared.domain.exception.UserNotFoundException;
import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementing all attendance use cases.
 * Handles attendance registration, updates, queries, and statistics.
 *
 * Business Rules:
 * - Attendance can only be registered for COMPLETADA sessions
 * - One attendance record per enrollment per session (no duplicates)
 * - Enrollment must be ACTIVO to register attendance
 * - Attendance can be modified within 7 days of recording
 * - Only AUSENTE status can be justified
 * - Statistics calculate effective attendance as PRESENTE + TARDANZA
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AttendanceService implements
    RegisterAttendanceUseCase,
    GetAttendanceUseCase,
    UpdateAttendanceUseCase,
    GetAttendanceStatisticsUseCase {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final UserRepository userRepository;

    // ==================== REGISTER ATTENDANCE ====================

    @Override
    public Attendance registerAttendance(RegisterAttendanceCommand command) {
        log.info("Registering attendance for student {} in session {}",
            command.studentId(), command.sessionId());

        // 1. Validate session exists and is completed
        Session session = validateSessionForAttendance(command.sessionId());

        // 2. Get enrollment for student in this subject group
        Enrollment enrollment = getActiveEnrollmentForSession(command.studentId(), session);

        // 3. Check for duplicate attendance
        if (attendanceRepository.existsBySessionIdAndEnrollmentId(
                command.sessionId(), enrollment.getId())) {
            throw new AttendanceAlreadyRegisteredException(
                command.sessionId(), command.studentId()
            );
        }

        // 4. Get user who is recording attendance
        User recordedBy = userRepository.findById(command.recordedById())
                .orElseThrow(() -> new UserNotFoundException(command.recordedById()));

        // 5. Parse and validate status
        AttendanceStatus status = parseAttendanceStatus(command.status());

        // 6. Build attendance record
        Attendance attendance = Attendance.builder()
            .session(session)
            .enrollment(enrollment)
            .status(status)
            .recordedAt(LocalDateTime.now())
            .recordedBy(recordedBy)
            .notes(command.notes())
            .build();

        // 7. Set minutes late if TARDANZA
        if (status == AttendanceStatus.TARDANZA && command.minutesLate() != null) {
            attendance.setMinutesLate(command.minutesLate());
        }

        // 8. Validate business rules
        validateAttendance(attendance);

        // 9. Save and return
        Attendance saved = attendanceRepository.save(attendance);
        log.info("Attendance registered successfully with ID: {}", saved.getId());

        return saved;
    }

    @Override
    public List<Attendance> registerBulkAttendance(RegisterBulkAttendanceCommand command) {
        log.info("Registering bulk attendance for session {} ({} students)",
            command.sessionId(), command.attendances().size());

        // 1. Validate session exists and is completed
        Session session = validateSessionForAttendance(command.sessionId());

        // 2. Get user who is recording attendance
        User recordedBy = userRepository.findById(command.recordedById())
                .orElseThrow(() -> new UserNotFoundException(command.recordedById()));

        // 3. Check for existing attendance records
        List<Attendance> existingAttendances = attendanceRepository
            .findBySessionId(command.sessionId());

        Set<Long> alreadyRecordedStudents = existingAttendances.stream()
            .map(a -> a.getEnrollment().getStudent().getId())
            .collect(Collectors.toSet());

        // 4. Filter out duplicates and warn
        List<StudentAttendanceData> validAttendances =
            command.attendances().stream()
                .filter(data -> {
                    if (alreadyRecordedStudents.contains(data.studentId())) {
                        log.warn("Skipping duplicate attendance for student {} in session {}",
                            data.studentId(), command.sessionId());
                        return false;
                    }
                    return true;
                })
                .toList();

        // 5. Create attendance records
        List<Attendance> attendances = new ArrayList<>();
        LocalDateTime recordedAt = LocalDateTime.now();

        for (StudentAttendanceData data : validAttendances) {
            try {
                // Get enrollment for this student
                Enrollment enrollment = getActiveEnrollmentForSession(data.studentId(), session);

                AttendanceStatus status = parseAttendanceStatus(data.status());

                Attendance attendance = Attendance.builder()
                    .session(session)
                    .enrollment(enrollment)
                    .status(status)
                    .recordedAt(recordedAt)
                    .recordedBy(recordedBy)
                    .notes(data.notes())
                    .build();

                // Set minutes late if TARDANZA
                if (status == AttendanceStatus.TARDANZA && data.minutesLate() != null) {
                    attendance.setMinutesLate(data.minutesLate());
                }

                validateAttendance(attendance);
                attendances.add(attendance);
            } catch (EnrollmentNotFoundException e) {
                log.warn("Skipping student {}: no active enrollment found for session {}",
                    data.studentId(), command.sessionId());
            }
        }

        // 6. Batch save
        List<Attendance> saved = attendanceRepository.saveAll(attendances);
        log.info("Bulk attendance registered: {} records created", saved.size());

        return saved;
    }

    // ==================== GET ATTENDANCE ====================

    @Override
    public List<Attendance> getAttendanceBySession(Long sessionId) {
        log.debug("Retrieving attendance for session {}", sessionId);

        // Validate session exists
        if (!sessionRepository.existsById(sessionId)) {
            throw new SessionNotFoundException(sessionId);
        }

        return attendanceRepository.findBySessionId(sessionId);
    }

    @Override
    public List<Attendance> getAttendanceHistoryByStudent(Long studentId) {
        log.debug("Retrieving attendance history for student {}", studentId);
        return attendanceRepository.findByStudentIdOrderByDate(studentId);
    }

    @Override
    public List<Attendance> getAttendanceHistoryByStudentAndDateRange(
            AttendanceHistoryQuery query) {
        log.debug("Retrieving attendance history for student {} from {} to {}",
            query.studentId(), query.startDate(), query.endDate());

        LocalDateTime startDateTime = query.startDate().atStartOfDay();
        LocalDateTime endDateTime = query.endDate().atTime(23, 59, 59);

        return attendanceRepository.findByStudentIdAndDateRange(
            query.studentId(), startDateTime, endDateTime
        );
    }

    @Override
    public List<Attendance> getAttendanceByGroup(Long groupId) {
        log.debug("Retrieving attendance for group {}", groupId);
        return attendanceRepository.findByGroupId(groupId);
    }

    @Override
    public Attendance getAttendanceById(Long attendanceId) {
        log.debug("Retrieving attendance by ID: {}", attendanceId);
        return attendanceRepository.findById(attendanceId)
            .orElseThrow(() -> new AttendanceNotFoundException(attendanceId));
    }

    @Override
    public Attendance getAttendanceBySessionAndStudent(Long sessionId, Long studentId) {
        log.debug("Retrieving attendance for session {} and student {}", sessionId, studentId);
        return attendanceRepository.findBySessionIdAndStudentId(sessionId, studentId)
            .orElseThrow(() -> new AttendanceNotFoundException(sessionId, studentId));
    }

    // ==================== UPDATE ATTENDANCE ====================

    @Override
    public Attendance updateAttendanceStatus(UpdateAttendanceStatusCommand command) {
        log.info("Updating attendance status for ID: {}", command.attendanceId());

        // 1. Fetch existing attendance
        Attendance attendance = attendanceRepository.findById(command.attendanceId())
            .orElseThrow(() -> new AttendanceNotFoundException(command.attendanceId()));

        // 2. Check if modification is allowed
        if (!attendance.canBeModified()) {
            throw InvalidAttendanceOperationException.modificationNotAllowed(
                command.attendanceId()
            );
        }

        // 3. Parse new status
        AttendanceStatus newStatus = parseAttendanceStatus(command.newStatus());

        // 4. Update status
        attendance.updateStatus(newStatus, command.notes());

        // 5. Save
        Attendance updated = attendanceRepository.save(attendance);
        log.info("Attendance status updated to {} for ID: {}", newStatus, updated.getId());

        return updated;
    }

    @Override
    public Attendance justifyAbsence(JustifyAbsenceCommand command) {
        log.info("Justifying absence for attendance ID: {}", command.attendanceId());

        // 1. Fetch existing attendance
        Attendance attendance = attendanceRepository.findById(command.attendanceId())
            .orElseThrow(() -> new AttendanceNotFoundException(command.attendanceId()));

        // 2. Get user who is justifying the absence
        User justifiedBy = userRepository.findById(command.justifiedById())
                .orElseThrow(() -> new UserNotFoundException(command.justifiedById()));

        // 3. Justify the absence (will throw exception if status is not AUSENTE)
        attendance.justify(justifiedBy, command.justificationReason());

        // 4. Save
        Attendance justified = attendanceRepository.save(attendance);
        log.info("Absence justified for attendance ID: {}", justified.getId());

        return justified;
    }

    @Override
    public Attendance markAsLate(MarkAsLateCommand command) {
        log.info("Marking attendance as late for ID: {}", command.attendanceId());

        // 1. Fetch existing attendance
        Attendance attendance = attendanceRepository.findById(command.attendanceId())
            .orElseThrow(() -> new AttendanceNotFoundException(command.attendanceId()));

        // 2. Check if modification is allowed
        if (!attendance.canBeModified()) {
            throw InvalidAttendanceOperationException.modificationNotAllowed(
                command.attendanceId()
            );
        }

        // 3. Mark as late
        attendance.markAsLate(command.minutesLate(), command.notes());

        // 4. Save
        Attendance updated = attendanceRepository.save(attendance);
        log.info("Attendance marked as late ({} minutes) for ID: {}",
            command.minutesLate(), updated.getId());

        return updated;
    }

    // ==================== STATISTICS ====================

    @Override
    public StudentAttendanceStats getStudentAttendanceStats(Long studentId) {
        log.debug("Calculating attendance statistics for student {}", studentId);

        List<Attendance> attendances = attendanceRepository.findByStudentId(studentId);
        return calculateStudentStats(studentId, attendances);
    }

    @Override
    public StudentAttendanceStats getStudentAttendanceStatsByDateRange(
            AttendanceStatsQuery query) {
        log.debug("Calculating attendance statistics for student {} from {} to {}",
            query.studentId(), query.startDate(), query.endDate());

        LocalDateTime startDateTime = query.startDate().atStartOfDay();
        LocalDateTime endDateTime = query.endDate().atTime(23, 59, 59);

        List<Attendance> attendances = attendanceRepository.findByStudentIdAndDateRange(
            query.studentId(), startDateTime, endDateTime
        );

        return calculateStudentStats(query.studentId(), attendances);
    }

    @Override
    public GroupAttendanceStats getGroupAttendanceStats(Long groupId) {
        log.debug("Calculating attendance statistics for group {}", groupId);

        // Get all attendance for the group
        List<Attendance> attendances = attendanceRepository.findByGroupId(groupId);

        if (attendances.isEmpty()) {
            return new GroupAttendanceStats(
                groupId, 0L, 0L, 0.0,
                new HashMap<>(), 0L, 0L
            );
        }

        // Count unique sessions and students
        long totalSessions = attendances.stream()
            .map(a -> a.getSession().getId())
            .distinct()
            .count();

        long totalStudents = attendances.stream()
            .map(a -> a.getEnrollment().getStudent().getId())
            .distinct()
            .count();

        // Calculate average attendance rate
        Double avgRate = attendanceRepository.calculateAttendanceRateForGroup(groupId);
        double averageAttendanceRate = avgRate != null ? avgRate : 0.0;

        // Count by status
        Map<AttendanceStatus, Long> statusCounts = attendances.stream()
            .collect(Collectors.groupingBy(
                Attendance::getStatus,
                Collectors.counting()
            ));

        // Sessions with full attendance (all students present or late)
        long sessionsWithFullAttendance = attendances.stream()
            .collect(Collectors.groupingBy(a -> a.getSession().getId()))
            .entrySet().stream()
            .filter(entry -> entry.getValue().stream()
                .allMatch(Attendance::countsAsEffectiveAttendance))
            .count();

        // Sessions with low attendance (< 60%)
        long sessionsWithLowAttendance = attendances.stream()
            .collect(Collectors.groupingBy(a -> a.getSession().getId()))
            .entrySet().stream()
            .filter(entry -> {
                long effective = entry.getValue().stream()
                    .filter(Attendance::countsAsEffectiveAttendance)
                    .count();
                long total = entry.getValue().size();
                double rate = (effective * 100.0) / total;
                return rate < 60.0;
            })
            .count();

        return new GroupAttendanceStats(
            groupId,
            totalSessions,
            totalStudents,
            averageAttendanceRate,
            statusCounts,
            sessionsWithFullAttendance,
            sessionsWithLowAttendance
        );
    }

    @Override
    public SessionAttendanceStats getSessionAttendanceStats(Long sessionId) {
        log.debug("Calculating attendance statistics for session {}", sessionId);

        // Validate session exists
        if (!sessionRepository.existsById(sessionId)) {
            throw new SessionNotFoundException(sessionId);
        }

        List<Attendance> attendances = attendanceRepository.findBySessionId(sessionId);

        if (attendances.isEmpty()) {
            return new SessionAttendanceStats(
                sessionId, 0L, 0L, 0L, 0L, 0L, 0.0, 0
            );
        }

        long totalStudents = attendances.size();
        long presentCount = attendances.stream()
            .filter(Attendance::isPresent)
            .count();
        long absentCount = attendances.stream()
            .filter(Attendance::isAbsent)
            .count();
        long tardanzaCount = attendances.stream()
            .filter(Attendance::wasLate)
            .count();
        long justifiedCount = attendances.stream()
            .filter(Attendance::isJustified)
            .count();

        double attendanceRate = ((presentCount + tardanzaCount) * 100.0) / totalStudents;

        // Calculate average minutes late
        Double avgMinutesLate = attendanceRepository
            .calculateAverageMinutesLateForSession(sessionId);
        int averageMinutesLate = avgMinutesLate != null ? avgMinutesLate.intValue() : 0;

        return new SessionAttendanceStats(
            sessionId,
            totalStudents,
            presentCount,
            absentCount,
            tardanzaCount,
            justifiedCount,
            attendanceRate,
            averageMinutesLate
        );
    }

    @Override
    public boolean meetsAttendanceRequirements(
            Long studentId, Long groupId, double minimumPercentage) {
        log.debug("Checking if student {} meets {}% attendance requirement in group {}",
            studentId, minimumPercentage, groupId);

        Double attendanceRate = attendanceRepository
            .calculateAttendanceRateForStudentInGroup(studentId, groupId);

        if (attendanceRate == null) {
            log.warn("No attendance records found for student {} in group {}",
                studentId, groupId);
            return false;
        }

        boolean meets = attendanceRate >= minimumPercentage;
        log.debug("Student {} attendance rate: {}% (required: {}%) - Meets: {}",
            studentId, attendanceRate, minimumPercentage, meets);

        return meets;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Gets the active enrollment for a student in the subject group of the given session.
     * Validates that the enrollment exists and is ACTIVO.
     *
     * @param studentId the student ID
     * @param session the session
     * @return the active enrollment
     * @throws EnrollmentNotFoundException if no active enrollment found
     * @throws InvalidAttendanceOperationException if enrollment is not active
     */
    private Enrollment getActiveEnrollmentForSession(Long studentId, Session session) {
        Long groupId = session.getSubjectGroup().getId();

        // Find enrollment for this student in this subject group
        Optional<Enrollment> enrollments = enrollmentRepository
                .findByStudentIdAndSubjectGroupId(studentId, groupId);

        if (enrollments.isEmpty()) {
            log.warn("No enrollment found for student {} in group {}", studentId, groupId);
            throw new EnrollmentNotFoundException(
                String.format("No enrollment found for student %d in group %d", studentId, groupId)
            );
        }

        // Find active enrollment
        Enrollment activeEnrollment = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVO)
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("No active enrollment found for student {} in group {}", studentId, groupId);
                    return new EnrollmentNotFoundException(
                        String.format("Student %d has no active enrollment in group %d", studentId, groupId)
                    );
                });

        log.debug("Found active enrollment {} for student {} in group {}",
            activeEnrollment.getId(), studentId, groupId);

        return activeEnrollment;
    }

    /**
     * Validates that a session exists and is in COMPLETADA status.
     * Attendance can only be registered for completed sessions.
     *
     * @param sessionId the session ID
     * @return the validated session
     * @throws SessionNotFoundException if session doesn't exist
     * @throws InvalidAttendanceOperationException if session is not completed
     */
    private Session validateSessionForAttendance(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (session.getStatus() != SessionStatus.COMPLETADA) {
            throw InvalidAttendanceOperationException.sessionNotCompleted(sessionId);
        }

        return session;
    }

    /**
     * Parses attendance status string to enum.
     * Validates that the status is valid.
     *
     * @param statusStr the status string
     * @return the parsed AttendanceStatus
     * @throws IllegalArgumentException if status is invalid
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

    /**
     * Validates attendance business rules.
     * Uses the entity's built-in validation methods.
     *
     * @param attendance the attendance to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateAttendance(Attendance attendance) {
        if (!attendance.isMinutesLateValid()) {
            throw new IllegalArgumentException(
                "Minutes late can only be set when status is TARDANZA"
            );
        }

        if (!attendance.isJustificationValid()) {
            throw new IllegalArgumentException(
                "Justification fields can only be set when status is JUSTIFICADO"
            );
        }
    }

    /**
     * Calculates detailed statistics for a student based on attendance records.
     *
     * @param studentId the student ID
     * @param attendances the list of attendance records
     * @return calculated statistics
     */
    private StudentAttendanceStats calculateStudentStats(
            Long studentId, List<Attendance> attendances) {

        if (attendances.isEmpty()) {
            return new StudentAttendanceStats(
                studentId, 0L, 0L, 0L, 0L, 0L,
                0.0, 0.0, 0, false
            );
        }

        long totalSessions = attendances.size();
        long presentCount = attendances.stream()
            .filter(Attendance::isPresent)
            .count();
        long absentCount = attendances.stream()
            .filter(Attendance::isAbsent)
            .count();
        long tardanzaCount = attendances.stream()
            .filter(Attendance::wasLate)
            .count();
        long justifiedCount = attendances.stream()
            .filter(Attendance::isJustified)
            .count();

        // Calculate rates
        double attendanceRate = ((presentCount + tardanzaCount) * 100.0) / totalSessions;
        double absenceRate = ((absentCount + justifiedCount) * 100.0) / totalSessions;

        // Calculate total minutes late
        int totalMinutesLate = attendances.stream()
            .filter(Attendance::wasLate)
            .mapToInt(a -> a.getMinutesLate() != null ? a.getMinutesLate() : 0)
            .sum();

        // Check minimum requirement (typically 75%)
        boolean meetsMinimum = attendanceRate >= 75.0;

        return new StudentAttendanceStats(
            studentId,
            totalSessions,
            presentCount,
            absentCount,
            tardanzaCount,
            justifiedCount,
            attendanceRate,
            absenceRate,
            totalMinutesLate,
            meetsMinimum
        );
    }
}
