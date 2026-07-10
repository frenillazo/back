package com.acainfo.course.infrastructure.adapter.out.persistence.repository;

import com.acainfo.course.application.dto.CourseFilters;
import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.course.domain.model.CourseStatus;
import com.acainfo.course.domain.model.Course;
import com.acainfo.course.infrastructure.adapter.out.persistence.entity.CourseJpaEntity;
import com.acainfo.course.infrastructure.adapter.out.persistence.specification.CourseSpecifications;
import com.acainfo.course.infrastructure.mapper.CoursePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing CourseRepositoryPort.
 * Translates domain operations to JPA operations.
 * Uses CoursePersistenceMapper to convert between domain and JPA entities.
 */
@Component
@RequiredArgsConstructor
public class CourseRepositoryAdapter implements CourseRepositoryPort {

    private final JpaCourseRepository jpaCourseRepository;
    private final CoursePersistenceMapper coursePersistenceMapper;

    @Override
    public Course save(Course group) {
        CourseJpaEntity jpaEntity = coursePersistenceMapper.toJpaEntity(group);
        CourseJpaEntity savedEntity = jpaCourseRepository.save(jpaEntity);
        return coursePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Course> findById(Long id) {
        return jpaCourseRepository.findById(id)
                .map(coursePersistenceMapper::toDomain);
    }

    @Override
    public Page<Course> findWithFilters(CourseFilters filters) {
        // Build specification from filters
        Specification<CourseJpaEntity> spec = CourseSpecifications.withFilters(filters);

        // Build pagination and sorting
        Sort sort = filters.sortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filters.sortBy()).ascending()
                : Sort.by(filters.sortBy()).descending();

        PageRequest pageRequest = PageRequest.of(filters.page(), filters.size(), sort);

        // Execute query and map to domain
        return jpaCourseRepository.findAll(spec, pageRequest)
                .map(coursePersistenceMapper::toDomain);
    }

    @Override
    public void delete(Long id) {
        jpaCourseRepository.deleteById(id);
    }

    @Override
    public List<Course> findAll() {
        return jpaCourseRepository.findAll().stream()
                .map(coursePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Course> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jpaCourseRepository.findAllById(ids).stream()
                .map(coursePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long countActiveGroupsByTeacherId(Long teacherId) {
        return jpaCourseRepository.countByTeacherIdAndStatusIn(
                teacherId,
                List.of(CourseStatus.OPEN, CourseStatus.CLOSED)
        );
    }

    @Override
    public long countActiveGroupsBySubjectId(Long subjectId) {
        return jpaCourseRepository.countBySubjectIdAndStatusIn(
                subjectId,
                List.of(CourseStatus.OPEN, CourseStatus.CLOSED)
        );
    }

    @Override
    public long countAllBySubjectId(Long subjectId) {
        return jpaCourseRepository.countBySubjectId(subjectId);
    }

    @Override
    public Optional<Course> findByIdForUpdate(Long id) {
        return jpaCourseRepository.findByIdForUpdate(id)
                .map(coursePersistenceMapper::toDomain);
    }
}
