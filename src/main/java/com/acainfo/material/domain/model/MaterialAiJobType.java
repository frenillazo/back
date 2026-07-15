package com.acainfo.material.domain.model;

/**
 * Kind of AI LaTeX job.
 */
public enum MaterialAiJobType {
    /** Capturas de enunciados -> ejercicios de repaso nuevos con solución (PDF). */
    GENERATE,
    /** Pizarra manuscrita ya publicada (PDF) -> transcripción a limpio (PDF). */
    TRANSCRIBE
}
