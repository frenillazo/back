package com.acainfo.material.infrastructure.adapter.in.rest;

import com.acainfo.material.application.dto.GenerateAiMaterialCommand;
import com.acainfo.material.application.dto.TranscribeAiMaterialCommand;
import com.acainfo.material.application.port.in.MaterialAiUseCase;
import com.acainfo.material.domain.exception.InvalidAiJobRequestException;
import com.acainfo.material.domain.model.MaterialAiJob;
import com.acainfo.material.domain.model.MaterialAiJobStatus;
import com.acainfo.material.domain.model.MaterialAiJobType;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.GenerateAiMaterialRequest;
import com.acainfo.material.infrastructure.adapter.in.rest.mapper.MaterialAiRestMapper;
import com.acainfo.security.userdetails.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link MaterialAiController} (validation + command building).
 */
@ExtendWith(MockitoExtension.class)
class MaterialAiControllerTest {

    private static final Long ADMIN_ID = 1L;

    @Mock
    private MaterialAiUseCase materialAiUseCase;
    @Mock
    private MaterialAiRestMapper mapper;
    @Mock
    private CustomUserDetails userDetails;

    @InjectMocks
    private MaterialAiController controller;

    private MaterialAiJob job() {
        return MaterialAiJob.builder()
                .id(40L).type(MaterialAiJobType.GENERATE)
                .status(MaterialAiJobStatus.PENDING).build();
    }

    @Test
    void generateConstruyeElCommandConDefaultDeDosEjerciciosYDevuelve201() throws Exception {
        when(userDetails.getUserId()).thenReturn(ADMIN_ID);
        when(materialAiUseCase.createGenerateJob(any())).thenReturn(job());
        MockMultipartFile image = new MockMultipartFile(
                "images", "captura.png", "image/png", new byte[]{1, 2, 3});

        var response = controller.generate(
                new GenerateAiMaterialRequest(30L, 7L, null), List.of(image), userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<GenerateAiMaterialCommand> captor =
                ArgumentCaptor.forClass(GenerateAiMaterialCommand.class);
        verify(materialAiUseCase).createGenerateJob(captor.capture());
        GenerateAiMaterialCommand command = captor.getValue();
        assertThat(command.subjectId()).isEqualTo(30L);
        assertThat(command.folderId()).isEqualTo(7L);
        assertThat(command.createdById()).isEqualTo(ADMIN_ID);
        assertThat(command.exerciseCount()).isEqualTo(2); // default cuando no se envía
        assertThat(command.images()).hasSize(1);
        assertThat(command.images().get(0).mimeType()).isEqualTo("image/png");
        assertThat(command.images().get(0).data()).containsExactly(1, 2, 3);
    }

    @Test
    void generateRechazaFormatosDeImagenNoSoportados() {
        MockMultipartFile pdf = new MockMultipartFile(
                "images", "doc.pdf", "application/pdf", new byte[]{1});

        assertThatThrownBy(() -> controller.generate(
                new GenerateAiMaterialRequest(30L, null, 2), List.of(pdf), userDetails))
                .isInstanceOf(InvalidAiJobRequestException.class)
                .hasMessageContaining("application/pdf");

        verify(materialAiUseCase, never()).createGenerateJob(any());
    }

    @Test
    void transcribeLanzaElJobConElAdminAutenticadoYDevuelve201() {
        when(userDetails.getUserId()).thenReturn(ADMIN_ID);
        when(materialAiUseCase.createTranscribeJob(any())).thenReturn(job());

        var response = controller.transcribe(20L, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<TranscribeAiMaterialCommand> captor =
                ArgumentCaptor.forClass(TranscribeAiMaterialCommand.class);
        verify(materialAiUseCase).createTranscribeJob(captor.capture());
        assertThat(captor.getValue().materialId()).isEqualTo(20L);
        assertThat(captor.getValue().createdById()).isEqualTo(ADMIN_ID);
    }
}
