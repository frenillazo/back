package com.acainfo.enrollment.infrastructure.adapter.out.persistence.specification;

import com.acainfo.enrollment.application.dto.GroupRequestFilters;
import com.acainfo.enrollment.domain.model.GroupRequestStatus;
import com.acainfo.enrollment.infrastructure.adapter.out.persistence.entity.GroupRequestJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for GroupRequestJpaEntity.
 */
public class GroupRequestSpecifications {

    private GroupRequestSpecifications() {
        // Utility class
    }

    public static Specification<GroupRequestJpaEntity> withFilters(GroupRequestFilters filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.subjectId() != null) {
                predicates.add(cb.equal(root.get("subjectId"), filters.subjectId()));
            }

            if (filters.requesterId() != null) {
                predicates.add(cb.equal(root.get("requesterId"), filters.requesterId()));
            }

            if (filters.status() != null) {
                predicates.add(cb.equal(root.get("status"), filters.status()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<GroupRequestJpaEntity> hasSubjectId(Long subjectId) {
        return (root, query, cb) -> subjectId == null ? cb.conjunction() : cb.equal(root.get("subjectId"), subjectId);
    }

    public static Specification<GroupRequestJpaEntity> hasRequesterId(Long requesterId) {
        return (root, query, cb) -> requesterId == null ? cb.conjunction() : cb.equal(root.get("requesterId"), requesterId);
    }

    public static Specification<GroupRequestJpaEntity> hasStatus(GroupRequestStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<GroupRequestJpaEntity> isPending() {
        return hasStatus(GroupRequestStatus.PENDING);
    }
}
