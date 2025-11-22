package acainfo.back.schedule.infrastructure.adapters.out;

import acainfo.back.schedule.application.ports.out.ScheduleRepositoryPort;
import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.domain.model.ScheduleDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementation for ScheduleRepositoryPort.
 * Delegates to Spring Data JPA ScheduleRepository.
 *
 * TODO: This adapter needs to be refactored to use ScheduleJpaMapper
 * to properly convert between ScheduleDomain and ScheduleJpaEntity.
 * Currently it's using the old Schedule entity directly.
 */
@Component
@RequiredArgsConstructor
public class ScheduleRepositoryAdapter implements ScheduleRepositoryPort {

    private final ScheduleRepository scheduleRepository;

    @Override
    public ScheduleDomain save(ScheduleDomain schedule) {
        // TODO: Use ScheduleJpaMapper to convert ScheduleDomain -> ScheduleJpaEntity
        return (ScheduleDomain) (Object) scheduleRepository.save((acainfo.back.schedule.domain.model.Schedule) (Object) schedule);
    }

    @Override
    public Optional<ScheduleDomain> findById(Long scheduleId) {
        // TODO: Use ScheduleJpaMapper to convert ScheduleJpaEntity -> ScheduleDomain
        return scheduleRepository.findById(scheduleId).map(s -> (ScheduleDomain) (Object) s);
    }

    @Override
    public List<ScheduleDomain> findAll() {
        // TODO: Use ScheduleJpaMapper to convert list of ScheduleJpaEntity -> ScheduleDomain
        return (List) scheduleRepository.findAll();
    }

    @Override
    public List<ScheduleDomain> findByGroupId(Long groupId) {
        return (List) scheduleRepository.findByGroupId(groupId);
    }

    @Override
    public List<ScheduleDomain> findByTeacherId(Long teacherId) {
        return (List) scheduleRepository.findByTeacherId(teacherId);
    }

    @Override
    public List<ScheduleDomain> findByClassroom(Classroom classroom) {
        return (List) scheduleRepository.findByClassroom(classroom);
    }

    @Override
    public List<ScheduleDomain> findByDayOfWeek(DayOfWeek dayOfWeek) {
        return (List) scheduleRepository.findByDayOfWeek(dayOfWeek);
    }

    @Override
    public List<ScheduleDomain> findBySubjectId(Long subjectId) {
        return (List) scheduleRepository.findBySubjectId(subjectId);
    }

    @Override
    public void deleteById(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }

    @Override
    public void deleteByGroupId(Long groupId) {
        scheduleRepository.deleteBySubjectGroupId(groupId);
    }

    @Override
    public boolean existsById(Long scheduleId) {
        return scheduleRepository.existsById(scheduleId);
    }

    @Override
    public long countByGroupId(Long groupId) {
        return scheduleRepository.countBySubjectGroupId(groupId);
    }
}
