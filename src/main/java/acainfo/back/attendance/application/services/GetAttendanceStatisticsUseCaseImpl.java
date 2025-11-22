package acainfo.back.attendance.application.services;

import acainfo.back.attendance.application.ports.in.GetAttendanceStatisticsUseCase;
import acainfo.back.attendance.application.ports.out.AttendanceRepositoryPort;
import acainfo.back.attendance.domain.model.AttendanceDomain;
import acainfo.back.attendance.domain.model.AttendanceStatus;
import acainfo.back.session.application.ports.out.SessionRepositoryPort;
import acainfo.back.session.domain.exception.SessionNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Use Case Implementation: Get Attendance Statistics
 * Application layer - implements attendance analytics and reporting operations
 *
 * Calculates various statistics:
 * - Student attendance rates and totals
 * - Group attendance averages and status distributions
 * - Session attendance rates
 * - Attendance requirement compliance
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GetAttendanceStatisticsUseCaseImpl implements GetAttendanceStatisticsUseCase {

    private final AttendanceRepositoryPort attendanceRepository;
    private final SessionRepositoryPort sessionRepository;

    @Override
    public StudentAttendanceStats getStudentAttendanceStats(Long studentId) {
        log.debug("Calculating attendance statistics for student {}", studentId);

        List<AttendanceDomain> attendances = attendanceRepository.findByStudentId(studentId);
        return calculateStudentStats(studentId, attendances);
    }

    @Override
    public StudentAttendanceStats getStudentAttendanceStatsByDateRange(
            AttendanceStatsQuery query) {
        log.debug("Calculating attendance statistics for student {} from {} to {}",
            query.studentId(), query.startDate(), query.endDate());

        LocalDateTime startDateTime = query.startDate().atStartOfDay();
        LocalDateTime endDateTime = query.endDate().atTime(23, 59, 59);

        List<AttendanceDomain> attendances = attendanceRepository.findByStudentIdAndDateRange(
            query.studentId(), startDateTime, endDateTime
        );

        return calculateStudentStats(query.studentId(), attendances);
    }

    @Override
    public GroupAttendanceStats getGroupAttendanceStats(Long groupId) {
        log.debug("Calculating attendance statistics for group {}", groupId);

        // Get all attendance for the group
        List<AttendanceDomain> attendances = attendanceRepository.findByGroupId(groupId);

        if (attendances.isEmpty()) {
            return new GroupAttendanceStats(
                groupId, 0L, 0L, 0.0,
                new HashMap<>(), 0L, 0L
            );
        }

        // Count unique sessions and students (enrollments)
        long totalSessions = attendances.stream()
            .map(AttendanceDomain::getSessionId)
            .distinct()
            .count();

        long totalStudents = attendances.stream()
            .map(AttendanceDomain::getEnrollmentId)
            .distinct()
            .count();

        // Calculate average attendance rate
        Double avgRate = attendanceRepository.calculateAttendanceRateForGroup(groupId);
        double averageAttendanceRate = avgRate != null ? avgRate : 0.0;

        // Count by status
        Map<AttendanceStatus, Long> statusCounts = attendances.stream()
            .collect(Collectors.groupingBy(
                AttendanceDomain::getStatus,
                Collectors.counting()
            ));

        // Sessions with full attendance (all students present or late)
        long sessionsWithFullAttendance = attendances.stream()
            .collect(Collectors.groupingBy(AttendanceDomain::getSessionId))
            .entrySet().stream()
            .filter(entry -> entry.getValue().stream()
                .allMatch(AttendanceDomain::countsAsEffectiveAttendance))
            .count();

        // Sessions with low attendance (< 60%)
        long sessionsWithLowAttendance = attendances.stream()
            .collect(Collectors.groupingBy(AttendanceDomain::getSessionId))
            .entrySet().stream()
            .filter(entry -> {
                long effective = entry.getValue().stream()
                    .filter(AttendanceDomain::countsAsEffectiveAttendance)
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

        List<AttendanceDomain> attendances = attendanceRepository.findBySessionId(sessionId);

        if (attendances.isEmpty()) {
            return new SessionAttendanceStats(
                sessionId, 0L, 0L, 0L, 0L, 0L, 0.0, 0
            );
        }

        long totalStudents = attendances.size();
        long presentCount = attendances.stream()
            .filter(AttendanceDomain::isPresent)
            .count();
        long absentCount = attendances.stream()
            .filter(AttendanceDomain::isAbsent)
            .count();
        long tardanzaCount = attendances.stream()
            .filter(AttendanceDomain::wasLate)
            .count();
        long justifiedCount = attendances.stream()
            .filter(AttendanceDomain::isJustified)
            .count();

        double attendanceRate = ((presentCount + tardanzaCount) * 100.0) / totalStudents;

        // Calculate average minutes late
        Integer totalMinutesLate = attendanceRepository
            .calculateTotalMinutesLateForStudent(attendances.get(0).getEnrollmentId());

        // Better approach: calculate from the attendances we have
        int sumMinutesLate = attendances.stream()
            .filter(AttendanceDomain::wasLate)
            .mapToInt(a -> a.getMinutesLate() != null ? a.getMinutesLate() : 0)
            .sum();

        int averageMinutesLate = tardanzaCount > 0 ? (sumMinutesLate / (int) tardanzaCount) : 0;

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
     * Calculates detailed statistics for a student based on attendance records.
     */
    private StudentAttendanceStats calculateStudentStats(
            Long studentId, List<AttendanceDomain> attendances) {

        if (attendances.isEmpty()) {
            return new StudentAttendanceStats(
                studentId, 0L, 0L, 0L, 0L, 0L,
                0.0, 0.0, 0, false
            );
        }

        long totalSessions = attendances.size();
        long presentCount = attendances.stream()
            .filter(AttendanceDomain::isPresent)
            .count();
        long absentCount = attendances.stream()
            .filter(AttendanceDomain::isAbsent)
            .count();
        long tardanzaCount = attendances.stream()
            .filter(AttendanceDomain::wasLate)
            .count();
        long justifiedCount = attendances.stream()
            .filter(AttendanceDomain::isJustified)
            .count();

        // Calculate rates
        double attendanceRate = ((presentCount + tardanzaCount) * 100.0) / totalSessions;
        double absenceRate = ((absentCount + justifiedCount) * 100.0) / totalSessions;

        // Calculate total minutes late
        int totalMinutesLate = attendances.stream()
            .filter(AttendanceDomain::wasLate)
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
