package com.acainfo.material.application.port.out;

import com.acainfo.material.domain.model.MaterialFolder;

import java.util.List;
import java.util.Optional;

/**
 * Output port for MaterialFolder persistence.
 */
public interface MaterialFolderRepositoryPort {

    /**
     * Save or update a folder.
     */
    MaterialFolder save(MaterialFolder folder);

    /**
     * Find folder by ID.
     */
    Optional<MaterialFolder> findById(Long id);

    /**
     * Find all folders for a subject, ordered by position (then name).
     */
    List<MaterialFolder> findBySubjectId(Long subjectId);

    /**
     * Find multiple folders by IDs (used to batch-resolve folder names).
     */
    List<MaterialFolder> findAllByIds(List<Long> ids);

    /**
     * Check name uniqueness within a subject.
     */
    boolean existsBySubjectIdAndName(Long subjectId, String name);

    /**
     * Delete a folder by ID.
     */
    void delete(Long id);
}
