package com.acainfo.reservation.infrastructure.mapper;

import com.acainfo.reservation.application.dto.*;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Reservation REST layer conversions.
 * Converts between REST DTOs and Application DTOs / Domain entities.
 */
@Mapper(componentModel = "spring")
public interface ReservationRestMapper {

    // ==================== Request to Command ====================

    /**
     * Convert CreateReservationRequest (REST) to CreateReservationCommand (Application).
     */
    CreateReservationCommand toCommand(CreateReservationRequest request);

    /**
     * Create SwitchSessionCommand with reservationId, studentId and request.
     */
    default SwitchSessionCommand toSwitchCommand(Long reservationId, Long studentId, SwitchSessionRequest request) {
        return new SwitchSessionCommand(studentId, reservationId, request.getNewSessionId());
    }

    /**
     * Create GenerateReservationsCommand from sessionId and courseId.
     */
    default GenerateReservationsCommand toGenerateCommand(Long sessionId, Long courseId) {
        return new GenerateReservationsCommand(sessionId, courseId);
    }

    // ==================== Domain to Response ====================

    /**
     * Convert SessionReservation (Domain) to ReservationResponse (REST).
     */
    @Mapping(target = "isConfirmed", expression = "java(reservation.isConfirmed())")
    @Mapping(target = "isCancelled", expression = "java(reservation.isCancelled())")
    @Mapping(target = "isInPerson", expression = "java(reservation.isInPerson())")
    @Mapping(target = "isOnline", expression = "java(reservation.isOnline())")
    @Mapping(target = "canBeCancelled", expression = "java(reservation.canBeCancelled())")
    @Mapping(target = "studentName", ignore = true)
    @Mapping(target = "studentEmail", ignore = true)
    ReservationResponse toResponse(SessionReservation reservation);

    /**
     * Convert list of SessionReservations (Domain) to list of ReservationResponses (REST).
     */
    List<ReservationResponse> toResponseList(List<SessionReservation> reservations);
}
