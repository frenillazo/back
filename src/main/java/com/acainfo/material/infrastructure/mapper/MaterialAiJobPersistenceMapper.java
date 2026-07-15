package com.acainfo.material.infrastructure.mapper;

import com.acainfo.material.domain.model.MaterialAiJob;
import com.acainfo.material.infrastructure.adapter.out.persistence.entity.MaterialAiJobJpaEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for MaterialAiJob persistence layer.
 */
@Mapper(componentModel = "spring")
public interface MaterialAiJobPersistenceMapper {

    MaterialAiJobJpaEntity toJpaEntity(MaterialAiJob job);

    MaterialAiJob toDomain(MaterialAiJobJpaEntity entity);
}
