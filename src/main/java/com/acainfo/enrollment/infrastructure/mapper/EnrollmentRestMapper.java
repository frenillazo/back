package com.acainfo.enrollment.infrastructure.mapper;

import com.acainfo.enrollment.application.dto.ChangeGroupCommand;
import com.acainfo.enrollment.application.dto.EnrollStudentCommand;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.ChangeGroupRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.EnrollStudentRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.EnrollmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Enrollment REST layer conversions.
 * Converts between REST DTOs and Application DTOs / Domain entities.
 */
@Mapper(componentModel = "spring")
public interface EnrollmentRestMapper {

    /**
     * Convert EnrollStudentRequest (REST) to EnrollStudentCommand (Application).
     */
    EnrollStudentCommand toCommand(EnrollStudentRequest request);

    /**
     * Convert ChangeGroupRequest (REST) to ChangeGroupCommand (Application).
     * Note: enrollmentId is passed separately in the controller.
     */
    @Mapping(target = "enrollmentId", ignore = true)
    @Mapping(target = "newGroupId", source = "newGroupId")
    ChangeGroupCommand toCommand(ChangeGroupRequest request);

    /**
     * Create ChangeGroupCommand with enrollmentId and request.
     */
    default ChangeGroupCommand toCommand(Long enrollmentId, ChangeGroupRequest request) {
        return new ChangeGroupCommand(enrollmentId, request.getNewGroupId());
    }

    /**
     * Convert Enrollment (Domain) to EnrollmentResponse (REST).
     */
    @Mapping(target = "isActive", expression = "java(enrollment.isActive())")
    @Mapping(target = "isOnWaitingList", expression = "java(enrollment.isOnWaitingList())")
    @Mapping(target = "isWithdrawn", expression = "java(enrollment.isWithdrawn())")
    @Mapping(target = "isCompleted", expression = "java(enrollment.isCompleted())")
    @Mapping(target = "wasPromotedFromWaitingList", expression = "java(enrollment.wasPromotedFromWaitingList())")
    @Mapping(target = "canBeWithdrawn", expression = "java(enrollment.canBeWithdrawn())")
    EnrollmentResponse toResponse(Enrollment enrollment);

    /**
     * Convert list of Enrollments (Domain) to list of EnrollmentResponses (REST).
     */
    List<EnrollmentResponse> toResponseList(List<Enrollment> enrollments);
}
