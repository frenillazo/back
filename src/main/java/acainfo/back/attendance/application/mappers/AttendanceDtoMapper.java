package acainfo.back.attendance.application.mappers;

import acainfo.back.attendance.domain.model.AttendanceDomain;
import acainfo.back.attendance.infrastructure.adapters.in.dto.AttendanceResponse;
import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.model.EnrollmentDomain;
import acainfo.back.session.application.ports.out.SessionRepositoryPort;
import acainfo.back.session.domain.model.SessionDomain;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository; // TODO: Create UserRepositoryPort in application layer
import acainfo.back.subjectgroup.application.ports.out.SubjectGroupRepositoryPort;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting AttendanceDomain objects to DTOs.
 * Application layer - handles DTO conversions with related entity data.
 *
 * This mapper fetches related entity information to populate complete DTOs:
 * - Session details (scheduled start, subject group name)
 * - Student information (from enrollment)
 * - User information (recorded by, justified by)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceDtoMapper {

    private final SessionRepositoryPort sessionRepository;
    private final SubjectGroupRepositoryPort subjectGroupRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final UserRepository userRepository;

    /**
     * Converts AttendanceDomain to AttendanceResponse DTO.
     * Fetches related entity information to populate complete response.
     */
    public AttendanceResponse toResponse(AttendanceDomain attendance) {
        if (attendance == null) {
            return null;
        }

        // Fetch session details
        SessionDomain session = sessionRepository.findById(attendance.getSessionId())
                .orElse(null);

        // Fetch subject group details (through session)
        SubjectGroupDomain subjectGroup = null;
        if (session != null && session.getSubjectGroupId() != null) {
            subjectGroup = subjectGroupRepository.findById(session.getSubjectGroupId())
                    .orElse(null);
        }

        // Fetch enrollment details
        EnrollmentDomain enrollment = enrollmentRepository.findById(attendance.getEnrollmentId())
                .orElse(null);

        // Fetch student details (through enrollment)
        User student = null;
        if (enrollment != null && enrollment.getStudentId() != null) {
            student = userRepository.findById(enrollment.getStudentId())
                    .orElse(null);
        }

        // Fetch recorded by user
        User recordedBy = userRepository.findById(attendance.getRecordedById())
                .orElse(null);

        // Fetch justified by user (if present)
        User justifiedBy = null;
        if (attendance.getJustifiedById() != null) {
            justifiedBy = userRepository.findById(attendance.getJustifiedById())
                    .orElse(null);
        }

        // Build response DTO
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .sessionId(attendance.getSessionId())
                .sessionScheduledStart(session != null ? session.getScheduledStart() : null)
                .sessionSubjectGroupName(subjectGroup != null ? subjectGroup.getDisplayName() : null)
                .enrollmentId(attendance.getEnrollmentId())
                .studentId(student != null ? student.getId() : null)
                .studentName(student != null ?
                    student.getFirstName() + " " + student.getLastName() : null)
                .status(attendance.getStatus())
                .recordedAt(attendance.getRecordedAt())
                .recordedById(attendance.getRecordedById())
                .recordedByName(recordedBy != null ?
                    recordedBy.getFirstName() + " " + recordedBy.getLastName() : null)
                .notes(attendance.getNotes())
                .minutesLate(attendance.getMinutesLate())
                .justifiedAt(attendance.getJustifiedAt())
                .justifiedById(attendance.getJustifiedById())
                .justifiedByName(justifiedBy != null ?
                    justifiedBy.getFirstName() + " " + justifiedBy.getLastName() : null)
                .countsAsEffectiveAttendance(attendance.countsAsEffectiveAttendance())
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }

    /**
     * Converts a list of AttendanceDomain objects to AttendanceResponse DTOs.
     */
    public List<AttendanceResponse> toResponses(List<AttendanceDomain> attendances) {
        if (attendances == null) {
            return List.of();
        }

        return attendances.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
