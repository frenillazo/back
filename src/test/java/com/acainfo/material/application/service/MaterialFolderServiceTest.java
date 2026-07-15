package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.UpdateMaterialFolderCommand;
import com.acainfo.material.application.port.out.MaterialFolderRepositoryPort;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.exception.DuplicateFolderNameException;
import com.acainfo.material.domain.exception.MaterialFolderNotFoundException;
import com.acainfo.material.domain.model.MaterialFolder;
import com.acainfo.subject.application.port.in.GetSubjectUseCase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link MaterialFolderService}.
 */
@ExtendWith(MockitoExtension.class)
class MaterialFolderServiceTest {

    private static final Long SUBJECT_ID = 30L;
    private static final Long FOLDER_ID = 7L;

    @Mock
    private MaterialFolderRepositoryPort folderRepository;

    @Mock
    private MaterialRepositoryPort materialRepository;

    @Mock
    private GetSubjectUseCase getSubjectUseCase;

    @InjectMocks
    private MaterialFolderService service;

    private MaterialFolder folder(Long id, String name, int position) {
        return MaterialFolder.builder()
                .id(id)
                .subjectId(SUBJECT_ID)
                .name(name)
                .position(position)
                .build();
    }

    @Nested
    class Create {

        @Test
        void createsFolderAppendedAtTheEnd() {
            when(folderRepository.existsBySubjectIdAndName(SUBJECT_ID, "Prácticas")).thenReturn(false);
            when(folderRepository.findBySubjectId(SUBJECT_ID))
                    .thenReturn(List.of(folder(1L, "Teoría", 0), folder(2L, "Exámenes", 4)));
            when(folderRepository.save(any(MaterialFolder.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MaterialFolder created = service.create(SUBJECT_ID, "  Prácticas  ");

            assertThat(created.getName()).isEqualTo("Prácticas");
            assertThat(created.getSubjectId()).isEqualTo(SUBJECT_ID);
            assertThat(created.getPosition()).isEqualTo(5);
        }

        @Test
        void rejectsDuplicateNameWithinSubject() {
            when(folderRepository.existsBySubjectIdAndName(SUBJECT_ID, "Teoría")).thenReturn(true);

            assertThatThrownBy(() -> service.create(SUBJECT_ID, "Teoría"))
                    .isInstanceOf(DuplicateFolderNameException.class);

            verify(folderRepository, never()).save(any());
        }
    }

    @Nested
    class Update {

        @Test
        void renamesAndReorders() {
            when(folderRepository.findById(FOLDER_ID))
                    .thenReturn(Optional.of(folder(FOLDER_ID, "Teoría", 0)));
            when(folderRepository.existsBySubjectIdAndName(SUBJECT_ID, "Temario")).thenReturn(false);
            when(folderRepository.save(any(MaterialFolder.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MaterialFolder updated = service.update(
                    FOLDER_ID, new UpdateMaterialFolderCommand("Temario", 3));

            assertThat(updated.getName()).isEqualTo("Temario");
            assertThat(updated.getPosition()).isEqualTo(3);
        }

        @Test
        void rejectsRenameToExistingName() {
            when(folderRepository.findById(FOLDER_ID))
                    .thenReturn(Optional.of(folder(FOLDER_ID, "Teoría", 0)));
            when(folderRepository.existsBySubjectIdAndName(SUBJECT_ID, "Exámenes")).thenReturn(true);

            assertThatThrownBy(() -> service.update(
                    FOLDER_ID, new UpdateMaterialFolderCommand("Exámenes", null)))
                    .isInstanceOf(DuplicateFolderNameException.class);
        }

        @Test
        void throwsWhenFolderNotFound() {
            when(folderRepository.findById(FOLDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(
                    FOLDER_ID, new UpdateMaterialFolderCommand("Temario", null)))
                    .isInstanceOf(MaterialFolderNotFoundException.class);
        }
    }

    @Nested
    class Delete {

        @Test
        void movesMaterialsToRootBeforeDeletingFolder() {
            when(folderRepository.findById(FOLDER_ID))
                    .thenReturn(Optional.of(folder(FOLDER_ID, "Teoría", 0)));
            when(materialRepository.clearFolderId(FOLDER_ID)).thenReturn(4);

            service.delete(FOLDER_ID);

            InOrder inOrder = inOrder(materialRepository, folderRepository);
            inOrder.verify(materialRepository).clearFolderId(FOLDER_ID);
            inOrder.verify(folderRepository).delete(FOLDER_ID);
        }

        @Test
        void throwsWhenFolderNotFound() {
            when(folderRepository.findById(FOLDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(FOLDER_ID))
                    .isInstanceOf(MaterialFolderNotFoundException.class);

            verify(materialRepository, never()).clearFolderId(any());
            verify(folderRepository, never()).delete(any());
        }
    }
}
