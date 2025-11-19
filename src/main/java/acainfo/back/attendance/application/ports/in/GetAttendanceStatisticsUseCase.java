package acainfo.back.attendance.application.ports.in;

import acainfo.back.attendance.domain.model.AttendanceStatus;

import java.time.LocalDate;
import java.util.Map;

/**
 * Use case port for retrieving attendance statistics.
 * Defines the contract for attendance analytics and reporting operations.
 */
public interface GetAttendanceStatisticsUseCase {

    /**
     * Gets attendance statistics for a specific student.
     *
     * @param studentId the student ID
     * @return statistics including attendance rate, absences, etc.
     */
    StudentAttendanceStats getStudentAttendanceStats(Long studentId);

    /**
     * Gets attendance statistics for a student within a date range.
     *
     * @param query the query containing student ID and date range
     * @return statistics for the specified period
     */
    StudentAttendanceStats getStudentAttendanceStatsByDateRange(AttendanceStatsQuery query);

    /**
     * Gets attendance statistics for a subject group.
     *
     * @param groupId the subject group ID
     * @return statistics for the entire group
     */
    GroupAttendanceStats getGroupAttendanceStats(Long groupId);

    /**
     * Gets attendance statistics for a specific session.
     *
     * @param sessionId the session ID
     * @return statistics for the session
     */
    SessionAttendanceStats getSessionAttendanceStats(Long sessionId);

    /**
     * Checks if a student meets minimum attendance requirements.
     * Typically used for access control to materials or exams.
     *
     * @param studentId the student ID
     * @param groupId the subject group ID
     * @param minimumPercentage the minimum required attendance percentage
     * @return true if student meets requirements
     */
    boolean meetsAttendanceRequirements(Long studentId, Long groupId, double minimumPercentage);

    /**
     * Query object for statistics with date range
     */
    record AttendanceStatsQuery(
        Long studentId,
        LocalDate startDate,
        LocalDate endDate
    ) {}

    /**
     * Statistics for individual student attendance
     */
    record StudentAttendanceStats(
        Long studentId,
        long totalSessions,
        long presentCount,
        long absentCount,
        long tardanzaCount,
        long justifiedCount,
        double attendanceRate,      // Percentage of effective attendance (presente + tardanza)
        double absenceRate,          // Percentage of absences (ausente + justificado)
        int totalMinutesLate,
        boolean meetsMinimumRequirement  // Based on center's policy (e.g., 75%)
    ) {}

    /**
     * Statistics for subject group attendance
     */
    record GroupAttendanceStats(
        Long groupId,
        long totalSessions,
        long totalStudents,
        double averageAttendanceRate,
        Map<AttendanceStatus, Long> statusCounts,
        long sessionsWithFullAttendance,    // Sessions where all students attended
        long sessionsWithLowAttendance      // Sessions with < 60% attendance
    ) {}

    /**
     * Statistics for a specific session
     */
    record SessionAttendanceStats(
        Long sessionId,
        long totalStudents,
        long presentCount,
        long absentCount,
        long tardanzaCount,
        long justifiedCount,
        double attendanceRate,
        int averageMinutesLate
    ) {}
}
