package acainfo.back.domain.validation;

import acainfo.back.domain.model.Degree;
import acainfo.back.domain.model.Subject;
import acainfo.back.domain.model.SubjectStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification builder for Subject entity filtering.
 * Provides reusable and combinable filter criteria using JPA Criteria API.
 */
public class SubjectSpecifications {

    /**
     * Filter by degree
     */
    public static Specification<Subject> hasDegree(Degree degree) {
        return (root, query, criteriaBuilder) -> {
            if (degree == null) {
                return criteriaBuilder.conjunction(); // No filter
            }
            return criteriaBuilder.equal(root.get("degree"), degree);
        };
    }

    /**
     * Filter by year
     */
    public static Specification<Subject> hasYear(Integer year) {
        return (root, query, criteriaBuilder) -> {
            if (year == null) {
                return criteriaBuilder.conjunction(); // No filter
            }
            return criteriaBuilder.equal(root.get("year"), year);
        };
    }

    /**
     * Filter by semester
     */
    public static Specification<Subject> hasSemester(Integer semester) {
        return (root, query, criteriaBuilder) -> {
            if (semester == null) {
                return criteriaBuilder.conjunction(); // No filter
            }
            return criteriaBuilder.equal(root.get("semester"), semester);
        };
    }

    /**
     * Filter by status
     */
    public static Specification<Subject> hasStatus(SubjectStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction(); // No filter
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Search by code or name (case insensitive)
     */
    public static Specification<Subject> searchByCodeOrName(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isBlank()) {
                return criteriaBuilder.conjunction(); // No filter
            }
            String likePattern = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern)
            );
        };
    }

    /**
     * Filter by active status (status = ACTIVO)
     */
    public static Specification<Subject> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), SubjectStatus.ACTIVO);
    }

    /**
     * Combines multiple specifications with AND logic
     */
    public static Specification<Subject> combineFilters(
            Degree degree,
            Integer year,
            Integer semester,
            SubjectStatus status,
            String search
    ) {
        Specification<Subject> spec = Specification.where(null);

        if (search != null && !search.isBlank()) {
            // If search is provided, use it as primary filter
            spec = spec.and(searchByCodeOrName(search));
        }

        if (degree != null) {
            spec = spec.and(hasDegree(degree));
        }

        if (year != null) {
            spec = spec.and(hasYear(year));
        }

        if (semester != null) {
            spec = spec.and(hasSemester(semester));
        }

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }

        return spec;
    }
}
