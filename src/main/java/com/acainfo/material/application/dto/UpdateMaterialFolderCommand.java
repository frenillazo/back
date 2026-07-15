package com.acainfo.material.application.dto;

/**
 * Command for updating a material folder. Any null field is left unchanged.
 *
 * @param name     new name, unique within the subject (or null to keep)
 * @param position new manual position (or null to keep)
 */
public record UpdateMaterialFolderCommand(
        String name,
        Integer position
) {
}
