package com.acainfo.session.infrastructure.adapter.out.persistence.specification;

import com.acainfo.session.application.dto.SessionFilters;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
import com.acainfo.session.infrastructure.adapter.out.persistence.entity.SessionJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for SessionJpaEntity (Criteria Builder).
 * Translates SessionFilters (application DTO) to JPA Specifications.
 *
 * This class encapsulates the JPA Criteria API complexity,
 * keeping the infrastructure layer isolated from domain layer.
 */
public class SessionSpecifications {

    private SessionSpecifications() {
        // Utility class
    }

    /**
     * Build dynamic specification from filters.
     * Combines all filter predicates with AND logic.
     *
     * @param filters Application filters
     * @return JPA Specification with combined predicates
     */
    public static Specification<SessionJpaEntity> withFilters(SessionFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by subjectId
            if (filters.subjectId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subjectId"), filters.subjectId()));
            }

            // Filter by groupId
            if (filters.groupId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("groupId"), filters.groupId()));
            }

            // Filter by scheduleId
            if (filters.scheduleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("scheduleId"), filters.scheduleId()));
            }

            // Filter by type
            if (filters.type() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filters.type()));
            }

            // Filter by status
            if (filters.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filters.status()));
            }

            // Filter by mode
            if (filters.mode() != null) {
                predicates.add(criteriaBuilder.equal(root.get("mode"), filters.mode()));
            }

            // Filter by date range (from)
            if (filters.dateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), filters.dateFrom()));
            }

            // Filter by date range (to)
            if (filters.dateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), filters.dateTo()));
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification to find sessions by subjectId.
     */
    public static Specification<SessionJpaEntity> hasSubjectId(Long subjectId) {
        return (root, query, criteriaBuilder) -> {
            if (subjectId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("subjectId"), subjectId);
        };
    }

    /**
     * Specification to find sessions by groupId.
     */
    public static Specification<SessionJpaEntity> hasGroupId(Long groupId) {
        return (root, query, criteriaBuilder) -> {
            if (groupId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("groupId"), groupId);
        };
    }

    /**
     * Specification to find sessions by scheduleId.
     */
    public static Specification<SessionJpaEntity> hasScheduleId(Long scheduleId) {
        return (root, query, criteriaBuilder) -> {
            if (scheduleId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("scheduleId"), scheduleId);
        };
    }

    /**
     * Specification to find sessions by type.
     */
    public static Specification<SessionJpaEntity> hasType(SessionType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }

    /**
     * Specification to find sessions by status.
     */
    public static Specification<SessionJpaEntity> hasStatus(SessionStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Specification to find sessions by mode.
     */
    public static Specification<SessionJpaEntity> hasMode(SessionMode mode) {
        return (root, query, criteriaBuilder) -> {
            if (mode == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("mode"), mode);
        };
    }

    /**
     * Specification to find sessions within a date range.
     */
    public static Specification<SessionJpaEntity> betweenDates(LocalDate from, LocalDate to) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), to));
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification to find sessions on a specific date.
     */
    public static Specification<SessionJpaEntity> onDate(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("date"), date);
        };
    }

    /**
     * Specification to find upcoming sessions (date >= today).
     */
    public static Specification<SessionJpaEntity> upcoming() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("date"), LocalDate.now());
    }

    /**
     * Specification to find past sessions (date < today).
     */
    public static Specification<SessionJpaEntity> past() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("date"), LocalDate.now());
    }
}
