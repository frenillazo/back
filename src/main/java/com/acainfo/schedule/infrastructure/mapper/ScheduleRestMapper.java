package com.acainfo.schedule.infrastructure.mapper;

import com.acainfo.schedule.application.dto.CreateScheduleCommand;
import com.acainfo.schedule.application.dto.UpdateScheduleCommand;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.schedule.infrastructure.adapter.in.rest.dto.CreateScheduleRequest;
import com.acainfo.schedule.infrastructure.adapter.in.rest.dto.ScheduleResponse;
import com.acainfo.schedule.infrastructure.adapter.in.rest.dto.UpdateScheduleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for REST layer conversions.
 * Converts between REST DTOs and Application DTOs / Domain entities.
 */
@Mapper(componentModel = "spring")
public interface ScheduleRestMapper {

    /**
     * Convert CreateScheduleRequest (REST) to CreateScheduleCommand (Application).
     */
    CreateScheduleCommand toCommand(CreateScheduleRequest request);

    /**
     * Convert UpdateScheduleRequest (REST) to UpdateScheduleCommand (Application).
     */
    UpdateScheduleCommand toCommand(UpdateScheduleRequest request);

    /**
     * Convert Schedule (Domain) to ScheduleResponse (REST).
     */
    @Mapping(target = "classroomDisplayName", expression = "java(schedule.getClassroom().getDisplayName())")
    @Mapping(target = "durationMinutes", expression = "java(schedule.getDurationMinutes())")
    ScheduleResponse toResponse(Schedule schedule);
}
