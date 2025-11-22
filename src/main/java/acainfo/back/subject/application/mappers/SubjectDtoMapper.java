package acainfo.back.subject.application.mappers;

import acainfo.back.subject.domain.model.SubjectDomain;
import acainfo.back.subject.domain.model.SubjectStatus;
import acainfo.back.subject.infrastructure.adapters.in.dto.CreateSubjectRequest;
import acainfo.back.subject.infrastructure.adapters.in.dto.SubjectResponse;
import acainfo.back.subject.infrastructure.adapters.in.dto.UpdateSubjectRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Mapper
 * Converts: Domain Subject ↔ DTOs
 *
 * Responsibility: Adapt domain for REST APIs
 */
@Component
public class SubjectDtoMapper {

    /**
     * Converts Domain → Response DTO (API output)
     */
    public SubjectResponse toResponse(SubjectDomain domain) {
        if (domain == null) {
            return null;
        }

        return SubjectResponse.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .year(domain.getYear())
                .degree(domain.getDegree())
                .semester(domain.getSemester())
                .status(domain.getStatus())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .fullName(domain.getFullName()) // Use domain business logic
                .build();
    }

    /**
     * Converts Request DTO → Domain (API input - creation)
     */
    public SubjectDomain toDomain(CreateSubjectRequest request) {
        if (request == null) {
            return null;
        }

        return SubjectDomain.builder()
                .code(request.getCode())
                .name(request.getName())
                .year(request.getYear())
                .degree(request.getDegree())
                .semester(request.getSemester())
                .description(request.getDescription())
                .status(SubjectStatus.ACTIVO)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Converts Update DTO → Domain (API input - update)
     */
    public SubjectDomain updateDomainFromDto(SubjectDomain existing, UpdateSubjectRequest request) {
        return SubjectDomain.builder()
                .id(existing.getId())
                .code(existing.getCode()) // Code cannot be changed
                .name(request.getName() != null ? request.getName() : existing.getName())
                .year(existing.getYear()) // Year cannot be changed after creation
                .degree(existing.getDegree()) // Degree cannot be changed
                .semester(existing.getSemester()) // Semester cannot be changed
                .description(request.getDescription() != null ? request.getDescription() : existing.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : existing.getStatus())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Converts list of Domain → Response
     */
    public List<SubjectResponse> toResponses(List<SubjectDomain> domains) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
