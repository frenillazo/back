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
     * Convert Enrollment (Domain) to EnrollmentResponse (REST) without enriched data.
     * Use toEnrichedResponse for enriched responses.
     */
    @Mapping(target = "studentName", ignore = true)
    @Mapping(target = "studentEmail", ignore = true)
    @Mapping(target = "subjectId", ignore = true)
    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "subjectCode", ignore = true)
    @Mapping(target = "groupType", ignore = true)
    @Mapping(target = "teacherName", ignore = true)
    @Mapping(target = "isActive", expression = "java(enrollment.isActive())")
    @Mapping(target = "isOnWaitingList", expression = "java(enrollment.isOnWaitingList())")
    @Mapping(target = "isWithdrawn", expression = "java(enrollment.isWithdrawn())")
    @Mapping(target = "isCompleted", expression = "java(enrollment.isCompleted())")
    @Mapping(target = "wasPromotedFromWaitingList", expression = "java(enrollment.wasPromotedFromWaitingList())")
    @Mapping(target = "canBeWithdrawn", expression = "java(enrollment.canBeWithdrawn())")
    EnrollmentResponse toResponse(Enrollment enrollment);

    /**
     * Convert Enrollment (Domain) to EnrollmentResponse (REST) with enriched data.
     *
     * @param enrollment   the enrollment domain object
     * @param studentName  full name of the student
     * @param studentEmail email of the student
     * @param subjectId    ID of the subject
     * @param subjectName  name of the subject
     * @param subjectCode  code of the subject
     * @param groupType    type of the group as string
     * @param teacherName  full name of the teacher
     * @return enriched enrollment response
     */
    @Mapping(target = "studentName", source = "studentName")
    @Mapping(target = "studentEmail", source = "studentEmail")
    @Mapping(target = "subjectId", source = "subjectId")
    @Mapping(target = "subjectName", source = "subjectName")
    @Mapping(target = "subjectCode", source = "subjectCode")
    @Mapping(target = "groupType", source = "groupType")
    @Mapping(target = "teacherName", source = "teacherName")
    @Mapping(target = "id", source = "enrollment.id")
    @Mapping(target = "studentId", source = "enrollment.studentId")
    @Mapping(target = "groupId", source = "enrollment.groupId")
    @Mapping(target = "status", source = "enrollment.status")
    @Mapping(target = "waitingListPosition", source = "enrollment.waitingListPosition")
    @Mapping(target = "enrolledAt", source = "enrollment.enrolledAt")
    @Mapping(target = "promotedAt", source = "enrollment.promotedAt")
    @Mapping(target = "withdrawnAt", source = "enrollment.withdrawnAt")
    @Mapping(target = "createdAt", source = "enrollment.createdAt")
    @Mapping(target = "updatedAt", source = "enrollment.updatedAt")
    @Mapping(target = "isActive", expression = "java(enrollment.isActive())")
    @Mapping(target = "isOnWaitingList", expression = "java(enrollment.isOnWaitingList())")
    @Mapping(target = "isWithdrawn", expression = "java(enrollment.isWithdrawn())")
    @Mapping(target = "isCompleted", expression = "java(enrollment.isCompleted())")
    @Mapping(target = "wasPromotedFromWaitingList", expression = "java(enrollment.wasPromotedFromWaitingList())")
    @Mapping(target = "canBeWithdrawn", expression = "java(enrollment.canBeWithdrawn())")
    EnrollmentResponse toEnrichedResponse(
            Enrollment enrollment,
            String studentName,
            String studentEmail,
            Long subjectId,
            String subjectName,
            String subjectCode,
            String groupType,
            String teacherName
    );

    /**
     * Convert list of Enrollments (Domain) to list of EnrollmentResponses (REST).
     */
    List<EnrollmentResponse> toResponseList(List<Enrollment> enrollments);
}
