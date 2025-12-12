package com.acainfo.reservation.infrastructure.mapper;

import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.reservation.infrastructure.adapter.out.persistence.entity.SessionReservationJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for SessionReservation persistence.
 * Converts between domain model (SessionReservation) and JPA entity (SessionReservationJpaEntity).
 */
@Mapper(componentModel = "spring")
public interface ReservationPersistenceMapper {

    /**
     * Convert domain SessionReservation to JPA entity.
     *
     * @param reservation Domain model
     * @return JPA entity
     */
    SessionReservationJpaEntity toJpaEntity(SessionReservation reservation);

    /**
     * Convert JPA entity to domain SessionReservation.
     *
     * @param entity JPA entity
     * @return Domain model
     */
    SessionReservation toDomain(SessionReservationJpaEntity entity);

    /**
     * Convert list of JPA entities to domain models.
     *
     * @param entities List of JPA entities
     * @return List of domain models
     */
    List<SessionReservation> toDomainList(List<SessionReservationJpaEntity> entities);

    /**
     * Convert list of domain models to JPA entities.
     *
     * @param reservations List of domain models
     * @return List of JPA entities
     */
    List<SessionReservationJpaEntity> toJpaEntityList(List<SessionReservation> reservations);
}
