package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException;
import com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.shared.factory.EnrollmentFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WaitingListService with Mockito.
 * Tests waiting list FIFO management logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WaitingListService Tests")
class WaitingListServiceTest {

    @Mock
    private EnrollmentRepositoryPort enrollmentRepositoryPort;

    @InjectMocks
    private WaitingListService waitingListService;

    @Nested
    @DisplayName("Get Waiting List Tests")
    class GetWaitingListTests {

        @Test
        @DisplayName("Should return waiting list for group ordered by position")
        void getWaitingListByGroupId_ReturnsOrderedList() {
            // Given
            Enrollment first = EnrollmentFactory.waitingListEnrollment(100L, 1L, 1);
            Enrollment second = EnrollmentFactory.waitingListEnrollment(101L, 1L, 2);
            Enrollment third = EnrollmentFactory.waitingListEnrollment(102L, 1L, 3);

            when(enrollmentRepositoryPort.findWaitingListByGroupId(1L))
                    .thenReturn(List.of(first, second, third));

            // When
            List<Enrollment> result = waitingListService.getWaitingListByGroupId(1L);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getWaitingListPosition()).isEqualTo(1);
            assertThat(result.get(1).getWaitingListPosition()).isEqualTo(2);
            assertThat(result.get(2).getWaitingListPosition()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return empty list when no students waiting")
        void getWaitingListByGroupId_WhenEmpty_ReturnsEmptyList() {
            // Given
            when(enrollmentRepositoryPort.findWaitingListByGroupId(1L))
                    .thenReturn(Collections.emptyList());

            // When
            List<Enrollment> result = waitingListService.getWaitingListByGroupId(1L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return all waiting positions for student")
        void getWaitingListByStudentId_ReturnsAllQueues() {
            // Given
            Enrollment queue1 = EnrollmentFactory.waitingListEnrollment(100L, 1L, 2);
            Enrollment queue2 = EnrollmentFactory.waitingListEnrollment(100L, 2L, 5);

            when(enrollmentRepositoryPort.findByStudentIdAndStatus(100L, EnrollmentStatus.WAITING_LIST))
                    .thenReturn(List.of(queue1, queue2));

            // When
            List<Enrollment> result = waitingListService.getWaitingListByStudentId(100L);

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Leave Waiting List Tests")
    class LeaveWaitingListTests {

        @Test
        @DisplayName("Should leave waiting list and adjust positions")
        void leaveWaitingList_WithdrawsAndAdjustsPositions() {
            // Given
            Enrollment waitingEnrollment = EnrollmentFactory.waitingListEnrollment(100L, 1L, 2);
            waitingEnrollment = waitingEnrollment.toBuilder().id(1L).build();

            when(enrollmentRepositoryPort.findById(1L)).thenReturn(Optional.of(waitingEnrollment));
            when(enrollmentRepositoryPort.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Enrollment result = waitingListService.leaveWaitingList(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.WITHDRAWN);
            assertThat(result.getWaitingListPosition()).isNull();
            assertThat(result.getWithdrawnAt()).isNotNull();
            verify(enrollmentRepositoryPort).decrementWaitingListPositionsAfter(1L, 2);
        }

        @Test
        @DisplayName("Should throw exception when enrollment not found")
        void leaveWaitingList_WhenNotFound_ThrowsException() {
            // Given
            when(enrollmentRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> waitingListService.leaveWaitingList(999L))
                    .isInstanceOf(EnrollmentNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when not on waiting list")
        void leaveWaitingList_WhenNotOnWaitingList_ThrowsException() {
            // Given
            Enrollment activeEnrollment = EnrollmentFactory.activeEnrollment(100L, 1L);
            activeEnrollment = activeEnrollment.toBuilder().id(1L).build();

            when(enrollmentRepositoryPort.findById(1L)).thenReturn(Optional.of(activeEnrollment));

            // When & Then
            assertThatThrownBy(() -> waitingListService.leaveWaitingList(1L))
                    .isInstanceOf(InvalidEnrollmentStateException.class)
                    .hasMessageContaining("not on waiting list");
        }
    }

    @Nested
    @DisplayName("Promote From Waiting List Tests")
    class PromoteFromWaitingListTests {

        @Test
        @DisplayName("Should promote first student in queue to ACTIVE")
        void promoteNextFromWaitingList_PromotesFirstInQueue() {
            // Given
            Enrollment first = EnrollmentFactory.waitingListEnrollment(100L, 1L, 1);
            first = first.toBuilder().id(1L).build();

            when(enrollmentRepositoryPort.findWaitingListByGroupId(1L))
                    .thenReturn(List.of(first));
            when(enrollmentRepositoryPort.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Enrollment result = waitingListService.promoteNextFromWaitingList(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            assertThat(result.getWaitingListPosition()).isNull();
            assertThat(result.getPromotedAt()).isNotNull();
            verify(enrollmentRepositoryPort).decrementWaitingListPositionsAfter(1L, 1);
        }

        @Test
        @DisplayName("Should return null when waiting list is empty")
        void promoteNextFromWaitingList_WhenEmpty_ReturnsNull() {
            // Given
            when(enrollmentRepositoryPort.findWaitingListByGroupId(1L))
                    .thenReturn(Collections.emptyList());

            // When
            Enrollment result = waitingListService.promoteNextFromWaitingList(1L);

            // Then
            assertThat(result).isNull();
            verify(enrollmentRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should adjust positions after promotion")
        void promoteNextFromWaitingList_AdjustsRemainingPositions() {
            // Given
            Enrollment first = EnrollmentFactory.waitingListEnrollment(100L, 1L, 1);
            first = first.toBuilder().id(1L).build();
            Enrollment second = EnrollmentFactory.waitingListEnrollment(101L, 1L, 2);
            second = second.toBuilder().id(2L).build();

            when(enrollmentRepositoryPort.findWaitingListByGroupId(1L))
                    .thenReturn(List.of(first, second));
            when(enrollmentRepositoryPort.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            waitingListService.promoteNextFromWaitingList(1L);

            // Then
            verify(enrollmentRepositoryPort).decrementWaitingListPositionsAfter(1L, 1);
        }
    }
}
