package com.acainfo.subject.infrastructure.mapper;

import com.acainfo.subject.application.dto.CreateSubjectCommand;
import com.acainfo.subject.application.dto.UpdateSubjectCommand;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.subject.infrastructure.adapter.in.rest.dto.CreateSubjectRequest;
import com.acainfo.subject.infrastructure.adapter.in.rest.dto.SubjectResponse;
import com.acainfo.subject.infrastructure.adapter.in.rest.dto.UpdateSubjectRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for REST layer.
 * Converts between REST DTOs and Application DTOs/Domain entities.
 *
 * Maps:
 * - Request DTOs → Commands (application layer)
 * - Domain entities → Response DTOs
 */
@Mapper(componentModel = "spring")
public interface SubjectRestMapper {

    // ==================== Request → Command ====================

    /**
     * Maps CreateSubjectRequest (REST) to CreateSubjectCommand (application).
     */
    CreateSubjectCommand toCreateSubjectCommand(CreateSubjectRequest request);

    /**
     * Maps UpdateSubjectRequest (REST) to UpdateSubjectCommand (application).
     */
    UpdateSubjectCommand toUpdateSubjectCommand(UpdateSubjectRequest request);

    // ==================== Domain → Response ====================

    /**
     * Maps Subject (domain) to SubjectResponse (REST).
     * Uses Java expressions to map computed properties.
     */
    @Mapping(target = "displayName", expression = "java(subject.getDisplayName())")
    @Mapping(target = "remainingGroupSlots", expression = "java(subject.getRemainingGroupSlots())")
    @Mapping(target = "active", expression = "java(subject.isActive())")
    @Mapping(target = "archived", expression = "java(subject.isArchived())")
    @Mapping(target = "canCreateGroup", expression = "java(subject.canCreateGroup())")
    SubjectResponse toSubjectResponse(Subject subject);
}
