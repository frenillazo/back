package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.AiImageInput;
import com.acainfo.material.application.dto.GenerateAiMaterialCommand;
import com.acainfo.material.application.dto.StoredCapture;
import com.acainfo.material.application.dto.TranscribeAiMaterialCommand;
import com.acainfo.material.application.port.out.MaterialAiJobRepositoryPort;
import com.acainfo.material.application.port.out.MaterialFolderRepositoryPort;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.exception.FolderSubjectMismatchException;
import com.acainfo.material.domain.exception.InvalidAiJobRequestException;
import com.acainfo.material.domain.exception.MaterialAiJobNotFoundException;
import com.acainfo.material.domain.model.Material;
import com.acainfo.material.domain.model.MaterialAiJob;
import com.acainfo.material.domain.model.MaterialAiJobStatus;
import com.acainfo.material.domain.model.MaterialAiJobType;
import com.acainfo.material.domain.model.MaterialFolder;
import com.acainfo.subject.application.port.in.GetSubjectUseCase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link MaterialAiService} (job creation + validation).
 */
@ExtendWith(MockitoExtension.class)
class MaterialAiServiceTest {

    private static final Long SUBJECT_ID = 30L;
    private static final Long ADMIN_ID = 1L;

    @Mock
    private MaterialAiJobRepositoryPort jobRepository;
    @Mock
    private MaterialRepositoryPort materialRepository;
    @Mock
    private MaterialFolderRepositoryPort materialFolderRepository;
    @Mock
    private GetSubjectUseCase getSubjectUseCase;
    @Mock
    private MaterialAiPipeline pipeline;

    @InjectMocks
    private MaterialAiService service;

    private void stubSaveAssignsId() {
        when(jobRepository.save(any())).thenAnswer(inv -> {
            MaterialAiJob job = inv.getArgument(0);
            job.setId(40L);
            return job;
        });
    }

    @Nested
    class CreateGenerateJob {

        private GenerateAiMaterialCommand command(Long folderId) {
            return new GenerateAiMaterialCommand(SUBJECT_ID, folderId, ADMIN_ID, 2,
                    List.of(new AiImageInput(new byte[]{1, 2, 3}, "image/png")));
        }

        @Test
        void guardaElJobPendingPersisteLasCapturasYLanzaElPipeline() throws Exception {
            stubSaveAssignsId();

            MaterialAiJob job = service.createGenerateJob(command(null));

            assertThat(job.getId()).isEqualTo(40L);
            assertThat(job.getStatus()).isEqualTo(MaterialAiJobStatus.PENDING);
            assertThat(job.getType()).isEqualTo(MaterialAiJobType.GENERATE);
            assertThat(job.getSubjectId()).isEqualTo(SUBJECT_ID);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<StoredCapture>> capturesCaptor = ArgumentCaptor.forClass(List.class);
            ArgumentCaptor<Path> dirCaptor = ArgumentCaptor.forClass(Path.class);
            verify(pipeline).runGenerateJob(eq(40L), eq(null), eq(2),
                    dirCaptor.capture(), capturesCaptor.capture());

            // La captura queda persistida en tmp (el multipart muere al responder)
            assertThat(capturesCaptor.getValue()).hasSize(1);
            Path stored = capturesCaptor.getValue().get(0).path();
            assertThat(Files.readAllBytes(stored)).containsExactly(1, 2, 3);
            assertThat(capturesCaptor.getValue().get(0).mimeType()).isEqualTo("image/png");

            // limpieza del test (en producción la borra el pipeline en su finally)
            Files.deleteIfExists(stored);
            Files.deleteIfExists(dirCaptor.getValue());
        }

        @Test
        void sinCapturasRechazaSinCrearJob() {
            GenerateAiMaterialCommand sinImagenes =
                    new GenerateAiMaterialCommand(SUBJECT_ID, null, ADMIN_ID, 2, List.of());

            assertThatThrownBy(() -> service.createGenerateJob(sinImagenes))
                    .isInstanceOf(InvalidAiJobRequestException.class);

            verify(jobRepository, never()).save(any());
            verify(pipeline, never()).runGenerateJob(anyLong(), any(), anyInt(), any(), anyList());
        }

        @Test
        void carpetaDeOtraAsignaturaRechazadaAntesDePagarNada() {
            when(materialFolderRepository.findById(7L)).thenReturn(Optional.of(
                    MaterialFolder.builder().id(7L).subjectId(99L).name("Ajena").build()));

            assertThatThrownBy(() -> service.createGenerateJob(command(7L)))
                    .isInstanceOf(FolderSubjectMismatchException.class);

            verify(jobRepository, never()).save(any());
        }
    }

    @Nested
    class CreateTranscribeJob {

        @Test
        void guardaElJobConElMaterialOrigenYLanzaElPipeline() {
            stubSaveAssignsId();
            when(materialRepository.findById(20L)).thenReturn(Optional.of(Material.builder()
                    .id(20L).subjectId(SUBJECT_ID).fileExtension("pdf").name("Pizarra").build()));

            MaterialAiJob job = service.createTranscribeJob(new TranscribeAiMaterialCommand(20L, ADMIN_ID));

            assertThat(job.getType()).isEqualTo(MaterialAiJobType.TRANSCRIBE);
            assertThat(job.getStatus()).isEqualTo(MaterialAiJobStatus.PENDING);
            assertThat(job.getSourceMaterialId()).isEqualTo(20L);
            assertThat(job.getSubjectId()).isEqualTo(SUBJECT_ID);
            verify(pipeline).runTranscribeJob(40L);
        }

        @Test
        void materialQueNoEsPdfRechazado() {
            when(materialRepository.findById(20L)).thenReturn(Optional.of(Material.builder()
                    .id(20L).subjectId(SUBJECT_ID).fileExtension("zip").name("Prácticas").build()));

            assertThatThrownBy(() -> service.createTranscribeJob(new TranscribeAiMaterialCommand(20L, ADMIN_ID)))
                    .isInstanceOf(InvalidAiJobRequestException.class)
                    .hasMessageContaining("PDF");

            verify(jobRepository, never()).save(any());
        }
    }

    @Nested
    class GetJob {

        @Test
        void jobInexistenteLanzaNotFound() {
            when(jobRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getJob(99L))
                    .isInstanceOf(MaterialAiJobNotFoundException.class);
        }
    }
}
