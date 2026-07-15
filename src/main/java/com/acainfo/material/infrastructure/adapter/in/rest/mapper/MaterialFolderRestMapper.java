package com.acainfo.material.infrastructure.adapter.in.rest.mapper;

import com.acainfo.material.domain.model.MaterialFolder;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.MaterialFolderResponse;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for MaterialFolder REST layer.
 */
@Mapper(componentModel = "spring")
public interface MaterialFolderRestMapper {

    MaterialFolderResponse toResponse(MaterialFolder folder);

    List<MaterialFolderResponse> toResponseList(List<MaterialFolder> folders);
}
