package com.acainfo.material.application.dto;

/**
 * Command to launch a TRANSCRIBE job: an already-published whiteboard PDF ->
 * clean LaTeX transcription, published next to the original.
 *
 * @param materialId  Source material (must be a PDF)
 * @param createdById Admin launching the job
 */
public record TranscribeAiMaterialCommand(
        Long materialId,
        Long createdById
) {
}
