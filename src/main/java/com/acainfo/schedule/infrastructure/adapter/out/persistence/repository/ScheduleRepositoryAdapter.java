package com.acainfo.schedule.infrastructure.adapter.out.persistence.repository;

import com.acainfo.schedule.application.dto.ScheduleFilters;
import com.acainfo.schedule.application.port.out.ScheduleRepositoryPort;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.schedule.infrastructure.adapter.out.persistence.entity.ScheduleJpaEntity;
import com.acainfo.schedule.infrastructure.adapter.out.persistence.specification.ScheduleSpecifications;
import com.acainfo.schedule.infrastructure.mapper.SchedulePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing ScheduleRepositoryPort.
 * Translates domain operations to JPA operations.
 * Uses SchedulePersistenceMapper to convert between domain and JPA entities.
 */
@Component
@RequiredArgsConstructor
public class ScheduleRepositoryAdapter implements ScheduleRepositoryPort {

    private final JpaScheduleRepository jpaScheduleRepository;
    private final SchedulePersistenceMapper schedulePersistenceMapper;

    @Override
    public Schedule save(Schedule schedule) {
        ScheduleJpaEntity jpaEntity = schedulePersistenceMapper.toJpaEntity(schedule);
        ScheduleJpaEntity savedEntity = jpaScheduleRepository.save(jpaEntity);
        return schedulePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Schedule> findById(Long id) {
        return jpaScheduleRepository.findById(id)
                .map(schedulePersistenceMapper::toDomain);
    }

    @Override
    public Page<Schedule> findWithFilters(ScheduleFilters filters) {
        // Build specification from filters
        Specification<ScheduleJpaEntity> spec = ScheduleSpecifications.withFilters(filters);

        // Build pagination and sorting
        Sort sort = filters.sortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filters.sortBy()).ascending()
                : Sort.by(filters.sortBy()).descending();

        PageRequest pageRequest = PageRequest.of(filters.page(), filters.size(), sort);

        // Execute query and map to domain
        return jpaScheduleRepository.findAll(spec, pageRequest)
                .map(schedulePersistenceMapper::toDomain);
    }

    @Override
    public List<Schedule> findByGroupId(Long groupId) {
        return schedulePersistenceMapper.toDomainList(
                jpaScheduleRepository.findByGroupId(groupId)
        );
    }

    @Override
    public List<Schedule> findByClassroomAndDayOfWeek(Classroom classroom, DayOfWeek dayOfWeek) {
        return schedulePersistenceMapper.toDomainList(
                jpaScheduleRepository.findByClassroomAndDayOfWeek(classroom, dayOfWeek)
        );
    }

    @Override
    public List<Schedule> findConflictingSchedules(
            Classroom classroom,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Long excludeScheduleId
    ) {
        return schedulePersistenceMapper.toDomainList(
                jpaScheduleRepository.findConflictingSchedules(
                        classroom, dayOfWeek, startTime, endTime, excludeScheduleId
                )
        );
    }

    @Override
    public void delete(Long id) {
        jpaScheduleRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaScheduleRepository.existsById(id);
    }
}
