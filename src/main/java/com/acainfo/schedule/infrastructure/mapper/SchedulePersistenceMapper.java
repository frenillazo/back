package com.acainfo.schedule.infrastructure.mapper;

import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.schedule.infrastructure.adapter.out.persistence.entity.ScheduleJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Schedule persistence.
 * Converts between domain model (Schedule) and JPA entity (ScheduleJpaEntity).
 */
@Mapper(componentModel = "spring")
public interface SchedulePersistenceMapper {

    /**
     * Convert domain Schedule to JPA entity.
     *
     * @param schedule Domain model
     * @return JPA entity
     */
    ScheduleJpaEntity toJpaEntity(Schedule schedule);

    /**
     * Convert JPA entity to domain Schedule.
     *
     * @param entity JPA entity
     * @return Domain model
     */
    Schedule toDomain(ScheduleJpaEntity entity);

    /**
     * Convert list of JPA entities to domain models.
     *
     * @param entities List of JPA entities
     * @return List of domain models
     */
    List<Schedule> toDomainList(List<ScheduleJpaEntity> entities);
}
