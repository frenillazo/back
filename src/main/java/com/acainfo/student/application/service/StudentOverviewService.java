package com.acainfo.student.application.service;

import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.course.domain.model.Course;
import com.acainfo.reservation.application.port.out.ReservationRepositoryPort;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.model.Session;
import com.acainfo.student.application.dto.StudentOverviewResponse;
import com.acainfo.student.application.dto.StudentOverviewResponse.EnrollmentSummary;
import com.acainfo.student.application.dto.StudentOverviewResponse.UpcomingSessionSummary;
import com.acainfo.student.application.port.in.GetStudentOverviewUseCase;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.exception.UserNotFoundException;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
public class StudentOverviewService implements GetStudentOverviewUseCase {

    private static final int DEFAULT_UPCOMING_SESSIONS_LIMIT = 5;

    private final UserRepositoryPort userRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final CourseRepositoryPort courseRepository;
    private final SubjectRepositoryPort subjectRepository;
    private final SessionRepositoryPort sessionRepository;
    private final ReservationRepositoryPort reservationRepository;

    /**
     * Get overview for a student.
     *
     * @param studentId Student ID
     * @param upcomingSessionsLimit Maximum number of upcoming sessions to return
     * @return Aggregated overview response
     */
    @Override
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

        return new StudentOverviewResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                enrollmentSummaries,
                waitingList.size(),
                upcomingSessions
        );
    }

    /**
     * Get overview with default upcoming sessions limit.
     */
    @Override
    public StudentOverviewResponse getOverview(Long studentId) {
        return getOverview(studentId, DEFAULT_UPCOMING_SESSIONS_LIMIT);
    }

    private List<EnrollmentSummary> buildEnrollmentSummaries(List<Enrollment> enrollments) {
        if (enrollments.isEmpty()) {
            return List.of();
        }

        // Collect all group IDs
        List<Long> courseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .toList();

        // Batch load groups
        Map<Long, Course> coursesById = courseRepository.findByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        // Collect all subject IDs from groups
        Set<Long> subjectIds = coursesById.values().stream()
                .map(Course::getSubjectId)
                .collect(Collectors.toSet());

        // Batch load subjects
        Map<Long, Subject> subjectsById = subjectRepository.findByIds(subjectIds.stream().toList()).stream()
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        // Collect all teacher IDs from groups
        Set<Long> teacherIds = coursesById.values().stream()
                .map(Course::getTeacherId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // Batch load teachers
        Map<Long, User> teachersById = teacherIds.stream()
                .map(id -> userRepository.findById(id).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Build summaries
        return enrollments.stream()
                .map(enrollment -> {
                    Course course = coursesById.get(enrollment.getCourseId());
                    Subject subject = course != null ? subjectsById.get(course.getSubjectId()) : null;
                    User teacher = course != null ? teachersById.get(course.getTeacherId()) : null;

                    return new EnrollmentSummary(
                            enrollment.getId(),
                            enrollment.getCourseId(),
                            subject != null ? subject.getName() : "Unknown",
                            subject != null ? subject.getCode() : null,
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
        List<Long> courseIds = activeEnrollments.stream()
                .map(Enrollment::getCourseId)
                .toList();

        // Get upcoming sessions for all groups
        List<Session> upcomingSessions = sessionRepository
                .findUpcomingByCourseIds(courseIds, LocalDate.now(), limit);

        if (upcomingSessions.isEmpty()) {
            return List.of();
        }

        // Batch load groups
        Set<Long> sessionCourseIds = upcomingSessions.stream()
                .map(Session::getCourseId)
                .collect(Collectors.toSet());
        Map<Long, Course> coursesById = courseRepository.findByIds(sessionCourseIds.stream().toList()).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        // Batch load subjects
        Set<Long> subjectIds = coursesById.values().stream()
                .map(Course::getSubjectId)
                .collect(Collectors.toSet());
        Map<Long, Subject> subjectsById = subjectRepository.findByIds(subjectIds.stream().toList()).stream()
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        // Get confirmed reservations for the student (exclude cancelled)
        List<SessionReservation> studentReservations = reservationRepository.findByStudentId(studentId);
        Set<Long> reservedSessionIds = studentReservations.stream()
                .filter(SessionReservation::isConfirmed)
                .map(SessionReservation::getSessionId)
                .collect(Collectors.toSet());

        // Build enrollment lookup by courseId for enriching with enrollmentId
        Map<Long, Long> courseIdToEnrollmentId = activeEnrollments.stream()
                .collect(Collectors.toMap(Enrollment::getCourseId, Enrollment::getId, (a, b) -> a));

        // Build summaries
        return upcomingSessions.stream()
                .map(session -> {
                    Course course = coursesById.get(session.getCourseId());
                    Subject subject = course != null ? subjectsById.get(course.getSubjectId()) : null;

                    return new UpcomingSessionSummary(
                            session.getId(),
                            session.getCourseId(),
                            courseIdToEnrollmentId.get(session.getCourseId()),
                            subject != null ? subject.getName() : "Unknown",
                            subject != null ? subject.getCode() : null,
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

}
