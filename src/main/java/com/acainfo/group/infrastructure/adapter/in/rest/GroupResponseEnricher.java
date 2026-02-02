package com.acainfo.group.infrastructure.adapter.in.rest;

import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.group.infrastructure.adapter.in.rest.dto.GroupResponse;
import com.acainfo.group.infrastructure.adapter.in.rest.dto.ScheduleSummary;
import com.acainfo.group.infrastructure.mapper.GroupRestMapper;
import com.acainfo.schedule.application.port.in.GetScheduleUseCase;
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
 * Infrastructure service to enrich GroupResponse with related entity data.
 * This service fetches data from other modules to build enriched responses,
 * reducing the number of API calls the frontend needs to make.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupResponseEnricher {

    private final GroupRestMapper groupRestMapper;
    private final GetSubjectUseCase getSubjectUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final GetScheduleUseCase getScheduleUseCase;

    /**
     * Enrich a single group with related entity data.
     *
     * @param group the group to enrich
     * @return enriched group response
     */
    public GroupResponse enrich(SubjectGroup group) {
        Subject subject = getSubjectUseCase.getById(group.getSubjectId());
        User teacher = getUserProfileUseCase.getUserById(group.getTeacherId());
        List<ScheduleSummary> schedules = getScheduleSummaries(group.getId());

        GroupResponse response = groupRestMapper.toEnrichedResponse(
                group,
                subject.getName(),
                subject.getCode(),
                teacher.getFullName()
        );
        response.setSchedules(schedules);
        return response;
    }

    /**
     * Get schedule summaries for a group.
     *
     * @param groupId the group ID
     * @return list of schedule summaries sorted by day of week
     */
    private List<ScheduleSummary> getScheduleSummaries(Long groupId) {
        return getScheduleUseCase.findByGroupId(groupId).stream()
                .map(schedule -> ScheduleSummary.builder()
                        .dayOfWeek(schedule.getDayOfWeek())
                        .startTime(schedule.getStartTime())
                        .endTime(schedule.getEndTime())
                        .build())
                .sorted((a, b) -> a.getDayOfWeek().compareTo(b.getDayOfWeek()))
                .toList();
    }

    /**
     * Enrich a list of groups with related entity data.
     * Optimized to batch-fetch related entities to minimize database queries.
     *
     * @param groups the groups to enrich
     * @return list of enriched group responses
     */
    public List<GroupResponse> enrichList(List<SubjectGroup> groups) {
        if (groups.isEmpty()) {
            return List.of();
        }

        // Collect unique IDs
        Set<Long> subjectIds = groups.stream()
                .map(SubjectGroup::getSubjectId)
                .collect(Collectors.toSet());

        Set<Long> teacherIds = groups.stream()
                .map(SubjectGroup::getTeacherId)
                .collect(Collectors.toSet());

        Set<Long> groupIds = groups.stream()
                .map(SubjectGroup::getId)
                .collect(Collectors.toSet());

        // Fetch subjects
        Map<Long, Subject> subjectsById = subjectIds.stream()
                .map(getSubjectUseCase::getById)
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        // Fetch teachers
        Map<Long, User> teachersById = teacherIds.stream()
                .map(getUserProfileUseCase::getUserById)
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Fetch schedules for all groups
        Map<Long, List<ScheduleSummary>> schedulesByGroupId = groupIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::getScheduleSummaries
                ));

        // Build enriched responses
        return groups.stream()
                .map(group -> {
                    Subject subject = subjectsById.get(group.getSubjectId());
                    User teacher = teachersById.get(group.getTeacherId());

                    GroupResponse response = groupRestMapper.toEnrichedResponse(
                            group,
                            subject.getName(),
                            subject.getCode(),
                            teacher.getFullName()
                    );
                    response.setSchedules(schedulesByGroupId.getOrDefault(group.getId(), List.of()));
                    return response;
                })
                .toList();
    }

    /**
     * Enrich a page of groups with related entity data.
     *
     * @param groupsPage the page of groups to enrich
     * @return page of enriched group responses
     */
    public Page<GroupResponse> enrichPage(Page<SubjectGroup> groupsPage) {
        List<GroupResponse> enrichedList = enrichList(groupsPage.getContent());

        // Create a map for quick lookup
        Map<Long, GroupResponse> enrichedById = enrichedList.stream()
                .collect(Collectors.toMap(GroupResponse::getId, Function.identity()));

        return groupsPage.map(group -> enrichedById.get(group.getId()));
    }
}
