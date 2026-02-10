package com.acainfo.student.application.service;

import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.payment.application.port.out.PaymentRepositoryPort;
import com.acainfo.payment.domain.model.Payment;
import com.acainfo.payment.domain.model.PaymentStatus;
import com.acainfo.reservation.application.port.out.ReservationRepositoryPort;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.model.Session;
import com.acainfo.student.application.dto.StudentOverviewResponse;
import com.acainfo.student.application.dto.StudentOverviewResponse.EnrollmentSummary;
import com.acainfo.student.application.dto.StudentOverviewResponse.PaymentSummary;
import com.acainfo.student.application.dto.StudentOverviewResponse.UpcomingSessionSummary;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.exception.UserNotFoundException;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for aggregating student dashboard data.
 * Combines data from multiple modules into a single overview response.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentOverviewService {

    private static final int DEFAULT_UPCOMING_SESSIONS_LIMIT = 5;

    private final UserRepositoryPort userRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final GroupRepositoryPort groupRepository;
    private final SubjectRepositoryPort subjectRepository;
    private final SessionRepositoryPort sessionRepository;
    private final ReservationRepositoryPort reservationRepository;
    private final PaymentRepositoryPort paymentRepository;

    /**
     * Get overview for a student.
     *
     * @param studentId Student ID
     * @param upcomingSessionsLimit Maximum number of upcoming sessions to return
     * @return Aggregated overview response
     */
    public StudentOverviewResponse getOverview(Long studentId, int upcomingSessionsLimit) {
        log.debug("Building overview for student: {}", studentId);

        // 1. Get user profile
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException(studentId));

        // 2. Get active enrollments
        List<Enrollment> activeEnrollments = enrollmentRepository
                .findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE);

        // 3. Get waiting list count
        List<Enrollment> waitingList = enrollmentRepository
                .findByStudentIdAndStatus(studentId, EnrollmentStatus.WAITING_LIST);

        // 4. Build enrollment summaries with related entity names
        List<EnrollmentSummary> enrollmentSummaries = buildEnrollmentSummaries(activeEnrollments);

        // 5. Get upcoming sessions
        List<UpcomingSessionSummary> upcomingSessions = buildUpcomingSessionsSummaries(
                studentId, activeEnrollments, upcomingSessionsLimit);

        // 6. Build payment summary
        PaymentSummary paymentSummary = buildPaymentSummary(studentId);

        return new StudentOverviewResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                enrollmentSummaries,
                waitingList.size(),
                upcomingSessions,
                paymentSummary
        );
    }

    /**
     * Get overview with default upcoming sessions limit.
     */
    public StudentOverviewResponse getOverview(Long studentId) {
        return getOverview(studentId, DEFAULT_UPCOMING_SESSIONS_LIMIT);
    }

    private List<EnrollmentSummary> buildEnrollmentSummaries(List<Enrollment> enrollments) {
        if (enrollments.isEmpty()) {
            return List.of();
        }

        // Collect all group IDs
        List<Long> groupIds = enrollments.stream()
                .map(Enrollment::getGroupId)
                .toList();

        // Batch load groups
        Map<Long, SubjectGroup> groupsById = groupRepository.findByIds(groupIds).stream()
                .collect(Collectors.toMap(SubjectGroup::getId, Function.identity()));

        // Collect all subject IDs from groups
        Set<Long> subjectIds = groupsById.values().stream()
                .map(SubjectGroup::getSubjectId)
                .collect(Collectors.toSet());

        // Batch load subjects
        Map<Long, Subject> subjectsById = subjectRepository.findByIds(subjectIds.stream().toList()).stream()
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        // Collect all teacher IDs from groups
        Set<Long> teacherIds = groupsById.values().stream()
                .map(SubjectGroup::getTeacherId)
                .collect(Collectors.toSet());

        // Batch load teachers
        Map<Long, User> teachersById = teacherIds.stream()
                .map(id -> userRepository.findById(id).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Build summaries
        return enrollments.stream()
                .map(enrollment -> {
                    SubjectGroup group = groupsById.get(enrollment.getGroupId());
                    Subject subject = group != null ? subjectsById.get(group.getSubjectId()) : null;
                    User teacher = group != null ? teachersById.get(group.getTeacherId()) : null;

                    return new EnrollmentSummary(
                            enrollment.getId(),
                            enrollment.getGroupId(),
                            subject != null ? subject.getName() : "Unknown",
                            subject != null ? subject.getCode() : null,
                            group != null ? group.getType().name() : null,
                            teacher != null ? teacher.getFullName() : "Unknown",
                            enrollment.getEnrolledAt()
                    );
                })
                .toList();
    }

    private List<UpcomingSessionSummary> buildUpcomingSessionsSummaries(
            Long studentId,
            List<Enrollment> activeEnrollments,
            int limit) {

        if (activeEnrollments.isEmpty()) {
            return List.of();
        }

        // Get group IDs from enrollments
        List<Long> groupIds = activeEnrollments.stream()
                .map(Enrollment::getGroupId)
                .toList();

        // Get upcoming sessions for all groups
        List<Session> upcomingSessions = sessionRepository
                .findUpcomingByGroupIds(groupIds, LocalDate.now(), limit);

        if (upcomingSessions.isEmpty()) {
            return List.of();
        }

        // Batch load groups
        Set<Long> sessionGroupIds = upcomingSessions.stream()
                .map(Session::getGroupId)
                .collect(Collectors.toSet());
        Map<Long, SubjectGroup> groupsById = groupRepository.findByIds(sessionGroupIds.stream().toList()).stream()
                .collect(Collectors.toMap(SubjectGroup::getId, Function.identity()));

        // Batch load subjects
        Set<Long> subjectIds = groupsById.values().stream()
                .map(SubjectGroup::getSubjectId)
                .collect(Collectors.toSet());
        Map<Long, Subject> subjectsById = subjectRepository.findByIds(subjectIds.stream().toList()).stream()
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        // Get confirmed reservations for the student (exclude cancelled)
        List<SessionReservation> studentReservations = reservationRepository.findByStudentId(studentId);
        Set<Long> reservedSessionIds = studentReservations.stream()
                .filter(SessionReservation::isConfirmed)
                .map(SessionReservation::getSessionId)
                .collect(Collectors.toSet());

        // Build enrollment lookup by groupId for enriching with enrollmentId
        Map<Long, Long> groupIdToEnrollmentId = activeEnrollments.stream()
                .collect(Collectors.toMap(Enrollment::getGroupId, Enrollment::getId, (a, b) -> a));

        // Build summaries
        return upcomingSessions.stream()
                .map(session -> {
                    SubjectGroup group = groupsById.get(session.getGroupId());
                    Subject subject = group != null ? subjectsById.get(group.getSubjectId()) : null;

                    return new UpcomingSessionSummary(
                            session.getId(),
                            session.getGroupId(),
                            groupIdToEnrollmentId.get(session.getGroupId()),
                            subject != null ? subject.getName() : "Unknown",
                            subject != null ? subject.getCode() : null,
                            group != null ? group.getType().name() : null,
                            session.getDate(),
                            session.getStartTime(),
                            session.getEndTime(),
                            session.getClassroom() != null ? session.getClassroom().getDisplayName() : null,
                            session.getStatus().name(),
                            reservedSessionIds.contains(session.getId())
                    );
                })
                .toList();
    }

    private PaymentSummary buildPaymentSummary(Long studentId) {
        // Get pending payments
        List<Payment> pendingPayments = paymentRepository
                .findByStudentIdAndStatus(studentId, PaymentStatus.PENDING);

        // Check for overdue
        boolean hasOverdue = paymentRepository.hasOverduePayments(studentId, LocalDate.now());

        // Calculate totals
        int pendingCount = pendingPayments.size();
        BigDecimal totalPending = pendingPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Find next due date
        LocalDate nextDueDate = pendingPayments.stream()
                .map(Payment::getDueDate)
                .filter(d -> d != null)
                .min(Comparator.naturalOrder())
                .orElse(null);

        // Can access resources = no overdue payments
        boolean canAccess = !hasOverdue;

        return new PaymentSummary(
                canAccess,
                hasOverdue,
                pendingCount,
                totalPending,
                nextDueDate
        );
    }
}
