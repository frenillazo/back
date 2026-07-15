package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.LatexCompilationResult;
import com.acainfo.material.application.dto.StoredCapture;
import com.acainfo.material.application.dto.UploadMaterialCommand;
import com.acainfo.material.application.port.in.UploadMaterialUseCase;
import com.acainfo.material.application.port.out.FileStoragePort;
import com.acainfo.material.application.port.out.LatexCompilerPort;
import com.acainfo.material.application.port.out.LlmLatexPort;
import com.acainfo.material.application.port.out.MaterialAiJobRepositoryPort;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.model.Material;
import com.acainfo.material.domain.model.MaterialAiJob;
import com.acainfo.material.domain.model.MaterialAiJobStatus;
import com.acainfo.material.domain.model.MaterialAiJobType;
import com.acainfo.shared.infrastructure.config.AnthropicProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link MaterialAiPipeline} (all ports mocked).
 */
@ExtendWith(MockitoExtension.class)
class MaterialAiPipelineTest {

    private static final Long JOB_ID = 40L;
    private static final Long SUBJECT_ID = 30L;
    private static final Long ADMIN_ID = 1L;
    private static final Long FOLDER_ID = 7L;
    private static final byte[] PDF = "pdf-bytes".getBytes(StandardCharsets.UTF_8);
    private static final String TEX_OK = "\\documentclass{article}\\title{Integrales impropias}\\begin{document}x\\end{document}";

    @Mock
    private MaterialAiJobRepositoryPort jobRepository;
    @Mock
    private MaterialRepositoryPort materialRepository;
    @Mock
    private UploadMaterialUseCase uploadMaterialUseCase;
    @Mock
    private FileStoragePort fileStorage;
    @Mock
    private LlmLatexPort llmLatex;
    @Mock
    private LatexCompilerPort latexCompiler;

    private MaterialAiPipeline pipeline;

    @BeforeEach
    void setUp() {
        AnthropicProperties properties = new AnthropicProperties(); // max-fix-retries = 2 por defecto
        // 15-jul-2026 fijo: los nombres "Repaso dd-mm" son deterministas en los tests
        Clock clock = Clock.fixed(Instant.parse("2026-07-15T10:00:00Z"), ZoneId.of("Europe/Madrid"));
        pipeline = new MaterialAiPipeline(jobRepository, materialRepository, uploadMaterialUseCase,
                fileStorage, llmLatex, latexCompiler, properties, clock);
    }

    private MaterialAiJob pendingJob(MaterialAiJobType type, Long sourceMaterialId) {
        return MaterialAiJob.builder()
                .id(JOB_ID)
                .type(type)
                .subjectId(SUBJECT_ID)
                .sourceMaterialId(sourceMaterialId)
                .status(MaterialAiJobStatus.PENDING)
                .createdById(ADMIN_ID)
                .build();
    }

    private Material publishedMaterial() {
        return Material.builder()
                .id(55L)
                .subjectId(SUBJECT_ID)
                .storedFilename("deadbeef.pdf")
                .build();
    }

    private Path capturesDirWithOneImage() throws Exception {
        Path dir = Files.createTempDirectory("test-captures-");
        Files.write(dir.resolve("captura-0"), new byte[]{1, 2, 3});
        return dir;
    }

    @Nested
    class Generate {

        @Test
        void pipelineFelizPublicaConTemaDelTitleYGuardaElTex() throws Exception {
            MaterialAiJob job = pendingJob(MaterialAiJobType.GENERATE, null);
            when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
            when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(llmLatex.generateExercises(any(), eq(2))).thenReturn(TEX_OK);
            when(latexCompiler.compile(TEX_OK)).thenReturn(LatexCompilationResult.ok(PDF));
            when(uploadMaterialUseCase.upload(any())).thenReturn(publishedMaterial());
            Path capturesDir = capturesDirWithOneImage();

            pipeline.runGenerateJob(JOB_ID, FOLDER_ID, 2, capturesDir,
                    List.of(new StoredCapture(capturesDir.resolve("captura-0"), "image/png")));

            ArgumentCaptor<UploadMaterialCommand> captor = ArgumentCaptor.forClass(UploadMaterialCommand.class);
            verify(uploadMaterialUseCase).upload(captor.capture());
            UploadMaterialCommand command = captor.getValue();
            assertThat(command.name()).isEqualTo("Repaso 15-07 — Integrales impropias");
            assertThat(command.description()).isEqualTo("Generado automáticamente con IA");
            assertThat(command.originalFilename()).isEqualTo("repaso-15-07.pdf");
            assertThat(command.mimeType()).isEqualTo("application/pdf");
            assertThat(command.folderId()).isEqualTo(FOLDER_ID);
            assertThat(command.subjectId()).isEqualTo(SUBJECT_ID);
            assertThat(command.uploadedById()).isEqualTo(ADMIN_ID);

            // El .tex queda junto al PDF: mismo UUID, extensión .tex
            verify(fileStorage).store(any(ByteArrayInputStream.class), eq("deadbeef.tex"), eq(SUBJECT_ID));

            assertThat(job.getStatus()).isEqualTo(MaterialAiJobStatus.COMPLETED);
            assertThat(job.getResultMaterialId()).isEqualTo(55L);
            // PENDING->RUNNING y RUNNING->COMPLETED: dos saves
            verify(jobRepository, times(2)).save(job);
            // Las capturas temporales se borran siempre
            assertThat(Files.notExists(capturesDir)).isTrue();
        }

        @Test
        void sinTitleParseableElNombreCaeARepasoDdMm() throws Exception {
            MaterialAiJob job = pendingJob(MaterialAiJobType.GENERATE, null);
            when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
            when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(llmLatex.generateExercises(any(), anyInt()))
                    .thenReturn("\\documentclass{article}\\begin{document}x\\end{document}");
            when(latexCompiler.compile(anyString())).thenReturn(LatexCompilationResult.ok(PDF));
            when(uploadMaterialUseCase.upload(any())).thenReturn(publishedMaterial());
            Path capturesDir = capturesDirWithOneImage();

            pipeline.runGenerateJob(JOB_ID, null, 2, capturesDir,
                    List.of(new StoredCapture(capturesDir.resolve("captura-0"), "image/png")));

            ArgumentCaptor<UploadMaterialCommand> captor = ArgumentCaptor.forClass(UploadMaterialCommand.class);
            verify(uploadMaterialUseCase).upload(captor.capture());
            assertThat(captor.getValue().name()).isEqualTo("Repaso 15-07");
            assertThat(captor.getValue().folderId()).isNull();
        }

        @Test
        void reintentaConClaudeTrasUnErrorDeCompilacionYPublica() throws Exception {
            MaterialAiJob job = pendingJob(MaterialAiJobType.GENERATE, null);
            when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
            when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            String texRoto = "\\documentclass{article}\\broken";
            String texArreglado = TEX_OK;
            when(llmLatex.generateExercises(any(), anyInt())).thenReturn(texRoto);
            when(latexCompiler.compile(texRoto))
                    .thenReturn(LatexCompilationResult.failure("error: doc.tex:1: Undefined control sequence"));
            when(llmLatex.fixLatex(texRoto, "error: doc.tex:1: Undefined control sequence"))
                    .thenReturn(texArreglado);
            when(latexCompiler.compile(texArreglado)).thenReturn(LatexCompilationResult.ok(PDF));
            when(uploadMaterialUseCase.upload(any())).thenReturn(publishedMaterial());
            Path capturesDir = capturesDirWithOneImage();

            pipeline.runGenerateJob(JOB_ID, null, 2, capturesDir,
                    List.of(new StoredCapture(capturesDir.resolve("captura-0"), "image/png")));

            verify(llmLatex).fixLatex(texRoto, "error: doc.tex:1: Undefined control sequence");
            assertThat(job.getStatus()).isEqualTo(MaterialAiJobStatus.COMPLETED);
        }

        @Test
        void failedTrasAgotarLosReintentosSinPublicarNada() throws Exception {
            MaterialAiJob job = pendingJob(MaterialAiJobType.GENERATE, null);
            when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
            when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(llmLatex.generateExercises(any(), anyInt())).thenReturn("tex-1");
            when(llmLatex.fixLatex(anyString(), anyString())).thenReturn("tex-2", "tex-3");
            when(latexCompiler.compile(anyString()))
                    .thenReturn(LatexCompilationResult.failure("error: doc.tex:9: Missing $ inserted"));
            Path capturesDir = capturesDirWithOneImage();

            pipeline.runGenerateJob(JOB_ID, null, 2, capturesDir,
                    List.of(new StoredCapture(capturesDir.resolve("captura-0"), "image/png")));

            // intento inicial + 2 correcciones = 3 compilaciones, 2 fixes
            verify(latexCompiler, times(3)).compile(anyString());
            verify(llmLatex, times(2)).fixLatex(anyString(), anyString());
            verify(uploadMaterialUseCase, never()).upload(any());
            assertThat(job.getStatus()).isEqualTo(MaterialAiJobStatus.FAILED);
            assertThat(job.getErrorMessage()).contains("no compila").contains("Missing $ inserted");
            assertThat(Files.notExists(capturesDir)).isTrue();
        }
    }

    @Nested
    class Transcribe {

        private Material source() {
            return Material.builder()
                    .id(20L)
                    .subjectId(SUBJECT_ID)
                    .name("Pizarra 2026-05-12")
                    .originalFilename("2026-05-12.pdf")
                    .fileExtension("pdf")
                    .folderId(9L)
                    .storagePath("subjects/inf006/orig.pdf")
                    .build();
        }

        @Test
        void pipelineFelizHeredaCarpetaYNombraAlLimpio() {
            MaterialAiJob job = pendingJob(MaterialAiJobType.TRANSCRIBE, 20L);
            when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
            when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(materialRepository.findById(20L)).thenReturn(Optional.of(source()));
            when(fileStorage.retrieve("subjects/inf006/orig.pdf"))
                    .thenReturn(new ByteArrayInputStream(PDF));
            when(llmLatex.transcribeDocument(any())).thenReturn(TEX_OK);
            when(latexCompiler.compile(TEX_OK)).thenReturn(LatexCompilationResult.ok(PDF));
            when(uploadMaterialUseCase.upload(any())).thenReturn(publishedMaterial());

            pipeline.runTranscribeJob(JOB_ID);

            ArgumentCaptor<UploadMaterialCommand> captor = ArgumentCaptor.forClass(UploadMaterialCommand.class);
            verify(uploadMaterialUseCase).upload(captor.capture());
            UploadMaterialCommand command = captor.getValue();
            assertThat(command.name()).isEqualTo("Pizarra 2026-05-12 (a limpio)");
            assertThat(command.description()).isEqualTo("Transcripción automática del original Pizarra 2026-05-12");
            assertThat(command.originalFilename()).isEqualTo("2026-05-12-a-limpio.pdf");
            assertThat(command.folderId()).isEqualTo(9L); // misma carpeta que el original

            verify(fileStorage).store(any(ByteArrayInputStream.class), eq("deadbeef.tex"), eq(SUBJECT_ID));
            assertThat(job.getStatus()).isEqualTo(MaterialAiJobStatus.COMPLETED);
            assertThat(job.getResultMaterialId()).isEqualTo(55L);
        }

        @Test
        void unErrorDelLlmDejaElJobFailedConMensaje() {
            MaterialAiJob job = pendingJob(MaterialAiJobType.TRANSCRIBE, 20L);
            when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
            when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(materialRepository.findById(20L)).thenReturn(Optional.of(source()));
            when(fileStorage.retrieve(anyString())).thenReturn(new ByteArrayInputStream(PDF));
            when(llmLatex.transcribeDocument(any()))
                    .thenThrow(new RuntimeException("ANTHROPIC_API_KEY no configurada"));

            pipeline.runTranscribeJob(JOB_ID);

            assertThat(job.getStatus()).isEqualTo(MaterialAiJobStatus.FAILED);
            assertThat(job.getErrorMessage()).contains("ANTHROPIC_API_KEY");
            verify(uploadMaterialUseCase, never()).upload(any());
        }
    }

    @Nested
    class ParseTitle {

        @Test
        void extraeElTemaYDevuelveNullSiFaltaOEstaVacio() {
            assertThat(MaterialAiPipeline.parseTitle("\\title{Grafos y árboles} resto")).isEqualTo("Grafos y árboles");
            assertThat(MaterialAiPipeline.parseTitle("\\title{  }")).isNull();
            assertThat(MaterialAiPipeline.parseTitle("sin titulo")).isNull();
        }
    }
}
