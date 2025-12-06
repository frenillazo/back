package com.acainfo.schedule.infrastructure.adapter.in.rest;

import com.acainfo.schedule.application.dto.ScheduleFilters;
import com.acainfo.schedule.application.port.in.CreateScheduleUseCase;
import com.acainfo.schedule.application.port.in.DeleteScheduleUseCase;
import com.acainfo.schedule.application.port.in.GetScheduleUseCase;
import com.acainfo.schedule.application.port.in.UpdateScheduleUseCase;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.schedule.infrastructure.adapter.in.rest.dto.CreateScheduleRequest;
import com.acainfo.schedule.infrastructure.adapter.in.rest.dto.ScheduleResponse;
import com.acainfo.schedule.infrastructure.adapter.in.rest.dto.UpdateScheduleRequest;
import com.acainfo.schedule.infrastructure.mapper.ScheduleRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

/**
 * REST Controller for Schedule management.
 * Endpoints: /api/schedules
 *
 * Security:
 * - GET (all, by id, by group): Authenticated users
 * - POST, PUT, DELETE: ADMIN only
 */
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final CreateScheduleUseCase createScheduleUseCase;
    private final UpdateScheduleUseCase updateScheduleUseCase;
    private final GetScheduleUseCase getScheduleUseCase;
    private final DeleteScheduleUseCase deleteScheduleUseCase;
    private final ScheduleRestMapper scheduleRestMapper;

    /**
     * Create a new schedule.
     * POST /api/schedules
     *
     * @param request CreateScheduleRequest
     * @return ScheduleResponse with 201 CREATED
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        log.info("REST: Creating schedule for group: {}, day: {}, time: {}-{}, classroom: {}",
                request.getGroupId(), request.getDayOfWeek(), request.getStartTime(),
                request.getEndTime(), request.getClassroom());

        Schedule createdSchedule = createScheduleUseCase.create(scheduleRestMapper.toCommand(request));
        ScheduleResponse response = scheduleRestMapper.toResponse(createdSchedule);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get schedule by ID.
     * GET /api/schedules/{id}
     *
     * @param id Schedule ID
     * @return ScheduleResponse with 200 OK
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getScheduleById(@PathVariable Long id) {
        log.debug("REST: Getting schedule by ID: {}", id);

        Schedule schedule = getScheduleUseCase.getById(id);
        ScheduleResponse response = scheduleRestMapper.toResponse(schedule);

        return ResponseEntity.ok(response);
    }

    /**
     * Get schedules with filters (pagination + sorting + filtering).
     * GET /api/schedules?groupId=1&classroom=AULA_PORTAL1&dayOfWeek=MONDAY&page=0&size=10
     *
     * @param groupId Filter by group ID (optional)
     * @param classroom Filter by classroom (optional)
     * @param dayOfWeek Filter by day of week (optional)
     * @param page Page number (default 0)
     * @param size Page size (default 20)
     * @param sortBy Sort field (default "dayOfWeek")
     * @param sortDirection Sort direction (default "ASC")
     * @return Page of ScheduleResponse with 200 OK
     */
    @GetMapping
    public ResponseEntity<Page<ScheduleResponse>> getSchedulesWithFilters(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Classroom classroom,
            @RequestParam(required = false) DayOfWeek dayOfWeek,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dayOfWeek") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        log.debug("REST: Getting schedules with filters - groupId: {}, classroom: {}, dayOfWeek: {}",
                groupId, classroom, dayOfWeek);

        ScheduleFilters filters = new ScheduleFilters(
                groupId,
                classroom,
                dayOfWeek,
                page,
                size,
                sortBy,
                sortDirection
        );

        Page<Schedule> schedulesPage = getScheduleUseCase.findWithFilters(filters);
        Page<ScheduleResponse> responsePage = schedulesPage.map(scheduleRestMapper::toResponse);

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Get all schedules for a specific group.
     * GET /api/schedules/group/{groupId}
     *
     * @param groupId Group ID
     * @return List of ScheduleResponse with 200 OK
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByGroup(@PathVariable Long groupId) {
        log.debug("REST: Getting schedules for group: {}", groupId);

        List<Schedule> schedules = getScheduleUseCase.findByGroupId(groupId);
        List<ScheduleResponse> responses = schedules.stream()
                .map(scheduleRestMapper::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Update schedule.
     * PUT /api/schedules/{id}
     *
     * @param id Schedule ID
     * @param request UpdateScheduleRequest
     * @return ScheduleResponse with 200 OK
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateScheduleRequest request
    ) {
        log.info("REST: Updating schedule ID: {}", id);

        Schedule updatedSchedule = updateScheduleUseCase.update(id, scheduleRestMapper.toCommand(request));
        ScheduleResponse response = scheduleRestMapper.toResponse(updatedSchedule);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete schedule by ID.
     * DELETE /api/schedules/{id}
     *
     * @param id Schedule ID
     * @return 204 NO CONTENT
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        log.info("REST: Deleting schedule ID: {}", id);

        deleteScheduleUseCase.delete(id);

        return ResponseEntity.noContent().build();
    }
}
