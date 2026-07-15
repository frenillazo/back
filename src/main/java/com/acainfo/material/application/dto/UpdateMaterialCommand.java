package com.acainfo.material.application.dto;

/**
 * Command for updating material metadata. Any null field is left unchanged.
 *
 * @param name              new name (or null to keep)
 * @param description       new description (or null to keep; pass empty string to clear)
 * @param visible           new visibility (or null to keep)
 * @param downloadDisabled  new downloadDisabled flag (or null to keep)
 * @param academicYear      new academic year, as its start year (or null to keep)
 * @param folderId          destination folder of the same subject (or null to keep)
 * @param clearFolder       true = move to the subject root; takes precedence over folderId
 *                          (same pattern as subject's clearYear: null cannot mean both
 *                          "keep" and "unset")
 */
public record UpdateMaterialCommand(
        String name,
        String description,
        Boolean visible,
        Boolean downloadDisabled,
        Integer academicYear,
        Long folderId,
        Boolean clearFolder
) {
}
