package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.UpdateMaterialCommand;
import com.acainfo.material.application.port.out.MaterialFolderRepositoryPort;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.exception.FolderSubjectMismatchException;
import com.acainfo.material.domain.model.Material;
import com.acainfo.material.domain.model.MaterialFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link MaterialUpdateService}.
 */
@ExtendWith(MockitoExtension.class)
class MaterialUpdateServiceTest {

    private static final Long MATERIAL_ID = 10L;
    private static final Long SUBJECT_ID = 30L;
    private static final Long FOLDER_ID = 7L;
    private static final Long OTHER_SUBJECT_FOLDER_ID = 8L;

    @Mock
    private MaterialRepositoryPort materialRepository;

    @Mock
    private MaterialFolderRepositoryPort materialFolderRepository;

    @InjectMocks
    private MaterialUpdateService service;

    private static UpdateMaterialCommand command(
            String name, Integer academicYear, Long folderId, Boolean clearFolder) {
        return new UpdateMaterialCommand(name, null, null, null, academicYear, folderId, clearFolder);
    }

    @BeforeEach
    void setUp() {
        Material material = Material.builder()
                .id(MATERIAL_ID)
                .subjectId(SUBJECT_ID)
                .name("Examen 2024")
                .academicYear(2024)
                .folderId(FOLDER_ID)
                .visible(true)
                .downloadDisabled(false)
                .build();
        when(materialRepository.findById(MATERIAL_ID)).thenReturn(Optional.of(material));
        // save() is not reached in the rejection test
        lenient().when(materialRepository.save(any(Material.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void updatesAcademicYearWhenProvided() {
        Material updated = service.updateMetadata(
                MATERIAL_ID, command(null, 2025, null, null));

        assertThat(updated.getAcademicYear()).isEqualTo(2025);
    }

    @Test
    void keepsAcademicYearAndFolderWhenNotProvided() {
        Material updated = service.updateMetadata(
                MATERIAL_ID, command("Examen práctica", null, null, null));

        assertThat(updated.getAcademicYear()).isEqualTo(2024);
        assertThat(updated.getFolderId()).isEqualTo(FOLDER_ID);
        assertThat(updated.getName()).isEqualTo("Examen práctica");
    }

    @Test
    void movesMaterialToAnotherFolderOfSameSubject() {
        Long newFolderId = 9L;
        when(materialFolderRepository.findById(newFolderId))
                .thenReturn(Optional.of(MaterialFolder.builder()
                        .id(newFolderId).subjectId(SUBJECT_ID).name("Parciales").build()));

        Material updated = service.updateMetadata(
                MATERIAL_ID, command(null, null, newFolderId, null));

        assertThat(updated.getFolderId()).isEqualTo(newFolderId);
    }

    @Test
    void clearFolderMovesMaterialToRootAndWinsOverFolderId() {
        Material updated = service.updateMetadata(
                MATERIAL_ID, command(null, null, 9L, true));

        assertThat(updated.getFolderId()).isNull();
    }

    @Test
    void rejectsMoveToFolderOfAnotherSubject() {
        when(materialFolderRepository.findById(OTHER_SUBJECT_FOLDER_ID))
                .thenReturn(Optional.of(MaterialFolder.builder()
                        .id(OTHER_SUBJECT_FOLDER_ID).subjectId(99L).name("Ajena").build()));

        assertThatThrownBy(() -> service.updateMetadata(
                MATERIAL_ID, command(null, null, OTHER_SUBJECT_FOLDER_ID, null)))
                .isInstanceOf(FolderSubjectMismatchException.class);
    }
}
