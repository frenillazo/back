package com.acainfo.session.infrastructure.adapter.in.rest;

import com.acainfo.group.application.port.in.GetGroupUseCase;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.SessionResponse;
import com.acainfo.session.infrastructure.mapper.SessionRestMapper;
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
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Infrastructure service to enrich SessionResponse with related entity data.
 * This service fetches data from other modules to build enriched responses,
 * reducing the number of API calls the frontend needs to make.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionResponseEnricher {

    private final SessionRestMapper sessionRestMapper;
    private final GetSubjectUseCase getSubjectUseCase;
    private final GetGroupUseCase getGroupUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;

    /**
     * Enrich a single session with related entity data.
     *
     * @param session the session to enrich
     * @return enriched session response
     */
    public SessionResponse enrich(Session session) {
        Subject subject = getSubjectUseCase.getById(session.getSubjectId());

        String groupName = null;
        String groupType = null;
        String teacherName = null;

        // Sessions may not have a group (e.g., SCHEDULING type sessions)
        if (session.getGroupId() != null) {
            SubjectGroup group = getGroupUseCase.getById(session.getGroupId());
            groupName = group.getName();
            groupType = group.getType().name();
            User teacher = getUserProfileUseCase.getUserById(group.getTeacherId());
            teacherName = teacher.getFullName();
        }

        return sessionRestMapper.toEnrichedResponse(
                session,
                subject.getName(),
                subject.getCode(),
                groupName,
                groupType,
                teacherName
        );
    }

    /**
     * Enrich a list of sessions with related entity data.
     * Optimized to batch-fetch related entities to minimize database queries.
     *
     * @param sessions the sessions to enrich
     * @return list of enriched session responses
     */
    public List<SessionResponse> enrichList(List<Session> sessions) {
        if (sessions.isEmpty()) {
            return List.of();
        }

        // Collect unique IDs
        Set<Long> subjectIds = sessions.stream()
                .map(Session::getSubjectId)
                .collect(Collectors.toSet());

        Set<Long> groupIds = sessions.stream()
                .map(Session::getGroupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Fetch subjects
        Map<Long, Subject> subjectsById = subjectIds.stream()
                .map(getSubjectUseCase::getById)
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        // Fetch groups (only for sessions that have a group)
        Map<Long, SubjectGroup> groupsById = groupIds.stream()
                .map(getGroupUseCase::getById)
                .collect(Collectors.toMap(SubjectGroup::getId, Function.identity()));

        // Collect teacher IDs from groups
        Set<Long> teacherIds = groupsById.values().stream()
                .map(SubjectGroup::getTeacherId)
                .collect(Collectors.toSet());

        // Fetch teachers
        Map<Long, User> teachersById = teacherIds.stream()
                .map(getUserProfileUseCase::getUserById)
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Build enriched responses
        return sessions.stream()
                .map(session -> {
                    Subject subject = subjectsById.get(session.getSubjectId());

                    String groupName = null;
                    String groupType = null;
                    String teacherName = null;

                    if (session.getGroupId() != null) {
                        SubjectGroup group = groupsById.get(session.getGroupId());
                        if (group != null) {
                            groupName = group.getName();
                            groupType = group.getType().name();
                            User teacher = teachersById.get(group.getTeacherId());
                            if (teacher != null) {
                                teacherName = teacher.getFullName();
                            }
                        }
                    }

                    return sessionRestMapper.toEnrichedResponse(
                            session,
                            subject.getName(),
                            subject.getCode(),
                            groupName,
                            groupType,
                            teacherName
                    );
                })
                .toList();
    }

    /**
     * Enrich a page of sessions with related entity data.
     *
     * @param sessionsPage the page of sessions to enrich
     * @return page of enriched session responses
     */
    public Page<SessionResponse> enrichPage(Page<Session> sessionsPage) {
        List<SessionResponse> enrichedList = enrichList(sessionsPage.getContent());

        // Create a map for quick lookup
        Map<Long, SessionResponse> enrichedById = enrichedList.stream()
                .collect(Collectors.toMap(SessionResponse::getId, Function.identity()));

        return sessionsPage.map(session -> enrichedById.get(session.getId()));
    }
}
