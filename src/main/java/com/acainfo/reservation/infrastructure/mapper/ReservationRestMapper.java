package com.acainfo.reservation.infrastructure.mapper;

import com.acainfo.reservation.application.dto.*;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

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
     * Create RequestOnlineAttendanceCommand from reservationId and studentId.
     */
    default RequestOnlineAttendanceCommand toRequestOnlineCommand(Long reservationId, Long studentId) {
        return new RequestOnlineAttendanceCommand(reservationId, studentId);
    }

    /**
     * Create ProcessOnlineRequestCommand from reservationId, teacherId and request.
     */
    default ProcessOnlineRequestCommand toProcessCommand(Long reservationId, Long teacherId, ProcessOnlineRequestRequest request) {
        return new ProcessOnlineRequestCommand(reservationId, teacherId, request.getApproved());
    }

    /**
     * Create RecordAttendanceCommand from reservationId, recordedById and request.
     */
    default RecordAttendanceCommand toRecordCommand(Long reservationId, Long recordedById, RecordAttendanceRequest request) {
        return new RecordAttendanceCommand(reservationId, request.getStatus(), recordedById);
    }

    /**
     * Create BulkRecordAttendanceCommand from sessionId, recordedById and request.
     */
    default BulkRecordAttendanceCommand toBulkRecordCommand(Long sessionId, Long recordedById, BulkRecordAttendanceRequest request) {
        return new BulkRecordAttendanceCommand(sessionId, request.getAttendanceMap(), recordedById);
    }

    /**
     * Create GenerateReservationsCommand from sessionId and groupId.
     */
    default GenerateReservationsCommand toGenerateCommand(Long sessionId, Long groupId) {
        return new GenerateReservationsCommand(sessionId, groupId);
    }

    // ==================== Domain to Response ====================

    /**
     * Convert SessionReservation (Domain) to ReservationResponse (REST).
     */
    @Mapping(target = "isConfirmed", expression = "java(reservation.isConfirmed())")
    @Mapping(target = "isCancelled", expression = "java(reservation.isCancelled())")
    @Mapping(target = "isInPerson", expression = "java(reservation.isInPerson())")
    @Mapping(target = "isOnline", expression = "java(reservation.isOnline())")
    @Mapping(target = "hasOnlineRequest", expression = "java(reservation.hasOnlineRequest())")
    @Mapping(target = "isOnlineRequestPending", expression = "java(reservation.isOnlineRequestPending())")
    @Mapping(target = "isOnlineRequestApproved", expression = "java(reservation.isOnlineRequestApproved())")
    @Mapping(target = "isOnlineRequestRejected", expression = "java(reservation.isOnlineRequestRejected())")
    @Mapping(target = "hasAttendanceRecorded", expression = "java(reservation.hasAttendanceRecorded())")
    @Mapping(target = "wasPresent", expression = "java(reservation.wasPresent())")
    @Mapping(target = "wasAbsent", expression = "java(reservation.wasAbsent())")
    @Mapping(target = "canBeCancelled", expression = "java(reservation.canBeCancelled())")
    @Mapping(target = "canRequestOnline", expression = "java(reservation.canRequestOnline())")
    ReservationResponse toResponse(SessionReservation reservation);

    /**
     * Convert list of SessionReservations (Domain) to list of ReservationResponses (REST).
     */
    List<ReservationResponse> toResponseList(List<SessionReservation> reservations);
}
