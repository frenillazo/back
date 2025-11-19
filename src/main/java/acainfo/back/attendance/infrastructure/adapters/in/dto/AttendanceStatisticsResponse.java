package acainfo.back.attendance.infrastructure.adapters.in.dto;

import acainfo.back.attendance.application.ports.in.GetAttendanceStatisticsUseCase.*;
import acainfo.back.attendance.domain.model.AttendanceStatus;
import lombok.Builder;

import java.util.Map;

/**
 * DTOs for attendance statistics responses.
 */
public class AttendanceStatisticsResponse {

    /**
     * Response for student attendance statistics
     */
    @Builder
    public record StudentStats(
        Long studentId,
        long totalSessions,
        long presentCount,
        long absentCount,
        long tardanzaCount,
        long justifiedCount,
        double attendanceRate,
        double absenceRate,
        int totalMinutesLate,
        boolean meetsMinimumRequirement
    ) {
        /**
         * Converts from use case result to DTO
         */
        public static StudentStats fromUseCaseResult(StudentAttendanceStats stats) {
            return StudentStats.builder()
                .studentId(stats.studentId())
                .totalSessions(stats.totalSessions())
                .presentCount(stats.presentCount())
                .absentCount(stats.absentCount())
                .tardanzaCount(stats.tardanzaCount())
                .justifiedCount(stats.justifiedCount())
                .attendanceRate(stats.attendanceRate())
                .absenceRate(stats.absenceRate())
                .totalMinutesLate(stats.totalMinutesLate())
                .meetsMinimumRequirement(stats.meetsMinimumRequirement())
                .build();
        }
    }

    /**
     * Response for group attendance statistics
     */
    @Builder
    public record GroupStats(
        Long groupId,
        long totalSessions,
        long totalStudents,
        double averageAttendanceRate,
        Map<AttendanceStatus, Long> statusCounts,
        long sessionsWithFullAttendance,
        long sessionsWithLowAttendance
    ) {
        /**
         * Converts from use case result to DTO
         */
        public static GroupStats fromUseCaseResult(GroupAttendanceStats stats) {
            return GroupStats.builder()
                .groupId(stats.groupId())
                .totalSessions(stats.totalSessions())
                .totalStudents(stats.totalStudents())
                .averageAttendanceRate(stats.averageAttendanceRate())
                .statusCounts(stats.statusCounts())
                .sessionsWithFullAttendance(stats.sessionsWithFullAttendance())
                .sessionsWithLowAttendance(stats.sessionsWithLowAttendance())
                .build();
        }
    }

    /**
     * Response for session attendance statistics
     */
    @Builder
    public record SessionStats(
        Long sessionId,
        long totalStudents,
        long presentCount,
        long absentCount,
        long tardanzaCount,
        long justifiedCount,
        double attendanceRate,
        int averageMinutesLate
    ) {
        /**
         * Converts from use case result to DTO
         */
        public static SessionStats fromUseCaseResult(SessionAttendanceStats stats) {
            return SessionStats.builder()
                .sessionId(stats.sessionId())
                .totalStudents(stats.totalStudents())
                .presentCount(stats.presentCount())
                .absentCount(stats.absentCount())
                .tardanzaCount(stats.tardanzaCount())
                .justifiedCount(stats.justifiedCount())
                .attendanceRate(stats.attendanceRate())
                .averageMinutesLate(stats.averageMinutesLate())
                .build();
        }
    }
}
