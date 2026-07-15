package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.AiImageInput;
import com.acainfo.material.application.dto.LatexCompilationResult;
import com.acainfo.material.application.dto.StoredCapture;
import com.acainfo.material.application.dto.UploadMaterialCommand;
import com.acainfo.material.application.port.in.UploadMaterialUseCase;
import com.acainfo.material.application.port.out.FileStoragePort;
import com.acainfo.material.application.port.out.LatexCompilerPort;
import com.acainfo.material.application.port.out.LlmLatexPort;
import com.acainfo.material.application.port.out.MaterialAiJobRepositoryPort;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.exception.MaterialAiJobNotFoundException;
import com.acainfo.material.domain.exception.MaterialNotFoundException;
import com.acainfo.material.domain.model.Material;
import com.acainfo.material.domain.model.MaterialAiJob;
import com.acainfo.material.domain.model.MaterialAiJobStatus;
import com.acainfo.shared.infrastructure.config.AnthropicProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Async pipeline of the AI LaTeX jobs: Claude -> tectonic (fix loop, max
 * {@code app.anthropic.max-fix-retries}) -> publish as material (+ .tex next
 * to the PDF, same UUID, no material row, for debugging and future re-runs).
 *
 * <p>Separate bean from {@link MaterialAiService} so @Async goes through the
 * proxy. ONLY the pipeline methods run on the dedicated single-thread executor
 * ("aiJobExecutor"): one job at a time on a 2 vCPU server.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialAiPipeline {

    private static final Pattern TITLE_PATTERN = Pattern.compile("\\\\title\\{([^}]*)}");
    private static final DateTimeFormatter DAY_MONTH = DateTimeFormatter.ofPattern("dd-MM");

    private final MaterialAiJobRepositoryPort jobRepository;
    private final MaterialRepositoryPort materialRepository;
    private final UploadMaterialUseCase uploadMaterialUseCase;
    private final FileStoragePort fileStorage;
    private final LlmLatexPort llmLatex;
    private final LatexCompilerPort latexCompiler;
    private final AnthropicProperties properties;
    private final Clock clock;

    @Async("aiJobExecutor")
    public void runGenerateJob(Long jobId, Long folderId, int exerciseCount,
                               Path capturesDir, List<StoredCapture> captures) {
        MaterialAiJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new MaterialAiJobNotFoundException(jobId));
        try {
            markRunning(job);

            List<AiImageInput> images = new ArrayList<>();
            for (StoredCapture capture : captures) {
                images.add(new AiImageInput(Files.readAllBytes(capture.path()), capture.mimeType()));
            }

            String tex = llmLatex.generateExercises(images, exerciseCount);
            CompiledDocument document = compileWithRetries(tex);

            String day = LocalDate.now(clock).format(DAY_MONTH);
            String topic = parseTitle(document.tex());
            String name = topic != null ? "Repaso %s — %s".formatted(day, topic) : "Repaso " + day;

            Long materialId = publish(job, folderId, name,
                    "Generado automáticamente con IA",
                    "repaso-" + day + ".pdf",
                    document);
            markCompleted(job, materialId);
        } catch (Exception e) {
            markFailed(job, e);
        } finally {
            deleteRecursively(capturesDir);
        }
    }

    @Async("aiJobExecutor")
    public void runTranscribeJob(Long jobId) {
        MaterialAiJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new MaterialAiJobNotFoundException(jobId));
        try {
            markRunning(job);

            Material source = materialRepository.findById(job.getSourceMaterialId())
                    .orElseThrow(() -> new MaterialNotFoundException(job.getSourceMaterialId()));
            byte[] pdfBytes;
            try (InputStream is = fileStorage.retrieve(source.getStoragePath())) {
                pdfBytes = is.readAllBytes();
            }

            String tex = llmLatex.transcribeDocument(pdfBytes);
            CompiledDocument document = compileWithRetries(tex);

            String baseName = source.getOriginalFilename().replaceAll("(?i)\\.pdf$", "");
            Long materialId = publish(job, source.getFolderId(),
                    source.getName() + " (a limpio)",
                    "Transcripción automática del original " + source.getName(),
                    baseName + "-a-limpio.pdf",
                    document);
            markCompleted(job, materialId);
        } catch (Exception e) {
            markFailed(job, e);
        }
    }

    /**
     * Compile; on failure send the .tex + "error:" lines back to Claude and
     * retry, up to max-fix-retries times. Standard mitigation for LLM LaTeX
     * (missing packages, unbalanced environments, math mode).
     */
    private CompiledDocument compileWithRetries(String tex) {
        String current = tex;
        LatexCompilationResult result = latexCompiler.compile(current);
        int attempts = 0;
        while (!result.success() && attempts < properties.getMaxFixRetries()) {
            attempts++;
            log.info("Compilación fallida, reintento {}/{} vía Claude", attempts, properties.getMaxFixRetries());
            current = llmLatex.fixLatex(current, result.errors());
            result = latexCompiler.compile(current);
        }
        if (!result.success()) {
            throw new IllegalStateException(
                    "El documento no compila tras " + attempts + " correcciones: " + result.errors());
        }
        return new CompiledDocument(result.pdf(), current);
    }

    /**
     * Publish the PDF as a material (born visible, with academic year and
     * folder) and store the .tex next to it: same UUID, .tex extension,
     * no material row.
     */
    private Long publish(MaterialAiJob job, Long folderId, String name, String description,
                         String originalFilename, CompiledDocument document) {
        UploadMaterialCommand command = new UploadMaterialCommand(
                job.getSubjectId(),
                job.getCreatedById(),
                name,
                description,
                originalFilename,
                "application/pdf",
                (long) document.pdf().length,
                new ByteArrayInputStream(document.pdf()),
                folderId);
        Material material = uploadMaterialUseCase.upload(command);

        String texFilename = material.getStoredFilename().replaceAll("(?i)\\.pdf$", "") + ".tex";
        fileStorage.store(new ByteArrayInputStream(document.tex().getBytes(StandardCharsets.UTF_8)),
                texFilename, job.getSubjectId());

        log.info("AI job {}: material {} publicado ('{}') con su .tex en storage",
                job.getId(), material.getId(), name);
        return material.getId();
    }

    /**
     * The GENERATE prompt requires \title{short topic}; if unparseable the
     * material falls back to plain "Repaso dd-mm".
     */
    static String parseTitle(String tex) {
        Matcher matcher = TITLE_PATTERN.matcher(tex);
        if (!matcher.find()) {
            return null;
        }
        String title = matcher.group(1).trim();
        return title.isBlank() ? null : title;
    }

    private void markRunning(MaterialAiJob job) {
        job.setStatus(MaterialAiJobStatus.RUNNING);
        jobRepository.save(job);
    }

    private void markCompleted(MaterialAiJob job, Long materialId) {
        job.setStatus(MaterialAiJobStatus.COMPLETED);
        job.setResultMaterialId(materialId);
        jobRepository.save(job);
        log.info("AI job {} COMPLETED (material {})", job.getId(), materialId);
    }

    private void markFailed(MaterialAiJob job, Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : e.toString();
        job.setStatus(MaterialAiJobStatus.FAILED);
        job.setErrorMessage(message.length() > 2000 ? message.substring(0, 2000) : message);
        jobRepository.save(job);
        log.error("AI job {} FAILED: {}", job.getId(), message, e);
    }

    private void deleteRecursively(Path dir) {
        if (dir == null) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.warn("No se pudo borrar la captura temporal {}", path);
                }
            });
        } catch (IOException e) {
            log.warn("No se pudo limpiar el directorio de capturas {}", dir);
        }
    }

    private record CompiledDocument(byte[] pdf, String tex) {
    }
}
