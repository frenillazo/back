package com.acainfo.enrollment.infrastructure.adapter.out.persistence.specification;

import com.acainfo.enrollment.application.dto.GroupRequestFilters;
import com.acainfo.enrollment.domain.model.GroupRequestStatus;
import com.acainfo.enrollment.infrastructure.adapter.out.persistence.entity.GroupRequestJpaEntity;
import com.acainfo.group.domain.model.GroupType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for GroupRequestJpaEntity (Criteria Builder).
 * Translates GroupRequestFilters (application DTO) to JPA Specifications.
 */
public class GroupRequestSpecifications {

    private GroupRequestSpecifications() {
        // Utility class
    }

    /**
     * Build dynamic specification from filters.
     * Combines all filter predicates with AND logic.
     *
     * @param filters Application filters
     * @return JPA Specification with combined predicates
     */
    public static Specification<GroupRequestJpaEntity> withFilters(GroupRequestFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by subjectId
            if (filters.subjectId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subjectId"), filters.subjectId()));
            }

            // Filter by requesterId
            if (filters.requesterId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("requesterId"), filters.requesterId()));
            }

            // Filter by requestedGroupType
            if (filters.requestedGroupType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("requestedGroupType"), filters.requestedGroupType()));
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
     * Specification to find group requests by subjectId.
     */
    public static Specification<GroupRequestJpaEntity> hasSubjectId(Long subjectId) {
        return (root, query, criteriaBuilder) -> {
            if (subjectId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("subjectId"), subjectId);
        };
    }

    /**
     * Specification to find group requests by requesterId.
     */
    public static Specification<GroupRequestJpaEntity> hasRequesterId(Long requesterId) {
        return (root, query, criteriaBuilder) -> {
            if (requesterId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("requesterId"), requesterId);
        };
    }

    /**
     * Specification to find group requests by status.
     */
    public static Specification<GroupRequestJpaEntity> hasStatus(GroupRequestStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Specification to find group requests by type.
     */
    public static Specification<GroupRequestJpaEntity> hasGroupType(GroupType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("requestedGroupType"), type);
        };
    }

    /**
     * Specification to find pending group requests.
     */
    public static Specification<GroupRequestJpaEntity> isPending() {
        return hasStatus(GroupRequestStatus.PENDING);
    }
}
