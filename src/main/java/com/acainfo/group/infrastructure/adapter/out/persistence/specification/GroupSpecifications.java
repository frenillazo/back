package com.acainfo.group.infrastructure.adapter.out.persistence.specification;

import com.acainfo.group.application.dto.GroupFilters;
import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.infrastructure.adapter.out.persistence.entity.SubjectGroupJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for SubjectGroupJpaEntity (Criteria Builder).
 */
public class GroupSpecifications {

    public static Specification<SubjectGroupJpaEntity> withFilters(GroupFilters filters) {
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

    public static Specification<SubjectGroupJpaEntity> hasSubjectId(Long subjectId) {
        return (root, query, criteriaBuilder) -> {
            if (subjectId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("subjectId"), subjectId);
        };
    }

    public static Specification<SubjectGroupJpaEntity> hasTeacherId(Long teacherId) {
        return (root, query, criteriaBuilder) -> {
            if (teacherId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("teacherId"), teacherId);
        };
    }

    public static Specification<SubjectGroupJpaEntity> hasStatus(GroupStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
}
