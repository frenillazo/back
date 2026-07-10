package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.port.out.AutoReservationPort;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.course.domain.model.Course;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import com.acainfo.user.domain.model.Role;
import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for the WAITING LIST ENTRY path of
 * {@link EnrollmentApprovalService#approve(Long, Long)}.
 *
 * <p>Queue entry (position assignment via
 * {@code EnrollmentRepositoryPort.getNextWaitingListPosition}) does NOT live in
 * {@code WaitingListService}: it happens when a teacher approves an enrollment
 * for a group that is already full. This class captures exactly that behavior
 * before the group -> course migration; the rest of the approval workflow is
 * intentionally out of scope here.</p>
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentApprovalServiceWaitingListEntryTest {

    private static final Long GROUP_ID = 100L;
    private static final Long STUDENT_ID = 10L;
    private static final Long ENROLLMENT_ID = 1L;
    private static final Long TEACHER_ID = 2L;
    private static final int CAPACITY = 2;

    @Mock
    private EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Mock
    private CourseRepositoryPort courseRepositoryPort;

    @Mock
    private GetUserProfileUseCase getUserProfileUseCase;

    @Mock
    private AutoReservationPort autoReservationPort;

    @InjectMocks
    private EnrollmentApprovalService enrollmentApprovalService;

    private Enrollment pendingEnrollment;
    private Course group;
    private User teacher;

    @BeforeEach
    void setUp() {
        pendingEnrollment = Enrollment.builder()
                .id(ENROLLMENT_ID)
                .studentId(STUDENT_ID)
                .courseId(GROUP_ID)
                .status(EnrollmentStatus.PENDING_APPROVAL)
                .build();

        group = Course.builder()
                .id(GROUP_ID)
                .teacherId(TEACHER_ID)
                .capacity(CAPACITY)
                .build();

        teacher = User.builder()
                .id(TEACHER_ID)
                .email("teacher@acainfo.com")
                .roles(Set.of(Role.builder().type(RoleType.TEACHER).build()))
                .build();

        when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.of(pendingEnrollment));
        when(courseRepositoryPort.findByIdForUpdate(GROUP_ID)).thenReturn(Optional.of(group));
        when(getUserProfileUseCase.getUserById(TEACHER_ID)).thenReturn(teacher);
        when(enrollmentRepositoryPort.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldAssignNextWaitingListPositionWhenGroupIsFullOnApproval() {
        // Group full: active count equals max capacity
        when(enrollmentRepositoryPort.countActiveByCourseId(GROUP_ID)).thenReturn((long) CAPACITY);
        when(enrollmentRepositoryPort.getNextWaitingListPosition(GROUP_ID)).thenReturn(3);

        Enrollment result = enrollmentApprovalService.approve(ENROLLMENT_ID, TEACHER_ID);

        // Position on entering the queue comes straight from the repository counter
        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.WAITING_LIST);
        assertThat(result.getWaitingListPosition()).isEqualTo(3);
        // It still counts as an approval, even though the student ends up queued
        assertThat(result.getApprovedAt()).isNotNull();
        assertThat(result.getApprovedByUserId()).isEqualTo(TEACHER_ID);

        verify(enrollmentRepositoryPort).getNextWaitingListPosition(GROUP_ID);
        verify(enrollmentRepositoryPort).save(pendingEnrollment);
        // No reservations while waiting: only ACTIVE students get auto-reservations
        verifyNoInteractions(autoReservationPort);
    }

    @Test
    void shouldApproveAsActiveWithoutQueuePositionWhenSeatsAvailable() {
        // One seat free: active count strictly below max capacity
        when(enrollmentRepositoryPort.countActiveByCourseId(GROUP_ID)).thenReturn((long) CAPACITY - 1);

        Enrollment result = enrollmentApprovalService.approve(ENROLLMENT_ID, TEACHER_ID);

        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(result.getWaitingListPosition()).isNull();
        assertThat(result.getApprovedAt()).isNotNull();
        assertThat(result.getApprovedByUserId()).isEqualTo(TEACHER_ID);

        // The queue position counter is never consulted when a seat is free
        verify(enrollmentRepositoryPort, never()).getNextWaitingListPosition(anyLong());
        verify(autoReservationPort).generateForNewEnrollment(STUDENT_ID, GROUP_ID, ENROLLMENT_ID);
    }
}
