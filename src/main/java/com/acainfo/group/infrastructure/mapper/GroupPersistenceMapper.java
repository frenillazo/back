package com.acainfo.group.infrastructure.mapper;

import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.group.infrastructure.adapter.out.persistence.entity.SubjectGroupJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for SubjectGroup persistence.
 * Converts between domain model (SubjectGroup) and JPA entity (SubjectGroupJpaEntity).
 */
@Mapper(componentModel = "spring")
public interface GroupPersistenceMapper {

    /**
     * Convert domain SubjectGroup to JPA entity.
     *
     * @param group Domain model
     * @return JPA entity
     */
    SubjectGroupJpaEntity toJpaEntity(SubjectGroup group);

    /**
     * Convert JPA entity to domain SubjectGroup.
     *
     * @param entity JPA entity
     * @return Domain model
     */
    SubjectGroup toDomain(SubjectGroupJpaEntity entity);

    /**
     * Convert list of JPA entities to domain models.
     *
     * @param entities List of JPA entities
     * @return List of domain models
     */
    List<SubjectGroup> toDomainList(List<SubjectGroupJpaEntity> entities);
}
