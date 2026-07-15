package com.acainfo.material.application.dto;

import java.util.List;

/**
 * Command to launch a GENERATE job: captures of exercises worked in class ->
 * new practice exercises with solutions, published as a PDF material.
 *
 * @param subjectId     Subject the resulting material belongs to
 * @param folderId      Destination folder (null = subject root); must belong to the subject
 * @param createdById   Admin launching the job
 * @param exerciseCount Number of exercises to generate (default 2 at the API layer)
 * @param images        Captures (bytes + MIME); persisted to tmp while the job runs
 */
public record GenerateAiMaterialCommand(
        Long subjectId,
        Long folderId,
        Long createdById,
        int exerciseCount,
        List<AiImageInput> images
) {
}
