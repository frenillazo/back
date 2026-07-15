package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.UploadMaterialCommand;
import com.acainfo.material.application.port.out.FileStoragePort;
import com.acainfo.material.application.port.out.MaterialFolderRepositoryPort;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.exception.FolderSubjectMismatchException;
import com.acainfo.material.domain.model.Material;
import com.acainfo.material.domain.model.MaterialFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link MaterialUploadService}.
 * The clock is fixed at 2026-02-15 → current academic year 2025 (course "2025-26").
 */
@ExtendWith(MockitoExtension.class)
class MaterialUploadServiceTest {

    private static final Long SUBJECT_ID = 30L;
    private static final Long OTHER_SUBJECT_ID = 31L;
    private static final Long UPLOADER_ID = 1L;
    private static final Long FOLDER_ID = 7L;
    private static final int CURRENT_ACADEMIC_YEAR = 2025;

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-02-15T12:00:00Z"), ZoneId.of("UTC"));

    @Mock
    private MaterialRepositoryPort materialRepository;

    @Mock
    private MaterialFolderRepositoryPort materialFolderRepository;

    @Mock
    private FileStoragePort fileStorage;

    private MaterialUploadService service;

    @BeforeEach
    void setUp() {
        service = new MaterialUploadService(
                materialRepository, materialFolderRepository, fileStorage, FIXED_CLOCK);
    }

    private UploadMaterialCommand commandWithFolder(Long folderId) {
        return new UploadMaterialCommand(
                SUBJECT_ID,
                UPLOADER_ID,
                "Apuntes tema 1",
                null,
                "apuntes.pdf",
                "application/pdf",
                1024L,
                InputStream.nullInputStream(),
                folderId
        );
    }

    private void stubHappyStorageAndSave() {
        when(fileStorage.store(any(InputStream.class), anyString(), anyLong()))
                .thenReturn("subjects/30/doc.pdf");
        when(materialRepository.save(any(Material.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void uploadAssignsCurrentAcademicYear() {
        stubHappyStorageAndSave();

        Material saved = service.upload(commandWithFolder(null));

        assertThat(saved.getAcademicYear()).isEqualTo(CURRENT_ACADEMIC_YEAR);
        assertThat(saved.isVisible()).isTrue();
    }

    @Test
    void uploadWithoutFolderGoesToSubjectRoot() {
        stubHappyStorageAndSave();

        Material saved = service.upload(commandWithFolder(null));

        assertThat(saved.getFolderId()).isNull();
    }

    @Test
    void uploadWithFolderOfSameSubjectAssignsIt() {
        stubHappyStorageAndSave();
        when(materialFolderRepository.findById(FOLDER_ID))
                .thenReturn(Optional.of(MaterialFolder.builder()
                        .id(FOLDER_ID).subjectId(SUBJECT_ID).name("Prácticas").build()));

        Material saved = service.upload(commandWithFolder(FOLDER_ID));

        assertThat(saved.getFolderId()).isEqualTo(FOLDER_ID);
    }

    @Test
    void uploadRejectsFolderOfAnotherSubject() {
        when(materialFolderRepository.findById(FOLDER_ID))
                .thenReturn(Optional.of(MaterialFolder.builder()
                        .id(FOLDER_ID).subjectId(OTHER_SUBJECT_ID).name("Prácticas").build()));

        assertThatThrownBy(() -> service.upload(commandWithFolder(FOLDER_ID)))
                .isInstanceOf(FolderSubjectMismatchException.class);

        verify(fileStorage, never()).store(any(), anyString(), anyLong());
        verify(materialRepository, never()).save(any());
    }
}
