package com.acainfo.enrollment.infrastructure.adapter.in.rest;

import com.acainfo.enrollment.domain.model.GroupRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.GroupRequestResponse;
import com.acainfo.enrollment.infrastructure.mapper.GroupRequestRestMapper;
import com.acainfo.subject.application.port.in.GetSubjectUseCase;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Infrastructure service to enrich GroupRequestResponse with related entity data.
 * This service fetches data from other modules to build enriched responses,
 * reducing the number of API calls the frontend needs to make.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupRequestResponseEnricher {

    private final GroupRequestRestMapper groupRequestRestMapper;
    private final GetSubjectUseCase getSubjectUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;

    /**
     * Enrich a single group request with related entity data.
     *
     * @param groupRequest the group request to enrich
     * @return enriched group request response
     */
    public GroupRequestResponse enrich(GroupRequest groupRequest) {
        Subject subject = getSubjectUseCase.getById(groupRequest.getSubjectId());
        User requester = getUserProfileUseCase.getUserById(groupRequest.getRequesterId());

        GroupRequestResponse response = groupRequestRestMapper.toResponse(groupRequest);
        response.setSubjectName(subject.getName());
        response.setSubjectDegree(subject.getDegree().getDisplayName());
        response.setRequesterName(requester.getFullName());

        return response;
    }

    /**
     * Enrich a list of group requests with related entity data.
     * Optimized to batch-fetch related entities to minimize database queries.
     *
     * @param groupRequests the group requests to enrich
     * @return list of enriched group request responses
     */
    public List<GroupRequestResponse> enrichList(List<GroupRequest> groupRequests) {
        if (groupRequests.isEmpty()) {
            return List.of();
        }

        // Collect unique IDs
        Set<Long> subjectIds = groupRequests.stream()
                .map(GroupRequest::getSubjectId)
                .collect(Collectors.toSet());

        Set<Long> requesterIds = groupRequests.stream()
                .map(GroupRequest::getRequesterId)
                .collect(Collectors.toSet());

        // Fetch subjects
        Map<Long, Subject> subjectsById = subjectIds.stream()
                .map(getSubjectUseCase::getById)
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        // Fetch requesters
        Map<Long, User> usersById = requesterIds.stream()
                .map(getUserProfileUseCase::getUserById)
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Build enriched responses
        return groupRequests.stream()
                .map(groupRequest -> {
                    Subject subject = subjectsById.get(groupRequest.getSubjectId());
                    User requester = usersById.get(groupRequest.getRequesterId());

                    GroupRequestResponse response = groupRequestRestMapper.toResponse(groupRequest);
                    response.setSubjectName(subject.getName());
                    response.setSubjectDegree(subject.getDegree().getDisplayName());
                    response.setRequesterName(requester.getFullName());

                    return response;
                })
                .toList();
    }

    /**
     * Enrich a page of group requests with related entity data.
     *
     * @param groupRequestsPage the page of group requests to enrich
     * @return page of enriched group request responses
     */
    public Page<GroupRequestResponse> enrichPage(Page<GroupRequest> groupRequestsPage) {
        List<GroupRequestResponse> enrichedList = enrichList(groupRequestsPage.getContent());

        // Create a map for quick lookup
        Map<Long, GroupRequestResponse> enrichedById = enrichedList.stream()
                .collect(Collectors.toMap(GroupRequestResponse::getId, Function.identity()));

        return groupRequestsPage.map(groupRequest -> enrichedById.get(groupRequest.getId()));
    }
}
