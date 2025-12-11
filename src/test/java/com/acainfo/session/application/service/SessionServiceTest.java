package com.acainfo.session.application.service;

import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.schedule.application.port.out.ScheduleRepositoryPort;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.session.application.dto.CreateSessionCommand;
import com.acainfo.session.application.dto.SessionFilters;
import com.acainfo.session.application.dto.UpdateSessionCommand;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.exception.InvalidSessionStateException;
import com.acainfo.session.domain.exception.SessionNotFoundException;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
import com.acainfo.shared.factory.GroupFactory;
import com.acainfo.shared.factory.ScheduleFactory;
import com.acainfo.shared.factory.SessionFactory;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.exception.SubjectNotFoundException;
import com.acainfo.subject.domain.model.Subject;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SessionService with Mockito.
 * Tests CRUD business logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionService Tests")
class SessionServiceTest {

    @Mock
    private SessionRepositoryPort sessionRepositoryPort;

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private SubjectRepositoryPort subjectRepositoryPort;

    @Mock
    private ScheduleRepositoryPort scheduleRepositoryPort;

    @InjectMocks
    private SessionService sessionService;

    private Session testSession;
    private SubjectGroup testGroup;
    private Schedule testSchedule;
    private Subject testSubject;

    @BeforeEach
    void setUp() {
        testSession = SessionFactory.defaultSession();
        testGroup = GroupFactory.defaultGroup();
        testSchedule = ScheduleFactory.defaultSchedule();
        testSubject = Subject.builder()
                .id(1L)
                .name("Test Subject")
                .build();
    }

    @Nested
    @DisplayName("Create Session Tests")
    class CreateSessionTests {

        @Test
        @DisplayName("Should create REGULAR session successfully")
        void create_RegularSession_CreatesSuccessfully() {
            // Given
            CreateSessionCommand command = CreateSessionCommand.forRegularSession(
                    1L, // groupId
                    1L, // scheduleId
                    Classroom.AULA_PORTAL1,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    SessionMode.IN_PERSON
            );

            when(scheduleRepositoryPort.findById(1L)).thenReturn(Optional.of(testSchedule));
            when(groupRepositoryPort.findById(testSchedule.getGroupId())).thenReturn(Optional.of(testGroup));
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> {
                Session s = invocation.getArgument(0);
                return s.toBuilder().id(1L).build();
            });

            // When
            Session result = sessionService.create(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getType()).isEqualTo(SessionType.REGULAR);
            assertThat(result.getStatus()).isEqualTo(SessionStatus.SCHEDULED);
            verify(sessionRepositoryPort).save(any(Session.class));
        }

        @Test
        @DisplayName("Should create EXTRA session successfully")
        void create_ExtraSession_CreatesSuccessfully() {
            // Given
            CreateSessionCommand command = CreateSessionCommand.forExtraSession(
                    1L, // groupId
                    Classroom.AULA_PORTAL2,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(14, 0),
                    LocalTime.of(16, 0),
                    SessionMode.DUAL
            );

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> {
                Session s = invocation.getArgument(0);
                return s.toBuilder().id(2L).build();
            });

            // When
            Session result = sessionService.create(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(SessionType.EXTRA);
            assertThat(result.getSubjectId()).isEqualTo(testGroup.getSubjectId());
            verify(sessionRepositoryPort).save(any(Session.class));
        }

        @Test
        @DisplayName("Should create SCHEDULING session successfully")
        void create_SchedulingSession_CreatesSuccessfully() {
            // Given
            CreateSessionCommand command = CreateSessionCommand.forSchedulingSession(
                    1L, // subjectId
                    LocalDate.now().plusDays(1),
                    LocalTime.of(18, 0),
                    LocalTime.of(20, 0)
            );

            when(subjectRepositoryPort.findById(1L)).thenReturn(Optional.of(testSubject));
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> {
                Session s = invocation.getArgument(0);
                return s.toBuilder().id(3L).build();
            });

            // When
            Session result = sessionService.create(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(SessionType.SCHEDULING);
            assertThat(result.getSubjectId()).isEqualTo(1L);
            assertThat(result.getGroupId()).isNull();
            verify(sessionRepositoryPort).save(any(Session.class));
        }

        @Test
        @DisplayName("Should throw exception when REGULAR session has no scheduleId")
        void create_RegularSessionWithoutScheduleId_ThrowsException() {
            // Given
            CreateSessionCommand command = new CreateSessionCommand(
                    SessionType.REGULAR,
                    null, // subjectId
                    1L,   // groupId
                    null, // scheduleId - MISSING
                    Classroom.AULA_PORTAL1,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    SessionMode.IN_PERSON
            );

            // When & Then
            assertThatThrownBy(() -> sessionService.create(command))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("REGULAR sessions require a scheduleId");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when EXTRA session has no groupId")
        void create_ExtraSessionWithoutGroupId_ThrowsException() {
            // Given
            CreateSessionCommand command = new CreateSessionCommand(
                    SessionType.EXTRA,
                    null, // subjectId
                    null, // groupId - MISSING
                    null, // scheduleId
                    Classroom.AULA_PORTAL1,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    SessionMode.IN_PERSON
            );

            // When & Then
            assertThatThrownBy(() -> sessionService.create(command))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("EXTRA sessions require a groupId");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when SCHEDULING session has no subjectId")
        void create_SchedulingSessionWithoutSubjectId_ThrowsException() {
            // Given
            CreateSessionCommand command = new CreateSessionCommand(
                    SessionType.SCHEDULING,
                    null, // subjectId - MISSING
                    null, // groupId
                    null, // scheduleId
                    Classroom.AULA_VIRTUAL,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    SessionMode.ONLINE
            );

            // When & Then
            assertThatThrownBy(() -> sessionService.create(command))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("SCHEDULING sessions require a subjectId");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when group not found for EXTRA session")
        void create_ExtraSessionWithNonExistentGroup_ThrowsException() {
            // Given
            CreateSessionCommand command = CreateSessionCommand.forExtraSession(
                    999L, // Non-existent groupId
                    Classroom.AULA_PORTAL1,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    SessionMode.IN_PERSON
            );

            when(groupRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> sessionService.create(command))
                    .isInstanceOf(GroupNotFoundException.class);

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when subject not found for SCHEDULING session")
        void create_SchedulingSessionWithNonExistentSubject_ThrowsException() {
            // Given
            CreateSessionCommand command = CreateSessionCommand.forSchedulingSession(
                    999L, // Non-existent subjectId
                    LocalDate.now().plusDays(1),
                    LocalTime.of(18, 0),
                    LocalTime.of(20, 0)
            );

            when(subjectRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> sessionService.create(command))
                    .isInstanceOf(SubjectNotFoundException.class);

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when schedule not found for REGULAR session")
        void create_RegularSessionWithNonExistentSchedule_ThrowsException() {
            // Given
            CreateSessionCommand command = CreateSessionCommand.forRegularSession(
                    1L,   // groupId
                    999L, // Non-existent scheduleId
                    Classroom.AULA_PORTAL1,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    SessionMode.IN_PERSON
            );

            when(scheduleRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> sessionService.create(command))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Schedule not found");

            verify(sessionRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Get Session Tests")
    class GetSessionTests {

        @Test
        @DisplayName("Should return session when found by ID")
        void getById_WhenSessionExists_ReturnsSession() {
            // Given
            Long sessionId = 1L;
            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(testSession));

            // When
            Session result = sessionService.getById(sessionId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testSession.getId());
            verify(sessionRepositoryPort).findById(sessionId);
        }

        @Test
        @DisplayName("Should throw SessionNotFoundException when session not found")
        void getById_WhenSessionNotFound_ThrowsException() {
            // Given
            Long sessionId = 999L;
            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> sessionService.getById(sessionId))
                    .isInstanceOf(SessionNotFoundException.class)
                    .hasMessageContaining("999");

            verify(sessionRepositoryPort).findById(sessionId);
        }

        @Test
        @DisplayName("Should return sessions with filters")
        void findWithFilters_ReturnsFilteredSessions() {
            // Given
            SessionFilters filters = SessionFilters.forGroup(1L);
            Page<Session> sessionsPage = new PageImpl<>(List.of(testSession));
            when(sessionRepositoryPort.findWithFilters(filters)).thenReturn(sessionsPage);

            // When
            Page<Session> result = sessionService.findWithFilters(filters);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(sessionRepositoryPort).findWithFilters(filters);
        }

        @Test
        @DisplayName("Should return sessions for group")
        void findByGroupId_ReturnsSessionsForGroup() {
            // Given
            Long groupId = 1L;
            List<Session> sessions = List.of(testSession);
            when(sessionRepositoryPort.findByGroupId(groupId)).thenReturn(sessions);

            // When
            List<Session> result = sessionService.findByGroupId(groupId);

            // Then
            assertThat(result).hasSize(1);
            verify(sessionRepositoryPort).findByGroupId(groupId);
        }

        @Test
        @DisplayName("Should return empty list when no sessions for group")
        void findByGroupId_WhenNoSessions_ReturnsEmptyList() {
            // Given
            Long groupId = 999L;
            when(sessionRepositoryPort.findByGroupId(groupId)).thenReturn(Collections.emptyList());

            // When
            List<Session> result = sessionService.findByGroupId(groupId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return sessions for subject")
        void findBySubjectId_ReturnsSessionsForSubject() {
            // Given
            Long subjectId = 1L;
            List<Session> sessions = List.of(testSession);
            when(sessionRepositoryPort.findBySubjectId(subjectId)).thenReturn(sessions);

            // When
            List<Session> result = sessionService.findBySubjectId(subjectId);

            // Then
            assertThat(result).hasSize(1);
            verify(sessionRepositoryPort).findBySubjectId(subjectId);
        }

        @Test
        @DisplayName("Should return sessions for schedule")
        void findByScheduleId_ReturnsSessionsForSchedule() {
            // Given
            Long scheduleId = 1L;
            List<Session> sessions = List.of(testSession);
            when(sessionRepositoryPort.findByScheduleId(scheduleId)).thenReturn(sessions);

            // When
            List<Session> result = sessionService.findByScheduleId(scheduleId);

            // Then
            assertThat(result).hasSize(1);
            verify(sessionRepositoryPort).findByScheduleId(scheduleId);
        }
    }

    @Nested
    @DisplayName("Update Session Tests")
    class UpdateSessionTests {

        @Test
        @DisplayName("Should update session successfully when scheduled")
        void update_WhenSessionIsScheduled_UpdatesSuccessfully() {
            // Given
            Long sessionId = 1L;
            UpdateSessionCommand command = new UpdateSessionCommand(
                    Classroom.AULA_PORTAL2,
                    LocalDate.now().plusDays(2),
                    LocalTime.of(14, 0),
                    LocalTime.of(16, 0),
                    SessionMode.DUAL
            );

            Session scheduledSession = SessionFactory.scheduledRegular();
            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(scheduledSession));
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Session result = sessionService.update(sessionId, command);

            // Then
            assertThat(result.getClassroom()).isEqualTo(Classroom.AULA_PORTAL2);
            assertThat(result.getDate()).isEqualTo(LocalDate.now().plusDays(2));
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(14, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalTime.of(16, 0));
            assertThat(result.getMode()).isEqualTo(SessionMode.DUAL);
            verify(sessionRepositoryPort).save(any(Session.class));
        }

        @Test
        @DisplayName("Should skip null fields during update")
        void update_WithNullFields_SkipsNullFields() {
            // Given
            Long sessionId = 1L;
            UpdateSessionCommand command = new UpdateSessionCommand(null, null, null, null, null);

            Session scheduledSession = SessionFactory.scheduledRegular();
            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(scheduledSession));
            when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Session result = sessionService.update(sessionId, command);

            // Then
            assertThat(result.getClassroom()).isEqualTo(scheduledSession.getClassroom());
            assertThat(result.getDate()).isEqualTo(scheduledSession.getDate());
            assertThat(result.getStartTime()).isEqualTo(scheduledSession.getStartTime());
            assertThat(result.getEndTime()).isEqualTo(scheduledSession.getEndTime());
            assertThat(result.getMode()).isEqualTo(scheduledSession.getMode());
        }

        @Test
        @DisplayName("Should throw exception when session not found")
        void update_WhenSessionNotFound_ThrowsException() {
            // Given
            Long sessionId = 999L;
            UpdateSessionCommand command = new UpdateSessionCommand(
                    Classroom.AULA_PORTAL2, null, null, null, null
            );

            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> sessionService.update(sessionId, command))
                    .isInstanceOf(SessionNotFoundException.class);

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when updating IN_PROGRESS session")
        void update_WhenSessionInProgress_ThrowsException() {
            // Given
            Long sessionId = 1L;
            UpdateSessionCommand command = new UpdateSessionCommand(
                    Classroom.AULA_PORTAL2, null, null, null, null
            );

            Session inProgressSession = SessionFactory.inProgressSession();
            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(inProgressSession));

            // When & Then
            assertThatThrownBy(() -> sessionService.update(sessionId, command))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be updated");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when updating COMPLETED session")
        void update_WhenSessionCompleted_ThrowsException() {
            // Given
            Long sessionId = 1L;
            UpdateSessionCommand command = new UpdateSessionCommand(
                    Classroom.AULA_PORTAL2, null, null, null, null
            );

            Session completedSession = SessionFactory.completedSession();
            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(completedSession));

            // When & Then
            assertThatThrownBy(() -> sessionService.update(sessionId, command))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be updated");

            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when updating CANCELLED session")
        void update_WhenSessionCancelled_ThrowsException() {
            // Given
            Long sessionId = 1L;
            UpdateSessionCommand command = new UpdateSessionCommand(
                    Classroom.AULA_PORTAL2, null, null, null, null
            );

            Session cancelledSession = SessionFactory.cancelledSession();
            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(cancelledSession));

            // When & Then
            assertThatThrownBy(() -> sessionService.update(sessionId, command))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be updated");

            verify(sessionRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Session Tests")
    class DeleteSessionTests {

        @Test
        @DisplayName("Should delete session successfully when scheduled")
        void delete_WhenSessionIsScheduled_DeletesSuccessfully() {
            // Given
            Long sessionId = 1L;
            Session scheduledSession = SessionFactory.scheduledRegular();
            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(scheduledSession));

            // When
            sessionService.delete(sessionId);

            // Then
            verify(sessionRepositoryPort).delete(sessionId);
        }

        @Test
        @DisplayName("Should throw exception when session not found")
        void delete_WhenSessionNotFound_ThrowsException() {
            // Given
            Long sessionId = 999L;
            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> sessionService.delete(sessionId))
                    .isInstanceOf(SessionNotFoundException.class);

            verify(sessionRepositoryPort, never()).delete(anyLong());
        }

        @Test
        @DisplayName("Should throw exception when deleting IN_PROGRESS session")
        void delete_WhenSessionInProgress_ThrowsException() {
            // Given
            Long sessionId = 1L;
            Session inProgressSession = SessionFactory.inProgressSession();
            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(inProgressSession));

            // When & Then
            assertThatThrownBy(() -> sessionService.delete(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be deleted");

            verify(sessionRepositoryPort, never()).delete(anyLong());
        }

        @Test
        @DisplayName("Should throw exception when deleting COMPLETED session")
        void delete_WhenSessionCompleted_ThrowsException() {
            // Given
            Long sessionId = 1L;
            Session completedSession = SessionFactory.completedSession();
            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(completedSession));

            // When & Then
            assertThatThrownBy(() -> sessionService.delete(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be deleted");

            verify(sessionRepositoryPort, never()).delete(anyLong());
        }

        @Test
        @DisplayName("Should throw exception when deleting CANCELLED session")
        void delete_WhenSessionCancelled_ThrowsException() {
            // Given
            Long sessionId = 1L;
            Session cancelledSession = SessionFactory.cancelledSession();
            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(cancelledSession));

            // When & Then
            assertThatThrownBy(() -> sessionService.delete(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be deleted");

            verify(sessionRepositoryPort, never()).delete(anyLong());
        }

        @Test
        @DisplayName("Should throw exception when deleting POSTPONED session")
        void delete_WhenSessionPostponed_ThrowsException() {
            // Given
            Long sessionId = 1L;
            Session postponedSession = SessionFactory.postponedSession();
            when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(postponedSession));

            // When & Then
            assertThatThrownBy(() -> sessionService.delete(sessionId))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("Only SCHEDULED sessions can be deleted");

            verify(sessionRepositoryPort, never()).delete(anyLong());
        }
    }
}
