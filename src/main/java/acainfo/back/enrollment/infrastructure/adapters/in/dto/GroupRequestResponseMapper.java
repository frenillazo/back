package acainfo.back.enrollment.infrastructure.adapters.in.dto;

import acainfo.back.enrollment.domain.model.GroupRequestDomain;
import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.model.SubjectDomain;
import acainfo.back.user.application.ports.out.UserRepositoryPort;
import acainfo.back.user.domain.model.UserDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper to convert GroupRequestDomain to GroupRequestResponse DTO.
 * Fetches related user and subject information when needed.
 */
@Component
@RequiredArgsConstructor
public class GroupRequestResponseMapper {

    private final UserRepositoryPort userRepository;
    private final SubjectRepositoryPort subjectRepository;

    public GroupRequestResponse toResponse(GroupRequestDomain request) {
        if (request == null) {
            return null;
        }

        GroupRequestResponse.GroupRequestResponseBuilder builder = GroupRequestResponse.builder()
                .id(request.getId())
                .status(request.getStatus())
                .supportersCount(request.getSupportersCount())
                .minimumSupporters(GroupRequestDomain.MINIMUM_SUPPORTERS)
                .supportersNeeded(request.getSupportersNeeded())
                .supportersProgress(request.getSupportersProgress())
                .hasMinimumSupporters(request.hasMinimumSupporters())
                .requestedAt(request.getRequestedAt())
                .resolvedAt(request.getResolvedAt())
                .rejectionReason(request.getRejectionReason())
                .comments(request.getComments());

        // Fetch and add subject info
        if (request.getSubjectId() != null) {
            subjectRepository.findById(request.getSubjectId())
                    .ifPresent(subject -> builder.subject(
                            GroupRequestResponse.SubjectBasicInfo.builder()
                                    .id(subject.getId())
                                    .code(subject.getCode())
                                    .name(subject.getName())
                                    .year(subject.getYear())
                                    .build()
                    ));
        }

        // Fetch and add requester info
        if (request.getRequestedById() != null) {
            userRepository.findById(request.getRequestedById())
                    .ifPresent(requester -> builder.requestedBy(
                            GroupRequestResponse.StudentBasicInfo.builder()
                                    .id(requester.getId())
                                    .email(requester.getEmail())
                                    .firstName(requester.getFirstName())
                                    .lastName(requester.getLastName())
                                    .fullName(requester.getFullName())
                                    .build()
                    ));
        }

        // Fetch and add supporters list
        if (request.getSupporterIds() != null && !request.getSupporterIds().isEmpty()) {
            List<UserDomain> supporters = request.getSupporterIds().stream()
                    .map(id -> userRepository.findById(id).orElse(null))
                    .filter(user -> user != null)
                    .collect(Collectors.toList());

            List<GroupRequestResponse.StudentBasicInfo> supportersList = supporters.stream()
                    .map(supporter -> GroupRequestResponse.StudentBasicInfo.builder()
                            .id(supporter.getId())
                            .email(supporter.getEmail())
                            .firstName(supporter.getFirstName())
                            .lastName(supporter.getLastName())
                            .fullName(supporter.getFullName())
                            .build())
                    .collect(Collectors.toList());

            builder.supporters(supportersList);
        }

        return builder.build();
    }
}
