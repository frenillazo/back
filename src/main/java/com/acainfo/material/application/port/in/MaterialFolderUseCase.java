package com.acainfo.material.application.port.in;

import com.acainfo.material.application.dto.UpdateMaterialFolderCommand;
import com.acainfo.material.domain.model.MaterialFolder;

import java.util.List;

/**
 * Input port for material folder management (per-subject folders, single level).
 */
public interface MaterialFolderUseCase {

    /**
     * List the folders of a subject ordered by position.
     */
    List<MaterialFolder> getBySubjectId(Long subjectId);

    /**
     * Create a folder for a subject. Name must be unique within the subject.
     * The new folder is appended at the end (max position + 1).
     */
    MaterialFolder create(Long subjectId, String name);

    /**
     * Rename and/or reorder a folder (null fields are left unchanged).
     */
    MaterialFolder update(Long folderId, UpdateMaterialFolderCommand command);

    /**
     * Delete a folder. Its materials are sent back to the subject root
     * (bulk {@code clearFolderId}) BEFORE deleting; materials are never deleted.
     */
    void delete(Long folderId);
}
