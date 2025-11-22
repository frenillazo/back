package acainfo.back.material.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.material.domain.model.MaterialDomain;
import acainfo.back.material.infrastructure.adapters.out.persistence.entities.MaterialJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities.SubjectGroupJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Infrastructure Mapper
 * Converts: MaterialDomain ↔ MaterialJpaEntity
 *
 * Responsibility: Anti-corruption layer between domain and persistence
 */
@Component
@RequiredArgsConstructor
public class MaterialJpaMapper {

    /**
     * Converts JPA Entity → Domain
     * Maps entity references to IDs
     */
    public MaterialDomain toDomain(MaterialJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return MaterialDomain.builder()
                .id(jpa.getId())
                .subjectGroupId(jpa.getSubjectGroup() != null ? jpa.getSubjectGroup().getId() : null)
                .fileName(jpa.getFileName())
                .filePath(jpa.getFilePath())
                .type(jpa.getType())
                .fileSize(jpa.getFileSize())
                .description(jpa.getDescription())
                .topic(jpa.getTopic())
                .uploadedById(jpa.getUploadedBy() != null ? jpa.getUploadedBy().getId() : null)
                .uploadedAt(jpa.getUploadedAt())
                .requiresPayment(jpa.getRequiresPayment())
                .isActive(jpa.getIsActive())
                .version(jpa.getVersion())
                .build();
    }

    /**
     * Converts Domain → JPA Entity (for creation)
     * Maps IDs to entity references
     */
    public MaterialJpaEntity toJpaEntity(MaterialDomain domain,
                                         SubjectGroupJpaEntity subjectGroup,
                                         User uploadedBy) {
        if (domain == null) {
            return null;
        }

        return MaterialJpaEntity.builder()
                .id(domain.getId())
                .subjectGroup(subjectGroup)
                .fileName(domain.getFileName())
                .filePath(domain.getFilePath())
                .type(domain.getType())
                .fileSize(domain.getFileSize())
                .description(domain.getDescription())
                .topic(domain.getTopic())
                .uploadedBy(uploadedBy)
                .uploadedAt(domain.getUploadedAt())
                .requiresPayment(domain.getRequiresPayment())
                .isActive(domain.getIsActive())
                .version(domain.getVersion())
                .build();
    }

    /**
     * Updates an existing JPA entity with domain data
     * Preserves entity references and immutable fields
     */
    public void updateJpaEntity(MaterialJpaEntity jpa, MaterialDomain domain) {
        if (jpa == null || domain == null) {
            return;
        }

        // Don't update: id, subjectGroup, fileName, filePath, type, fileSize, uploadedBy, uploadedAt
        // These are immutable after creation

        jpa.setDescription(domain.getDescription());
        jpa.setTopic(domain.getTopic());
        jpa.setRequiresPayment(domain.getRequiresPayment());
        jpa.setIsActive(domain.getIsActive());
        // version is managed by JPA
    }
}
