package com.acainfo.material.infrastructure.adapter.in.rest.mapper;

import com.acainfo.material.domain.model.MaterialAiJob;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.MaterialAiJobResponse;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for MaterialAiJob REST layer.
 */
@Mapper(componentModel = "spring")
public interface MaterialAiRestMapper {

    MaterialAiJobResponse toResponse(MaterialAiJob job);
}
