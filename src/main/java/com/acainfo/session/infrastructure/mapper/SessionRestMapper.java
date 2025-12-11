package com.acainfo.session.infrastructure.mapper;

import com.acainfo.session.application.dto.CreateSessionCommand;
import com.acainfo.session.application.dto.GenerateSessionsCommand;
import com.acainfo.session.application.dto.PostponeSessionCommand;
import com.acainfo.session.application.dto.UpdateSessionCommand;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for REST layer conversions.
 * Converts between REST DTOs and Application DTOs / Domain entities.
 */
@Mapper(componentModel = "spring")
public interface SessionRestMapper {

    /**
     * Convert CreateSessionRequest (REST) to CreateSessionCommand (Application).
     */
    CreateSessionCommand toCommand(CreateSessionRequest request);

    /**
     * Convert UpdateSessionRequest (REST) to UpdateSessionCommand (Application).
     */
    UpdateSessionCommand toCommand(UpdateSessionRequest request);

    /**
     * Convert PostponeSessionRequest (REST) to PostponeSessionCommand (Application).
     */
    PostponeSessionCommand toCommand(PostponeSessionRequest request);

    /**
     * Convert GenerateSessionsRequest (REST) to GenerateSessionsCommand (Application).
     */
    GenerateSessionsCommand toCommand(GenerateSessionsRequest request);

    /**
     * Convert Session (Domain) to SessionResponse (REST).
     */
    @Mapping(target = "durationMinutes", expression = "java(session.getDurationMinutes())")
    @Mapping(target = "isScheduled", expression = "java(session.isScheduled())")
    @Mapping(target = "isInProgress", expression = "java(session.isInProgress())")
    @Mapping(target = "isCompleted", expression = "java(session.isCompleted())")
    @Mapping(target = "isCancelled", expression = "java(session.isCancelled())")
    @Mapping(target = "isPostponed", expression = "java(session.isPostponed())")
    @Mapping(target = "isRegular", expression = "java(session.isRegular())")
    @Mapping(target = "isExtra", expression = "java(session.isExtra())")
    @Mapping(target = "isSchedulingType", expression = "java(session.isSchedulingType())")
    @Mapping(target = "hasGroup", expression = "java(session.hasGroup())")
    @Mapping(target = "hasSchedule", expression = "java(session.hasSchedule())")
    SessionResponse toResponse(Session session);

    /**
     * Convert list of Sessions (Domain) to list of SessionResponses (REST).
     */
    List<SessionResponse> toResponseList(List<Session> sessions);
}
