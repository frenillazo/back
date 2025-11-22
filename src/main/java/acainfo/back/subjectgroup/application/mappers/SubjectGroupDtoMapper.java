package acainfo.back.subjectgroup.application.mappers;

import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.out.UserRepository;
import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.model.SubjectDomain;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import acainfo.back.subjectgroup.infrastructure.adapters.in.dto.CreateSubjectGroupRequest;
import acainfo.back.subjectgroup.infrastructure.adapters.in.dto.SubjectGroupResponse;
import acainfo.back.subjectgroup.infrastructure.adapters.in.dto.UpdateSubjectGroupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Mapper
 * Converts: SubjectGroupDomain ↔ DTOs
 *
 * Responsibility: Adapt domain for REST APIs
 */
@Component
@RequiredArgsConstructor
public class SubjectGroupDtoMapper {

    private final SubjectRepositoryPort subjectRepository;
    private final UserRepository userRepository;

    /**
     * Converts Domain → Response DTO (API output)
     * Enriches with related entity data (subject and teacher info)
     */
    public SubjectGroupResponse toResponse(SubjectGroupDomain domain) {
        if (domain == null) {
            return null;
        }

        SubjectGroupResponse.SubjectGroupResponseBuilder builder = SubjectGroupResponse.builder()
                .id(domain.getId())
                .type(domain.getType())
                .period(domain.getPeriod())
                .status(domain.getStatus())
                .maxCapacity(domain.getMaxCapacity())
                .currentOccupancy(domain.getCurrentOccupancy())
                .availablePlaces(domain.getAvailablePlaces())
                .hasAvailablePlaces(domain.hasAvailablePlaces())
                .isFull(domain.isFull())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt());

        // Fetch and add subject info
        if (domain.getSubjectId() != null) {
            subjectRepository.findById(domain.getSubjectId()).ifPresent(subject ->
                builder.subject(SubjectGroupResponse.SubjectBasicInfo.builder()
                        .id(subject.getId())
                        .code(subject.getCode())
                        .name(subject.getName())
                        .year(subject.getYear())
                        .degree(subject.getDegree())
                        .build())
            );
        }

        // Fetch and add teacher info (if assigned)
        if (domain.getTeacherId() != null) {
            userRepository.findById(domain.getTeacherId()).ifPresent(teacher ->
                builder.teacher(SubjectGroupResponse.TeacherBasicInfo.builder()
                        .id(teacher.getId())
                        .email(teacher.getEmail())
                        .firstName(teacher.getFirstName())
                        .lastName(teacher.getLastName())
                        .fullName(teacher.getFullName())
                        .build())
            );
        }

        return builder.build();
    }

    /**
     * Converts Request DTO → Domain (API input - creation)
     */
    public SubjectGroupDomain toDomain(CreateSubjectGroupRequest request) {
        if (request == null) {
            return null;
        }

        return SubjectGroupDomain.builder()
                .subjectId(request.getSubjectId())
                .teacherId(request.getTeacherId())
                .type(request.getType())
                .period(request.getPeriod())
                .status(GroupStatus.ACTIVO) // New groups are always active
                .maxCapacity(request.getMaxCapacity())
                .currentOccupancy(0) // New groups start empty
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Converts Update DTO → Domain (API input - update)
     * Creates a partial domain object with only fields to update
     */
    public SubjectGroupDomain toDomainFromUpdate(UpdateSubjectGroupRequest request) {
        if (request == null) {
            return null;
        }

        return SubjectGroupDomain.builder()
                .teacherId(request.getTeacherId())
                .type(request.getType())
                .period(request.getPeriod())
                .status(request.getStatus())
                .maxCapacity(request.getMaxCapacity())
                .currentOccupancy(0) // Will be preserved from existing
                .build();
    }

    /**
     * Converts list of Domain → Response
     */
    public List<SubjectGroupResponse> toResponses(List<SubjectGroupDomain> domains) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
