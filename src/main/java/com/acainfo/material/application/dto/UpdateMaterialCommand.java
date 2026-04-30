package com.acainfo.material.application.dto;

/**
 * Command for updating material metadata. Any null field is left unchanged.
 *
 * @param name              new name (or null to keep)
 * @param description       new description (or null to keep; pass empty string to clear)
 * @param visible           new visibility (or null to keep)
 * @param downloadDisabled  new downloadDisabled flag (or null to keep)
 */
public record UpdateMaterialCommand(
        String name,
        String description,
        Boolean visible,
        Boolean downloadDisabled
) {
}
