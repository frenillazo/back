package com.acainfo.group.infrastructure.adapter.out.persistence.specification;

import com.acainfo.group.application.dto.GroupFilters;
import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.domain.model.GroupType;
import com.acainfo.group.infrastructure.adapter.out.persistence.entity.SubjectGroupJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for SubjectGroupJpaEntity (Criteria Builder).
 * Translates GroupFilters (domain DTO) to JPA Specifications.
 *
 * This class encapsulates the JPA Criteria API complexity,
 * keeping the infrastructure layer isolated from domain layer.
 */
public class GroupSpecifications {

    /**
     * Build dynamic specification from filters.
     * Combines all filter predicates with AND logic.
     *
     * @param filters Domain filters
     * @return JPA Specification with combined predicates
     */
    public static Specification<SubjectGroupJpaEntity> withFilters(GroupFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by subjectId
            if (filters.subjectId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subjectId"), filters.subjectId()));
            }

            // Filter by teacherId
            if (filters.teacherId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("teacherId"), filters.teacherId()));
            }

            // Filter by type
            if (filters.type() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filters.type()));
            }

            // Filter by status
            if (filters.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filters.status()));
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification to find groups by subjectId.
     */
    public static Specification<SubjectGroupJpaEntity> hasSubjectId(Long subjectId) {
        return (root, query, criteriaBuilder) -> {
            if (subjectId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("subjectId"), subjectId);
        };
    }

    /**
     * Specification to find groups by teacherId.
     */
    public static Specification<SubjectGroupJpaEntity> hasTeacherId(Long teacherId) {
        return (root, query, criteriaBuilder) -> {
            if (teacherId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("teacherId"), teacherId);
        };
    }

    /**
     * Specification to find groups by type.
     */
    public static Specification<SubjectGroupJpaEntity> hasType(GroupType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }

    /**
     * Specification to find groups by status.
     */
    public static Specification<SubjectGroupJpaEntity> hasStatus(GroupStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
}
