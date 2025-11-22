package acainfo.back.schedule.application.usecases;

import acainfo.back.schedule.application.ports.in.ManageScheduleUseCase;
import acainfo.back.schedule.application.ports.out.ScheduleRepositoryPort;
import acainfo.back.schedule.domain.model.ScheduleDomain;
import acainfo.back.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of ManageScheduleUseCase
 * Handles schedule creation, updates, and deletion operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ManageScheduleUseCaseImpl implements ManageScheduleUseCase {

    private final ScheduleRepositoryPort scheduleRepository;

    @Override
    public ScheduleDomain createSchedule(ScheduleDomain schedule) {
        log.debug("Creating schedule: {}", schedule);

        // Validation is done in domain builder
        // Additional business validations could be added here if needed

        return scheduleRepository.save(schedule);
    }

    @Override
    public ScheduleDomain updateSchedule(Long scheduleId, ScheduleDomain schedule) {
        log.debug("Updating schedule with ID: {}", scheduleId);

        // Verify schedule exists
        ScheduleDomain existing = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with ID: " + scheduleId
                ));

        // Build updated schedule preserving ID and creation date
        ScheduleDomain updated = ScheduleDomain.builder()
                .id(existing.getId())
                .subjectGroupId(schedule.getSubjectGroupId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .classroom(schedule.getClassroom())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        return scheduleRepository.save(updated);
    }

    @Override
    public void deleteSchedule(Long scheduleId) {
        log.debug("Deleting schedule with ID: {}", scheduleId);

        // Verify schedule exists before deleting
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new ResourceNotFoundException(
                    "Schedule not found with ID: " + scheduleId
            );
        }

        scheduleRepository.deleteById(scheduleId);
    }

    @Override
    public void deleteSchedulesByGroupId(Long groupId) {
        log.debug("Deleting all schedules for group ID: {}", groupId);

        scheduleRepository.deleteByGroupId(groupId);
    }
}
