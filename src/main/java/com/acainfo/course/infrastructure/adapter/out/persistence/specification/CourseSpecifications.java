package com.acainfo.course.infrastructure.adapter.out.persistence.specification;

import com.acainfo.course.application.dto.CourseFilters;
import com.acainfo.course.domain.model.CourseStatus;
import com.acainfo.course.infrastructure.adapter.out.persistence.entity.CourseJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for CourseJpaEntity (Criteria Builder).
 */
public class CourseSpecifications {

    public static Specification<CourseJpaEntity> withFilters(CourseFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.subjectId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subjectId"), filters.subjectId()));
            }

            if (filters.teacherId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("teacherId"), filters.teacherId()));
            }

            if (filters.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filters.status()));
            }

            if (filters.searchTerm() != null && !filters.searchTerm().isBlank()) {
                String pattern = "%" + filters.searchTerm().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<CourseJpaEntity> hasSubjectId(Long subjectId) {
        return (root, query, criteriaBuilder) -> {
            if (subjectId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("subjectId"), subjectId);
        };
    }

    public static Specification<CourseJpaEntity> hasTeacherId(Long teacherId) {
        return (root, query, criteriaBuilder) -> {
            if (teacherId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("teacherId"), teacherId);
        };
    }

    public static Specification<CourseJpaEntity> hasStatus(CourseStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
}
