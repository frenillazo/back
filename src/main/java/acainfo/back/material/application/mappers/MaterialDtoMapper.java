package acainfo.back.material.application.mappers;

import acainfo.back.material.domain.model.MaterialDomain;
import acainfo.back.material.infrastructure.adapters.in.dto.MaterialResponse;
import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.model.SubjectDomain;
import acainfo.back.subjectgroup.application.ports.out.SubjectGroupRepositoryPort;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import acainfo.back.user.application.ports.out.UserRepositoryPort;
import acainfo.back.user.domain.model.UserDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Mapper
 * Converts: MaterialDomain ↔ DTOs
 *
 * Responsibility: Adapt domain for REST APIs
 */
@Component
@RequiredArgsConstructor
public class MaterialDtoMapper {

    private final SubjectGroupRepositoryPort groupRepository;
    private final SubjectRepositoryPort subjectRepository;
    private final UserRepositoryPort userRepository;

    /**
     * Converts Domain → Response DTO (API output)
     * Enriches with related entity data (subject group, subject, uploader info)
     */
    public MaterialResponse toResponse(MaterialDomain domain) {
        if (domain == null) {
            return null;
        }

        MaterialResponse.MaterialResponseBuilder builder = MaterialResponse.builder()
                .id(domain.getId())
                .subjectGroupId(domain.getSubjectGroupId())
                .fileName(domain.getFileName())
                .type(domain.getType())
                .fileSize(domain.getFileSize())
                .formattedFileSize(domain.getFormattedFileSize())
                .description(domain.getDescription())
                .topic(domain.getTopic())
                .uploadedById(domain.getUploadedById())
                .uploadedAt(domain.getUploadedAt())
                .requiresPayment(domain.getRequiresPayment())
                .isActive(domain.getIsActive())
                .version(domain.getVersion());

        // Fetch and add subject group info
        if (domain.getSubjectGroupId() != null) {
            groupRepository.findById(domain.getSubjectGroupId()).ifPresent(group -> {
                builder.subjectGroupName(getGroupDisplayName(group));
            });
        }

        // Fetch and add uploader info
        if (domain.getUploadedById() != null) {
            userRepository.findById(domain.getUploadedById()).ifPresent(user -> {
                builder.uploadedByName(user.getFullName());
            });
        }

        return builder.build();
    }

    /**
     * Converts list of Domain → Response
     */
    public List<MaterialResponse> toResponses(List<MaterialDomain> domains) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper to generate display name for subject group
     */
    private String getGroupDisplayName(SubjectGroupDomain group) {
        StringBuilder name = new StringBuilder();

        // Fetch subject for name
        if (group.getSubjectId() != null) {
            subjectRepository.findById(group.getSubjectId()).ifPresent(subject ->
                    name.append(subject.getName())
            );
        }

        if (name.length() > 0) {
            name.append(" - ");
        }

        name.append(group.getPeriod().getDisplayName())
                .append(" ")
                .append(group.getType().getDisplayName());

        return name.toString();
    }
}
