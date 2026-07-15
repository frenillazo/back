package com.acainfo.material.application.dto;

/**
 * Outcome of compiling a .tex document.
 *
 * @param success true if the compiler produced a PDF
 * @param pdf     PDF bytes when success; null otherwise
 * @param errors  compiler "error:" lines when failed; null when success.
 *                Fed back to Claude in the fix loop.
 */
public record LatexCompilationResult(
        boolean success,
        byte[] pdf,
        String errors
) {

    public static LatexCompilationResult ok(byte[] pdf) {
        return new LatexCompilationResult(true, pdf, null);
    }

    public static LatexCompilationResult failure(String errors) {
        return new LatexCompilationResult(false, null, errors);
    }
}
