package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.dto.ChangeGroupCommand;
import com.acainfo.enrollment.application.dto.EnrollStudentCommand;
import com.acainfo.enrollment.application.dto.EnrollmentFilters;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.exception.AlreadyEnrolledException;
import com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException;
import com.acainfo.enrollment.domain.exception.GroupFullException;
import com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.shared.factory.EnrollmentFactory;
import com.acainfo.shared.factory.GroupFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EnrollmentService with Mockito.
 * Tests enrollment business logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService Tests")
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private WaitingListService waitingListService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private SubjectGroup testGroup;
    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        testGroup = GroupFactory.defaultGroup();
        testEnrollment = EnrollmentFactory.defaultEnrollment();
    }

    @Nested
    @DisplayName("Enroll Student Tests")
    class EnrollStudentTests {

        @Test
        @DisplayName("Should enroll student as ACTIVE when group has available seats")
        void enroll_WhenSeatsAvailable_EnrollsAsActive() {
            // Given
            EnrollStudentCommand command = new EnrollStudentCommand(100L, 1L);

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(enrollmentRepositoryPort.existsActiveOrWaitingEnrollment(100L, 1L)).thenReturn(false);
            when(enrollmentRepositoryPort.countActiveByGroupId(1L)).thenReturn(10L); // < 24
            when(enrollmentRepositoryPort.save(any(Enrollment.class))).thenAnswer(invocation -> {
                Enrollment e = invocation.getArgument(0);
                return e.toBuilder().id(1L).build();
            });

            // When
            Enrollment result = enrollmentService.enroll(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            assertThat(result.getStudentId()).isEqualTo(100L);
            assertThat(result.getGroupId()).isEqualTo(1L);
            verify(enrollmentRepositoryPort).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should add to waiting list when group is full")
        void enroll_WhenGroupFull_AddsToWaitingList() {
            // Given
            EnrollStudentCommand command = new EnrollStudentCommand(100L, 1L);

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(enrollmentRepositoryPort.existsActiveOrWaitingEnrollment(100L, 1L)).thenReturn(false);
            when(enrollmentRepositoryPort.countActiveByGroupId(1L)).thenReturn(24L); // Full
            when(enrollmentRepositoryPort.getNextWaitingListPosition(1L)).thenReturn(1);
            when(enrollmentRepositoryPort.save(any(Enrollment.class))).thenAnswer(invocation -> {
                Enrollment e = invocation.getArgument(0);
                return e.toBuilder().id(1L).build();
            });

            // When
            Enrollment result = enrollmentService.enroll(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.WAITING_LIST);
            assertThat(result.getWaitingListPosition()).isEqualTo(1);
            verify(enrollmentRepositoryPort).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should throw exception when group not found")
        void enroll_WhenGroupNotFound_ThrowsException() {
            // Given
            EnrollStudentCommand command = new EnrollStudentCommand(100L, 999L);

            when(groupRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> enrollmentService.enroll(command))
                    .isInstanceOf(GroupNotFoundException.class);

            verify(enrollmentRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when student already enrolled")
        void enroll_WhenAlreadyEnrolled_ThrowsException() {
            // Given
            EnrollStudentCommand command = new EnrollStudentCommand(100L, 1L);

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(enrollmentRepositoryPort.existsActiveOrWaitingEnrollment(100L, 1L)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> enrollmentService.enroll(command))
                    .isInstanceOf(AlreadyEnrolledException.class);

            verify(enrollmentRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Withdraw Enrollment Tests")
    class WithdrawEnrollmentTests {

        @Test
        @DisplayName("Should withdraw active enrollment and promote next from waiting list")
        void withdraw_WhenActive_WithdrawsAndPromotes() {
            // Given
            Enrollment activeEnrollment = EnrollmentFactory.activeEnrollment(100L, 1L);
            activeEnrollment = activeEnrollment.toBuilder().id(1L).build();

            when(enrollmentRepositoryPort.findById(1L)).thenReturn(Optional.of(activeEnrollment));
            when(enrollmentRepositoryPort.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Enrollment result = enrollmentService.withdraw(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.WITHDRAWN);
            assertThat(result.getWithdrawnAt()).isNotNull();
            verify(waitingListService).promoteNextFromWaitingList(1L);
        }

        @Test
        @DisplayName("Should withdraw from waiting list and adjust positions")
        void withdraw_WhenOnWaitingList_WithdrawsAndAdjustsPositions() {
            // Given
            Enrollment waitingEnrollment = EnrollmentFactory.waitingListEnrollment(100L, 1L, 3);
            waitingEnrollment = waitingEnrollment.toBuilder().id(1L).build();

            when(enrollmentRepositoryPort.findById(1L)).thenReturn(Optional.of(waitingEnrollment));
            when(enrollmentRepositoryPort.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Enrollment result = enrollmentService.withdraw(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.WITHDRAWN);
            assertThat(result.getWaitingListPosition()).isNull();
            verify(enrollmentRepositoryPort).decrementWaitingListPositionsAfter(1L, 3);
            verify(waitingListService, never()).promoteNextFromWaitingList(anyLong());
        }

        @Test
        @DisplayName("Should throw exception when enrollment not found")
        void withdraw_WhenNotFound_ThrowsException() {
            // Given
            when(enrollmentRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> enrollmentService.withdraw(999L))
                    .isInstanceOf(EnrollmentNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when already withdrawn")
        void withdraw_WhenAlreadyWithdrawn_ThrowsException() {
            // Given
            Enrollment withdrawnEnrollment = EnrollmentFactory.withdrawnEnrollment(100L, 1L);
            withdrawnEnrollment = withdrawnEnrollment.toBuilder().id(1L).build();

            when(enrollmentRepositoryPort.findById(1L)).thenReturn(Optional.of(withdrawnEnrollment));

            // When & Then
            assertThatThrownBy(() -> enrollmentService.withdraw(1L))
                    .isInstanceOf(InvalidEnrollmentStateException.class)
                    .hasMessageContaining("Cannot withdraw");
        }
    }

    @Nested
    @DisplayName("Change Group Tests")
    class ChangeGroupTests {

        @Test
        @DisplayName("Should change group when new group has seats")
        void changeGroup_WhenSeatsAvailable_ChangesGroup() {
            // Given
            Enrollment activeEnrollment = EnrollmentFactory.activeEnrollment(100L, 1L);
            activeEnrollment = activeEnrollment.toBuilder().id(1L).build();
            SubjectGroup newGroup = GroupFactory.builder().id(2L).buildDomain();

            ChangeGroupCommand command = new ChangeGroupCommand(1L, 2L);

            when(enrollmentRepositoryPort.findById(1L)).thenReturn(Optional.of(activeEnrollment));
            when(groupRepositoryPort.findById(2L)).thenReturn(Optional.of(newGroup));
            when(enrollmentRepositoryPort.countActiveByGroupId(2L)).thenReturn(10L);
            when(enrollmentRepositoryPort.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Enrollment result = enrollmentService.changeGroup(command);

            // Then
            assertThat(result.getGroupId()).isEqualTo(2L);
            verify(waitingListService).promoteNextFromWaitingList(1L);
        }

        @Test
        @DisplayName("Should throw exception when new group is full")
        void changeGroup_WhenNewGroupFull_ThrowsException() {
            // Given
            Enrollment activeEnrollment = EnrollmentFactory.activeEnrollment(100L, 1L);
            activeEnrollment = activeEnrollment.toBuilder().id(1L).build();
            SubjectGroup newGroup = GroupFactory.builder().id(2L).buildDomain();

            ChangeGroupCommand command = new ChangeGroupCommand(1L, 2L);

            when(enrollmentRepositoryPort.findById(1L)).thenReturn(Optional.of(activeEnrollment));
            when(groupRepositoryPort.findById(2L)).thenReturn(Optional.of(newGroup));
            when(enrollmentRepositoryPort.countActiveByGroupId(2L)).thenReturn(24L); // Full

            // When & Then
            assertThatThrownBy(() -> enrollmentService.changeGroup(command))
                    .isInstanceOf(GroupFullException.class);

            verify(enrollmentRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when enrollment not active")
        void changeGroup_WhenNotActive_ThrowsException() {
            // Given
            Enrollment waitingEnrollment = EnrollmentFactory.waitingListEnrollment(100L, 1L, 1);
            waitingEnrollment = waitingEnrollment.toBuilder().id(1L).build();

            ChangeGroupCommand command = new ChangeGroupCommand(1L, 2L);

            when(enrollmentRepositoryPort.findById(1L)).thenReturn(Optional.of(waitingEnrollment));

            // When & Then
            assertThatThrownBy(() -> enrollmentService.changeGroup(command))
                    .isInstanceOf(InvalidEnrollmentStateException.class)
                    .hasMessageContaining("Only ACTIVE");
        }

        @Test
        @DisplayName("Should throw exception when new group not found")
        void changeGroup_WhenNewGroupNotFound_ThrowsException() {
            // Given
            Enrollment activeEnrollment = EnrollmentFactory.activeEnrollment(100L, 1L);
            activeEnrollment = activeEnrollment.toBuilder().id(1L).build();

            ChangeGroupCommand command = new ChangeGroupCommand(1L, 999L);

            when(enrollmentRepositoryPort.findById(1L)).thenReturn(Optional.of(activeEnrollment));
            when(groupRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> enrollmentService.changeGroup(command))
                    .isInstanceOf(GroupNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get Enrollment Tests")
    class GetEnrollmentTests {

        @Test
        @DisplayName("Should return enrollment by ID")
        void getById_WhenExists_ReturnsEnrollment() {
            // Given
            when(enrollmentRepositoryPort.findById(1L)).thenReturn(Optional.of(testEnrollment));

            // When
            Enrollment result = enrollmentService.getById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testEnrollment.getId());
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void getById_WhenNotFound_ThrowsException() {
            // Given
            when(enrollmentRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> enrollmentService.getById(999L))
                    .isInstanceOf(EnrollmentNotFoundException.class);
        }

        @Test
        @DisplayName("Should return enrollments with filters")
        void findWithFilters_ReturnsFilteredResults() {
            // Given
            EnrollmentFilters filters = new EnrollmentFilters(100L, null, EnrollmentStatus.ACTIVE, 0, 20, "enrolledAt", "DESC");
            Page<Enrollment> page = new PageImpl<>(List.of(testEnrollment));

            when(enrollmentRepositoryPort.findWithFilters(filters)).thenReturn(page);

            // When
            Page<Enrollment> result = enrollmentService.findWithFilters(filters);

            // Then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return active enrollments by student")
        void findActiveByStudentId_ReturnsActiveEnrollments() {
            // Given
            when(enrollmentRepositoryPort.findByStudentIdAndStatus(100L, EnrollmentStatus.ACTIVE))
                    .thenReturn(List.of(testEnrollment));

            // When
            List<Enrollment> result = enrollmentService.findActiveByStudentId(100L);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return active enrollments by group")
        void findActiveByGroupId_ReturnsActiveEnrollments() {
            // Given
            when(enrollmentRepositoryPort.findByGroupIdAndStatus(1L, EnrollmentStatus.ACTIVE))
                    .thenReturn(List.of(testEnrollment));

            // When
            List<Enrollment> result = enrollmentService.findActiveByGroupId(1L);

            // Then
            assertThat(result).hasSize(1);
        }
    }
}
