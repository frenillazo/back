package acainfo.back.attendance.application.services;

import acainfo.back.attendance.application.ports.in.GetAttendanceUseCase;
import acainfo.back.attendance.application.ports.out.AttendanceRepositoryPort;
import acainfo.back.attendance.domain.exception.AttendanceNotFoundException;
import acainfo.back.attendance.domain.model.AttendanceDomain;
import acainfo.back.session.application.ports.out.SessionRepositoryPort;
import acainfo.back.session.domain.exception.SessionNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Use Case Implementation: Get Attendance
 * Application layer - implements attendance query operations
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GetAttendanceUseCaseImpl implements GetAttendanceUseCase {

    private final AttendanceRepositoryPort attendanceRepository;
    private final SessionRepositoryPort sessionRepository;

    @Override
    public List<AttendanceDomain> getAttendanceBySession(Long sessionId) {
        log.debug("Retrieving attendance for session {}", sessionId);

        // Validate session exists
        if (!sessionRepository.existsById(sessionId)) {
            throw new SessionNotFoundException(sessionId);
        }

        return attendanceRepository.findBySessionId(sessionId);
    }

    @Override
    public List<AttendanceDomain> getAttendanceHistoryByStudent(Long studentId) {
        log.debug("Retrieving attendance history for student {}", studentId);
        return attendanceRepository.findByStudentId(studentId);
    }

    @Override
    public List<AttendanceDomain> getAttendanceHistoryByStudentAndDateRange(
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
    public List<AttendanceDomain> getAttendanceByGroup(Long groupId) {
        log.debug("Retrieving attendance for group {}", groupId);
        return attendanceRepository.findByGroupId(groupId);
    }

    @Override
    public AttendanceDomain getAttendanceById(Long attendanceId) {
        log.debug("Retrieving attendance by ID: {}", attendanceId);
        return attendanceRepository.findById(attendanceId)
            .orElseThrow(() -> new AttendanceNotFoundException(attendanceId));
    }

    @Override
    public AttendanceDomain getAttendanceBySessionAndStudent(Long sessionId, Long studentId) {
        log.debug("Retrieving attendance for session {} and student {}", sessionId, studentId);

        // Get all attendances for the session
        List<AttendanceDomain> sessionAttendances = attendanceRepository.findBySessionId(sessionId);

        // Find the one for this student by checking enrollment
        return sessionAttendances.stream()
            .filter(a -> {
                // We need to check if this attendance belongs to this student
                // We can use findByStudentId and check if it's in that list
                List<AttendanceDomain> studentAttendances = attendanceRepository.findByStudentId(studentId);
                return studentAttendances.stream()
                    .anyMatch(sa -> sa.getId() != null && sa.getId().equals(a.getId()));
            })
            .findFirst()
            .orElseThrow(() -> new AttendanceNotFoundException(sessionId, studentId));
    }
}
