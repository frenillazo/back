package acainfo.back.attendance.application.ports.in;

import acainfo.back.attendance.domain.model.Attendance;

import java.time.LocalDate;
import java.util.List;

/**
 * Use case port for retrieving attendance records.
 * Defines the contract for attendance query operations.
 */
public interface GetAttendanceUseCase {

    /**
     * Gets all attendance records for a specific session.
     *
     * @param sessionId the session ID
     * @return list of attendance records for the session
     */
    List<Attendance> getAttendanceBySession(Long sessionId);

    /**
     * Gets attendance history for a specific student.
     *
     * @param studentId the student ID
     * @return list of all attendance records for the student
     */
    List<Attendance> getAttendanceHistoryByStudent(Long studentId);

    /**
     * Gets attendance history for a student within a date range.
     *
     * @param query the query containing student ID and date range
     * @return list of attendance records within the specified period
     */
    List<Attendance> getAttendanceHistoryByStudentAndDateRange(AttendanceHistoryQuery query);

    /**
     * Gets attendance records for a subject group.
     *
     * @param groupId the subject group ID
     * @return list of all attendance records for the group
     */
    List<Attendance> getAttendanceByGroup(Long groupId);

    /**
     * Gets a specific attendance record by ID.
     *
     * @param attendanceId the attendance record ID
     * @return the attendance record
     * @throws acainfo.back.attendance.domain.exception.AttendanceNotFoundException if not found
     */
    Attendance getAttendanceById(Long attendanceId);

    /**
     * Finds attendance record for a specific student in a specific session.
     *
     * @param sessionId the session ID
     * @param studentId the student ID
     * @return the attendance record if exists
     * @throws acainfo.back.attendance.domain.exception.AttendanceNotFoundException if not found
     */
    Attendance getAttendanceBySessionAndStudent(Long sessionId, Long studentId);

    /**
     * Query object for attendance history with date range
     */
    record AttendanceHistoryQuery(
        Long studentId,
        LocalDate startDate,
        LocalDate endDate
    ) {}
}
