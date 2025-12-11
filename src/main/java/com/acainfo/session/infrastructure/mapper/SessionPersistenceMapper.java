package com.acainfo.session.infrastructure.mapper;

import com.acainfo.session.domain.model.Session;
import com.acainfo.session.infrastructure.adapter.out.persistence.entity.SessionJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Session persistence.
 * Converts between domain model (Session) and JPA entity (SessionJpaEntity).
 */
@Mapper(componentModel = "spring")
public interface SessionPersistenceMapper {

    /**
     * Convert domain Session to JPA entity.
     *
     * @param session Domain model
     * @return JPA entity
     */
    SessionJpaEntity toJpaEntity(Session session);

    /**
     * Convert JPA entity to domain Session.
     *
     * @param entity JPA entity
     * @return Domain model
     */
    Session toDomain(SessionJpaEntity entity);

    /**
     * Convert list of JPA entities to domain models.
     *
     * @param entities List of JPA entities
     * @return List of domain models
     */
    List<Session> toDomainList(List<SessionJpaEntity> entities);

    /**
     * Convert list of domain models to JPA entities.
     *
     * @param sessions List of domain models
     * @return List of JPA entities
     */
    List<SessionJpaEntity> toJpaEntityList(List<Session> sessions);
}
