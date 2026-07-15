package com.acainfo.material.application.port.out;

import com.acainfo.material.application.dto.AiImageInput;

import java.util.List;

/**
 * Output port to the LLM that produces LaTeX documents.
 * Implemented by AnthropicLatexAdapter (Claude via official Java SDK).
 *
 * <p>Every method returns the FULL source of a compilable LaTeX document
 * (from \documentclass to \end{document}).</p>
 */
public interface LlmLatexPort {

    /**
     * GENERATE mode: from captures of exercises worked in class, produce
     * {@code exerciseCount} new similar exercises with reasoned solutions.
     * The resulting document carries a \title{short topic} the pipeline
     * parses to name the material.
     */
    String generateExercises(List<AiImageInput> images, int exerciseCount);

    /**
     * TRANSCRIBE mode: faithful clean transcription of a handwritten
     * whiteboard PDF. Illegible symbols become \textbf{[?]}.
     */
    String transcribeDocument(byte[] pdfBytes);

    /**
     * Fix loop: given a .tex that failed to compile and the compiler's
     * "error:" lines, return a minimally-corrected full document.
     */
    String fixLatex(String texSource, String compilationErrors);
}
