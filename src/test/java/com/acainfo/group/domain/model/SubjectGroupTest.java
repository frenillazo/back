package com.acainfo.group.domain.model;

import com.acainfo.shared.factory.GroupFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SubjectGroup domain entity.
 * Tests query methods and business logic (no Spring context).
 */
@DisplayName("SubjectGroup Domain Tests")
class SubjectGroupTest {

    @Nested
    @DisplayName("Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should identify open group correctly")
        void isOpen_WhenStatusIsOpen_ReturnsTrue() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .status(GroupStatus.OPEN)
                    .buildDomain();

            // When
            boolean result = group.isOpen();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify non-open group as open")
        void isOpen_WhenStatusIsNotOpen_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .status(GroupStatus.CLOSED)
                    .buildDomain();

            // When
            boolean result = group.isOpen();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should identify closed group correctly")
        void isClosed_WhenStatusIsClosed_ReturnsTrue() {
            // Given
            SubjectGroup group = GroupFactory.closedGroup();

            // When
            boolean result = group.isClosed();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify non-closed group as closed")
        void isClosed_WhenStatusIsNotClosed_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.defaultGroup();

            // When
            boolean result = group.isClosed();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should identify cancelled group correctly")
        void isCancelled_WhenStatusIsCancelled_ReturnsTrue() {
            // Given
            SubjectGroup group = GroupFactory.cancelledGroup();

            // When
            boolean result = group.isCancelled();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify non-cancelled group as cancelled")
        void isCancelled_WhenStatusIsNotCancelled_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.defaultGroup();

            // When
            boolean result = group.isCancelled();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Group Type Tests")
    class GroupTypeTests {

        @Test
        @DisplayName("Should identify regular Q1 group correctly")
        void isRegular_WhenTypeIsRegularQ1_ReturnsTrue() {
            // Given
            SubjectGroup group = GroupFactory.openRegularQ1();

            // When
            boolean result = group.isRegular();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should identify regular Q2 group correctly")
        void isRegular_WhenTypeIsRegularQ2_ReturnsTrue() {
            // Given
            SubjectGroup group = GroupFactory.openRegularQ2();

            // When
            boolean result = group.isRegular();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify intensive group as regular")
        void isRegular_WhenTypeIsIntensive_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.openIntensiveQ1();

            // When
            boolean result = group.isRegular();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should identify intensive Q1 group correctly")
        void isIntensive_WhenTypeIsIntensiveQ1_ReturnsTrue() {
            // Given
            SubjectGroup group = GroupFactory.openIntensiveQ1();

            // When
            boolean result = group.isIntensive();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should identify intensive Q2 group correctly")
        void isIntensive_WhenTypeIsIntensiveQ2_ReturnsTrue() {
            // Given
            SubjectGroup group = GroupFactory.openIntensiveQ2();

            // When
            boolean result = group.isIntensive();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify regular group as intensive")
        void isIntensive_WhenTypeIsRegular_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.openRegularQ1();

            // When
            boolean result = group.isIntensive();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle null type for isRegular")
        void isRegular_WhenTypeIsNull_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .type(null)
                    .buildDomain();

            // When
            boolean result = group.isRegular();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle null type for isIntensive")
        void isIntensive_WhenTypeIsNull_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .type(null)
                    .buildDomain();

            // When
            boolean result = group.isIntensive();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Capacity Tests")
    class CapacityTests {

        @Test
        @DisplayName("Should return default regular capacity when no custom capacity")
        void getMaxCapacity_WhenRegularWithNoCustomCapacity_ReturnsDefault() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .asRegularQ1()
                    .capacity(null)
                    .buildDomain();

            // When
            int result = group.getMaxCapacity();

            // Then
            assertThat(result).isEqualTo(SubjectGroup.REGULAR_MAX_CAPACITY);
            assertThat(result).isEqualTo(24);
        }

        @Test
        @DisplayName("Should return default intensive capacity when no custom capacity")
        void getMaxCapacity_WhenIntensiveWithNoCustomCapacity_ReturnsDefault() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .asIntensiveQ1()
                    .capacity(null)
                    .buildDomain();

            // When
            int result = group.getMaxCapacity();

            // Then
            assertThat(result).isEqualTo(SubjectGroup.INTENSIVE_MAX_CAPACITY);
            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("Should return custom capacity when set")
        void getMaxCapacity_WhenCustomCapacitySet_ReturnsCustom() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .asRegularQ1()
                    .capacity(20)
                    .buildDomain();

            // When
            int result = group.getMaxCapacity();

            // Then
            assertThat(result).isEqualTo(20);
        }

        @Test
        @DisplayName("Should calculate available seats correctly")
        void getAvailableSeats_ReturnsCorrectValue() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .asRegularQ1()
                    .currentEnrollmentCount(10)
                    .buildDomain();

            // When
            int result = group.getAvailableSeats();

            // Then
            assertThat(result).isEqualTo(14); // 24 - 10
        }

        @Test
        @DisplayName("Should return zero available seats when full")
        void getAvailableSeats_WhenFull_ReturnsZero() {
            // Given
            SubjectGroup group = GroupFactory.fullRegularGroup();

            // When
            int result = group.getAvailableSeats();

            // Then
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("Should never return negative available seats")
        void getAvailableSeats_WhenOverCapacity_ReturnsZero() {
            // Given - Simulating a corrupted state where enrollment exceeds capacity
            SubjectGroup group = GroupFactory.builder()
                    .asRegularQ1()
                    .currentEnrollmentCount(30) // More than 24
                    .buildDomain();

            // When
            int result = group.getAvailableSeats();

            // Then
            assertThat(result).isEqualTo(0); // Math.max(0, ...) ensures no negative
        }

        @Test
        @DisplayName("Should identify group with available seats")
        void hasAvailableSeats_WhenNotFull_ReturnsTrue() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .asRegularQ1()
                    .currentEnrollmentCount(10)
                    .buildDomain();

            // When
            boolean result = group.hasAvailableSeats();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should identify group without available seats")
        void hasAvailableSeats_WhenFull_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.fullRegularGroup();

            // When
            boolean result = group.hasAvailableSeats();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should identify full group correctly")
        void isFull_WhenAtCapacity_ReturnsTrue() {
            // Given
            SubjectGroup group = GroupFactory.fullRegularGroup();

            // When
            boolean result = group.isFull();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify non-full group as full")
        void isFull_WhenNotAtCapacity_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .asRegularQ1()
                    .currentEnrollmentCount(20)
                    .buildDomain();

            // When
            boolean result = group.isFull();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle full intensive group correctly")
        void isFull_WhenIntensiveAtCapacity_ReturnsTrue() {
            // Given
            SubjectGroup group = GroupFactory.fullIntensiveGroup();

            // When
            boolean result = group.isFull();

            // Then
            assertThat(result).isTrue();
            assertThat(group.getCurrentEnrollmentCount()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Enrollment Tests")
    class EnrollmentTests {

        @Test
        @DisplayName("Should allow enrollment when open and has seats")
        void canEnroll_WhenOpenAndHasSeats_ReturnsTrue() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .status(GroupStatus.OPEN)
                    .currentEnrollmentCount(10)
                    .buildDomain();

            // When
            boolean result = group.canEnroll();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not allow enrollment when closed")
        void canEnroll_WhenClosed_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .status(GroupStatus.CLOSED)
                    .currentEnrollmentCount(10)
                    .buildDomain();

            // When
            boolean result = group.canEnroll();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should not allow enrollment when cancelled")
        void canEnroll_WhenCancelled_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.cancelledGroup();

            // When
            boolean result = group.canEnroll();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should not allow enrollment when full")
        void canEnroll_WhenFull_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .status(GroupStatus.OPEN)
                    .asRegularQ1()
                    .full()
                    .buildDomain();

            // When
            boolean result = group.canEnroll();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should not allow enrollment when open but full")
        void canEnroll_WhenOpenButFull_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .status(GroupStatus.OPEN)
                    .asRegularQ1()
                    .currentEnrollmentCount(24)
                    .buildDomain();

            // When
            boolean result = group.canEnroll();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Display Name Tests")
    class DisplayNameTests {

        @Test
        @DisplayName("Should return display name in correct format")
        void getDisplayName_ReturnsFormattedString() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .subjectId(5L)
                    .type(GroupType.REGULAR_Q1)
                    .buildDomain();

            // When
            String result = group.getDisplayName();

            // Then
            assertThat(result).isEqualTo("Subject 5 - REGULAR_Q1");
        }

        @Test
        @DisplayName("Should handle different type combinations")
        void getDisplayName_WithDifferentTypes_FormatsCorrectly() {
            // Given
            SubjectGroup group = GroupFactory.builder()
                    .subjectId(10L)
                    .type(GroupType.INTENSIVE_Q2)
                    .buildDomain();

            // When
            String result = group.getDisplayName();

            // Then
            assertThat(result).isEqualTo("Subject 10 - INTENSIVE_Q2");
        }
    }

    @Nested
    @DisplayName("Equality and HashCode Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should consider groups equal when ID is the same")
        void equals_WhenSameId_ReturnsTrue() {
            // Given
            SubjectGroup group1 = GroupFactory.builder()
                    .id(1L)
                    .subjectId(1L)
                    .type(GroupType.REGULAR_Q1)
                    .buildDomain();

            SubjectGroup group2 = GroupFactory.builder()
                    .id(1L) // Same ID
                    .subjectId(2L) // Different subject
                    .type(GroupType.INTENSIVE_Q1) // Different type
                    .buildDomain();

            // When & Then
            assertThat(group1).isEqualTo(group2);
            assertThat(group1.hashCode()).isEqualTo(group2.hashCode());
        }

        @Test
        @DisplayName("Should consider groups different when ID differs")
        void equals_WhenDifferentId_ReturnsFalse() {
            // Given
            SubjectGroup group1 = GroupFactory.builder()
                    .id(1L)
                    .buildDomain();

            SubjectGroup group2 = GroupFactory.builder()
                    .id(2L)
                    .buildDomain();

            // When & Then
            assertThat(group1).isNotEqualTo(group2);
        }

        @Test
        @DisplayName("Should handle null comparison")
        void equals_WhenComparedToNull_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.defaultGroup();

            // When & Then
            assertThat(group).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should handle comparison to different class")
        void equals_WhenComparedToDifferentClass_ReturnsFalse() {
            // Given
            SubjectGroup group = GroupFactory.defaultGroup();
            String notAGroup = "Not a group";

            // When & Then
            assertThat(group).isNotEqualTo(notAGroup);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build group with all properties")
        void builder_WithAllProperties_CreatesGroupCorrectly() {
            // When
            SubjectGroup group = SubjectGroup.builder()
                    .id(1L)
                    .subjectId(5L)
                    .teacherId(10L)
                    .type(GroupType.INTENSIVE_Q1)
                    .status(GroupStatus.OPEN)
                    .currentEnrollmentCount(25)
                    .capacity(40)
                    .build();

            // Then
            assertThat(group.getId()).isEqualTo(1L);
            assertThat(group.getSubjectId()).isEqualTo(5L);
            assertThat(group.getTeacherId()).isEqualTo(10L);
            assertThat(group.getType()).isEqualTo(GroupType.INTENSIVE_Q1);
            assertThat(group.getStatus()).isEqualTo(GroupStatus.OPEN);
            assertThat(group.getCurrentEnrollmentCount()).isEqualTo(25);
            assertThat(group.getCapacity()).isEqualTo(40);
        }

        @Test
        @DisplayName("Should initialize currentEnrollmentCount as zero by default")
        void builder_WithoutEnrollmentCount_InitializesAsZero() {
            // Given & When
            SubjectGroup group = SubjectGroup.builder()
                    .subjectId(1L)
                    .teacherId(1L)
                    .type(GroupType.REGULAR_Q1)
                    .build();

            // Then
            assertThat(group.getCurrentEnrollmentCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Factory Tests")
    class FactoryTests {

        @Test
        @DisplayName("Should create default group with correct properties")
        void defaultGroup_HasCorrectDefaults() {
            // When
            SubjectGroup group = GroupFactory.defaultGroup();

            // Then
            assertThat(group.getType()).isEqualTo(GroupType.REGULAR_Q1);
            assertThat(group.getStatus()).isEqualTo(GroupStatus.OPEN);
            assertThat(group.getCurrentEnrollmentCount()).isEqualTo(0);
            assertThat(group.getCapacity()).isNull();
            assertThat(group.isOpen()).isTrue();
            assertThat(group.canEnroll()).isTrue();
        }

        @Test
        @DisplayName("Should create closed group correctly")
        void closedGroup_HasClosedStatus() {
            // When
            SubjectGroup group = GroupFactory.closedGroup();

            // Then
            assertThat(group.getStatus()).isEqualTo(GroupStatus.CLOSED);
            assertThat(group.isOpen()).isFalse();
            assertThat(group.isClosed()).isTrue();
        }

        @Test
        @DisplayName("Should create cancelled group correctly")
        void cancelledGroup_HasCancelledStatus() {
            // When
            SubjectGroup group = GroupFactory.cancelledGroup();

            // Then
            assertThat(group.getStatus()).isEqualTo(GroupStatus.CANCELLED);
            assertThat(group.isCancelled()).isTrue();
            assertThat(group.canEnroll()).isFalse();
        }

        @Test
        @DisplayName("Should create full regular group correctly")
        void fullRegularGroup_HasMaxEnrollments() {
            // When
            SubjectGroup group = GroupFactory.fullRegularGroup();

            // Then
            assertThat(group.getCurrentEnrollmentCount()).isEqualTo(24);
            assertThat(group.isFull()).isTrue();
            assertThat(group.hasAvailableSeats()).isFalse();
        }

        @Test
        @DisplayName("Should create full intensive group correctly")
        void fullIntensiveGroup_HasMaxEnrollments() {
            // When
            SubjectGroup group = GroupFactory.fullIntensiveGroup();

            // Then
            assertThat(group.getCurrentEnrollmentCount()).isEqualTo(50);
            assertThat(group.isFull()).isTrue();
            assertThat(group.hasAvailableSeats()).isFalse();
        }

        @Test
        @DisplayName("Should create group with specific enrollments")
        void withEnrollments_CreatesGroupWithSpecifiedCount() {
            // Given
            int enrollmentCount = 15;

            // When
            SubjectGroup group = GroupFactory.withEnrollments(enrollmentCount);

            // Then
            assertThat(group.getCurrentEnrollmentCount()).isEqualTo(enrollmentCount);
        }

        @Test
        @DisplayName("Should create group for specific subject")
        void forSubject_CreatesGroupWithSpecifiedSubject() {
            // Given
            Long subjectId = 42L;

            // When
            SubjectGroup group = GroupFactory.forSubject(subjectId);

            // Then
            assertThat(group.getSubjectId()).isEqualTo(subjectId);
        }

        @Test
        @DisplayName("Should create group with specific teacher")
        void withTeacher_CreatesGroupWithSpecifiedTeacher() {
            // Given
            Long teacherId = 99L;

            // When
            SubjectGroup group = GroupFactory.withTeacher(teacherId);

            // Then
            assertThat(group.getTeacherId()).isEqualTo(teacherId);
        }
    }

    @Nested
    @DisplayName("Constants Tests")
    class ConstantsTests {

        @Test
        @DisplayName("Should have correct regular max capacity constant")
        void regularMaxCapacity_ShouldBe24() {
            assertThat(SubjectGroup.REGULAR_MAX_CAPACITY).isEqualTo(24);
        }

        @Test
        @DisplayName("Should have correct intensive max capacity constant")
        void intensiveMaxCapacity_ShouldBe50() {
            assertThat(SubjectGroup.INTENSIVE_MAX_CAPACITY).isEqualTo(50);
        }
    }
}
