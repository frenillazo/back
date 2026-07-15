package com.acainfo.material.infrastructure.adapter.out.latex;

import com.acainfo.material.application.dto.LatexCompilationResult;
import com.acainfo.material.application.port.out.LatexCompilerPort;
import com.acainfo.material.domain.exception.LatexCompilationException;
import com.acainfo.shared.infrastructure.config.AnthropicProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * LatexCompilerPort adapter over the tectonic binary (ProcessBuilder).
 *
 * <p>Depends on tectonic-only flags and error format ("error: doc.tex:line: msg"):
 * do NOT swap it for MiKTeX/xelatex. {@code --untrusted} because the .tex is
 * written by an LLM. Each compilation runs in a fresh temp dir, always removed.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TectonicCompilerAdapter implements LatexCompilerPort {

    private static final String TEX_FILENAME = "doc.tex";
    private static final String PDF_FILENAME = "doc.pdf";

    private final AnthropicProperties properties;

    @Override
    public LatexCompilationResult compile(String texSource) {
        Path workDir;
        try {
            workDir = Files.createTempDirectory("acainfo-tex-");
        } catch (IOException e) {
            throw new LatexCompilationException("No se pudo crear el directorio de compilación", e);
        }

        try {
            Files.writeString(workDir.resolve(TEX_FILENAME), texSource, StandardCharsets.UTF_8);

            ProcessBuilder pb = new ProcessBuilder(
                    properties.getTectonic().getBinaryPath(),
                    "--untrusted",
                    "--chatter", "minimal",
                    "--outdir", workDir.toString(),
                    workDir.resolve(TEX_FILENAME).toString());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            // La salida se consume en otro hilo: si el proceso se cuelga sin cerrar
            // stdout, el waitFor con timeout sigue mandando y podemos matarlo.
            CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
                try (InputStream is = process.getInputStream()) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    return "";
                }
            });

            long timeoutSeconds = properties.getTectonic().getTimeoutSeconds();
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                log.warn("tectonic superó el timeout de {}s; proceso destruido", timeoutSeconds);
                return LatexCompilationResult.failure(
                        "La compilación superó el tiempo máximo (" + timeoutSeconds + "s)");
            }

            String output = outputFuture.get(5, TimeUnit.SECONDS);

            if (process.exitValue() == 0) {
                byte[] pdf = Files.readAllBytes(workDir.resolve(PDF_FILENAME));
                log.debug("tectonic OK: PDF de {} bytes", pdf.length);
                return LatexCompilationResult.ok(pdf);
            }

            String errors = extractErrorLines(output);
            log.info("tectonic falló (exit={}): {}", process.exitValue(), errors);
            return LatexCompilationResult.failure(errors);

        } catch (IOException e) {
            throw new LatexCompilationException(
                    "No se pudo ejecutar tectonic (" + properties.getTectonic().getBinaryPath() + ")", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LatexCompilationException("Compilación interrumpida", e);
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            throw new LatexCompilationException("No se pudo leer la salida de tectonic", e);
        } finally {
            deleteRecursively(workDir);
        }
    }

    /**
     * Tectonic emits parseable lines "error: doc.tex:12: Undefined control sequence".
     * If none match (unexpected format), fall back to the output tail.
     */
    static String extractErrorLines(String output) {
        String errorLines = Stream.of(output.split("\n"))
                .filter(line -> line.startsWith("error:"))
                .collect(Collectors.joining("\n"));
        if (!errorLines.isBlank()) {
            return errorLines;
        }
        String trimmed = output.trim();
        return trimmed.length() <= 1500 ? trimmed : trimmed.substring(trimmed.length() - 1500);
    }

    private void deleteRecursively(Path dir) {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.warn("No se pudo borrar {} del directorio de compilación", path);
                }
            });
        } catch (IOException e) {
            log.warn("No se pudo limpiar el directorio de compilación {}", dir);
        }
    }
}
