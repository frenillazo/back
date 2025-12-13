package com.acainfo.material.infrastructure.adapter.in.rest.mapper;

import com.acainfo.material.domain.model.Material;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.MaterialResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Material REST layer.
 * Converts between domain Material and REST DTOs.
 */
@Mapper(componentModel = "spring")
public interface MaterialRestMapper {

    @Mapping(target = "fileSizeFormatted", expression = "java(material.getFileSizeFormatted())")
    @Mapping(target = "isCodeFile", expression = "java(material.isCodeFile())")
    @Mapping(target = "isDocumentFile", expression = "java(material.isDocumentFile())")
    MaterialResponse toResponse(Material material);

    List<MaterialResponse> toResponseList(List<Material> materials);
}
