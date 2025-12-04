package com.acainfo.subject.domain.model;

import com.acainfo.shared.factory.SubjectFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Subject domain entity.
 * Tests query methods and business logic (no Spring context).
 */
@DisplayName("Subject Domain Tests")
class SubjectTest {

    @Nested
    @DisplayName("Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should identify active subject correctly")
        void isActive_WhenStatusIsActive_ReturnsTrue() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .status(SubjectStatus.ACTIVE)
                    .buildDomain();

            // When
            boolean result = subject.isActive();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify non-active subject as active")
        void isActive_WhenStatusIsNotActive_ReturnsFalse() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .status(SubjectStatus.INACTIVE)
                    .buildDomain();

            // When
            boolean result = subject.isActive();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should identify archived subject correctly")
        void isArchived_WhenStatusIsArchived_ReturnsTrue() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .status(SubjectStatus.ARCHIVED)
                    .buildDomain();

            // When
            boolean result = subject.isArchived();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify non-archived subject as archived")
        void isArchived_WhenStatusIsNotArchived_ReturnsFalse() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .status(SubjectStatus.ACTIVE)
                    .buildDomain();

            // When
            boolean result = subject.isArchived();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Group Management Tests")
    class GroupManagementTests {

        @Test
        @DisplayName("Should allow creating group when count is less than maximum")
        void canCreateGroup_WhenCountBelowMax_ReturnsTrue() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .currentGroupCount(2)
                    .buildDomain();

            // When
            boolean result = subject.canCreateGroup();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not allow creating group when count reaches maximum")
        void canCreateGroup_WhenCountAtMax_ReturnsFalse() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .currentGroupCount(3) // MAX_GROUPS_PER_SUBJECT
                    .buildDomain();

            // When
            boolean result = subject.canCreateGroup();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should allow creating group when count is zero")
        void canCreateGroup_WhenCountIsZero_ReturnsTrue() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .currentGroupCount(0)
                    .buildDomain();

            // When
            boolean result = subject.canCreateGroup();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should calculate remaining group slots correctly")
        void getRemainingGroupSlots_ReturnsCorrectValue() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .currentGroupCount(1)
                    .buildDomain();

            // When
            int result = subject.getRemainingGroupSlots();

            // Then
            assertThat(result).isEqualTo(2); // 3 (MAX) - 1 (current) = 2
        }

        @Test
        @DisplayName("Should return zero remaining slots when at maximum")
        void getRemainingGroupSlots_WhenAtMax_ReturnsZero() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .currentGroupCount(3)
                    .buildDomain();

            // When
            int result = subject.getRemainingGroupSlots();

            // Then
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return all slots when no groups exist")
        void getRemainingGroupSlots_WhenNoGroups_ReturnsMax() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .currentGroupCount(0)
                    .buildDomain();

            // When
            int result = subject.getRemainingGroupSlots();

            // Then
            assertThat(result).isEqualTo(3); // MAX_GROUPS_PER_SUBJECT
        }
    }

    @Nested
    @DisplayName("Display Name Tests")
    class DisplayNameTests {

        @Test
        @DisplayName("Should return display name in correct format")
        void getDisplayName_ReturnsFormattedString() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .code("ING101")
                    .name("Programación I")
                    .buildDomain();

            // When
            String result = subject.getDisplayName();

            // Then
            assertThat(result).isEqualTo("ING101 - Programación I");
        }

        @Test
        @DisplayName("Should handle different code and name combinations")
        void getDisplayName_WithDifferentValues_FormatsCorrectly() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .code("MAT201")
                    .name("Matemáticas Avanzadas")
                    .buildDomain();

            // When
            String result = subject.getDisplayName();

            // Then
            assertThat(result).isEqualTo("MAT201 - Matemáticas Avanzadas");
        }
    }

    @Nested
    @DisplayName("Equality and HashCode Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should consider subjects equal when code is the same")
        void equals_WhenSameCode_ReturnsTrue() {
            // Given
            Subject subject1 = SubjectFactory.builder()
                    .id(1L)
                    .code("ING101")
                    .name("Programación I")
                    .buildDomain();

            Subject subject2 = SubjectFactory.builder()
                    .id(2L) // Different ID
                    .code("ING101") // Same code
                    .name("Programming I") // Different name
                    .buildDomain();

            // When & Then
            assertThat(subject1).isEqualTo(subject2);
            assertThat(subject1.hashCode()).isEqualTo(subject2.hashCode());
        }

        @Test
        @DisplayName("Should consider subjects different when code differs")
        void equals_WhenDifferentCode_ReturnsFalse() {
            // Given
            Subject subject1 = SubjectFactory.builder()
                    .code("ING101")
                    .buildDomain();

            Subject subject2 = SubjectFactory.builder()
                    .code("ING102")
                    .buildDomain();

            // When & Then
            assertThat(subject1).isNotEqualTo(subject2);
        }

        @Test
        @DisplayName("Should handle null comparison")
        void equals_WhenComparedToNull_ReturnsFalse() {
            // Given
            Subject subject = SubjectFactory.defaultSubject();

            // When & Then
            assertThat(subject).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should handle comparison to different class")
        void equals_WhenComparedToDifferentClass_ReturnsFalse() {
            // Given
            Subject subject = SubjectFactory.defaultSubject();
            String notASubject = "Not a subject";

            // When & Then
            assertThat(subject).isNotEqualTo(notASubject);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build subject with all properties")
        void builder_WithAllProperties_CreatesSubjectCorrectly() {
            // When
            Subject subject = Subject.builder()
                    .id(1L)
                    .code("ING101")
                    .name("Programación I")
                    .degree(Degree.INGENIERIA_INFORMATICA)
                    .status(SubjectStatus.ACTIVE)
                    .currentGroupCount(2)
                    .build();

            // Then
            assertThat(subject.getId()).isEqualTo(1L);
            assertThat(subject.getCode()).isEqualTo("ING101");
            assertThat(subject.getName()).isEqualTo("Programación I");
            assertThat(subject.getDegree()).isEqualTo(Degree.INGENIERIA_INFORMATICA);
            assertThat(subject.getStatus()).isEqualTo(SubjectStatus.ACTIVE);
            assertThat(subject.getCurrentGroupCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should initialize currentGroupCount as zero by default")
        void builder_WithoutGroupCount_InitializesAsZero() {
            // Given & When
            Subject subject = Subject.builder()
                    .code("ING101")
                    .name("Test")
                    .build();

            // Then
            assertThat(subject.getCurrentGroupCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Factory Tests")
    class FactoryTests {

        @Test
        @DisplayName("Should create default subject with correct properties")
        void defaultSubject_HasCorrectDefaults() {
            // When
            Subject subject = SubjectFactory.defaultSubject();

            // Then
            assertThat(subject.getCode()).isEqualTo("ING101");
            assertThat(subject.getName()).isEqualTo("Programación I");
            assertThat(subject.getDegree()).isEqualTo(Degree.INGENIERIA_INFORMATICA);
            assertThat(subject.getStatus()).isEqualTo(SubjectStatus.ACTIVE);
            assertThat(subject.getCurrentGroupCount()).isEqualTo(0);
            assertThat(subject.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should create inactive subject correctly")
        void inactiveSubject_HasInactiveStatus() {
            // When
            Subject subject = SubjectFactory.inactiveSubject();

            // Then
            assertThat(subject.getStatus()).isEqualTo(SubjectStatus.INACTIVE);
            assertThat(subject.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should create archived subject correctly")
        void archivedSubject_HasArchivedStatus() {
            // When
            Subject subject = SubjectFactory.archivedSubject();

            // Then
            assertThat(subject.getStatus()).isEqualTo(SubjectStatus.ARCHIVED);
            assertThat(subject.isArchived()).isTrue();
            assertThat(subject.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should create subject with max groups correctly")
        void subjectWithMaxGroups_CannotCreateMoreGroups() {
            // When
            Subject subject = SubjectFactory.subjectWithMaxGroups();

            // Then
            assertThat(subject.getCurrentGroupCount()).isEqualTo(3);
            assertThat(subject.canCreateGroup()).isFalse();
            assertThat(subject.getRemainingGroupSlots()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create subject with no groups correctly")
        void subjectWithNoGroups_CanCreateGroups() {
            // When
            Subject subject = SubjectFactory.subjectWithNoGroups();

            // Then
            assertThat(subject.getCurrentGroupCount()).isEqualTo(0);
            assertThat(subject.canCreateGroup()).isTrue();
            assertThat(subject.getRemainingGroupSlots()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should create subject with custom code")
        void withCode_CreatesSubjectWithSpecifiedCode() {
            // Given
            String customCode = "MAT201";

            // When
            Subject subject = SubjectFactory.withCode(customCode);

            // Then
            assertThat(subject.getCode()).isEqualTo(customCode);
        }

        @Test
        @DisplayName("Should create subject for specific degree")
        void forDegree_CreatesSubjectWithSpecifiedDegree() {
            // When
            Subject subject = SubjectFactory.forDegree(Degree.INGENIERIA_SOFTWARE);

            // Then
            assertThat(subject.getDegree()).isEqualTo(Degree.INGENIERIA_SOFTWARE);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include code in toString")
        void toString_IncludesCode() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .code("ING101")
                    .buildDomain();

            // When
            String subjectString = subject.toString();

            // Then
            assertThat(subjectString).contains("ING101");
        }

        @Test
        @DisplayName("Should include name in toString")
        void toString_IncludesName() {
            // Given
            Subject subject = SubjectFactory.builder()
                    .name("Programación I")
                    .buildDomain();

            // When
            String subjectString = subject.toString();

            // Then
            assertThat(subjectString).contains("Programación I");
        }
    }
}
