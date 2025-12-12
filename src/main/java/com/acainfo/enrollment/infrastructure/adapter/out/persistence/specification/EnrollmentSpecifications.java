package com.acainfo.enrollment.infrastructure.adapter.out.persistence.specification;

import com.acainfo.enrollment.application.dto.EnrollmentFilters;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.enrollment.infrastructure.adapter.out.persistence.entity.EnrollmentJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for EnrollmentJpaEntity (Criteria Builder).
 * Translates EnrollmentFilters (application DTO) to JPA Specifications.
 */
public class EnrollmentSpecifications {

    private EnrollmentSpecifications() {
        // Utility class
    }

    /**
     * Build dynamic specification from filters.
     * Combines all filter predicates with AND logic.
     *
     * @param filters Application filters
     * @return JPA Specification with combined predicates
     */
    public static Specification<EnrollmentJpaEntity> withFilters(EnrollmentFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by studentId
            if (filters.studentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("studentId"), filters.studentId()));
            }

            // Filter by groupId
            if (filters.groupId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("groupId"), filters.groupId()));
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
     * Specification to find enrollments by studentId.
     */
    public static Specification<EnrollmentJpaEntity> hasStudentId(Long studentId) {
        return (root, query, criteriaBuilder) -> {
            if (studentId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("studentId"), studentId);
        };
    }

    /**
     * Specification to find enrollments by groupId.
     */
    public static Specification<EnrollmentJpaEntity> hasGroupId(Long groupId) {
        return (root, query, criteriaBuilder) -> {
            if (groupId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("groupId"), groupId);
        };
    }

    /**
     * Specification to find enrollments by status.
     */
    public static Specification<EnrollmentJpaEntity> hasStatus(EnrollmentStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Specification to find active enrollments.
     */
    public static Specification<EnrollmentJpaEntity> isActive() {
        return hasStatus(EnrollmentStatus.ACTIVE);
    }

    /**
     * Specification to find waiting list enrollments.
     */
    public static Specification<EnrollmentJpaEntity> isOnWaitingList() {
        return hasStatus(EnrollmentStatus.WAITING_LIST);
    }
}
