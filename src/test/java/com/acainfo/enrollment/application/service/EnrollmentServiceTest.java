package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.dto.ChangeGroupCommand;
import com.acainfo.enrollment.application.dto.EnrollStudentCommand;
import com.acainfo.enrollment.application.port.out.AutoReservationPort;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.exception.AlreadyEnrolledException;
import com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException;
import com.acainfo.enrollment.domain.exception.GroupFullException;
import com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.domain.model.SubjectGroup;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link EnrollmentService}.
 *
 * <p>These tests capture the CURRENT behavior of the service (safety net before
 * the group -> course migration), including quirks such as {@code enroll} not
 * validating the group status (a CLOSED/CANCELLED group still accepts enrollment
 * requests).</p>
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    private static final Long STUDENT_ID = 10L;
    private static final Long GROUP_ID = 100L;
    private static final Long NEW_GROUP_ID = 200L;
    private static final Long ENROLLMENT_ID = 1000L;

    @Mock
    private EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private WaitingListService waitingListService;

    @Mock
    private AutoReservationPort autoReservationPort;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private SubjectGroup.SubjectGroupBuilder openGroupBuilder() {
        return SubjectGroup.builder()
                .id(GROUP_ID)
                .name("Algebra grupo 1 25-26")
                .subjectId(5L)
                .teacherId(7L)
                .status(GroupStatus.OPEN);
    }

    private Enrollment.EnrollmentBuilder enrollmentBuilder(EnrollmentStatus status) {
        return Enrollment.builder()
                .id(ENROLLMENT_ID)
                .studentId(STUDENT_ID)
                .groupId(GROUP_ID)
                .pricePerHour(new BigDecimal("15.00"))
                .status(status)
                .enrolledAt(LocalDateTime.now().minusDays(3));
    }

    // ==================== enroll ====================

    @Nested
    class Enroll {

        @Test
        void shouldCreatePendingApprovalEnrollmentWithGroupPriceWhenEnrolling() {
            SubjectGroup group = openGroupBuilder()
                    .pricePerHour(new BigDecimal("18.50"))
                    .build();
            when(groupRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(group));
            when(enrollmentRepositoryPort.existsActiveOrWaitingOrPendingEnrollment(STUDENT_ID, GROUP_ID))
                    .thenReturn(false);
            when(enrollmentRepositoryPort.save(any(Enrollment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LocalDateTime before = LocalDateTime.now();
            Enrollment result = enrollmentService.enroll(new EnrollStudentCommand(STUDENT_ID, GROUP_ID));
            LocalDateTime after = LocalDateTime.now();

            ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
            verify(enrollmentRepositoryPort).save(captor.capture());
            Enrollment saved = captor.getValue();

            assertThat(saved.getStudentId()).isEqualTo(STUDENT_ID);
            assertThat(saved.getGroupId()).isEqualTo(GROUP_ID);
            assertThat(saved.getStatus()).isEqualTo(EnrollmentStatus.PENDING_APPROVAL);
            assertThat(saved.getPricePerHour()).isEqualByComparingTo("18.50");
            assertThat(saved.getEnrolledAt()).isNotNull()
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
            assertThat(saved.getWaitingListPosition()).isNull();
            assertThat(saved.getIntensiveId()).isNull();
            assertThat(result).isSameAs(saved);
        }

        @Test
        void shouldUseDefaultPricePerHourWhenGroupHasNoCustomPrice() {
            SubjectGroup group = openGroupBuilder()
                    .pricePerHour(null)
                    .build();
            when(groupRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(group));
            when(enrollmentRepositoryPort.existsActiveOrWaitingOrPendingEnrollment(STUDENT_ID, GROUP_ID))
                    .thenReturn(false);
            when(enrollmentRepositoryPort.save(any(Enrollment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Enrollment result = enrollmentService.enroll(new EnrollStudentCommand(STUDENT_ID, GROUP_ID));

            assertThat(result.getPricePerHour())
                    .isEqualByComparingTo(SubjectGroup.DEFAULT_PRICE_PER_HOUR);
        }

        @Test
        void shouldThrowGroupNotFoundExceptionWhenEnrollingInNonExistentGroup() {
            when(groupRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.enroll(new EnrollStudentCommand(STUDENT_ID, GROUP_ID)))
                    .isInstanceOf(GroupNotFoundException.class)
                    .hasMessageContaining("Group not found with ID: " + GROUP_ID);

            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }

        @Test
        void shouldThrowAlreadyEnrolledExceptionWhenStudentAlreadyHasActiveWaitingOrPendingEnrollment() {
            SubjectGroup group = openGroupBuilder().build();
            when(groupRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(group));
            when(enrollmentRepositoryPort.existsActiveOrWaitingOrPendingEnrollment(STUDENT_ID, GROUP_ID))
                    .thenReturn(true);

            assertThatThrownBy(() -> enrollmentService.enroll(new EnrollStudentCommand(STUDENT_ID, GROUP_ID)))
                    .isInstanceOf(AlreadyEnrolledException.class)
                    .hasMessageContaining("already enrolled");

            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }

        @Test
        void shouldStillCreateEnrollmentWhenGroupIsNotOpen() {
            // CURRENT behavior: enroll() never checks the group status, so a CLOSED
            // (or CANCELLED) group still accepts enrollment requests. Captured as-is.
            SubjectGroup closedGroup = openGroupBuilder()
                    .status(GroupStatus.CLOSED)
                    .pricePerHour(new BigDecimal("20.00"))
                    .build();
            when(groupRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(closedGroup));
            when(enrollmentRepositoryPort.existsActiveOrWaitingOrPendingEnrollment(STUDENT_ID, GROUP_ID))
                    .thenReturn(false);
            when(enrollmentRepositoryPort.save(any(Enrollment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Enrollment result = enrollmentService.enroll(new EnrollStudentCommand(STUDENT_ID, GROUP_ID));

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.PENDING_APPROVAL);
            assertThat(result.getPricePerHour()).isEqualByComparingTo("20.00");
            verify(enrollmentRepositoryPort).save(any(Enrollment.class));
        }
    }

    // ==================== withdraw ====================

    @Nested
    class Withdraw {

        @Test
        void shouldMarkWithdrawnCancelFutureReservationsAndPromoteNextWhenWithdrawingActiveEnrollment() {
            Enrollment active = enrollmentBuilder(EnrollmentStatus.ACTIVE)
                    .waitingListPosition(null)
                    .build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(active));
            when(enrollmentRepositoryPort.save(any(Enrollment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LocalDateTime before = LocalDateTime.now();
            Enrollment result = enrollmentService.withdraw(ENROLLMENT_ID);
            LocalDateTime after = LocalDateTime.now();

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.WITHDRAWN);
            assertThat(result.getWithdrawnAt()).isNotNull()
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
            assertThat(result.getWaitingListPosition()).isNull();

            InOrder inOrder = inOrder(enrollmentRepositoryPort, autoReservationPort, waitingListService);
            inOrder.verify(enrollmentRepositoryPort).save(active);
            inOrder.verify(autoReservationPort).cancelFutureReservations(STUDENT_ID, GROUP_ID);
            inOrder.verify(waitingListService).promoteNextFromWaitingList(GROUP_ID);
            // ACTIVE enrollments have no waiting list position -> no decrement
            verify(enrollmentRepositoryPort, never()).decrementWaitingListPositionsAfter(anyLong(), anyInt());
        }

        @Test
        void shouldDecrementFollowingPositionsWithoutPromotionWhenWithdrawingFromWaitingList() {
            Enrollment waiting = enrollmentBuilder(EnrollmentStatus.WAITING_LIST)
                    .waitingListPosition(3)
                    .build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(waiting));
            when(enrollmentRepositoryPort.save(any(Enrollment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Enrollment result = enrollmentService.withdraw(ENROLLMENT_ID);

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.WITHDRAWN);
            assertThat(result.getWithdrawnAt()).isNotNull();
            assertThat(result.getWaitingListPosition()).isNull();

            verify(enrollmentRepositoryPort).decrementWaitingListPositionsAfter(GROUP_ID, 3);
            verifyNoInteractions(autoReservationPort);
            verifyNoInteractions(waitingListService);
        }

        @Test
        void shouldWithdrawPendingApprovalEnrollmentWithoutAnySideEffects() {
            Enrollment pending = enrollmentBuilder(EnrollmentStatus.PENDING_APPROVAL)
                    .waitingListPosition(null)
                    .build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(pending));
            when(enrollmentRepositoryPort.save(any(Enrollment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Enrollment result = enrollmentService.withdraw(ENROLLMENT_ID);

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.WITHDRAWN);
            verify(enrollmentRepositoryPort, never()).decrementWaitingListPositionsAfter(anyLong(), anyInt());
            verifyNoInteractions(autoReservationPort);
            verifyNoInteractions(waitingListService);
        }

        @Test
        void shouldThrowInvalidEnrollmentStateExceptionWhenWithdrawingAlreadyWithdrawnEnrollment() {
            Enrollment withdrawn = enrollmentBuilder(EnrollmentStatus.WITHDRAWN)
                    .withdrawnAt(LocalDateTime.now().minusDays(1))
                    .build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(withdrawn));

            assertThatThrownBy(() -> enrollmentService.withdraw(ENROLLMENT_ID))
                    .isInstanceOf(InvalidEnrollmentStateException.class)
                    .hasMessageContaining("Cannot withdraw enrollment with status: WITHDRAWN");

            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
            verifyNoInteractions(autoReservationPort);
            verifyNoInteractions(waitingListService);
        }

        @Test
        void shouldThrowInvalidEnrollmentStateExceptionWhenWithdrawingCompletedEnrollment() {
            Enrollment completed = enrollmentBuilder(EnrollmentStatus.COMPLETED).build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(completed));

            assertThatThrownBy(() -> enrollmentService.withdraw(ENROLLMENT_ID))
                    .isInstanceOf(InvalidEnrollmentStateException.class)
                    .hasMessageContaining("COMPLETED");

            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }

        @Test
        void shouldThrowEnrollmentNotFoundExceptionWhenWithdrawingNonExistentEnrollment() {
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.withdraw(ENROLLMENT_ID))
                    .isInstanceOf(EnrollmentNotFoundException.class)
                    .hasMessageContaining("Enrollment not found with ID: " + ENROLLMENT_ID);

            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }
    }

    // ==================== changeGroup ====================

    @Nested
    class ChangeGroup {

        @Test
        void shouldChangeGroupAndPromoteNextFromOldGroupWhenTargetGroupHasCapacity() {
            Enrollment active = enrollmentBuilder(EnrollmentStatus.ACTIVE).build();
            SubjectGroup newGroup = openGroupBuilder()
                    .id(NEW_GROUP_ID)
                    .capacity(null) // default capacity = 24
                    .build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(active));
            when(groupRepositoryPort.findById(NEW_GROUP_ID)).thenReturn(Optional.of(newGroup));
            when(enrollmentRepositoryPort.countActiveByGroupId(NEW_GROUP_ID)).thenReturn(23L);
            when(enrollmentRepositoryPort.save(any(Enrollment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Enrollment result = enrollmentService.changeGroup(
                    new ChangeGroupCommand(ENROLLMENT_ID, NEW_GROUP_ID));

            assertThat(result.getGroupId()).isEqualTo(NEW_GROUP_ID);
            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            verify(enrollmentRepositoryPort).save(active);
            // Promotion happens on the OLD group's waiting list (a seat was freed there)
            verify(waitingListService).promoteNextFromWaitingList(GROUP_ID);
        }

        @Test
        void shouldThrowGroupFullExceptionWhenTargetGroupIsAtCustomCapacity() {
            Enrollment active = enrollmentBuilder(EnrollmentStatus.ACTIVE).build();
            SubjectGroup newGroup = openGroupBuilder()
                    .id(NEW_GROUP_ID)
                    .capacity(10)
                    .build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(active));
            when(groupRepositoryPort.findById(NEW_GROUP_ID)).thenReturn(Optional.of(newGroup));
            when(enrollmentRepositoryPort.countActiveByGroupId(NEW_GROUP_ID)).thenReturn(10L);

            assertThatThrownBy(() -> enrollmentService.changeGroup(
                    new ChangeGroupCommand(ENROLLMENT_ID, NEW_GROUP_ID)))
                    .isInstanceOf(GroupFullException.class)
                    .hasMessageContaining("Group " + NEW_GROUP_ID + " is full");

            assertThat(active.getGroupId()).isEqualTo(GROUP_ID);
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
            verifyNoInteractions(waitingListService);
        }

        @Test
        void shouldThrowGroupFullExceptionWhenTargetGroupIsAtDefaultCapacity() {
            Enrollment active = enrollmentBuilder(EnrollmentStatus.ACTIVE).build();
            SubjectGroup newGroup = openGroupBuilder()
                    .id(NEW_GROUP_ID)
                    .capacity(null) // default capacity = 24
                    .build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(active));
            when(groupRepositoryPort.findById(NEW_GROUP_ID)).thenReturn(Optional.of(newGroup));
            when(enrollmentRepositoryPort.countActiveByGroupId(NEW_GROUP_ID))
                    .thenReturn((long) SubjectGroup.DEFAULT_MAX_CAPACITY);

            assertThatThrownBy(() -> enrollmentService.changeGroup(
                    new ChangeGroupCommand(ENROLLMENT_ID, NEW_GROUP_ID)))
                    .isInstanceOf(GroupFullException.class);

            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }

        @Test
        void shouldThrowInvalidEnrollmentStateExceptionWhenChangingGroupOfNonActiveEnrollment() {
            Enrollment waiting = enrollmentBuilder(EnrollmentStatus.WAITING_LIST)
                    .waitingListPosition(1)
                    .build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(waiting));

            assertThatThrownBy(() -> enrollmentService.changeGroup(
                    new ChangeGroupCommand(ENROLLMENT_ID, NEW_GROUP_ID)))
                    .isInstanceOf(InvalidEnrollmentStateException.class)
                    .hasMessageContaining("Only ACTIVE enrollments can change group")
                    .hasMessageContaining("WAITING_LIST");

            verify(groupRepositoryPort, never()).findById(anyLong());
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }

        @Test
        void shouldThrowGroupNotFoundExceptionWhenTargetGroupDoesNotExist() {
            Enrollment active = enrollmentBuilder(EnrollmentStatus.ACTIVE).build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(active));
            when(groupRepositoryPort.findById(NEW_GROUP_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.changeGroup(
                    new ChangeGroupCommand(ENROLLMENT_ID, NEW_GROUP_ID)))
                    .isInstanceOf(GroupNotFoundException.class)
                    .hasMessageContaining("Group not found with ID: " + NEW_GROUP_ID);

            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
            verifyNoInteractions(waitingListService);
        }

        @Test
        void shouldThrowEnrollmentNotFoundExceptionWhenChangingGroupOfNonExistentEnrollment() {
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.changeGroup(
                    new ChangeGroupCommand(ENROLLMENT_ID, NEW_GROUP_ID)))
                    .isInstanceOf(EnrollmentNotFoundException.class);

            verifyNoInteractions(groupRepositoryPort);
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }
    }

    // ==================== getById ====================

    @Nested
    class GetById {

        @Test
        void shouldReturnEnrollmentWhenItExists() {
            Enrollment enrollment = enrollmentBuilder(EnrollmentStatus.ACTIVE).build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));

            Enrollment result = enrollmentService.getById(ENROLLMENT_ID);

            assertThat(result).isSameAs(enrollment);
        }

        @Test
        void shouldThrowEnrollmentNotFoundExceptionWhenEnrollmentDoesNotExist() {
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.getById(ENROLLMENT_ID))
                    .isInstanceOf(EnrollmentNotFoundException.class)
                    .hasMessageContaining(String.valueOf(ENROLLMENT_ID));
        }
    }
}
