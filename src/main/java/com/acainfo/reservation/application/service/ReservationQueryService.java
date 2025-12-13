package com.acainfo.reservation.application.service;

import com.acainfo.reservation.application.dto.ReservationFilters;
import com.acainfo.reservation.application.port.in.GetReservationUseCase;
import com.acainfo.reservation.application.port.out.ReservationRepositoryPort;
import com.acainfo.reservation.domain.exception.ReservationNotFoundException;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.shared.application.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementing reservation query use cases.
 * Handles all read operations for reservations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReservationQueryService implements GetReservationUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;

    // ==================== GetReservationUseCase ====================

    @Override
    public SessionReservation getById(Long id) {
        log.debug("Getting reservation by ID: {}", id);
        return reservationRepositoryPort.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    @Override
    public PageResponse<SessionReservation> findWithFilters(ReservationFilters filters) {
        log.debug("Finding reservations with filters: studentId={}, sessionId={}, status={}, mode={}",
                filters.studentId(), filters.sessionId(), filters.status(), filters.mode());

        Page<SessionReservation> page = reservationRepositoryPort.findWithFilters(filters);

        return PageResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    @Override
    public List<SessionReservation> getBySessionId(Long sessionId) {
        log.debug("Getting reservations by sessionId: {}", sessionId);
        return reservationRepositoryPort.findBySessionId(sessionId);
    }

    @Override
    public List<SessionReservation> getByStudentId(Long studentId) {
        log.debug("Getting reservations by studentId: {}", studentId);
        return reservationRepositoryPort.findByStudentId(studentId);
    }

    @Override
    public List<SessionReservation> getPendingOnlineRequestsForTeacher(Long teacherId) {
        log.debug("Getting pending online requests for teacher: {}", teacherId);
        return reservationRepositoryPort.findPendingOnlineRequestsByTeacherId(teacherId);
    }
}
