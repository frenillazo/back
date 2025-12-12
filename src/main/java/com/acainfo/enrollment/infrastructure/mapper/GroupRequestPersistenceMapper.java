package com.acainfo.enrollment.infrastructure.mapper;

import com.acainfo.enrollment.domain.model.GroupRequest;
import com.acainfo.enrollment.infrastructure.adapter.out.persistence.entity.GroupRequestJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for GroupRequest persistence.
 * Converts between domain model (GroupRequest) and JPA entity (GroupRequestJpaEntity).
 */
@Mapper(componentModel = "spring")
public interface GroupRequestPersistenceMapper {

    /**
     * Convert domain GroupRequest to JPA entity.
     *
     * @param groupRequest Domain model
     * @return JPA entity
     */
    GroupRequestJpaEntity toJpaEntity(GroupRequest groupRequest);

    /**
     * Convert JPA entity to domain GroupRequest.
     *
     * @param entity JPA entity
     * @return Domain model
     */
    GroupRequest toDomain(GroupRequestJpaEntity entity);

    /**
     * Convert list of JPA entities to domain models.
     *
     * @param entities List of JPA entities
     * @return List of domain models
     */
    List<GroupRequest> toDomainList(List<GroupRequestJpaEntity> entities);

    /**
     * Convert list of domain models to JPA entities.
     *
     * @param groupRequests List of domain models
     * @return List of JPA entities
     */
    List<GroupRequestJpaEntity> toJpaEntityList(List<GroupRequest> groupRequests);
}
