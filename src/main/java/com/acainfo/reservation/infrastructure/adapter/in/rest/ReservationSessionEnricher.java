package com.acainfo.reservation.infrastructure.adapter.in.rest;

import com.acainfo.course.application.port.in.GetCourseUseCase;
import com.acainfo.course.domain.model.Course;
import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.EnrichedReservationResponse;
import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.ReservationResponse;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.model.Session;
import com.acainfo.subject.application.port.in.GetSubjectUseCase;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enriches ReservationResponse with session, subject, group, and teacher data.
 * Used by the enriched student reservations endpoint.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationSessionEnricher {

    private final SessionRepositoryPort sessionRepositoryPort;
    private final GetSubjectUseCase getSubjectUseCase;
    private final GetCourseUseCase getCourseUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;

    /**
     * Enrich a list of reservation responses with session details.
     * Batch-fetches sessions, subjects, groups, and teachers to avoid N+1 queries.
     */
    public List<EnrichedReservationResponse> enrichWithSessionData(List<ReservationResponse> responses) {
        if (responses.isEmpty()) {
            return List.of();
        }

        // Collect unique session IDs and batch-fetch sessions
        Set<Long> sessionIds = responses.stream()
                .map(ReservationResponse::getSessionId)
                .collect(Collectors.toSet());

        Map<Long, Session> sessionsById = sessionIds.stream()
                .map(id -> sessionRepositoryPort.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Session::getId, Function.identity()));

        // Collect unique subject and group IDs from sessions
        Set<Long> subjectIds = sessionsById.values().stream()
                .map(Session::getSubjectId)
                .collect(Collectors.toSet());

        Set<Long> courseIds = sessionsById.values().stream()
                .map(Session::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Batch-fetch subjects
        Map<Long, Subject> subjectsById = subjectIds.stream()
                .map(getSubjectUseCase::getById)
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        // Batch-fetch groups
        Map<Long, Course> groupsById = courseIds.stream()
                .map(getCourseUseCase::getById)
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        // Batch-fetch teachers from groups
        Set<Long> teacherIds = groupsById.values().stream()
                .map(Course::getTeacherId)
                .collect(Collectors.toSet());

        Map<Long, User> teachersById = teacherIds.stream()
                .map(getUserProfileUseCase::getUserById)
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Build enriched responses
        return responses.stream()
                .map(r -> {
                    EnrichedReservationResponse enriched = EnrichedReservationResponse.from(r);

                    Session session = sessionsById.get(r.getSessionId());
                    if (session != null) {
                        enriched.setSessionDate(session.getDate());
                        enriched.setSessionStartTime(session.getStartTime());
                        enriched.setSessionEndTime(session.getEndTime());
                        enriched.setSessionStatus(session.getStatus().name());
                        enriched.setClassroom(session.getClassroom() != null ? session.getClassroom().name() : null);

                        Subject subject = subjectsById.get(session.getSubjectId());
                        if (subject != null) {
                            enriched.setSubjectName(subject.getName());
                            enriched.setSubjectCode(subject.getCode());
                        }

                        if (session.getCourseId() != null) {
                            Course group = groupsById.get(session.getCourseId());
                            if (group != null) {
                                User teacher = teachersById.get(group.getTeacherId());
                                if (teacher != null) {
                                    enriched.setTeacherName(teacher.getFullName());
                                }
                            }
                        }
                    }

                    return enriched;
                })
                .toList();
    }
}
