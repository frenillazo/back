package com.acainfo.schedule.infrastructure.mapper;

import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.schedule.application.dto.CreateScheduleCommand;
import com.acainfo.schedule.application.dto.UpdateScheduleCommand;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.schedule.infrastructure.adapter.in.rest.dto.CreateScheduleRequest;
import com.acainfo.schedule.infrastructure.adapter.in.rest.dto.ScheduleEnrichedResponse;
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

    /**
     * Convert Schedule (Domain) to ScheduleEnrichedResponse (REST) with group data.
     *
     * @param schedule    the schedule domain object
     * @param group       the group domain object
     * @param subjectName name of the subject
     * @param subjectCode code of the subject
     * @param teacherName full name of the teacher
     * @return enriched schedule response
     */
    default ScheduleEnrichedResponse toEnrichedResponse(
            Schedule schedule,
            SubjectGroup group,
            String subjectName,
            String subjectCode,
            String teacherName
    ) {
        return ScheduleEnrichedResponse.builder()
                .id(schedule.getId())
                .groupId(schedule.getGroupId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .classroom(schedule.getClassroom())
                .classroomDisplayName(schedule.getClassroom().getDisplayName())
                .durationMinutes(schedule.getDurationMinutes())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .groupType(group.getType())
                .groupStatus(group.getStatus())
                .pricePerHour(group.getEffectivePricePerHour())
                .subjectId(group.getSubjectId())
                .subjectName(subjectName)
                .subjectCode(subjectCode)
                .teacherId(group.getTeacherId())
                .teacherName(teacherName)
                .build();
    }
}
