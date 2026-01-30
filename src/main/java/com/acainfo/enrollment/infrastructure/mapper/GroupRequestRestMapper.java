package com.acainfo.enrollment.infrastructure.mapper;

import com.acainfo.enrollment.application.dto.CreateGroupRequestCommand;
import com.acainfo.enrollment.application.dto.ProcessGroupRequestCommand;
import com.acainfo.enrollment.domain.model.GroupRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.CreateGroupRequestRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.GroupRequestResponse;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.ProcessGroupRequestRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for GroupRequest REST layer conversions.
 * Converts between REST DTOs and Application DTOs / Domain entities.
 */
@Mapper(componentModel = "spring")
public interface GroupRequestRestMapper {

    /**
     * Convert CreateGroupRequestRequest (REST) to CreateGroupRequestCommand (Application).
     */
    CreateGroupRequestCommand toCommand(CreateGroupRequestRequest request);

    /**
     * Convert ProcessGroupRequestRequest (REST) to ProcessGroupRequestCommand (Application).
     * Note: groupRequestId is passed separately in the controller.
     */
    @Mapping(target = "groupRequestId", ignore = true)
    ProcessGroupRequestCommand toCommand(ProcessGroupRequestRequest request);

    /**
     * Create ProcessGroupRequestCommand with groupRequestId and request.
     */
    default ProcessGroupRequestCommand toCommand(Long groupRequestId, ProcessGroupRequestRequest request) {
        return new ProcessGroupRequestCommand(groupRequestId, request.getAdminId(), request.getAdminResponse());
    }

    /**
     * Convert GroupRequest (Domain) to GroupRequestResponse (REST).
     * Note: Fields marked as ignored are enriched by GroupRequestResponseEnricher.
     */
    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "subjectDegree", ignore = true)
    @Mapping(target = "requesterName", ignore = true)
    @Mapping(target = "supporterNames", ignore = true)
    @Mapping(target = "processedByAdminName", ignore = true)
    @Mapping(target = "supporterCount", expression = "java(groupRequest.getSupporterCount())")
    @Mapping(target = "hasMinimumSupporters", expression = "java(groupRequest.hasMinimumSupporters())")
    @Mapping(target = "supportersNeeded", expression = "java(groupRequest.getSupportersNeeded())")
    @Mapping(target = "isPending", expression = "java(groupRequest.isPending())")
    @Mapping(target = "isApproved", expression = "java(groupRequest.isApproved())")
    @Mapping(target = "isRejected", expression = "java(groupRequest.isRejected())")
    @Mapping(target = "isExpired", expression = "java(groupRequest.isExpired())")
    @Mapping(target = "isProcessed", expression = "java(groupRequest.isProcessed())")
    GroupRequestResponse toResponse(GroupRequest groupRequest);

    /**
     * Convert list of GroupRequests (Domain) to list of GroupRequestResponses (REST).
     */
    List<GroupRequestResponse> toResponseList(List<GroupRequest> groupRequests);
}
