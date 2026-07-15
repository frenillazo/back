package com.acainfo.material.domain.exception;

/**
 * Environment-level failure running the LaTeX compiler (missing binary, I/O).
 * Ordinary compilation errors are NOT an exception: they travel in
 * LatexCompilationResult so the pipeline can retry with Claude.
 */
public class LatexCompilationException extends RuntimeException {

    public LatexCompilationException(String message) {
        super(message);
    }

    public LatexCompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}
