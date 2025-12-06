package com.acainfo.schedule.infrastructure.adapter.out.persistence.specification;

import com.acainfo.schedule.application.dto.ScheduleFilters;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.infrastructure.adapter.out.persistence.entity.ScheduleJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for ScheduleJpaEntity (Criteria Builder).
 * Translates ScheduleFilters (domain DTO) to JPA Specifications.
 *
 * This class encapsulates the JPA Criteria API complexity,
 * keeping the infrastructure layer isolated from domain layer.
 */
public class ScheduleSpecifications {

    /**
     * Build dynamic specification from filters.
     * Combines all filter predicates with AND logic.
     *
     * @param filters Domain filters
     * @return JPA Specification with combined predicates
     */
    public static Specification<ScheduleJpaEntity> withFilters(ScheduleFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by groupId
            if (filters.groupId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("groupId"), filters.groupId()));
            }

            // Filter by classroom
            if (filters.classroom() != null) {
                predicates.add(criteriaBuilder.equal(root.get("classroom"), filters.classroom()));
            }

            // Filter by dayOfWeek
            if (filters.dayOfWeek() != null) {
                predicates.add(criteriaBuilder.equal(root.get("dayOfWeek"), filters.dayOfWeek()));
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification to find schedules by groupId.
     */
    public static Specification<ScheduleJpaEntity> hasGroupId(Long groupId) {
        return (root, query, criteriaBuilder) -> {
            if (groupId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("groupId"), groupId);
        };
    }

    /**
     * Specification to find schedules by classroom.
     */
    public static Specification<ScheduleJpaEntity> hasClassroom(Classroom classroom) {
        return (root, query, criteriaBuilder) -> {
            if (classroom == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("classroom"), classroom);
        };
    }

    /**
     * Specification to find schedules by dayOfWeek.
     */
    public static Specification<ScheduleJpaEntity> hasDayOfWeek(DayOfWeek dayOfWeek) {
        return (root, query, criteriaBuilder) -> {
            if (dayOfWeek == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("dayOfWeek"), dayOfWeek);
        };
    }
}
