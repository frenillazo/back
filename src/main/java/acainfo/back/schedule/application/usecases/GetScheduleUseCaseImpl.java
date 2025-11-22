package acainfo.back.schedule.application.usecases;

import acainfo.back.schedule.application.ports.in.GetScheduleUseCase;
import acainfo.back.schedule.application.ports.out.ScheduleRepositoryPort;
import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.domain.model.ScheduleDomain;
import acainfo.back.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Implementation of GetScheduleUseCase
 * Handles all schedule retrieval operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetScheduleUseCaseImpl implements GetScheduleUseCase {

    private final ScheduleRepositoryPort scheduleRepository;

    @Override
    public ScheduleDomain getScheduleById(Long scheduleId) {
        log.debug("Getting schedule by ID: {}", scheduleId);

        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with ID: " + scheduleId
                ));
    }

    @Override
    public List<ScheduleDomain> getAllSchedules() {
        log.debug("Getting all schedules");

        return scheduleRepository.findAll();
    }

    @Override
    public List<ScheduleDomain> getSchedulesByGroupId(Long groupId) {
        log.debug("Getting schedules by group ID: {}", groupId);

        return scheduleRepository.findByGroupId(groupId);
    }

    @Override
    public List<ScheduleDomain> getSchedulesByTeacherId(Long teacherId) {
        log.debug("Getting schedules by teacher ID: {}", teacherId);

        return scheduleRepository.findByTeacherId(teacherId);
    }

    @Override
    public List<ScheduleDomain> getSchedulesByClassroom(Classroom classroom) {
        log.debug("Getting schedules by classroom: {}", classroom);

        return scheduleRepository.findByClassroom(classroom);
    }

    @Override
    public List<ScheduleDomain> getSchedulesByDayOfWeek(DayOfWeek dayOfWeek) {
        log.debug("Getting schedules by day of week: {}", dayOfWeek);

        return scheduleRepository.findByDayOfWeek(dayOfWeek);
    }

    @Override
    public List<ScheduleDomain> getSchedulesBySubjectId(Long subjectId) {
        log.debug("Getting schedules by subject ID: {}", subjectId);

        return scheduleRepository.findBySubjectId(subjectId);
    }
}
