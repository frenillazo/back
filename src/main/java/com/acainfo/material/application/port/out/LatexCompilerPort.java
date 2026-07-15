package com.acainfo.material.application.port.out;

import com.acainfo.material.application.dto.LatexCompilationResult;

/**
 * Output port to the LaTeX compiler.
 * Implemented by TectonicCompilerAdapter (tectonic binary via ProcessBuilder).
 */
public interface LatexCompilerPort {

    /**
     * Compile a full .tex source into a PDF.
     * Never throws on ordinary compilation errors: they come back in the
     * result so the pipeline can feed them to the fix loop. Environment-level
     * failures (missing binary, I/O) do throw.
     *
     * @throws com.acainfo.material.domain.exception.LatexCompilationException
     *         if the compiler process cannot run at all
     */
    LatexCompilationResult compile(String texSource);
}
