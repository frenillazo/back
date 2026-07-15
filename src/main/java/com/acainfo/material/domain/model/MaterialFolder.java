package com.acainfo.material.domain.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * MaterialFolder domain entity - Anemic model with Lombok.
 * A per-subject folder for organizing materials (single level, no nesting).
 * Materials with {@code folderId == null} live at the subject root ("sin carpeta").
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class MaterialFolder {

    private Long id;

    /**
     * Subject this folder belongs to. Folders are per-subject.
     */
    private Long subjectId;

    /**
     * Display name, unique within the subject.
     */
    private String name;

    /**
     * Manual ordering set by the admin (ascending).
     */
    @Builder.Default
    private int position = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
