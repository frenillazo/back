package com.acainfo.subject.infrastructure.mapper;

import com.acainfo.subject.domain.model.Subject;
import com.acainfo.subject.infrastructure.adapter.out.persistence.entity.SubjectJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Subject persistence.
 * Converts between domain model (Subject) and JPA entity (SubjectJpaEntity).
 */
@Mapper(componentModel = "spring")
public interface SubjectPersistenceMapper {

    /**
     * Convert domain Subject to JPA entity.
     *
     * @param subject Domain model
     * @return JPA entity
     */
    SubjectJpaEntity toJpaEntity(Subject subject);

    /**
     * Convert JPA entity to domain Subject.
     *
     * @param entity JPA entity
     * @return Domain model
     */
    Subject toDomain(SubjectJpaEntity entity);

    /**
     * Convert list of JPA entities to domain models.
     *
     * @param entities List of JPA entities
     * @return List of domain models
     */
    List<Subject> toDomainList(List<SubjectJpaEntity> entities);
}
