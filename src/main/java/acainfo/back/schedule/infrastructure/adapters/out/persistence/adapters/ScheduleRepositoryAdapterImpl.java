package acainfo.back.schedule.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.schedule.application.ports.out.ScheduleRepositoryPort;
import acainfo.back.schedule.domain.model.Classroom;
import acainfo.back.schedule.domain.model.ScheduleDomain;
import acainfo.back.schedule.infrastructure.adapters.out.persistence.entities.ScheduleJpaEntity;
import acainfo.back.schedule.infrastructure.adapters.out.persistence.mappers.ScheduleJpaMapper;
import acainfo.back.schedule.infrastructure.adapters.out.persistence.repositories.ScheduleJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Infrastructure Adapter for Schedule persistence
 * Implements ScheduleRepositoryPort
 * Uses ScheduleJpaMapper to convert between Domain and JPA entities
 * Delegates to ScheduleJpaRepository for database operations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleRepositoryAdapterImpl implements ScheduleRepositoryPort {

    private final ScheduleJpaRepository jpaRepository;
    private final ScheduleJpaMapper mapper;

    @Override
    public ScheduleDomain save(ScheduleDomain schedule) {
        log.debug("Saving schedule: {}", schedule);

        ScheduleJpaEntity jpaEntity = mapper.toJpaEntity(schedule);
        ScheduleJpaEntity saved = jpaRepository.save(jpaEntity);

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ScheduleDomain> findById(Long scheduleId) {
        log.debug("Finding schedule by ID: {}", scheduleId);

        return jpaRepository.findById(scheduleId)
                .map(mapper::toDomain);
    }

    @Override
    public List<ScheduleDomain> findAll() {
        log.debug("Finding all schedules");

        return mapper.toDomains(jpaRepository.findAll());
    }

    @Override
    public List<ScheduleDomain> findByGroupId(Long groupId) {
        log.debug("Finding schedules by group ID: {}", groupId);

        return mapper.toDomains(jpaRepository.findByGroupId(groupId));
    }

    @Override
    public List<ScheduleDomain> findByTeacherId(Long teacherId) {
        log.debug("Finding schedules by teacher ID: {}", teacherId);

        return mapper.toDomains(jpaRepository.findByTeacherId(teacherId));
    }

    @Override
    public List<ScheduleDomain> findByClassroom(Classroom classroom) {
        log.debug("Finding schedules by classroom: {}", classroom);

        return mapper.toDomains(jpaRepository.findByClassroom(classroom));
    }

    @Override
    public List<ScheduleDomain> findByDayOfWeek(DayOfWeek dayOfWeek) {
        log.debug("Finding schedules by day of week: {}", dayOfWeek);

        return mapper.toDomains(jpaRepository.findByDayOfWeek(dayOfWeek));
    }

    @Override
    public List<ScheduleDomain> findBySubjectId(Long subjectId) {
        log.debug("Finding schedules by subject ID: {}", subjectId);

        return mapper.toDomains(jpaRepository.findBySubjectId(subjectId));
    }

    @Override
    public void deleteById(Long scheduleId) {
        log.debug("Deleting schedule by ID: {}", scheduleId);

        jpaRepository.deleteById(scheduleId);
    }

    @Override
    public void deleteByGroupId(Long groupId) {
        log.debug("Deleting schedules by group ID: {}", groupId);

        jpaRepository.deleteBySubjectGroupId(groupId);
    }

    @Override
    public boolean existsById(Long scheduleId) {
        log.debug("Checking if schedule exists by ID: {}", scheduleId);

        return jpaRepository.existsById(scheduleId);
    }

    @Override
    public long countByGroupId(Long groupId) {
        log.debug("Counting schedules by group ID: {}", groupId);

        return jpaRepository.countBySubjectGroupId(groupId);
    }
}
