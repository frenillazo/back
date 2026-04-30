package com.acainfo.intensive.infrastructure.mapper;

import com.acainfo.intensive.domain.model.Intensive;
import com.acainfo.intensive.infrastructure.adapter.out.persistence.entity.IntensiveJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper between Intensive (domain) and IntensiveJpaEntity (persistence).
 */
@Mapper(componentModel = "spring")
public interface IntensivePersistenceMapper {

    IntensiveJpaEntity toJpaEntity(Intensive intensive);

    Intensive toDomain(IntensiveJpaEntity entity);

    List<Intensive> toDomainList(List<IntensiveJpaEntity> entities);
}
