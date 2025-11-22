package acainfo.back.session.application.mappers;

import acainfo.back.session.domain.model.SessionDomain;
import acainfo.back.session.infrastructure.adapters.in.dto.SessionResponse;
import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.model.SubjectDomain;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import acainfo.back.user.application.ports.out.UserRepositoryPort;
import acainfo.back.user.domain.model.UserDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Mapper
 * Converts: SessionDomain ↔ DTOs
 *
 * Responsibility: Adapt domain for REST APIs
 */
@Component
@RequiredArgsConstructor
public class SessionDtoMapper {

    private final GroupRepositoryPort groupRepository;
    private final SubjectRepositoryPort subjectRepository;
    private final UserRepositoryPort userRepository;

    /**
     * Converts Domain → Response DTO (API output)
     * Enriches with related entity data (subject group, subject, teacher info)
     */
    public SessionResponse toResponse(SessionDomain domain) {
        if (domain == null) {
            return null;
        }

        SessionResponse.SessionResponseBuilder builder = SessionResponse.builder()
                .id(domain.getId())
                .subjectGroupId(domain.getSubjectGroupId())
                .generatedFromScheduleId(domain.getGeneratedFromScheduleId())
                .type(domain.getType())
                .scheduledStart(domain.getScheduledStart())
                .scheduledEnd(domain.getScheduledEnd())
                .actualStart(domain.getActualStart())
                .actualEnd(domain.getActualEnd())
                .mode(domain.getMode())
                .status(domain.getStatus())
                .classroom(domain.getClassroom())
                .zoomMeetingId(domain.getZoomMeetingId())
                .cancellationReason(domain.getCancellationReason())
                .postponementReason(domain.getPostponementReason())
                .originalSessionId(domain.getOriginalSessionId())
                .recoveryForSessionId(domain.getRecoveryForSessionId())
                .notes(domain.getNotes())
                .topicsCovered(domain.getTopicsCovered())
                .scheduledDurationMinutes(domain.getScheduledDurationInMinutes())
                .actualDurationMinutes(domain.getActualDurationInMinutes())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt());

        // Fetch and add subject group info
        if (domain.getSubjectGroupId() != null) {
            groupRepository.findById(domain.getSubjectGroupId()).ifPresent(group -> {
                builder.subjectGroupName(getGroupDisplayName(group));

                // Fetch subject info
                if (group.getSubjectId() != null) {
                    subjectRepository.findById(group.getSubjectId()).ifPresent(subject -> {
                        builder.subjectCode(subject.getCode());
                        builder.subjectName(subject.getName());
                    });
                }

                // Fetch teacher info
                if (group.getTeacherId() != null) {
                    userRepository.findById(group.getTeacherId()).ifPresent(teacher -> {
                        builder.teacherId(teacher.getId());
                        builder.teacherName(teacher.getFullName());
                    });
                }
            });
        }

        return builder.build();
    }

    /**
     * Converts list of Domain → Response
     */
    public List<SessionResponse> toResponses(List<SessionDomain> domains) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper to generate display name for subject group
     */
    private String getGroupDisplayName(SubjectGroupDomain group) {
        // Format: "SubjectCode - Period Type"
        StringBuilder name = new StringBuilder();

        // Fetch subject for code
        if (group.getSubjectId() != null) {
            subjectRepository.findById(group.getSubjectId()).ifPresent(subject ->
                    name.append(subject.getCode())
            );
        }

        if (name.length() > 0) {
            name.append(" - ");
        }

        name.append(group.getPeriod().getDisplayName())
                .append(" ")
                .append(group.getType().getDisplayName());

        return name.toString();
    }
}
