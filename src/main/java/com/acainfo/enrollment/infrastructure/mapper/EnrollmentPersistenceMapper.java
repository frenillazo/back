package com.acainfo.enrollment.infrastructure.mapper;

import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.infrastructure.adapter.out.persistence.entity.EnrollmentJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Enrollment persistence.
 * Converts between domain model (Enrollment) and JPA entity (EnrollmentJpaEntity).
 */
@Mapper(componentModel = "spring")
public interface EnrollmentPersistenceMapper {

    /**
     * Convert domain Enrollment to JPA entity.
     *
     * @param enrollment Domain model
     * @return JPA entity
     */
    EnrollmentJpaEntity toJpaEntity(Enrollment enrollment);

    /**
     * Convert JPA entity to domain Enrollment.
     *
     * @param entity JPA entity
     * @return Domain model
     */
    Enrollment toDomain(EnrollmentJpaEntity entity);

    /**
     * Convert list of JPA entities to domain models.
     *
     * @param entities List of JPA entities
     * @return List of domain models
     */
    List<Enrollment> toDomainList(List<EnrollmentJpaEntity> entities);

    /**
     * Convert list of domain models to JPA entities.
     *
     * @param enrollments List of domain models
     * @return List of JPA entities
     */
    List<EnrollmentJpaEntity> toJpaEntityList(List<Enrollment> enrollments);
}
