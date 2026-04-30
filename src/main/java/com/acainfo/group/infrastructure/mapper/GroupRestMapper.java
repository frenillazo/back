package com.acainfo.group.infrastructure.mapper;

import com.acainfo.group.application.dto.CreateGroupCommand;
import com.acainfo.group.application.dto.UpdateGroupCommand;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.group.infrastructure.adapter.in.rest.dto.CreateGroupRequest;
import com.acainfo.group.infrastructure.adapter.in.rest.dto.GroupResponse;
import com.acainfo.group.infrastructure.adapter.in.rest.dto.UpdateGroupRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for the Group REST layer.
 */
@Mapper(componentModel = "spring")
public interface GroupRestMapper {

    CreateGroupCommand toCommand(CreateGroupRequest request);

    UpdateGroupCommand toCommand(UpdateGroupRequest request);

    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "subjectCode", ignore = true)
    @Mapping(target = "teacherName", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "availableSeats", expression = "java(group.getAvailableSeats())")
    @Mapping(target = "maxCapacity", expression = "java(group.getMaxCapacity())")
    @Mapping(target = "pricePerHour", expression = "java(group.getEffectivePricePerHour())")
    @Mapping(target = "isOpen", expression = "java(group.isOpen())")
    @Mapping(target = "canEnroll", expression = "java(group.canEnroll())")
    GroupResponse toResponse(SubjectGroup group);

    @Mapping(target = "subjectName", source = "subjectName")
    @Mapping(target = "subjectCode", source = "subjectCode")
    @Mapping(target = "teacherName", source = "teacherName")
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "id", source = "group.id")
    @Mapping(target = "name", source = "group.name")
    @Mapping(target = "subjectId", source = "group.subjectId")
    @Mapping(target = "teacherId", source = "group.teacherId")
    @Mapping(target = "status", source = "group.status")
    @Mapping(target = "currentEnrollmentCount", source = "group.currentEnrollmentCount")
    @Mapping(target = "capacity", source = "group.capacity")
    @Mapping(target = "startDate", source = "group.startDate")
    @Mapping(target = "endDate", source = "group.endDate")
    @Mapping(target = "createdAt", source = "group.createdAt")
    @Mapping(target = "updatedAt", source = "group.updatedAt")
    @Mapping(target = "availableSeats", expression = "java(group.getAvailableSeats())")
    @Mapping(target = "maxCapacity", expression = "java(group.getMaxCapacity())")
    @Mapping(target = "pricePerHour", expression = "java(group.getEffectivePricePerHour())")
    @Mapping(target = "isOpen", expression = "java(group.isOpen())")
    @Mapping(target = "canEnroll", expression = "java(group.canEnroll())")
    GroupResponse toEnrichedResponse(
            SubjectGroup group,
            String subjectName,
            String subjectCode,
            String teacherName
    );
}
