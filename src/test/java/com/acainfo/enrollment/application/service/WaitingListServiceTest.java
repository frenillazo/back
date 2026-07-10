package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.port.out.AutoReservationPort;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException;
import com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.model.SubjectGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link WaitingListService}.
 *
 * <p>Captures the CURRENT behavior of the FIFO waiting list logic before the
 * group -> course migration. No Spring context, no database.</p>
 */
@ExtendWith(MockitoExtension.class)
class WaitingListServiceTest {

    private static final Long GROUP_ID = 100L;
    private static final Long STUDENT_ID = 10L;
    private static final Long ENROLLMENT_ID = 1L;

    @Mock
    private EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Mock
    private AutoReservationPort autoReservationPort;

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @InjectMocks
    private WaitingListService waitingListService;

    private SubjectGroup group;

    @BeforeEach
    void setUp() {
        group = SubjectGroup.builder()
                .id(GROUP_ID)
                .name("Test group")
                .subjectId(1L)
                .teacherId(2L)
                .build();
    }

    private Enrollment waitingEnrollment(Long id, Long studentId, Integer position) {
        return Enrollment.builder()
                .id(id)
                .studentId(studentId)
                .groupId(GROUP_ID)
                .status(EnrollmentStatus.WAITING_LIST)
                .waitingListPosition(position)
                .enrolledAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    /** Makes save() return the (mutated) enrollment it receives, like the real adapter. */
    private void stubSaveEcho() {
        when(enrollmentRepositoryPort.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ==================== getWaitingListByGroupId ====================

    @Nested
    class GetWaitingListByGroupId {

        @Test
        void shouldReturnWaitingListOrderedByRepositoryWhenGroupHasQueue() {
            List<Enrollment> queue = List.of(
                    waitingEnrollment(1L, 10L, 1),
                    waitingEnrollment(2L, 11L, 2));
            when(enrollmentRepositoryPort.findWaitingListByGroupId(GROUP_ID)).thenReturn(queue);

            List<Enrollment> result = waitingListService.getWaitingListByGroupId(GROUP_ID);

            assertThat(result).isSameAs(queue);
            verify(enrollmentRepositoryPort).findWaitingListByGroupId(GROUP_ID);
        }

        @Test
        void shouldReturnEmptyListWhenGroupHasNoQueue() {
            when(enrollmentRepositoryPort.findWaitingListByGroupId(GROUP_ID)).thenReturn(List.of());

            List<Enrollment> result = waitingListService.getWaitingListByGroupId(GROUP_ID);

            assertThat(result).isEmpty();
        }
    }

    // ==================== getWaitingListByStudentId ====================

    @Nested
    class GetWaitingListByStudentId {

        @Test
        void shouldQueryOnlyWaitingListStatusWhenGettingStudentPositions() {
            List<Enrollment> queuePositions = List.of(waitingEnrollment(1L, STUDENT_ID, 3));
            when(enrollmentRepositoryPort.findByStudentIdAndStatus(STUDENT_ID, EnrollmentStatus.WAITING_LIST))
                    .thenReturn(queuePositions);

            List<Enrollment> result = waitingListService.getWaitingListByStudentId(STUDENT_ID);

            assertThat(result).isSameAs(queuePositions);
            verify(enrollmentRepositoryPort).findByStudentIdAndStatus(STUDENT_ID, EnrollmentStatus.WAITING_LIST);
        }
    }

    // ==================== leaveWaitingList ====================

    @Nested
    class LeaveWaitingList {

        @Test
        void shouldWithdrawAndDecrementLaterPositionsWhenStudentLeavesQueue() {
            Enrollment enrollment = waitingEnrollment(ENROLLMENT_ID, STUDENT_ID, 2);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            stubSaveEcho();

            LocalDateTime before = LocalDateTime.now();
            Enrollment result = waitingListService.leaveWaitingList(ENROLLMENT_ID);
            LocalDateTime after = LocalDateTime.now();

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.WITHDRAWN);
            assertThat(result.getWaitingListPosition()).isNull();
            assertThat(result.getWithdrawnAt()).isNotNull()
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);

            // Save happens BEFORE positions of the rest of the queue are decremented
            InOrder inOrder = inOrder(enrollmentRepositoryPort);
            inOrder.verify(enrollmentRepositoryPort).save(enrollment);
            inOrder.verify(enrollmentRepositoryPort).decrementWaitingListPositionsAfter(GROUP_ID, 2);

            // Leaving the queue does NOT trigger any reservation logic
            verifyNoInteractions(autoReservationPort);
        }

        @Test
        void shouldThrowEnrollmentNotFoundWhenEnrollmentDoesNotExist() {
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> waitingListService.leaveWaitingList(ENROLLMENT_ID))
                    .isInstanceOf(EnrollmentNotFoundException.class)
                    .hasMessageContaining(String.valueOf(ENROLLMENT_ID));

            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
            verify(enrollmentRepositoryPort, never()).decrementWaitingListPositionsAfter(anyLong(), anyInt());
        }

        @Test
        void shouldThrowInvalidStateWhenEnrollmentIsNotOnWaitingList() {
            Enrollment activeEnrollment = Enrollment.builder()
                    .id(ENROLLMENT_ID)
                    .studentId(STUDENT_ID)
                    .groupId(GROUP_ID)
                    .status(EnrollmentStatus.ACTIVE)
                    .build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(activeEnrollment));

            assertThatThrownBy(() -> waitingListService.leaveWaitingList(ENROLLMENT_ID))
                    .isInstanceOf(InvalidEnrollmentStateException.class)
                    .hasMessageContaining("not on waiting list")
                    .hasMessageContaining("ACTIVE");

            // The enrollment must not be touched
            assertThat(activeEnrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
            verify(enrollmentRepositoryPort, never()).decrementWaitingListPositionsAfter(anyLong(), anyInt());
        }

        @Test
        void shouldSkipDecrementWhenWaitingEnrollmentHasNullPosition() {
            // Data-inconsistency edge captured as-is: WAITING_LIST status but null position.
            // The service still withdraws but silently skips the position adjustment.
            Enrollment inconsistent = waitingEnrollment(ENROLLMENT_ID, STUDENT_ID, null);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(inconsistent));
            stubSaveEcho();

            Enrollment result = waitingListService.leaveWaitingList(ENROLLMENT_ID);

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.WITHDRAWN);
            verify(enrollmentRepositoryPort, never()).decrementWaitingListPositionsAfter(anyLong(), anyInt());
        }
    }

    // ==================== promoteNextFromWaitingList ====================

    @Nested
    class PromoteNextFromWaitingList {

        @Test
        void shouldPromoteFirstInQueueToActiveWhenWaitingListHasStudents() {
            Enrollment first = waitingEnrollment(1L, 10L, 1);
            Enrollment second = waitingEnrollment(2L, 11L, 2);
            Enrollment third = waitingEnrollment(3L, 12L, 3);
            when(groupRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(group));
            when(enrollmentRepositoryPort.findWaitingListByGroupId(GROUP_ID))
                    .thenReturn(List.of(first, second, third));
            stubSaveEcho();

            LocalDateTime before = LocalDateTime.now();
            Enrollment result = waitingListService.promoteNextFromWaitingList(GROUP_ID);
            LocalDateTime after = LocalDateTime.now();

            // FIFO: the head of the list (position 1) is the one promoted
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getStudentId()).isEqualTo(10L);
            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            assertThat(result.getWaitingListPosition()).isNull();
            assertThat(result.getPromotedAt()).isNotNull()
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);

            verify(enrollmentRepositoryPort).save(first);
            // Everyone behind position 1 moves up one slot
            verify(enrollmentRepositoryPort).decrementWaitingListPositionsAfter(GROUP_ID, 1);
            // The others are not persisted individually by the service
            verify(enrollmentRepositoryPort, never()).save(second);
            verify(enrollmentRepositoryPort, never()).save(third);
        }

        @Test
        void shouldGenerateAutoReservationsForPromotedStudent() {
            Enrollment first = waitingEnrollment(7L, 42L, 1);
            when(groupRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(group));
            when(enrollmentRepositoryPort.findWaitingListByGroupId(GROUP_ID)).thenReturn(List.of(first));
            stubSaveEcho();

            waitingListService.promoteNextFromWaitingList(GROUP_ID);

            verify(autoReservationPort).generateForNewEnrollment(42L, GROUP_ID, 7L);
        }

        @Test
        void shouldReturnNullWithoutSideEffectsWhenWaitingListIsEmpty() {
            when(groupRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(group));
            when(enrollmentRepositoryPort.findWaitingListByGroupId(GROUP_ID)).thenReturn(List.of());

            Enrollment result = waitingListService.promoteNextFromWaitingList(GROUP_ID);

            // No-op contract: null return, nothing saved, no reservations generated
            assertThat(result).isNull();
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
            verify(enrollmentRepositoryPort, never()).decrementWaitingListPositionsAfter(anyLong(), anyInt());
            verifyNoInteractions(autoReservationPort);
        }

        @Test
        void shouldThrowGroupNotFoundWhenGroupDoesNotExist() {
            when(groupRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> waitingListService.promoteNextFromWaitingList(GROUP_ID))
                    .isInstanceOf(GroupNotFoundException.class)
                    .hasMessageContaining(String.valueOf(GROUP_ID));

            verify(enrollmentRepositoryPort, never()).findWaitingListByGroupId(anyLong());
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
            verifyNoInteractions(autoReservationPort);
        }

        @Test
        void shouldLockGroupRowBeforeReadingWaitingList() {
            Enrollment first = waitingEnrollment(1L, 10L, 1);
            when(groupRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(group));
            when(enrollmentRepositoryPort.findWaitingListByGroupId(GROUP_ID)).thenReturn(List.of(first));
            stubSaveEcho();

            waitingListService.promoteNextFromWaitingList(GROUP_ID);

            InOrder inOrder = inOrder(groupRepositoryPort, enrollmentRepositoryPort);
            inOrder.verify(groupRepositoryPort).findByIdForUpdate(GROUP_ID);
            inOrder.verify(enrollmentRepositoryPort).findWaitingListByGroupId(GROUP_ID);
        }

        @Test
        void shouldPromoteButSkipDecrementWhenHeadOfQueueHasNullPosition() {
            // Data-inconsistency edge captured as-is: head of queue with null position.
            // Promotion still happens (ACTIVE + reservations) but no decrement is issued.
            Enrollment inconsistentHead = waitingEnrollment(9L, 50L, null);
            when(groupRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(group));
            when(enrollmentRepositoryPort.findWaitingListByGroupId(GROUP_ID))
                    .thenReturn(List.of(inconsistentHead));
            stubSaveEcho();

            Enrollment result = waitingListService.promoteNextFromWaitingList(GROUP_ID);

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            verify(enrollmentRepositoryPort, never()).decrementWaitingListPositionsAfter(anyLong(), anyInt());
            verify(autoReservationPort).generateForNewEnrollment(50L, GROUP_ID, 9L);
        }
    }
}
