package acainfo.back.infrastructure.adapters.out;

import acainfo.back.application.ports.out.ScheduleRepositoryPort;
import acainfo.back.domain.model.Classroom;
import acainfo.back.domain.model.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementation for ScheduleRepositoryPort.
 * Delegates to Spring Data JPA ScheduleRepository.
 */
@Component
@RequiredArgsConstructor
public class ScheduleRepositoryAdapter implements ScheduleRepositoryPort {

    private final ScheduleRepository scheduleRepository;

    @Override
    public Schedule save(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @Override
    public Optional<Schedule> findById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId);
    }

    @Override
    public List<Schedule> findAll() {
        return scheduleRepository.findAll();
    }

    @Override
    public List<Schedule> findByGroupId(Long groupId) {
        return scheduleRepository.findByGroupId(groupId);
    }

    @Override
    public List<Schedule> findByTeacherId(Long teacherId) {
        return scheduleRepository.findByTeacherId(teacherId);
    }

    @Override
    public List<Schedule> findByClassroom(Classroom classroom) {
        return scheduleRepository.findByClassroom(classroom);
    }

    @Override
    public List<Schedule> findByDayOfWeek(DayOfWeek dayOfWeek) {
        return scheduleRepository.findByDayOfWeek(dayOfWeek);
    }

    @Override
    public List<Schedule> findBySubjectId(Long subjectId) {
        return scheduleRepository.findBySubjectId(subjectId);
    }

    @Override
    public void deleteById(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }

    @Override
    public void deleteByGroupId(Long groupId) {
        scheduleRepository.deleteByGroupId(groupId);
    }

    @Override
    public boolean existsById(Long scheduleId) {
        return scheduleRepository.existsById(scheduleId);
    }

    @Override
    public long countByGroupId(Long groupId) {
        return scheduleRepository.countByGroupId(groupId);
    }
}
