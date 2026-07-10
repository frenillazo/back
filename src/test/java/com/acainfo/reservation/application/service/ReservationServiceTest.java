package com.acainfo.reservation.application.service;

import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.course.domain.model.Course;
import com.acainfo.reservation.application.dto.CreateReservationCommand;
import com.acainfo.reservation.application.dto.SwitchSessionCommand;
import com.acainfo.reservation.application.port.out.ReservationRepositoryPort;
import com.acainfo.reservation.domain.exception.CrossGroupReservationNotAllowedException;
import com.acainfo.reservation.domain.exception.InvalidReservationStateException;
import com.acainfo.reservation.domain.exception.ReservationAlreadyExistsException;
import com.acainfo.reservation.domain.exception.ReservationNotFoundException;
import com.acainfo.reservation.domain.exception.SessionFullException;
import com.acainfo.reservation.domain.exception.SubjectReservationAlreadyExistsException;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.exception.SessionNotFoundException;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link ReservationService}.
 *
 * <p>These tests pin down the CURRENT behaviour of the service before the
 * group/course unification migration. Deliberate quirks of the existing code
 * are tested as-is (see test names mentioning "even when" / "not check").</p>
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final Long STUDENT_ID = 10L;
    private static final Long OTHER_STUDENT_ID = 11L;
    private static final Long SESSION_ID = 100L;
    private static final Long NEW_SESSION_ID = 101L;
    private static final Long ENROLLMENT_ID = 1000L;
    private static final Long GROUP_ID = 50L;
    private static final Long SUBJECT_ID = 5L;
    private static final Long OTHER_SUBJECT_ID = 6L;
    private static final Long RESERVATION_ID = 7000L;
    private static final int MAX_IN_PERSON_CAPACITY = 24;

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    @Mock
    private SessionRepositoryPort sessionRepositoryPort;

    @Mock
    private EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Mock
    private CourseRepositoryPort courseRepositoryPort;

    @InjectMocks
    private ReservationService reservationService;

    // ==================== Test Data Builders ====================

    private Session aSession(Long id, Long subjectId, LocalDate date) {
        return Session.builder()
                .id(id)
                .subjectId(subjectId)
                .courseId(GROUP_ID)
                .date(date)
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(18, 0))
                .status(SessionStatus.SCHEDULED)
                .type(SessionType.REGULAR)
                .mode(SessionMode.DUAL)
                .build();
    }

    private Session aFutureSession() {
        return aSession(SESSION_ID, SUBJECT_ID, LocalDate.now().plusDays(7));
    }

    private Enrollment anEnrollment(EnrollmentStatus status) {
        return Enrollment.builder()
                .id(ENROLLMENT_ID)
                .studentId(STUDENT_ID)
                .courseId(GROUP_ID)
                .status(status)
                .enrolledAt(LocalDateTime.now().minusMonths(1))
                .build();
    }

    private Course aGroup(Long subjectId) {
        return Course.builder()
                .id(GROUP_ID)
                .subjectId(subjectId)
                .build();
    }

    private SessionReservation aReservation(ReservationStatus status, ReservationMode mode) {
        return SessionReservation.builder()
                .id(RESERVATION_ID)
                .studentId(STUDENT_ID)
                .sessionId(SESSION_ID)
                .enrollmentId(ENROLLMENT_ID)
                .mode(mode)
                .status(status)
                .reservedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    private void stubSaveEchoesArgument() {
        when(reservationRepositoryPort.save(any(SessionReservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ==================== create() ====================

    @Nested
    class CreateReservation {

        private CreateReservationCommand command(ReservationMode mode) {
            return new CreateReservationCommand(STUDENT_ID, SESSION_ID, ENROLLMENT_ID, mode);
        }

        @Test
        void shouldCreateConfirmedInPersonReservationWhenAllValidationsPass() {
            when(sessionRepositoryPort.findById(SESSION_ID)).thenReturn(Optional.of(aFutureSession()));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, SESSION_ID)).thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(SUBJECT_ID)));
            when(reservationRepositoryPort.existsConfirmedByStudentIdAndSubjectId(STUDENT_ID, SUBJECT_ID))
                    .thenReturn(false);
            when(reservationRepositoryPort.countInPersonReservations(SESSION_ID)).thenReturn(0L);
            stubSaveEchoesArgument();

            SessionReservation result = reservationService.create(command(ReservationMode.IN_PERSON));

            assertThat(result.getStudentId()).isEqualTo(STUDENT_ID);
            assertThat(result.getSessionId()).isEqualTo(SESSION_ID);
            assertThat(result.getEnrollmentId()).isEqualTo(ENROLLMENT_ID);
            assertThat(result.getMode()).isEqualTo(ReservationMode.IN_PERSON);
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            assertThat(result.getReservedAt()).isNotNull();
            assertThat(result.getCancelledAt()).isNull();

            ArgumentCaptor<SessionReservation> captor = ArgumentCaptor.forClass(SessionReservation.class);
            verify(reservationRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        }

        @Test
        void shouldSkipInPersonCapacityCheckWhenModeIsOnline() {
            when(sessionRepositoryPort.findById(SESSION_ID)).thenReturn(Optional.of(aFutureSession()));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, SESSION_ID)).thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(SUBJECT_ID)));
            when(reservationRepositoryPort.existsConfirmedByStudentIdAndSubjectId(STUDENT_ID, SUBJECT_ID))
                    .thenReturn(false);
            stubSaveEchoesArgument();

            SessionReservation result = reservationService.create(command(ReservationMode.ONLINE));

            assertThat(result.getMode()).isEqualTo(ReservationMode.ONLINE);
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            verify(reservationRepositoryPort, never()).countInPersonReservations(anyLong());
            verify(reservationRepositoryPort, never())
                    .countBySessionIdAndStatusAndMode(anyLong(), any(), any());
        }

        @Test
        void shouldAllowInPersonReservationWhenCountIsJustBelowCapacity() {
            when(sessionRepositoryPort.findById(SESSION_ID)).thenReturn(Optional.of(aFutureSession()));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, SESSION_ID)).thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(SUBJECT_ID)));
            when(reservationRepositoryPort.existsConfirmedByStudentIdAndSubjectId(STUDENT_ID, SUBJECT_ID))
                    .thenReturn(false);
            when(reservationRepositoryPort.countInPersonReservations(SESSION_ID))
                    .thenReturn((long) MAX_IN_PERSON_CAPACITY - 1);
            stubSaveEchoesArgument();

            SessionReservation result = reservationService.create(command(ReservationMode.IN_PERSON));

            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            verify(reservationRepositoryPort).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowSessionFullWhenInPersonCapacityReached() {
            // Current behaviour: hard failure, NO automatic fallback to ONLINE mode.
            when(sessionRepositoryPort.findById(SESSION_ID)).thenReturn(Optional.of(aFutureSession()));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, SESSION_ID)).thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(SUBJECT_ID)));
            when(reservationRepositoryPort.existsConfirmedByStudentIdAndSubjectId(STUDENT_ID, SUBJECT_ID))
                    .thenReturn(false);
            when(reservationRepositoryPort.countInPersonReservations(SESSION_ID))
                    .thenReturn((long) MAX_IN_PERSON_CAPACITY);

            assertThatThrownBy(() -> reservationService.create(command(ReservationMode.IN_PERSON)))
                    .isInstanceOf(SessionFullException.class)
                    .hasMessageContaining("capacity of " + MAX_IN_PERSON_CAPACITY);

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowSessionNotFoundWhenSessionDoesNotExist() {
            when(sessionRepositoryPort.findById(SESSION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.create(command(ReservationMode.IN_PERSON)))
                    .isInstanceOf(SessionNotFoundException.class)
                    .hasMessageContaining(SESSION_ID.toString());

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowReservationAlreadyExistsWhenStudentAlreadyReservedSameSession() {
            when(sessionRepositoryPort.findById(SESSION_ID)).thenReturn(Optional.of(aFutureSession()));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, SESSION_ID)).thenReturn(true);

            assertThatThrownBy(() -> reservationService.create(command(ReservationMode.IN_PERSON)))
                    .isInstanceOf(ReservationAlreadyExistsException.class)
                    .hasMessageContaining("already has a reservation for session " + SESSION_ID);

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowInvalidReservationStateWhenEnrollmentDoesNotExist() {
            when(sessionRepositoryPort.findById(SESSION_ID)).thenReturn(Optional.of(aFutureSession()));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, SESSION_ID)).thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.create(command(ReservationMode.IN_PERSON)))
                    .isInstanceOf(InvalidReservationStateException.class)
                    .hasMessageContaining("Enrollment not found: " + ENROLLMENT_ID);

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowInvalidReservationStateWhenEnrollmentGroupDoesNotExist() {
            when(sessionRepositoryPort.findById(SESSION_ID)).thenReturn(Optional.of(aFutureSession()));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, SESSION_ID)).thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.create(command(ReservationMode.IN_PERSON)))
                    .isInstanceOf(InvalidReservationStateException.class)
                    .hasMessageContaining("Course not found: " + GROUP_ID);

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowCrossGroupReservationNotAllowedWhenSessionSubjectDiffersFromEnrollmentSubject() {
            when(sessionRepositoryPort.findById(SESSION_ID)).thenReturn(Optional.of(aFutureSession()));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, SESSION_ID)).thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            // Enrollment's group belongs to a DIFFERENT subject than the session
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(OTHER_SUBJECT_ID)));

            assertThatThrownBy(() -> reservationService.create(command(ReservationMode.IN_PERSON)))
                    .isInstanceOf(CrossGroupReservationNotAllowedException.class)
                    .hasMessageContaining("different subject");

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowSubjectReservationAlreadyExistsWhenStudentHasConfirmedReservationForSameSubject() {
            // Subject-level uniqueness: one CONFIRMED reservation per subject, checked
            // against the SESSION's subjectId (any session/date of that subject blocks).
            when(sessionRepositoryPort.findById(SESSION_ID)).thenReturn(Optional.of(aFutureSession()));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, SESSION_ID)).thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(SUBJECT_ID)));
            when(reservationRepositoryPort.existsConfirmedByStudentIdAndSubjectId(STUDENT_ID, SUBJECT_ID))
                    .thenReturn(true);

            assertThatThrownBy(() -> reservationService.create(command(ReservationMode.IN_PERSON)))
                    .isInstanceOf(SubjectReservationAlreadyExistsException.class)
                    .hasMessageContaining("already has a confirmed reservation for subject " + SUBJECT_ID);

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldCreateReservationEvenWhenEnrollmentIsWithdrawn() {
            // QUIRK: the service never validates the enrollment status.
            // A WITHDRAWN (or any non-active) enrollment can still create reservations.
            when(sessionRepositoryPort.findById(SESSION_ID)).thenReturn(Optional.of(aFutureSession()));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, SESSION_ID)).thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.WITHDRAWN)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(SUBJECT_ID)));
            when(reservationRepositoryPort.existsConfirmedByStudentIdAndSubjectId(STUDENT_ID, SUBJECT_ID))
                    .thenReturn(false);
            when(reservationRepositoryPort.countInPersonReservations(SESSION_ID)).thenReturn(0L);
            stubSaveEchoesArgument();

            SessionReservation result = reservationService.create(command(ReservationMode.IN_PERSON));

            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            verify(reservationRepositoryPort).save(any(SessionReservation.class));
        }

        @Test
        void shouldCreateReservationEvenWhenSessionIsInThePast() {
            // QUIRK: the service never checks the session date/status.
            // Reservations for past sessions are accepted.
            Session pastSession = aSession(SESSION_ID, SUBJECT_ID, LocalDate.now().minusDays(30));
            when(sessionRepositoryPort.findById(SESSION_ID)).thenReturn(Optional.of(pastSession));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, SESSION_ID)).thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(SUBJECT_ID)));
            when(reservationRepositoryPort.existsConfirmedByStudentIdAndSubjectId(STUDENT_ID, SUBJECT_ID))
                    .thenReturn(false);
            when(reservationRepositoryPort.countInPersonReservations(SESSION_ID)).thenReturn(0L);
            stubSaveEchoesArgument();

            SessionReservation result = reservationService.create(command(ReservationMode.IN_PERSON));

            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            verify(reservationRepositoryPort).save(any(SessionReservation.class));
        }
    }

    // ==================== cancel() ====================

    @Nested
    class CancelReservation {

        @Test
        void shouldCancelConfirmedReservationWhenOwnedByStudent() {
            SessionReservation reservation = aReservation(ReservationStatus.CONFIRMED, ReservationMode.IN_PERSON);
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));
            stubSaveEchoesArgument();

            SessionReservation result = reservationService.cancel(RESERVATION_ID, STUDENT_ID);

            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
            assertThat(result.getCancelledAt()).isNotNull();
            verify(reservationRepositoryPort).save(reservation);
        }

        @Test
        void shouldThrowReservationNotFoundWhenReservationDoesNotExist() {
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.cancel(RESERVATION_ID, STUDENT_ID))
                    .isInstanceOf(ReservationNotFoundException.class)
                    .hasMessageContaining(RESERVATION_ID.toString());

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowInvalidReservationStateWhenReservationBelongsToAnotherStudent() {
            SessionReservation reservation = aReservation(ReservationStatus.CONFIRMED, ReservationMode.IN_PERSON);
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));

            assertThatThrownBy(() -> reservationService.cancel(RESERVATION_ID, OTHER_STUDENT_ID))
                    .isInstanceOf(InvalidReservationStateException.class)
                    .hasMessageContaining("does not belong to student " + OTHER_STUDENT_ID);

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowInvalidReservationStateWhenReservationAlreadyCancelled() {
            SessionReservation reservation = aReservation(ReservationStatus.CANCELLED, ReservationMode.IN_PERSON);
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));

            assertThatThrownBy(() -> reservationService.cancel(RESERVATION_ID, STUDENT_ID))
                    .isInstanceOf(InvalidReservationStateException.class)
                    .hasMessageContaining("Cannot cancel reservation " + RESERVATION_ID)
                    .hasMessageContaining("CANCELLED");

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

    }

    // ==================== switchSession() ====================

    @Nested
    class SwitchSession {

        private final SwitchSessionCommand command =
                new SwitchSessionCommand(STUDENT_ID, RESERVATION_ID, NEW_SESSION_ID);

        private Session aNewSession(Long subjectId) {
            return aSession(NEW_SESSION_ID, subjectId, LocalDate.now().plusDays(8));
        }

        @Test
        void shouldCancelCurrentAndCreateNewConfirmedReservationWhenSwitchingSession() {
            SessionReservation current = aReservation(ReservationStatus.CONFIRMED, ReservationMode.IN_PERSON);
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.of(current));
            when(sessionRepositoryPort.findById(NEW_SESSION_ID)).thenReturn(Optional.of(aNewSession(SUBJECT_ID)));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, NEW_SESSION_ID))
                    .thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(SUBJECT_ID)));
            when(reservationRepositoryPort.countInPersonReservations(NEW_SESSION_ID)).thenReturn(0L);
            stubSaveEchoesArgument();

            SessionReservation result = reservationService.switchSession(command);

            ArgumentCaptor<SessionReservation> captor = ArgumentCaptor.forClass(SessionReservation.class);
            verify(reservationRepositoryPort, times(2)).save(captor.capture());
            List<SessionReservation> saved = captor.getAllValues();

            // First save: the old reservation, now cancelled
            SessionReservation cancelledOld = saved.get(0);
            assertThat(cancelledOld.getId()).isEqualTo(RESERVATION_ID);
            assertThat(cancelledOld.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
            assertThat(cancelledOld.getCancelledAt()).isNotNull();

            // Second save: the new reservation, confirmed, same enrollment and mode
            SessionReservation createdNew = saved.get(1);
            assertThat(createdNew.getSessionId()).isEqualTo(NEW_SESSION_ID);
            assertThat(createdNew.getStudentId()).isEqualTo(STUDENT_ID);
            assertThat(createdNew.getEnrollmentId()).isEqualTo(ENROLLMENT_ID);
            assertThat(createdNew.getMode()).isEqualTo(ReservationMode.IN_PERSON);
            assertThat(createdNew.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            assertThat(createdNew.getReservedAt()).isNotNull();

            assertThat(result).isSameAs(createdNew);
        }

        @Test
        void shouldNotCheckSubjectLevelUniquenessWhenSwitchingSession() {
            // QUIRK: unlike create(), switchSession never calls
            // existsConfirmedByStudentIdAndSubjectId.
            SessionReservation current = aReservation(ReservationStatus.CONFIRMED, ReservationMode.ONLINE);
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.of(current));
            when(sessionRepositoryPort.findById(NEW_SESSION_ID)).thenReturn(Optional.of(aNewSession(SUBJECT_ID)));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, NEW_SESSION_ID))
                    .thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(SUBJECT_ID)));
            stubSaveEchoesArgument();

            reservationService.switchSession(command);

            verify(reservationRepositoryPort, never())
                    .existsConfirmedByStudentIdAndSubjectId(anyLong(), anyLong());
        }

        @Test
        void shouldSkipCapacityCheckWhenCurrentReservationIsOnline() {
            SessionReservation current = aReservation(ReservationStatus.CONFIRMED, ReservationMode.ONLINE);
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.of(current));
            when(sessionRepositoryPort.findById(NEW_SESSION_ID)).thenReturn(Optional.of(aNewSession(SUBJECT_ID)));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, NEW_SESSION_ID))
                    .thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(SUBJECT_ID)));
            stubSaveEchoesArgument();

            SessionReservation result = reservationService.switchSession(command);

            assertThat(result.getMode()).isEqualTo(ReservationMode.ONLINE);
            verify(reservationRepositoryPort, never()).countInPersonReservations(anyLong());
            verify(reservationRepositoryPort, never())
                    .countBySessionIdAndStatusAndMode(anyLong(), any(), any());
        }

        @Test
        void shouldAllowSwitchingFromAlreadyCancelledReservation() {
            // QUIRK: switchSession never checks the current reservation status.
            // A CANCELLED reservation can be "switched": it is re-cancelled (cancelledAt
            // overwritten) and a brand new CONFIRMED reservation is created.
            SessionReservation current = aReservation(ReservationStatus.CANCELLED, ReservationMode.IN_PERSON);
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.of(current));
            when(sessionRepositoryPort.findById(NEW_SESSION_ID)).thenReturn(Optional.of(aNewSession(SUBJECT_ID)));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, NEW_SESSION_ID))
                    .thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(SUBJECT_ID)));
            when(reservationRepositoryPort.countInPersonReservations(NEW_SESSION_ID)).thenReturn(0L);
            stubSaveEchoesArgument();

            SessionReservation result = reservationService.switchSession(command);

            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            assertThat(result.getSessionId()).isEqualTo(NEW_SESSION_ID);
            verify(reservationRepositoryPort, times(2)).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowReservationNotFoundWhenCurrentReservationDoesNotExist() {
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.switchSession(command))
                    .isInstanceOf(ReservationNotFoundException.class)
                    .hasMessageContaining(RESERVATION_ID.toString());

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowInvalidReservationStateWhenCurrentReservationBelongsToAnotherStudent() {
            SessionReservation current = aReservation(ReservationStatus.CONFIRMED, ReservationMode.IN_PERSON);
            SwitchSessionCommand otherStudentCommand =
                    new SwitchSessionCommand(OTHER_STUDENT_ID, RESERVATION_ID, NEW_SESSION_ID);
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.of(current));

            assertThatThrownBy(() -> reservationService.switchSession(otherStudentCommand))
                    .isInstanceOf(InvalidReservationStateException.class)
                    .hasMessageContaining("does not belong to student " + OTHER_STUDENT_ID);

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowSessionNotFoundWhenNewSessionDoesNotExist() {
            SessionReservation current = aReservation(ReservationStatus.CONFIRMED, ReservationMode.IN_PERSON);
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.of(current));
            when(sessionRepositoryPort.findById(NEW_SESSION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.switchSession(command))
                    .isInstanceOf(SessionNotFoundException.class)
                    .hasMessageContaining(NEW_SESSION_ID.toString());

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowReservationAlreadyExistsWhenStudentAlreadyReservedNewSession() {
            SessionReservation current = aReservation(ReservationStatus.CONFIRMED, ReservationMode.IN_PERSON);
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.of(current));
            when(sessionRepositoryPort.findById(NEW_SESSION_ID)).thenReturn(Optional.of(aNewSession(SUBJECT_ID)));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, NEW_SESSION_ID))
                    .thenReturn(true);

            assertThatThrownBy(() -> reservationService.switchSession(command))
                    .isInstanceOf(ReservationAlreadyExistsException.class)
                    .hasMessageContaining("already has a reservation for session " + NEW_SESSION_ID);

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowInvalidReservationStateWhenEnrollmentOfCurrentReservationDoesNotExist() {
            SessionReservation current = aReservation(ReservationStatus.CONFIRMED, ReservationMode.IN_PERSON);
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.of(current));
            when(sessionRepositoryPort.findById(NEW_SESSION_ID)).thenReturn(Optional.of(aNewSession(SUBJECT_ID)));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, NEW_SESSION_ID))
                    .thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.switchSession(command))
                    .isInstanceOf(InvalidReservationStateException.class)
                    .hasMessageContaining("Enrollment not found: " + ENROLLMENT_ID);

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowCrossGroupReservationNotAllowedWhenNewSessionSubjectDiffers() {
            SessionReservation current = aReservation(ReservationStatus.CONFIRMED, ReservationMode.IN_PERSON);
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.of(current));
            when(sessionRepositoryPort.findById(NEW_SESSION_ID))
                    .thenReturn(Optional.of(aNewSession(OTHER_SUBJECT_ID)));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, NEW_SESSION_ID))
                    .thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(SUBJECT_ID)));

            assertThatThrownBy(() -> reservationService.switchSession(command))
                    .isInstanceOf(CrossGroupReservationNotAllowedException.class)
                    .hasMessageContaining("different subject");

            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
        }

        @Test
        void shouldThrowSessionFullAndKeepCurrentReservationWhenNewSessionInPersonCapacityReached() {
            SessionReservation current = aReservation(ReservationStatus.CONFIRMED, ReservationMode.IN_PERSON);
            when(reservationRepositoryPort.findById(RESERVATION_ID)).thenReturn(Optional.of(current));
            when(sessionRepositoryPort.findById(NEW_SESSION_ID)).thenReturn(Optional.of(aNewSession(SUBJECT_ID)));
            when(reservationRepositoryPort.existsByStudentIdAndSessionId(STUDENT_ID, NEW_SESSION_ID))
                    .thenReturn(false);
            when(enrollmentRepositoryPort.findById(ENROLLMENT_ID))
                    .thenReturn(Optional.of(anEnrollment(EnrollmentStatus.ACTIVE)));
            when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.of(aGroup(SUBJECT_ID)));
            when(reservationRepositoryPort.countInPersonReservations(NEW_SESSION_ID))
                    .thenReturn((long) MAX_IN_PERSON_CAPACITY);

            assertThatThrownBy(() -> reservationService.switchSession(command))
                    .isInstanceOf(SessionFullException.class)
                    .hasMessageContaining("capacity of " + MAX_IN_PERSON_CAPACITY);

            // The current reservation is left untouched: no save, still CONFIRMED
            verify(reservationRepositoryPort, never()).save(any(SessionReservation.class));
            assertThat(current.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            assertThat(current.getCancelledAt()).isNull();
        }
    }
}
