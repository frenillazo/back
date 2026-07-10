package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.port.out.AutoReservationPort;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.exception.EnrollmentNotFoundException;
import com.acainfo.enrollment.domain.exception.InvalidEnrollmentStateException;
import com.acainfo.enrollment.domain.exception.UnauthorizedApprovalException;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.course.domain.exception.CourseNotFoundException;
import com.acainfo.course.domain.model.CourseStatus;
import com.acainfo.course.domain.model.Course;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import com.acainfo.user.domain.model.Role;
import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.domain.model.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link EnrollmentApprovalService}.
 *
 * <p>These tests capture the CURRENT behavior of the approval/rejection flow
 * as a safety net before the group->course migration. They intentionally
 * document quirks (e.g. {@code approvedByUserId} being reused for rejections,
 * {@code reject} not taking the pessimistic lock) exactly as the code behaves today.</p>
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentApprovalServiceTest {

    private static final Long ENROLLMENT_ID = 10L;
    private static final Long STUDENT_ID = 100L;
    private static final Long GROUP_ID = 5L;
    private static final Long TEACHER_ID = 50L;
    private static final Long ADMIN_ID = 1L;
    private static final Long OTHER_TEACHER_ID = 99L;

    @Mock
    private EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Mock
    private CourseRepositoryPort courseRepositoryPort;

    @Mock
    private GetUserProfileUseCase getUserProfileUseCase;

    @Mock
    private AutoReservationPort autoReservationPort;

    @InjectMocks
    private EnrollmentApprovalService service;

    // ==================== Test data builders ====================

    private Enrollment pendingEnrollment() {
        return Enrollment.builder()
                .id(ENROLLMENT_ID)
                .studentId(STUDENT_ID)
                .courseId(GROUP_ID)
                .status(EnrollmentStatus.PENDING_APPROVAL)
                .build();
    }

    private Enrollment enrollmentWithStatus(EnrollmentStatus status) {
        return Enrollment.builder()
                .id(ENROLLMENT_ID)
                .studentId(STUDENT_ID)
                .courseId(GROUP_ID)
                .status(status)
                .build();
    }

    private Course groupWithCapacity(Integer capacity) {
        return Course.builder()
                .id(GROUP_ID)
                .name("Algebra grupo 1 25-26")
                .subjectId(7L)
                .teacherId(TEACHER_ID)
                .status(CourseStatus.OPEN)
                .capacity(capacity)
                .build();
    }

    private User userWithRole(Long id, String email, RoleType roleType) {
        return User.builder()
                .id(id)
                .email(email)
                .roles(Set.of(Role.builder().type(roleType).build()))
                .build();
    }

    private User admin() {
        return userWithRole(ADMIN_ID, "admin@acainfo.com", RoleType.ADMIN);
    }

    private User groupTeacher() {
        return userWithRole(TEACHER_ID, "teacher@acainfo.com", RoleType.TEACHER);
    }

    private void stubSaveReturnsArgument() {
        when(enrollmentRepositoryPort.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ==================== approve() ====================

    @Nested
    class Approve {

        @Test
        void shouldApproveAsActiveWhenSeatsAvailable() {
            Enrollment enrollment = pendingEnrollment();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(2)));
            when(getUserProfileUseCase.getUserById(ADMIN_ID)).thenReturn(admin());
            when(enrollmentRepositoryPort.countActiveByCourseId(GROUP_ID)).thenReturn(0L);
            stubSaveReturnsArgument();

            Enrollment result = service.approve(ENROLLMENT_ID, ADMIN_ID);

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            assertThat(result.getApprovedAt()).isNotNull();
            assertThat(result.getApprovedByUserId()).isEqualTo(ADMIN_ID);
            assertThat(result.getWaitingListPosition()).isNull();
            assertThat(result.getRejectedAt()).isNull();

            ArgumentCaptor<Enrollment> savedCaptor = ArgumentCaptor.forClass(Enrollment.class);
            verify(enrollmentRepositoryPort).save(savedCaptor.capture());
            assertThat(savedCaptor.getValue().getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            verify(enrollmentRepositoryPort, never()).getNextWaitingListPosition(anyLong());
        }

        @Test
        void shouldTriggerAutoReservationsWhenApprovedAsActive() {
            Enrollment enrollment = pendingEnrollment();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(24)));
            when(getUserProfileUseCase.getUserById(ADMIN_ID)).thenReturn(admin());
            when(enrollmentRepositoryPort.countActiveByCourseId(GROUP_ID)).thenReturn(10L);
            stubSaveReturnsArgument();

            service.approve(ENROLLMENT_ID, ADMIN_ID);

            verify(autoReservationPort).generateForNewEnrollment(STUDENT_ID, GROUP_ID, ENROLLMENT_ID);
        }

        @Test
        void shouldApproveAsActiveWhenExactlyOneSeatRemains() {
            // Boundary: activeCount (1) < maxCapacity (2) -> last seat goes to this student
            Enrollment enrollment = pendingEnrollment();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(2)));
            when(getUserProfileUseCase.getUserById(ADMIN_ID)).thenReturn(admin());
            when(enrollmentRepositoryPort.countActiveByCourseId(GROUP_ID)).thenReturn(1L);
            stubSaveReturnsArgument();

            Enrollment result = service.approve(ENROLLMENT_ID, ADMIN_ID);

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            verify(autoReservationPort).generateForNewEnrollment(STUDENT_ID, GROUP_ID, ENROLLMENT_ID);
        }

        @Test
        void shouldAddToWaitingListWithNextPositionWhenGroupIsFull() {
            // Boundary: activeCount (2) == maxCapacity (2) -> no seat
            Enrollment enrollment = pendingEnrollment();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(2)));
            when(getUserProfileUseCase.getUserById(ADMIN_ID)).thenReturn(admin());
            when(enrollmentRepositoryPort.countActiveByCourseId(GROUP_ID)).thenReturn(2L);
            when(enrollmentRepositoryPort.getNextWaitingListPosition(GROUP_ID)).thenReturn(3);
            stubSaveReturnsArgument();

            Enrollment result = service.approve(ENROLLMENT_ID, ADMIN_ID);

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.WAITING_LIST);
            assertThat(result.getWaitingListPosition()).isEqualTo(3);
            // Current behavior: approval metadata is set even when parked on the waiting list
            assertThat(result.getApprovedAt()).isNotNull();
            assertThat(result.getApprovedByUserId()).isEqualTo(ADMIN_ID);

            verify(enrollmentRepositoryPort).save(enrollment);
            verifyNoInteractions(autoReservationPort);
        }

        @Test
        void shouldApproveAsActiveWithoutCapacityCheckWhenCapacityIsNull() {
            // Unified course model: capacity == null -> unlimited (virtual/dual).
            // The approval is ALWAYS ACTIVE: occupancy is never queried and the
            // waiting list position counter is never consulted, no matter how many
            // students (24, 100, N...) are already enrolled.
            Enrollment enrollment = pendingEnrollment();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(null)));
            when(getUserProfileUseCase.getUserById(ADMIN_ID)).thenReturn(admin());
            stubSaveReturnsArgument();

            Enrollment result = service.approve(ENROLLMENT_ID, ADMIN_ID);

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            assertThat(result.getWaitingListPosition()).isNull();
            verify(enrollmentRepositoryPort, never()).countActiveByCourseId(anyLong());
            verify(enrollmentRepositoryPort, never()).getNextWaitingListPosition(anyLong());
        }

        @Test
        void shouldTriggerAutoReservationsWhenApprovedOnNullCapacityCourse() {
            // Null-capacity approvals are ACTIVE, so they also trigger auto-reservations.
            Enrollment enrollment = pendingEnrollment();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(null)));
            when(getUserProfileUseCase.getUserById(ADMIN_ID)).thenReturn(admin());
            stubSaveReturnsArgument();

            service.approve(ENROLLMENT_ID, ADMIN_ID);

            verify(autoReservationPort).generateForNewEnrollment(STUDENT_ID, GROUP_ID, ENROLLMENT_ID);
        }

        @Test
        void shouldAllowGroupTeacherToApprove() {
            Enrollment enrollment = pendingEnrollment();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(24)));
            when(getUserProfileUseCase.getUserById(TEACHER_ID)).thenReturn(groupTeacher());
            when(enrollmentRepositoryPort.countActiveByCourseId(GROUP_ID)).thenReturn(0L);
            stubSaveReturnsArgument();

            Enrollment result = service.approve(ENROLLMENT_ID, TEACHER_ID);

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            assertThat(result.getApprovedByUserId()).isEqualTo(TEACHER_ID);
        }

        @Test
        void shouldAllowAnyUserWhoseIdMatchesGroupTeacherIdRegardlessOfRole() {
            // Current behavior: authorization only compares group.teacherId with the approver's
            // user id; the approver's actual roles are NOT checked for the teacher path.
            Enrollment enrollment = pendingEnrollment();
            User studentWithTeacherId = userWithRole(TEACHER_ID, "student@acainfo.com", RoleType.STUDENT);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(24)));
            when(getUserProfileUseCase.getUserById(TEACHER_ID)).thenReturn(studentWithTeacherId);
            when(enrollmentRepositoryPort.countActiveByCourseId(GROUP_ID)).thenReturn(0L);
            stubSaveReturnsArgument();

            Enrollment result = service.approve(ENROLLMENT_ID, TEACHER_ID);

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        }

        @Test
        void shouldThrowUnauthorizedWhenApproverIsNeitherAdminNorGroupTeacher() {
            // A teacher of ANOTHER group is not authorized either
            Enrollment enrollment = pendingEnrollment();
            User otherTeacher = userWithRole(OTHER_TEACHER_ID, "other@acainfo.com", RoleType.TEACHER);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(24)));
            when(getUserProfileUseCase.getUserById(OTHER_TEACHER_ID)).thenReturn(otherTeacher);

            assertThatThrownBy(() -> service.approve(ENROLLMENT_ID, OTHER_TEACHER_ID))
                    .isInstanceOf(UnauthorizedApprovalException.class)
                    .hasMessageContaining("User " + OTHER_TEACHER_ID)
                    .hasMessageContaining("group " + GROUP_ID);

            assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.PENDING_APPROVAL);
            verify(enrollmentRepositoryPort, never()).countActiveByCourseId(anyLong());
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
            verifyNoInteractions(autoReservationPort);
        }

        @Test
        void shouldThrowUnauthorizedWhenCourseHasNoTeacherAndApproverIsNotAdmin() {
            // teacherId can be null (course without assigned teacher): only an admin
            // may approve; a non-admin user is rejected regardless of their id/role.
            Enrollment enrollment = pendingEnrollment();
            Course teacherlessCourse = groupWithCapacity(24).toBuilder().teacherId(null).build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(teacherlessCourse));
            when(getUserProfileUseCase.getUserById(TEACHER_ID)).thenReturn(groupTeacher());

            assertThatThrownBy(() -> service.approve(ENROLLMENT_ID, TEACHER_ID))
                    .isInstanceOf(UnauthorizedApprovalException.class);

            assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.PENDING_APPROVAL);
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
            verifyNoInteractions(autoReservationPort);
        }

        @Test
        void shouldAllowAdminToApproveWhenCourseHasNoTeacher() {
            Enrollment enrollment = pendingEnrollment();
            Course teacherlessCourse = groupWithCapacity(24).toBuilder().teacherId(null).build();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(teacherlessCourse));
            when(getUserProfileUseCase.getUserById(ADMIN_ID)).thenReturn(admin());
            when(enrollmentRepositoryPort.countActiveByCourseId(GROUP_ID)).thenReturn(0L);
            stubSaveReturnsArgument();

            Enrollment result = service.approve(ENROLLMENT_ID, ADMIN_ID);

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            assertThat(result.getApprovedByUserId()).isEqualTo(ADMIN_ID);
        }

        @ParameterizedTest
        @EnumSource(value = EnrollmentStatus.class, names = "PENDING_APPROVAL", mode = EnumSource.Mode.EXCLUDE)
        void shouldThrowInvalidStateWhenApprovingNonPendingEnrollment(EnrollmentStatus status) {
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(enrollmentWithStatus(status)));

            assertThatThrownBy(() -> service.approve(ENROLLMENT_ID, ADMIN_ID))
                    .isInstanceOf(InvalidEnrollmentStateException.class)
                    .hasMessage("Cannot approve enrollment with status: " + status);

            // State is validated BEFORE loading the group and BEFORE the authorization check
            verifyNoInteractions(courseRepositoryPort, getUserProfileUseCase, autoReservationPort);
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }

        @Test
        void shouldThrowEnrollmentNotFoundWhenEnrollmentDoesNotExist() {
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.approve(ENROLLMENT_ID, ADMIN_ID))
                    .isInstanceOf(EnrollmentNotFoundException.class)
                    .hasMessage("Enrollment not found with ID: " + ENROLLMENT_ID);

            verifyNoInteractions(courseRepositoryPort, getUserProfileUseCase, autoReservationPort);
        }

        @Test
        void shouldThrowGroupNotFoundWhenGroupDoesNotExist() {
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(pendingEnrollment()));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.approve(ENROLLMENT_ID, ADMIN_ID))
                    .isInstanceOf(CourseNotFoundException.class)
                    .hasMessage("Course not found with ID: " + GROUP_ID);

            verifyNoInteractions(getUserProfileUseCase, autoReservationPort);
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }

        @Test
        void shouldLockGroupWithFindByIdForUpdateBeforeAuthorizationAndCapacityCheck() {
            Enrollment enrollment = pendingEnrollment();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(24)));
            when(getUserProfileUseCase.getUserById(ADMIN_ID)).thenReturn(admin());
            when(enrollmentRepositoryPort.countActiveByCourseId(GROUP_ID)).thenReturn(0L);
            stubSaveReturnsArgument();

            service.approve(ENROLLMENT_ID, ADMIN_ID);

            InOrder order = inOrder(enrollmentRepositoryPort, courseRepositoryPort, getUserProfileUseCase);
            order.verify(enrollmentRepositoryPort).findById(ENROLLMENT_ID);
            order.verify(courseRepositoryPort).findByIdForUpdate(GROUP_ID);
            order.verify(getUserProfileUseCase).getUserById(ADMIN_ID);
            order.verify(enrollmentRepositoryPort).countActiveByCourseId(GROUP_ID);
            order.verify(enrollmentRepositoryPort).save(any(Enrollment.class));

            // approve() never uses the unlocked lookup
            verify(courseRepositoryPort, never()).findById(anyLong());
        }

        @Test
        void shouldSkipAutoReservationsWhenSaveReturnsNonActiveEnrollment() {
            // Current behavior quirk: the auto-reservation trigger checks the enrollment
            // RETURNED by save(), not the mutated in-memory instance. If the persistence
            // adapter ever returned a stale/non-active snapshot, no reservations would
            // be generated even though seats were available.
            Enrollment enrollment = pendingEnrollment();
            Enrollment staleSnapshot = enrollmentWithStatus(EnrollmentStatus.PENDING_APPROVAL);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(24)));
            when(getUserProfileUseCase.getUserById(ADMIN_ID)).thenReturn(admin());
            when(enrollmentRepositoryPort.countActiveByCourseId(GROUP_ID)).thenReturn(0L);
            when(enrollmentRepositoryPort.save(any(Enrollment.class))).thenReturn(staleSnapshot);

            Enrollment result = service.approve(ENROLLMENT_ID, ADMIN_ID);

            assertThat(result).isSameAs(staleSnapshot);
            verifyNoInteractions(autoReservationPort);
        }
    }

    // ==================== reject() ====================

    @Nested
    class Reject {

        @Test
        void shouldRejectWithReasonWhenPendingApproval() {
            Enrollment enrollment = pendingEnrollment();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(24)));
            when(getUserProfileUseCase.getUserById(TEACHER_ID)).thenReturn(groupTeacher());
            stubSaveReturnsArgument();

            Enrollment result = service.reject(ENROLLMENT_ID, TEACHER_ID, "Group is closing this term");

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.REJECTED);
            assertThat(result.getRejectedAt()).isNotNull();
            assertThat(result.getRejectionReason()).isEqualTo("Group is closing this term");
            // Current behavior quirk: the rejecter is stored in approvedByUserId
            assertThat(result.getApprovedByUserId()).isEqualTo(TEACHER_ID);
            assertThat(result.getApprovedAt()).isNull();

            verify(enrollmentRepositoryPort).save(enrollment);
            verifyNoInteractions(autoReservationPort);
        }

        @Test
        void shouldAllowAdminToRejectAnyGroupEnrollment() {
            Enrollment enrollment = pendingEnrollment();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(24)));
            when(getUserProfileUseCase.getUserById(ADMIN_ID)).thenReturn(admin());
            stubSaveReturnsArgument();

            Enrollment result = service.reject(ENROLLMENT_ID, ADMIN_ID, null);

            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.REJECTED);
            assertThat(result.getApprovedByUserId()).isEqualTo(ADMIN_ID);
            // Null reason is stored as-is
            assertThat(result.getRejectionReason()).isNull();
        }

        @Test
        void shouldUseUnlockedFindByIdOnReject() {
            // Current behavior: unlike approve(), reject() loads the group WITHOUT
            // the pessimistic lock (plain findById, no findByIdForUpdate).
            Enrollment enrollment = pendingEnrollment();
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(24)));
            when(getUserProfileUseCase.getUserById(TEACHER_ID)).thenReturn(groupTeacher());
            stubSaveReturnsArgument();

            service.reject(ENROLLMENT_ID, TEACHER_ID, "reason");

            verify(courseRepositoryPort).findById(GROUP_ID);
            verify(courseRepositoryPort, never()).findByIdForUpdate(anyLong());
        }

        @ParameterizedTest
        @EnumSource(value = EnrollmentStatus.class, names = "PENDING_APPROVAL", mode = EnumSource.Mode.EXCLUDE)
        void shouldThrowInvalidStateWhenRejectingNonPendingEnrollment(EnrollmentStatus status) {
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(enrollmentWithStatus(status)));

            assertThatThrownBy(() -> service.reject(ENROLLMENT_ID, TEACHER_ID, "too late"))
                    .isInstanceOf(InvalidEnrollmentStateException.class)
                    .hasMessage("Cannot reject enrollment with status: " + status);

            verifyNoInteractions(courseRepositoryPort, getUserProfileUseCase, autoReservationPort);
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }

        @Test
        void shouldThrowUnauthorizedWhenRejecterIsNeitherAdminNorGroupTeacher() {
            Enrollment enrollment = pendingEnrollment();
            User otherTeacher = userWithRole(OTHER_TEACHER_ID, "other@acainfo.com", RoleType.TEACHER);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(enrollment));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(groupWithCapacity(24)));
            when(getUserProfileUseCase.getUserById(OTHER_TEACHER_ID)).thenReturn(otherTeacher);

            assertThatThrownBy(() -> service.reject(ENROLLMENT_ID, OTHER_TEACHER_ID, "nope"))
                    .isInstanceOf(UnauthorizedApprovalException.class);

            assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.PENDING_APPROVAL);
            assertThat(enrollment.getRejectionReason()).isNull();
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }

        @Test
        void shouldThrowEnrollmentNotFoundWhenRejectingMissingEnrollment() {
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.reject(ENROLLMENT_ID, TEACHER_ID, "reason"))
                    .isInstanceOf(EnrollmentNotFoundException.class);

            verifyNoInteractions(courseRepositoryPort, getUserProfileUseCase, autoReservationPort);
        }

        @Test
        void shouldThrowGroupNotFoundWhenRejectingAndGroupDoesNotExist() {
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(pendingEnrollment()));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.reject(ENROLLMENT_ID, TEACHER_ID, "reason"))
                    .isInstanceOf(CourseNotFoundException.class);

            verifyNoInteractions(getUserProfileUseCase, autoReservationPort);
            verify(enrollmentRepositoryPort, never()).save(any(Enrollment.class));
        }
    }
}
