package com.acainfo.session.application.service;

import com.acainfo.reservation.application.port.out.ReservationRepositoryPort;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.session.application.dto.PostponeSessionCommand;
import com.acainfo.session.application.port.in.GetSessionUseCase;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionLifecycleServiceTest {

    @Mock
    private SessionRepositoryPort sessionRepositoryPort;

    @Mock
    private GetSessionUseCase getSessionUseCase;

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    @InjectMocks
    private SessionLifecycleService service;

    private static final Long ORIGINAL_SESSION_ID = 1L;
    private static final Long NEW_SESSION_ID = 2L;

    private Session scheduledSession() {
        return Session.builder()
                .id(ORIGINAL_SESSION_ID)
                .subjectId(10L)
                .courseId(20L)
                .scheduleId(30L)
                .classroom(Classroom.AULA_PORTAL1)
                .date(LocalDate.of(2026, 7, 20))
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(18, 0))
                .status(SessionStatus.SCHEDULED)
                .type(SessionType.REGULAR)
                .mode(SessionMode.IN_PERSON)
                .build();
    }

    private SessionReservation reservation(Long id, Long studentId, ReservationStatus status) {
        return SessionReservation.builder()
                .id(id)
                .studentId(studentId)
                .sessionId(ORIGINAL_SESSION_ID)
                .enrollmentId(100L + studentId)
                .mode(ReservationMode.IN_PERSON)
                .status(status)
                .reservedAt(LocalDateTime.of(2026, 7, 1, 10, 0))
                .build();
    }

    private void stubPostponeFlow() {
        when(getSessionUseCase.getById(ORIGINAL_SESSION_ID)).thenReturn(scheduledSession());
        when(sessionRepositoryPort.save(any(Session.class))).thenAnswer(invocation -> {
            Session session = invocation.getArgument(0);
            if (session.getId() == null) {
                session.setId(NEW_SESSION_ID);
            }
            return session;
        });
    }

    private PostponeSessionCommand command() {
        return new PostponeSessionCommand(LocalDate.of(2026, 7, 27), null, null, null, null);
    }

    @Test
    void posponerLlevaLasReservasConfirmadasALaSesionNueva() {
        // La sesión de reemplazo nacía SIN reservas: el alumno veía "Pospuesta al
        // X", iba a la nueva y no tenía plaza, sin que nada se lo advirtiera.
        stubPostponeFlow();
        when(reservationRepositoryPort.findBySessionId(ORIGINAL_SESSION_ID)).thenReturn(List.of(
                reservation(1L, 3L, ReservationStatus.CONFIRMED),
                reservation(2L, 4L, ReservationStatus.CONFIRMED)
        ));

        service.postpone(ORIGINAL_SESSION_ID, command());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SessionReservation>> captor = ArgumentCaptor.forClass(List.class);
        verify(reservationRepositoryPort).saveAll(captor.capture());

        assertThat(captor.getValue())
                .hasSize(2)
                .allSatisfy(r -> {
                    assertThat(r.getSessionId()).isEqualTo(NEW_SESSION_ID);
                    // Reserva nueva, no la misma movida: la original queda de histórico
                    assertThat(r.getId()).isNull();
                    assertThat(r.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
                })
                .extracting(SessionReservation::getStudentId)
                .containsExactlyInAnyOrder(3L, 4L);
    }

    @Test
    void posponerNoArrastraLasReservasYaCanceladas() {
        stubPostponeFlow();
        when(reservationRepositoryPort.findBySessionId(ORIGINAL_SESSION_ID)).thenReturn(List.of(
                reservation(1L, 3L, ReservationStatus.CONFIRMED),
                reservation(2L, 4L, ReservationStatus.CANCELLED)
        ));

        service.postpone(ORIGINAL_SESSION_ID, command());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SessionReservation>> captor = ArgumentCaptor.forClass(List.class);
        verify(reservationRepositoryPort).saveAll(captor.capture());

        assertThat(captor.getValue())
                .hasSize(1)
                .extracting(SessionReservation::getStudentId)
                .containsExactly(3L);
    }

    @Test
    void posponerUnaSesionSinReservasNoLlamaAsaveAll() {
        stubPostponeFlow();
        when(reservationRepositoryPort.findBySessionId(ORIGINAL_SESSION_ID)).thenReturn(List.of());

        service.postpone(ORIGINAL_SESSION_ID, command());

        verify(reservationRepositoryPort, never()).saveAll(any());
    }

    @Test
    void laSesionOriginalQuedaPospuestaYApuntaALaNuevaFecha() {
        stubPostponeFlow();
        when(reservationRepositoryPort.findBySessionId(ORIGINAL_SESSION_ID)).thenReturn(List.of());

        Session nueva = service.postpone(ORIGINAL_SESSION_ID, command());

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepositoryPort, org.mockito.Mockito.times(2)).save(captor.capture());

        Session original = captor.getAllValues().get(0);
        assertThat(original.getStatus()).isEqualTo(SessionStatus.POSTPONED);
        assertThat(original.getPostponedToDate()).isEqualTo(LocalDate.of(2026, 7, 27));
        assertThat(nueva.getStatus()).isEqualTo(SessionStatus.SCHEDULED);
        assertThat(nueva.getDate()).isEqualTo(LocalDate.of(2026, 7, 27));
    }
}
