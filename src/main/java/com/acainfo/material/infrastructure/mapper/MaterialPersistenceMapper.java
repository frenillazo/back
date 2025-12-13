package com.acainfo.material.infrastructure.mapper;

import com.acainfo.material.domain.model.Material;
import com.acainfo.material.infrastructure.adapter.out.persistence.entity.MaterialJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Material persistence layer.
 * Converts between domain Material and JPA MaterialJpaEntity.
 */
@Mapper(componentModel = "spring")
public interface MaterialPersistenceMapper {

    MaterialJpaEntity toJpaEntity(Material material);

    Material toDomain(MaterialJpaEntity entity);

    List<Material> toDomainList(List<MaterialJpaEntity> entities);

    List<MaterialJpaEntity> toJpaEntityList(List<Material> materials);
}
