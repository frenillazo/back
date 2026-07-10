package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.dto.ChangeCourseCommand;
import com.acainfo.enrollment.application.dto.EnrollStudentCommand;
import com.acainfo.enrollment.application.port.out.AutoReservationPort;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.exception.AlreadyEnrolledException;
import com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException;
import com.acainfo.enrollment.domain.exception.CourseFullException;
import com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.course.domain.exception.CourseNotFoundException;
import com.acainfo.course.domain.model.CourseStatus;
import com.acainfo.course.domain.model.Course;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private CourseRepositoryPort courseRepositoryPort;

    @Mock
    private WaitingListService waitingListService;

    @Mock
    private AutoReservationPort autoReservationPort;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Course.CourseBuilder openGroupBuilder() {
        return Course.builder()
                .id(GROUP_ID)
                .name("Algebra grupo 1 25-26")
                .subjectId(5L)
                .teacherId(7L)
                .status(CourseStatus.OPEN);
    }

    private Enrollment.EnrollmentBuilder enrollmentBuilder(EnrollmentStatus status) {
        return Enrollment.builder()
                .id(ENROLLMENT_ID)
                .studentId(STUDENT_ID)
                .courseId(GROUP_ID)
                .status(status)
                .enrolledAt(LocalDateTime.now().minusDays(3));
    }

    // ==================== enroll ====================

    @Nested
    class Enroll {

        @Test
        void shouldCreatePendingApprovalEnrollmentWhenEnrolling() {
            // Unified course model: enrollments no longer copy any price from the course.
            Course group = openGroupBuilder().build();
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(group));
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
            assertThat(saved.getCourseId()).isEqualTo(GROUP_ID);
            assertThat(saved.getStatus()).isEqualTo(EnrollmentStatus.PENDING_APPROVAL);
            assertThat(saved.getEnrolledAt()).isNotNull()
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
            assertThat(saved.getWaitingListPosition()).isNull();
            assertThat(result).isSameAs(saved);
        }

        @Test
        void shouldThrowCourseNotFoundExceptionWhenEnrollingInNonExistentGroup() {
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.enroll(new EnrollStudentCommand(STUDENT_ID, GROUP_ID)))
                    .isInstanceOf(CourseNotFoundException.class)
                    .hasMessageContaining("Course not found with ID: " + GROUP_ID);

            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }

        @Test
        void shouldThrowAlreadyEnrolledExceptionWhenStudentAlreadyHasActiveWaitingOrPendingEnrollment() {
            Course group = openGroupBuilder().build();
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(group));
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
            Course closedGroup = openGroupBuilder()
                    .status(CourseStatus.CLOSED)
                    .build();
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(closedGroup));
            when(enrollmentRepositoryPort.existsActiveOrWaitingOrPendingEnrollment(STUDENT_ID, GROUP_ID))
                    .thenReturn(false);
            when(enrollmentRepositoryPort.save(any(Enrollment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Enrollment result = enrollmentService.enroll(new EnrollStudentCommand(STUDENT_ID, GROUP_ID));

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.PENDING_APPROVAL);
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

    // ==================== changeCourse ====================

    @Nested
    class ChangeGroup {

        @Test
        void shouldChangeGroupAndPromoteNextFromOldGroupWhenTargetGroupHasCapacity() {
            Enrollment active = enrollmentBuilder(EnrollmentStatus.ACTIVE).build();
            Course newGroup = openGroupBuilder()
                    .id(NEW_GROUP_ID)
                    .capacity(24)
                    .build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(active));
            when(courseRepositoryPort.findById(NEW_GROUP_ID)).thenReturn(Optional.of(newGroup));
            when(enrollmentRepositoryPort.countActiveByCourseId(NEW_GROUP_ID)).thenReturn(23L);
            when(enrollmentRepositoryPort.save(any(Enrollment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Enrollment result = enrollmentService.changeCourse(
                    new ChangeCourseCommand(ENROLLMENT_ID, NEW_GROUP_ID));

            assertThat(result.getCourseId()).isEqualTo(NEW_GROUP_ID);
            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            verify(enrollmentRepositoryPort).save(active);
            // Promotion happens on the OLD group's waiting list (a seat was freed there)
            verify(waitingListService).promoteNextFromWaitingList(GROUP_ID);
        }

        @Test
        void shouldThrowCourseFullExceptionWhenTargetGroupIsAtCustomCapacity() {
            Enrollment active = enrollmentBuilder(EnrollmentStatus.ACTIVE).build();
            Course newGroup = openGroupBuilder()
                    .id(NEW_GROUP_ID)
                    .capacity(10)
                    .build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(active));
            when(courseRepositoryPort.findById(NEW_GROUP_ID)).thenReturn(Optional.of(newGroup));
            when(enrollmentRepositoryPort.countActiveByCourseId(NEW_GROUP_ID)).thenReturn(10L);

            assertThatThrownBy(() -> enrollmentService.changeCourse(
                    new ChangeCourseCommand(ENROLLMENT_ID, NEW_GROUP_ID)))
                    .isInstanceOf(CourseFullException.class)
                    .hasMessageContaining("Course " + NEW_GROUP_ID + " is full");

            assertThat(active.getCourseId()).isEqualTo(GROUP_ID);
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
            verifyNoInteractions(waitingListService);
        }

        @Test
        void shouldChangeCourseWithoutCapacityCheckWhenTargetCourseHasNullCapacity() {
            // Unified course model: capacity == null means unlimited (virtual/dual),
            // so occupancy is never queried and the change always succeeds.
            Enrollment active = enrollmentBuilder(EnrollmentStatus.ACTIVE).build();
            Course newGroup = openGroupBuilder()
                    .id(NEW_GROUP_ID)
                    .capacity(null)
                    .build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(active));
            when(courseRepositoryPort.findById(NEW_GROUP_ID)).thenReturn(Optional.of(newGroup));
            when(enrollmentRepositoryPort.save(any(Enrollment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Enrollment result = enrollmentService.changeCourse(
                    new ChangeCourseCommand(ENROLLMENT_ID, NEW_GROUP_ID));

            assertThat(result.getCourseId()).isEqualTo(NEW_GROUP_ID);
            verify(enrollmentRepositoryPort, never()).countActiveByCourseId(anyLong());
            verify(waitingListService).promoteNextFromWaitingList(GROUP_ID);
        }

        @Test
        void shouldThrowInvalidEnrollmentStateExceptionWhenChangingGroupOfNonActiveEnrollment() {
            Enrollment waiting = enrollmentBuilder(EnrollmentStatus.WAITING_LIST)
                    .waitingListPosition(1)
                    .build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(waiting));

            assertThatThrownBy(() -> enrollmentService.changeCourse(
                    new ChangeCourseCommand(ENROLLMENT_ID, NEW_GROUP_ID)))
                    .isInstanceOf(InvalidEnrollmentStateException.class)
                    .hasMessageContaining("Only ACTIVE enrollments can change group")
                    .hasMessageContaining("WAITING_LIST");

            verify(courseRepositoryPort, never()).findById(anyLong());
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }

        @Test
        void shouldThrowCourseNotFoundExceptionWhenTargetGroupDoesNotExist() {
            Enrollment active = enrollmentBuilder(EnrollmentStatus.ACTIVE).build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(active));
            when(courseRepositoryPort.findById(NEW_GROUP_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.changeCourse(
                    new ChangeCourseCommand(ENROLLMENT_ID, NEW_GROUP_ID)))
                    .isInstanceOf(CourseNotFoundException.class)
                    .hasMessageContaining("Course not found with ID: " + NEW_GROUP_ID);

            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
            verifyNoInteractions(waitingListService);
        }

        @Test
        void shouldThrowEnrollmentNotFoundExceptionWhenChangingGroupOfNonExistentEnrollment() {
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.changeCourse(
                    new ChangeCourseCommand(ENROLLMENT_ID, NEW_GROUP_ID)))
                    .isInstanceOf(EnrollmentNotFoundException.class);

            verifyNoInteractions(courseRepositoryPort);
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
