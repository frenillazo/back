package acainfo.back.domain.validation;

import acainfo.back.domain.model.*;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for dynamic Group filtering using Criteria API.
 * Allows combining multiple filter criteria programmatically.
 */
public class GroupSpecifications {

    /**
     * Filter by subject ID.
     */
    public static Specification<Group> hasSubjectId(Long subjectId) {
        return (root, query, criteriaBuilder) -> {
            if (subjectId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Group, Subject> subjectJoin = root.join("subject");
            return criteriaBuilder.equal(subjectJoin.get("id"), subjectId);
        };
    }

    /**
     * Filter by teacher ID.
     */
    public static Specification<Group> hasTeacherId(Long teacherId) {
        return (root, query, criteriaBuilder) -> {
            if (teacherId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Group, User> teacherJoin = root.join("teacher");
            return criteriaBuilder.equal(teacherJoin.get("id"), teacherId);
        };
    }

    /**
     * Filter by group type.
     */
    public static Specification<Group> hasType(GroupType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }

    /**
     * Filter by academic period.
     */
    public static Specification<Group> hasPeriod(AcademicPeriod period) {
        return (root, query, criteriaBuilder) -> {
            if (period == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("period"), period);
        };
    }

    /**
     * Filter by status.
     */
    public static Specification<Group> hasStatus(GroupStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter by availability (has available places).
     */
    public static Specification<Group> hasAvailablePlaces(Boolean hasAvailable) {
        return (root, query, criteriaBuilder) -> {
            if (hasAvailable == null || !hasAvailable) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(
                    criteriaBuilder.lessThan(root.get("currentOccupancy"), root.get("maxCapacity")),
                    criteriaBuilder.equal(root.get("status"), GroupStatus.ACTIVO)
            );
        };
    }

    /**
     * Filter by year (from subject relationship).
     */
    public static Specification<Group> hasYear(Integer year) {
        return (root, query, criteriaBuilder) -> {
            if (year == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Group, Subject> subjectJoin = root.join("subject");
            return criteriaBuilder.equal(subjectJoin.get("year"), year);
        };
    }

    /**
     * Combines all filter criteria into a single Specification.
     * This allows dynamic query building based on provided filters.
     * Note: Classroom filtering has been removed as classrooms are now assigned per schedule, not per group.
     *
     * @param subjectId Filter by subject ID
     * @param teacherId Filter by teacher ID
     * @param type Filter by group type
     * @param period Filter by academic period
     * @param status Filter by group status
     * @param classroom Deprecated parameter (kept for compatibility, ignored)
     * @param hasAvailable Filter groups with available places
     * @param year Filter by year (from subject)
     * @return Combined Specification
     */
    public static Specification<Group> combineFilters(
            Long subjectId,
            Long teacherId,
            GroupType type,
            AcademicPeriod period,
            GroupStatus status,
            Classroom classroom,  // Deprecated but kept for compatibility
            Boolean hasAvailable,
            Integer year
    ) {
        Specification<Group> spec = Specification.where(null);

        if (subjectId != null) {
            spec = spec.and(hasSubjectId(subjectId));
        }
        if (teacherId != null) {
            spec = spec.and(hasTeacherId(teacherId));
        }
        if (type != null) {
            spec = spec.and(hasType(type));
        }
        if (period != null) {
            spec = spec.and(hasPeriod(period));
        }
        if (status != null) {
            spec = spec.and(hasStatus(status));
        }
        // classroom parameter is deprecated and ignored
        if (hasAvailable != null && hasAvailable) {
            spec = spec.and(hasAvailablePlaces(hasAvailable));
        }
        if (year != null) {
            spec = spec.and(hasYear(year));
        }

        return spec;
    }
}
