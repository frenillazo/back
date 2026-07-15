package com.acainfo.material.infrastructure.mapper;

import com.acainfo.material.domain.model.MaterialFolder;
import com.acainfo.material.infrastructure.adapter.out.persistence.entity.MaterialFolderJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for MaterialFolder persistence layer.
 */
@Mapper(componentModel = "spring")
public interface MaterialFolderPersistenceMapper {

    MaterialFolderJpaEntity toJpaEntity(MaterialFolder folder);

    MaterialFolder toDomain(MaterialFolderJpaEntity entity);

    List<MaterialFolder> toDomainList(List<MaterialFolderJpaEntity> entities);
}
