package acainfo.back.application.services;

import acainfo.back.application.ports.in.GetScheduleUseCase;
import acainfo.back.application.ports.in.ManageScheduleUseCase;
import acainfo.back.application.ports.out.GroupRepositoryPort;
import acainfo.back.application.ports.out.ScheduleRepositoryPort;
import acainfo.back.domain.exception.GroupNotFoundException;
import acainfo.back.domain.exception.ScheduleNotFoundException;
import acainfo.back.domain.model.Classroom;
import acainfo.back.domain.model.Group;
import acainfo.back.domain.model.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Service implementing schedule management use cases.
 * Handles creation, updates, deletion, and retrieval of schedules with validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduleService implements ManageScheduleUseCase, GetScheduleUseCase {

    private final ScheduleRepositoryPort scheduleRepository;
    private final GroupRepositoryPort groupRepository;
    private final ScheduleValidationService validationService;

    // ==================== MANAGE OPERATIONS ====================

    @Override
    public Schedule createSchedule(Schedule schedule) {
        log.info("Creating new schedule for group ID: {}", schedule.getGroup().getId());

        // Validate that the group exists and fetch full entity
        Group group = groupRepository.findById(schedule.getGroup().getId())
                .orElseThrow(() -> new GroupNotFoundException(schedule.getGroup().getId()));

        // Set the full group entity
        schedule.setGroup(group);

        // Validate schedule conflicts (teacher and classroom availability)
        validationService.validateNewSchedule(schedule);

        // Save the schedule
        Schedule savedSchedule = scheduleRepository.save(schedule);

        log.info("Schedule created successfully with ID: {}", savedSchedule.getId());
        return savedSchedule;
    }

    @Override
    public Schedule updateSchedule(Long scheduleId, Schedule updatedSchedule) {
        log.info("Updating schedule with ID: {}", scheduleId);

        // Check if schedule exists
        Schedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));

        // Update fields if provided
        if (updatedSchedule.getDayOfWeek() != null) {
            existingSchedule.setDayOfWeek(updatedSchedule.getDayOfWeek());
        }
        if (updatedSchedule.getStartTime() != null) {
            existingSchedule.setStartTime(updatedSchedule.getStartTime());
        }
        if (updatedSchedule.getEndTime() != null) {
            existingSchedule.setEndTime(updatedSchedule.getEndTime());
        }
        if (updatedSchedule.getClassroom() != null) {
            existingSchedule.setClassroom(updatedSchedule.getClassroom());
        }

        // Validate the updated schedule (excluding itself from conflict checks)
        validationService.validateSchedule(existingSchedule, scheduleId);

        // Save the updated schedule
        Schedule savedSchedule = scheduleRepository.save(existingSchedule);

        log.info("Schedule updated successfully: {}", savedSchedule.getId());
        return savedSchedule;
    }

    @Override
    public void deleteSchedule(Long scheduleId) {
        log.info("Deleting schedule with ID: {}", scheduleId);

        // Verify schedule exists
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new ScheduleNotFoundException(scheduleId);
        }

        scheduleRepository.deleteById(scheduleId);
        log.info("Schedule deleted successfully: {}", scheduleId);
    }

    @Override
    public void deleteSchedulesByGroupId(Long groupId) {
        log.info("Deleting all schedules for group ID: {}", groupId);

        // Verify group exists
        if (!groupRepository.existsById(groupId)) {
            throw new GroupNotFoundException(groupId);
        }

        long count = scheduleRepository.countByGroupId(groupId);
        scheduleRepository.deleteByGroupId(groupId);

        log.info("Deleted {} schedule(s) for group ID: {}", count, groupId);
    }

    // ==================== QUERY OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public Schedule getScheduleById(Long scheduleId) {
        log.debug("Fetching schedule with ID: {}", scheduleId);

        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> getAllSchedules() {
        log.debug("Fetching all schedules");

        return scheduleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByGroupId(Long groupId) {
        log.debug("Fetching schedules for group ID: {}", groupId);

        // Verify group exists
        if (!groupRepository.existsById(groupId)) {
            throw new GroupNotFoundException(groupId);
        }

        return scheduleRepository.findByGroupId(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByTeacherId(Long teacherId) {
        log.debug("Fetching schedules for teacher ID: {}", teacherId);

        return scheduleRepository.findByTeacherId(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByClassroom(Classroom classroom) {
        log.debug("Fetching schedules for classroom: {}", classroom);

        return scheduleRepository.findByClassroom(classroom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByDayOfWeek(DayOfWeek dayOfWeek) {
        log.debug("Fetching schedules for day of week: {}", dayOfWeek);

        return scheduleRepository.findByDayOfWeek(dayOfWeek);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesBySubjectId(Long subjectId) {
        log.debug("Fetching schedules for subject ID: {}", subjectId);

        return scheduleRepository.findBySubjectId(subjectId);
    }
}
