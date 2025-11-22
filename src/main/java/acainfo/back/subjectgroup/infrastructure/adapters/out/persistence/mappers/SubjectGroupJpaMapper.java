package acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.shared.domain.model.User;
import acainfo.back.subject.infrastructure.adapters.out.persistence.entities.SubjectJpaEntity;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities.SubjectGroupJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Infrastructure Mapper
 * Converts: SubjectGroupDomain ↔ JPA Entity
 *
 * Responsibility: Anti-corruption layer between domain and persistence
 */
@Component
public class SubjectGroupJpaMapper {

    /**
     * Converts Domain → JPA Entity (for persistence)
     */
    public SubjectGroupJpaEntity toJpaEntity(SubjectGroupDomain domain) {
        if (domain == null) {
            return null;
        }

        // Create Subject reference with only the ID
        // This is a JPA optimization: we don't need to load the full entity
        // just to persist a foreign key reference
        SubjectJpaEntity subjectRef = null;
        if (domain.getSubjectId() != null) {
            subjectRef = new SubjectJpaEntity();
            subjectRef.setId(domain.getSubjectId());
        }

        // Create User (teacher) reference with only the ID
        User teacherRef = null;
        if (domain.getTeacherId() != null) {
            teacherRef = new User();
            teacherRef.setId(domain.getTeacherId());
        }

        return SubjectGroupJpaEntity.builder()
                .id(domain.getId())
                .subject(subjectRef)
                .teacher(teacherRef)
                .type(domain.getType())
                .period(domain.getPeriod())
                .status(domain.getStatus())
                .maxCapacity(domain.getMaxCapacity())
                .currentOccupancy(domain.getCurrentOccupancy())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * Converts JPA Entity → Domain (for business logic)
     */
    public SubjectGroupDomain toDomain(SubjectGroupJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return SubjectGroupDomain.builder()
                .id(jpa.getId())
                .subjectId(jpa.getSubject() != null ? jpa.getSubject().getId() : null)
                .teacherId(jpa.getTeacher() != null ? jpa.getTeacher().getId() : null)
                .type(jpa.getType())
                .period(jpa.getPeriod())
                .status(jpa.getStatus())
                .maxCapacity(jpa.getMaxCapacity())
                .currentOccupancy(jpa.getCurrentOccupancy())
                .description(jpa.getDescription())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    /**
     * Converts list of JPA entities → Domain objects
     */
    public List<SubjectGroupDomain> toDomains(List<SubjectGroupJpaEntity> jpaEntities) {
        if (jpaEntities == null) {
            return List.of();
        }

        return jpaEntities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Converts list of Domain objects → JPA entities
     */
    public List<SubjectGroupJpaEntity> toJpaEntities(List<SubjectGroupDomain> domains) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(this::toJpaEntity)
                .collect(Collectors.toList());
    }
}
