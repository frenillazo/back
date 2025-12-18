package com.acainfo.enrollment.infrastructure.adapter.in.rest;

import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.EnrollmentResponse;
import com.acainfo.enrollment.infrastructure.mapper.EnrollmentRestMapper;
import com.acainfo.group.application.port.in.GetGroupUseCase;
import com.acainfo.group.domain.model.SubjectGroup;
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
 * Infrastructure service to enrich EnrollmentResponse with related entity data.
 * This service fetches data from other modules to build enriched responses,
 * reducing the number of API calls the frontend needs to make.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentResponseEnricher {

    private final EnrollmentRestMapper enrollmentRestMapper;
    private final GetGroupUseCase getGroupUseCase;
    private final GetSubjectUseCase getSubjectUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;

    /**
     * Enrich a single enrollment with related entity data.
     *
     * @param enrollment the enrollment to enrich
     * @return enriched enrollment response
     */
    public EnrollmentResponse enrich(Enrollment enrollment) {
        // Fetch related entities
        SubjectGroup group = getGroupUseCase.getById(enrollment.getGroupId());
        Subject subject = getSubjectUseCase.getById(group.getSubjectId());
        User student = getUserProfileUseCase.getUserById(enrollment.getStudentId());
        User teacher = getUserProfileUseCase.getUserById(group.getTeacherId());

        return enrollmentRestMapper.toEnrichedResponse(
                enrollment,
                student.getFullName(),
                subject.getName(),
                subject.getCode(),
                group.getType().name(),
                teacher.getFullName()
        );
    }

    /**
     * Enrich a list of enrollments with related entity data.
     * Optimized to batch-fetch related entities to minimize database queries.
     *
     * @param enrollments the enrollments to enrich
     * @return list of enriched enrollment responses
     */
    public List<EnrollmentResponse> enrichList(List<Enrollment> enrollments) {
        if (enrollments.isEmpty()) {
            return List.of();
        }

        // Collect unique IDs
        Set<Long> groupIds = enrollments.stream()
                .map(Enrollment::getGroupId)
                .collect(Collectors.toSet());

        Set<Long> studentIds = enrollments.stream()
                .map(Enrollment::getStudentId)
                .collect(Collectors.toSet());

        // Fetch groups and collect subject/teacher IDs
        Map<Long, SubjectGroup> groupsById = groupIds.stream()
                .map(getGroupUseCase::getById)
                .collect(Collectors.toMap(SubjectGroup::getId, Function.identity()));

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

        // Fetch all users (students + teachers)
        Set<Long> allUserIds = new java.util.HashSet<>(studentIds);
        allUserIds.addAll(teacherIds);
        Map<Long, User> usersById = allUserIds.stream()
                .map(getUserProfileUseCase::getUserById)
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Build enriched responses
        return enrollments.stream()
                .map(enrollment -> {
                    SubjectGroup group = groupsById.get(enrollment.getGroupId());
                    Subject subject = subjectsById.get(group.getSubjectId());
                    User student = usersById.get(enrollment.getStudentId());
                    User teacher = usersById.get(group.getTeacherId());

                    return enrollmentRestMapper.toEnrichedResponse(
                            enrollment,
                            student.getFullName(),
                            subject.getName(),
                            subject.getCode(),
                            group.getType().name(),
                            teacher.getFullName()
                    );
                })
                .toList();
    }

    /**
     * Enrich a page of enrollments with related entity data.
     *
     * @param enrollmentsPage the page of enrollments to enrich
     * @return page of enriched enrollment responses
     */
    public Page<EnrollmentResponse> enrichPage(Page<Enrollment> enrollmentsPage) {
        List<EnrollmentResponse> enrichedList = enrichList(enrollmentsPage.getContent());

        // Create a map for quick lookup
        Map<Long, EnrollmentResponse> enrichedById = enrichedList.stream()
                .collect(Collectors.toMap(EnrollmentResponse::getId, Function.identity()));

        return enrollmentsPage.map(enrollment -> enrichedById.get(enrollment.getId()));
    }
}
