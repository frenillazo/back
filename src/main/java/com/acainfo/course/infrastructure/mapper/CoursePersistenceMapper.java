package com.acainfo.course.infrastructure.mapper;

import com.acainfo.course.domain.model.Course;
import com.acainfo.course.infrastructure.adapter.out.persistence.entity.CourseJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Course persistence.
 * Converts between domain model (Course) and JPA entity (CourseJpaEntity).
 */
@Mapper(componentModel = "spring")
public interface CoursePersistenceMapper {

    /**
     * Convert domain Course to JPA entity.
     *
     * @param group Domain model
     * @return JPA entity
     */
    CourseJpaEntity toJpaEntity(Course group);

    /**
     * Convert JPA entity to domain Course.
     *
     * @param entity JPA entity
     * @return Domain model
     */
    Course toDomain(CourseJpaEntity entity);

    /**
     * Convert list of JPA entities to domain models.
     *
     * @param entities List of JPA entities
     * @return List of domain models
     */
    List<Course> toDomainList(List<CourseJpaEntity> entities);
}
