package acainfo.back.attendance.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.attendance.application.ports.out.AttendanceRepositoryPort;
import acainfo.back.attendance.domain.model.AttendanceDomain;
import acainfo.back.attendance.domain.model.AttendanceStatus;
import acainfo.back.attendance.infrastructure.adapters.out.persistence.entities.AttendanceJpaEntity;
import acainfo.back.attendance.infrastructure.adapters.out.persistence.mappers.AttendanceJpaMapper;
import acainfo.back.attendance.infrastructure.adapters.out.persistence.repositories.AttendanceJpaRepository;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.entities.EnrollmentJpaEntity;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.repositories.EnrollmentJpaRepository;
import acainfo.back.session.infrastructure.adapters.out.persistence.entities.SessionJpaEntity;
import acainfo.back.session.infrastructure.adapters.out.persistence.repositories.SessionJpaRepository;
import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository Adapter Implementation
 * Infrastructure layer - implements domain port using JPA
 *
 * Responsibility: Bridge between domain and JPA infrastructure
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceRepositoryAdapterImpl implements AttendanceRepositoryPort {

    private final AttendanceJpaRepository jpaRepository;
    private final AttendanceJpaMapper mapper;
    private final SessionJpaRepository sessionRepository;
    private final EnrollmentJpaRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Override
    public AttendanceDomain save(AttendanceDomain attendance) {
        if (attendance == null) {
            return null;
        }

        // Fetch required entities for JPA relationships
        SessionJpaEntity session = null;
        if (attendance.getSessionId() != null) {
            session = sessionRepository.findById(attendance.getSessionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Session not found with ID: " + attendance.getSessionId()));
        }

        EnrollmentJpaEntity enrollment = null;
        if (attendance.getEnrollmentId() != null) {
            enrollment = enrollmentRepository.findById(attendance.getEnrollmentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Enrollment not found with ID: " + attendance.getEnrollmentId()));
        }

        User recordedBy = null;
        if (attendance.getRecordedById() != null) {
            recordedBy = userRepository.findById(attendance.getRecordedById())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "User not found with ID: " + attendance.getRecordedById()));
        }

        User justifiedBy = null;
        if (attendance.getJustifiedById() != null) {
            justifiedBy = userRepository.findById(attendance.getJustifiedById())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Justified by user not found with ID: " + attendance.getJustifiedById()));
        }

        // Handle create vs update
        AttendanceJpaEntity jpaEntity;
        if (attendance.getId() != null) {
            // Update existing
            jpaEntity = jpaRepository.findById(attendance.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Attendance not found with ID: " + attendance.getId()));
            mapper.updateJpaEntity(jpaEntity, attendance, justifiedBy);
        } else {
            // Create new
            jpaEntity = mapper.toJpaEntity(attendance, session, enrollment, recordedBy, justifiedBy);
        }

        AttendanceJpaEntity saved = jpaRepository.save(jpaEntity);
        log.debug("Saved attendance with ID: {}", saved.getId());

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AttendanceDomain> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AttendanceDomain> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDomain> findBySessionId(Long sessionId) {
        return jpaRepository.findBySessionId(sessionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDomain> findByEnrollmentId(Long enrollmentId) {
        return jpaRepository.findByEnrollmentId(enrollmentId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDomain> findByStudentId(Long studentId) {
        return jpaRepository.findByStudentId(studentId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AttendanceDomain> findBySessionIdAndEnrollmentId(Long sessionId, Long enrollmentId) {
        return jpaRepository.findBySessionIdAndEnrollmentId(sessionId, enrollmentId)
                .map(mapper::toDomain);
    }

    @Override
    public List<AttendanceDomain> findByGroupId(Long groupId) {
        return jpaRepository.findByGroupId(groupId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDomain> findByEnrollmentIdAndStatus(Long enrollmentId, AttendanceStatus status) {
        return jpaRepository.findByEnrollmentIdAndStatus(enrollmentId, status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDomain> findByStudentIdAndStatus(Long studentId, AttendanceStatus status) {
        return jpaRepository.findByStudentIdAndStatus(studentId, status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDomain> findBySessionIdAndStatus(Long sessionId, AttendanceStatus status) {
        return jpaRepository.findBySessionIdAndStatus(sessionId, status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDomain> findAbsencesByStudentId(Long studentId) {
        return jpaRepository.findAbsencesByStudentId(studentId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDomain> findEffectiveAttendanceByStudentId(Long studentId) {
        return jpaRepository.findEffectiveAttendanceByStudentId(studentId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDomain> findByEnrollmentIdAndDateRange(
            Long enrollmentId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return jpaRepository.findByEnrollmentIdAndDateRange(enrollmentId, startDate, endDate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDomain> findByStudentIdAndDateRange(
            Long studentId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return jpaRepository.findByStudentIdAndDateRange(studentId, startDate, endDate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDomain> findByGroupIdAndDateRange(
            Long groupId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return jpaRepository.findByGroupIdAndDateRange(groupId, startDate, endDate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsBySessionIdAndEnrollmentId(Long sessionId, Long enrollmentId) {
        return jpaRepository.existsBySessionIdAndEnrollmentId(sessionId, enrollmentId);
    }

    @Override
    public boolean existsBySessionId(Long sessionId) {
        return jpaRepository.existsBySessionId(sessionId);
    }

    @Override
    public boolean existsByEnrollmentId(Long enrollmentId) {
        return jpaRepository.existsByEnrollmentId(enrollmentId);
    }

    @Override
    public long countBySessionId(Long sessionId) {
        return jpaRepository.countBySessionId(sessionId);
    }

    @Override
    public long countByEnrollmentId(Long enrollmentId) {
        return jpaRepository.countByEnrollmentId(enrollmentId);
    }

    @Override
    public long countByStudentId(Long studentId) {
        return jpaRepository.countByStudentId(studentId);
    }

    @Override
    public long countByEnrollmentIdAndStatus(Long enrollmentId, AttendanceStatus status) {
        return jpaRepository.countByEnrollmentIdAndStatus(enrollmentId, status);
    }

    @Override
    public long countByStudentIdAndStatus(Long studentId, AttendanceStatus status) {
        return jpaRepository.countByStudentIdAndStatus(studentId, status);
    }

    @Override
    public long countEffectiveAttendanceByEnrollmentId(Long enrollmentId) {
        return jpaRepository.countEffectiveAttendanceByEnrollmentId(enrollmentId);
    }

    @Override
    public long countAbsencesByEnrollmentId(Long enrollmentId) {
        return jpaRepository.countAbsencesByEnrollmentId(enrollmentId);
    }

    @Override
    public List<Object[]> countByStatusForEnrollment(Long enrollmentId) {
        return jpaRepository.countByStatusForEnrollment(enrollmentId);
    }

    @Override
    public List<Object[]> countByStatusForStudent(Long studentId) {
        return jpaRepository.countByStatusForStudent(studentId);
    }

    @Override
    public List<Object[]> countByStatusForSession(Long sessionId) {
        return jpaRepository.countByStatusForSession(sessionId);
    }

    @Override
    public List<Object[]> countByStatusForGroup(Long groupId) {
        return jpaRepository.countByStatusForGroup(groupId);
    }

    @Override
    public Double calculateAttendanceRateForEnrollment(Long enrollmentId) {
        return jpaRepository.calculateAttendanceRateForEnrollment(enrollmentId);
    }

    @Override
    public Double calculateAttendanceRateForStudent(Long studentId) {
        return jpaRepository.calculateAttendanceRateForStudent(studentId);
    }

    @Override
    public Double calculateAttendanceRateForStudentInGroup(Long studentId, Long groupId) {
        return jpaRepository.calculateAttendanceRateForStudentInGroup(studentId, groupId);
    }

    @Override
    public Double calculateAttendanceRateForSession(Long sessionId) {
        return jpaRepository.calculateAttendanceRateForSession(sessionId);
    }

    @Override
    public Double calculateAttendanceRateForGroup(Long groupId) {
        return jpaRepository.calculateAttendanceRateForGroup(groupId);
    }

    @Override
    public Integer calculateTotalMinutesLateForEnrollment(Long enrollmentId) {
        return jpaRepository.calculateTotalMinutesLateForEnrollment(enrollmentId);
    }

    @Override
    public Integer calculateTotalMinutesLateForStudent(Long studentId) {
        return jpaRepository.calculateTotalMinutesLateForStudent(studentId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
        log.debug("Deleted attendance with ID: {}", id);
    }

    @Override
    public void deleteBySessionId(Long sessionId) {
        jpaRepository.deleteBySessionId(sessionId);
        log.debug("Deleted all attendance for session: {}", sessionId);
    }

    @Override
    public void deleteByEnrollmentId(Long enrollmentId) {
        jpaRepository.deleteByEnrollmentId(enrollmentId);
        log.debug("Deleted all attendance for enrollment: {}", enrollmentId);
    }
}
