package acainfo.back.schedule.application.mappers;

import acainfo.back.schedule.domain.model.ScheduleDomain;
import acainfo.back.schedule.infrastructure.adapters.in.dto.CreateScheduleRequest;
import acainfo.back.schedule.infrastructure.adapters.in.dto.ScheduleResponse;
import acainfo.back.schedule.infrastructure.adapters.in.dto.UpdateScheduleRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Mapper
 * Converts: Domain Schedule ↔ DTOs
 *
 * Responsibility: Adapt domain for REST APIs
 */
@Component
public class ScheduleDtoMapper {

    /**
     * Converts Domain → Response DTO (API output)
     * Note: This creates a basic response. Related entity data (groupName, subjectName, teacherName)
     * should be populated by the controller if needed.
     */
    public ScheduleResponse toResponse(ScheduleDomain domain) {
        if (domain == null) {
            return null;
        }

        return ScheduleResponse.builder()
                .id(domain.getId())
                .groupId(domain.getSubjectGroupId())
                .dayOfWeek(domain.getDayOfWeek())
                .dayOfWeekLocalized(domain.getLocalizedDayName()) // Use domain business logic
                .startTime(domain.getStartTime())
                .endTime(domain.getEndTime())
                .durationInMinutes(domain.getDurationInMinutes()) // Use domain business logic
                .classroom(domain.getClassroom())
                .classroomDisplayName(domain.getClassroom() != null ?
                        domain.getClassroom().getDisplayName() : null)
                .formattedSchedule(domain.getFormattedScheduleSpanish()) // Use domain business logic
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * Converts Request DTO → Domain (API input - creation)
     */
    public ScheduleDomain toDomain(CreateScheduleRequest request) {
        if (request == null) {
            return null;
        }

        return ScheduleDomain.builder()
                .subjectGroupId(request.getGroupId())
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .classroom(request.getClassroom())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Converts Update DTO → Domain (API input - update)
     * Merges update request data with existing domain entity
     */
    public ScheduleDomain updateDomainFromDto(ScheduleDomain existing, UpdateScheduleRequest request) {
        return ScheduleDomain.builder()
                .id(existing.getId())
                .subjectGroupId(existing.getSubjectGroupId()) // SubjectGroup cannot be changed after creation
                .dayOfWeek(request.getDayOfWeek() != null ? request.getDayOfWeek() : existing.getDayOfWeek())
                .startTime(request.getStartTime() != null ? request.getStartTime() : existing.getStartTime())
                .endTime(request.getEndTime() != null ? request.getEndTime() : existing.getEndTime())
                .classroom(request.getClassroom() != null ? request.getClassroom() : existing.getClassroom())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Converts list of Domain → Response
     */
    public List<ScheduleResponse> toResponses(List<ScheduleDomain> domains) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
