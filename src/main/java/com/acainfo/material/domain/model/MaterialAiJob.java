package com.acainfo.material.domain.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * MaterialAiJob domain entity - Anemic model with Lombok.
 * Async job of the LaTeX generator/transcriber (Claude + tectonic).
 *
 * <p>The request returns the job id immediately and the frontend polls its
 * status; the heavy work happens in a dedicated single-thread executor.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class MaterialAiJob {

    private Long id;

    private MaterialAiJobType type;

    /**
     * Subject the resulting material will belong to.
     */
    private Long subjectId;

    /**
     * Original material being transcribed (TRANSCRIBE only, null for GENERATE).
     */
    private Long sourceMaterialId;

    private MaterialAiJobStatus status;

    /**
     * Human-readable error shown to the admin when status is FAILED.
     */
    private String errorMessage;

    /**
     * Material published on success (null until COMPLETED).
     */
    private Long resultMaterialId;

    /**
     * Admin who launched the job.
     */
    private Long createdById;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
