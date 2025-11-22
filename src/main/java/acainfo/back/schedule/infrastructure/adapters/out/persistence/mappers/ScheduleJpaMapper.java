package acainfo.back.schedule.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.schedule.domain.model.ScheduleDomain;
import acainfo.back.schedule.infrastructure.adapters.out.persistence.entities.ScheduleJpaEntity;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Infrastructure Mapper
 * Converts: Domain Schedule ↔ JPA Entity
 *
 * Responsibility: Anti-corruption layer between domain and persistence
 */
@Component
public class ScheduleJpaMapper {

    /**
     * Converts Domain → JPA Entity (for persistence)
     */
    public ScheduleJpaEntity toJpaEntity(ScheduleDomain domain) {
        if (domain == null) {
            return null;
        }

        // Create a SubjectGroup reference with only the ID
        // This is a JPA optimization: we don't need to load the full entity
        // just to persist a foreign key reference
        SubjectGroup subjectGroupRef = null;
        if (domain.getSubjectGroupId() != null) {
            subjectGroupRef = new SubjectGroup();
            subjectGroupRef.setId(domain.getSubjectGroupId());
        }

        return ScheduleJpaEntity.builder()
                .id(domain.getId())
                .subjectGroup(subjectGroupRef)
                .dayOfWeek(domain.getDayOfWeek())
                .startTime(domain.getStartTime())
                .endTime(domain.getEndTime())
                .classroom(domain.getClassroom())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * Converts JPA Entity → Domain (for business logic)
     */
    public ScheduleDomain toDomain(ScheduleJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return ScheduleDomain.builder()
                .id(jpa.getId())
                .subjectGroupId(jpa.getSubjectGroup() != null ? jpa.getSubjectGroup().getId() : null)
                .dayOfWeek(jpa.getDayOfWeek())
                .startTime(jpa.getStartTime())
                .endTime(jpa.getEndTime())
                .classroom(jpa.getClassroom())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    /**
     * Converts list of JPA entities → Domain objects
     */
    public List<ScheduleDomain> toDomains(List<ScheduleJpaEntity> jpaEntities) {
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
    public List<ScheduleJpaEntity> toJpaEntities(List<ScheduleDomain> domains) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(this::toJpaEntity)
                .collect(Collectors.toList());
    }
}
