package com.acainfo.group.application.service;

import com.acainfo.group.application.dto.CreateGroupCommand;
import com.acainfo.group.application.dto.UpdateGroupCommand;
import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.exception.InvalidGroupDataException;
import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.domain.model.GroupType;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.shared.factory.GroupFactory;
import com.acainfo.shared.factory.SubjectFactory;
import com.acainfo.shared.factory.UserFactory;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.exception.SubjectNotFoundException;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.exception.UserNotFoundException;
import com.acainfo.user.domain.model.User;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GroupService with Mockito.
 * Tests business logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GroupService Tests")
class GroupServiceTest {

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private SubjectRepositoryPort subjectRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private GroupService groupService;

    private SubjectGroup testGroup;
    private Subject testSubject;
    private User testTeacher;

    @BeforeEach
    void setUp() {
        testGroup = GroupFactory.defaultGroup();
        testSubject = SubjectFactory.defaultSubject();
        testTeacher = UserFactory.defaultTeacher();
    }

    @Nested
    @DisplayName("Create Group Tests")
    class CreateGroupTests {

        @Test
        @DisplayName("Should create group successfully with valid data")
        void create_WithValidData_CreatesGroup() {
            // Given
            CreateGroupCommand command = new CreateGroupCommand(
                    1L, // subjectId
                    2L, // teacherId
                    GroupType.REGULAR_Q1,
                    null // default capacity
            );

            when(subjectRepositoryPort.findById(1L)).thenReturn(Optional.of(testSubject));
            when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(testTeacher));
            when(groupRepositoryPort.save(any(SubjectGroup.class))).thenReturn(testGroup);
            when(subjectRepositoryPort.save(any(Subject.class))).thenReturn(testSubject);

            // When
            SubjectGroup result = groupService.create(command);

            // Then
            assertThat(result).isNotNull();
            verify(subjectRepositoryPort).findById(1L);
            verify(userRepositoryPort).findById(2L);
            verify(groupRepositoryPort).save(any(SubjectGroup.class));
            verify(subjectRepositoryPort).save(any(Subject.class));
        }

        @Test
        @DisplayName("Should create group with custom capacity")
        void create_WithCustomCapacity_CreatesGroupWithCapacity() {
            // Given
            CreateGroupCommand command = new CreateGroupCommand(
                    1L,
                    2L,
                    GroupType.REGULAR_Q1,
                    20 // Custom capacity
            );

            when(subjectRepositoryPort.findById(1L)).thenReturn(Optional.of(testSubject));
            when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(testTeacher));
            when(groupRepositoryPort.save(any(SubjectGroup.class))).thenAnswer(invocation -> {
                SubjectGroup group = invocation.getArgument(0);
                return group.toBuilder().id(1L).build();
            });
            when(subjectRepositoryPort.save(any(Subject.class))).thenReturn(testSubject);

            // When
            SubjectGroup result = groupService.create(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCapacity()).isEqualTo(20);
        }

        @Test
        @DisplayName("Should throw exception when subject not found")
        void create_WhenSubjectNotFound_ThrowsException() {
            // Given
            CreateGroupCommand command = new CreateGroupCommand(
                    999L,
                    2L,
                    GroupType.REGULAR_Q1,
                    null
            );

            when(subjectRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> groupService.create(command))
                    .isInstanceOf(SubjectNotFoundException.class);

            verify(subjectRepositoryPort).findById(999L);
            verify(groupRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when teacher not found")
        void create_WhenTeacherNotFound_ThrowsException() {
            // Given
            CreateGroupCommand command = new CreateGroupCommand(
                    1L,
                    999L,
                    GroupType.REGULAR_Q1,
                    null
            );

            when(subjectRepositoryPort.findById(1L)).thenReturn(Optional.of(testSubject));
            when(userRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> groupService.create(command))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepositoryPort).findById(999L);
            verify(groupRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user is not a teacher")
        void create_WhenUserNotTeacher_ThrowsException() {
            // Given
            CreateGroupCommand command = new CreateGroupCommand(
                    1L,
                    1L, // Student ID
                    GroupType.REGULAR_Q1,
                    null
            );

            User studentUser = UserFactory.defaultStudent();

            when(subjectRepositoryPort.findById(1L)).thenReturn(Optional.of(testSubject));
            when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(studentUser));

            // When & Then
            assertThatThrownBy(() -> groupService.create(command))
                    .isInstanceOf(InvalidGroupDataException.class)
                    .hasMessageContaining("is not a teacher or admin");

            verify(groupRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should allow admin to be assigned as teacher")
        void create_WhenUserIsAdmin_CreatesGroup() {
            // Given
            CreateGroupCommand command = new CreateGroupCommand(
                    1L,
                    3L, // Admin ID
                    GroupType.REGULAR_Q1,
                    null
            );

            User adminUser = UserFactory.defaultAdmin();

            when(subjectRepositoryPort.findById(1L)).thenReturn(Optional.of(testSubject));
            when(userRepositoryPort.findById(3L)).thenReturn(Optional.of(adminUser));
            when(groupRepositoryPort.save(any(SubjectGroup.class))).thenReturn(testGroup);
            when(subjectRepositoryPort.save(any(Subject.class))).thenReturn(testSubject);

            // When
            SubjectGroup result = groupService.create(command);

            // Then
            assertThat(result).isNotNull();
            verify(groupRepositoryPort).save(any(SubjectGroup.class));
        }

        @Test
        @DisplayName("Should throw exception when regular capacity is too high")
        void create_WhenRegularCapacityTooHigh_ThrowsException() {
            // Given
            CreateGroupCommand command = new CreateGroupCommand(
                    1L,
                    2L,
                    GroupType.REGULAR_Q1,
                    30 // Exceeds max 24 for regular
            );

            when(subjectRepositoryPort.findById(1L)).thenReturn(Optional.of(testSubject));
            when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(testTeacher));

            // When & Then
            assertThatThrownBy(() -> groupService.create(command))
                    .isInstanceOf(InvalidGroupDataException.class)
                    .hasMessageContaining("Capacity must be between 1 and 24");

            verify(groupRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when intensive capacity is too high")
        void create_WhenIntensiveCapacityTooHigh_ThrowsException() {
            // Given
            CreateGroupCommand command = new CreateGroupCommand(
                    1L,
                    2L,
                    GroupType.INTENSIVE_Q1,
                    60 // Exceeds max 50 for intensive
            );

            when(subjectRepositoryPort.findById(1L)).thenReturn(Optional.of(testSubject));
            when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(testTeacher));

            // When & Then
            assertThatThrownBy(() -> groupService.create(command))
                    .isInstanceOf(InvalidGroupDataException.class)
                    .hasMessageContaining("Capacity must be between 1 and 50");

            verify(groupRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when capacity is less than 1")
        void create_WhenCapacityLessThanOne_ThrowsException() {
            // Given
            CreateGroupCommand command = new CreateGroupCommand(
                    1L,
                    2L,
                    GroupType.REGULAR_Q1,
                    0 // Invalid capacity
            );

            when(subjectRepositoryPort.findById(1L)).thenReturn(Optional.of(testSubject));
            when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(testTeacher));

            // When & Then
            assertThatThrownBy(() -> groupService.create(command))
                    .isInstanceOf(InvalidGroupDataException.class)
                    .hasMessageContaining("Capacity must be between 1 and");

            verify(groupRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should increment subject group count after creation")
        void create_ShouldIncrementSubjectGroupCount() {
            // Given
            CreateGroupCommand command = new CreateGroupCommand(
                    1L,
                    2L,
                    GroupType.REGULAR_Q1,
                    null
            );

            Subject subjectWithZeroGroups = SubjectFactory.builder()
                    .currentGroupCount(0)
                    .buildDomain();

            when(subjectRepositoryPort.findById(1L)).thenReturn(Optional.of(subjectWithZeroGroups));
            when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(testTeacher));
            when(groupRepositoryPort.save(any(SubjectGroup.class))).thenReturn(testGroup);
            when(subjectRepositoryPort.save(any(Subject.class))).thenAnswer(invocation -> {
                Subject saved = invocation.getArgument(0);
                assertThat(saved.getCurrentGroupCount()).isEqualTo(1);
                return saved;
            });

            // When
            groupService.create(command);

            // Then
            verify(subjectRepositoryPort).save(any(Subject.class));
        }
    }

    @Nested
    @DisplayName("Update Group Tests")
    class UpdateGroupTests {

        @Test
        @DisplayName("Should update group capacity successfully")
        void update_WithValidCapacity_UpdatesGroup() {
            // Given
            Long groupId = 1L;
            UpdateGroupCommand command = new UpdateGroupCommand(20, null);

            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(testGroup));
            when(groupRepositoryPort.save(any(SubjectGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            SubjectGroup result = groupService.update(groupId, command);

            // Then
            assertThat(result.getCapacity()).isEqualTo(20);
            verify(groupRepositoryPort).save(any(SubjectGroup.class));
        }

        @Test
        @DisplayName("Should update group status successfully")
        void update_WithNewStatus_UpdatesGroup() {
            // Given
            Long groupId = 1L;
            UpdateGroupCommand command = new UpdateGroupCommand(null, GroupStatus.CLOSED);

            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(testGroup));
            when(groupRepositoryPort.save(any(SubjectGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            SubjectGroup result = groupService.update(groupId, command);

            // Then
            assertThat(result.getStatus()).isEqualTo(GroupStatus.CLOSED);
        }

        @Test
        @DisplayName("Should throw exception when capacity is less than current enrollments")
        void update_WhenCapacityLessThanEnrollments_ThrowsException() {
            // Given
            Long groupId = 1L;
            SubjectGroup groupWithEnrollments = GroupFactory.builder()
                    .currentEnrollmentCount(15)
                    .buildDomain();
            UpdateGroupCommand command = new UpdateGroupCommand(10, null); // Less than 15

            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(groupWithEnrollments));

            // When & Then
            assertThatThrownBy(() -> groupService.update(groupId, command))
                    .isInstanceOf(InvalidGroupDataException.class)
                    .hasMessageContaining("cannot be less than current enrollments");

            verify(groupRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when capacity exceeds max for type")
        void update_WhenCapacityExceedsMax_ThrowsException() {
            // Given
            Long groupId = 1L;
            SubjectGroup regularGroup = GroupFactory.builder()
                    .asRegularQ1()
                    .currentEnrollmentCount(5)
                    .buildDomain();
            UpdateGroupCommand command = new UpdateGroupCommand(30, null); // Exceeds 24

            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(regularGroup));

            // When & Then
            assertThatThrownBy(() -> groupService.update(groupId, command))
                    .isInstanceOf(InvalidGroupDataException.class)
                    .hasMessageContaining("cannot exceed 24");

            verify(groupRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should skip null fields during update")
        void update_WithNullFields_SkipsNullFields() {
            // Given
            Long groupId = 1L;
            UpdateGroupCommand command = new UpdateGroupCommand(null, null);

            SubjectGroup existingGroup = GroupFactory.builder()
                    .capacity(20)
                    .status(GroupStatus.OPEN)
                    .buildDomain();

            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(existingGroup));
            when(groupRepositoryPort.save(any(SubjectGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            SubjectGroup result = groupService.update(groupId, command);

            // Then
            assertThat(result.getCapacity()).isEqualTo(20);
            assertThat(result.getStatus()).isEqualTo(GroupStatus.OPEN);
        }

        @Test
        @DisplayName("Should throw GroupNotFoundException when group not found")
        void update_WhenGroupNotFound_ThrowsException() {
            // Given
            Long groupId = 999L;
            UpdateGroupCommand command = new UpdateGroupCommand(20, null);

            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> groupService.update(groupId, command))
                    .isInstanceOf(GroupNotFoundException.class);

            verify(groupRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Get Group Tests")
    class GetGroupTests {

        @Test
        @DisplayName("Should return group when found by ID")
        void getById_WhenGroupExists_ReturnsGroup() {
            // Given
            Long groupId = 1L;
            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(testGroup));

            // When
            SubjectGroup result = groupService.getById(groupId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testGroup.getId());
            verify(groupRepositoryPort).findById(groupId);
        }

        @Test
        @DisplayName("Should throw GroupNotFoundException when group not found")
        void getById_WhenGroupNotFound_ThrowsException() {
            // Given
            Long groupId = 999L;
            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> groupService.getById(groupId))
                    .isInstanceOf(GroupNotFoundException.class)
                    .hasMessageContaining("999");

            verify(groupRepositoryPort).findById(groupId);
        }
    }

    @Nested
    @DisplayName("Delete Group Tests")
    class DeleteGroupTests {

        @Test
        @DisplayName("Should delete group successfully when no enrollments")
        void delete_WhenNoEnrollments_DeletesGroup() {
            // Given
            Long groupId = 1L;
            SubjectGroup groupWithNoEnrollments = GroupFactory.builder()
                    .id(groupId)
                    .subjectId(1L)
                    .currentEnrollmentCount(0)
                    .buildDomain();

            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(groupWithNoEnrollments));
            when(subjectRepositoryPort.findById(1L)).thenReturn(Optional.of(testSubject));
            when(subjectRepositoryPort.save(any(Subject.class))).thenReturn(testSubject);

            // When
            groupService.delete(groupId);

            // Then
            verify(groupRepositoryPort).delete(groupId);
            verify(subjectRepositoryPort).save(any(Subject.class));
        }

        @Test
        @DisplayName("Should throw exception when group has enrollments")
        void delete_WhenHasEnrollments_ThrowsException() {
            // Given
            Long groupId = 1L;
            SubjectGroup groupWithEnrollments = GroupFactory.builder()
                    .currentEnrollmentCount(10)
                    .buildDomain();

            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(groupWithEnrollments));

            // When & Then
            assertThatThrownBy(() -> groupService.delete(groupId))
                    .isInstanceOf(InvalidGroupDataException.class)
                    .hasMessageContaining("Cannot delete group with existing enrollments");

            verify(groupRepositoryPort, never()).delete(anyLong());
        }

        @Test
        @DisplayName("Should throw GroupNotFoundException when group not found")
        void delete_WhenGroupNotFound_ThrowsException() {
            // Given
            Long groupId = 999L;
            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> groupService.delete(groupId))
                    .isInstanceOf(GroupNotFoundException.class);

            verify(groupRepositoryPort, never()).delete(anyLong());
        }

        @Test
        @DisplayName("Should decrement subject group count after deletion")
        void delete_ShouldDecrementSubjectGroupCount() {
            // Given
            Long groupId = 1L;
            SubjectGroup groupToDelete = GroupFactory.builder()
                    .id(groupId)
                    .subjectId(1L)
                    .currentEnrollmentCount(0)
                    .buildDomain();

            Subject subjectWithGroups = SubjectFactory.builder()
                    .currentGroupCount(2)
                    .buildDomain();

            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(groupToDelete));
            when(subjectRepositoryPort.findById(1L)).thenReturn(Optional.of(subjectWithGroups));
            when(subjectRepositoryPort.save(any(Subject.class))).thenAnswer(invocation -> {
                Subject saved = invocation.getArgument(0);
                assertThat(saved.getCurrentGroupCount()).isEqualTo(1);
                return saved;
            });

            // When
            groupService.delete(groupId);

            // Then
            verify(subjectRepositoryPort).save(any(Subject.class));
        }

        @Test
        @DisplayName("Should not decrement below zero")
        void delete_ShouldNotDecrementBelowZero() {
            // Given
            Long groupId = 1L;
            SubjectGroup groupToDelete = GroupFactory.builder()
                    .id(groupId)
                    .subjectId(1L)
                    .currentEnrollmentCount(0)
                    .buildDomain();

            Subject subjectWithZeroGroups = SubjectFactory.builder()
                    .currentGroupCount(0)
                    .buildDomain();

            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(groupToDelete));
            when(subjectRepositoryPort.findById(1L)).thenReturn(Optional.of(subjectWithZeroGroups));
            when(subjectRepositoryPort.save(any(Subject.class))).thenAnswer(invocation -> {
                Subject saved = invocation.getArgument(0);
                assertThat(saved.getCurrentGroupCount()).isEqualTo(0); // Math.max(0, -1) = 0
                return saved;
            });

            // When
            groupService.delete(groupId);

            // Then
            verify(subjectRepositoryPort).save(any(Subject.class));
        }
    }

    @Nested
    @DisplayName("Cancel Group Tests")
    class CancelGroupTests {

        @Test
        @DisplayName("Should cancel group successfully")
        void cancel_WhenGroupExists_CancelsGroup() {
            // Given
            Long groupId = 1L;
            SubjectGroup cancelledGroup = testGroup.toBuilder()
                    .status(GroupStatus.CANCELLED)
                    .build();

            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.of(testGroup));
            when(groupRepositoryPort.save(any(SubjectGroup.class))).thenReturn(cancelledGroup);

            // When
            SubjectGroup result = groupService.cancel(groupId);

            // Then
            assertThat(result.getStatus()).isEqualTo(GroupStatus.CANCELLED);
            assertThat(result.isCancelled()).isTrue();
            verify(groupRepositoryPort).save(any(SubjectGroup.class));
        }

        @Test
        @DisplayName("Should throw GroupNotFoundException when group not found")
        void cancel_WhenGroupNotFound_ThrowsException() {
            // Given
            Long groupId = 999L;
            when(groupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> groupService.cancel(groupId))
                    .isInstanceOf(GroupNotFoundException.class);

            verify(groupRepositoryPort, never()).save(any());
        }
    }
}
