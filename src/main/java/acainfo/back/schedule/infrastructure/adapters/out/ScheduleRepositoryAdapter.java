package acainfo.back.schedule.infrastructure.adapters.out;

import acainfo.back.schedule.application.ports.out.ScheduleRepositoryPort;
import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.domain.model.ScheduleDomain;
import acainfo.back.schedule.infrastructure.adapters.out.persistence.entities.ScheduleJpaEntity;
import acainfo.back.schedule.infrastructure.adapters.out.persistence.mappers.ScheduleJpaMapper;
import acainfo.back.schedule.infrastructure.adapters.out.persistence.repositories.ScheduleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementation for ScheduleRepositoryPort.
 * Delegates to Spring Data JPA ScheduleJpaRepository.
 * Converts between ScheduleDomain (application layer) and ScheduleJpaEntity (infrastructure layer).
 */
@Component
@RequiredArgsConstructor
public class ScheduleRepositoryAdapter implements ScheduleRepositoryPort {

    private final ScheduleJpaRepository scheduleRepository;
    private final ScheduleJpaMapper mapper;

    @Override
    public ScheduleDomain save(ScheduleDomain schedule) {
        ScheduleJpaEntity jpaEntity = mapper.toJpaEntity(schedule);
        ScheduleJpaEntity savedEntity = scheduleRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ScheduleDomain> findById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .map(mapper::toDomain);
    }

    @Override
    public List<ScheduleDomain> findAll() {
        return mapper.toDomains(scheduleRepository.findAll());
    }

    @Override
    public List<ScheduleDomain> findByGroupId(Long groupId) {
        return mapper.toDomains(scheduleRepository.findByGroupId(groupId));
    }

    @Override
    public List<ScheduleDomain> findByTeacherId(Long teacherId) {
        return mapper.toDomains(scheduleRepository.findByTeacherId(teacherId));
    }

    @Override
    public List<ScheduleDomain> findByClassroom(Classroom classroom) {
        return mapper.toDomains(scheduleRepository.findByClassroom(classroom));
    }

    @Override
    public List<ScheduleDomain> findByDayOfWeek(DayOfWeek dayOfWeek) {
        return mapper.toDomains(scheduleRepository.findByDayOfWeek(dayOfWeek));
    }

    @Override
    public List<ScheduleDomain> findBySubjectId(Long subjectId) {
        return mapper.toDomains(scheduleRepository.findBySubjectId(subjectId));
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
