package com.acainfo.course.infrastructure.adapter.in.rest;

import com.acainfo.enrollment.application.port.in.GetEnrollmentUseCase;
import com.acainfo.course.domain.model.Course;
import com.acainfo.course.infrastructure.adapter.in.rest.dto.CourseResponse;
import com.acainfo.course.infrastructure.adapter.in.rest.dto.ScheduleSummary;
import com.acainfo.course.infrastructure.mapper.CourseRestMapper;
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
 * Infrastructure service to enrich CourseResponse with related entity data.
 * This service fetches data from other modules to build enriched responses,
 * reducing the number of API calls the frontend needs to make.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CourseResponseEnricher {

    private final CourseRestMapper courseRestMapper;
    private final GetSubjectUseCase getSubjectUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final GetScheduleUseCase getScheduleUseCase;
    private final GetEnrollmentUseCase getEnrollmentUseCase;

    /**
     * Enrich a single group with related entity data.
     *
     * @param group the group to enrich
     * @return enriched group response
     */
    public CourseResponse enrich(Course course) {
        Subject subject = getSubjectUseCase.getById(course.getSubjectId());
        String teacherName = resolveTeacherName(course.getTeacherId());
        List<ScheduleSummary> schedules = getScheduleSummaries(course.getId());

        CourseResponse response = courseRestMapper.toEnrichedResponse(
                course,
                subject.getName(),
                subject.getCode(),
                teacherName
        );
        response.setSchedules(schedules);

        // Dynamic enrollment count — single source of truth
        applyDynamicEnrollmentCount(response, course);

        return response;
    }

    /** Teacher is optional on a course; resolve to a display name or null. */
    private String resolveTeacherName(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        return getUserProfileUseCase.getUserById(teacherId).getFullName();
    }

    /**
     * Get schedule summaries for a group.
     *
     * @param courseId the group ID
     * @return list of schedule summaries sorted by day of week
     */
    private List<ScheduleSummary> getScheduleSummaries(Long courseId) {
        return getScheduleUseCase.findByCourseId(courseId).stream()
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
    public List<CourseResponse> enrichList(List<Course> groups) {
        if (groups.isEmpty()) {
            return List.of();
        }

        // Collect unique IDs
        Set<Long> subjectIds = groups.stream()
                .map(Course::getSubjectId)
                .collect(Collectors.toSet());

        Set<Long> teacherIds = groups.stream()
                .map(Course::getTeacherId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Set<Long> courseIds = groups.stream()
                .map(Course::getId)
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
        Map<Long, List<ScheduleSummary>> schedulesByCourseId = courseIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::getScheduleSummaries
                ));

        // Build enriched responses
        return groups.stream()
                .map(group -> {
                    Subject subject = subjectsById.get(group.getSubjectId());
                    User teacher = group.getTeacherId() != null ? teachersById.get(group.getTeacherId()) : null;

                    CourseResponse response = courseRestMapper.toEnrichedResponse(
                            group,
                            subject.getName(),
                            subject.getCode(),
                            teacher != null ? teacher.getFullName() : null
                    );
                    response.setSchedules(schedulesByCourseId.getOrDefault(group.getId(), List.of()));

                    // Dynamic enrollment count — single source of truth
                    applyDynamicEnrollmentCount(response, group);

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
    /**
     * Override currentEnrollmentCount with a dynamic count from the enrollment table.
     * This ensures the count is always accurate, regardless of the stored field.
     */
    private void applyDynamicEnrollmentCount(CourseResponse response, Course course) {
        int activeCount = (int) getEnrollmentUseCase.countActiveByCourseId(course.getId());
        response.setCurrentEnrollmentCount(activeCount);
        if (course.hasCapacityLimit()) {
            response.setAvailableSeats(Math.max(0, course.getCapacity() - activeCount));
            response.setCanEnroll(course.isOpen() && activeCount < course.getCapacity());
        } else {
            // Unlimited course (virtual/dual): no seat cap, always enrollable while OPEN
            response.setAvailableSeats(null);
            response.setCanEnroll(course.isOpen());
        }
    }

    public Page<CourseResponse> enrichPage(Page<Course> groupsPage) {
        List<CourseResponse> enrichedList = enrichList(groupsPage.getContent());

        // Create a map for quick lookup
        Map<Long, CourseResponse> enrichedById = enrichedList.stream()
                .collect(Collectors.toMap(CourseResponse::getId, Function.identity()));

        return groupsPage.map(group -> enrichedById.get(group.getId()));
    }
}
