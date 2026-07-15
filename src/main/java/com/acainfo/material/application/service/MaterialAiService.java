package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.GenerateAiMaterialCommand;
import com.acainfo.material.application.dto.StoredCapture;
import com.acainfo.material.application.dto.TranscribeAiMaterialCommand;
import com.acainfo.material.application.port.in.MaterialAiUseCase;
import com.acainfo.material.application.port.out.MaterialAiJobRepositoryPort;
import com.acainfo.material.application.port.out.MaterialFolderRepositoryPort;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.exception.FolderSubjectMismatchException;
import com.acainfo.material.domain.exception.InvalidAiJobRequestException;
import com.acainfo.material.domain.exception.MaterialAiJobNotFoundException;
import com.acainfo.material.domain.exception.MaterialFolderNotFoundException;
import com.acainfo.material.domain.exception.MaterialNotFoundException;
import com.acainfo.material.domain.model.Material;
import com.acainfo.material.domain.model.MaterialAiJob;
import com.acainfo.material.domain.model.MaterialAiJobStatus;
import com.acainfo.material.domain.model.MaterialAiJobType;
import com.acainfo.material.domain.model.MaterialFolder;
import com.acainfo.subject.application.port.in.GetSubjectUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that creates AI LaTeX jobs and hands them to the async pipeline.
 * Validation happens HERE (fail fast, before paying an API call); the heavy
 * work happens in {@link MaterialAiPipeline} on the dedicated executor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialAiService implements MaterialAiUseCase {

    private final MaterialAiJobRepositoryPort jobRepository;
    private final MaterialRepositoryPort materialRepository;
    private final MaterialFolderRepositoryPort materialFolderRepository;
    private final GetSubjectUseCase getSubjectUseCase;
    private final MaterialAiPipeline pipeline;

    @Override
    public MaterialAiJob createGenerateJob(GenerateAiMaterialCommand command) {
        if (command.images() == null || command.images().isEmpty()) {
            throw new InvalidAiJobRequestException("Hacen falta capturas para generar ejercicios");
        }
        // Throws SubjectNotFoundException if the subject does not exist
        getSubjectUseCase.getById(command.subjectId());
        validateFolderBelongsToSubject(command.folderId(), command.subjectId());

        // Captures go to tmp NOW: the multipart InputStream/bytes die with the
        // request, and the job may wait queued behind another one for minutes.
        Path capturesDir;
        List<StoredCapture> captures = new ArrayList<>();
        try {
            capturesDir = Files.createTempDirectory("acainfo-ai-captures-");
            for (int i = 0; i < command.images().size(); i++) {
                Path file = capturesDir.resolve("captura-" + i);
                Files.write(file, command.images().get(i).data());
                captures.add(new StoredCapture(file, command.images().get(i).mimeType()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudieron guardar las capturas temporales", e);
        }

        MaterialAiJob job = jobRepository.save(MaterialAiJob.builder()
                .type(MaterialAiJobType.GENERATE)
                .subjectId(command.subjectId())
                .status(MaterialAiJobStatus.PENDING)
                .createdById(command.createdById())
                .build());

        log.info("AI job GENERATE creado: id={}, subject={}, capturas={}, ejercicios={}",
                job.getId(), command.subjectId(), captures.size(), command.exerciseCount());
        pipeline.runGenerateJob(job.getId(), command.folderId(), command.exerciseCount(), capturesDir, captures);
        return job;
    }

    @Override
    public MaterialAiJob createTranscribeJob(TranscribeAiMaterialCommand command) {
        Material source = materialRepository.findById(command.materialId())
                .orElseThrow(() -> new MaterialNotFoundException(command.materialId()));
        if (!"pdf".equalsIgnoreCase(source.getFileExtension())) {
            throw new InvalidAiJobRequestException(
                    "Solo se pueden transcribir materiales PDF (este es ." + source.getFileExtension() + ")");
        }

        MaterialAiJob job = jobRepository.save(MaterialAiJob.builder()
                .type(MaterialAiJobType.TRANSCRIBE)
                .subjectId(source.getSubjectId())
                .sourceMaterialId(source.getId())
                .status(MaterialAiJobStatus.PENDING)
                .createdById(command.createdById())
                .build());

        log.info("AI job TRANSCRIBE creado: id={}, material origen={} ('{}')",
                job.getId(), source.getId(), source.getName());
        pipeline.runTranscribeJob(job.getId());
        return job;
    }

    @Override
    public MaterialAiJob getJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new MaterialAiJobNotFoundException(jobId));
    }

    private void validateFolderBelongsToSubject(Long folderId, Long subjectId) {
        if (folderId == null) {
            return;
        }
        MaterialFolder folder = materialFolderRepository.findById(folderId)
                .orElseThrow(() -> new MaterialFolderNotFoundException(folderId));
        if (!folder.getSubjectId().equals(subjectId)) {
            throw new FolderSubjectMismatchException(folderId, subjectId);
        }
    }
}
