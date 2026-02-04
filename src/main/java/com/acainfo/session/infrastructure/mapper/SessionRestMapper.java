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
     * Convert Session (Domain) to SessionResponse (REST) without enriched data.
     * Use toEnrichedResponse for enriched responses.
     */
    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "subjectCode", ignore = true)
    @Mapping(target = "groupName", ignore = true)
    @Mapping(target = "groupType", ignore = true)
    @Mapping(target = "teacherName", ignore = true)
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
     * Convert Session (Domain) to SessionResponse (REST) with enriched data.
     *
     * @param session     the session domain object
     * @param subjectName name of the subject
     * @param subjectCode code of the subject
     * @param groupName   name of the group (nullable for sessions without group)
     * @param groupType   type of the group as string (nullable for sessions without group)
     * @param teacherName full name of the teacher (nullable for sessions without group)
     * @return enriched session response
     */
    @Mapping(target = "subjectName", source = "subjectName")
    @Mapping(target = "subjectCode", source = "subjectCode")
    @Mapping(target = "groupName", source = "groupName")
    @Mapping(target = "groupType", source = "groupType")
    @Mapping(target = "teacherName", source = "teacherName")
    @Mapping(target = "id", source = "session.id")
    @Mapping(target = "subjectId", source = "session.subjectId")
    @Mapping(target = "groupId", source = "session.groupId")
    @Mapping(target = "scheduleId", source = "session.scheduleId")
    @Mapping(target = "classroom", source = "session.classroom")
    @Mapping(target = "date", source = "session.date")
    @Mapping(target = "startTime", source = "session.startTime")
    @Mapping(target = "endTime", source = "session.endTime")
    @Mapping(target = "status", source = "session.status")
    @Mapping(target = "type", source = "session.type")
    @Mapping(target = "mode", source = "session.mode")
    @Mapping(target = "postponedToDate", source = "session.postponedToDate")
    @Mapping(target = "createdAt", source = "session.createdAt")
    @Mapping(target = "updatedAt", source = "session.updatedAt")
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
    SessionResponse toEnrichedResponse(
            Session session,
            String subjectName,
            String subjectCode,
            String groupName,
            String groupType,
            String teacherName
    );

    /**
     * Convert list of Sessions (Domain) to list of SessionResponses (REST).
     */
    List<SessionResponse> toResponseList(List<Session> sessions);
}
