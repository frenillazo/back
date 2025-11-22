package acainfo.back.enrollment.application.mappers;

import acainfo.back.enrollment.domain.model.EnrollmentDomain;
import acainfo.back.enrollment.infrastructure.adapters.in.dto.EnrollmentResponse;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository;
import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.model.SubjectDomain;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Mapper
 * Converts: EnrollmentDomain ↔ DTOs
 *
 * Responsibility: Adapt domain for REST APIs
 */
@Component
@RequiredArgsConstructor
public class EnrollmentDtoMapper {

    private final UserRepository userRepository;
    private final GroupRepositoryPort groupRepository;
    private final SubjectRepositoryPort subjectRepository;

    /**
     * Converts Domain → Response DTO (API output)
     * Enriches with related entity data (student, subject group info)
     */
    public EnrollmentResponse toResponse(EnrollmentDomain domain) {
        if (domain == null) {
            return null;
        }

        EnrollmentResponse.EnrollmentResponseBuilder builder = EnrollmentResponse.builder()
                .id(domain.getId())
                .status(domain.getStatus())
                .attendanceMode(domain.getAttendanceMode())
                .enrollmentDate(domain.getEnrollmentDate())
                .withdrawalDate(domain.getWithdrawalDate())
                .withdrawalReason(domain.getWithdrawalReason())
                .updatedAt(domain.getUpdatedAt());

        // Fetch and add student info
        if (domain.getStudentId() != null) {
            userRepository.findById(domain.getStudentId()).ifPresent(student -> {
                builder.student(EnrollmentResponse.StudentBasicInfo.builder()
                        .id(student.getId())
                        .email(student.getEmail())
                        .firstName(student.getFirstName())
                        .lastName(student.getLastName())
                        .fullName(student.getFullName())
                        .build());
            });
        }

        // Fetch and add subject group info
        if (domain.getSubjectGroupId() != null) {
            groupRepository.findById(domain.getSubjectGroupId()).ifPresent(group -> {
                builder.subjectGroup(buildSubjectGroupInfo(group));
            });
        }

        return builder.build();
    }

    /**
     * Converts list of Domain → Response
     */
    public List<EnrollmentResponse> toResponses(List<EnrollmentDomain> domains) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper to build subject group info with subject details
     */
    private EnrollmentResponse.SubjectGroupBasicInfo buildSubjectGroupInfo(SubjectGroupDomain group) {
        EnrollmentResponse.SubjectGroupBasicInfo.SubjectGroupBasicInfoBuilder builder =
                EnrollmentResponse.SubjectGroupBasicInfo.builder()
                        .id(group.getId())
                        .groupType(group.getType().getDisplayName())
                        .groupStatus(group.getStatus().getDisplayName())
                        .occupancy(group.getCurrentOccupancy() + "/" + group.getMaxCapacity());

        // Fetch subject for code and name
        if (group.getSubjectId() != null) {
            subjectRepository.findById(group.getSubjectId()).ifPresent(subject -> {
                builder.subjectCode(subject.getCode())
                       .subjectName(subject.getName());
            });
        } else {
            builder.subjectCode("N/A")
                   .subjectName("N/A");
        }

        return builder.build();
    }
}
