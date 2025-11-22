package acainfo.back.shared.application.services.student;

import acainfo.back.attendance.application.ports.out.AttendanceRepositoryPort;
import acainfo.back.attendance.domain.model.AttendanceDomain;
import acainfo.back.attendance.domain.model.AttendanceStatus;
import acainfo.back.enrollment.application.services.EnrollmentService; // TODO: Replace with use cases/ports
import acainfo.back.enrollment.application.services.GroupRequestService; // TODO: Replace with use cases/ports
import acainfo.back.enrollment.domain.model.EnrollmentDomain;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.material.application.ports.out.MaterialRepositoryPort;
import acainfo.back.material.domain.model.MaterialDomain;
import acainfo.back.payment.application.services.PaymentService; // TODO: Replace with use cases/ports
import acainfo.back.payment.domain.model.Payment;
import acainfo.back.session.application.ports.out.SessionRepositoryPort;
import acainfo.back.session.domain.model.SessionDomain;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.user.domain.exception.UserNotFoundException;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.shared.infrastructure.adapters.in.dto.*;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository; // TODO: Create UserRepositoryPort in application layer
import acainfo.back.enrollment.infrastructure.adapters.in.dto.EnrollmentResponse;
import acainfo.back.payment.infrastructure.adapters.in.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for student-specific operations and dashboard aggregation.
 * Consolidates data from multiple modules to provide a unified student experience.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentService {

    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;
    private final GroupRequestService groupRequestService;
    private final PaymentService paymentService;
    private final AttendanceRepositoryPort attendanceRepository;
    private final SessionRepositoryPort sessionRepository;
    private final MaterialRepositoryPort materialRepository;

    /**
     * Get complete dashboard for a student.
     * Aggregates data from enrollments, payments, sessions, attendance, etc.
     *
     * @param studentId Student ID
     * @return Complete dashboard response
     */
    public StudentDashboardResponse getDashboard(Long studentId) {
        log.info("Building dashboard for student {}", studentId);

        // 1. Get student profile
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException(studentId));
        StudentProfileResponse profile = StudentProfileResponse.fromEntity(student);

        // 2. Get active enrollments
        List<EnrollmentDomain> activeEnrollments = enrollmentService.getActiveEnrollmentsByStudent(studentId);
        List<EnrollmentResponse> activeEnrollmentsDto = activeEnrollments.stream()
                .map(EnrollmentResponse::fromDomain)
                .collect(Collectors.toList());

        // 3. Get waiting enrollments
        List<EnrollmentDomain> waitingEnrollments = enrollmentService.getAllEnrollmentsByStudent(studentId).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.EN_ESPERA)
                .toList();
        List<EnrollmentResponse> waitingEnrollmentsDto = waitingEnrollments.stream()
                .map(EnrollmentResponse::fromDomain)
                .collect(Collectors.toList());

        // 4. Get pending payments
        List<Payment> pendingPayments = paymentService.getPendingPaymentsByStudent(studentId);
        List<PaymentResponse> pendingPaymentsDto = pendingPayments.stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
        BigDecimal totalPending = paymentService.calculateTotalPendingByStudent(studentId);
        boolean hasOverdue = paymentService.hasOverduePayments(studentId);

        // 5. Get upcoming sessions
        List<UpcomingSessionDTO> upcomingSessions = getUpcomingSessions(studentId, activeEnrollments);

        // 6. Get attendance summary
        AttendanceSummaryDTO attendanceSummary = getAttendanceSummary(studentId, activeEnrollments);

        // 8. Get counts
        int pendingGroupRequests = groupRequestService.getPendingRequestsByStudent(studentId);
        int newMaterials = countNewMaterials(activeEnrollments);

        return StudentDashboardResponse.builder()
                .profile(profile)
                .activeEnrollments(activeEnrollmentsDto)
                .waitingEnrollments(waitingEnrollmentsDto)
                .pendingPayments(pendingPaymentsDto)
                .totalPendingAmount(totalPending)
                .hasOverduePayments(hasOverdue)
                .upcomingSessions(upcomingSessions)
                .attendanceSummary(attendanceSummary)
                .alerts(generateAlerts(student, pendingPayments, waitingEnrollments, attendanceSummary))
                .activeEnrollmentsCount(activeEnrollments.size())
                .pendingGroupRequestsCount(pendingGroupRequests)
                .newMaterialsCount(newMaterials)
                .build();
    }

    /**
     * Get student profile.
     */
    public StudentProfileResponse getProfile(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException(studentId));
        return StudentProfileResponse.fromEntity(student);
    }

    /**
     * Update student profile.
     */
    @Transactional
    public StudentProfileResponse updateProfile(Long studentId, UpdateStudentProfileRequest request) {
        log.info("Updating profile for student {}", studentId);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException(studentId));

        // Update fields if provided
        if (request.getFirstName() != null) {
            student.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            student.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            student.setPhone(request.getPhone());
        }

        User updated = userRepository.save(student);
        return StudentProfileResponse.fromEntity(updated);
    }

    /**
     * Get upcoming sessions for student (next 7 days).
     */
    private List<UpcomingSessionDTO> getUpcomingSessions(Long studentId, List<EnrollmentDomain> enrollments) {
        List<UpcomingSessionDTO> upcoming = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekFromNow = now.plusDays(7);

        for (EnrollmentDomain enrollment : enrollments) {
            List<SessionDomain> sessions = sessionRepository.findBySubjectGroupIdAndScheduledStartBetween(
                    enrollment.getSubjectGroupId(),
                    now,
                    weekFromNow
            );

            for (SessionDomain session : sessions) {
                if (session.getStatus() != SessionStatus.CANCELADA) {
                    upcoming.add(mapSessionToDTO(session));
                }
            }
        }

        // Sort by start time
        upcoming.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));

        return upcoming;
    }

    /**
     * Map Session to UpcomingSessionDTO.
     * Note: This requires fetching related entities (SubjectGroup, Subject, Teacher, Classroom).
     * For production, consider using a DTO mapper with joins or caching.
     */
    private UpcomingSessionDTO mapSessionToDTO(SessionDomain session) {
        LocalDateTime now = LocalDateTime.now();
        long minutesUntil = ChronoUnit.MINUTES.between(now, session.getScheduledStart());

        // TODO: Fetch SubjectGroup, Subject, Teacher info via repositories/ports
        // For now, returning basic info from SessionDomain
        return UpcomingSessionDTO.builder()
                .sessionId(session.getId())
                .subjectName("") // TODO: Fetch via SubjectGroupRepositoryPort
                .subjectCode("") // TODO: Fetch via SubjectRepositoryPort
                .teacherName("N/A") // TODO: Fetch via UserRepository
                .startTime(session.getScheduledStart())
                .endTime(session.getScheduledEnd())
                .mode(session.getMode().name())
                .location(session.getClassroom() != null ? session.getClassroom().name() : "ONLINE")
                .zoomMeetingId(session.getZoomMeetingId())
                .minutesUntilStart(minutesUntil)
                .isToday(session.getScheduledStart().toLocalDate().equals(LocalDate.now()))
                .isImminent(minutesUntil > 0 && minutesUntil <= 60)
                .build();
    }

    /**
     * Get attendance summary for student.
     */
    private AttendanceSummaryDTO getAttendanceSummary(Long studentId, List<EnrollmentDomain> enrollments) {
        List<Long> enrollmentIds = enrollments.stream()
                .map(EnrollmentDomain::getId)
                .collect(Collectors.toList());

        if (enrollmentIds.isEmpty()) {
            return AttendanceSummaryDTO.empty();
        }

        List<AttendanceDomain> allAttendances = attendanceRepository.findByEnrollmentIdIn(enrollmentIds);

        int total = allAttendances.size();
        int attended = (int) allAttendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENTE).count();
        int absent = (int) allAttendances.stream().filter(a -> a.getStatus() == AttendanceStatus.AUSENTE).count();
        int late = (int) allAttendances.stream().filter(a -> a.getStatus() == AttendanceStatus.TARDANZA).count();
        int justified = (int) allAttendances.stream().filter(a -> a.getStatus() == AttendanceStatus.JUSTIFICADO).count();

        AttendanceSummaryDTO summary = AttendanceSummaryDTO.builder()
                .totalSessions(total)
                .attended(attended)
                .absent(absent)
                .late(late)
                .justified(justified)
                .build();

        summary.calculatePercentage();

        return summary;
    }

    /**
     * Generate alerts for student dashboard.
     * Provides intelligent notifications about payments, attendance, and enrollment status.
     */
    private List<AlertDTO> generateAlerts(
            User student,
            List<Payment> pendingPayments,
            List<EnrollmentDomain> waitingEnrollments,
            AttendanceSummaryDTO attendance
    ) {
        List<AlertDTO> alerts = new ArrayList<>();

        // Payment alerts
        for (Payment payment : pendingPayments) {
            if (payment.isOverdue()) {
                alerts.add(AlertDTO.error(
                    AlertDTO.AlertType.PAYMENT_OVERDUE,
                    String.format("Pago atrasado de %.2f EUR. Vencía el %s",
                        payment.getAmount(), payment.getDueDate())
                ).toBuilder().relatedId(payment.getId()).build());
            } else if (payment.getDaysOverdue() >= -3) {
                alerts.add(AlertDTO.warning(
                    AlertDTO.AlertType.PAYMENT_DUE,
                    String.format("Pago de %.2f EUR vence en %d días",
                        payment.getAmount(), Math.abs(payment.getDaysOverdue()))
                ).toBuilder().relatedId(payment.getId()).build());
            }
        }

        // Waiting queue alerts
        for (EnrollmentDomain waiting : waitingEnrollments) {
            alerts.add(AlertDTO.info(
                AlertDTO.AlertType.WAITING_QUEUE_POSITION,
                String.format("En lista de espera - Enrollment ID: %s",
                    waiting.getId())
                    // TODO: Fetch subject group and subject name via repositories
            ).toBuilder().relatedId(waiting.getId()).build());
        }

        // Attendance alert
        if (attendance.getAtRisk()) {
            alerts.add(AlertDTO.warning(
                    AlertDTO.AlertType.ATTENDANCE_LOW,
                    String.format("Tu asistencia es del %.1f%%. Necesitas al menos 75%%",
                            attendance.getAttendancePercentage())
            ));
        }

        return alerts;
    }

    /**
     * Count new materials (uploaded in last 7 days).
     */
    private int countNewMaterials(List<EnrollmentDomain> enrollments) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        int count = 0;

        for (EnrollmentDomain enrollment : enrollments) {
            List<MaterialDomain> materials = materialRepository
                    .findBySubjectGroupIdAndIsActiveTrue(enrollment.getSubjectGroupId());

            count += (int) materials.stream()
                    .filter(m -> m.getUploadDate().isAfter(Instant.from(weekAgo)))
                    .count();
        }

        return count;
    }
}
