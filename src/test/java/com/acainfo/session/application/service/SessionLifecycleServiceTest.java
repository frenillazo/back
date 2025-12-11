package com.acainfo.session.application.service;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.session.application.dto.PostponeSessionCommand;
import com.acainfo.session.application.port.in.GetSessionUseCase;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.exception.InvalidSessionStateException;
import com.acainfo.session.domain.exception.SessionNotFoundException;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.shared.factory.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SessionLifecycleService with Mockito.
 * Tests lifecycle transitions: start, complete, cancel, postpone.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionLifecycleService Tests")
class SessionLifecycleServiceTest {

    @Mock
    private SessionRepositoryPort sessionRepositoryPort;

    @Mock
    private GetSessionUseCase getSessionUseCase;

    @InjectMocks
    private SessionLifecycleService sessionLifecycleService;

    private Session scheduledSession;
    private Session inProgressSession;
    private Session completedSession;
    private Session cancelledSession;
    private Session postponedSession;

    @BeforeEach
    void setUp() {
        scheduledSession = SessionFactory.scheduledRegular();
        inProgressSession = SessionFactory.inProgressSession();
        completedSession = SessionFactory.completedSession();
        cancelledSession = SessionFactory.cancelledSession();
        postponedSession = SessionFactory.postponedSession();
    }

    @Nested
    @DisplayName("Start Session Tests")
    class StartSessionTests {

        @Test
        @DisplayName("Should start scheduled session successfully")
        void start_WhenSessionIsScheduled_StartsSuccessfully() {
            // Given
            Long sessionId = 1L;
            when(getSessionUseCase.getById(sessionId)).thenReturn(scheduledSession);
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Session result = sessionLifecycleService.start(sessionId);

            // Then
            assertThat(result.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
            assertThat(result.isInProgress()).isTrue();
            verify(sessionRepositoryPort).save(any(Session.class));
        }

        @Test
        @DisplayName("Should throw exception when starting IN_PROGRESS session")
        void start_WhenSessionInProgress_ThrowsException() {
            // Given
            Long sessionId = 1L;
            when(getSessionUseCase.getById(sessionId)).thenReturn(inProgressSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.start(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be started")
                    .hasMessageContaining("IN_PROGRESS");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when starting COMPLETED session")
        void start_WhenSessionCompleted_ThrowsException() {
            // Given
            Long sessionId = 1L;
            when(getSessionUseCase.getById(sessionId)).thenReturn(completedSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.start(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be started");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when starting CANCELLED session")
        void start_WhenSessionCancelled_ThrowsException() {
            // Given
            Long sessionId = 1L;
            when(getSessionUseCase.getById(sessionId)).thenReturn(cancelledSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.start(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be started");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when starting POSTPONED session")
        void start_WhenSessionPostponed_ThrowsException() {
            // Given
            Long sessionId = 1L;
            when(getSessionUseCase.getById(sessionId)).thenReturn(postponedSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.start(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be started");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when session not found")
        void start_WhenSessionNotFound_ThrowsException() {
            // Given
            Long sessionId = 999L;
            when(getSessionUseCase.getById(sessionId)).thenThrow(new SessionNotFoundException(sessionId));

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.start(sessionId))
                    .isInstanceOf(SessionNotFoundException.class);

            verify(sessionRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Complete Session Tests")
    class CompleteSessionTests {

        @Test
        @DisplayName("Should complete IN_PROGRESS session successfully")
        void complete_WhenSessionInProgress_CompletesSuccessfully() {
            // Given
            Long sessionId = 1L;
            when(getSessionUseCase.getById(sessionId)).thenReturn(inProgressSession);
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Session result = sessionLifecycleService.complete(sessionId);

            // Then
            assertThat(result.getStatus()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(result.isCompleted()).isTrue();
            verify(sessionRepositoryPort).save(any(Session.class));
        }

        @Test
        @DisplayName("Should throw exception when completing SCHEDULED session")
        void complete_WhenSessionScheduled_ThrowsException() {
            // Given
            Long sessionId = 1L;
            when(getSessionUseCase.getById(sessionId)).thenReturn(scheduledSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.complete(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only IN_PROGRESS sessions can be completed")
                    .hasMessageContaining("SCHEDULED");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when completing already COMPLETED session")
        void complete_WhenSessionAlreadyCompleted_ThrowsException() {
            // Given
            Long sessionId = 1L;
            when(getSessionUseCase.getById(sessionId)).thenReturn(completedSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.complete(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only IN_PROGRESS sessions can be completed");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when completing CANCELLED session")
        void complete_WhenSessionCancelled_ThrowsException() {
            // Given
            Long sessionId = 1L;
            when(getSessionUseCase.getById(sessionId)).thenReturn(cancelledSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.complete(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only IN_PROGRESS sessions can be completed");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when session not found")
        void complete_WhenSessionNotFound_ThrowsException() {
            // Given
            Long sessionId = 999L;
            when(getSessionUseCase.getById(sessionId)).thenThrow(new SessionNotFoundException(sessionId));

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.complete(sessionId))
                    .isInstanceOf(SessionNotFoundException.class);

            verify(sessionRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cancel Session Tests")
    class CancelSessionTests {

        @Test
        @DisplayName("Should cancel SCHEDULED session successfully")
        void cancel_WhenSessionScheduled_CancelsSuccessfully() {
            // Given
            Long sessionId = 1L;
            when(getSessionUseCase.getById(sessionId)).thenReturn(scheduledSession);
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Session result = sessionLifecycleService.cancel(sessionId);

            // Then
            assertThat(result.getStatus()).isEqualTo(SessionStatus.CANCELLED);
            assertThat(result.isCancelled()).isTrue();
            verify(sessionRepositoryPort).save(any(Session.class));
        }

        @Test
        @DisplayName("Should throw exception when cancelling IN_PROGRESS session")
        void cancel_WhenSessionInProgress_ThrowsException() {
            // Given
            Long sessionId = 1L;
            when(getSessionUseCase.getById(sessionId)).thenReturn(inProgressSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.cancel(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be cancelled")
                    .hasMessageContaining("IN_PROGRESS");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when cancelling COMPLETED session")
        void cancel_WhenSessionCompleted_ThrowsException() {
            // Given
            Long sessionId = 1L;
            when(getSessionUseCase.getById(sessionId)).thenReturn(completedSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.cancel(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be cancelled");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when cancelling already CANCELLED session")
        void cancel_WhenSessionAlreadyCancelled_ThrowsException() {
            // Given
            Long sessionId = 1L;
            when(getSessionUseCase.getById(sessionId)).thenReturn(cancelledSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.cancel(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be cancelled");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when session not found")
        void cancel_WhenSessionNotFound_ThrowsException() {
            // Given
            Long sessionId = 999L;
            when(getSessionUseCase.getById(sessionId)).thenThrow(new SessionNotFoundException(sessionId));

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.cancel(sessionId))
                    .isInstanceOf(SessionNotFoundException.class);

            verify(sessionRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Postpone Session Tests")
    class PostponeSessionTests {

        @Test
        @DisplayName("Should postpone SCHEDULED session successfully")
        void postpone_WhenSessionScheduled_PostponesSuccessfully() {
            // Given
            Long sessionId = 1L;
            LocalDate newDate = LocalDate.now().plusDays(7);
            PostponeSessionCommand command = new PostponeSessionCommand(
                    newDate,
                    LocalTime.of(14, 0),
                    LocalTime.of(16, 0),
                    Classroom.AULA_PORTAL2,
                    SessionMode.DUAL
            );

            when(getSessionUseCase.getById(sessionId)).thenReturn(scheduledSession);
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> {
                Session s = invocation.getArgument(0);
                if (s.getStatus() == SessionStatus.POSTPONED) {
                    return s; // Original session marked as postponed
                }
                return s.toBuilder().id(2L).build(); // New session with new ID
            });

            // When
            Session result = sessionLifecycleService.postpone(sessionId, command);

            // Then
            assertThat(result.getId()).isEqualTo(2L); // New session
            assertThat(result.getDate()).isEqualTo(newDate);
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(14, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalTime.of(16, 0));
            assertThat(result.getClassroom()).isEqualTo(Classroom.AULA_PORTAL2);
            assertThat(result.getMode()).isEqualTo(SessionMode.DUAL);
            assertThat(result.getStatus()).isEqualTo(SessionStatus.SCHEDULED);

            // Verify both original (marked as postponed) and new session are saved
            verify(sessionRepositoryPort, times(2)).save(any(Session.class));
        }

        @Test
        @DisplayName("Should postpone session keeping original properties when command has nulls")
        void postpone_WithNullCommandProperties_KeepsOriginalProperties() {
            // Given
            Long sessionId = 1L;
            LocalDate newDate = LocalDate.now().plusDays(7);
            PostponeSessionCommand command = new PostponeSessionCommand(
                    newDate,
                    null, // Keep original start time
                    null, // Keep original end time
                    null, // Keep original classroom
                    null  // Keep original mode
            );

            when(getSessionUseCase.getById(sessionId)).thenReturn(scheduledSession);
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> {
                Session s = invocation.getArgument(0);
                if (s.getStatus() == SessionStatus.POSTPONED) {
                    return s;
                }
                return s.toBuilder().id(2L).build();
            });

            // When
            Session result = sessionLifecycleService.postpone(sessionId, command);

            // Then
            assertThat(result.getDate()).isEqualTo(newDate);
            assertThat(result.getStartTime()).isEqualTo(scheduledSession.getStartTime());
            assertThat(result.getEndTime()).isEqualTo(scheduledSession.getEndTime());
            assertThat(result.getClassroom()).isEqualTo(scheduledSession.getClassroom());
            assertThat(result.getMode()).isEqualTo(scheduledSession.getMode());
        }

        @Test
        @DisplayName("Should mark original session as POSTPONED with new date reference")
        void postpone_MarksOriginalSessionAsPostponed() {
            // Given
            Long sessionId = 1L;
            LocalDate newDate = LocalDate.now().plusDays(7);
            PostponeSessionCommand command = new PostponeSessionCommand(
                    newDate, null, null, null, null
            );

            Session capturedOriginalSession = null;
            when(getSessionUseCase.getById(sessionId)).thenReturn(scheduledSession);
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> {
                Session s = invocation.getArgument(0);
                if (s.getStatus() == SessionStatus.POSTPONED) {
                    // Verify original session is marked correctly
                    assertThat(s.isPostponed()).isTrue();
                    assertThat(s.getPostponedToDate()).isEqualTo(newDate);
                    return s;
                }
                return s.toBuilder().id(2L).build();
            });

            // When
            sessionLifecycleService.postpone(sessionId, command);

            // Then - verified in the answer above
            verify(sessionRepositoryPort, times(2)).save(any(Session.class));
        }

        @Test
        @DisplayName("Should throw exception when postponing IN_PROGRESS session")
        void postpone_WhenSessionInProgress_ThrowsException() {
            // Given
            Long sessionId = 1L;
            PostponeSessionCommand command = new PostponeSessionCommand(
                    LocalDate.now().plusDays(7), null, null, null, null
            );

            when(getSessionUseCase.getById(sessionId)).thenReturn(inProgressSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.postpone(sessionId, command))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be postponed")
                    .hasMessageContaining("IN_PROGRESS");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when postponing COMPLETED session")
        void postpone_WhenSessionCompleted_ThrowsException() {
            // Given
            Long sessionId = 1L;
            PostponeSessionCommand command = new PostponeSessionCommand(
                    LocalDate.now().plusDays(7), null, null, null, null
            );

            when(getSessionUseCase.getById(sessionId)).thenReturn(completedSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.postpone(sessionId, command))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be postponed");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when postponing CANCELLED session")
        void postpone_WhenSessionCancelled_ThrowsException() {
            // Given
            Long sessionId = 1L;
            PostponeSessionCommand command = new PostponeSessionCommand(
                    LocalDate.now().plusDays(7), null, null, null, null
            );

            when(getSessionUseCase.getById(sessionId)).thenReturn(cancelledSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.postpone(sessionId, command))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be postponed");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when postponing already POSTPONED session")
        void postpone_WhenSessionAlreadyPostponed_ThrowsException() {
            // Given
            Long sessionId = 1L;
            PostponeSessionCommand command = new PostponeSessionCommand(
                    LocalDate.now().plusDays(7), null, null, null, null
            );

            when(getSessionUseCase.getById(sessionId)).thenReturn(postponedSession);

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.postpone(sessionId, command))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be postponed");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when session not found")
        void postpone_WhenSessionNotFound_ThrowsException() {
            // Given
            Long sessionId = 999L;
            PostponeSessionCommand command = new PostponeSessionCommand(
                    LocalDate.now().plusDays(7), null, null, null, null
            );

            when(getSessionUseCase.getById(sessionId)).thenThrow(new SessionNotFoundException(sessionId));

            // When & Then
            assertThatThrownBy(() -> sessionLifecycleService.postpone(sessionId, command))
                    .isInstanceOf(SessionNotFoundException.class);

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should preserve subject, group and schedule references in new session")
        void postpone_PreservesReferencesInNewSession() {
            // Given
            Long sessionId = 1L;
            LocalDate newDate = LocalDate.now().plusDays(7);
            PostponeSessionCommand command = new PostponeSessionCommand(
                    newDate, null, null, null, null
            );

            when(getSessionUseCase.getById(sessionId)).thenReturn(scheduledSession);
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> {
                Session s = invocation.getArgument(0);
                if (s.getStatus() != SessionStatus.POSTPONED) {
                    // Verify new session keeps references
                    assertThat(s.getSubjectId()).isEqualTo(scheduledSession.getSubjectId());
                    assertThat(s.getGroupId()).isEqualTo(scheduledSession.getGroupId());
                    assertThat(s.getScheduleId()).isEqualTo(scheduledSession.getScheduleId());
                    assertThat(s.getType()).isEqualTo(scheduledSession.getType());
                }
                return s.toBuilder().id(s.getStatus() == SessionStatus.POSTPONED ? 1L : 2L).build();
            });

            // When
            Session result = sessionLifecycleService.postpone(sessionId, command);

            // Then
            assertThat(result.getSubjectId()).isEqualTo(scheduledSession.getSubjectId());
            assertThat(result.getGroupId()).isEqualTo(scheduledSession.getGroupId());
            assertThat(result.getScheduleId()).isEqualTo(scheduledSession.getScheduleId());
            assertThat(result.getType()).isEqualTo(scheduledSession.getType());
        }
    }

    @Nested
    @DisplayName("State Machine Validation Tests")
    class StateMachineTests {

        @Test
        @DisplayName("Full lifecycle: SCHEDULED -> IN_PROGRESS -> COMPLETED")
        void fullLifecycle_ScheduledToCompleted() {
            // Given
            Long sessionId = 1L;
            Session session = SessionFactory.scheduledRegular();

            // Start: SCHEDULED -> IN_PROGRESS
            when(getSessionUseCase.getById(sessionId)).thenReturn(session);
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Session startedSession = sessionLifecycleService.start(sessionId);
            assertThat(startedSession.isInProgress()).isTrue();

            // Complete: IN_PROGRESS -> COMPLETED
            when(getSessionUseCase.getById(sessionId)).thenReturn(startedSession);

            Session completedSession = sessionLifecycleService.complete(sessionId);
            assertThat(completedSession.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("Cancel lifecycle: SCHEDULED -> CANCELLED")
        void cancelLifecycle_ScheduledToCancelled() {
            // Given
            Long sessionId = 1L;
            Session session = SessionFactory.scheduledRegular();

            when(getSessionUseCase.getById(sessionId)).thenReturn(session);
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Session cancelledSession = sessionLifecycleService.cancel(sessionId);

            // Then
            assertThat(cancelledSession.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("Postpone lifecycle: SCHEDULED -> POSTPONED + new SCHEDULED")
        void postponeLifecycle_ScheduledToPostponedAndNewScheduled() {
            // Given
            Long sessionId = 1L;
            LocalDate newDate = LocalDate.now().plusDays(7);
            Session session = SessionFactory.scheduledRegular();
            PostponeSessionCommand command = new PostponeSessionCommand(
                    newDate, null, null, null, null
            );

            when(getSessionUseCase.getById(sessionId)).thenReturn(session);
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> {
                Session s = invocation.getArgument(0);
                return s.toBuilder().id(s.getStatus() == SessionStatus.POSTPONED ? 1L : 2L).build();
            });

            // When
            Session newSession = sessionLifecycleService.postpone(sessionId, command);

            // Then
            assertThat(newSession.isScheduled()).isTrue();
            assertThat(newSession.getDate()).isEqualTo(newDate);
            verify(sessionRepositoryPort, times(2)).save(any(Session.class));
        }
    }
}
