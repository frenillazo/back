package com.acainfo.subject.infrastructure.adapter.out.persistence.specification;

import com.acainfo.subject.application.dto.SubjectFilters;
import com.acainfo.subject.domain.model.Degree;
import com.acainfo.subject.domain.model.SubjectStatus;
import com.acainfo.subject.infrastructure.adapter.out.persistence.entity.SubjectJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for SubjectJpaEntity (Criteria Builder).
 * Translates SubjectFilters (domain DTO) to JPA Specifications.
 *
 * This class encapsulates the JPA Criteria API complexity,
 * keeping the infrastructure layer isolated from domain layer.
 */
public class SubjectSpecifications {

    /**
     * Build dynamic specification from filters.
     * Combines all filter predicates with AND logic.
     *
     * @param filters Domain filters
     * @return JPA Specification with combined predicates
     */
    public static Specification<SubjectJpaEntity> withFilters(SubjectFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by exact code (case insensitive)
            if (filters.code() != null && !filters.code().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("code")),
                        filters.code().toLowerCase().trim()
                ));
            }

            // Filter by search term (search in code, name)
            if (filters.searchTerm() != null && !filters.searchTerm().isBlank()) {
                String searchPattern = "%" + filters.searchTerm().toLowerCase().trim() + "%";
                Predicate codeLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")),
                        searchPattern
                );
                Predicate nameLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        searchPattern
                );
                predicates.add(criteriaBuilder.or(codeLike, nameLike));
            }

            // Filter by degree
            if (filters.degree() != null) {
                predicates.add(criteriaBuilder.equal(root.get("degree"), filters.degree()));
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
     * Specification to find subject by code (case insensitive).
     */
    public static Specification<SubjectJpaEntity> hasCode(String code) {
        return (root, query, criteriaBuilder) -> {
            if (code == null || code.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("code")),
                    code.toLowerCase().trim()
            );
        };
    }

    /**
     * Specification to find subjects by degree.
     */
    public static Specification<SubjectJpaEntity> hasDegree(Degree degree) {
        return (root, query, criteriaBuilder) -> {
            if (degree == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("degree"), degree);
        };
    }

    /**
     * Specification to find subjects by status.
     */
    public static Specification<SubjectJpaEntity> hasStatus(SubjectStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Specification to search subjects by term (code, name).
     */
    public static Specification<SubjectJpaEntity> searchByTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + searchTerm.toLowerCase().trim() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern)
            );
        };
    }
}
