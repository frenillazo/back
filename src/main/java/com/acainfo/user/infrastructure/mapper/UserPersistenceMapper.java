package com.acainfo.user.infrastructure.mapper;

import com.acainfo.user.domain.model.User;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for User persistence.
 * Converts between domain model (User) and JPA entity (UserJpaEntity).
 */
@Mapper(componentModel = "spring", uses = RolePersistenceMapper.class)
public interface UserPersistenceMapper {

    /**
     * Convert domain User to JPA entity.
     *
     * @param user Domain model
     * @return JPA entity with mapped roles
     */
    UserJpaEntity toJpaEntity(User user);

    /**
     * Convert JPA entity to domain User.
     *
     * @param entity JPA entity
     * @return Domain model with mapped roles
     */
    User toDomain(UserJpaEntity entity);
}
