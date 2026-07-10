package com.acainfo.course.infrastructure.adapter.in.rest;

import com.acainfo.course.application.dto.CourseFilters;
import com.acainfo.course.application.port.in.CreateCourseUseCase;
import com.acainfo.course.application.port.in.DeleteCourseUseCase;
import com.acainfo.course.application.port.in.GetCourseUseCase;
import com.acainfo.course.application.port.in.UpdateCourseUseCase;
import com.acainfo.course.domain.model.CourseStatus;
import com.acainfo.course.domain.model.Course;
import com.acainfo.course.infrastructure.adapter.in.rest.dto.CreateCourseRequest;
import com.acainfo.course.infrastructure.adapter.in.rest.dto.CourseResponse;
import com.acainfo.course.infrastructure.adapter.in.rest.dto.UpdateCourseRequest;
import com.acainfo.course.infrastructure.mapper.CourseRestMapper;
import com.acainfo.shared.application.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Course management.
 * Endpoints: /api/courses
 *
 * Security:
 * - GET (all, by id): Authenticated users
 * - POST, PUT, DELETE: ADMIN only
 *
 * All responses are enriched with related entity data (subject name, teacher name, etc.).
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CreateCourseUseCase createCourseUseCase;
    private final UpdateCourseUseCase updateCourseUseCase;
    private final GetCourseUseCase getCourseUseCase;
    private final DeleteCourseUseCase deleteCourseUseCase;
    private final CourseRestMapper courseRestMapper;
    private final CourseResponseEnricher courseResponseEnricher;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> createGroup(@Valid @RequestBody CreateCourseRequest request) {
        log.info("REST: Creating group for subject: {}, teacher: {}",
                request.getSubjectId(), request.getTeacherId());

        Course createdGroup = createCourseUseCase.create(courseRestMapper.toCommand(request));
        CourseResponse response = courseResponseEnricher.enrich(createdGroup);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getGroupById(@PathVariable Long id) {
        log.debug("REST: Getting group by ID: {}", id);

        Course group = getCourseUseCase.getById(id);
        CourseResponse response = courseResponseEnricher.enrich(group);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/courses?subjectId=1&teacherId=2&status=OPEN&page=0&size=10&sortBy=createdAt&sortDirection=DESC
     */
    @GetMapping
    public ResponseEntity<PageResponse<CourseResponse>> getGroupsWithFilters(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.debug("REST: Getting groups with filters - subjectId: {}, teacherId: {}, status: {}, searchTerm: {}",
                subjectId, teacherId, status, searchTerm);

        CourseFilters filters = new CourseFilters(
                subjectId,
                teacherId,
                status,
                searchTerm,
                page,
                size,
                sortBy,
                sortDirection
        );

        Page<Course> groupsPage = getCourseUseCase.findWithFilters(filters);
        Page<CourseResponse> responsePage = courseResponseEnricher.enrichPage(groupsPage);

        return ResponseEntity.ok(PageResponse.of(responsePage));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request
    ) {
        log.info("REST: Updating group ID: {}", id);

        Course updatedGroup = updateCourseUseCase.update(id, courseRestMapper.toCommand(request));
        CourseResponse response = courseResponseEnricher.enrich(updatedGroup);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        log.info("REST: Deleting group ID: {}", id);

        deleteCourseUseCase.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> cancelGroup(@PathVariable Long id) {
        log.info("REST: Cancelling group ID: {}", id);

        Course cancelledGroup = deleteCourseUseCase.cancel(id);
        CourseResponse response = courseResponseEnricher.enrich(cancelledGroup);

        return ResponseEntity.ok(response);
    }
}
