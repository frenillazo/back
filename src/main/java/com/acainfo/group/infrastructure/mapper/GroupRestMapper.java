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
 * MapStruct mapper for REST layer conversions.
 * Converts between REST DTOs and Application DTOs / Domain entities.
 */
@Mapper(componentModel = "spring")
public interface GroupRestMapper {

    /**
     * Convert CreateGroupRequest (REST) to CreateGroupCommand (Application).
     */
    CreateGroupCommand toCommand(CreateGroupRequest request);

    /**
     * Convert UpdateGroupRequest (REST) to UpdateGroupCommand (Application).
     */
    UpdateGroupCommand toCommand(UpdateGroupRequest request);

    /**
     * Convert SubjectGroup (Domain) to GroupResponse (REST) without enriched data.
     * Use toEnrichedResponse for enriched responses.
     */
    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "subjectCode", ignore = true)
    @Mapping(target = "teacherName", ignore = true)
    @Mapping(target = "availableSeats", expression = "java(group.getAvailableSeats())")
    @Mapping(target = "maxCapacity", expression = "java(group.getMaxCapacity())")
    @Mapping(target = "isOpen", expression = "java(group.isOpen())")
    @Mapping(target = "canEnroll", expression = "java(group.canEnroll())")
    @Mapping(target = "isIntensive", expression = "java(group.isIntensive())")
    @Mapping(target = "isRegular", expression = "java(group.isRegular())")
    GroupResponse toResponse(SubjectGroup group);

    /**
     * Convert SubjectGroup (Domain) to GroupResponse (REST) with enriched data.
     *
     * @param group       the group domain object
     * @param subjectName name of the subject
     * @param subjectCode code of the subject
     * @param teacherName full name of the teacher
     * @return enriched group response
     */
    @Mapping(target = "subjectName", source = "subjectName")
    @Mapping(target = "subjectCode", source = "subjectCode")
    @Mapping(target = "teacherName", source = "teacherName")
    @Mapping(target = "id", source = "group.id")
    @Mapping(target = "subjectId", source = "group.subjectId")
    @Mapping(target = "teacherId", source = "group.teacherId")
    @Mapping(target = "type", source = "group.type")
    @Mapping(target = "status", source = "group.status")
    @Mapping(target = "currentEnrollmentCount", source = "group.currentEnrollmentCount")
    @Mapping(target = "capacity", source = "group.capacity")
    @Mapping(target = "createdAt", source = "group.createdAt")
    @Mapping(target = "updatedAt", source = "group.updatedAt")
    @Mapping(target = "availableSeats", expression = "java(group.getAvailableSeats())")
    @Mapping(target = "maxCapacity", expression = "java(group.getMaxCapacity())")
    @Mapping(target = "isOpen", expression = "java(group.isOpen())")
    @Mapping(target = "canEnroll", expression = "java(group.canEnroll())")
    @Mapping(target = "isIntensive", expression = "java(group.isIntensive())")
    @Mapping(target = "isRegular", expression = "java(group.isRegular())")
    GroupResponse toEnrichedResponse(
            SubjectGroup group,
            String subjectName,
            String subjectCode,
            String teacherName
    );
}
