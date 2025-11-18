package acainfo.back.schedule.infrastructure.adapters.in.rest;

import acainfo.back.schedule.application.ports.in.GetScheduleUseCase;
import acainfo.back.schedule.application.ports.in.ManageScheduleUseCase;
import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import acainfo.back.schedule.domain.model.Schedule;
import acainfo.back.schedule.infrastructure.adapters.in.dto.CreateScheduleRequest;
import acainfo.back.schedule.infrastructure.adapters.in.dto.ScheduleResponse;
import acainfo.back.schedule.infrastructure.adapters.in.dto.UpdateScheduleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing schedules.
 * Provides endpoints for CRUD operations and queries on schedules.
 */
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schedule Management", description = "Endpoints for managing class schedules")
public class ScheduleController {

    private final ManageScheduleUseCase manageScheduleUseCase;
    private final GetScheduleUseCase getScheduleUseCase;

    /**
     * Creates a new schedule.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(
            summary = "Create a new schedule",
            description = "Creates a new weekly time slot for a subjectGroup. Validates conflicts with teachers and classrooms.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Schedule created successfully",
                            content = @Content(schema = @Schema(implementation = ScheduleResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "404", description = "SubjectGroup not found"),
                    @ApiResponse(responseCode = "409", description = "Schedule conflict detected")
            }
    )
    public ResponseEntity<ScheduleResponse> createSchedule(
            @Valid @RequestBody CreateScheduleRequest request
    ) {
        log.info("Creating schedule for subjectGroup ID: {}", request.getGroupId());

        Schedule schedule = Schedule.builder()
                .subjectGroup(SubjectGroup.builder().id(request.getGroupId()).build())
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .classroom(request.getClassroom())
                .build();

        Schedule createdSchedule = manageScheduleUseCase.createSchedule(schedule);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ScheduleResponse.fromEntity(createdSchedule));
    }

    /**
     * Gets all schedules with optional filters.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    @Operation(
            summary = "Get all schedules",
            description = "Retrieves all schedules with optional filtering by subjectGroup, teacher, classroom, day, or subject.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Schedules retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ScheduleResponse.class))))
            }
    )
    public ResponseEntity<List<ScheduleResponse>> getAllSchedules(
            @Parameter(description = "Filter by subjectGroup ID") @RequestParam(required = false) Long groupId,
            @Parameter(description = "Filter by teacher ID") @RequestParam(required = false) Long teacherId,
            @Parameter(description = "Filter by classroom") @RequestParam(required = false) Classroom classroom,
            @Parameter(description = "Filter by day of week") @RequestParam(required = false) DayOfWeek dayOfWeek,
            @Parameter(description = "Filter by subject ID") @RequestParam(required = false) Long subjectId
    ) {
        log.debug("Fetching schedules with filters - groupId: {}, teacherId: {}, classroom: {}, dayOfWeek: {}, subjectId: {}",
                groupId, teacherId, classroom, dayOfWeek, subjectId);

        List<Schedule> schedules;

        // Apply filters
        if (groupId != null) {
            schedules = getScheduleUseCase.getSchedulesByGroupId(groupId);
        } else if (teacherId != null) {
            schedules = getScheduleUseCase.getSchedulesByTeacherId(teacherId);
        } else if (classroom != null) {
            schedules = getScheduleUseCase.getSchedulesByClassroom(classroom);
        } else if (dayOfWeek != null) {
            schedules = getScheduleUseCase.getSchedulesByDayOfWeek(dayOfWeek);
        } else if (subjectId != null) {
            schedules = getScheduleUseCase.getSchedulesBySubjectId(subjectId);
        } else {
            schedules = getScheduleUseCase.getAllSchedules();
        }

        List<ScheduleResponse> response = schedules.stream()
                .map(ScheduleResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Gets a schedule by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    @Operation(
            summary = "Get schedule by ID",
            description = "Retrieves a specific schedule by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Schedule retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ScheduleResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Schedule not found")
            }
    )
    public ResponseEntity<ScheduleResponse> getScheduleById(
            @Parameter(description = "Schedule ID") @PathVariable Long id
    ) {
        log.debug("Fetching schedule with ID: {}", id);

        Schedule schedule = getScheduleUseCase.getScheduleById(id);

        return ResponseEntity.ok(ScheduleResponse.fromEntity(schedule));
    }

    /**
     * Gets all schedules for a specific subjectGroup.
     */
    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    @Operation(
            summary = "Get schedules by subjectGroup",
            description = "Retrieves all schedules for a specific subjectGroup.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Schedules retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ScheduleResponse.class)))),
                    @ApiResponse(responseCode = "404", description = "SubjectGroup not found")
            }
    )
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByGroup(
            @Parameter(description = "SubjectGroup ID") @PathVariable Long groupId
    ) {
        log.debug("Fetching schedules for subjectGroup ID: {}", groupId);

        List<Schedule> schedules = getScheduleUseCase.getSchedulesByGroupId(groupId);

        List<ScheduleResponse> response = schedules.stream()
                .map(ScheduleResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Gets all schedules for a specific teacher.
     */
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(
            summary = "Get schedules by teacher",
            description = "Retrieves all schedules for a specific teacher.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Schedules retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ScheduleResponse.class))))
            }
    )
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByTeacher(
            @Parameter(description = "Teacher ID") @PathVariable Long teacherId
    ) {
        log.debug("Fetching schedules for teacher ID: {}", teacherId);

        List<Schedule> schedules = getScheduleUseCase.getSchedulesByTeacherId(teacherId);

        List<ScheduleResponse> response = schedules.stream()
                .map(ScheduleResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Gets all schedules for a specific classroom.
     */
    @GetMapping("/classroom/{classroom}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(
            summary = "Get schedules by classroom",
            description = "Retrieves all schedules for a specific classroom.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Schedules retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ScheduleResponse.class))))
            }
    )
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByClassroom(
            @Parameter(description = "Classroom") @PathVariable Classroom classroom
    ) {
        log.debug("Fetching schedules for classroom: {}", classroom);

        List<Schedule> schedules = getScheduleUseCase.getSchedulesByClassroom(classroom);

        List<ScheduleResponse> response = schedules.stream()
                .map(ScheduleResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing schedule.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(
            summary = "Update a schedule",
            description = "Updates an existing schedule. All fields are optional. Validates conflicts.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Schedule updated successfully",
                            content = @Content(schema = @Schema(implementation = ScheduleResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "404", description = "Schedule not found"),
                    @ApiResponse(responseCode = "409", description = "Schedule conflict detected")
            }
    )
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @Parameter(description = "Schedule ID") @PathVariable Long id,
            @Valid @RequestBody UpdateScheduleRequest request
    ) {
        log.info("Updating schedule with ID: {}", id);

        Schedule schedule = Schedule.builder()
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .classroom(request.getClassroom())
                .build();

        Schedule updatedSchedule = manageScheduleUseCase.updateSchedule(id, schedule);

        return ResponseEntity.ok(ScheduleResponse.fromEntity(updatedSchedule));
    }

    /**
     * Deletes a schedule.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a schedule",
            description = "Deletes a schedule by its ID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Schedule deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Schedule not found")
            }
    )
    public ResponseEntity<Void> deleteSchedule(
            @Parameter(description = "Schedule ID") @PathVariable Long id
    ) {
        log.info("Deleting schedule with ID: {}", id);

        manageScheduleUseCase.deleteSchedule(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes all schedules for a subjectGroup.
     */
    @DeleteMapping("/group/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete all schedules for a subjectGroup",
            description = "Deletes all schedules associated with a specific subjectGroup.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Schedules deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "SubjectGroup not found")
            }
    )
    public ResponseEntity<Void> deleteSchedulesByGroup(
            @Parameter(description = "SubjectGroup ID") @PathVariable Long groupId
    ) {
        log.info("Deleting all schedules for subjectGroup ID: {}", groupId);

        manageScheduleUseCase.deleteSchedulesByGroupId(groupId);

        return ResponseEntity.noContent().build();
    }
}
