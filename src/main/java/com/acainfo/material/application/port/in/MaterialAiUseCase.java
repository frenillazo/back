package com.acainfo.material.application.port.in;

import com.acainfo.material.application.dto.GenerateAiMaterialCommand;
import com.acainfo.material.application.dto.TranscribeAiMaterialCommand;
import com.acainfo.material.domain.model.MaterialAiJob;

/**
 * Use case for the AI LaTeX generator/transcriber.
 * Creating a job returns immediately (PENDING); the pipeline runs on a
 * dedicated single-thread executor and the frontend polls getJob().
 */
public interface MaterialAiUseCase {

    /**
     * Launch a GENERATE job (captures -> new practice exercises PDF).
     */
    MaterialAiJob createGenerateJob(GenerateAiMaterialCommand command);

    /**
     * Launch a TRANSCRIBE job (whiteboard PDF -> clean transcription PDF).
     */
    MaterialAiJob createTranscribeJob(TranscribeAiMaterialCommand command);

    /**
     * Poll a job's state.
     *
     * @throws com.acainfo.material.domain.exception.MaterialAiJobNotFoundException if not found
     */
    MaterialAiJob getJob(Long jobId);
}
