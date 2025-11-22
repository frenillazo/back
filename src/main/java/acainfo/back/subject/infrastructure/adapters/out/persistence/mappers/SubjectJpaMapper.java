package acainfo.back.subject.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.subject.domain.model.SubjectDomain;
import acainfo.back.subject.infrastructure.adapters.out.persistence.entities.SubjectJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Infrastructure Mapper
 * Converts: Domain Subject ↔ JPA SubjectJpaEntity
 *
 * Responsibility: Isolate domain from persistence implementation
 */
@Component
public class SubjectJpaMapper {

    /**
     * Converts Domain → JPA (for saving to database)
     */
    public SubjectJpaEntity toJpaEntity(SubjectDomain domain) {
        if (domain == null) {
            return null;
        }

        return SubjectJpaEntity.builder()
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
                .build();
    }

    /**
     * Converts JPA → Domain (for loading from database)
     */
    public SubjectDomain toDomain(SubjectJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return SubjectDomain.builder()
                .id(jpa.getId())
                .code(jpa.getCode())
                .name(jpa.getName())
                .year(jpa.getYear())
                .degree(jpa.getDegree())
                .semester(jpa.getSemester())
                .status(jpa.getStatus())
                .description(jpa.getDescription())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    /**
     * Converts list of JPA → Domain
     */
    public List<SubjectDomain> toDomains(List<SubjectJpaEntity> jpaEntities) {
        if (jpaEntities == null) {
            return List.of();
        }

        return jpaEntities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Converts list of Domain → JPA
     */
    public List<SubjectJpaEntity> toJpaEntities(List<SubjectDomain> domains) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(this::toJpaEntity)
                .collect(Collectors.toList());
    }
}
