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

    /**
     * Convert Material (Domain) to MaterialResponse (REST) without enriched data.
     * Use toEnrichedResponse for enriched responses.
     */
    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "uploadedByName", ignore = true)
    @Mapping(target = "fileSizeFormatted", expression = "java(material.getFileSizeFormatted())")
    @Mapping(target = "isCodeFile", expression = "java(material.isCodeFile())")
    @Mapping(target = "isDocumentFile", expression = "java(material.isDocumentFile())")
    @Mapping(target = "categoryDisplayName", expression = "java(material.getCategoryDisplayName())")
    MaterialResponse toResponse(Material material);

    /**
     * Convert Material (Domain) to MaterialResponse (REST) with enriched data.
     *
     * @param material       the material domain object
     * @param subjectName    name of the subject
     * @param uploadedByName full name of the user who uploaded
     * @return enriched material response
     */
    @Mapping(target = "subjectName", source = "subjectName")
    @Mapping(target = "uploadedByName", source = "uploadedByName")
    @Mapping(target = "id", source = "material.id")
    @Mapping(target = "subjectId", source = "material.subjectId")
    @Mapping(target = "uploadedById", source = "material.uploadedById")
    @Mapping(target = "name", source = "material.name")
    @Mapping(target = "description", source = "material.description")
    @Mapping(target = "originalFilename", source = "material.originalFilename")
    @Mapping(target = "fileExtension", source = "material.fileExtension")
    @Mapping(target = "mimeType", source = "material.mimeType")
    @Mapping(target = "fileSize", source = "material.fileSize")
    @Mapping(target = "category", source = "material.category")
    @Mapping(target = "uploadedAt", source = "material.uploadedAt")
    @Mapping(target = "createdAt", source = "material.createdAt")
    @Mapping(target = "updatedAt", source = "material.updatedAt")
    @Mapping(target = "fileSizeFormatted", expression = "java(material.getFileSizeFormatted())")
    @Mapping(target = "isCodeFile", expression = "java(material.isCodeFile())")
    @Mapping(target = "isDocumentFile", expression = "java(material.isDocumentFile())")
    @Mapping(target = "categoryDisplayName", expression = "java(material.getCategoryDisplayName())")
    MaterialResponse toEnrichedResponse(
            Material material,
            String subjectName,
            String uploadedByName
    );

    List<MaterialResponse> toResponseList(List<Material> materials);
}
