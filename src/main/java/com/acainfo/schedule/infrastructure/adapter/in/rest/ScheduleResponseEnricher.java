package com.acainfo.schedule.infrastructure.adapter.in.rest;

import com.acainfo.group.application.port.in.GetGroupUseCase;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.schedule.infrastructure.adapter.in.rest.dto.ScheduleEnrichedResponse;
import com.acainfo.schedule.infrastructure.mapper.ScheduleRestMapper;
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
 * Infrastructure service to enrich ScheduleResponse with related entity data.
 * This service fetches data from other modules to build enriched responses,
 * reducing the number of API calls the frontend needs to make.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleResponseEnricher {

    private final ScheduleRestMapper scheduleRestMapper;
    private final GetGroupUseCase getGroupUseCase;
    private final GetSubjectUseCase getSubjectUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;

    /**
     * Enrich a single schedule with related entity data.
     *
     * @param schedule the schedule to enrich
     * @return enriched schedule response
     */
    public ScheduleEnrichedResponse enrich(Schedule schedule) {
        SubjectGroup group = getGroupUseCase.getById(schedule.getGroupId());
        Subject subject = getSubjectUseCase.getById(group.getSubjectId());
        User teacher = getUserProfileUseCase.getUserById(group.getTeacherId());

        return scheduleRestMapper.toEnrichedResponse(
                schedule,
                group,
                subject.getName(),
                subject.getCode(),
                teacher.getFullName()
        );
    }

    /**
     * Enrich a list of schedules with related entity data.
     * Optimized to batch-fetch related entities to minimize database queries.
     *
     * @param schedules the schedules to enrich
     * @return list of enriched schedule responses
     */
    public List<ScheduleEnrichedResponse> enrichList(List<Schedule> schedules) {
        if (schedules.isEmpty()) {
            return List.of();
        }

        // Collect unique group IDs
        Set<Long> groupIds = schedules.stream()
                .map(Schedule::getGroupId)
                .collect(Collectors.toSet());

        // Fetch groups
        Map<Long, SubjectGroup> groupsById = groupIds.stream()
                .map(getGroupUseCase::getById)
                .collect(Collectors.toMap(SubjectGroup::getId, Function.identity()));

        // Collect unique subject and teacher IDs from groups
        Set<Long> subjectIds = groupsById.values().stream()
                .map(SubjectGroup::getSubjectId)
                .collect(Collectors.toSet());

        Set<Long> teacherIds = groupsById.values().stream()
                .map(SubjectGroup::getTeacherId)
                .collect(Collectors.toSet());

        // Fetch subjects
        Map<Long, Subject> subjectsById = subjectIds.stream()
                .map(getSubjectUseCase::getById)
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        // Fetch teachers
        Map<Long, User> teachersById = teacherIds.stream()
                .map(getUserProfileUseCase::getUserById)
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Build enriched responses
        return schedules.stream()
                .map(schedule -> {
                    SubjectGroup group = groupsById.get(schedule.getGroupId());
                    Subject subject = subjectsById.get(group.getSubjectId());
                    User teacher = teachersById.get(group.getTeacherId());

                    return scheduleRestMapper.toEnrichedResponse(
                            schedule,
                            group,
                            subject.getName(),
                            subject.getCode(),
                            teacher.getFullName()
                    );
                })
                .toList();
    }

    /**
     * Enrich a page of schedules with related entity data.
     *
     * @param schedulesPage the page of schedules to enrich
     * @return page of enriched schedule responses
     */
    public Page<ScheduleEnrichedResponse> enrichPage(Page<Schedule> schedulesPage) {
        List<ScheduleEnrichedResponse> enrichedList = enrichList(schedulesPage.getContent());

        // Create a map for quick lookup
        Map<Long, ScheduleEnrichedResponse> enrichedById = enrichedList.stream()
                .collect(Collectors.toMap(ScheduleEnrichedResponse::getId, Function.identity()));

        return schedulesPage.map(schedule -> enrichedById.get(schedule.getId()));
    }
}
