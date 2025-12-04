package com.acainfo.subject.application.service;

import com.acainfo.shared.factory.SubjectFactory;
import com.acainfo.subject.application.dto.CreateSubjectCommand;
import com.acainfo.subject.application.dto.UpdateSubjectCommand;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.exception.DuplicateSubjectCodeException;
import com.acainfo.subject.domain.exception.InvalidSubjectDataException;
import com.acainfo.subject.domain.exception.SubjectNotFoundException;
import com.acainfo.subject.domain.model.Degree;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.subject.domain.model.SubjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubjectService with Mockito.
 * Tests business logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubjectService Tests")
class SubjectServiceTest {

    @Mock
    private SubjectRepositoryPort subjectRepositoryPort;

    @InjectMocks
    private SubjectService subjectService;

    private Subject testSubject;

    @BeforeEach
    void setUp() {
        testSubject = SubjectFactory.defaultSubject();
    }

    @Nested
    @DisplayName("Create Subject Tests")
    class CreateSubjectTests {

        @Test
        @DisplayName("Should create subject successfully with valid data")
        void create_WithValidData_CreatesSubject() {
            // Given
            CreateSubjectCommand command = new CreateSubjectCommand(
                    "ING101",
                    "Programación I",
                    Degree.INGENIERIA_INFORMATICA
            );

            when(subjectRepositoryPort.existsByCode("ING101")).thenReturn(false);
            when(subjectRepositoryPort.save(any(Subject.class))).thenReturn(testSubject);

            // When
            Subject result = subjectService.create(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("ING101");
            assertThat(result.getName()).isEqualTo("Programación I");
            assertThat(result.getDegree()).isEqualTo(Degree.INGENIERIA_INFORMATICA);
            assertThat(result.getStatus()).isEqualTo(SubjectStatus.ACTIVE);
            assertThat(result.getCurrentGroupCount()).isEqualTo(0);

            verify(subjectRepositoryPort).existsByCode("ING101");
            verify(subjectRepositoryPort).save(any(Subject.class));
        }

        @Test
        @DisplayName("Should normalize code to uppercase")
        void create_WithLowercaseCode_NormalizesToUppercase() {
            // Given
            CreateSubjectCommand command = new CreateSubjectCommand(
                    "ing101",
                    "Programación I",
                    Degree.INGENIERIA_INFORMATICA
            );

            when(subjectRepositoryPort.existsByCode("ING101")).thenReturn(false);
            when(subjectRepositoryPort.save(any(Subject.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Subject result = subjectService.create(command);

            // Then
            assertThat(result.getCode()).isEqualTo("ING101");
            verify(subjectRepositoryPort).existsByCode("ING101");
        }

        @Test
        @DisplayName("Should trim whitespace from code and name")
        void create_WithWhitespace_TrimsFields() {
            // Given
            CreateSubjectCommand command = new CreateSubjectCommand(
                    "  ING101  ",
                    "  Programación I  ",
                    Degree.INGENIERIA_INFORMATICA
            );

            when(subjectRepositoryPort.existsByCode("ING101")).thenReturn(false);
            when(subjectRepositoryPort.save(any(Subject.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Subject result = subjectService.create(command);

            // Then
            assertThat(result.getCode()).isEqualTo("ING101");
            assertThat(result.getName()).isEqualTo("Programación I");
        }

        @Test
        @DisplayName("Should throw exception when code format is invalid")
        void create_WithInvalidCodeFormat_ThrowsException() {
            // Given - Invalid format (should be 3 letters + 3 digits)
            CreateSubjectCommand command = new CreateSubjectCommand(
                    "INVALID",
                    "Test Subject",
                    Degree.INGENIERIA_INFORMATICA
            );

            // When & Then
            assertThatThrownBy(() -> subjectService.create(command))
                    .isInstanceOf(InvalidSubjectDataException.class)
                    .hasMessageContaining("Code must be 3 uppercase letters followed by 3 digits");

            verify(subjectRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when code already exists")
        void create_WithDuplicateCode_ThrowsException() {
            // Given
            CreateSubjectCommand command = new CreateSubjectCommand(
                    "ING101",
                    "Programación I",
                    Degree.INGENIERIA_INFORMATICA
            );

            when(subjectRepositoryPort.existsByCode("ING101")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> subjectService.create(command))
                    .isInstanceOf(DuplicateSubjectCodeException.class)
                    .hasMessageContaining("ING101");

            verify(subjectRepositoryPort).existsByCode("ING101");
            verify(subjectRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when name is null or blank")
        void create_WithBlankName_ThrowsException() {
            // Given
            CreateSubjectCommand command = new CreateSubjectCommand(
                    "ING101",
                    "   ",
                    Degree.INGENIERIA_INFORMATICA
            );

            // When & Then
            assertThatThrownBy(() -> subjectService.create(command))
                    .isInstanceOf(InvalidSubjectDataException.class)
                    .hasMessageContaining("Name is required");

            verify(subjectRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Subject Tests")
    class UpdateSubjectTests {

        @Test
        @DisplayName("Should update subject successfully")
        void update_WithValidData_UpdatesSubject() {
            // Given
            Long subjectId = 1L;
            UpdateSubjectCommand command = new UpdateSubjectCommand(
                    "Programación Avanzada",
                    SubjectStatus.INACTIVE
            );

            Subject updatedSubject = testSubject.toBuilder()
                    .name("Programación Avanzada")
                    .status(SubjectStatus.INACTIVE)
                    .build();

            when(subjectRepositoryPort.findById(subjectId)).thenReturn(Optional.of(testSubject));
            when(subjectRepositoryPort.save(any(Subject.class))).thenReturn(updatedSubject);

            // When
            Subject result = subjectService.update(subjectId, command);

            // Then
            assertThat(result.getName()).isEqualTo("Programación Avanzada");
            assertThat(result.getStatus()).isEqualTo(SubjectStatus.INACTIVE);
            verify(subjectRepositoryPort).findById(subjectId);
            verify(subjectRepositoryPort).save(any(Subject.class));
        }

        @Test
        @DisplayName("Should trim whitespace from name")
        void update_WithWhitespace_TrimsName() {
            // Given
            Long subjectId = 1L;
            UpdateSubjectCommand command = new UpdateSubjectCommand(
                    "  New Name  ",
                    null
            );

            when(subjectRepositoryPort.findById(subjectId)).thenReturn(Optional.of(testSubject));
            when(subjectRepositoryPort.save(any(Subject.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Subject result = subjectService.update(subjectId, command);

            // Then
            assertThat(result.getName()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("Should skip null or blank fields")
        void update_WithNullFields_SkipsNullFields() {
            // Given
            Long subjectId = 1L;
            UpdateSubjectCommand command = new UpdateSubjectCommand(null, null);
            String originalName = testSubject.getName();
            SubjectStatus originalStatus = testSubject.getStatus();

            when(subjectRepositoryPort.findById(subjectId)).thenReturn(Optional.of(testSubject));
            when(subjectRepositoryPort.save(any(Subject.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Subject result = subjectService.update(subjectId, command);

            // Then
            assertThat(result.getName()).isEqualTo(originalName);
            assertThat(result.getStatus()).isEqualTo(originalStatus);
        }

        @Test
        @DisplayName("Should throw SubjectNotFoundException when subject not found")
        void update_WhenSubjectNotFound_ThrowsException() {
            // Given
            Long subjectId = 999L;
            UpdateSubjectCommand command = new UpdateSubjectCommand("New Name", SubjectStatus.INACTIVE);
            when(subjectRepositoryPort.findById(subjectId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> subjectService.update(subjectId, command))
                    .isInstanceOf(SubjectNotFoundException.class);

            verify(subjectRepositoryPort).findById(subjectId);
            verify(subjectRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Get Subject Tests")
    class GetSubjectTests {

        @Test
        @DisplayName("Should return subject when found by ID")
        void getById_WhenSubjectExists_ReturnsSubject() {
            // Given
            Long subjectId = 1L;
            when(subjectRepositoryPort.findById(subjectId)).thenReturn(Optional.of(testSubject));

            // When
            Subject result = subjectService.getById(subjectId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testSubject.getId());
            assertThat(result.getCode()).isEqualTo(testSubject.getCode());
            verify(subjectRepositoryPort).findById(subjectId);
        }

        @Test
        @DisplayName("Should throw SubjectNotFoundException when subject not found by ID")
        void getById_WhenSubjectNotFound_ThrowsException() {
            // Given
            Long subjectId = 999L;
            when(subjectRepositoryPort.findById(subjectId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> subjectService.getById(subjectId))
                    .isInstanceOf(SubjectNotFoundException.class)
                    .hasMessageContaining("999");

            verify(subjectRepositoryPort).findById(subjectId);
        }

        @Test
        @DisplayName("Should return subject when found by code")
        void getByCode_WhenSubjectExists_ReturnsSubject() {
            // Given
            String code = "ING101";
            when(subjectRepositoryPort.findByCode(code)).thenReturn(Optional.of(testSubject));

            // When
            Subject result = subjectService.getByCode(code);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo(code);
            verify(subjectRepositoryPort).findByCode(code);
        }

        @Test
        @DisplayName("Should throw SubjectNotFoundException when subject not found by code")
        void getByCode_WhenSubjectNotFound_ThrowsException() {
            // Given
            String code = "XXX999";
            when(subjectRepositoryPort.findByCode(code)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> subjectService.getByCode(code))
                    .isInstanceOf(SubjectNotFoundException.class)
                    .hasMessageContaining(code);

            verify(subjectRepositoryPort).findByCode(code);
        }
    }

    @Nested
    @DisplayName("Delete Subject Tests")
    class DeleteSubjectTests {

        @Test
        @DisplayName("Should delete subject successfully")
        void delete_WhenSubjectExists_DeletesSubject() {
            // Given
            Long subjectId = 1L;
            when(subjectRepositoryPort.findById(subjectId)).thenReturn(Optional.of(testSubject));

            // When
            subjectService.delete(subjectId);

            // Then
            verify(subjectRepositoryPort).findById(subjectId);
            verify(subjectRepositoryPort).delete(subjectId);
        }

        @Test
        @DisplayName("Should throw exception when subject has active groups")
        void delete_WhenSubjectHasGroups_ThrowsException() {
            // Given
            Long subjectId = 1L;
            Subject subjectWithGroups = testSubject.toBuilder()
                    .currentGroupCount(2)
                    .build();

            when(subjectRepositoryPort.findById(subjectId)).thenReturn(Optional.of(subjectWithGroups));

            // When & Then
            assertThatThrownBy(() -> subjectService.delete(subjectId))
                    .isInstanceOf(InvalidSubjectDataException.class)
                    .hasMessageContaining("Cannot delete subject with existing groups");

            verify(subjectRepositoryPort).findById(subjectId);
            verify(subjectRepositoryPort, never()).delete(anyLong());
        }

        @Test
        @DisplayName("Should throw SubjectNotFoundException when subject not found")
        void delete_WhenSubjectNotFound_ThrowsException() {
            // Given
            Long subjectId = 999L;
            when(subjectRepositoryPort.findById(subjectId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> subjectService.delete(subjectId))
                    .isInstanceOf(SubjectNotFoundException.class);

            verify(subjectRepositoryPort).findById(subjectId);
            verify(subjectRepositoryPort, never()).delete(anyLong());
        }
    }

    @Nested
    @DisplayName("Archive Subject Tests")
    class ArchiveSubjectTests {

        @Test
        @DisplayName("Should archive subject successfully")
        void archive_WhenSubjectExists_ArchivesSubject() {
            // Given
            Long subjectId = 1L;
            Subject archivedSubject = testSubject.toBuilder()
                    .status(SubjectStatus.ARCHIVED)
                    .build();

            when(subjectRepositoryPort.findById(subjectId)).thenReturn(Optional.of(testSubject));
            when(subjectRepositoryPort.save(any(Subject.class))).thenReturn(archivedSubject);

            // When
            Subject result = subjectService.archive(subjectId);

            // Then
            assertThat(result.getStatus()).isEqualTo(SubjectStatus.ARCHIVED);
            assertThat(result.isArchived()).isTrue();
            verify(subjectRepositoryPort).findById(subjectId);
            verify(subjectRepositoryPort).save(any(Subject.class));
        }

        @Test
        @DisplayName("Should throw SubjectNotFoundException when subject not found")
        void archive_WhenSubjectNotFound_ThrowsException() {
            // Given
            Long subjectId = 999L;
            when(subjectRepositoryPort.findById(subjectId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> subjectService.archive(subjectId))
                    .isInstanceOf(SubjectNotFoundException.class);

            verify(subjectRepositoryPort).findById(subjectId);
            verify(subjectRepositoryPort, never()).save(any());
        }
    }
}
