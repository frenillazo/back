package com.acainfo.user.infrastructure.mapper;

import com.acainfo.user.domain.model.Role;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.RoleJpaEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for Role persistence.
 * Converts between domain model (Role) and JPA entity (RoleJpaEntity).
 */
@Mapper(componentModel = "spring")
public interface RolePersistenceMapper {

    /**
     * Convert domain Role to JPA entity.
     */
    RoleJpaEntity toJpaEntity(Role role);

    /**
     * Convert JPA entity to domain Role.
     */
    Role toDomain(RoleJpaEntity entity);
}
