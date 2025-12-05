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
     * Convert SubjectGroup (Domain) to GroupResponse (REST).
     */
    @Mapping(target = "availableSeats", expression = "java(group.getAvailableSeats())")
    @Mapping(target = "maxCapacity", expression = "java(group.getMaxCapacity())")
    @Mapping(target = "isOpen", expression = "java(group.isOpen())")
    @Mapping(target = "canEnroll", expression = "java(group.canEnroll())")
    @Mapping(target = "isIntensive", expression = "java(group.isIntensive())")
    @Mapping(target = "isRegular", expression = "java(group.isRegular())")
    GroupResponse toResponse(SubjectGroup group);
}
